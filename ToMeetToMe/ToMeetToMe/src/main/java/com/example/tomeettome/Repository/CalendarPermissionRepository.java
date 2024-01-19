package com.example.tomeettome.Repository;

import com.example.tomeettome.Model.CalendarEntity;
import com.example.tomeettome.Model.CalendarPermissionEntity;
import com.example.tomeettome.Model.ScheduleEntity;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CalendarPermissionRepository extends JpaRepository<CalendarPermissionEntity, String> {
//    CalendarPermissionEntity findByUserId(String userId);
    List<CalendarPermissionEntity> findByUserId(String userId); // CalendarPermission이 1개 이상일 수 있으므로 List 반환
    List<CalendarPermissionEntity> findByOwnerOriginKey(String ownerOriginKey);
    List<CalendarPermissionEntity> findByIcsFileName(String icsFileName);
    CalendarPermissionEntity findOne(Specification<CalendarPermissionEntity> spec);

    static Specification<CalendarPermissionEntity> findCalendarPermission(String ownerOriginkey, String userId) {
        return (root, query, builder) -> builder.and(builder.equal(root.get("ownerOriginKey"), ownerOriginkey),
                builder.equal(root.get("userId"), userId));
    }
}
