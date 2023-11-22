package com.example.tomeettome.Controller;

import com.example.tomeettome.DTO.*;
import com.example.tomeettome.Model.ScheduleEntity;
import com.example.tomeettome.Service.CalendarService;
import com.example.tomeettome.Service.CategoryService;
import lombok.extern.slf4j.Slf4j;
import net.fortuna.ical4j.data.ParserException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.text.ParseException;
import java.util.Collections;

@Slf4j
@RestController
@RequestMapping("/schedule")
public class ScheduleController {

    @Autowired
    CalendarService calendarService;
    @Autowired
    CategoryService categoryService;

    /**
     * Calendar Component 생성
     * @param userId Authentication Principal
     * @param icsFileName yjzzangman@gmail.com.ics
     * @param component
     * @return
     * @throws ParserException
     * @throws IOException
     * @throws ParseException
     */
    @PutMapping("/create/{icsFileName}")
    public ResponseEntity<?> create(@AuthenticationPrincipal String userId,
                                    @PathVariable("icsFileName") String icsFileName,
                                    @RequestBody String component) throws ParserException, IOException {
        try {
            CaldavDTO dto = new CaldavDTO(component);
            ScheduleEntity entity = dto.toEntity(dto);
            entity.setIcsFileName(icsFileName);
            String categories = dto.getValue(component, "CATEGORIES");

            // 존재하지 않는 카테고리로 스케쥴 생성요청을 보내면 error
            ScheduleEntity schedule = calendarService.create(entity, userId, categories);

            ScheduleDTO scheduleDTO = ScheduleDTO.builder().uid(schedule.getUid()).build();
            ResponseDTO response = ResponseDTO.<ScheduleDTO>builder().data(Collections.singletonList(scheduleDTO)).status("success").build();

            // 201 : Created
            return ResponseEntity.status(201).body(response);
        } catch (NullPointerException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PutMapping("/update/{icsFileName}")
    public ResponseEntity<?> update(@PathVariable("icsFileName") String icsFileName,
                                    @RequestBody String calendarString) throws ParserException, IOException {
        CaldavDTO dto = new CaldavDTO(calendarString);
        ScheduleEntity entity = dto.toEntity(dto);
        entity.setCategoryOriginKey(categoryService.findOriginKeyByName(dto.getValue(calendarString, "CATEGORIES")));
        calendarService.update(entity);

        // 204 : No Content
        return ResponseEntity.status(204).body(null);
    }

    @PutMapping("/delete/{icsFileName}/{uid}")
    public ResponseEntity<?> delete(@PathVariable("icsFileName") String icsFileName,
                                    @PathVariable("uid") String uid) {
        try {
            ScheduleEntity entity = calendarService.delete(uid);

            ResponseDTO response = ResponseDTO.<ScheduleEntity>builder().data(Collections.singletonList(entity)).status("succeed").build();

            return ResponseEntity.status(204).body(response);

        } catch (Exception e) {
            String error = e.getMessage();
            ResponseDTO response = ResponseDTO.<ScheduleEntity>builder().error(error).build();
            return ResponseEntity.badRequest().body(response);
        }
    }
}