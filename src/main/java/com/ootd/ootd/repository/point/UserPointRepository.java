package com.ootd.ootd.repository.point;

import com.ootd.ootd.model.entity.point.UserPoint;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 사용자 적립금 데이터 접근 레포지토리
 * 경로: src/main/java/com/ootd/ootd/repository/point/UserPointRepository.java
 */
@Repository
public interface UserPointRepository extends JpaRepository<UserPoint, Long> {

    /**
     * 사용자 ID로 적립금 정보 조회
     */
    Optional<UserPoint> findByUserId(Long userId);

    /**
     * 사용자 ID로 적립금 정보 존재 여부 확인
     */
    boolean existsByUserId(Long userId);

    /**
     * 사용 가능한 적립금이 특정 금액 이상인 사용자들 조회
     */
    List<UserPoint> findByAvailablePointsGreaterThanEqual(Long minPoints);

    /**
     * 사용 가능한 적립금이 0보다 큰 사용자들 조회
     */
    List<UserPoint> findByAvailablePointsGreaterThan(Long points);

    /**
     * 전체 적립금 통계 조회
     */
    @Query("SELECT COALESCE(SUM(up.totalPoints), 0) FROM UserPoint up")
    Long calculateTotalPointsSum();

    /**
     * 전체 사용 가능한 적립금 통계 조회
     */
    @Query("SELECT COALESCE(SUM(up.availablePoints), 0) FROM UserPoint up")
    Long calculateAvailablePointsSum();

    /**
     * 전체 사용된 적립금 통계 조회
     */
    @Query("SELECT COALESCE(SUM(up.usedPoints), 0) FROM UserPoint up")
    Long calculateUsedPointsSum();

    /**
     * 적립금 순위 조회 (상위 N명)
     */
    @Query("SELECT up FROM UserPoint up ORDER BY up.totalPoints DESC")
    List<UserPoint> findTopByTotalPointsOrderByTotalPointsDesc(@Param("limit") int limit);
}