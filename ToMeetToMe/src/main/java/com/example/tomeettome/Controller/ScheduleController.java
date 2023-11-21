package com.example.tomeettome.Controller;

import com.example.tomeettome.DTO.*;
import com.example.tomeettome.Model.CalendarEntity;
import com.example.tomeettome.Model.ScheduleEntity;
import com.example.tomeettome.Service.CalendarService;
import com.example.tomeettome.Service.CategoryService;
import lombok.extern.slf4j.Slf4j;
import net.fortuna.ical4j.data.CalendarBuilder;
import net.fortuna.ical4j.data.ParserException;
import net.fortuna.ical4j.model.Calendar;
import net.fortuna.ical4j.model.Property;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.io.StringReader;
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

    @PutMapping("/create")
    public ResponseEntity<?> create(@AuthenticationPrincipal String userId, @RequestBody String calendarString) throws ParserException, IOException, ParseException {
        try {
            CaldavDTO dto = new CaldavDTO(calendarString);
            ScheduleEntity entity = dto.toEntity(dto);
            String categories = dto.getValue(calendarString, "CATEGORIES");
            // 존재하지 않는 카테고리로 스케쥴 생성요청을 보내면 error
            calendarService.create(entity, userId, categories);

            // 201 : Created
            return ResponseEntity.status(201).body(null);
        } catch (NullPointerException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PatchMapping("/{originKey}")
    public ResponseEntity<?> update(@AuthenticationPrincipal String userId,
                                    @PathVariable("originKey") String originKey,
                                    @RequestBody String calendarString) throws ParserException, IOException {
        CaldavDTO dto = new CaldavDTO(calendarString);
        ScheduleEntity entity = dto.toEntity(dto);
        entity.setOriginKey(originKey);
        entity.setCategoryOriginKey(categoryService.findOriginKeyByName(dto.getValue(calendarString, "CATEGORIES")));
        calendarService.update(entity);

        // 204 : No Content
        return ResponseEntity.status(204).body(null);
    }
}