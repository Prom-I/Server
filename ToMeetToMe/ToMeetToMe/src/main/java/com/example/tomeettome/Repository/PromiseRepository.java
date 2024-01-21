package com.example.tomeettome.Repository;

import com.example.tomeettome.Model.PromiseEntity;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PromiseRepository extends JpaRepository<PromiseEntity, String> {
    List<PromiseEntity> findByIcsFileName(String icsFileName);
    List<PromiseEntity> findAll(Specification<PromiseEntity> spec);

    static Specification<PromiseEntity> getConfirmedPromise(String icsFileName) {
        return (root, query, builder) ->
            builder.and(builder.equal(root.get("icsFileName"), icsFileName),
                    builder.equal(root.get("status"), "CONFIRMED"));

    }
}
