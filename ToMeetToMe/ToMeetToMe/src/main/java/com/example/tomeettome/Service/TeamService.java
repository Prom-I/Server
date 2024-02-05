package com.example.tomeettome.Service;

import com.example.tomeettome.Constant.OWNERTYPE;
import com.example.tomeettome.DTO.UserDTO;
import com.example.tomeettome.Model.CalendarPermissionEntity;
import com.example.tomeettome.Model.TeamEntity;
import com.example.tomeettome.Model.UserEntity;
import com.example.tomeettome.Repository.CalendarPermissionRepository;
import com.example.tomeettome.Repository.TeamRepository;
import com.example.tomeettome.Repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Example;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import javax.persistence.EntityNotFoundException;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
public class TeamService {
    @Autowired
    private UserRepository userRepository;

    @Autowired TeamRepository teamRepository;
    @Autowired CalendarPermissionRepository calendarPermissionRepository;

    public TeamEntity createTeam(TeamEntity team) {
        return teamRepository.save(team);
    }

    public boolean checkUserExistenceInTeam(TeamEntity teamEntity , String userId){
        Specification<CalendarPermissionEntity> spec =
                CalendarPermissionRepository.findCalendarPermission(teamEntity.getUid(),userId);
        // Return true if there is a value present, otherwise false.
        return calendarPermissionRepository.findOne(spec).isPresent();
    }

    public TeamEntity retrieveTeamEntity(String groupOriginKey) {
        return teamRepository.findByUid(groupOriginKey);
    }

    public List<TeamEntity> retrieveTeamEntities(String userId) {
        List<TeamEntity> entities = new ArrayList<>();

        Specification<CalendarPermissionEntity> spec = CalendarPermissionRepository.findTeamOriginKeysByOwnerTypeAndUserId(OWNERTYPE.TEAM.name(),userId);
        List<CalendarPermissionEntity> permissions = calendarPermissionRepository.findAll(spec);
        for(CalendarPermissionEntity p : permissions){
            entities.add(teamRepository.findByUid(p.getOwnerOriginKey()));
        }
        return entities;
    }

    public List<UserEntity> retrieveTeamUsers(String teamIcsFileName){
        List<CalendarPermissionEntity> permissions = calendarPermissionRepository.findByIcsFileName(teamIcsFileName);

        List<UserEntity> result = new ArrayList<>();
        for (CalendarPermissionEntity p : permissions) {
            result.add(userRepository.findByUserId(p.getUserId()));
        }
        return result;
    }

    public void increaseNumOfUsers(String teamUid) {
        TeamEntity entity = teamRepository.findByUid(teamUid);
        entity.setNumOfUsers(entity.getNumOfUsers()+1);
        teamRepository.save(entity);
    }

    public Boolean isUserFounderOfTeam(String userId, String teamOriginKey) {
        return teamRepository.findByUid(teamOriginKey).getFounderId().equals(userId);
    }

    public TeamEntity updateTeam(String teamOriginKey, final TeamEntity entity) {
        teamRepository.findOne(Example.of(TeamEntity.builder().uid(teamOriginKey).build())).ifPresentOrElse(
                teamEntity -> {
                    teamEntity.setName(entity.getName() == null ? teamEntity.getName() : entity.getName());
                    teamEntity.setFounderId(entity.getFounderId() == null ? teamEntity.getFounderId() : entity.getFounderId());
                    teamEntity.setImage(entity.getImage() == null ? teamEntity.getImage():entity.getImage());

                    teamRepository.save(teamEntity);
                } , // Consumer (값이 존재할때 ifPresnet)
                () -> {throw new EntityNotFoundException();} // Runnable (값이 존재하지 않을 때 or Else)
        );
        return teamRepository.findByUid(entity.getUid());
    }


    public void deleteTeamUser(String teamOriginKey, UserDTO userDTO) {
            teamRepository.findOne(Example.of(teamRepository.findByUid(teamOriginKey))).ifPresent(
                    teamEntity -> {
                        teamEntity.setNumOfUsers(teamEntity.getNumOfUsers()-1);
                    }
            );

            calendarPermissionRepository.delete(
                    calendarPermissionRepository.findOne(
                            Example.of(CalendarPermissionEntity.builder()
                                    .userId(userDTO.getUserId())
                                    .ownerOriginKey(teamOriginKey)
                                    .build())
                    ).orElseThrow()
            );
    }

    public void deleteTeam(String teamUid) {
        teamRepository.deleteById(teamUid);
    }

    public boolean isTeamEmptyByIcsFileName(String teamIcsFileName) {
        if(calendarPermissionRepository.findByIcsFileName(teamIcsFileName).isEmpty()) {
            return true;
        }
        else return false;
    }

}
