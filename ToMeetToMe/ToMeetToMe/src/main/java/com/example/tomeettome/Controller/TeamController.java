package com.example.tomeettome.Controller;

import com.example.tomeettome.DTO.*;
import com.example.tomeettome.Model.*;
import com.example.tomeettome.Repository.CalendarPermissionRepository;
import com.example.tomeettome.Service.*;
import com.google.firebase.messaging.Message;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import javax.persistence.EntityNotFoundException;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/team")
public class TeamController {
    @Autowired
    private CalendarPermissionRepository calendarPermissionRepository;

    @Autowired TeamService teamService;
    @Autowired CalendarService calendarService;
    @Autowired PromiseService promiseService;
    @Autowired PreferenceService preferenceService;
    @Autowired NotificationService notificationService;
    @Autowired PreferenceController preferenceController;

    /**
     * 팀을 만들 때, 팀원이 한명도 없으면 못 만듦
     * 팀의 이름은 바꿀 수 없음, 팀장은 없고 약속에 대한 관리자는 존재함
     * @param dto TeamDTO (이름, 참여자, 팀장, 이미지)
     * @return TeamDTO
     */

    @PostMapping("/create")
    public ResponseEntity<?> create(@AuthenticationPrincipal String userId,
                                    @RequestBody TeamDTO dto) {
        try {
            // 팀원이 자기 자신밖에 없거나 아예 안보내주는 경우 400 Bad Request
            if (dto.getTeamUsers().length <= 1) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
            }
            TeamEntity team = dto.toEntity(dto);

            // FounderId 세팅
            team.setFounderId(userId);

            team = teamService.createTeam(team);
            CalendarEntity calendar = calendarService.creatTeamCalendar(team);
            calendarService.createTeamCalendarPermission(team, dto, calendar);

            TeamDTO teamDTO = TeamDTO.builder()
                    .name(team.getName())
                    .teamUsers(dto.getTeamUsers())
                    .founderId(team.getFounderId())
                    .image(team.getImage())
                    .build();

            ResponseDTO<TeamDTO> response = ResponseDTO.<TeamDTO>builder().data(Collections.singletonList(teamDTO)).status("success").build();
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        }
        catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    /***
     * 팀 초대 API
     * @return
     */
    @PostMapping("/invite/{teamIcsFileName}")
    public ResponseEntity<?> invite(@AuthenticationPrincipal String inviterId,
                                    @PathVariable("teamIcsFileName") String teamIcsFileName,
                                    @RequestBody TeamDTO dto){
        try {
            String ownerOriginKey = calendarService.getOwnerOriginKeyByIcsFileName(teamIcsFileName);
            TeamEntity teamEntity = teamService.retrieveTeamEntity(ownerOriginKey);
            List<Message> messages = notificationService.makeInvitesMessages(teamEntity, inviterId, dto.getTeamUsers());
            notificationService.sendNotificatons(messages);

            return ResponseEntity.status(HttpStatus.CREATED).body(null);
        }
        catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    /***
     * 팀 초대 수락 API , 팀 초대 거절은
     * @return
     */
    @PutMapping("/accept/{teamIcsFileName}")
    public ResponseEntity<?> acceptInvite(@AuthenticationPrincipal String inviteeId,
                                          @PathVariable("teamIcsFileName") String teamIcsFileName){
        try {
            String ownerOriginKey = calendarService.getOwnerOriginKeyByIcsFileName(teamIcsFileName);
            calendarService.addNewTeamUser(teamService.retrieveTeamEntity(ownerOriginKey), inviteeId, teamIcsFileName);
            teamService.increaseNumOfUsers(ownerOriginKey);

            return ResponseEntity.status(HttpStatus.CREATED).body(null);
        }
        catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    @GetMapping("/retrieve")
    public ResponseEntity<?> retrieveTeamByUserId(@AuthenticationPrincipal String userId){
        try {
            List<TeamEntity> teamEntity = teamService.retrieveTeamEntities(userId);
            List<TeamDTO> dtos = teamEntity.stream().map(TeamDTO::new).collect(Collectors.toList());
            ResponseDTO<TeamDTO> response = ResponseDTO.<TeamDTO>builder().data(dtos).status("success").build();

            return ResponseEntity.status(HttpStatus.OK).body(response);
        }
        catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());

        }
    }

    @PatchMapping("/leave/{teamIcsFileName}") // 팀 탈퇴
    public ResponseEntity<?> leaveTeam(@AuthenticationPrincipal String userId,
                                       @PathVariable("teamIcsFileName") String teamIcsFileName){
        try {
            String ownerOriginKey = calendarService.getOwnerOriginKeyByIcsFileName(teamIcsFileName);
            teamService.deleteTeamUser(ownerOriginKey, UserDTO.builder().userId(userId).build());

            if(teamService.isTeamEmptyByIcsFileName(teamIcsFileName))
                deleteTeam(teamIcsFileName, ownerOriginKey);

            return ResponseEntity.status(HttpStatus.OK).body(null);
        } catch (Exception e){
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    @GetMapping("retrieve/users/{teamIcsFileName}")
    public ResponseEntity<?> retrieveUsers(@PathVariable("teamIcsFileName") String teamIcsFileName) {
        try {
            List<UserEntity> entities = teamService.retrieveTeamUsers(teamIcsFileName);
            List<UserDTO> dtos = entities.stream().map(UserDTO::new).collect(Collectors.toList());
            ResponseDTO<UserDTO> response = ResponseDTO.<UserDTO>builder().data(dtos).status("success").build();

            return ResponseEntity.status(HttpStatus.OK).body(response);
        }
        catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    // 팀 삭제
    public void deleteTeam(String teamIcsFileName, String teamUid){
        // promise Uid 리스트 뽑아서 cleanup
        List<PromiseEntity> promises = promiseService.retrieve(teamIcsFileName);
        for (PromiseEntity pm : promises) {
            preferenceController.cleanupPromiseData(pm.getUid());
        }

        // TeamCalendar 삭제
        calendarService.deleteTeamCalendar(teamIcsFileName);

        // TeamEntity 삭제
        teamService.deleteTeam(teamUid);
    }
}
