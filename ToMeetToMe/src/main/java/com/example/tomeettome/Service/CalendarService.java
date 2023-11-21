package com.example.tomeettome.Service;

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
                .componentType("VEVENT")
                .build();
        return calendarRepository.save(calendar);
    }
    public CalendarPermissionEntity createUserCalendarPermission(UserEntity user, CalendarEntity calendar) {
        CalendarPermissionEntity calendarPermission = CalendarPermissionEntity.builder()
                .calendarOriginKey(calendar.getOriginKey())
                .ownerOriginKey(user.getOriginKey())
                .ownerType("user")
                .permissionLevel("admin")
                .userId(user.getUserId())
                .build();
        return calendarPermissionRepository.save(calendarPermission);
    }

    public ScheduleEntity create(ScheduleEntity schedule, String userId, String categories) {

        CategoryEntity categoryEntity = categoryRepository.findByName(categories);
        schedule.setCategoryOriginKey(categoryEntity.getOriginKey());

        CalendarPermissionEntity calendarPermissionEntity = calendarPermissionRepository.findByUserId(userId);
        schedule.setCalendarOriginKey(calendarPermissionEntity.getCalendarOriginKey());
        return scheduleRepository.save(schedule);
    }

    public ScheduleEntity update(ScheduleEntity entity) {
        final Optional<ScheduleEntity> original = Optional.ofNullable(scheduleRepository.findByOriginKey(entity.getOriginKey()));

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
        return scheduleRepository.findByOriginKey(entity.getOriginKey());
    }


}
