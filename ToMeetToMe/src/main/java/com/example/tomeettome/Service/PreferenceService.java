package com.example.tomeettome.Service;

import com.example.tomeettome.Model.CalendarPermissionEntity;
import com.example.tomeettome.Model.PreferenceEntity;
import com.example.tomeettome.Model.ScheduleEntity;
import com.example.tomeettome.Model.TeamEntity;
import com.example.tomeettome.Repository.CalendarPermissionRepository;
import com.example.tomeettome.Repository.TeamRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
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

    public PreferenceEntity create(PreferenceEntity entity, List<String> preferredDays) {

        // 조건 설정 (소요 시간, 기간대, 시간대, 선호 요일)
        // 20231207
        // 20231219
        LocalDate startDayScope = LocalDate.parse(entity.getStartDayScope().split("T")[0], DateTimeFormatter.ofPattern("yyyyMMdd"));
        LocalDate endDayScope = LocalDate.parse(entity.getEndDayScope().split("T")[0], DateTimeFormatter.ofPattern("yyyyMMdd"));

        // 날짜 사이의 일 수 계산
        // Math.toIntExact : int 범위를 초과하면 ArithmeticException throw, Java 8부터 지원
        // ChronoUnit.DAYS.between은 마지막 날짜를 포함하지 않으므로 +1 해줘야함
        int daysBetween = Math.toIntExact(ChronoUnit.DAYS.between(startDayScope, endDayScope)) + 1;

        // 선호 요일을 제외해서 날짜 Parsing

        // 가능한 총 갯수 : 날짜 갯수 * 하루에 일정을 잡을 수 있는 갯수


        // 팀원들 각각 일정을 뽑아야 하니까
        TeamEntity team = teamRepository.findByOriginKey(entity.getTeamOriginKey());
        List<CalendarPermissionEntity> permissions = calendarPermissionRepository.findByOwnerOriginKey(team.getOriginKey());

        for (CalendarPermissionEntity p : permissions) {
            List<ScheduleEntity> schedules = calendarService.retrieveOnlyUser(p.getUserId());

        }

    }


//    public class DatesWithoutSpecificDays {
//
//        public static void main(String[] args) {
//            String dateString = "20231207";
//            LocalDate date = LocalDate.parse(dateString, DateTimeFormatter.ofPattern("yyyyMMdd"));
//
//            // 특정 요일을 제외한 날짜들을 얻기
//            Set<Integer> excludedDays = new HashSet<>();
//            excludedDays.add(1); // 월요일 제외
//            excludedDays.add(7); // 일요일 제외
//
//            List<LocalDate> datesWithoutSpecificDays = getDatesWithoutSpecificDays(date, excludedDays);
//
//            // 결과 출력
//            System.out.println("Original Date: " + date);
//            System.out.println("Dates without specific days: " + datesWithoutSpecificDays);
//        }
//
//        private static List<LocalDate> getDatesWithoutSpecificDays(LocalDate startDate, Set<Integer> daysToExclude) {
//            List<LocalDate> dates = new ArrayList<>();
//
//            while (daysToExclude.contains(startDate.getDayOfWeek().getValue())) {
//                startDate = startDate.plusDays(1);
//            }
//
//            while (!daysToExclude.contains(startDate.getDayOfWeek().getValue())) {
//                dates.add(startDate);
//                startDate = startDate.plusDays(1);
//            }
//
//            return dates;
//        }
//    }
}
