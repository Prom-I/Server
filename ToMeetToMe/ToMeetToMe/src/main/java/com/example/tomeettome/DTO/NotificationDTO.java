package com.example.tomeettome.DTO;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.Map;

@Getter
@NoArgsConstructor
@Builder
public class NotificationDTO {
    private String fcmToken;
    private String title;
    private String body;
//    private String image;
//    private Map<String, String> data;

    public NotificationDTO(String fcmToken, String title, String body) {
        this.fcmToken = fcmToken;
        this.title = title;
        this.body = body;
    }
}
