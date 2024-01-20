package com.example.tomeettome.Repository;

import com.example.tomeettome.Model.VoteEntity;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;

public interface VoteRepository extends JpaRepository<VoteEntity, String>, JpaSpecificationExecutor<VoteEntity> {
    List<VoteEntity> findByPreferenceUid(String preferenceUid);
    List<VoteEntity> findAll(Specification<VoteEntity> spec);

    static Specification<VoteEntity> findPreferenceByUser(String preferenceUid, String userUid) {
        return (root, query, builder) ->
                builder.and(
                        builder.equal(root.get("preferenceUid"), preferenceUid),
                        builder.equal(root.get("userUid"), userUid)
                );
    }
}
