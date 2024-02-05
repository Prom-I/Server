package com.example.tomeettome.Service;

import com.example.tomeettome.DTO.NotificationDTO;
import com.example.tomeettome.Model.CalendarPermissionEntity;
import com.example.tomeettome.Model.TeamEntity;
import com.example.tomeettome.Model.UserEntity;
import com.example.tomeettome.Repository.CalendarPermissionRepository;
import com.example.tomeettome.Repository.TeamRepository;
import com.example.tomeettome.Repository.UserRepository;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.Notification;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
public class NotificationService {
    @Autowired private TeamRepository teamRepository;
    @Autowired private CalendarPermissionRepository calendarPermissionRepository;
    @Autowired private FirebaseMessaging firebaseMessaging;
    @Autowired UserRepository userRepository;
    @Autowired TeamService teamService;

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
                    .body(following.getUserName() + " 님과 친구가 되었습니다.")
                    .build();
        }
        else return null;
    }

    public List<NotificationDTO> makeModifyOrConfirmPromiseNotiDTOList(String teamIcsFileName, boolean flag) {
        // 해당 팀의 permission들
        List<CalendarPermissionEntity> permissions = calendarPermissionRepository.findByIcsFileName(teamIcsFileName);
        List<String> fcmTokenList = new ArrayList<>();
        List<NotificationDTO> result = new ArrayList<>();

        // 팀에 해당하는 모든 유저들의 fcm토큰을 리스트에 넣고
        for (CalendarPermissionEntity p : permissions) {
            fcmTokenList.add(userRepository.findByUserId(p.getUserId()).getFcmToken());
        }

        // 알림 body에 넣을 팀 이름 찾고
        String teamName = teamRepository.findByUid(permissions.get(0).getOwnerOriginKey()).getName();

        String title = "";
        String body = "";
        if (flag) {
            title = "약속 수정";
            body = "팀 " + teamName + "의 약속이 수정되었습니다. 확인해주세요 !";
        }
        else {
            title = "약속 확정";
            body = "팀 " + teamName + "의 약속이 확정되었습니다. 확인해주세요 !";
        }

        for (String tklist : fcmTokenList) {
            NotificationDTO noti = NotificationDTO.builder()
                    .fcmToken(tklist)
                    .title(title)
                    .body(body)
                    .build();
            result.add(noti);
        }
        return result;
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

    public List<Message> makeMessagesByToken(List<NotificationDTO> dtos) {
        List<Message> msgList = new ArrayList<>();

        for (NotificationDTO noti : dtos) {
            if (noti.getFcmToken() != null) {
                Notification notification = Notification.builder()
                        .setTitle(noti.getTitle())
                        .setBody(noti.getBody())
                        .build();

                Message message = Message.builder()
                        .setToken(noti.getFcmToken())
                        .setNotification(notification)
                        .build();
                msgList.add(message);
            }
            else {
                log.error("Not Exist This User's FCM Token" + noti.getFcmToken());
                return null;
            }
        }
        return msgList;
    }

    public List<Message> makeInvitesMessages(TeamEntity teamEntity,String inviterId,String [] invitees) {
        List<Message> messageList = new ArrayList<>();

        for(String invitee : invitees){

            if((teamService.checkUserExistenceInTeam(teamEntity,invitee))) // 이미 팀에 존재하는데 초대하는 지 확인하는 예외처리
                continue;

            Notification notification = Notification.builder()
                    .setTitle("그룹 초대 알림")
                    .setBody(inviterId+"이 당신을"+ teamEntity.getName()+"에 초대하였습니다")
                    .build();

            Message message = Message.builder()
                    .setToken(userRepository.findByUserId(invitee).getFcmToken())
                    .setNotification(notification)
                    .putData("teamOriginKey",teamEntity.getUid()) // 데이터 암호화?
                    .build();
            messageList.add(message);
        }

        return messageList;
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
