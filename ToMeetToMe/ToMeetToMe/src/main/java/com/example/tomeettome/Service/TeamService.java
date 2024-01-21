package com.example.tomeettome.Service;

import com.example.tomeettome.Constant.OWNERTYPE;
import com.example.tomeettome.Model.CalendarPermissionEntity;
import com.example.tomeettome.Model.TeamEntity;
import com.example.tomeettome.Repository.CalendarPermissionRepository;
import com.example.tomeettome.Repository.TeamRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Example;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
public class TeamService {

    @Autowired TeamRepository teamRepository;
    @Autowired CalendarPermissionRepository calendarPermissionRepository;

    public TeamEntity createTeam(TeamEntity team) {
        return teamRepository.save(team);
    }

    public boolean checkUserExistenceInTeam(TeamEntity teamEntity , String userId){
        Specification<CalendarPermissionEntity> spec =
                CalendarPermissionRepository.findCalendarPermission(teamEntity.getOriginKey(),userId);
        // Return true if there is a value present, otherwise false.
        return calendarPermissionRepository.findOne(spec).isPresent();
    }

    public TeamEntity getTeamEntityByOriginKey(String groupOriginKey) {
        return teamRepository.findByOriginKey(groupOriginKey);
    }

    public List<TeamEntity> retrieveTeamByUserId(String userId) {
        List<TeamEntity> entities = new ArrayList<>();
        List<String> teamOriginKeys = calendarPermissionRepository.findTeamOriginKeysByOwnerTypeAndUserId(OWNERTYPE.TEAM.name(),userId);
        for(String teamOriginKey : teamOriginKeys){
            entities.add(teamRepository.findByOriginKey(teamOriginKey));
        }

        return entities;
    }

    public void increaseNumOfUsers(String groupOriginKey) {
        TeamEntity entity = teamRepository.findOne(Example.of(TeamEntity.builder().originKey(groupOriginKey).build())).orElseThrow();
        entity.setNumOfUsers(entity.getNumOfUsers()+1);
        teamRepository.save(entity);
    }
}
