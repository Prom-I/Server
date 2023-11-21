package com.example.tomeettome.Model;

import com.example.tomeettome.DTO.CaldavDTO;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Data;

import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.Id;
import javax.persistence.GeneratedValue;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Data
@Entity
@Table(name = "Schedule")
public class ScheduleEntity {
    @Id
    @GeneratedValue(generator="system-uuid")
    @GenericGenerator(name="system-uuid", strategy="uuid")
    private String originKey;
    private String calendarOriginKey;
    private String categoryOriginKey;
    private String summary; // 일정의 제목
    private String description; // 일정의 상세설명
    private String dtStart; //시작 시간
    private String dtEnd; //종료시간
    private String location;
    private String rRule;
    private String status;
    private String allDay;

    @UpdateTimestamp
    private LocalDateTime lastModifiedAt;

    @CreationTimestamp
    private LocalDateTime createdAt;


}
