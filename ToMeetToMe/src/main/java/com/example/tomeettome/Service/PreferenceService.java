package com.example.tomeettome.Service;

import com.example.tomeettome.DTO.CaldavDTO;
import com.example.tomeettome.Model.CalendarPermissionEntity;
import com.example.tomeettome.Model.PreferenceEntity;
import com.example.tomeettome.Model.ScheduleEntity;
import com.example.tomeettome.Model.TeamEntity;
import com.example.tomeettome.Repository.CalendarPermissionRepository;
import com.example.tomeettome.Repository.PreferenceRepository;
import com.example.tomeettome.Repository.ScheduleRepository;
import com.example.tomeettome.Repository.TeamRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.sql.Time;
import java.sql.Timestamp;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.AbstractMap.SimpleEntry;


@Slf4j
@Service
public class PreferenceService {
    @Autowired
    private PreferenceRepository preferenceRepository;

    @Autowired CalendarService calendarService;
    @Autowired TeamRepository teamRepository;
    @Autowired CalendarPermissionRepository calendarPermissionRepository;
    @Autowired ScheduleRepository scheduleRepository;

    final long oneHourInMillis = 60 * 60 * 1000;

    public List<PreferenceEntity> create(PreferenceEntity entity, String icsFileName, CaldavDTO.Precondition precondition) {
        long beforeTime = System.currentTimeMillis(); //코드 실행 전에 시간 받아오기

        // 조건 설정 (소요 시간, 기간대, 시간대, 선호 요일)
        // 20231207T090000Z : T를 기준으로 앞이 DayScope, 뒤가 TimeScope
        // 20231219T210000Z
        String [] startScope = precondition.getStartScope().split("T");
        String [] endScope = precondition.getEndScope().split("T");

        // Day Scope Parsing
        LocalDate startDayScope = LocalDate.parse(startScope[0], DateTimeFormatter.ofPattern("yyyyMMdd"));
        LocalDate endDayScope = LocalDate.parse(endScope[0], DateTimeFormatter.ofPattern("yyyyMMdd"));

        int duration = Integer.parseInt(precondition.getDuration().substring(2).split("H")[0]);
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

        // 날짜 사이의 일 수 계산
        // Math.toIntExact : int 범위를 초과하면 ArithmeticException throw, Java 8부터 지원
        // ChronoUnit.DAYS.between은 마지막 날짜를 포함하지 않으므로 +1 해줘야함
        int dayRange = Math.toIntExact(ChronoUnit.DAYS.between(startDayScope, endDayScope)) + 1;

        // 선호 요일을 제외해서 날짜 Parsing
        // 특정 요일을 제외한 날짜들을 얻기
        Set<Integer> includedDays = new HashSet<>();

        // preferredDays가 String 형태로 들어왔기 때문에 parseInt로 바꿔서 넣어줌 -> 에러처리 해야함!
        for(String day : precondition.getPreferredDays()) {
            includedDays.add(Integer.parseInt(day));
        }

        // LocalDate List로 내가 제외할 날짜를 제외해서 약속을 만들 가능성이 있는 날짜를 parsing
        List<LocalDate> datesWithSpecificDays =
                getDatesWithSpecificDays(startDayScope, includedDays, dayRange);

        log.info("datesSpecific = " + datesWithSpecificDays);
        // 가능한 총 갯수 : 날짜 갯수 * 하루에 일정을 잡을 수 있는 갯수
//        int availableCounts = datesWithSpecificDays.size() * timeRange;

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

        // 전체 Team의 UserId를 가져옴
        List<String> userIdList = new ArrayList<>();
        for (CalendarPermissionEntity p : permissions) {
            userIdList.add(p.getUserId());
        }

        schedules.addAll(findSchedulesWithPreferences(preferredDateTimes, userIdList, duration));

        System.out.println("dd");
        // 참석자와 불참석자의 각 List를 담은 class
        class AttendanceRecord {
            List<String> attendee;
            List<String> absentee;

            public AttendanceRecord(List<String> attendee) {
                this.attendee = attendee;
                this.absentee = new ArrayList<>();
            }
        }

//        // 각 Indexing Block, Timestamp 별로 참석자와 불참석자 List를 가짐
//        class AppointmentBlock {
//            Timestamp startTime;
//            AttendanceRecord attendanceRecord;
//        }

        HashMap<Timestamp, AttendanceRecord> appointmentBlock = new HashMap<>();
        HashMap<LocalDate, HashMap<Integer, Integer>> dateStatistics = new HashMap<>();

        TreeMap<LocalDate, String> score = new TreeMap<>();

        // Appointment Block init
        for (int k = 0; k < datesWithSpecificDays.size(); k++) {
            Timestamp startTime = new Timestamp(preferredDateTimes.get(k * 2).getTime());
            for (int i = 0; i < timeRange; i++) {
                List<String> uiList = new ArrayList<>();
                for (String user : userIdList) {
                    uiList.add(user);
                }
                AttendanceRecord attendanceRecord = new AttendanceRecord(uiList);
                appointmentBlock.put(new Timestamp(startTime.getTime()), attendanceRecord);
                startTime.setTime(startTime.getTime() + oneHourInMillis);
            }
        }

        // dateStatistics init
        for (LocalDate date: datesWithSpecificDays){
                HashMap<Integer,Integer> attendanceStatistics = new HashMap<>();
                for (int i = 0; i <= permissions.size(); i++) {
                    if(i == permissions.size()) {
                        attendanceStatistics.put(permissions.size(),timeRange);
                    }
                    else {
                        attendanceStatistics.put(i,0);
                    }
                }
                dateStatistics.put(date,attendanceStatistics);
        }

        for (Timestamp timestamp : appointmentBlock.keySet()) {
            for (ScheduleEntity schedule : schedules) {
                if (isTimestampRangeContained(schedule.getDtStart(), schedule.getDtEnd(), timestamp, new Timestamp(timestamp.getTime() + (oneHourInMillis * duration)))) {
                    if(appointmentBlock.get(timestamp).attendee.remove(getUserIdFromIcsFileName(schedule.getIcsFileName()))) {
                        int prev = appointmentBlock.get(timestamp).attendee.size() + 1;
                        int next = prev - 1;
                        appointmentBlock.get(timestamp).absentee.add(getUserIdFromIcsFileName(schedule.getIcsFileName()));
                        //appointmentBlock.get(timestamp).attendee.remove(schedule.getIcsFileName());
                        //appointmentBlock.get(timestamp).absentee.add(schedule.getIcsFileName());
                        dateStatistics.get(timestamp.toLocalDateTime().toLocalDate()).put(prev, dateStatistics.get(timestamp.toLocalDateTime().toLocalDate()).get(prev)-1);
                        dateStatistics.get(timestamp.toLocalDateTime().toLocalDate()).put(next, dateStatistics.get(timestamp.toLocalDateTime().toLocalDate()).get(next)+1);
                    }
                }
            }
        }

        // 날짜별 appointment block 세팅 완료
        // score 변수 init
        for (LocalDate date: dateStatistics.keySet()) {
            String scoreString = "";
            for (int i = dateStatistics.get(date).size()-1; i >= 0; i--) {
                if(dateStatistics.get(date).get(i) < 10){
                    scoreString += i + "0" + dateStatistics.get(date).get(i).toString();
                }else{
                    scoreString += i + dateStatistics.get(date).get(i).toString();
                }
            }
            score.put(date,scoreString);
        }

        // TreeMap의 entrySet을 리스트로 변환
        List<Map.Entry<LocalDate, String>> entries = new ArrayList<>(score.entrySet());
        // 리스트를 값 기준으로 정렬
        entries.sort(Map.Entry.comparingByValue(Collections.reverseOrder()));

        System.out.println("entries.get(0) = " + entries.get(0));
        System.out.println("entries.get(1) = " + entries.get(1));
        System.out.println("entries.get(2) = " + entries.get(2));

        long afterTime = System.currentTimeMillis(); // 코드 실행 후에 시간 받아오기
        long secDiffTime = (afterTime - beforeTime); //두 시간에 차 계산
        System.out.println("시간차이(m) : "+secDiffTime);
        System.out.println("GG");

        return createByEntries(entries, entity.getOrganizerId(), team.getOriginKey());
    }

    private List<PreferenceEntity> createByEntries(List<Map.Entry<LocalDate, String>> entries,
                                                   String organizerId, String teamOriginKey) {
        List<PreferenceEntity> pList = new ArrayList<>();
        for (int i = 0; i < 3; i++) {
            PreferenceEntity p = new PreferenceEntity();

            p.setDtStart(entries.get(i).getKey().atTime(0,0,0));
            p.setDtEnd(entries.get(i).getKey().atTime(23,59,59));

            p.setStatus("TENTATIVE");
            p.setOrganizerId(organizerId);
            p.setTeamOriginKey(teamOriginKey);

            p.setLikes(0);
            pList.add(p);
            savePreference(p);
        }
        return pList;
    }

    private void savePreference(PreferenceEntity entity) {
        preferenceRepository.save(entity);
    }


    private static String getUserIdFromIcsFileName(String icsFileName) {
        return icsFileName.substring(0, icsFileName.length() - 4);
    }

    public static boolean isTimestampRangeContained(Timestamp startA, Timestamp endA, Timestamp startB, Timestamp endB) {
        // A는 schedule, B는 Indexing Block (내가 칠할 블록)
        // case 1 : IB의 start == schedule의 start
        // case 2 : IB의 end == schedule의 end
        // case 3 : schedule의 end가 IB의 start와 end 사이에 있는 경우, 위로 걸쳐있는 경우
        // case 4 : schedule의 start가 IB의 start와 end 사이에 있는 경우,아래로 걸쳐있는 경우
        // case 5 : schedule의 start, end가 IB에 쏙 들어가 있는 경우, start와 end가 겹쳐지지 않고
        // case 6 : schedule의 start, end가 IB를 완전히 감싸는 경우
        return startA.equals(startB)
                || endA.equals(endB)
                || (endA.after(startB)&&endA.before(endB))
                || (startA.after(startB)&&startA.before(endB))
                || (startB.before(startA) && endB.after(endA))
                || (startA.before(startB)&&endA.after(endB));

    }

    public List<ScheduleEntity> findSchedulesWithPreferences(List<Timestamp> preferredDateTimes, List<String> userIdList, int duration) {
        List<ScheduleEntity> result = new ArrayList<>();

        Specification<ScheduleEntity> timeSpec = null;
        Specification<ScheduleEntity> teamSpec = null;

        log.info("combined " + preferredDateTimes.get(0));

        // preferredDateTimes에는 지금 날짜별로 처음, 끝 scope - duration만 들어 있음

        for (int i = 0; i < preferredDateTimes.size(); i += 2) {
            Timestamp start = preferredDateTimes.get(i);
            Timestamp end = preferredDateTimes.get(i+1);
            end.setTime(end.getTime() + (oneHourInMillis * duration));

            Specification<ScheduleEntity> currentSpec = ScheduleRepository.hasPreferredTimeRange(start, end);
            timeSpec = (timeSpec == null) ? currentSpec : timeSpec.or(currentSpec);
        }

        for (String userId : userIdList) {
            Specification<ScheduleEntity> currentSpec = ScheduleRepository.hasTeam(userId);
            teamSpec = (teamSpec == null) ? currentSpec : teamSpec.or(currentSpec);
        }

        result.addAll(scheduleRepository.findAll(timeSpec.and(teamSpec)));
        log.info("result" + result.size());
        return result;
    }

//    // preference 현황 retrieve
//    public List<PreferenceEntity> retrieve() {
//        preferenceRepository.findBy
//    }


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
