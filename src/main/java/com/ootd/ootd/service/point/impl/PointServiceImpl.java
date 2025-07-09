package com.ootd.ootd.service.point.impl;

import com.ootd.ootd.model.dto.point.UserPointDTO;
import com.ootd.ootd.model.entity.point.PointHistory;
import com.ootd.ootd.model.entity.point.UserPoint;
import com.ootd.ootd.repository.point.PointHistoryRepository;
import com.ootd.ootd.repository.point.UserPointRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 적립금 관리 서비스 (단순화된 버전)
 * 경로: src/main/java/com/ootd/ootd/service/point/impl/PointServiceImpl.java
 */
@Service("pointService")  // 빈 이름 명시적 지정
@Transactional
public class PointServiceImpl {  // extends 제거

    @Autowired
    private UserPointRepository userPointRepository;

    @Autowired
    private PointHistoryRepository pointHistoryRepository;

    // 설정 파일에서 적립률 가져오기 (기본값: 1%)
    @Value("${app.point.earn-rate:0.01}")
    private Double earnRate;

    // 회원가입 축하 적립금 (기본값: 1000원)
    @Value("${app.point.signup-bonus:1000}")
    private Long signupBonusPoints;

    // ==================== 적립금 잔액 관리 ====================

    @Transactional(readOnly = true)
    public UserPoint getUserPoints(Long userId) {
        return userPointRepository.findByUserId(userId)
                .orElseGet(() -> createUserPoint(userId));
    }

    @Transactional(readOnly = true)
    public UserPointDTO getUserPointsDTO(Long userId) {
        UserPoint userPoint = getUserPoints(userId);
        return UserPointDTO.convertToDTO(userPoint);
    }

    /**
     * 새로운 사용자 적립금 정보 생성
     */
    private UserPoint createUserPoint(Long userId) {
        UserPoint userPoint = new UserPoint(userId);
        return userPointRepository.save(userPoint);
    }

    // ==================== 적립금 적립 ====================

    public void earnPoints(Long userId, Long points, PointHistory.PointType pointType, String description) {
        earnPoints(userId, points, pointType, description, null);
    }

    public void earnPoints(Long userId, Long points, PointHistory.PointType pointType, String description, Long orderId) {
        try {
            if (userId == null || points == null || points == 0) {
                throw new IllegalArgumentException("유효하지 않은 적립 요청입니다.");
            }

            UserPoint userPoint = getUserPoints(userId);
            userPoint.addPoints(points);
            userPointRepository.save(userPoint);

            PointHistory history = new PointHistory(userId, points, pointType, description, orderId);
            pointHistoryRepository.save(history);

            if (points > 0) {
                System.out.println("✅ 적립금 적립 완료 - 사용자: " + userId + ", 적립: " + points + "원, 타입: " + pointType);
            } else {
                System.out.println("✅ 적립금 차감 완료 - 사용자: " + userId + ", 차감: " + Math.abs(points) + "원, 타입: " + pointType);
            }

        } catch (Exception e) {
            System.err.println("❌ 적립금 처리 실패: " + e.getMessage());
            throw new RuntimeException("적립금 처리에 실패했습니다: " + e.getMessage());
        }
    }

    /**
     * 구매 적립금 지급
     */
    public void earnPointsFromPurchase(Long userId, Long purchaseAmount, Long orderId) {
        Long earnPoints = calculateEarnPoints(purchaseAmount);
        if (earnPoints > 0) {
            earnPoints(userId, earnPoints, PointHistory.PointType.EARN_PURCHASE,
                    "구매 적립 (" + purchaseAmount + "원 구매)", orderId);
        }
    }

    // ==================== 적립금 사용 ====================

    public boolean usePoints(Long userId, Long points, String description) {
        return usePoints(userId, points, description, null);
    }

    public boolean usePoints(Long userId, Long points, String description, Long orderId) {
        try {
            if (userId == null || points == null || points <= 0) {
                System.out.println("❌ 유효하지 않은 적립금 사용 요청");
                return false;
            }

            UserPoint userPoint = getUserPoints(userId);

            if (userPoint.getAvailablePoints() < points) {
                System.out.println("❌ 적립금 부족 - 사용자: " + userId +
                        ", 요청: " + points + ", 보유: " + userPoint.getAvailablePoints());
                return false;
            }

            boolean success = userPoint.usePoints(points);
            if (!success) {
                return false;
            }

            userPointRepository.save(userPoint);

            PointHistory history = new PointHistory(userId, -points,
                    PointHistory.PointType.USE_PURCHASE, description, orderId);
            pointHistoryRepository.save(history);

            System.out.println("✅ 적립금 사용 완료 - 사용자: " + userId + ", 사용: " + points + "원");
            return true;

        } catch (Exception e) {
            System.err.println("❌ 적립금 사용 실패: " + e.getMessage());
            return false;
        }
    }

    @Transactional(readOnly = true)
    public boolean canUsePoints(Long userId, Long points) {
        if (userId == null || points == null || points <= 0) {
            return false;
        }

        UserPoint userPoint = getUserPoints(userId);
        return userPoint.getAvailablePoints() >= points;
    }

    // ==================== 적립금 내역 관리 ====================

    @Transactional(readOnly = true)
    public List<PointHistory> getPointHistory(Long userId) {
        return pointHistoryRepository.findByUserIdOrderByCreatedAtDesc(userId);
    }

    @Transactional(readOnly = true)
    public Page<PointHistory> getPointHistoryPage(Long userId, Pageable pageable) {
        return pointHistoryRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable);
    }

    // ==================== 적립금 계산 및 정책 ====================

    public Long calculateEarnPoints(Long purchaseAmount) {
        if (purchaseAmount == null || purchaseAmount <= 0) {
            return 0L;
        }
        return Math.round(purchaseAmount * earnRate);
    }

    public Double getEarnRate() {
        return earnRate;
    }

    // ==================== 특별 적립금 지급 ====================

    public void giveSignupPoints(Long userId) {
        earnPoints(userId, signupBonusPoints, PointHistory.PointType.EARN_SIGNUP,
                "회원가입 축하 적립금");
        System.out.println("🎉 회원가입 축하 적립금 지급 - 사용자: " + userId + ", 금액: " + signupBonusPoints + "원");
    }

    // ==================== 적립금 환원 및 취소 ====================

    public void refundUsedPoints(Long userId, Long points, Long orderId) {
        earnPoints(userId, points, PointHistory.PointType.REFUND,
                "결제 실패로 인한 적립금 환원", orderId);
        System.out.println("🔄 결제 실패 적립금 환원 - 사용자: " + userId + ", 금액: " + points + "원");
    }

    // ==================== 적립금 통계 ====================

    @Transactional(readOnly = true)
    public Map<String, Object> getUserPointsStatistics(Long userId) {
        Map<String, Object> statistics = new HashMap<>();

        UserPoint userPoint = getUserPoints(userId);

        statistics.put("totalPoints", userPoint.getTotalPoints());
        statistics.put("availablePoints", userPoint.getAvailablePoints());
        statistics.put("usedPoints", userPoint.getUsedPoints());

        Long totalEarned = pointHistoryRepository.calculateTotalEarnedPoints(userId);
        Long totalUsed = pointHistoryRepository.calculateTotalUsedPoints(userId);

        statistics.put("totalEarned", totalEarned != null ? totalEarned : 0L);
        statistics.put("totalUsedHistory", totalUsed != null ? totalUsed : 0L);

        statistics.put("purchaseEarnCount", pointHistoryRepository.countByUserIdAndPointType(userId, PointHistory.PointType.EARN_PURCHASE));
        statistics.put("purchaseUseCount", pointHistoryRepository.countByUserIdAndPointType(userId, PointHistory.PointType.USE_PURCHASE));

        return statistics;
    }

    @Transactional(readOnly = true)
    public List<PointHistory> getRecentPointActivity(Long userId, int days) {
        LocalDateTime startDate = LocalDateTime.now().minusDays(days);
        return pointHistoryRepository.findRecentPointHistory(userId, startDate);
    }
}