package com.example.tomeettome.Service;

import com.example.tomeettome.Model.CalendarPermissionEntity;
import com.example.tomeettome.Model.PreferenceEntity;
import com.example.tomeettome.Model.ScheduleEntity;
import com.example.tomeettome.Model.TeamEntity;
import com.example.tomeettome.Repository.CalendarPermissionRepository;
import com.example.tomeettome.Repository.ScheduleRepository;
import com.example.tomeettome.Repository.TeamRepository;
import lombok.extern.slf4j.Slf4j;
import net.bytebuddy.asm.Advice;
import net.fortuna.ical4j.model.WeekDay;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.relational.core.sql.In;
import org.springframework.stereotype.Service;

import java.sql.Date;
import java.sql.Timestamp;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;

@Slf4j
@Service
public class PreferenceService {

    @Autowired CalendarService calendarService;
    @Autowired TeamRepository teamRepository;
    @Autowired CalendarPermissionRepository calendarPermissionRepository;
    @Autowired ScheduleRepository scheduleRepository;

    public PreferenceEntity create(PreferenceEntity entity, List<String> preferredDays, String icsFileName) {

        // 조건 설정 (소요 시간, 기간대, 시간대, 선호 요일)
        // 20231207T090000Z : T를 기준으로 앞이 DayScope, 뒤가 TimeScope
        // 20231219T210000Z
        String [] startScope = entity.getStartScope().split("T");
        String [] endScope = entity.getEndScope().split("T");

        // Day Scope Parsing
        LocalDate startDayScope = LocalDate.parse(startScope[0], DateTimeFormatter.ofPattern("yyyyMMdd"));
        LocalDate endDayScope = LocalDate.parse(endScope[0], DateTimeFormatter.ofPattern("yyyyMMdd"));

        int duration = Integer.parseInt(entity.getDuration().substring(2).split("H")[0]);
        // Time Scope Parsing, 하루에 가능한 블록 개수 : end-duration-start+1
        // 예시로, timeScope=09~21, duration=2라면 마지막 블록이 19~21 블록이므로 이런 계산이 나옴
        int timeRange = Integer.parseInt(endScope[1].substring(0,2))
                - duration - Integer.parseInt(startScope[1].substring(0,2)) + 1;

        List<String> times = new ArrayList<>();
        // 블록 개수가 10개면 가능한 블록들을 시간으로 바꿔서 넣으려면 시작시간부터 블록 개수를 하나씩 늘려가며 더해주면 됨
        // 09시 시작이고 duration은 2, 블록 개수 10개면 List에는 ["09", "10", ..]
        for (int i = 0; i < timeRange; i++) {
            int time = Integer.parseInt(startScope[1].substring(0,2)) + i;
            times.add(String.valueOf(time));
        }

        log.info("times = " + times);
        // 날짜 사이의 일 수 계산
        // Math.toIntExact : int 범위를 초과하면 ArithmeticException throw, Java 8부터 지원
        // ChronoUnit.DAYS.between은 마지막 날짜를 포함하지 않으므로 +1 해줘야함
        int dayRange = Math.toIntExact(ChronoUnit.DAYS.between(startDayScope, endDayScope)) + 1;

        // 선호 요일을 제외해서 날짜 Parsing
        // 특정 요일을 제외한 날짜들을 얻기
        Set<Integer> includedDays = new HashSet<>();

        // preferredDays가 String 형태로 들어왔기 때문에 parseInt로 바꿔서 넣어줌 -> 에러처리 해야함!
        for(String day : preferredDays) {
            includedDays.add(Integer.parseInt(day));
        }

        // LocalDate List로 내가 제외할 날짜를 제외해서 약속을 만들 가능성이 있는 날짜를 parsing
        List<LocalDate> datesWithSpecificDays =
                getDatesWithSpecificDays(startDayScope, includedDays, dayRange);

        log.info("datesSpecific = " + datesWithSpecificDays);
        // 가능한 총 갯수 : 날짜 갯수 * 하루에 일정을 잡을 수 있는 갯수
        int availableCounts = datesWithSpecificDays.size() * timeRange;

        // 팀원들의 일정을 요구 조건에 맞게 추출하는 로직
        // 팀원들 각각 일정을 뽑아야 하니까
        // 팀 ics 파일 이름으로 permission -> team 찾기
        // 이때 permission은 Team에 대한 팀원들의 permission
        List<CalendarPermissionEntity> permissions = calendarPermissionRepository.findByIcsFileName(icsFileName);
        TeamEntity team = teamRepository.findByOriginKey(permissions.get(0).getOwnerOriginKey());

        log.info(team.toString());

        // 각 팀원들의 일정을 day scope, time scope, preferredDays에 맞게 추출
        List<ScheduleEntity> schedules = new ArrayList<>();
        List<Timestamp> preferredDateTimes = new ArrayList<>();

        for (LocalDate date : datesWithSpecificDays) {
            preferredDateTimes.add(formatDateAndTime(date, times.get(0)));
            preferredDateTimes.add(formatDateAndTime(date, times.get(times.size()-1)));
        }

        for (CalendarPermissionEntity p : permissions) {
            schedules.addAll(findSchedulesWithPreferences(preferredDateTimes));
            log.info("Schedules : " + schedules);
        }

//        HashMap<Date, DayData>: 여기서 DayData는 다음과 같은 정보를 저장하는 클래스입니다:
//        HashMap<Timestamp, Integer>: 시간대별 겹치는 인원 수.
//        int minFrequency: 최소값 빈도수.
//        int minValue: 해당 날짜의 최소값.
        class DayInfo {
            HashMap<Timestamp, List<String>> conflictUsersByTime; // 시간대별 겹치는 인원.
            int minFrequency; // 최소값 빈도수 : 각 날짜에 해당하는 최소값의 개수
            int minValue; // 해당 날짜의 최소값

            public DayInfo() {
                this.minFrequency = 0;
                this.minValue = 0;
            }
        }
        HashMap<LocalDate, DayInfo> options = new HashMap<>();

        // 초기 블록 생성
        for (LocalDate date : datesWithSpecificDays) {
            DayInfo dayInfo = new DayInfo();
            dayInfo.conflictUsersByTime = new HashMap<>();
            for (String time : times) {
                dayInfo.conflictUsersByTime.put(formatDateAndTime(date, time), new ArrayList<>());
                options.put(date, dayInfo);
            }
        }

        long oneHourInMillis = 60 * 60 * 1000;
        for (ScheduleEntity schedule : schedules) {

            Timestamp startTime = schedule.getDtStart();
            Timestamp endTime = schedule.getDtEnd();
            
            LocalDate startLocalDate = startTime.toLocalDateTime().toLocalDate();
            LocalDate endLocalDate = endTime.toLocalDateTime().toLocalDate();

            if(endTime.getMinutes() != 0) {
                endTime.setTime(endTime.getTime() + oneHourInMillis);
            }
            int term = endTime.getHours() - startTime.getHours();
            if (duration != 1) term += 1;

            // duration 때문에 위로 올려치기
            if(startTime.getHours() != Integer.parseInt(times.get(0)))
                startTime.setTime(startTime.getTime() -(oneHourInMillis * (duration-1)));

            // 분단위 내림
            startTime.setMinutes(0);

            Timestamp key = startTime;

            // 시나리오
            // 각 블록에 대해 일정이 있는 유저의 userId를 conflictUsersByTime 안의 List<String>에 넣음
            // 처음엔 먼저 들어가는 게 minValue로 지정됨
            // 그다음부턴 min

            DayInfo dayInfo = options.get(startLocalDate);

            dayInfo.conflictUsersByTime.get(startTime).add(schedule.getIcsFileName().substring(0,schedule.getIcsFileName().length()-4));
            dayInfo.minValue = dayInfo.conflictUsersByTime.get(startTime).size();
            dayInfo.minFrequency = 1;
            startTime.setTime(startTime.getTime() + oneHourInMillis );

            for (int i = 1; i < term; i++) {
                //
                // 안되는 user에 userId 추가
                dayInfo.conflictUsersByTime.get(startTime).add(schedule.getIcsFileName().substring(0,schedule.getIcsFileName().length()-4));
                // userId를 추가하고, Date의 최솟값과 최솟값의 빈도수 update
                // dayInfo의 minValue 값 ==  dayInfo의 List Size 와 같을 경우 minFreq++
                // dayInfo의 minValue 값 < dayInfo의 List의 Size
                // dayInfo의 minValue 값 < dayInfo의 List의 Size 경우는 존재하지 않음
                if(dayInfo.minValue == dayInfo.conflictUsersByTime.get(startTime).size()) {
                    dayInfo.minFrequency += 1;
                }
                if(dayInfo.minValue < dayInfo.conflictUsersByTime.get(startTime).size()) {
                    dayInfo.minFrequency -= 1;
                }
                startTime.setTime(startTime.getTime() + oneHourInMillis );
            }
        }

        // minValue가 0이 있는지를 확인하기 위해 Day에 해당하는 반복 수행
        //options에 각 날짜에 해당하는 내용을 확인


        for (LocalDate date : options.keySet()) {
            DayInfo dayInfo = options.get(date);
            for (Timestamp time : dayInfo.conflictUsersByTime.keySet()) {
                if(options.get(date).conflictUsersByTime.get(time).equals(null)) {
                    // minFreq & minValue 수정
                    if(dayInfo.minValue == 0) {
                        dayInfo.minFrequency += 1;
                    }
                    else {
                        dayInfo.minValue = 0;
                        dayInfo.minFrequency = 1;
                    }
                }
            }
        }
        System.out.println("해벌레");
        return null;
    }

    public List<ScheduleEntity> findSchedulesWithPreferences(List<Timestamp> preferredDateTimes) {
        List<ScheduleEntity> result = new ArrayList<>();

        Specification<ScheduleEntity> timeSpec = null;

        log.info("combined " + preferredDateTimes.get(0));

        for (int i = 0; i < preferredDateTimes.size(); i += 2) {
            Specification<ScheduleEntity> currentSpec = ScheduleRepository.hasPreferredTimeRange(preferredDateTimes.get(i), preferredDateTimes.get(i+1));
            timeSpec = (timeSpec == null) ? currentSpec : timeSpec.or(currentSpec);
        }

        result.addAll(scheduleRepository.findAll(timeSpec));
        log.info("result" + result.size());
        return result;
    }

    public Set<DayOfWeek> changeStringToDayOfWeek(List<String> days) {
        // 변환할 Set 초기화
        Set<DayOfWeek> dayOfWeekSet = new HashSet<>();

        // 문자열을 DayOfWeek로 변환하여 Set에 추가
        for (String day : days) {
            int dayNumber = Integer.parseInt(day);
            DayOfWeek dayOfWeek = DayOfWeek.of(dayNumber);
            dayOfWeekSet.add(dayOfWeek);
        }
        return dayOfWeekSet;
    }

    private static List<LocalDate> getDatesWithSpecificDays(LocalDate startDate, Set<Integer> daysToInclude, int range) {
        List<LocalDate> dates = new ArrayList<>();

        int daysChecked = 0;

        while (daysChecked < range) {
            if (daysToInclude.contains(startDate.getDayOfWeek().getValue())) {
                dates.add(startDate);
            }
            daysChecked++;

            startDate = startDate.plusDays(1);
        }

        return dates;
    }

    private static Timestamp formatDateAndTime(LocalDate date, String time) {
        // LocalDate와 시간을 결합하여 LocalDateTime 생성
        LocalDateTime dateTime = date.atTime(Integer.parseInt(time), 0);

        // LocalDateTime을 Timestamp로 변환
        return Timestamp.valueOf(dateTime);
    }

}
