package com.ootd.ootd.repository.point;

import com.ootd.ootd.model.entity.point.PointHistory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface PointHistoryRepository extends JpaRepository<PointHistory, Long> {

    List<PointHistory> findByUserIdOrderByCreatedAtDesc(Long userId);

    Page<PointHistory> findByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);

    List<PointHistory> findByUserIdAndPointTypeOrderByCreatedAtDesc(Long userId, PointHistory.PointType pointType);

    List<PointHistory> findByOrderId(Long orderId);

    List<PointHistory> findByUserIdAndCreatedAtBetweenOrderByCreatedAtDesc(
            Long userId, LocalDateTime startDate, LocalDateTime endDate);

    @Query("SELECT COALESCE(SUM(ph.points), 0) FROM PointHistory ph WHERE ph.userId = :userId AND ph.points > 0")
    Long calculateTotalEarnedPoints(@Param("userId") Long userId);

    @Query("SELECT COALESCE(SUM(ABS(ph.points)), 0) FROM PointHistory ph WHERE ph.userId = :userId AND ph.points < 0")
    Long calculateTotalUsedPoints(@Param("userId") Long userId);

    Long countByUserIdAndPointType(Long userId, PointHistory.PointType pointType);

    @Query("SELECT ph FROM PointHistory ph WHERE ph.userId = :userId AND ph.createdAt >= :startDate ORDER BY ph.createdAt DESC")
    List<PointHistory> findRecentPointHistory(@Param("userId") Long userId, @Param("startDate") LocalDateTime startDate);

    @Query("SELECT COALESCE(SUM(ph.points), 0) FROM PointHistory ph WHERE ph.createdAt BETWEEN :startDate AND :endDate")
    Long calculatePointsStatsBetween(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);
}