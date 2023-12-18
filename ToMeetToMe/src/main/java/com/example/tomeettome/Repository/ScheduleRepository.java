package com.example.tomeettome.Repository;

import com.example.tomeettome.Model.ScheduleEntity;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Path;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import static net.fortuna.ical4j.filter.PredicateFactory.or;

@Repository
// JpaSpecificationExecutor<ScheduleEntity>
public interface ScheduleRepository extends JpaRepository<ScheduleEntity, String> {
    ScheduleEntity findByUid(String uid);
    List<ScheduleEntity> findByIcsFileName(String icsFileName);
//    List<ScheduleEntity> findAllByDtStartBetweenAndDtEndBetween(LocalDateTime dtStart, LocalDateTime dtEnd);
    List<ScheduleEntity> findAll(Specification<ScheduleEntity> spec);

    static Specification<ScheduleEntity> hasPreferredDays(LocalDate preferredDay) {
        String pf = preferredDay.format(DateTimeFormatter.ofPattern("yyyyMMdd"));

        return (root, query, builder) -> builder.like(root.get("dtStart"), pf + "%");
    }

    static Specification<ScheduleEntity> hasPreferredTimeRange(LocalDateTime startTime, LocalDateTime endTime) {
        // 20231215T090000Z .. 모든 block이 다들어옴


    }


}

    // 약속 시간대 09~21시 duration 2시간
    // 09-11 10-12 11-13
    // 일정 08시~10시
