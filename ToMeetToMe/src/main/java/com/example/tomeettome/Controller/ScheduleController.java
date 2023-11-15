package com.example.tomeettome.Controller;

import com.example.tomeettome.DTO.ScheduleDTO;
import com.example.tomeettome.Model.ScheduleEntity;
import com.example.tomeettome.Service.CalendarService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/schedule")
public class ScheduleController {

//    @PostMapping("/create")
//    public ResponseEntity<?> createSchedule(@AuthenticationPrincipal String userId, @RequestBody ScheduleDTO dto) {
//        ScheduleEntity schedule = dto.toEntity(dto);
//    }
}
