package com.example.tomeettome.DTO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Data
public class TestDTO {
    private String os;
    private String userName;
    private String userId;
    private String msg;
}
