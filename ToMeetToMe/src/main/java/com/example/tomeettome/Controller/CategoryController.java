package com.example.tomeettome.Controller;

import com.example.tomeettome.DTO.*;
import com.example.tomeettome.Model.CategoryEntity;
import com.example.tomeettome.Model.ScheduleEntity;
import com.example.tomeettome.Service.CategoryService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.text.ParseException;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/category")
public class CategoryController {

    @Autowired CategoryService categoryService;

    @PostMapping("/create/{icsFileName}")
    public ResponseEntity<?> create(@PathVariable("icsFileName") String icsFileName,
                                    @RequestBody CategoryDTO dto) {
        CategoryEntity category = dto.toEntity(dto);
        category.setIcsFileName(icsFileName);
        category = categoryService.create(category);

        CategoryDTO categoryDTO = CategoryDTO.builder()
                .icsFileName(category.getIcsFileName())
                .color(category.getColor())
                .name(category.getName())
                .scope(category.getScope())
                .build();
        ResponseDTO<CategoryDTO> response = ResponseDTO.<CategoryDTO>builder().data(Collections.singletonList(categoryDTO)).status("success").build();
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/retrieve/{icsFileName}")
    public ResponseEntity<?> retrieve(@PathVariable("icsFileName") String icsFileName) {
        List<CategoryEntity> categories = categoryService.retrieve(icsFileName);
        List<CategoryDTO> categoriesDTO = categories.stream().map(CategoryDTO::new).collect(Collectors.toList());

        ResponseDTO<CategoryDTO> response = ResponseDTO.<CategoryDTO>builder().data(categoriesDTO).status("success").build();
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @GetMapping("/retrieve/schedules/{categoryUid}")
    public ResponseEntity<?> retrieveSchedules(@PathVariable("categoryUid") String categoryUid) throws ParseException {
        List<ScheduleEntity> schedules = categoryService.retrieveSchedules(categoryUid);
        return ResponseEntity.status(HttpStatus.OK).body(CaldavDTO.setScheduleValue(schedules));
    }

    @PatchMapping("/update/{icsFileName}")
    public ResponseEntity<?> update(@PathVariable("icsFileName") String icsFileName,
                                    @RequestBody CategoryDTO dto) {
        try {
            CategoryEntity category = dto.toEntity(dto);
            category.setIcsFileName(icsFileName);
            category = categoryService.update(category);

            CategoryDTO categoryDTO = CategoryDTO.builder()
                    .uid(category.getUid())
                    .icsFileName(category.getIcsFileName())
                    .name(category.getName())
                    .scope(category.getScope())
                    .color(category.getColor())
                    .build();
            ResponseDTO<CategoryDTO> response = ResponseDTO.<CategoryDTO>builder().data(Collections.singletonList(categoryDTO)).status("success").build();

            return ResponseEntity.status(HttpStatus.OK).body(response);
        }
        catch (NullPointerException e) {
            return ResponseEntity.badRequest().body(null);
        }
    }

    @DeleteMapping("/delete/{categoryUid}")
    public ResponseEntity<?> delete(@PathVariable("categoryUid") String uid) {
        try {
            categoryService.delete(uid);

            return ResponseEntity.status(HttpStatus.NO_CONTENT).body(null);

        } catch (Exception e) {
            String error = e.getMessage();
            ResponseDTO response = ResponseDTO.<ScheduleEntity>builder().error(error).build();
            return ResponseEntity.badRequest().body(response);
        }
    }
}
