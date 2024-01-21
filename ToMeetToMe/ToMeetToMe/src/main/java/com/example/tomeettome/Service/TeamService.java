package com.example.tomeettome.Service;

import com.example.tomeettome.Model.CalendarEntity;
import com.example.tomeettome.Model.CalendarPermissionEntity;
import com.example.tomeettome.Model.TeamEntity;
import com.example.tomeettome.Model.UserEntity;
import com.example.tomeettome.Repository.CalendarPermissionRepository;
import com.example.tomeettome.Repository.CalendarRepository;
import com.example.tomeettome.Repository.CategoryRepository;
import com.example.tomeettome.Repository.TeamRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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
        List<CalendarPermissionEntity> calendarPermissionEntityList = calendarPermissionRepository.findByUserId(userId);
        for (CalendarPermissionEntity entity: calendarPermissionEntityList) {
            if(entity.getOriginKey() == teamEntity.getOriginKey()
                    && entity.getOwnerType() == "team"
                    && entity.getUserId() == userId )
                return true;
        }
        return false;
    }

    public TeamEntity getTeamEntityByOriginKey(String groupOriginKey) {
        return teamRepository.findByOriginKey(groupOriginKey);
    }
}
