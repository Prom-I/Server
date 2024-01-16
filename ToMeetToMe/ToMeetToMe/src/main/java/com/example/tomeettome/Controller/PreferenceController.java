package com.example.tomeettome.Controller;

import com.example.tomeettome.DTO.BlockDTO;
import com.example.tomeettome.DTO.CaldavDTO;
import com.example.tomeettome.DTO.CategoryDTO;
import com.example.tomeettome.Model.AppointmentBlockEntity;
import com.example.tomeettome.Model.PreferenceEntity;
import com.example.tomeettome.Model.PromiseEntity;
import com.example.tomeettome.Service.PreferenceService;
import com.example.tomeettome.Service.PromiseService;
import lombok.extern.slf4j.Slf4j;
import net.fortuna.ical4j.data.ParserException;
import net.fortuna.ical4j.model.Property;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cglib.core.Block;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

import java.io.IOException;
import java.net.URISyntaxException;
import java.sql.Timestamp;
import java.text.ParseException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/preference")
public class PreferenceController {

    @Autowired PreferenceService preferenceService;
    @Autowired PromiseService promiseService;
    /**
     *
     * @param userId Preference의 Organizer
     * @param icsFileName Team Calender의 ics File Name
     * @param component 실제 ics File
     * @return
     * @throws ParserException
     * @throws IOException
     */

    @PutMapping("/create/{icsFileName}")
    public ResponseEntity<String> create(@AuthenticationPrincipal String userId,
                                    @PathVariable("icsFileName") String icsFileName,
                                    @RequestBody String component) throws ParserException, IOException, ParseException, URISyntaxException {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType("text/calendar"));
        headers.add("Content-Disposition", "attachment; filename=calendar.ics");

        CaldavDTO dto = new CaldavDTO(component);
        PreferenceEntity preference = dto.toPreferenceEntity(dto);
        List<String> preferredDays = List.of(dto.getValue(component, Property.EXPERIMENTAL_PREFIX + "PREFERREDDAYS").split(","));
        String startScope = dto.getValue(component, Property.EXPERIMENTAL_PREFIX + "STARTSCOPE");
        String endScope = dto.getValue(component, Property.EXPERIMENTAL_PREFIX + "ENDSCOPE");
        String duration = dto.getValue(component, Property.DURATION);

        LocalDate startDayScope = LocalDate.parse(startScope.split("T")[0], DateTimeFormatter.ofPattern("yyyyMMdd"));
        LocalDate endDayScope = LocalDate.parse(endScope.split("T")[0], DateTimeFormatter.ofPattern("yyyyMMdd"));

        // Promise가 처음 만들어지는 것이므로 dtStart, dtEnd는 처음 설정한 scope로, time은 0시 0분으로
        PromiseEntity promise = PromiseEntity.builder()
                .organizerId(userId)
                .status("TENTATIVE")
                .icsFileName(icsFileName)
                .dtStart(startDayScope.atTime(0,0))
                .dtEnd(endDayScope.atTime(0,0))
                .location(dto.getValue(component, Property.LOCATION))
                .summary(dto.getValue(component, Property.SUMMARY))
                .build();

        promise = promiseService.create(promise);
        preference.setPromiseUid(promise.getUid());

        CaldavDTO.Precondition precondition = new CaldavDTO.Precondition(preferredDays,startScope,endScope,duration, promise.getUid());

        return ResponseEntity.ok().headers(headers).body(CaldavDTO.setPreferenceValue(preferenceService.create(preference, icsFileName,precondition), promise));
    }

    // 팀을 누르면 팀에서 만들어진 약속들을 보내주는 API
    // 팀의 IcsFileName을 보내줘야 함
    @GetMapping("/retrieve/{icsFileName}")
    public ResponseEntity<?> retrievePromises(@PathVariable("icsFileName") String icsFileName) throws URISyntaxException {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType("text/calendar"));
        headers.add("Content-Disposition", "attachment; filename=calendar.ics");

        List<PromiseEntity> promises = promiseService.retrieve(icsFileName);

        return ResponseEntity.ok().headers(headers).body(CaldavDTO.setPromiseValue(promises));
    }

    // Appointment Block을 보내주는 API
    @GetMapping("/retrieve/block/{promiseUid}")
    public ResponseEntity<?> retrieveAppointmentBlocks(@PathVariable("promiseUid") String promiseUid) {
            List<AppointmentBlockEntity> blocks = promiseService.retrieveAppointmentBlocks(promiseUid);
            List<BlockDTO> blocksDTO = blocks.stream().map(BlockDTO::new).collect(Collectors.toList());

            return ResponseEntity.ok().body(blocksDTO);
    }

    // 약속 추천된 거 3개 + 투표 현황 보내주는 API
    @GetMapping("/retrieve/vote/{promiseUid}")
    public ResponseEntity<String> retrievePreferences(@PathVariable("promiseUid") String promiseUid) throws ParseException, URISyntaxException, IOException {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType("text/calendar"));
        headers.add("Content-Disposition", "attachment; filename=calendar.ics");

        List<PreferenceEntity> preferences = preferenceService.retrieve(promiseUid);
        PromiseEntity promise = promiseService.findByPromiseUid(promiseUid);
        return ResponseEntity.ok().headers(headers).body(CaldavDTO.setPreferenceValue(preferences, promise));
    }

    // 약속 확정 API
    // CalvDTO 형태로 확정을 약속의 형태를 보내주면
    // 약속의 Status를 바꾸고
    // 약속의 Dtstart와 DtEnd를 수정해야 되고
    // 약속의 참여자 수정
    // Appointment Block 삭제해야 되고
    // Preference 3개 지우고
    // Vote도 지우고

    // 또 ?
    @PutMapping("/confirm")
    public ResponseEntity<String> confirmPromise(@RequestBody String component) throws ParserException, IOException, URISyntaxException {
        CaldavDTO dto = new CaldavDTO(component);
        PromiseEntity promise = CaldavDTO.toPromiseEntity(dto);
        promise = promiseService.confirm(promise);

        return ResponseEntity.ok().body(CaldavDTO.setPromiseValue(Collections.singletonList(promise)));
    }

    // 약속 갱신 API

    // 약속 삭제 API

    // 직접 선택으로 만든 약속 생성 API
}


