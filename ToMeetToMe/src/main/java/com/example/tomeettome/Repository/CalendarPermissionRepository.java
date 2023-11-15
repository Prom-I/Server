package com.example.tomeettome.Repository;

import com.example.tomeettome.Model.CalendarEntity;
import com.example.tomeettome.Model.CalendarPermissionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CalendarPermissionRepository extends JpaRepository<CalendarPermissionEntity, String> {
}
