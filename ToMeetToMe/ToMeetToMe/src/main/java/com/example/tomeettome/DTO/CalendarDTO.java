package com.example.tomeettome.DTO;

import com.example.tomeettome.Model.CalendarEntity;
import com.example.tomeettome.Model.UserEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Data
public class CalendarDTO {
    private String icsFileName;
    private String userId;
    private String userName;
    private String componentType;
    private LocalDateTime lastModifiedAt;
    private LocalDateTime createdAt;

    public CalendarDTO(final CalendarEntity calendarEntity, final UserEntity userEntity) {
        this.icsFileName = calendarEntity.getIcsFileName();
        this.userId = userEntity.getUserId();
        this.userName = userEntity.getUserName();
        this.lastModifiedAt = calendarEntity.getLastModifiedAt();
        this.createdAt = calendarEntity.getCreatedAt();
    }
    public static CalendarEntity toEntity(CalendarDTO dto){
        return CalendarEntity.builder()
                .icsFileName(dto.getIcsFileName())
                .componentType(dto.getComponentType())
                .build();
    }
}



