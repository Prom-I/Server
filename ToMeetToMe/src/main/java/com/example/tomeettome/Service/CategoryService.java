package com.example.tomeettome.Service;

import com.example.tomeettome.Model.CalendarEntity;
import com.example.tomeettome.Model.CategoryEntity;
import com.example.tomeettome.Repository.CategoryRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class CategoryService {
    @Autowired CategoryRepository categoryRepository;

    public CategoryEntity init(CalendarEntity entity) {
        CategoryEntity category = CategoryEntity.builder()
                .calendarOriginKey(entity.getOriginKey())
                .name("DEFAULT")
                .scope("PUBLIC")
                .color("WHITE")
                .build();
        return categoryRepository.save(category);
    }

    public String findOriginKeyByName(String name) {
        return categoryRepository.findByName(name).getOriginKey();
    }


}
