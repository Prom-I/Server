package com.example.tomeettome.Repository;

import com.example.tomeettome.Model.PromiseEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PromiseRepository extends JpaRepository<PromiseEntity, String> {
    List<PromiseEntity> findByIcsFileName(String icsFileName);
}
