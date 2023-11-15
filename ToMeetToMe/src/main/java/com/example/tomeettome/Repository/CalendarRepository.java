package com.example.tomeettome.Repository;

import com.example.tomeettome.Model.CalendarEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CalendarRepository extends JpaRepository<CalendarEntity, String> {
}
