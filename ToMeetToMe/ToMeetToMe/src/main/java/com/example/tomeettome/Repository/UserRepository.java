package com.example.tomeettome.Repository;

import com.example.tomeettome.Model.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserRepository extends JpaRepository<UserEntity, String> {

    boolean existsByUserId(String userId);
    UserEntity findByUserId(String userId);
    List<UserEntity> findAll();
    List<UserEntity> findByUserNameLike(String pattern);
    UserEntity findByFcmToken(String fcmToken);
}