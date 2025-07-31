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

@Service
@Transactional
public class PointService_backup {

    @Autowired
    private UserPointRepository userPointRepository;

    @Autowired
    private PointHistoryRepository pointHistoryRepository;

    private static final Double DEFAULT_EARN_RATE = 0.01;


    public UserPointDTO getUserPointsDTO(Long userId) {
        UserPoint userPoint = getUserPoint(userId);
        return UserPointDTO.convertToDTO(userPoint);
    }

    public UserPoint getUserPoint(Long userId) {
        Optional<UserPoint> optionalUserPoint = userPointRepository.findByUserId(userId);

        if (optionalUserPoint.isPresent()) {
            return optionalUserPoint.get();
        } else {
            UserPoint newUserPoint = new UserPoint(userId);
            return userPointRepository.save(newUserPoint);
        }
    }

    public Page<PointHistory> getPointHistoryPage(Long userId, Pageable pageable) {
        return pointHistoryRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable);
    }

    public List<PointHistory> getAllPointHistory(Long userId) {
        return pointHistoryRepository.findByUserIdOrderByCreatedAtDesc(userId);
    }


    public void earnPointsFromPurchase(Long userId, Long purchaseAmount, Long orderId) {
        Long earnPoints = calculateEarnPoints(purchaseAmount);

        if (earnPoints > 0) {
            earnPoints(userId, earnPoints, PointHistory.PointType.EARN_PURCHASE,
                    "구매 적립 (" + purchaseAmount.toString() + "원 구매)", orderId);
        }
    }

    public void earnPoints(Long userId, Long points, PointHistory.PointType pointType, String description) {
        earnPoints(userId, points, pointType, description, null);
    }

    public void earnPoints(Long userId, Long points, PointHistory.PointType pointType,
                           String description, Long orderId) {
        UserPoint userPoint = getUserPoint(userId);
        userPoint.addPoints(points);
        userPointRepository.save(userPoint);

        PointHistory history = new PointHistory(userId, points, pointType, description, orderId);
        pointHistoryRepository.save(history);

        System.out.println("✅ 적립금 지급 완료 - 사용자ID: " + userId + ", 적립금: " + points + "원");
    }

    public boolean usePoints(Long userId, Long points, String description) {
        return usePoints(userId, points, description, null);
    }

    public boolean usePoints(Long userId, Long points, String description, Long orderId) {
        UserPoint userPoint = getUserPoint(userId);

        if (userPoint.getAvailablePoints() < points) {
            System.out.println("❌ 적립금 부족 - 보유: " + userPoint.getAvailablePoints() + ", 사용요청: " + points);
            return false;
        }

        if (userPoint.usePoints(points)) {
            userPointRepository.save(userPoint);

            PointHistory history = new PointHistory(userId, -points, PointHistory.PointType.USE_PURCHASE,
                    description, orderId);
            pointHistoryRepository.save(history);

            System.out.println("✅ 적립금 사용 완료 - 사용자ID: " + userId + ", 사용금액: " + points + "원");
            return true;
        }

        return false;
    }

    public boolean canUsePoints(Long userId, Long points) {
        UserPoint userPoint = getUserPoint(userId);
        return userPoint.getAvailablePoints() >= points;
    }


    public void refundUsedPoints(Long userId, Long points, Long orderId) {
        earnPoints(userId, points, PointHistory.PointType.REFUND,
                "적립금 환원 (주문취소/결제실패)", orderId);
    }

    public Long calculateEarnPoints(Long purchaseAmount) {
        if (purchaseAmount == null || purchaseAmount <= 0) {
            return 0L;
        }
        return Math.round(purchaseAmount * DEFAULT_EARN_RATE);
    }

    public Double getEarnRate() {
        return DEFAULT_EARN_RATE;
    }


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

    public List<PointHistory> getRecentPointActivity(Long userId, int days) {
        LocalDateTime startDate = LocalDateTime.now().minusDays(days);
        return pointHistoryRepository.findRecentPointHistory(userId, startDate);
    }


    public void adminEarnPoints(Long userId, Long points, String description) {
        earnPoints(userId, points, PointHistory.PointType.EARN_ADMIN, description);
    }

    public void giveSignupPoints(Long userId) {
        Long signupPoints = 1000L; // 회원가입 시 1000원 적립
        earnPoints(userId, signupPoints, PointHistory.PointType.EARN_SIGNUP, "회원가입 축하 적립금");
    }


    public List<PointHistory> getPointHistoryByOrderId(Long orderId) {
        return pointHistoryRepository.findByOrderId(orderId);
    }

    public void expirePoints(Long userId, Long points, String reason) {
        UserPoint userPoint = getUserPoint(userId);

        Long expireAmount = Math.min(points, userPoint.getAvailablePoints());

        if (expireAmount > 0) {
            userPoint.addPoints(-expireAmount);
            userPointRepository.save(userPoint);

            PointHistory history = new PointHistory(userId, -expireAmount,
                    PointHistory.PointType.EXPIRE, reason);
            pointHistoryRepository.save(history);
        }
    }
}