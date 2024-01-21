package com.example.tomeettome.Service;

import com.example.tomeettome.DTO.TeamDTO;
import com.example.tomeettome.Model.*;
import com.example.tomeettome.Repository.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Slf4j
@Service
public class CalendarService {
    @Autowired CalendarRepository calendarRepository;
    @Autowired CalendarPermissionRepository calendarPermissionRepository;
    @Autowired ScheduleRepository scheduleRepository;
    @Autowired CategoryRepository categoryRepository;

    public CalendarEntity createUserCalendar(UserEntity user) {
        CalendarEntity calendar = CalendarEntity.builder()
                .icsFileName(user.getUserId()+".ics")
                .componentType("VEVENT")
                .build();
        return calendarRepository.save(calendar);
    }

    public CalendarEntity creatTeamCalendar(TeamEntity team) {
        CalendarEntity calendar = CalendarEntity.builder()
                .icsFileName("TEAM"+team.getFounderId()+".ics")
                .componentType("VEVENT")
                .build();
        return calendarRepository.save(calendar);
    }

    public CalendarPermissionEntity createUserCalendarPermission(UserEntity user, CalendarEntity calendar) {
        CalendarPermissionEntity calendarPermission = CalendarPermissionEntity.builder()
                .icsFileName(calendar.getIcsFileName())
                .ownerOriginKey(user.getUid())
                .ownerType("user")
                .permissionLevel("admin")
                .userId(user.getUserId())
                .build();
        return calendarPermissionRepository.save(calendarPermission);
    }

    public CalendarPermissionEntity createTeamCalendarPermission(TeamEntity team, TeamDTO dto, CalendarEntity calendar) {

        for (String user : dto.getTeamUsers()) {
            String permissionLevel = "readOnly";

            if(user == team.getFounderId()){ // 팀장꺼
                permissionLevel = "admin";
            }
            // 나머지
            CalendarPermissionEntity calendarPermission = CalendarPermissionEntity.builder()
                    .icsFileName(calendar.getIcsFileName())
                    .ownerOriginKey(team.getOriginKey())
                    .ownerType("team")
                    .permissionLevel(permissionLevel)
                    .userId(user)
                    .build();
             calendarPermissionRepository.save(calendarPermission);
        }
        return null; // 수정 해야 함
    }

    public ScheduleEntity create(ScheduleEntity schedule, String userId, String categories) {

        CategoryEntity categoryEntity = categoryRepository.findByName(categories);
        schedule.setCategoryUid(categoryEntity.getUid());

        List<CalendarPermissionEntity> calendarPermissionEntities = calendarPermissionRepository.findByUserId(userId);

        CalendarPermissionEntity permission = new CalendarPermissionEntity();

        // CalendarPermission 중에 user 개인의 Calendar를 찾기 위해
        for (CalendarPermissionEntity p : calendarPermissionEntities) {
            if (p.getOwnerType().equals("user")) {
                permission = p;
            }
        }
        schedule.setIcsFileName(permission.getIcsFileName());
        return scheduleRepository.save(schedule);
    }

    public List<ScheduleEntity> retrieveOnlyUser(String userId) {
        List<CalendarPermissionEntity> calendarPermissionEntities = calendarPermissionRepository.findByUserId(userId);
        CalendarPermissionEntity permission = new CalendarPermissionEntity();

        // CalendarPermission 중에 user 개인의 Calendar를 찾기 위해
        for (CalendarPermissionEntity p : calendarPermissionEntities) {
            if (p.getOwnerType().equals("user")) {
                permission = p;
            }
        }
        // user 개인의 Calendar를 찾음
        CalendarEntity calendar = calendarRepository.findByIcsFileName(permission.getIcsFileName());
        // schedule List
        return scheduleRepository.findByIcsFileName(calendar.getIcsFileName());
    }

//    public List<ScheduleEntity> retrieveByDateRange(String userId, LocalDate dtStart, LocalDate dtEnd) {
//        LocalDateTime dtStartDateTime = dtStart.atTime(LocalTime.MIN); // 시작날짜에 00시 00분 00초
//        LocalDateTime dtEndDateTime = dtEnd.atTime(LocalTime.MAX); // 끝날짜에 23시 59분 59초
//
//        return scheduleRepository.findAllByDtStartBetweenAndDtEndBetween(dtStartDateTime, dtEndDateTime);
//    }

    public ScheduleEntity update(ScheduleEntity entity) {
        final Optional<ScheduleEntity> original = Optional.ofNullable(scheduleRepository.findByUid(entity.getUid()));

        if(original.isEmpty())
            throw new NullPointerException();

        original.ifPresent(schedule ->{
            schedule.setCategoryUid(entity.getCategoryUid()!=null ? entity.getCategoryUid() : schedule.getCategoryUid());
            schedule.setSummary(entity.getSummary()!= null ? entity.getSummary() : schedule.getSummary());
            schedule.setDescription(entity.getDescription() != null ? entity.getDescription() : schedule.getDescription());
            schedule.setDtStart(entity.getDtStart() != null ? entity.getDtStart() : schedule.getDtStart());
            schedule.setDtEnd(entity.getDtEnd() != null ? entity.getDtEnd() : schedule.getDtEnd());
            schedule.setLocation(entity.getLocation() != null ? entity.getLocation() : schedule.getLocation());
            schedule.setRRule(entity.getRRule() != null ? entity.getRRule() : schedule.getRRule());
            schedule.setStatus(entity.getStatus() != null ? entity.getStatus() : schedule.getStatus());
            schedule.setAllDay(entity.getAllDay() != null ? entity.getAllDay() : schedule.getAllDay());
            scheduleRepository.save(schedule);
        });
        return scheduleRepository.findByUid(entity.getUid());
    }

    public ScheduleEntity delete(String uid) {
        ScheduleEntity entity = scheduleRepository.findByUid(uid);
        scheduleRepository.delete(entity);
        return entity;
    }

    // Status Toggle
    public ScheduleEntity confirm(String uid) {
        ScheduleEntity entity = scheduleRepository.findByUid(uid);
        if (entity.getStatus().equals("TENTATIVE")) {
            entity.setStatus("CONFIRMED");
        }
        else if (entity.getStatus().equals("CONFIRMED")) {
            entity.setStatus("TENTATIVE");
        }
        return scheduleRepository.findByUid(entity.getUid());
    }
}
