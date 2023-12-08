package com.example.tomeettome.Repository;

import com.example.tomeettome.Model.ScheduleEntity;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.Set;

@Repository
public interface ScheduleRepository extends JpaRepository<ScheduleEntity, String>, JpaSpecificationExecutor<ScheduleEntity> {
    ScheduleEntity findByUid(String uid);
    List<ScheduleEntity> findByIcsFileName(String icsFileName);
    List<ScheduleEntity> findAllByDtStartBetweenAndDtEndBetween(LocalDateTime dtStart, LocalDateTime dtEnd);
    List<ScheduleEntity> findAll(Specification<ScheduleEntity> spec);

    static Specification<ScheduleEntity> hasPreferredDays(Set<DayOfWeek> preferredDays, ScheduleEntity schedule) {
        String dtStart = schedule.getDtStart();
        LocalDate dtStartDate = LocalDate.parse(dtStart.split("T")[0], DateTimeFormatter.ofPattern("yyyyMMdd"));
        return (root, query, builder) -> root.get(dtStartDate.getDayOfWeek().toString()).in(preferredDays);
    }

    static Specification<ScheduleEntity> hasPreferredTimeRange(LocalTime startTime, LocalTime endTime) {
        return (root, query, builder) -> builder.between(root.get("startTime"), startTime, endTime)
                .or(builder.between(root.get("endTime"), startTime, endTime));
    }
}