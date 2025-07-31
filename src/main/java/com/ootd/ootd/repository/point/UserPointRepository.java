package com.ootd.ootd.repository.point;

import com.ootd.ootd.model.entity.point.UserPoint;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserPointRepository extends JpaRepository<UserPoint, Long> {

    Optional<UserPoint> findByUserId(Long userId);

    boolean existsByUserId(Long userId);

    List<UserPoint> findByAvailablePointsGreaterThanEqual(Long minPoints);

    List<UserPoint> findByAvailablePointsGreaterThan(Long points);

    @Query("SELECT COALESCE(SUM(up.totalPoints), 0) FROM UserPoint up")
    Long calculateTotalPointsSum();

    @Query("SELECT COALESCE(SUM(up.availablePoints), 0) FROM UserPoint up")
    Long calculateAvailablePointsSum();

    @Query("SELECT COALESCE(SUM(up.usedPoints), 0) FROM UserPoint up")
    Long calculateUsedPointsSum();

    @Query("SELECT up FROM UserPoint up ORDER BY up.totalPoints DESC")
    List<UserPoint> findTopByTotalPointsOrderByTotalPointsDesc(@Param("limit") int limit);
}