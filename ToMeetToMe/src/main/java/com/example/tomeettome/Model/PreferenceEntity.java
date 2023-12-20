package com.example.tomeettome.Model;

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
@Table(name = "Preference")
public class PreferenceEntity {
    @Id
    @GeneratedValue(generator="system-uuid")
    @GenericGenerator(name="system-uuid", strategy="uuid")
    private String uid;
    private String teamOriginKey;
    private String organizerId; // 약속을 만든 사람, 약속을 수정하고 확정할 수 있는 권한자

    private String summary;
    private LocalDateTime dtStart;
    private LocalDateTime dtEnd;
    private String location;

    private String duration; // P15DT5H0M20S : 15Days 5Hours 0Minutes 20Seconds
                             // PT2H : 2Hours
    private String startScope; // T를 기준으로 앞은 dayScope, 뒤는 timeScope
    private String endScope;

    private String possibleUserCount;
    private String status;
    private String allDay;

    @UpdateTimestamp
    private LocalDateTime lastModifiedAt;

    @CreationTimestamp
    private LocalDateTime createdAt;
}