package com.example.tomeettome.Controller;

import com.example.tomeettome.DTO.NotificationDTO;
import com.example.tomeettome.Service.NotificationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/notification")
public class NotificationController {
    @Autowired NotificationService notificationService;

    @PostMapping
    public ResponseEntity<?> sendNotificationByToken(@RequestBody NotificationDTO dto) {
        log.info("controller enter");
        notificationService.sendNotificaton(notificationService.makeMessageByToken(dto));
        return ResponseEntity.ok().body(null);
    }

}
