package com.example.tomeettome.Service;

import com.example.tomeettome.DTO.NotificationDTO;
import com.example.tomeettome.Model.UserEntity;
import com.example.tomeettome.Repository.UserRepository;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.Notification;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class NotificationService {

    private FirebaseMessaging firebaseMessaging;
    @Autowired UserRepository userRepository;

    public String sendNotificatonByToken(NotificationDTO dto) {
        UserEntity user = userRepository.findByFcmToken(dto.getFcmToken());
        if (user != null) {
            if (user.getFcmToken() != null) {
                Notification notification = Notification.builder()
                        .setTitle(dto.getTitle())
                        .setBody(dto.getBody())
                        .build();
                Message message = Message.builder()
                        .setToken(user.getFcmToken())
                        .setNotification(notification)
                        .build();

                try {
                    firebaseMessaging.send(message);
                    return "Notification Send Success" + dto.getFcmToken();
                }
                catch (FirebaseMessagingException e) {
                    e.printStackTrace();
                    return "Notificaton Send Fail" + dto.getFcmToken();
                }
            }
            else {
                return "Not Exist This User's FCM Token" + dto.getFcmToken();
            }
        }
        else {
            return "Not Exist This User" + dto.getFcmToken();
        }
    }
}