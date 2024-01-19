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

import java.util.List;

@Slf4j
@Service
public class NotificationService {

    @Autowired private FirebaseMessaging firebaseMessaging;
    @Autowired UserRepository userRepository;

    public NotificationDTO makefollowNotiDTO(String followerId, String followingId) {
        UserEntity follower = userRepository.findByUserId(followerId);
        UserEntity following = userRepository.findByUserId(followingId);
        if(follower != null && following != null) {
            return NotificationDTO.builder()
                    .fcmToken(following.getFcmToken())
                    .title("친구 추가 요청")
                    .body(follower.getUserName() + " 님이 친구가 되고 싶어합니다.")
                    .build();
        }
        else return null;
    }

    public NotificationDTO makeAcceptFollowNotiDTO(String followerId, String followingId) {
        UserEntity follower = userRepository.findByUserId(followerId);
        UserEntity following = userRepository.findByUserId(followingId);
        if(follower != null && following != null) {
            return NotificationDTO.builder()
                    .fcmToken(follower.getFcmToken())
                    .title("친구 요청 수락")
                    .body(following.getUserName() + " 님과 친구과 되었습니다.")
                    .build();
        }
        else return null;
    }

    public Message makeMessageByToken(NotificationDTO dto) {
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

                return message;
            }
            else {
                log.error("Not Exist This User's FCM Token" + dto.getFcmToken());
                return null;
            }
        }
        else {
            log.error("Not Exist This User" + dto.getFcmToken());
            return null;
        }
    }

    public void sendNotificaton(Message message) {
        try {
            firebaseMessaging.send(message);
        }
        catch (FirebaseMessagingException e) {
            e.printStackTrace();
        }
    }

    public void sendNotificatons(List<Message> messages){
        try {
            for (Message m :messages) {
                firebaseMessaging.send(m);
            }
        }
        catch (FirebaseMessagingException e) {
            e.printStackTrace();
        }
    }


}
