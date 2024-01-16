package com.example.tomeettome.Repository;

import com.example.tomeettome.Model.CategoryEntity;
import com.example.tomeettome.Model.ScheduleEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CategoryRepository extends JpaRepository<CategoryEntity, String> {
    CategoryEntity findByName(String name);
    List<CategoryEntity> findByIcsFileName(String icsFileName);
    CategoryEntity findByUid(String uid);

}