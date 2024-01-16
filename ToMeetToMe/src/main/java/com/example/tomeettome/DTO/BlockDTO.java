package com.example.tomeettome.DTO;

import com.example.tomeettome.Model.AppointmentBlockEntity;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nimbusds.openid.connect.sdk.claims.UserInfo;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.IOException;
import java.sql.Timestamp;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
public class BlockDTO {
    private String uid;
    private String timestamp;
    private int rate;
    private List<UserInfo> attendee;
    private List<UserInfo> absentee;


    public static class UserInfo {
        @JsonProperty
        private String uid;

        @JsonProperty
        private String name;

        // Getter 및 Setter 메소드
        public String getUid() {
            return uid;
        }

        public UserInfo setUid(String uid) {
            this.uid = uid;
            return this;
        }

        public String getName() {
            return name;
        }

        public UserInfo setName(String name) {
            this.name = name;
            return this;
        }
    }

    public BlockDTO(AppointmentBlockEntity entity) {
        this.uid = entity.getUid();
        this.timestamp = entity.getTimestamp().toLocalDateTime().format(DateTimeFormatter.ofPattern("yyyyMMdd'T'HHmmss'Z'"));
        this.rate = entity.getRate();
        try {
            this.attendee = convertJsonToUserInfoList(entity.getAttendee());
            this.absentee = convertJsonToUserInfoList(entity.getAbsentee());
        } catch (IOException e) {
            // IOException 처리 로직
            this.attendee = Collections.emptyList();
            this.absentee = Collections.emptyList();
        }
    }

    // JSON 문자열을 UserInfo 리스트로 변환하는 메서드 구현
    private List<UserInfo> convertJsonToUserInfoList(String json) throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        return objectMapper.readValue(json, new TypeReference<List<UserInfo>>() {});
    }
}
