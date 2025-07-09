package com.ootd.ootd.service.reward;

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
 * 적립금(리워드) 관리 서비스
 * - 회원가입 축하 적립금 지급
 * - 구매 적립금 적립/사용
 * - 적립금 내역 조회
 */
@Service("rewardService")
@Transactional
public class RewardService {

    @Autowired
    private UserPointRepository userPointRepository;

    @Autowired
    private PointHistoryRepository pointHistoryRepository;

    // 적립률 (기본값: 1%)
    @Value("${app.point.earn-rate:0.01}")
    private Double earnRate;

    // 회원가입 축하 적립금 (기본값: 1000원)
    @Value("${app.point.signup-bonus:1000}")
    private Long signupBonusPoints;

    // ==================== 적립금 조회 ====================

    /**
     * 사용자 적립금 정보 조회 (DTO 형태)
     */
    @Transactional(readOnly = true)
    public UserPointDTO getUserPointsDTO(Long userId) {
        UserPoint userPoint = getUserPointEntity(userId);
        return UserPointDTO.convertToDTO(userPoint);
    }

    /**
     * 사용자 적립금 엔티티 조회 (없으면 생성)
     */
    @Transactional(readOnly = true)
    public UserPoint getUserPointEntity(Long userId) {
        return userPointRepository.findByUserId(userId)
                .orElseGet(() -> createNewUserPoint(userId));
    }

    /**
     * 새로운 사용자 적립금 정보 생성
     */
    private UserPoint createNewUserPoint(Long userId) {
        UserPoint userPoint = new UserPoint(userId);
        return userPointRepository.save(userPoint);
    }

    /**
     * 사용 가능한 적립금 조회
     */
    @Transactional(readOnly = true)
    public Long getAvailablePoints(Long userId) {
        UserPoint userPoint = getUserPointEntity(userId);
        return userPoint.getAvailablePoints();
    }

    // ==================== 적립금 지급 ====================

    /**
     * 회원가입 축하 적립금 지급
     */
    public void giveSignupReward(Long userId) {
        addPoints(userId, signupBonusPoints, PointHistory.PointType.EARN_SIGNUP,
                "🎉 회원가입 축하 적립금");
        System.out.println("🎉 회원가입 축하 적립금 지급 완료 - 사용자ID: " + userId + ", 금액: " + signupBonusPoints + "원");
    }

    /**
     * 구매 적립금 지급
     */
    public void givePurchaseReward(Long userId, Long purchaseAmount, Long orderId) {
        Long earnPoints = calculateRewardPoints(purchaseAmount);
        if (earnPoints > 0) {
            addPoints(userId, earnPoints, PointHistory.PointType.EARN_PURCHASE,
                    "💰 구매 적립 (" + purchaseAmount.toString() + "원 구매)", orderId);
        }
    }

    /**
     * 관리자 적립금 지급
     */
    public void giveAdminReward(Long userId, Long points, String description) {
        addPoints(userId, points, PointHistory.PointType.EARN_ADMIN, description);
    }

    /**
     * 적립금 지급 공통 메소드
     */
    public void addPoints(Long userId, Long points, PointHistory.PointType pointType, String description) {
        addPoints(userId, points, pointType, description, null);
    }

    /**
     * 적립금 지급 공통 메소드 (주문 ID 포함)
     */
    public void addPoints(Long userId, Long points, PointHistory.PointType pointType,
                          String description, Long orderId) {
        try {
            if (userId == null || points == null || points <= 0) {
                throw new IllegalArgumentException("유효하지 않은 적립 요청입니다.");
            }

            // 적립금 업데이트
            UserPoint userPoint = getUserPointEntity(userId);
            userPoint.addPoints(points);
            userPointRepository.save(userPoint);

            // 적립금 내역 저장
            PointHistory history = new PointHistory(userId, points, pointType, description, orderId);
            pointHistoryRepository.save(history);

            System.out.println("✅ 적립금 지급 완료 - 사용자ID: " + userId + ", 적립금: " + points + "원, 타입: " + pointType);

        } catch (Exception e) {
            System.err.println("❌ 적립금 지급 실패: " + e.getMessage());
            throw new RuntimeException("적립금 지급에 실패했습니다: " + e.getMessage());
        }
    }

    // ==================== 적립금 사용 ====================

    /**
     * 적립금 사용 (기본)
     */
    public boolean useReward(Long userId, Long points, String description) {
        return useReward(userId, points, description, null);
    }

    /**
     * 적립금 사용 (주문 ID 포함)
     */
    public boolean useReward(Long userId, Long points, String description, Long orderId) {
        try {
            if (userId == null || points == null || points <= 0) {
                System.out.println("❌ 유효하지 않은 적립금 사용 요청");
                return false;
            }

            UserPoint userPoint = getUserPointEntity(userId);

            // 사용 가능한 적립금 확인
            if (userPoint.getAvailablePoints() < points) {
                System.out.println("❌ 적립금 부족 - 사용자ID: " + userId +
                        ", 요청: " + points + "원, 보유: " + userPoint.getAvailablePoints() + "원");
                return false;
            }

            // 적립금 사용 처리
            boolean success = userPoint.usePoints(points);
            if (!success) {
                return false;
            }

            userPointRepository.save(userPoint);

            // 적립금 사용 내역 저장 (음수로 저장)
            PointHistory history = new PointHistory(userId, -points,
                    PointHistory.PointType.USE_PURCHASE, description, orderId);
            pointHistoryRepository.save(history);

            System.out.println("✅ 적립금 사용 완료 - 사용자ID: " + userId + ", 사용금액: " + points + "원");
            return true;

        } catch (Exception e) {
            System.err.println("❌ 적립금 사용 실패: " + e.getMessage());
            return false;
        }
    }

    /**
     * 적립금 사용 가능 여부 확인
     */
    @Transactional(readOnly = true)
    public boolean canUseReward(Long userId, Long points) {
        if (userId == null || points == null || points <= 0) {
            return false;
        }

        UserPoint userPoint = getUserPointEntity(userId);
        return userPoint.getAvailablePoints() >= points;
    }

    // ==================== 적립금 환원 ====================

    /**
     * 사용한 적립금 환원 (결제 실패, 주문 취소 시)
     */
    public void refundUsedReward(Long userId, Long points, Long orderId) {
        addPoints(userId, points, PointHistory.PointType.REFUND,
                "🔄 적립금 환원 (주문취소/결제실패)", orderId);
        System.out.println("🔄 적립금 환원 완료 - 사용자ID: " + userId + ", 환원금액: " + points + "원");
    }

    // ==================== 적립금 내역 조회 ====================

    /**
     * 적립금 내역 전체 조회
     */
    @Transactional(readOnly = true)
    public List<PointHistory> getRewardHistory(Long userId) {
        return pointHistoryRepository.findByUserIdOrderByCreatedAtDesc(userId);
    }

    /**
     * 적립금 내역 페이지 조회
     */
    @Transactional(readOnly = true)
    public Page<PointHistory> getRewardHistoryPage(Long userId, Pageable pageable) {
        return pointHistoryRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable);
    }

    /**
     * 최근 적립금 활동 조회
     */
    @Transactional(readOnly = true)
    public List<PointHistory> getRecentRewardActivity(Long userId, int days) {
        LocalDateTime startDate = LocalDateTime.now().minusDays(days);
        return pointHistoryRepository.findRecentPointHistory(userId, startDate);
    }

    // ==================== 적립금 계산 ====================

    /**
     * 구매 금액에 따른 적립금 계산
     */
    public Long calculateRewardPoints(Long purchaseAmount) {
        if (purchaseAmount == null || purchaseAmount <= 0) {
            return 0L;
        }
        return Math.round(purchaseAmount * earnRate);
    }

    /**
     * 적립률 조회
     */
    public Double getEarnRate() {
        return earnRate;
    }

    /**
     * 회원가입 보너스 적립금 조회
     */
    public Long getSignupBonusPoints() {
        return signupBonusPoints;
    }

    // ==================== 적립금 통계 ====================

    /**
     * 사용자 적립금 통계 조회
     */
    @Transactional(readOnly = true)
    public Map<String, Object> getRewardStatistics(Long userId) {
        Map<String, Object> statistics = new HashMap<>();

        UserPoint userPoint = getUserPointEntity(userId);

        // 기본 통계
        statistics.put("totalPoints", userPoint.getTotalPoints());
        statistics.put("availablePoints", userPoint.getAvailablePoints());
        statistics.put("usedPoints", userPoint.getUsedPoints());

        // 내역 기반 통계
        Long totalEarned = pointHistoryRepository.calculateTotalEarnedPoints(userId);
        Long totalUsed = pointHistoryRepository.calculateTotalUsedPoints(userId);

        statistics.put("totalEarned", totalEarned != null ? totalEarned : 0L);
        statistics.put("totalUsedHistory", totalUsed != null ? totalUsed : 0L);

        // 활동 횟수
        statistics.put("purchaseEarnCount",
                pointHistoryRepository.countByUserIdAndPointType(userId, PointHistory.PointType.EARN_PURCHASE));
        statistics.put("purchaseUseCount",
                pointHistoryRepository.countByUserIdAndPointType(userId, PointHistory.PointType.USE_PURCHASE));

        return statistics;
    }

    // ==================== 기타 유틸리티 ====================

    /**
     * 특정 주문의 적립금 내역 조회
     */
    @Transactional(readOnly = true)
    public List<PointHistory> getRewardHistoryByOrderId(Long orderId) {
        return pointHistoryRepository.findByOrderId(orderId);
    }

    /**
     * 적립금 만료 처리 (배치 작업용)
     */
    public void expireReward(Long userId, Long points, String reason) {
        UserPoint userPoint = getUserPointEntity(userId);

        // 사용 가능한 적립금에서 차감
        Long expireAmount = Math.min(points, userPoint.getAvailablePoints());

        if (expireAmount > 0) {
            userPoint.addPoints(-expireAmount); // 음수로 차감
            userPointRepository.save(userPoint);

            // 만료 내역 저장
            PointHistory history = new PointHistory(userId, -expireAmount,
                    PointHistory.PointType.EXPIRE, reason);
            pointHistoryRepository.save(history);

            System.out.println("⏰ 적립금 만료 처리 완료 - 사용자ID: " + userId + ", 만료금액: " + expireAmount + "원");
        }
    }
}