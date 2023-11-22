package com.example.tomeettome.Service;

import com.example.tomeettome.Model.UserEntity;
import com.example.tomeettome.Repository.UserRepository;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.io.*;
import java.security.GeneralSecurityException;
import java.util.Arrays;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken.Payload;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;

@Slf4j
@Service
public class UserService {

    @Autowired UserRepository userRepository;

    public UserEntity validateIdToken(String idTokenString) throws GeneralSecurityException, IOException {

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
//            boolean emailVerified = Boolean.valueOf(payload.getEmailVerified());
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
                    .userName(name)
                    .userId(email)
                    .platform("Google")
                    .build();
            return user;
        } else {
            log.warn("Invalid ID token.");
            return null;
        }
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

}
