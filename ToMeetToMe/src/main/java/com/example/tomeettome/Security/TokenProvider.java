package com.example.tomeettome.Security;

import com.example.tomeettome.Model.UserEntity;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
public class TokenProvider {
    private static final String SECRET_KEY = "2B738E9FDA91B5366619C5D21156F";

    public String create(UserEntity userEntity) {
        Date expiryDate = Date.from(
                Instant.now().plus(1, ChronoUnit.DAYS));

        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", userEntity.getUserId());

        return Jwts.builder()
                .signWith(SignatureAlgorithm.HS512, SECRET_KEY)
                .setClaims(claims)
                .setIssuer("ToMeetToMe")
                .setIssuedAt(new Date())
                .setExpiration(expiryDate)
                .compact();
    }

    public String validateAndGetUserId(String token) {
        try {
            Claims claims = Jwts.parser()
                    .setSigningKey(SECRET_KEY)
                    .parseClaimsJws(token)
                    .getBody();

            return claims.get("userId", String.class);
        }
        catch (Exception e){
            log.error("Error validating token: {}", e.getMessage());
            return null;

        }
    }

<<<<<<< e3f7b12cea76e31cf041f350eb02ab253c212957
<<<<<<< e3f7b12cea76e31cf041f350eb02ab253c212957

=======
    public TokenResponseDTO servicesRedirect (AuthCodeDTO dto) {
        String code = dto.getCode();
        String client_secret =
=======
    public String validateIdTokenAndGetEmail(String idToken) {
        try {
            Claims claims = Jwts.parser()
                    .parseClaimsJws(idToken)
                    .getBody();
            if (claims.get("email_verified", String.class).equals("true")) {
                return claims.get("email", String.class);
            }
            else return null;
        }
        catch (Exception e) {
            log.error("Error validating token: {}", e.getMessage());
            return null;
        }
>>>>>>> FEAT/ Apple 소셜로그인 플로우 완료
    }
>>>>>>> FEAT/ Apple 소셜 로그인 구현 중
}
