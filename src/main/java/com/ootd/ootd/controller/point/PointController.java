package com.ootd.ootd.controller.point;

import com.ootd.ootd.model.dto.point.PointRequestDTO;
import com.ootd.ootd.model.dto.point.UserPointDTO;
import com.ootd.ootd.model.entity.point.PointHistory;
import com.ootd.ootd.model.entity.user.User;
import com.ootd.ootd.repository.user.UserRepository;
import com.ootd.ootd.service.point.PointService_backup;  // 기존 PointService 사용
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 적립금 관리 API 컨트롤러
 */
@RestController
@RequestMapping("/api/points")
public class PointController {

    @Autowired
    private PointService_backup pointService;  // 기존 PointService 사용

    @Autowired
    private UserRepository userRepository;

    /**
     * 내 적립금 정보 조회
     */
    @GetMapping("/my-points")
    public ResponseEntity<?> getMyPoints(@AuthenticationPrincipal UserDetails userDetails) {
        Map<String, Object> response = new HashMap<>();

        try {
            if (userDetails == null) {
                response.put("success", false);
                response.put("message", "로그인이 필요합니다");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
            }

            User user = userRepository.findByEmail(userDetails.getUsername())
                    .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다"));

            UserPointDTO userPointDTO = pointService.getUserPointsDTO(user.getId());

            response.put("success", true);
            response.put("data", userPointDTO);
            response.put("totalPoints", userPointDTO.getTotalPoints());
            response.put("availablePoints", userPointDTO.getAvailablePoints());
            response.put("usedPoints", userPointDTO.getUsedPoints());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "적립금 조회 중 오류가 발생했습니다: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * 적립금 내역 조회
     */
    @GetMapping("/history")
    public ResponseEntity<?> getPointHistory(@AuthenticationPrincipal UserDetails userDetails,
                                             @RequestParam(defaultValue = "0") int page,
                                             @RequestParam(defaultValue = "20") int size) {
        Map<String, Object> response = new HashMap<>();

        try {
            if (userDetails == null) {
                response.put("success", false);
                response.put("message", "로그인이 필요합니다");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
            }

            User user = userRepository.findByEmail(userDetails.getUsername())
                    .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다"));

            Pageable pageable = PageRequest.of(page, size);
            Page<PointHistory> historyPage = pointService.getPointHistoryPage(user.getId(), pageable);

            response.put("success", true);
            response.put("history", historyPage.getContent());
            response.put("totalElements", historyPage.getTotalElements());
            response.put("totalPages", historyPage.getTotalPages());
            response.put("currentPage", historyPage.getNumber());
            response.put("hasNext", historyPage.hasNext());
            response.put("hasPrevious", historyPage.hasPrevious());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "적립금 내역 조회 중 오류가 발생했습니다: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * 전체 적립금 내역 조회
     */
    @GetMapping("/history/all")
    public ResponseEntity<?> getAllPointHistory(@AuthenticationPrincipal UserDetails userDetails) {
        Map<String, Object> response = new HashMap<>();

        try {
            if (userDetails == null) {
                response.put("success", false);
                response.put("message", "로그인이 필요합니다");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
            }

            User user = userRepository.findByEmail(userDetails.getUsername())
                    .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다"));

            List<PointHistory> allHistory = pointService.getAllPointHistory(user.getId());

            response.put("success", true);
            response.put("history", allHistory);
            response.put("totalCount", allHistory.size());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "적립금 내역 조회 중 오류가 발생했습니다: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * 적립금 사용
     */
    @PostMapping("/use")
    public ResponseEntity<?> usePoints(@RequestBody PointRequestDTO request,
                                       @AuthenticationPrincipal UserDetails userDetails) {
        Map<String, Object> response = new HashMap<>();

        try {
            if (userDetails == null) {
                response.put("success", false);
                response.put("message", "로그인이 필요합니다");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
            }

            User user = userRepository.findByEmail(userDetails.getUsername())
                    .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다"));

            if (request.getPoints() == null || request.getPoints() <= 0) {
                response.put("success", false);
                response.put("message", "유효하지 않은 적립금 금액입니다");
                return ResponseEntity.badRequest().body(response);
            }

            String description = request.getDescription() != null ? request.getDescription() : "상품 구매";

            boolean success = pointService.usePoints(user.getId(), request.getPoints(), description, request.getOrderId());

            if (success) {
                UserPointDTO updatedPoints = pointService.getUserPointsDTO(user.getId());
                response.put("success", true);
                response.put("message", "적립금 사용이 완료되었습니다");
                response.put("usedPoints", request.getPoints());
                response.put("remainingPoints", updatedPoints.getAvailablePoints());
            } else {
                response.put("success", false);
                response.put("message", "적립금이 부족하거나 사용에 실패했습니다");
            }

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "적립금 사용 중 오류가 발생했습니다: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * 적립금 사용 가능 여부 확인
     */
    @GetMapping("/can-use")
    public ResponseEntity<?> canUsePoints(@RequestParam Long points,
                                          @AuthenticationPrincipal UserDetails userDetails) {
        Map<String, Object> response = new HashMap<>();

        try {
            if (userDetails == null) {
                response.put("success", false);
                response.put("message", "로그인이 필요합니다");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
            }

            User user = userRepository.findByEmail(userDetails.getUsername())
                    .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다"));

            boolean canUse = pointService.canUsePoints(user.getId(), points);
            UserPointDTO userPointDTO = pointService.getUserPointsDTO(user.getId());

            response.put("success", true);
            response.put("canUse", canUse);
            response.put("requestedPoints", points);
            response.put("availablePoints", userPointDTO.getAvailablePoints());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "적립금 확인 중 오류가 발생했습니다: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * 적립금 환원
     */
    @PostMapping("/refund")
    public ResponseEntity<?> refundPoints(@RequestBody PointRequestDTO request,
                                          @AuthenticationPrincipal UserDetails userDetails) {
        Map<String, Object> response = new HashMap<>();

        try {
            if (userDetails == null) {
                response.put("success", false);
                response.put("message", "로그인이 필요합니다");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
            }

            User user = userRepository.findByEmail(userDetails.getUsername())
                    .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다"));

            pointService.refundUsedPoints(user.getId(), request.getPoints(), request.getOrderId());

            UserPointDTO updatedPoints = pointService.getUserPointsDTO(user.getId());
            response.put("success", true);
            response.put("message", "적립금 환원이 완료되었습니다");
            response.put("refundedPoints", request.getPoints());
            response.put("currentPoints", updatedPoints.getAvailablePoints());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "적립금 환원 중 오류가 발생했습니다: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * 적립금 통계 조회
     */
    @GetMapping("/statistics")
    public ResponseEntity<?> getPointsStatistics(@AuthenticationPrincipal UserDetails userDetails) {
        Map<String, Object> response = new HashMap<>();

        try {
            if (userDetails == null) {
                response.put("success", false);
                response.put("message", "로그인이 필요합니다");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
            }

            User user = userRepository.findByEmail(userDetails.getUsername())
                    .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다"));

            Map<String, Object> statistics = pointService.getUserPointsStatistics(user.getId());

            response.put("success", true);
            response.put("statistics", statistics);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "적립금 통계 조회 중 오류가 발생했습니다: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * 최근 적립금 활동 조회
     */
    @GetMapping("/recent")
    public ResponseEntity<?> getRecentActivity(@RequestParam(defaultValue = "7") int days,
                                               @AuthenticationPrincipal UserDetails userDetails) {
        Map<String, Object> response = new HashMap<>();

        try {
            if (userDetails == null) {
                response.put("success", false);
                response.put("message", "로그인이 필요합니다");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
            }

            User user = userRepository.findByEmail(userDetails.getUsername())
                    .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다"));

            List<PointHistory> recentActivity = pointService.getRecentPointActivity(user.getId(), days);

            response.put("success", true);
            response.put("recentActivity", recentActivity);
            response.put("days", days);
            response.put("activityCount", recentActivity.size());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "최근 활동 조회 중 오류가 발생했습니다: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * 적립률 정보 조회
     */
    @GetMapping("/earn-rate")
    public ResponseEntity<?> getEarnRate() {
        Map<String, Object> response = new HashMap<>();

        try {
            Double earnRate = pointService.getEarnRate();
            Long sampleAmount = 10000L;
            Long sampleEarnPoints = pointService.calculateEarnPoints(sampleAmount);

            response.put("success", true);
            response.put("earnRate", earnRate);
            response.put("earnRatePercent", earnRate * 100);

            Map<String, Object> example = new HashMap<>();
            example.put("purchaseAmount", sampleAmount);
            example.put("earnPoints", sampleEarnPoints);
            example.put("description", sampleAmount + "원 구매 시 " + sampleEarnPoints + "원 적립");
            response.put("example", example);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "적립률 정보 조회 중 오류가 발생했습니다: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
}