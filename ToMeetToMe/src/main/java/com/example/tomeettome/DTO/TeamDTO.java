package com.example.tomeettome.DTO;

import com.example.tomeettome.Model.ScheduleEntity;
import com.example.tomeettome.Model.TeamEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Data
public class TeamDTO {
    private String name;
    private String leaderId;
    private String image;
    private String [] teamUsers;

    // DTO -> Entity 변환
    public static TeamEntity toEntity(final TeamDTO dto) {
        return TeamEntity.builder()
                .name(dto.getName())
                .leaderId(dto.getLeaderId())
                .image(dto.getImage())
                .numOfUsers(dto.teamUsers.length)
                .build();
    }
}
