package com.example.tomeettome.DTO.Apple;

import lombok.*;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
@Getter
@Setter
public class AppleKeyDTO {
    private String kty;
    private String kid;
    private String use;
    private String alg;
    private String n;
    private String e;
}
