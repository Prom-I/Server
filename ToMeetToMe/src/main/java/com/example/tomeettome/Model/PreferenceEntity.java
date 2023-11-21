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
    private String originKey;
    private String teamOriginKey;
    private String name;
    private String startAt;
    private String endAt;
    private String possibleUserCount;
    private String confirmed;

    private int duration;
    private String startDayScope;
    private String endDayScope;
    private String startTimeScope;
    private String endTimeScope;
    private String allDay;

    @UpdateTimestamp
    private LocalDateTime lastModifiedAt;

    @CreationTimestamp
    private LocalDateTime createdAt;
}