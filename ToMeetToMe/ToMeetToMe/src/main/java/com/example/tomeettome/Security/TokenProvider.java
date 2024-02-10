package com.example.tomeettome.Security;

import com.example.tomeettome.DTO.Apple.AppleKeyDTO;
import com.example.tomeettome.Model.UserEntity;
import com.google.api.client.util.PemReader;
import com.nimbusds.jwt.SignedJWT;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import lombok.extern.slf4j.Slf4j;
import org.bouncycastle.asn1.pkcs.PrivateKeyInfo;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.*;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.RSAPublicKeySpec;
import java.text.ParseException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;
import java.lang.*;

@Slf4j
@Service
public class TokenProvider {
    @Value("${spring.security.secret-key}") String SECRET_KEY;

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
    public String createClientSecret() throws IOException, NoSuchAlgorithmException, InvalidKeySpecException {
        Date expiryDate = Date.from(
                Instant.now().plus(1, ChronoUnit.DAYS));
        return Jwts.builder()
                .setHeaderParam("alg", "ES256")
                .setHeaderParam("kid", "PLN2475U77") // Apple Developer 페이지에 명시되어있는 Key ID
                .setIssuer("WJKNV9KXF2") // Team ID
                .setAudience("https://appleid.apple.com")
                .setExpiration(expiryDate)
                .signWith(SignatureAlgorithm.ES256, getPrivateKey())
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

    private PrivateKey getPrivateKey() throws IOException, NoSuchAlgorithmException, InvalidKeySpecException {
        // .p8 파일을 ClassPathResource를 통해 로드
        ClassPathResource resource = new ClassPathResource("static/AuthKey_PLN2475U77.p8");
        String privateKeyContent = new String(resource.getInputStream().readAllBytes(), StandardCharsets.UTF_8)
                .replace("-----BEGIN PRIVATE KEY-----", "")
                .replace("-----END PRIVATE KEY-----", "")
                .replaceAll("\\s", "");

        // Base64 디코딩 및 KeyFactory를 사용해 PrivateKey 객체 생성
        byte[] pkcs8EncodedBytes = Base64.getDecoder().decode(privateKeyContent);
        PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(pkcs8EncodedBytes);
        KeyFactory keyFactory = KeyFactory.getInstance("EC");
        return keyFactory.generatePrivate(keySpec);
    }

//    public PrivateKey getPrivateKey() throws IOException {
//        ClassPathResource resource = new ClassPathResource("static/AuthKey_1234ABCD.p8"); // .p8 key파일 위치
//        String privateKey = new String(Files.readAllBytes(Paths.get(resource.getURI())));
//
////    File f = new File("C:/workspace/AuthKey_1234ABCD.p8");
////    String privateKey = new String(Files.readAllBytes(f.toPath()));
//
//        Reader pemReader = new StringReader(privateKey);
//        PEMParser pemParser = new PEMParser(pemReader);
//        JcaPEMKeyConverter converter = new JcaPEMKeyConverter();
//        PrivateKeyInfo object = (PrivateKeyInfo) pemParser.readObject();
//        return converter.getPrivateKey(object);
//    }
}

