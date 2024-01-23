package com.example.tomeettome.Model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.UpdateTimestamp;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import java.time.LocalDateTime;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Data
@Entity
@Table(name = "CalendarPermission")
public class CalendarPermissionEntity {
    @Id
    @GeneratedValue(generator="system-uuid")
    @GenericGenerator(name="system-uuid", strategy="uuid")
    private String originKey;
    private String icsFileName;
    private String ownerOriginKey; // userUid 또는 teamUid, 캘린더의 소유자
    private String ownerType; // user 또는 team
    private String permissionLevel; // 관리자(admin) 또는 Member or Guest
    private String userId; // 권한의 소유자
    @UpdateTimestamp
    private LocalDateTime lastModifiedAt;
    @CreationTimestamp
    private LocalDateTime createdAt;
}
