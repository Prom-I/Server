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

    private LocalDateTime dtStart;
    private LocalDateTime dtEnd;

    private int likes; // 좋아요 누른 사람 수

    private String status;

    @UpdateTimestamp
    private LocalDateTime lastModifiedAt;

    @CreationTimestamp
    private LocalDateTime createdAt;
}