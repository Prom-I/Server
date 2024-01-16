package com.example.tomeettome.DTO;

import com.example.tomeettome.Model.UserEntity;
import com.example.tomeettome.Model.VoteEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Data
public class VoteDTO {
    private String preferenceUid;

    public VoteDTO(VoteEntity entity) {
        this.preferenceUid = entity.getPreferenceUid();
    }

    public static VoteEntity toEntity(VoteDTO dto) {
        return VoteEntity.builder()
                .preferenceUid(dto.preferenceUid)
                .build();
    }
}
