package com.example.tomeettome.Repository;

import com.example.tomeettome.Model.VoteEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface VoteRepository extends JpaRepository<VoteEntity, String> {

}
