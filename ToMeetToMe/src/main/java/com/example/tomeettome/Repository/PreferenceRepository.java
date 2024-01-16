package com.example.tomeettome.Repository;

import com.example.tomeettome.Model.PreferenceEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PreferenceRepository extends JpaRepository<PreferenceEntity, String> {
    PreferenceEntity findByUid(String uid);
    List<PreferenceEntity> findByPromiseUid(String promiseUid);
}
