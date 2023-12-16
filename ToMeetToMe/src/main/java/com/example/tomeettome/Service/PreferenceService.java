package com.example.tomeettome.Service;

import com.example.tomeettome.Model.CalendarPermissionEntity;
import com.example.tomeettome.Model.PreferenceEntity;
import com.example.tomeettome.Model.ScheduleEntity;
import com.example.tomeettome.Model.TeamEntity;
import com.example.tomeettome.Repository.CalendarPermissionRepository;
import com.example.tomeettome.Repository.ScheduleRepository;
import com.example.tomeettome.Repository.TeamRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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

        LocalTime startTimeScope = LocalTime.parse(startScope[1].substring(0,6), DateTimeFormatter.ofPattern("HHmmss"));
        LocalTime endTimeScope = LocalTime.parse(endScope[1].substring(0,6), DateTimeFormatter.ofPattern("HHmmss"));

        // Time Scope Parsing, 하루에 가능한 블록 개수 : end-duration-start+1
        // 예시로, timeScope=09~21, duration=2라면 마지막 블록이 19~21 블록이므로 이런 계산이 나옴
        int timeRange = Integer.parseInt(endScope[1].substring(0,2))
                - Integer.parseInt(entity.getDuration().substring(2).split("H")[0]) - Integer.parseInt(startScope[1].substring(0,2)) + 1;

        List<String> times = new ArrayList<>();
        // 블록 개수가 10개면 가능한 블록들을 시간으로 바꿔서 넣으려면 시작시간부터 블록 개수를 하나씩 늘려가며 더해주면 됨
        // 09시 시작이고 duration은 2, 블록 개수 10개면 List에는 ["09", "10", ..]
        for (int i = 0; i < timeRange; i++) {
            int time = Integer.parseInt(startScope[1].substring(0,2)) + i;
            times.add(String.valueOf(time));
        }

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

        // 가능한 총 갯수 : 날짜 갯수 * 하루에 일정을 잡을 수 있는 갯수
        int availableCounts = datesWithSpecificDays.size() * timeRange;

        // 팀원들의 일정을 요구 조건에 맞게 추출하는 로직
        // 팀원들 각각 일정을 뽑아야 하니까
        List<CalendarPermissionEntity> permissions = calendarPermissionRepository.findByIcsFileName(icsFileName);
        TeamEntity team = teamRepository.findByOriginKey(permissions.get(0).getOwnerOriginKey());

        log.info(team.toString());

        // 각 팀원들의 일정을 day scope, time scope, preferredDays에 맞게 추출
        List<ScheduleEntity> schedules = new ArrayList<>();

        calendarPermissionRepository.findByIcsFileName(icsFileName);

        for (CalendarPermissionEntity p : permissions) {
            schedules.addAll(findSchedulesWithPreferences(p.getIcsFileName(), changeStringToDayOfWeek(preferredDays),startTimeScope, endTimeScope));
        }

        // 1207T09 ~ 1219T21, duration 2
        // 20231207T09, 20231207T10 ,,, ~ 20231219T19
        // 1207 09-11 start 또는 end 걸리면 count ++

        // 20231207T09 key (날짜T시작시간)
        // 1 value (possibleUserCount)

        // duration 단위로 블록화
        for (ScheduleEntity s : schedules) {
            String dtStart = s.getDtStart(); //20231207T101000Z
            String dtEnd = s.getDtEnd(); // 20231207T152500Z

        }
        // TreeMap에 넣고 최소값 추출
        return null;
    }

    public List<ScheduleEntity> findSchedulesWithPreferences(String icsFileName, Set<DayOfWeek> preferredDays, LocalTime startTime, LocalTime endTime) {
        List<ScheduleEntity> entities = scheduleRepository.findByIcsFileName(icsFileName);
        List<ScheduleEntity> result = new ArrayList<>();
        for (ScheduleEntity e : entities) {
            Specification<ScheduleEntity> spec = Specification.where(ScheduleRepository.hasPreferredDays(preferredDays, e))
                    .and(ScheduleRepository.hasPreferredTimeRange(startTime, endTime));
            result.addAll(scheduleRepository.findAll(spec));
        }
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
}
