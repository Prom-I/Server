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

    private String promiseUid;

    private LocalDateTime dtStart; // 날짜지만 시간을 00시-2359로 표기
    private LocalDateTime dtEnd;

    private int likes; // 좋아요 누른 사람 수
    private int priority; // 로직으로 추천된 경우, 순위
    private String type; // 로직으로 추천된 건지, 직접 선택으로 만든건지 구별하기 위해

//    private String location;
    @UpdateTimestamp
    private LocalDateTime lastModifiedAt;

    @CreationTimestamp
    private LocalDateTime createdAt;
}