package com.example.tomeettome.Service;

import com.example.tomeettome.Constant.OWNERTYPE;
import com.example.tomeettome.Constant.PERMISSIONLEVEL;
import com.example.tomeettome.DTO.Apple.AppleKeyDTO;
import com.example.tomeettome.Model.CalendarPermissionEntity;
import com.example.tomeettome.Model.UserEntity;
import com.example.tomeettome.Repository.CalendarPermissionRepository;
import com.example.tomeettome.Repository.UserRepository;
import com.example.tomeettome.Security.TokenProvider;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONArray;
import org.springframework.beans.factory.annotation.Autowired;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import java.io.*;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken.Payload;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

@Slf4j
@Service
public class UserService {

    @Autowired UserRepository userRepository;
    @Autowired CalendarPermissionRepository calendarPermissionRepository;
    @Autowired TokenProvider tokenProvider;


    public UserEntity validateIdTokenGoogle(String idTokenString) throws GeneralSecurityException, IOException {

        GoogleIdTokenVerifier verifier = new GoogleIdTokenVerifier.Builder(getHttpTransport(), getJsonFactory())
                // Specify the CLIENT_ID of the app that accesses the backend:
                //.setAudience(Collections.singletonList(client_id)
                .setAudience(Arrays.asList("317253442779-2n3971n1mg5p0piue9ekfl9bnt20v41p.apps.googleusercontent.com", "317253442779-aj2npvsq0m9r6op406ej550cheqm5qdj.apps.googleusercontent.com", "317253442779-7iq9rqdlmkut3i60giveb7bf28kosacr.apps.googleusercontent.com"))
                // Or, if multiple clients access the backend:
                //.setAudience(Arrays.asList(CLIENT_ID_1, CLIENT_ID_2, CLIENT_ID_3))
                .build();

        // (Receive idTokenString by HTTPS POST)

        GoogleIdToken idToken = verifier.verify(idTokenString);
        if (idToken != null) {
            Payload payload = idToken.getPayload();

            // Print user identifier
            String userId = payload.getSubject();
            log.info("User ID: " + userId);

            // Get profile information from payload
            String email = payload.getEmail();
//          boolean emailVerified = Boolean.valueOf(payload.getEmailVerified());
            String name = (String) payload.get("name");
            String pictureUrl = (String) payload.get("picture");
            String locale = (String) payload.get("locale");
            String familyName = (String) payload.get("family_name");
            String givenName = (String) payload.get("given_name");

            log.info("Email: " + email);
//            log.info("Email Verified: " + emailVerified);
            log.info("Name: " + name);
            log.info("Picture URL: " + pictureUrl);
            log.info("Locale: " + locale);
            log.info("Family Name: " + familyName);
            log.info("Given Name: " + givenName);

            UserEntity user = UserEntity.builder()
                    .userName(name + "#" +userRepository.findAll().size())
                    .userId(email)
                    .platform("Google")
                    .build();
            return user;
        } else {
            log.warn("Invalid ID token.");
            return null;
        }
    }

    public String createAuthCodeKakao(String code) throws JSONException {
        String result="";
        String requestUrl = "https://kauth.kakao.com/oauth/token";

        //보낼 파라메터 셋팅
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("grant_type", "authorization_code");
        params.add("client_id","5ce66229811ad9d602663cadba712f32");
        params.add("redirect_uri","https://www.tmtm.site/user/kakao/callback");
        params.add("code",code);
        params.add("client_secret", "tp7pv9xiq7MIJ9rRgotjQDdrpM8mjD2S");

        //헤더셋팅
        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-type", "application/x-www-form-urlencoded;charset=utf-8");

        //파라메터와 헤더 합치기
        HttpEntity<MultiValueMap<String, String>> entity = new HttpEntity<>(params, headers);
        //RestTemplate 초기화
        RestTemplate rt = new RestTemplate();

        //전송 및 결과 처리
        ResponseEntity<String> response = rt.exchange(
                requestUrl,
                HttpMethod.POST,
                entity,
                String.class
        );
        result = response.getBody(); //리턴되는 결과의 body를 저장.result.sp
        JSONObject jsonObject = new JSONObject(result);
        String idToken = jsonObject.getString("id_token");
        String accessToken = jsonObject.getString("access_token");

//        byte[] decodedBytes = Base64.getDecoder().decode(idToken);
//        String decodedStr = new String(decodedBytes);

//        log.info("decoded : ", decodedStr );
        return accessToken;
    }

    public UserEntity getUserInfoKaKao(String accessToken) throws JSONException {
        String result="";
        String requestUrl = "https://kapi.kakao.com/v1/oidc/userinfo";

        //보낼 파라메터 셋팅
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();

        //헤더셋팅
        HttpHeaders headers = new HttpHeaders();
        headers.add("Authorization", "Bearer " + accessToken);

        //파라메터와 헤더 합치기
        HttpEntity<MultiValueMap<String, String>> entity = new HttpEntity<>(params, headers);
        //RestTemplate 초기화
        RestTemplate rt = new RestTemplate();

        //전송 및 결과 처리
        ResponseEntity<String> response = rt.exchange(
                requestUrl,
                HttpMethod.GET,
                entity,
                String.class
        );
        JSONObject jsonObject = new JSONObject(response.getBody());

        UserEntity user = UserEntity.builder()
                .userId(jsonObject.getString("email"))
                .build();

        if(!(jsonObject.getBoolean("email_verified"))) {
            throw new IllegalArgumentException("Email Not Verified");
        }

        return user;
    }

    public void validateAccessTokenKakao(String accessToken) {
        String result="";
        String requestUrl = "https://kapi.kakao.com/v1/user/access_token_info";

        //보낼 파라메터 셋팅
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();

        //헤더셋팅
        HttpHeaders headers = new HttpHeaders();
        headers.add("Authorization", "Bearer " + accessToken);

        //파라메터와 헤더 합치기
        HttpEntity<MultiValueMap<String, String>> entity = new HttpEntity<>(params, headers);
        //RestTemplate 초기화
        RestTemplate rt = new RestTemplate();

        //전송 및 결과 처리
        ResponseEntity<String> response = rt.exchange(
                requestUrl,
                HttpMethod.GET,
                entity,
                String.class
        );
    }

    public String createIdTokenApple(String code) {
        String result = "";
        String requestUrl = "https://appleid.apple.com/auth/token";

        //보낼 파라메터 셋팅
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("client_id","com.ToMeetToMe.services");
        params.add("client_secret", tokenProvider.createClientSecret());
        params.add("grant_type", "authorization_code");
        params.add("redirect_uri","https://www.tmtm.site/user/apple/callback");
        params.add("code", code);

        //헤더셋팅
        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-type", "application/x-www-form-urlencoded;charset=utf-8");

        //파라메터와 헤더 합치기
        HttpEntity<MultiValueMap<String, String>> entity = new HttpEntity<>(params, headers);
        //RestTemplate 초기화
        RestTemplate rt = new RestTemplate();

        //전송 및 결과 처리
        ResponseEntity<String> response = rt.exchange(
                requestUrl,
                HttpMethod.POST,
                entity,
                String.class
        );

        result = response.getBody();

        JSONObject jsonObject = new JSONObject(result);
        String idToken = jsonObject.getString("id_token");

        return idToken;
    }

    public List<AppleKeyDTO> createPublicKeyApple() {
        String result = "";
        String requestUrl = "https://appleid.apple.com/auth/keys";
        //헤더셋팅
        HttpHeaders headers = new HttpHeaders();

        //파라메터와 헤더 합치기
        HttpEntity<MultiValueMap<String, String>> entity = new HttpEntity<>(headers);
        //RestTemplate 초기화
        RestTemplate rt = new RestTemplate();

        //전송 및 결과 처리
        ResponseEntity<String> response = rt.exchange(
                requestUrl,
                HttpMethod.GET,
                entity,
                String.class
        );

        result = response.getBody();

        JSONObject jsonObject = new JSONObject(result);
        JSONArray keys = (JSONArray) jsonObject.get("keys");

        List<AppleKeyDTO> keyList = new ArrayList<>();
        for (int i = 0; i < keys.length(); i++) {

            JSONObject keyObject = (JSONObject) keys.get(i);

            AppleKeyDTO dto = AppleKeyDTO.builder()
                    .kty(keyObject.getString("kty"))
                    .kid(keyObject.getString("kid"))
                    .use(keyObject.getString("use"))
                    .alg(keyObject.getString("alg"))
                    .n(keyObject.getString("n"))
                    .e(keyObject.getString("e"))
                    .build();

            keyList.add(dto);
        }
        return keyList;
    }

    private HttpTransport getHttpTransport() {
        return new NetHttpTransport();
    }

    private JsonFactory getJsonFactory() {
        JsonFactory jsonFactory = new JacksonFactory(); //lo 예시로 JacksonFactory 사용
        return jsonFactory;
    }

    public UserEntity create(UserEntity user) {
        if(user == null || user.getUserId() == null ) {
            throw new RuntimeException("Invalid arguments");
        }
        final String id = user.getUserId();
        if(userRepository.existsByUserId(id)) {
            log.warn("id already exists {}", id);
            throw new RuntimeException("id already exists");
        }
        log.info("id 생성 완료! " + user);
        return userRepository.save(user);
    }

    public boolean checkUserExists(String userId) {
        return userRepository.existsByUserId(userId);
    }

    public List<UserEntity> retrieve(String pattern) {
        return userRepository.findByUserNameLike("%" + pattern + "%");
    }

    // 친구 조회 API
    // 내가 갖고 있는 cp에서 ownertype이 User이고 permissionlevel이 member인거
    public List<UserEntity> retrieveFollowings(String userId) {
        Specification<CalendarPermissionEntity> spec = CalendarPermissionRepository.findFollowingLists(userId);
        List<CalendarPermissionEntity> permissions = calendarPermissionRepository.findAll(spec);
        List<UserEntity> users = new ArrayList<>();
        for (CalendarPermissionEntity p : permissions) {
            String followingId = PreferenceService.getUserIdFromIcsFileName(p.getIcsFileName());
            users.add(userRepository.findByUserId(followingId));
        }
        return users;
    }

    public UserEntity retrieveProfile(String userId) {
        return userRepository.findByUserId(userId);
    }

    public UserEntity updateProfileImage(String userId, String image) {
        UserEntity user = userRepository.findByUserId(userId);
        user.setImage(image);
        userRepository.save(user);
        return userRepository.findByUserId(userId);
    }

    public UserEntity updateProfileUserName(String userId, String userName) {
        UserEntity user = userRepository.findByUserId(userId);
        String [] tag = user.getUserName().split("#");
        user.setUserName(userName + "#" +tag[1]);
        userRepository.save(user);
        return userRepository.findByUserId(userId);
    }

    public Boolean saveFcmToken(String userId, String token) {
        UserEntity user = userRepository.findByUserId(userId);
        if (user == null) return false;

        user.setFcmToken(token);
        userRepository.save(user);
        return true;
    }

    // follower가 following을 친구 추가 요청 보냄
    // 수락 요청을 받기 전까지는 permission level "guest"
    public void followRequest(String followerId, String followingId) {
        UserEntity follower = userRepository.findByUserId(followerId);
        UserEntity following = userRepository.findByUserId(followingId);

        if(follower != null && following != null) {
            CalendarPermissionEntity calendarPermission = CalendarPermissionEntity.builder()
                    .ownerType(OWNERTYPE.USER.name())
                    .ownerOriginKey(following.getUid())
                    .userId(follower.getUserId())
                    .permissionLevel(PERMISSIONLEVEL.GUEST.name())
                    .icsFileName(following.getUserId() + ".ics")
                    .build();
            calendarPermissionRepository.save(calendarPermission);
        }
        else {
            log.error("user not exist");
        }
    }

    public void acceptFollowRequest(String followerId, String followingId) {
        UserEntity follower = userRepository.findByUserId(followerId);
        UserEntity following = userRepository.findByUserId(followingId);

        if(follower != null && following != null) {
            // Specification
            // following-ownerOriginKey(권한의 대상자), follower-userId(권한의 소유자)
            // follower은 following의 permission을 가짐
            Specification<CalendarPermissionEntity> spec = CalendarPermissionRepository.findCalendarPermission(following.getUid(), follower.getUserId());
            CalendarPermissionEntity calendarPermission = calendarPermissionRepository.findOne(spec).get();
            calendarPermission.setPermissionLevel(PERMISSIONLEVEL.MEMBER.name());
            calendarPermissionRepository.save(calendarPermission);
        }
        else {
            log.error("user not exist");
        }
    }

    public void deleteCalenderPermission(String followerId, String followingId) {
        UserEntity follower = userRepository.findByUserId(followerId);
        UserEntity following = userRepository.findByUserId(followingId);

        if(follower != null && following != null) {
            // Specification
            // following-ownerOriginKey(권한의 대상자), follower-userId(권한의 소유자)
            // follower은 following의 permission을 가짐
            Specification<CalendarPermissionEntity> spec = CalendarPermissionRepository.findCalendarPermission(following.getUid(), follower.getUserId());
            CalendarPermissionEntity calendarPermission = calendarPermissionRepository.findOne(spec).orElseThrow();
            calendarPermissionRepository.delete(calendarPermission);
        }
        else {
            log.error("user not exist");
        }
    }

    public String findFcmTokenByUserId(String userId) {
        return userRepository.findByUserId(userId).getFcmToken();
    }
}
