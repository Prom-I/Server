package com.example.tomeettome.Controller;

import com.example.tomeettome.Constant.STATUS;
import com.example.tomeettome.DTO.BlockDTO;
import com.example.tomeettome.DTO.CaldavDTO;
import com.example.tomeettome.Model.AppointmentBlockEntity;
import com.example.tomeettome.Model.PreferenceEntity;
import com.example.tomeettome.Model.PromiseEntity;
import com.example.tomeettome.Service.NotificationService;
import com.example.tomeettome.Service.PreferenceService;
import com.example.tomeettome.Service.PromiseService;
import com.example.tomeettome.Service.VoteService;
import lombok.extern.slf4j.Slf4j;
import net.fortuna.ical4j.data.ParserException;
import net.fortuna.ical4j.model.Property;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.parameters.P;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

import java.io.IOException;
import java.net.URISyntaxException;
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
    @Autowired NotificationService notificationService;
    @Autowired VoteService voteService;
    /**
     *
     * @param userId Preference의 Organizer
     * @param icsFileName Team Calender의 ics File Name
     * @param component 실제 ics File
     * @return
     * @throws ParserException
     * @throws IOException
     */
//    @AuthenticationPrincipal String userId,
    @PutMapping("/create/{icsFileName}")
    public ResponseEntity<String> create(
                                    @PathVariable("icsFileName") String icsFileName,
                                    @RequestBody String component) throws ParserException, IOException, ParseException, URISyntaxException {
        try {
            String userId = "yourUserId1";
            HttpHeaders headers = makeCaldavHeader();

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
                    .status(STATUS.TENTATIVE.name())
                    .icsFileName(icsFileName)
                    .dtStart(startDayScope.atTime(0, 0))
                    .dtEnd(endDayScope.atTime(0, 0))
                    .location(dto.getValue(component, Property.LOCATION))
                    .summary(dto.getValue(component, Property.SUMMARY))
                    .build();

            promise = promiseService.create(promise);
            preference.setPromiseUid(promise.getUid());

            CaldavDTO.Precondition precondition = new CaldavDTO.Precondition(preferredDays, startScope, endScope, duration, promise.getUid());

            return ResponseEntity.status(HttpStatus.CREATED).headers(headers).body(CaldavDTO.setPreferenceValue
                    (preferenceService.create(icsFileName, precondition), promise));
        }
        catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    // 팀을 누르면 팀에서 만들어진 약속들을 보내주는 API
    // 팀의 IcsFileName을 보내줘야 함
    @GetMapping("/retrieve/{icsFileName}")
    public ResponseEntity<?> retrievePromises(@PathVariable("icsFileName") String icsFileName) throws URISyntaxException {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.parseMediaType("text/calendar"));
            headers.add("Content-Disposition", "attachment; filename=calendar.ics");

            List<PromiseEntity> promises = promiseService.retrieve(icsFileName);

            return ResponseEntity.status(HttpStatus.OK).headers(headers).body(CaldavDTO.setPromiseValue(promises));
        }
        catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());

        }
    }

    // Appointment Block을 보내주는 API
    @GetMapping("/retrieve/block/{promiseUid}")
    public ResponseEntity<?> retrieveAppointmentBlocks(@PathVariable("promiseUid") String promiseUid) {
        try {
            List<AppointmentBlockEntity> blocks = promiseService.retrieveAppointmentBlocks(promiseUid);
            List<BlockDTO> blocksDTO = blocks.stream().map(BlockDTO::new).collect(Collectors.toList());
            return ResponseEntity.status(HttpStatus.OK).body(blocksDTO);
        }
        catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    // 약속 추천된 거 3개 + 투표 현황 보내주는 API
    @GetMapping("/retrieve/vote/{promiseUid}")
    public ResponseEntity<String> retrievePreferences(@PathVariable("promiseUid") String promiseUid) throws ParseException, URISyntaxException, IOException {
        try {
            HttpHeaders headers = makeCaldavHeader();

            List<PreferenceEntity> preferences = preferenceService.retrieve(promiseUid);
            PromiseEntity promise = promiseService.findByPromiseUid(promiseUid);
            return ResponseEntity.status(HttpStatus.OK).headers(headers).body(CaldavDTO.setPreferenceValue(preferences, promise));
        }
        catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    // 약속 확정 API
    // CalvDTO 형태로 확정을 약속의 형태를 보내주면
    // 약속의 Status를 바꾸고
    // 약속의 Dtstart와 DtEnd를 수정해야 되고
    // 약속의 참여자 수정
    @PutMapping("/confirm/{teamIcsFileName}")
    public ResponseEntity<String> confirmPromise(@RequestBody String component,
                                                 @PathVariable("teamIcsFileName") String teamIcsFileName) throws ParserException, IOException, URISyntaxException {
        try {
            CaldavDTO dto = new CaldavDTO(component);
            PromiseEntity promise = CaldavDTO.toPromiseEntity(dto);

            boolean result = promiseService.isPromiseConfirmed(promise);

            // 확정이든 수정이든 변경사항 DB에 저장
            promise = promiseService.confirm(promise);

            // 알림만 다르게 해서 날리면 됨
            // result가 true : 약속 수정
            // false : 약속 최초 확정
            notificationService.sendNotificatons(
                    notificationService.makeMessagesByToken(
                            notificationService.makeModifyOrConfirmPromiseNotiDTOList(teamIcsFileName, result)));

            return ResponseEntity.status(HttpStatus.OK).body(CaldavDTO.setPromiseValue(Collections.singletonList(promise)));
        }
        catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());

        }
    }

    // 약속 갱신 API
    // preference 삭제
    // appointment block 삭제
    // vote 삭제
    // refresh 하려면 preferredDays 정보가 있어야 하는데, DB에 저장을 안함 ...
//    @GetMapping("/refresh/{icsFileName}/{promiseUid}")
//    public ResponseEntity<?> refresh(@PathVariable("icsFileName") String icsFileName,
//                                     @PathVariable("promiseUid") String promiseUid,
//                                     @RequestBody String component) throws IOException, ParseException, URISyntaxException {
//        cleanupPromiseData(promiseUid);
//        HttpHeaders headers = makeCaldavHeader();
//
//        PromiseEntity promise = promiseService.findByPromiseUid(promiseUid);
//
//        CaldavDTO.Precondition precondition = new CaldavDTO.Precondition(preferredDays,startScope,endScope,duration, promise.getUid());
//
//        return ResponseEntity.status(HttpStatus.OK).headers(headers).body(CaldavDTO.setPreferenceValue
//                (preferenceService.create(icsFileName, precondition), promise));
//    }

    // 약속 삭제 API
    // Promise 테이블 삭제 -> promiseService 4
    // 관련된 Vote 테이블 삭제 -> voteService 3
    // 관련된 Preference 테이블 삭제 -> preferenceService 2
    // 관련된 AppointmentBlock 테이블 삭제 -> preferenceService 1
    @DeleteMapping("/{promiseUid}")
    public ResponseEntity<?> deletePromise(@PathVariable("promiseUid") String promiseUid) {
        try {
            preferenceService.deletePreferences(promiseUid);
            cleanupPromiseData(promiseUid);
            return ResponseEntity.status(HttpStatus.NO_CONTENT).body(null);
        }
        catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    // 직접 선택으로 만든 약속 추천 생성 API
    @PutMapping("/create/custom/{promiseUid}")
    public ResponseEntity<?> createCustomPreference(@AuthenticationPrincipal String userId,
                                                    @RequestBody String component,
                                                    @PathVariable("promiseUid") String promiseUid) throws ParserException, IOException {
        try {
            CaldavDTO dto = new CaldavDTO(component);
            PreferenceEntity preference = dto.toPreferenceEntity(dto);
            preference.setPromiseUid(promiseUid);
            preference.setLikes(0);
            boolean duplicateTest = preferenceService.duplicateTest(preference);
            boolean permissionTest = preferenceService.permissionTest(preference.getPromiseUid(), userId);
            if (duplicateTest && permissionTest) {
                preferenceService.savePreference(preference);
                return ResponseEntity.status(HttpStatus.CREATED).body(null);
            } else {
                log.error("preference already exist or, validation failed");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
            }
        }
        catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    public void cleanupPromiseData(String promiseUid) {
        preferenceService.deleteAppointmentBlocks(promiseUid);
        voteService.deleteVotes(promiseUid);
        promiseService.deletePromise(promiseUid);
    }

    private HttpHeaders makeCaldavHeader() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType("text/calendar"));
        headers.add("Content-Disposition", "attachment; filename=calendar.ics");
        return headers;
    }

}


