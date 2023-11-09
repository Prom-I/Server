package com.example.tomeettome.Repository;

import com.example.tomeettome.Model.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserRepository extends JpaRepository<UserEntity, String> {

    UserEntity findByOriginKey(String originKey);
    UserEntity findByUserId(String userId);
    boolean existsById(String userId);
//    UserEntity findByUserNameAndPassword(String userId, String password);
    UserEntity findByUserName(String userId);
    List<UserEntity> findAll();
}