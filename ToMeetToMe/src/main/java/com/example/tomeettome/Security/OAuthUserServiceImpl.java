//package com.example.tomeettome.Security;
//
//import com.example.tomeettome.Model.UserEntity;
//import com.example.tomeettome.Repository.UserRepository;
//import com.fasterxml.jackson.core.JsonProcessingException;
//import com.fasterxml.jackson.databind.ObjectMapper;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
//import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
//import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
//import org.springframework.security.oauth2.core.user.OAuth2User;
//import org.springframework.stereotype.Service;
//
//@Slf4j
//@Service
//public class OAuthUserServiceImpl extends DefaultOAuth2UserService {
//
//    @Autowired
//    private UserRepository userRepository;
//
//    public OAuthUserServiceImpl() {
//        super();
//    }
//
//    @Override
//    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
//        final OAuth2User oAuth2User = super.loadUser(userRequest);
//
//        try {
//            // 테스트 시에만 사용하는 로깅 코드
//            log.info("OAuth2User attributes {}", new ObjectMapper().writeValueAsString(oAuth2User.getAttributes()));
//        } catch (JsonProcessingException e) {
//            throw new RuntimeException(e);
//        }
//
//        final String userId = (String) oAuth2User.getAttributes().get("email");
//
//        final String authProvider = userRequest.getClientRegistration().getClientName();
//
//        UserEntity userEntity = null;
//
//        // 유저가 존재하지 않으면 생성
//        if(!userRepository.existsById(userId)) {
//            userEntity = UserEntity.builder()
//                    .userId(userId)
//                    .build();
//            userRepository.save(userEntity);
//        }
//
//        log.info("Successfully pulled user info username {} authProvider {}",
//                userId);
//
//        return oAuth2User;
//
//
//
//    }
//
//}
