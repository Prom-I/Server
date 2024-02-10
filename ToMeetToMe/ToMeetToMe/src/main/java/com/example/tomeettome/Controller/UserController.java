package com.example.tomeettome.Controller;

import com.example.tomeettome.Constant.ERRORMSG;
import com.example.tomeettome.Constant.PLATFORM;
import com.example.tomeettome.DTO.*;
import com.example.tomeettome.DTO.Apple.AppleKeyDTO;
import com.example.tomeettome.Model.CalendarEntity;
import com.example.tomeettome.Model.UserEntity;
import com.example.tomeettome.Security.TokenProvider;
import com.example.tomeettome.Service.CalendarService;
import com.example.tomeettome.Service.CategoryService;
import com.example.tomeettome.Service.NotificationService;
import com.example.tomeettome.Service.UserService;
import com.google.firebase.messaging.Message;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.json.JSONException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.text.ParseException;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/user")
public class UserController {

    @Autowired private UserService userService;
    @Autowired private CalendarService calendarService;
    @Autowired private TokenProvider tokenProvider;
    @Autowired private CategoryService categoryService;
    @Autowired private NotificationService notificationService;

    @Value("${kakao.client.id}")
    private String CLIENT_ID_KAKAO;

    @Value("${kakao.client.secret}")
    private String CLIENT_SECRET_KAKAO;

    // FCM token 저장하는 API
    @PostMapping("/auth/fcm/token")
    public ResponseEntity<?> getFcmToken(@AuthenticationPrincipal String userId ,
                                         @RequestBody TokenDTO dto) {
        try {// userId로 User 찾아서 Fcm token 저장하기
            Boolean result = userService.saveFcmToken(userId, dto.getToken());
            if (result) return ResponseEntity.status(HttpStatus.OK).body(null);
            else return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ERRORMSG.NullPointerException.name());
        }
        catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }
    // Goolge이 Redirect로 보내는 URL
    @GetMapping("/google/callback")
    public ResponseEntity<?> responseGoogleResponse(@RequestParam("code") String code) throws GeneralSecurityException, IOException {
        log.info("authorization code : " + code);
        List<String> tokens = userService.createAccessTokenGoogle(code);

        UserEntity user = userService.validateIdTokenGoogle(tokens.get(2));
        try {
            user.setPlatform(PLATFORM.GOOGLE.name());
            user.setAccessToken(tokens.get(0));
            user.setRefreshToken(tokens.get(1));
            return signUpOrLogin(user);
        }
        catch (Exception e) {
            log.error("error msg : " + e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
        //https://accounts.google.com/o/oauth2/v2/auth?scope=email%20profile&access_type=offline&include_granted_scopes=true&response_type=code&state=state_parameter_passthrough_value&redirect_uri=https://www.tmtm.site/user/google/callback&client_id=317253442779-2n3971n1mg5p0piue9ekfl9bnt20v41p.apps.googleusercontent.com
    }


    // Kakao가 Redirect로 보내는 URL
    @GetMapping("/kakao/callback")
    public ResponseEntity<?> redirectKakaoResponse(@RequestParam String code) throws JSONException {
        // Redirect로 인증코드를 받음

        // 받은 인증 코드로 Access Token 요청
        List<String> tokens = userService.createAuthCodeKakao(code);
        String accessToken = tokens.get(0);
        String refreshToken = tokens.get(1);
        try {
            // access Token Validation
            userService.validateAccessTokenKakao(accessToken);

            // KaKao API로 유저 정보 얻기
            UserEntity user = userService.getUserInfoKaKao(accessToken);
            user.setAccessToken(accessToken);
            user.setRefreshToken(refreshToken);
            user.setPlatform(PLATFORM.KAKAO.name());
            return signUpOrLogin(user);
        }
        catch (Exception e) {
            log.error("error msg : " + e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }

        // 테스트용 URL
        // https://kauth.kakao.com/oauth/authorize?client_id=5ce66229811ad9d602663cadba712f32&redirect_uri=https://www.tmtm.site/user/kakao/callback&response_type=code
        // https://kauth.kakao.com/oauth/authorize?client_id=5ce66229811ad9d602663cadba712f32&redirect_uri=http://localhost/user/kakao/callback&response_type=code
    }


    // Apple에 인증코드 요청한 결과인 인증코드 결과 (redirect url)
    // application/x-www-form-urlencoded application/json
    // user : name(f,l), email
    // 회원가입하다가 나가는 경우 error 처리 해야 됨 
    @PostMapping("/apple/callback")
    public ResponseEntity<?> redirectAppleResponse(@RequestParam("code") String code,
                                                   @RequestParam("id_token") String idToken,
                                                   @RequestParam(value="user",
                                                           required = false, defaultValue="") String userInfo) throws ParseException, IOException, NoSuchAlgorithmException, InvalidKeySpecException {
        // public key를 발급 받아야 함
        List<AppleKeyDTO> keys = userService.createPublicKeyApple();
        // Id Token  Valildation
        String email = tokenProvider.validateIdTokenAndGetUserIdApple(idToken,keys);
        // Access Token 발급
        List<String> tokens = userService.createAccessTokenApple(code);

        // 사용자의 존재유무 확인
        boolean result = userService.checkUserExists(email);

        //UserEntity 세팅
        UserEntity user = userService.getUserInfoApple(email, tokens.get(0), tokens.get(1), userInfo);

        if(!result) { // 회원가입
            userService.create(user);
            CalendarEntity calendar = calendarService.createUserCalendar(user); // Calendar 생성
            calendarService.createUserCalendarPermission(user,calendar); // CalendarPermission 생성
            categoryService.init(calendar); // Default Category 생성
        }

        return login(user);

        // 테스트용 URL
        // https://appleid.apple.com/auth/authorize?client_id=com.ToMeetToMe.services&redirect_uri=https%3A%2F%2Fwww.tmtm.site%2Fuser%2Fapple%2Fcallback&response_type=code id_token&state=test&scope=name email&nonce=20B20D-0S8-1K8&response_mode=form_post&frame_id=64f53562-9fee-4003-8ca7-12696c2c7cc1&m=12&v=1.5.5
    }

    @GetMapping("/retrieve/{pattern}")
    public ResponseEntity<?> retrieve(@PathVariable("pattern") String pattern) throws UnsupportedEncodingException {
        try {
            String decodedPattern = URLDecoder.decode(pattern, StandardCharsets.UTF_8);

            List<UserEntity> users = userService.retrieve(decodedPattern);
            List<UserDTO> usersDTO = users.stream().map(UserDTO::new).collect(Collectors.toList());

            ResponseDTO<UserDTO> response = ResponseDTO.<UserDTO>builder().data(usersDTO).status("success").build();
            return ResponseEntity.status(HttpStatus.OK).body(response);
        }
        catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    @GetMapping("/retrieve/follow")
    public ResponseEntity<?> retrieveFollowings(@AuthenticationPrincipal String userId) {
        try {
            List<UserEntity> users = userService.retrieveFollowings(userId);
            return ResponseEntity.status(HttpStatus.OK).body(users);
        }
        catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    @GetMapping("/retrieve/profile")
    public ResponseEntity<?> retrieveProfile(@AuthenticationPrincipal String userId) {
        try {
            UserEntity user = userService.retrieveProfile(userId);
            UserDTO dto = UserDTO.builder()
                    .userId(user.getUserId())
                    .userName(user.getUserName())
                    .image(user.getImage())
                    .platform(user.getPlatform())
                    .build();
            ResponseDTO<UserDTO> response = ResponseDTO.<UserDTO>builder().data(Collections.singletonList(dto)).status("success").build();
            return ResponseEntity.status(HttpStatus.OK).body(response);
        }
        catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    @PatchMapping("update/profile")
    public ResponseEntity<?> updateProfile(@AuthenticationPrincipal String userId,
                                                @RequestBody UserDTO dto) {
        try {
            UserEntity user = new UserEntity();

            if (dto.getImage() != null)
                user = userService.updateProfileImage(userId, dto.getImage());

            if(dto.getUserName() != null)
                user = userService.updateProfileUserName(userId, dto.getUserName());

            UserDTO userDTO = UserDTO.builder()
                    .userId(userId)
                    .userName(user.getUserName())
                    .image(user.getImage())
                    .platform(user.getPlatform())
                    .lastModifiedAt(user.getLastModifiedAt())
                    .createdAt(user.getCreatedAt())
                    .build();
            ResponseDTO<UserDTO> response = ResponseDTO.<UserDTO>builder().data(Collections.singletonList(userDTO)).status("success").build();
            return ResponseEntity.status(HttpStatus.OK).body(response);
        }
        catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    // 친구 추가 요청 API
    // 수락해주기 전까지는 follower가 갖고 있는 following의 permission이 'guest'
    @GetMapping("/follow/{followingId}")
    public ResponseEntity<?> followRequest(@AuthenticationPrincipal String userId,
                                    @PathVariable("followingId") String followingId) {
        try {
            userService.followRequest(userId, followingId);
            Message message = notificationService.makeMessageByToken(notificationService.makefollowNotiDTO(userId, followingId));
            notificationService.sendNotificaton(message);
            return ResponseEntity.ok().body(null);
        }
        catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    // 친구 추가 수락 API
    // 친구 추가 요청을 보낸 사람의 id를 Parameter로 받음
    @GetMapping("/follow/accept/{followerId}")
    public ResponseEntity<?> acceptFollowRequest(@AuthenticationPrincipal String userId,
                                                 @PathVariable("followerId") String followerId) {
        try {
            userService.acceptFollowRequest(followerId, "yourUserId2");
            Message message = notificationService.makeMessageByToken(notificationService.makeAcceptFollowNotiDTO(followerId, userId));
            notificationService.sendNotificaton(message);
            return ResponseEntity.status(HttpStatus.OK).body(null);
        }
        catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    // 친구 끊기 API
    // 끊을 친구의 id를 parameter로
    @DeleteMapping("/unfollow/{followingId}")
    public ResponseEntity<?> unfollow(@AuthenticationPrincipal String userId,
                                      @PathVariable("followingId") String followingId) {
        try {
            userService.deleteCalenderPermission(userId, followingId);
            return ResponseEntity.status(HttpStatus.NO_CONTENT).body(null);
        }
        catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }
    // 회원 탈퇴
    //@AuthenticationPrincipal String userId
    @DeleteMapping
    public ResponseEntity<?> deleteUser() throws IOException, NoSuchAlgorithmException, InvalidKeySpecException {
        try {
            String userId = "zxy789@nate.com";
            // 스케쥴 삭제
            calendarService.deleteUserSchedules(userId);
            //카테고리 삭제
            categoryService.deleteAll(userId);
            // 내가 가진 캘린더 권한 삭제
            calendarService.deleteCalendarPermissionsByUserId(userId);
            // 상대가 가진 나의 캘린더 권한을 삭제
            calendarService.deleteCalendarPermissionsByIcsFileName(userId);
            // 캘린더 삭제
            calendarService.deleteCalendar(userId);
            //소셜 로그인 끊기
            UserEntity user = userService.retrieveProfile(userId);
            String platform = user.getPlatform();

            if (platform.equals(PLATFORM.GOOGLE.name())) {
                // Google 처리 로직
                deleteUserGoogle(user);
            } else if (platform.equals(PLATFORM.APPLE.name())) {
                // Apple 처리 로직
                deleteUserApple(user);
            } else if (platform.equals(PLATFORM.KAKAO.name())) {
                // KaKao 처리 로직
                deleteUserKakao(user);
            }

            // 유저 삭제
            userService.deleteUser(userId);

            return null;
        }
        catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    private void deleteUserGoogle(UserEntity user){
        // access Token 재발급
        String accessToken = userService.getAccessTokenByRefreshTokenGoogle(user.getRefreshToken());
        // access Token 취소 (구글이 알아서 refreshToken까지 취소해줌)
        userService.unlinkGoogle(accessToken);
    }

    private void deleteUserApple(UserEntity user) throws IOException, NoSuchAlgorithmException, InvalidKeySpecException {
        // access 토큰 취소
        userService.unlinkApple(user.getAccessToken(),"access_token");
        // access 토큰 취소
        userService.unlinkApple(user.getRefreshToken(),"refresh_token");
    }

    private void deleteUserKakao(UserEntity user) {
        // Kakao 처리 로직
        String accessToken = user.getAccessToken();

        // 1. 엑세스 토큰 정보보기
        if(userService.validateAccessTokenKakao(user.getAccessToken()).equals(HttpStatus.UNAUTHORIZED)) {
            // 2. 엑세스 토큰 갱신하기(필요한 경우에만)
            accessToken = userService.getAccessTokenByRefreshTokenKakao(CLIENT_ID_KAKAO, user.getRefreshToken(), CLIENT_SECRET_KAKAO);
        }

        // 3. 연결 끊기
        userService.unlinkKakao(accessToken);
    }

    // User를 주면 
    // 토큰 발급해서 로그인 진행
    private ResponseEntity<ResponseDTO<UserDTO>> login(UserEntity user) {
        String token = tokenProvider.create(user);
        UserDTO userDTO = UserDTO.builder()
                .userId(user.getUserId())
                .userName(user.getUserName()!=null ? user.getUserName() : "")
                .token(token)
                .build();
        log.info("로그인 후 token 발급완료!" + token);
        ResponseDTO<UserDTO> response = ResponseDTO.<UserDTO>builder().data(Collections.singletonList(userDTO)).status("success").build();
        return ResponseEntity.ok().body(response);
    }

    private ResponseEntity<?> signUpOrLogin(UserEntity user) {
        // 사용자의 존재유무 확인
        boolean result = userService.checkUserExists(user.getUserId());
        if (result) { // 로그인
            return login(user);
        }
        else { // 회원가입
            try {
                user = userService.create(user);
                CalendarEntity calendar = calendarService.createUserCalendar(user); // Calendar 생성
                calendarService.createUserCalendarPermission(user,calendar); // CalendarPermission 생성
                categoryService.init(calendar); // Default Category 생성

                return login(user);
            }
            catch (Exception e) {
                log.error("error msg : " + e.getMessage());
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
            }

        }
    }
}
