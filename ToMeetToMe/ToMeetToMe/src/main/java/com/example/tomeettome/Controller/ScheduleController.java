package com.example.tomeettome.Controller;

import com.example.tomeettome.Constant.ERRORMSG;
import com.example.tomeettome.DTO.*;
import com.example.tomeettome.Model.ScheduleEntity;
import com.example.tomeettome.Service.CalendarService;
import com.example.tomeettome.Service.CategoryService;
import com.example.tomeettome.Service.PreferenceService;
import com.example.tomeettome.Service.PromiseService;
import lombok.extern.slf4j.Slf4j;
import net.fortuna.ical4j.data.ParserException;
import net.fortuna.ical4j.model.Property;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.net.URISyntaxException;
import java.text.ParseException;
import java.util.Collections;

@Slf4j
@RestController
@RequestMapping("/schedule")
public class ScheduleController {

    @Autowired CalendarService calendarService;
    @Autowired CategoryService categoryService;
    @Autowired PromiseService promiseService;

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

            ScheduleEntity entity = dto.toScheduleEntity(dto);
            entity.setIcsFileName(icsFileName);
            String categories = dto.getValue(component, Property.CATEGORIES);
            log.info("CATEGORIES : " + categories);
            // 존재하지 않는 카테고리로 스케쥴 생성요청을 보내면 error
            calendarService.create(entity, userId, categories);

            return ResponseEntity.status(HttpStatus.CREATED).body(null);
        } catch (NullPointerException e) {
            return ResponseEntity.badRequest().body(ERRORMSG.NullPointerException.name());
        }
    }

    @PutMapping("/update/{icsFileName}")
    public ResponseEntity<?> update(@PathVariable("icsFileName") String icsFileName,
                                    @RequestBody String calendarString) throws ParserException, IOException {
        try {
            CaldavDTO dto = new CaldavDTO(calendarString);
            ScheduleEntity entity = dto.toScheduleEntity(dto);

            entity.setCategoryUid(categoryService.findIcsFileNameByName(dto.getValue(calendarString, "CATEGORIES")));
            calendarService.update(entity);

            return ResponseEntity.status(HttpStatus.NO_CONTENT).body(null);
        } catch (NullPointerException e) {
            return ResponseEntity.badRequest().body(null);
        }
    }

    @PutMapping("/delete/{icsFileName}/{scheduleUid}")
    public ResponseEntity<?> delete(@PathVariable("icsFileName") String icsFileName,
                                    @PathVariable("scheduleUid") String uid) {
        try {
            ScheduleEntity entity = calendarService.delete(uid);

            ResponseDTO response = ResponseDTO.<ScheduleEntity>builder().data(Collections.singletonList(entity)).status("succeed").build();

            return ResponseEntity.status(HttpStatus.NO_CONTENT).body(response);

        } catch (Exception e) {
            String error = e.getMessage();
            ResponseDTO response = ResponseDTO.<ScheduleEntity>builder().error(error).build();
            return ResponseEntity.badRequest().body(response);
        }
    }

    @PatchMapping("/confirm/{icsFileName}/{scheduleUid}")
    public ResponseEntity<?> confirm(@PathVariable("icsFileName") String icsFileName,
                                      @PathVariable("scheduleUid") String uid) {
        if(calendarService.confirm(uid) != null) {
            if(calendarService.validatePermissionByScheduleUid(icsFileName, uid)) {
                return ResponseEntity.status(HttpStatus.NO_CONTENT).body(null);
            }
            else return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ERRORMSG.Permission_Denied.name());

        }
        else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ERRORMSG.NullPointerException.name());
        }
    }

    @GetMapping("/retrieve/{icsFileName}")
    public String retrieve(@AuthenticationPrincipal String userId,
            @PathVariable("icsFileName") String icsFileName) throws ParseException, URISyntaxException {
        try {
            String target = PreferenceService.getUserIdFromIcsFileName(icsFileName);
            boolean result = calendarService.validatePermission(userId, icsFileName);
            if (result) {
                StringBuilder sb = new StringBuilder();
                sb.append(CaldavDTO.setScheduleValue(calendarService.retrieveOnlyTarget(target)) != null ?
                        CaldavDTO.setScheduleValue(calendarService.retrieveOnlyTarget(target)) : "");

                sb.append(CaldavDTO.setPromiseValue(promiseService.retrieveOnlyConfirmedPromises(target)) != null ?
                        CaldavDTO.setPromiseValue(promiseService.retrieveOnlyConfirmedPromises(target)) : "");

                return sb.toString();
            }
            else {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ERRORMSG.Permission_Denied.name()).toString();
            }
        }
        catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e).toString();
        }


    }

}