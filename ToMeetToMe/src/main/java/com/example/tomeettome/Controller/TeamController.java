package com.example.tomeettome.Controller;

import com.example.tomeettome.DTO.CalendarDTO;
import com.example.tomeettome.DTO.ResponseDTO;
import com.example.tomeettome.DTO.TeamDTO;
import com.example.tomeettome.DTO.TestDTO;
import com.example.tomeettome.Model.CalendarEntity;
import com.example.tomeettome.Model.TeamEntity;
import com.example.tomeettome.Service.CalendarService;
import com.example.tomeettome.Service.TeamService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collections;

@Slf4j
@RestController
@RequestMapping("/team")
public class TeamController {
    @Autowired TeamService teamService;
    @Autowired CalendarService calendarService;

    /**
     *
     * @param dto TeamDTO (이름, 참여자, 팀장, 이미지)
     * @return TeamDTO
     */
    @PostMapping("/create")
    public ResponseEntity<?> create(@RequestBody TeamDTO dto) {
        TeamEntity team = dto.toEntity(dto);
        team = teamService.createTeam(team);
        CalendarEntity calendar = calendarService.creatTeamCalendar(team);
        calendarService.createTeamCalendarPermission(team,dto,calendar);

        TeamDTO teamDTO = TeamDTO.builder()
                .name(team.getName())
                .teamUsers(dto.getTeamUsers())
                .leaderId(dto.getLeaderId())
                .build();

        ResponseDTO<TeamDTO> response = ResponseDTO.<TeamDTO>builder().data(Collections.singletonList(teamDTO)).status("success").build();
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

}
