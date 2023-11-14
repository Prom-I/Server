package com.example.tomeettome.DTO;

import com.example.tomeettome.Model.UserEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Data
public class UserDTO {
    private String userId;
    private String userName;
    private String image;
    private String token; // user 응답으로 token을 보내줘야 해서 필요
    private String platform;
    private LocalDateTime lastModifiedAt;
    private LocalDateTime createdAt;

    public UserDTO(final UserEntity entity) {
        this.userId = entity.getUserId();
        this.userName = entity.getUserName();
        this.image = entity.getImage();
        this.platform = entity.getPlatform();
        this.lastModifiedAt = entity.getLastModifiedAt();
        this.createdAt = entity.getCreatedAt();
    }
    public static UserEntity toEntity(UserDTO userDTO){
        return UserEntity.builder()
                .userId(userDTO.getUserId())
                .image(userDTO.getImage())
                .userName(userDTO.getUserName())
                .platform(userDTO.getPlatform())
                .build();
    }
}

