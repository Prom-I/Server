package com.example.tomeettome.Model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.UpdateTimestamp;

import javax.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Data
@Entity
@Table(name = "Promise")
public class PromiseEntity {
    @Id
    @GeneratedValue(generator="system-uuid")
    @GenericGenerator(name="system-uuid", strategy="uuid")
    private String uid;
    private String icsFileName; // 약속이 생성된 팀의 캘린더 ics File Name
    private String summary; // 약속 제목
    private String organizerId; // 약속을 만든 사람
    private String status; // 약속의 수행 상태
    private LocalDateTime dtStart; // 확정되기 전에는 처음 설정한 약속의 day scope
    private LocalDateTime dtEnd; // 확정된 후에는 최종 약속 날짜+시간
    private String location;

    private String preferredDays; // 약속을 갱신할 때 약속 추천을 갱신하려면 이 정보가 필요하기 때문에 1,2,3,4,5
    private int duration; // 2

    private String absentee;
    private String attendee;

    @UpdateTimestamp
    private LocalDateTime lastModifiedAt;

    @CreationTimestamp
    private LocalDateTime createdAt;
}
