package com.example.tomeettome.DTO;

import com.example.tomeettome.Model.ScheduleEntity;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Data
public class ScheduleDTO {
    private String uid;
    private String icsFileName;
    private String categoryUid;
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
        this.uid = entity.getUid();
        this.icsFileName = entity.getIcsFileName();
        this.categoryUid = entity.getCategoryUid();
        this.summary = entity.getSummary();
        this.description = entity.getDescription();
        this.dtStart = entity.getDtStart().toString();
        this.dtEnd = entity.getDtEnd().toString();
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
                .uid(dto.getUid())
                .icsFileName(dto.getIcsFileName())
                .categoryUid(dto.getCategoryUid())
                .summary(dto.getSummary())
                .description(dto.getSummary())
                .dtStart(Timestamp.valueOf(LocalDateTime.parse(dto.getDtStart(), DateTimeFormatter.ofPattern("yyyyMMdd'T'HHmmss'Z'"))))
                .dtEnd(Timestamp.valueOf(LocalDateTime.parse(dto.getDtEnd(), DateTimeFormatter.ofPattern("yyyyMMdd'T'HHmmss'Z'"))))
                .location(dto.getLocation())
                .rRule(dto.getRRule())
                .status(dto.getStatus())
                .allDay(dto.getAllDay())
                .lastModifiedAt(dto.getLastModifiedAt())
                .createdAt(dto.getCreatedAt())
                .build();
    }

}



