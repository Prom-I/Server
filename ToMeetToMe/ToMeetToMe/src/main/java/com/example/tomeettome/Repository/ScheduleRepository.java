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
import java.sql.Timestamp;
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
public interface ScheduleRepository extends JpaRepository<ScheduleEntity, String>, JpaSpecificationExecutor<ScheduleEntity> {
    ScheduleEntity findByUid(String uid);
    List<ScheduleEntity> findByIcsFileName(String icsFileName);
    List<ScheduleEntity> findByCategoryUid(String categoryUid);
//    List<ScheduleEntity> findAllByDtStartBetweenAndDtEndBetween(LocalDateTime dtStart, LocalDateTime dtEnd);
    List<ScheduleEntity> findAll(Specification<ScheduleEntity> spec);

    static Specification<ScheduleEntity> hasPreferredTimeRange(Timestamp startTime, Timestamp endTime) {
        return (root, query, builder) ->
                builder.or(
                        builder.between(root.get("dtStart"), startTime, endTime),
                        builder.between(root.get("dtEnd"), startTime, endTime)
                );
    }

    static Specification<ScheduleEntity> hasTeam(String userId) {
        return (root, query, builder) -> builder.equal(root.get("icsFileName"), userId+".ics");
    }

}


