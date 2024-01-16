package com.example.tomeettome.Controller;

import com.example.tomeettome.DTO.*;
import com.example.tomeettome.DTO.Apple.AuthCodeDTO;
import com.example.tomeettome.Model.CalendarEntity;
import com.example.tomeettome.Model.UserEntity;
import com.example.tomeettome.Security.TokenProvider;
import com.example.tomeettome.Service.CalendarService;
import com.example.tomeettome.Service.CategoryService;
import com.example.tomeettome.Service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.json.JSONException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
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

    // FCM token 저장하는 API
        @PostMapping("/auth/fcm/token")
    public ResponseEntity<?> getFcmToken(@AuthenticationPrincipal String userId ,
                                         @RequestBody TokenDTO dto) {
        // userId로 User 찾아서 Fcm token 저장하기
        Boolean result = userService.saveFcmToken(userId,dto.getToken());
        if (result) return ResponseEntity.status(HttpStatus.OK).body(null);
        else return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
    }

    @PostMapping("/authentication")
    public ResponseEntity<?> validateIdToken(@RequestBody TokenDTO dto) throws GeneralSecurityException, IOException {
        log.info("Token DTO :" + dto.getToken());
        String idToken = dto.getToken();
        UserEntity user = userService.validateIdTokenGoogle(idToken);

        if (user != null) { // 회원가입 또는 로그인의 경우
            return (user);
        }
        else { // ID Token이 유효하지 않은 경우
            ResponseDTO response = ResponseDTO.builder()
                    .status("fail")
                    .error("Invaild ID Token")
                    .build();
            return ResponseEntity.ok().body(response);
        }
    }

    //Kakao가 Redirect로 보내는 URL
    @GetMapping("/kakao/callback")
    public ResponseEntity<?> redirectKakaoResponse(@RequestParam String code) throws JSONException {
        // Redirect로 인증코드를 받음
        // 받은 인증 코드로 Access Token 요청
        String accessToken = userService.createAuthCodeKakao(code);
        try {
            // access Token Validation
            userService.validateAccessTokenKakao(accessToken);

            // KaKao API로 유저 정보 얻기
            UserEntity user = userService.getUserInfoKaKao(accessToken);

            return login(user);
        }
        catch (Exception e) {
            log.error("error msg : " + e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }

        // https://kauth.kakao.com/oauth/authorize?client_id=5ce66229811ad9d602663cadba712f32&redirect_uri=https://www.tmtm.site/user/kakao/callback&response_type=code
    }

    // Apple에 인증코드 요청한 결과인 인증코드 결과 (redirect url)
    // application/x-www-form-urlencoded application/json
    // user : name(f,l), email
    @PostMapping("/apple/callback")
    public ResponseEntity<?> redirectAppleResponse(AuthCodeDTO dto){

        // id token에서 email을 뽑음
        String idToken = dto.getAuthorization().getId_token();
        String email = tokenProvider.validateIdTokenAndGetEmail(idToken);
        // 사용자의 존재유무 확인
        boolean result = userService.checkUserExists(email);

        UserEntity user = UserEntity.builder()
                .userId(email)
                .build();

        if(!result) { // 회원가입
            if(dto.getUser() != null) { // user의 정보가 딸려오는 회원가입
                JSONObject jsonObject = new JSONObject(dto.getUser());
                JSONObject name = (JSONObject) jsonObject.get("name");
                String firstName = name.getString("firstName");
                String lastName = name.getString("lastName");

                user.setUserName(lastName+firstName);
                userService.create(user);
            }
            else { // 회원가입인데, user 정보가 딸려오지 않아서 userName 디폴트로 설정하는 부분
                user.setUserName("default");
                userService.create(user);
            }
        }

        return login(user);
        // https://appleid.apple.com/auth/authorize?client_id=com.ToMeetToMe.services&redirect_uri=https%3A%2F%2Fwww.tmtm.site%2Fuser%2Fapple%2Fcallback&response_type=code id_token&state=test&scope=name email&nonce=20B20D-0S8-1K8&response_mode=form_post&frame_id=64f53562-9fee-4003-8ca7-12696c2c7cc1&m=12&v=1.5.5
    }

    @PostMapping("/signup")
    public ResponseEntity<?> create(@RequestBody UserDTO dto) {
        UserEntity user = dto.toEntity(dto);
        try {
            user = userService.create(user);
            CalendarEntity calendar = calendarService.createUserCalendar(user); // Calendar 생성
            calendarService.createUserCalendarPermission(user,calendar); // CalendarPermission 생성
            categoryService.init(calendar); // Default Category 생성

            CalendarDTO calendarDTO = CalendarDTO.builder()
                    .icsFileName(calendar.getIcsFileName())
                    .componentType(calendar.getComponentType())
                    .userId(user.getUserId())
                    .userName(user.getUserName())
                    .build();
            ResponseDTO<CalendarDTO> response = ResponseDTO.<CalendarDTO>builder().data(Collections.singletonList(calendarDTO)).status("success").build();
            return ResponseEntity.ok().body(response);
        }
        catch (Exception e) {
            ResponseDTO response = ResponseDTO.builder()
                    .status("fail")
                    .error("id already exists")
                    .build();
            return ResponseEntity.ok().body(response);
        }
    }

    @GetMapping("/retrieve/{pattern}")
    public ResponseEntity<?> retrieve(@PathVariable("pattern") String pattern) throws UnsupportedEncodingException {
        String decodedPattern = URLDecoder.decode(pattern, StandardCharsets.UTF_8);

        List<UserEntity> users = userService.retrieve(decodedPattern);
        List<UserDTO> usersDTO = users.stream().map(UserDTO::new).collect(Collectors.toList());

        ResponseDTO<UserDTO> response = ResponseDTO.<UserDTO>builder().data(usersDTO).status("success").build();
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @PatchMapping("update/profile")
    public ResponseEntity<?> updateProfile(@AuthenticationPrincipal String userId,
                                                @RequestBody UserDTO dto) {
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
                .build();
        ResponseDTO<UserDTO> response = ResponseDTO.<UserDTO>builder().data(Collections.singletonList(userDTO)).status("success").build();
        return ResponseEntity.ok().body(response);
    }

    // User를 주면 
    // 토큰 발급해서 로그인 진행
    private ResponseEntity<ResponseDTO<UserDTO>> login(UserEntity user) {
        String token = tokenProvider.create(user);
        UserDTO userDTO = UserDTO.builder()
                .userId(user.getUserId())
                .token(token)
                .build();
        log.info("로그인 후 token 발급완료!" + token);
        ResponseDTO<UserDTO> response = ResponseDTO.<UserDTO>builder().data(Collections.singletonList(userDTO)).status("success").build();
        return ResponseEntity.ok().body(response);
    }





}
