package com.example.tomeettome.Controller;

import com.example.tomeettome.DTO.*;
import com.example.tomeettome.Model.CalendarEntity;
import com.example.tomeettome.Model.CalendarPermissionEntity;
import com.example.tomeettome.Model.UserEntity;
import com.example.tomeettome.Repository.CalendarPermissionRepository;
import com.example.tomeettome.Repository.UserRepository;
import com.example.tomeettome.Security.TokenProvider;
import com.example.tomeettome.Service.CalendarService;
import com.example.tomeettome.Service.CategoryService;
import com.example.tomeettome.Service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.*;

@Slf4j
@RestController
@RequestMapping("/user")
public class UserController {

    @Autowired private UserService userService;
    @Autowired private CalendarService calendarService;
    @Autowired private TokenProvider tokenProvider;
    @Autowired private CategoryService categoryService;

    /***
     * Validation ID Token & Authentication
     * @param dto ID Token
     * @return User Information
     * @throws GeneralSecurityException
     * @throws IOException
     */
    @PostMapping("/authentication")
    public ResponseEntity<?> validateIdToken(@RequestBody TokenDTO dto) throws GeneralSecurityException, IOException {
        log.info("Token DTO :" + dto.getIdToken());
        String idToken = dto.getIdToken();
        UserEntity user = userService.validateIdToken(idToken);

        if (user != null) { // 회원가입 또는 로그인의 경우
            boolean result = userService.checkUserExists(user.getUserId());
            log.info("로그인 인가요? "+result);

            if (result) { // 로그인
                String token = tokenProvider.create(user);
                UserDTO userDTO = UserDTO.builder()
                        .userId(user.getUserId())
                        .userName(user.getUserName())
                        .token(token)
                        .build();
                log.info("로그인 후 token 발급완료!" + token);
                ResponseDTO<UserDTO> response = ResponseDTO.<UserDTO>builder().data(Collections.singletonList(userDTO)).status("success").build();
                return ResponseEntity.ok().body(response);
            }
            else { // 회원가입
                ResponseDTO<UserDTO> response = ResponseDTO.<UserDTO>builder().data(null).status("success").build();
                return ResponseEntity.ok().body(response);
            }
        } // ID Token이 유효하지 않은 경우
        else {
            ResponseDTO response = ResponseDTO.builder()
                    .status("fail")
                    .error("Invaild ID Token")
                    .build();
            return ResponseEntity.ok().body(response);
        }
    }

    /***
     * Sign Up
     * @param dto UserDTO
     * @return UserDTO
     */

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

}
