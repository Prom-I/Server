package com.example.tomeettome.Security;

import com.example.tomeettome.DTO.Apple.AppleKeyDTO;
import com.example.tomeettome.Model.UserEntity;
import com.nimbusds.jwt.SignedJWT;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigInteger;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.RSAPublicKeySpec;
import java.text.ParseException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;

@Slf4j
@Service
public class TokenProvider {
    private static final String SECRET_KEY = "2B738E9FDA91B5366619C5D21156F";
    private static final String APPLE_KEY = "MIGTAgEAMBMGByqGSM49AgEGCCqGSM49AwEHBHkwdwIBAQQghB8f+subbfCE7QfG" +
            "TLP2HZ9uKi9zF0pOS3M6RV2r5OegCgYIKoZIzj0DAQehRANCAASiIvAhjFE5YsLj" +
            "efzoWlxFpEzjD8+VywkAo6nw/Vh5RtdwD/hBDtEbg9Fmo4cKaLFPXDwBvcAktuJP" +
            "fuEALxMh";

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

    // Apple 용 ClientSecret을 생성하는 함수
    public String createClientSecret() {

        Date expiryDate = Date.from(
                Instant.now().plus(1, ChronoUnit.DAYS));
        return Jwts.builder()
                .setHeaderParam("alg", "ES256")
                .setHeaderParam("kid", "PLN2475U77") // Apple Developer 페이지에 명시되어있는 Key ID
                .setIssuer("WJKNV9KXF2") // Team ID
                .setAudience("https://appleid.apple.com")
                .setExpiration(expiryDate)
                .signWith(SignatureAlgorithm.ES256, APPLE_KEY)
                .setSubject("com.ToMeetToMe.services")
                .compact();

    }

    public String validateAndGetUserId(String token) {
        try {
            Claims claims = Jwts.parser()
                    .setSigningKey(SECRET_KEY)
                    .parseClaimsJws(token)
                    .getBody();

            return claims.get("userId", String.class);
        } catch (Exception e) {
            log.error("Error validating token: {}", e.getMessage());
            return null;
        }
    }

    public String validateIdTokenAndGetUserIdApple(String idToken, List<AppleKeyDTO> keys) throws ParseException {
        for (AppleKeyDTO dto : keys) {
            SignedJWT decodedJWT = SignedJWT.parse(idToken);
            String kid = decodedJWT.getHeader().getKeyID();

            if (dto.getKid().equals(kid)) {
                try {
                    PublicKey pk = createPublicKey(dto);
                    Claims claims = Jwts.parser()
                            .setSigningKey(pk)
                            .parseClaimsJws(idToken)
                            .getBody();

                    return claims.get("email", String.class);
                } catch (Exception e) {
                    log.error("Error validating token: {}", e.getMessage());
                    return null;
                }
            }
        }
        return null;
    }

    public PublicKey createPublicKey(AppleKeyDTO dto) throws InvalidKeySpecException, NoSuchAlgorithmException {
        byte[] nBytes = Base64.getUrlDecoder().decode(dto.getN());
        byte[] eBytes = Base64.getUrlDecoder().decode(dto.getE());

        BigInteger n = new BigInteger(1, nBytes);
        BigInteger e = new BigInteger(1, eBytes);

        RSAPublicKeySpec publicKeySpec = new RSAPublicKeySpec(n, e);
        KeyFactory keyFactory = KeyFactory.getInstance(dto.getKty());

        return keyFactory.generatePublic(publicKeySpec);

    }
}

