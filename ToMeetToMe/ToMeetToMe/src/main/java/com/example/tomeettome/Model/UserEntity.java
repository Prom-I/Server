package com.example.tomeettome.Model;

import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.UpdateTimestamp;

import javax.persistence.Entity;
import javax.persistence.UniqueConstraint;
import javax.persistence.Table;
import javax.persistence.Id;
import javax.persistence.GeneratedValue;
import java.time.LocalDateTime;

@Data
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "User",uniqueConstraints = {@UniqueConstraint(columnNames = "userId")})
// Calendar Collection
public class UserEntity {
    @Id
    @GeneratedValue(generator = "system-uuid")
    @GenericGenerator(name = "system-uuid", strategy = "uuid")
    private String uid;

    @NonNull
    private String userId;
    private String userName;
    private String image;
    private String platform;
    private String fcmToken;
    private String accessToken;
    private String refreshToken;

    @UpdateTimestamp
    private LocalDateTime lastModifiedAt;
    @CreationTimestamp
    private LocalDateTime createdAt;
}

