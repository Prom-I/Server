package com.example.tomeettome.Repository;

import com.example.tomeettome.Model.AppointmentBlockEntity;
import com.example.tomeettome.Model.CalendarPermissionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.sql.Timestamp;
import java.util.List;

public interface AppointmentBlockRepository extends JpaRepository<AppointmentBlockEntity, String> {
    List<AppointmentBlockEntity> findByPromiseUidOrderByRateDesc(String promiseUid);
    AppointmentBlockEntity findByTimestamp(Timestamp time);
    List<AppointmentBlockEntity> findByPromiseUid(String promiseUid);

    @Query(value = "SELECT json_extract(absentee, '$.name') FROM appointment_block WHERE timestamp = :ts", nativeQuery = true)
    List<String> findNameByTimestamp(@Param("ts") Timestamp timestamp);
}
