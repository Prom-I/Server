package com.example.tomeettome.DTO;

import com.example.tomeettome.Model.CategoryEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
public class CategoryDTO {
    private String uid;
    private String icsFileName;
    private String name;
    private String color;
    private String scope;


    public CategoryDTO(CategoryEntity entity) {
        this.uid = entity.getUid();
        this.icsFileName = entity.getIcsFileName();
        this.name = entity.getName();
        this.color = entity.getColor();
        this.scope = entity.getScope();
    }

    public static CategoryEntity toEntity(final CategoryDTO dto) {
        return CategoryEntity.builder()
                .uid(dto.getUid())
                .icsFileName(dto.getIcsFileName())
                .name(dto.getName())
                .scope(dto.getScope())
                .color(dto.getColor())
                .build();
    }
}
