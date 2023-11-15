package com.example.tomeettome.Service;

import com.example.tomeettome.Model.CalendarEntity;
import com.example.tomeettome.Model.CalendarPermissionEntity;
import com.example.tomeettome.Model.ScheduleEntity;
import com.example.tomeettome.Model.UserEntity;
import com.example.tomeettome.Repository.CalendarPermissionRepository;
import com.example.tomeettome.Repository.CalendarRepository;
import com.example.tomeettome.Repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class CalendarService {
    @Autowired CalendarRepository calendarRepository;
    @Autowired CalendarPermissionRepository calendarPermissionRepository;

    public CalendarEntity createUserCalendar(UserEntity user) {
        CalendarEntity calendar = CalendarEntity.builder()
                .componentType("VEVENT")
                .build();
        CalendarPermissionEntity calendarPermission = CalendarPermissionEntity.builder()
                .calendarOriginKey(calendar.getOriginKey())
                .ownerOriginKey(user.getOriginKey())
                .ownerType("user")
                .permissionLevel("admin")
                .userId(user.getUserId())
                .build();
        calendarPermissionRepository.save(calendarPermission);
        return calendarRepository.save(calendar);
    }

}
