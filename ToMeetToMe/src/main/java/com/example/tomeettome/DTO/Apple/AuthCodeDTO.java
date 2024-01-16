package com.example.tomeettome.DTO.Apple;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.*;

import java.util.Map;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
@Getter
@Setter
public class AuthCodeDTO {
    private AuthorizationDTO authorization;
    private String user;
}

