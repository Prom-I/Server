package com.example.tomeettome.Controller;

import com.example.tomeettome.DTO.ResponseDTO;
import com.example.tomeettome.DTO.TestDTO;
import com.example.tomeettome.DTO.TokenDTO;
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
    UserService userService;

    @PostMapping
    public ResponseEntity<?> validateIdToken(@RequestBody TokenDTO dto) throws GeneralSecurityException, IOException {
        log.info("UserController is activate");
        log.info("Token DTO :" + dto.getIdToken());
        String idToken = dto.getIdToken();
        userService.validateIdToken(idToken);

        return ResponseEntity.ok().body("success");
    }

}
