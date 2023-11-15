package com.example.tomeettome.DTO;

import com.example.tomeettome.Model.ScheduleEntity;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Data
public class ScheduleDTO {
    private String originKey;
    private String calendarOriginKey;
    private String categoryOriginKey;
    private String summary;
    private String description;
    private String dtStart;
    private String dtEnd;
    private String location;
    private String rRule;
    private String status;
    private String allDay;
    private LocalDateTime lastModifiedAt;
    private LocalDateTime createdAt;

    public ScheduleDTO(final ScheduleEntity entity) {
        this.originKey = entity.getOriginKey();
        this.calendarOriginKey = entity.getCalendarOriginKey();
        this.categoryOriginKey = entity.getCategoryOriginKey();
        this.summary = entity.getSummary();
        this.description = entity.getDescription();
        this.dtStart = entity.getDtStart();
        this.dtEnd = entity.getDtEnd();
        this.location = entity.getLocation();
        this.rRule = entity.getRRule();
        this.status = entity.getStatus();
        this.allDay = entity.getAllDay();
        this.lastModifiedAt = entity.getLastModifiedAt();
        this.createdAt = entity.getCreatedAt();
    }

    // DTO -> Entity 변환
    public static ScheduleEntity toEntity(final ScheduleDTO dto) {
        return ScheduleEntity.builder()
                .originKey(dto.getOriginKey())
                .calendarOriginKey(dto.getCalendarOriginKey())
                .categoryOriginKey(dto.getCategoryOriginKey())
                .summary(dto.getSummary())
                .description(dto.getSummary())
                .dtStart(dto.getDtStart())
                .dtEnd(dto.getDtEnd())
                .location(dto.getLocation())
                .rRule(dto.getRRule())
                .status(dto.getStatus())
                .allDay(dto.getAllDay())
                .lastModifiedAt(dto.getLastModifiedAt())
                .createdAt(dto.getCreatedAt())
                .build();
    }
}



