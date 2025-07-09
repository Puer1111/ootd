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

/**
 * 적립금 내역 데이터 접근 레포지토리
 * 경로: src/main/java/com/ootd/ootd/repository/point/PointHistoryRepository.java
 */
@Repository
public interface PointHistoryRepository extends JpaRepository<PointHistory, Long> {

    /**
     * 사용자 ID로 적립금 내역 조회 (최신순)
     */
    List<PointHistory> findByUserIdOrderByCreatedAtDesc(Long userId);

    /**
     * 사용자 ID로 적립금 내역 페이지 조회 (최신순)
     */
    Page<PointHistory> findByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);

    /**
     * 사용자 ID와 적립금 타입으로 내역 조회
     */
    List<PointHistory> findByUserIdAndPointTypeOrderByCreatedAtDesc(Long userId, PointHistory.PointType pointType);

    /**
     * 특정 주문과 관련된 적립금 내역 조회
     */
    List<PointHistory> findByOrderId(Long orderId);

    /**
     * 특정 기간 동안의 적립금 내역 조회
     */
    List<PointHistory> findByUserIdAndCreatedAtBetweenOrderByCreatedAtDesc(
            Long userId, LocalDateTime startDate, LocalDateTime endDate);

    /**
     * 사용자의 총 적립 금액 계산 (양수 금액만)
     */
    @Query("SELECT COALESCE(SUM(ph.points), 0) FROM PointHistory ph WHERE ph.userId = :userId AND ph.points > 0")
    Long calculateTotalEarnedPoints(@Param("userId") Long userId);

    /**
     * 사용자의 총 사용 금액 계산 (음수 금액만)
     */
    @Query("SELECT COALESCE(SUM(ABS(ph.points)), 0) FROM PointHistory ph WHERE ph.userId = :userId AND ph.points < 0")
    Long calculateTotalUsedPoints(@Param("userId") Long userId);

    /**
     * 특정 타입의 적립금 내역 개수 조회
     */
    Long countByUserIdAndPointType(Long userId, PointHistory.PointType pointType);

    /**
     * 최근 N일간의 적립금 내역 조회
     */
    @Query("SELECT ph FROM PointHistory ph WHERE ph.userId = :userId AND ph.createdAt >= :startDate ORDER BY ph.createdAt DESC")
    List<PointHistory> findRecentPointHistory(@Param("userId") Long userId, @Param("startDate") LocalDateTime startDate);

    /**
     * 특정 기간 동안의 적립금 통계 조회
     */
    @Query("SELECT COALESCE(SUM(ph.points), 0) FROM PointHistory ph WHERE ph.createdAt BETWEEN :startDate AND :endDate")
    Long calculatePointsStatsBetween(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);
}