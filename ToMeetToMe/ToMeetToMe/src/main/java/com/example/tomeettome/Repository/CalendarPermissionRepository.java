package com.example.tomeettome.Repository;

import com.example.tomeettome.Model.CalendarEntity;
import com.example.tomeettome.Model.CalendarPermissionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CalendarPermissionRepository extends JpaRepository<CalendarPermissionEntity, String> {
//    CalendarPermissionEntity findByUserId(String userId);
    List<CalendarPermissionEntity> findByUserId(String userId); // CalendarPermission이 1개 이상일 수 있으므로 List 반환
    List<CalendarPermissionEntity> findByOwnerOriginKey(String ownerOriginKey);
    List<CalendarPermissionEntity> findByIcsFileName(String icsFileName);
}
