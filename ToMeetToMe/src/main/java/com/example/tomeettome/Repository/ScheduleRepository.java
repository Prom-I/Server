package com.example.tomeettome.Repository;

import com.example.tomeettome.Model.CalendarEntity;
import com.example.tomeettome.Model.ScheduleEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ScheduleRepository extends JpaRepository<ScheduleEntity, String> {
}