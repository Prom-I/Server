package com.example.tomeettome.Service;

import com.example.tomeettome.Model.CalendarEntity;
import com.example.tomeettome.Model.CategoryEntity;
import com.example.tomeettome.Model.ScheduleEntity;
import com.example.tomeettome.Repository.CategoryRepository;
import com.example.tomeettome.Repository.ScheduleRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Slf4j
@Service

public class CategoryService {
    @Autowired CategoryRepository categoryRepository;
    @Autowired ScheduleRepository scheduleRepository;


    private String [] colors = {
            "D895A6", "A0A3F2", "A7C9F7", "F6D68C", "E8C1C1",
            "C9BAF3", "CAD1E0", "9E9EA3", "B5E1B2", "FFB877" };

    public List<CategoryEntity> init(CalendarEntity entity) {
        for (int i = 1; i <= 10; i++) {
            CategoryEntity category = CategoryEntity.builder()
                    .icsFileName(entity.getIcsFileName())
                    .name("카테고리" + i)
                    .scope("PUBLIC")
                    .color(colors[i-1])
                    .build();
            categoryRepository.save(category);
        }

        return categoryRepository.findByIcsFileName(entity.getIcsFileName());
    }

    public String findIcsFileNameByName(String name) {
        return categoryRepository.findByName(name).getIcsFileName();
    }

    public CategoryEntity create(CategoryEntity entity) {
        return categoryRepository.save(entity);
    }

    public List<CategoryEntity> retrieve(String icsFileName) {
        return categoryRepository.findByIcsFileName(icsFileName);
    }

    public List<ScheduleEntity> retrieveSchedules(String categoryUid) {
        return scheduleRepository.findByCategoryUid(categoryUid);
    }

    public CategoryEntity update(CategoryEntity entity) {
        Optional<CategoryEntity> original = Optional.ofNullable(categoryRepository.findByUid(entity.getUid()));

        if(original.isEmpty())
            throw new NullPointerException();

        original.ifPresent( category -> {
            category.setName(category.getName() != null ? entity.getName() : category.getName());
            category.setColor(category.getColor() != null ? entity.getColor() : category.getColor());
            category.setScope(category.getScope() != null ? entity.getScope() : category.getScope());
            categoryRepository.save(category);
        });

        return categoryRepository.findByUid(entity.getUid());
    }

    public void delete(String uid) {
        categoryRepository.delete(categoryRepository.findByUid(uid));
    }

}
