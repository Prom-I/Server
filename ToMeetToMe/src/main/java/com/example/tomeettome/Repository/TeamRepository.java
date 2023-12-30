package com.example.tomeettome.Repository;

import com.example.tomeettome.Model.TeamEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TeamRepository extends JpaRepository<TeamEntity, String> {
    TeamEntity findByOriginKey(String originKey);
}