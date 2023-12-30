package com.example.tomeettome.DTO;

import com.example.tomeettome.Model.UserEntity;
import com.example.tomeettome.Model.VoteEntity;

public class VoteDTO {
    private String preferenceUid;

    public static VoteEntity toEntity(VoteDTO dto) {
        return VoteEntity.builder()
                .preferenceUid(dto.preferenceUid)
                .build();
    }
}
