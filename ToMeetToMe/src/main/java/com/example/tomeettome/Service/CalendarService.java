package com.example.tomeettome.Service;

import com.example.tomeettome.DTO.TeamDTO;
import com.example.tomeettome.Model.*;
import com.example.tomeettome.Repository.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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
                .icsFileName("TEAM"+team.getLeaderId()+".ics")
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

            if(user == team.getLeaderId()){ // 팀장꺼
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
        schedule.setCategoryOriginKey(categoryEntity.getOriginKey());

        CalendarPermissionEntity calendarPermissionEntity = calendarPermissionRepository.findByUserId(userId);
        schedule.setIcsFileName(calendarPermissionEntity.getIcsFileName());
        return scheduleRepository.save(schedule);
    }

    public ScheduleEntity update(ScheduleEntity entity) {
        final Optional<ScheduleEntity> original = Optional.ofNullable(scheduleRepository.findByUid(entity.getUid()));

        if(original.isEmpty())
            throw new NullPointerException();

        original.ifPresent(schedule ->{
            schedule.setCategoryOriginKey(entity.getCategoryOriginKey()!=null ? entity.getCategoryOriginKey() : schedule.getCategoryOriginKey());
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

}
