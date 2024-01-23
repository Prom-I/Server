package com.example.tomeettome.Repository;

import com.example.tomeettome.Constant.OWNERTYPE;
import com.example.tomeettome.Constant.PERMISSIONLEVEL;
import com.example.tomeettome.Model.CalendarPermissionEntity;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CalendarPermissionRepository extends JpaRepository<CalendarPermissionEntity, String>,
        JpaSpecificationExecutor<CalendarPermissionEntity> {
//    CalendarPermissionEntity findByUserId(String userId);
    List<CalendarPermissionEntity> findByUserId(String userId); // CalendarPermission이 1개 이상일 수 있으므로 List 반환
    List<CalendarPermissionEntity> findByOwnerOriginKey(String ownerOriginKey);
    List<CalendarPermissionEntity> findByIcsFileName(String icsFileName);
    void deleteAllByOwnerOriginKey(String teamOriginKey);

    //CalendarPermissionEntity findOne(Specification<CalendarPermissionEntity> spec);

    static Specification<CalendarPermissionEntity> findCalendarPermission(String ownerOriginkey, String userId) {
        return (root, query, builder) -> builder.and(builder.equal(root.get("ownerOriginKey"), ownerOriginkey),
                builder.equal(root.get("userId"), userId));
    }

    static Specification<CalendarPermissionEntity> findFollowingLists(String userId) {
        return ((root, query, builder) -> builder.and(
                builder.and(
                        builder.equal(root.get("userId"), userId),
                        builder.equal(root.get("ownerType"), OWNERTYPE.USER.name())
                ),
                builder.equal(root.get("permissionLevel"), PERMISSIONLEVEL.MEMBER.name())
                ));
    }

    //JPQL(JPA Query Language) 사용
    @Query("SELECT c.ownerOriginKey FROM CalendarPermissionEntity c WHERE c.ownerType = :ownerType AND c.userId = :userId")
    List<String> findTeamOriginKeysByOwnerTypeAndUserId(@Param("ownerType") String ownerType
            , @Param("userId") String userId);

    @Query("SELECT c.userId FROM CalendarPermissionEntity c WHERE c.ownerOriginKey = :key")
    List<String> findUserIdsByTeamOriginKey(@Param("teamOriginKey") String key);
}
