package com.example.tomeettome.DTO.Apple;

import lombok.*;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
@Getter
@Setter
public class AuthorizationDTO {
    private String code;
    private String id_token;
    private String state;
}
