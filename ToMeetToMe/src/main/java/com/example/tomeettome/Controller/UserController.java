package com.example.tomeettome.Controller;

import com.example.tomeettome.DTO.ResponseDTO;
import com.example.tomeettome.DTO.TestDTO;
import com.example.tomeettome.DTO.TokenDTO;
import com.example.tomeettome.DTO.UserDTO;
import com.example.tomeettome.Model.UserEntity;
import com.example.tomeettome.Security.TokenProvider;
import com.example.tomeettome.Service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.*;

@Slf4j
@RestController
@RequestMapping("/user")

public class UserController {

    @Autowired
    private UserService userService;

    @Autowired
    private TokenProvider tokenProvider;

    /***
     *
     * @param dto ID Token
     * @return User Information
     * @throws GeneralSecurityException
     * @throws IOException
     */
    @PostMapping
    public ResponseEntity<?> validateIdToken(@RequestBody TokenDTO dto) throws GeneralSecurityException, IOException {
        log.info("UserController is activate");
        log.info("Token DTO :" + dto.getIdToken());
        String idToken = dto.getIdToken();
        UserEntity user = userService.validateIdToken(idToken);

        if (user != null) {
            String token = tokenProvider.create(user);
            UserDTO userDTO = UserDTO.builder()
                    .userId(user.getUserId())
                    .userName(user.getUserName())
                    .token(token)
                    .build();
            ResponseDTO<UserDTO> response = ResponseDTO.<UserDTO>builder().data(Collections.singletonList(userDTO)).status("success").build();
            return ResponseEntity.ok().body(response);
        }
        else {
            ResponseDTO response = ResponseDTO.builder()
                    .status("fail")
                    .error("Invaild ID Token")
                    .build();
            return ResponseEntity.ok().body(response);
        }
    }
    




}
