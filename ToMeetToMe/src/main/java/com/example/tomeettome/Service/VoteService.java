package com.example.tomeettome.Service;

import com.example.tomeettome.Model.PreferenceEntity;
import com.example.tomeettome.Model.UserEntity;
import com.example.tomeettome.Model.VoteEntity;
import com.example.tomeettome.Repository.PreferenceRepository;
import com.example.tomeettome.Repository.UserRepository;
import com.example.tomeettome.Repository.VoteRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class VoteService {
    @Autowired VoteRepository voteRepository;
    @Autowired UserRepository userRepository;
    @Autowired PreferenceRepository preferenceRepository;

    public PreferenceEntity like(String userId, VoteEntity entity) {
        UserEntity user = userRepository.findByUserId(userId);
        entity.setUserUid(user.getUid());
        // data update
        voteRepository.save(entity);

        PreferenceEntity preference = preferenceRepository.findByUid(entity.getPreferenceUid());
        preference.setLikes(preference.getLikes()+1);

        // preference repo retrieve return
        return preferenceRepository.save(preference);
    }

    public PreferenceEntity dislike(String userId, VoteEntity entity) {
        UserEntity user = userRepository.findByUserId(userId);
        entity.setUserUid(user.getUid());

        // preference update
        PreferenceEntity preference = preferenceRepository.findByUid(entity.getPreferenceUid());
        preference.setLikes(preference.getLikes()-1);
        return preferenceRepository.save(preference);
    }

    // 내가 어떤 Preference에 투표를 했는가
    public PreferenceEntity retreive(){
        return null;
    }
}
