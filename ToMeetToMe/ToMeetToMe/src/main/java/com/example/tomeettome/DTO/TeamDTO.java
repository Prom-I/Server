package com.example.tomeettome.DTO;

import com.example.tomeettome.Model.ScheduleEntity;
import com.example.tomeettome.Model.TeamEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Data
public class TeamDTO {
    private String name;
    private String founderId;
    private String image;
    private String [] teamUsers;
    private String icsFileName;

    public TeamDTO(TeamEntity entity) {
        this.name = entity.getName();
        this.founderId = entity.getFounderId();
        this.image = entity.getImage();
    }

    public void setTeamUsers(List<String> teamUsers) {
        this.teamUsers = teamUsers.toArray(new String[0]);
    }

    // DTO -> Entity 변환
    public static TeamEntity toEntity(final TeamDTO dto) {
        return TeamEntity.builder()
                .name(dto.getName())
                //.founderId(dto.getFounderId())
                .image(dto.getImage())
                .numOfUsers(dto.teamUsers.length)
                .build();
    }
}
