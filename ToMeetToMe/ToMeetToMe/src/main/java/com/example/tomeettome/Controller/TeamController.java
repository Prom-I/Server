package com.example.tomeettome.Controller;

import com.example.tomeettome.DTO.CalendarDTO;
import com.example.tomeettome.DTO.ResponseDTO;
import com.example.tomeettome.DTO.TeamDTO;
import com.example.tomeettome.DTO.TestDTO;
import com.example.tomeettome.Model.CalendarEntity;
import com.example.tomeettome.Model.TeamEntity;
import com.example.tomeettome.Service.CalendarService;
import com.example.tomeettome.Service.NotificationService;
import com.example.tomeettome.Service.TeamService;
import com.google.api.Http;
import com.google.firebase.messaging.Message;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/team")
public class TeamController {

    @Autowired TeamService teamService;
    @Autowired CalendarService calendarService;
    @Autowired NotificationService notificationService;


    /**
     * 팀을 만들 때, 팀원이 한명도 없으면 못 만듦
     * 팀의 이름은 바꿀 수 없음, 팀장은 없고 약속에 대한 관리자는 존재함
     * @param dto TeamDTO (이름, 참여자, 팀장, 이미지)
     * @return TeamDTO
     */
    @PostMapping("/create")
    public ResponseEntity<?> create(@AuthenticationPrincipal String userId,
                                    @RequestBody TeamDTO dto) {

        // 팀원이 자기 자신밖에 없거나 아예 안보내주는 경우 400 Bad Request
        if(dto.getTeamUsers().length <= 1) {
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
                .founderId(dto.getFounderId())
                .build();

        ResponseDTO<TeamDTO> response = ResponseDTO.<TeamDTO>builder().data(Collections.singletonList(teamDTO)).status("success").build();
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /***
     * 팀 초대 API
     * @return
     */
    @PostMapping("/invite/{groupOriginKey}")
    public ResponseEntity<?> invite(@AuthenticationPrincipal String inviterId,
                                    @PathVariable("groupOriginKey") String groupOriginKey,
                                    @RequestBody TeamDTO dto){

        TeamEntity teamEntity =teamService.getTeamEntityByOriginKey(groupOriginKey);
        List<Message> messages =  notificationService.makeInvitesMessages(teamEntity,inviterId,dto.getTeamUsers());
        notificationService.sendNotificatons(messages);

        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    /***
     * 팀 초대 수락 API , 팀 초대 거절은
     * @return
     */
    @PutMapping("/{groupOriginKey}/accept")
    public ResponseEntity<?> acceptInvite(@AuthenticationPrincipal String inviteeId,
                                          @PathVariable("groupOriginKey") String groupOriginKey){

        calendarService.addNewTeamUser(teamService.getTeamEntityByOriginKey(groupOriginKey),inviteeId);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

}
