package com.ootd.ootd.service.point;

import com.ootd.ootd.model.dto.point.UserPointDTO;
import com.ootd.ootd.model.entity.point.PointHistory;
import com.ootd.ootd.model.entity.point.UserPoint;
import com.ootd.ootd.repository.point.PointHistoryRepository;
import com.ootd.ootd.repository.point.UserPointRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * 적립금 관리 서비스
 * 경로: src/main/java/com/ootd/ootd/service/point/PointService.java
 */
@Service
@Transactional
public class PointService_backup {

    @Autowired
    private UserPointRepository userPointRepository;

    @Autowired
    private PointHistoryRepository pointHistoryRepository;

    // 기본 적립률 (1%)
    private static final Double DEFAULT_EARN_RATE = 0.01;

    // ==================== 적립금 조회 ====================

    /**
     * 사용자 적립금 정보 조회
     */
    public UserPointDTO getUserPointsDTO(Long userId) {
        UserPoint userPoint = getUserPoint(userId);
        return UserPointDTO.convertToDTO(userPoint);
    }

    /**
     * 사용자 적립금 엔티티 조회 (없으면 생성)
     */
    public UserPoint getUserPoint(Long userId) {
        Optional<UserPoint> optionalUserPoint = userPointRepository.findByUserId(userId);

        if (optionalUserPoint.isPresent()) {
            return optionalUserPoint.get();
        } else {
            // 사용자 적립금 정보가 없으면 새로 생성
            UserPoint newUserPoint = new UserPoint(userId);
            return userPointRepository.save(newUserPoint);
        }
    }

    /**
     * 적립금 내역 페이지 조회
     */
    public Page<PointHistory> getPointHistoryPage(Long userId, Pageable pageable) {
        return pointHistoryRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable);
    }

    /**
     * 적립금 내역 전체 조회
     */
    public List<PointHistory> getAllPointHistory(Long userId) {
        return pointHistoryRepository.findByUserIdOrderByCreatedAtDesc(userId);
    }

    // ==================== 적립금 적립 ====================

    /**
     * 구매 적립금 지급
     */
    public void earnPointsFromPurchase(Long userId, Long purchaseAmount, Long orderId) {
        Long earnPoints = calculateEarnPoints(purchaseAmount);

        if (earnPoints > 0) {
            earnPoints(userId, earnPoints, PointHistory.PointType.EARN_PURCHASE,
                    "구매 적립 (" + purchaseAmount.toString() + "원 구매)", orderId);
        }
    }

    /**
     * 적립금 지급 (일반)
     */
    public void earnPoints(Long userId, Long points, PointHistory.PointType pointType, String description) {
        earnPoints(userId, points, pointType, description, null);
    }

    /**
     * 적립금 지급 (주문 ID 포함)
     */
    public void earnPoints(Long userId, Long points, PointHistory.PointType pointType,
                           String description, Long orderId) {
        // UserPoint 업데이트
        UserPoint userPoint = getUserPoint(userId);
        userPoint.addPoints(points);
        userPointRepository.save(userPoint);

        // 적립금 내역 저장
        PointHistory history = new PointHistory(userId, points, pointType, description, orderId);
        pointHistoryRepository.save(history);

        System.out.println("✅ 적립금 지급 완료 - 사용자ID: " + userId + ", 적립금: " + points + "원");
    }

    // ==================== 적립금 사용 ====================

    /**
     * 적립금 사용 (기본)
     */
    public boolean usePoints(Long userId, Long points, String description) {
        return usePoints(userId, points, description, null);
    }

    /**
     * 적립금 사용 (주문 ID 포함)
     */
    public boolean usePoints(Long userId, Long points, String description, Long orderId) {
        UserPoint userPoint = getUserPoint(userId);

        // 사용 가능한 적립금 확인
        if (userPoint.getAvailablePoints() < points) {
            System.out.println("❌ 적립금 부족 - 보유: " + userPoint.getAvailablePoints() + ", 사용요청: " + points);
            return false;
        }

        // 적립금 사용 처리
        if (userPoint.usePoints(points)) {
            userPointRepository.save(userPoint);

            // 적립금 내역 저장 (음수로 저장)
            PointHistory history = new PointHistory(userId, -points, PointHistory.PointType.USE_PURCHASE,
                    description, orderId);
            pointHistoryRepository.save(history);

            System.out.println("✅ 적립금 사용 완료 - 사용자ID: " + userId + ", 사용금액: " + points + "원");
            return true;
        }

        return false;
    }

    /**
     * 적립금 사용 가능 여부 확인
     */
    public boolean canUsePoints(Long userId, Long points) {
        UserPoint userPoint = getUserPoint(userId);
        return userPoint.getAvailablePoints() >= points;
    }

    // ==================== 적립금 환원 ====================

    /**
     * 사용한 적립금 환원 (결제 실패, 주문 취소 시)
     */
    public void refundUsedPoints(Long userId, Long points, Long orderId) {
        earnPoints(userId, points, PointHistory.PointType.REFUND,
                "적립금 환원 (주문취소/결제실패)", orderId);
    }

    // ==================== 적립금 계산 ====================

    /**
     * 적립금 계산
     */
    public Long calculateEarnPoints(Long purchaseAmount) {
        if (purchaseAmount == null || purchaseAmount <= 0) {
            return 0L;
        }
        return Math.round(purchaseAmount * DEFAULT_EARN_RATE);
    }

    /**
     * 적립률 조회
     */
    public Double getEarnRate() {
        return DEFAULT_EARN_RATE;
    }

    // ==================== 적립금 통계 ====================

    /**
     * 사용자 적립금 통계 조회
     */
    public Map<String, Object> getUserPointsStatistics(Long userId) {
        Map<String, Object> statistics = new HashMap<>();

        // 총 적립 금액
        Long totalEarned = pointHistoryRepository.calculateTotalEarnedPoints(userId);
        if (totalEarned == null) totalEarned = 0L;

        // 총 사용 금액
        Long totalUsed = pointHistoryRepository.calculateTotalUsedPoints(userId);
        if (totalUsed == null) totalUsed = 0L;

        // 구매 적립 횟수
        Long purchaseEarnCount = pointHistoryRepository.countByUserIdAndPointType(
                userId, PointHistory.PointType.EARN_PURCHASE);

        // 구매 사용 횟수
        Long purchaseUseCount = pointHistoryRepository.countByUserIdAndPointType(
                userId, PointHistory.PointType.USE_PURCHASE);

        statistics.put("totalEarned", totalEarned);
        statistics.put("totalUsedHistory", totalUsed);
        statistics.put("purchaseEarnCount", purchaseEarnCount);
        statistics.put("purchaseUseCount", purchaseUseCount);

        return statistics;
    }

    /**
     * 최근 적립금 활동 조회
     */
    public List<PointHistory> getRecentPointActivity(Long userId, int days) {
        LocalDateTime startDate = LocalDateTime.now().minusDays(days);
        return pointHistoryRepository.findRecentPointHistory(userId, startDate);
    }

    // ==================== 관리자 기능 ====================

    /**
     * 관리자 적립금 지급
     */
    public void adminEarnPoints(Long userId, Long points, String description) {
        earnPoints(userId, points, PointHistory.PointType.EARN_ADMIN, description);
    }

    /**
     * 회원 가입 적립금 지급
     */
    public void giveSignupPoints(Long userId) {
        Long signupPoints = 1000L; // 회원가입 시 1000원 적립
        earnPoints(userId, signupPoints, PointHistory.PointType.EARN_SIGNUP, "회원가입 축하 적립금");
    }

    // ==================== 유틸리티 ====================

    /**
     * 특정 주문의 적립금 내역 조회
     */
    public List<PointHistory> getPointHistoryByOrderId(Long orderId) {
        return pointHistoryRepository.findByOrderId(orderId);
    }

    /**
     * 적립금 만료 처리 (배치 작업용)
     */
    public void expirePoints(Long userId, Long points, String reason) {
        UserPoint userPoint = getUserPoint(userId);

        // 사용 가능한 적립금에서 차감
        Long expireAmount = Math.min(points, userPoint.getAvailablePoints());

        if (expireAmount > 0) {
            userPoint.addPoints(-expireAmount); // 음수로 차감
            userPointRepository.save(userPoint);

            // 만료 내역 저장
            PointHistory history = new PointHistory(userId, -expireAmount,
                    PointHistory.PointType.EXPIRE, reason);
            pointHistoryRepository.save(history);
        }
    }
}