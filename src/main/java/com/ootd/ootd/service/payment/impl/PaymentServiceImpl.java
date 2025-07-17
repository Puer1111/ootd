package com.ootd.ootd.service.payment.impl;

import com.ootd.ootd.model.dto.payment.PaymentDTO;
import com.ootd.ootd.model.entity.order.UserOrder;
import com.ootd.ootd.repository.order.OrderRepository;
import com.ootd.ootd.repository.order.UserOrderRepository;
import com.ootd.ootd.repository.payment.PaymentRepository;
import com.ootd.ootd.service.payment.PaymentService;
import com.ootd.ootd.service.reward.RewardService;
import com.siot.IamportRestClient.IamportClient;
import com.siot.IamportRestClient.request.CancelData;
import com.siot.IamportRestClient.response.IamportResponse;
import com.siot.IamportRestClient.response.Payment;
import jakarta.annotation.PostConstruct;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class PaymentServiceImpl implements PaymentService {

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private UserOrderRepository userOrderRepository;

    @Autowired
    private RewardService rewardService;

    private IamportClient iamportClient;

    @Value("${IMP_API_KEY}")
    private String apiKey;

    @Value("${IMP_API_SECRET_KEY}")
    private String secretKey;

    @PostConstruct
    public void init() {
        this.iamportClient = new IamportClient(apiKey, secretKey);
    }

    @Transactional
    @Override
    public IamportResponse<Payment> validatePayment(String imp_uid) {
        try {
            IamportResponse<Payment> payment = iamportClient.paymentByImpUid(imp_uid);
            System.out.println("✅ 결제 검증 완료: " + payment);
            return payment;
        } catch (Exception e) {
            System.err.println("❌ 결제 검증 실패: " + e.getMessage());
            return null;
        }
    }

    @Transactional
    @Override
    public void savePayment(PaymentDTO dto) {
        try {
            System.out.println("🔍 결제 처리 시작 - OrderID: " + dto.getOrderId() + ", 금액: " + dto.getTotalPrice() + "원");

            // 1. 결제 정보 저장
            com.ootd.ootd.model.entity.payment.Payment savedPayment = PaymentDTO.convertToEntity(dto);
            paymentRepository.save(savedPayment);

            // 2. 주문 상태를 '완료'로 변경 (기존 Order 테이블)
            orderRepository.changeOrderStatus(dto.getOrderId());

            // 🆕 3. 가장 최근 UserOrder에서 사용자 정보 찾기 (간단한 방법)
            try {
                Long paymentAmount = dto.getTotalPrice().longValue();

                // 🔥 방법 1: 최근 5개 UserOrder 중에서 같은 금액 찾기
                List<UserOrder> recentUserOrders = userOrderRepository.findTop5ByOrderByCreatedAtDesc();

                System.out.println("🔍 최근 UserOrder 목록 (" + recentUserOrders.size() + "개):");
                for (UserOrder uo : recentUserOrders) {
                    System.out.println("   UserOrder - ID: " + uo.getId() +
                            ", UserID: " + uo.getUserId() +
                            ", TotalPrice: " + uo.getTotalPrice() + "원" +
                            ", Status: " + uo.getStatus());
                }

                // 같은 금액의 UserOrder 찾기
                Optional<UserOrder> matchingUserOrder = recentUserOrders.stream()
                        .filter(uo -> uo.getTotalPrice() != null && uo.getTotalPrice().equals(paymentAmount))
                        .filter(uo -> uo.getStatus() == UserOrder.OrderStatus.ORDERED)
                        .findFirst();

                if (matchingUserOrder.isPresent()) {
                    UserOrder userOrder = matchingUserOrder.get();
                    Long userId = userOrder.getUserId();

                    System.out.println("🔍 매칭된 UserOrder 찾음:");
                    System.out.println("   - UserOrder ID: " + userOrder.getId());
                    System.out.println("   - User ID: " + userId);
                    System.out.println("   - Product No: " + userOrder.getProductNo());
                    System.out.println("   - Total Price: " + userOrder.getTotalPrice());

                    if (userId != null) {
                        // 🔥 적립금 지급 전 현재 적립금 확인
                        Long currentPoints = rewardService.getAvailablePoints(userId);
                        System.out.println("🔍 적립금 지급 전 현재 적립금: " + currentPoints + "원");

                        // 적립금 지급
                        rewardService.givePurchaseReward(userId, paymentAmount, dto.getOrderId());

                        // 적립금 지급 후 확인
                        Long afterPoints = rewardService.getAvailablePoints(userId);
                        Long earnedPoints = rewardService.calculateRewardPoints(paymentAmount);

                        System.out.println("🔍 적립금 지급 후 현재 적립금: " + afterPoints + "원");
                        System.out.println("💰 적립금 지급 완료 - 사용자ID: " + userId +
                                ", 구매금액: " + paymentAmount + "원" +
                                ", 지급된 적립금: " + earnedPoints + "원" +
                                ", 증가분: " + (afterPoints - currentPoints) + "원");
                    } else {
                        System.out.println("⚠️ UserOrder에 사용자 ID가 없습니다.");
                    }
                } else {
                    System.out.println("⚠️ 결제 금액 " + paymentAmount + "원과 일치하는 UserOrder를 찾을 수 없습니다.");

                    // 🔥 방법 2: 금액이 안 맞으면 가장 최근 UserOrder 사용 (임시)
                    if (!recentUserOrders.isEmpty()) {
                        UserOrder latestUserOrder = recentUserOrders.get(0);
                        Long userId = latestUserOrder.getUserId();

                        System.out.println("🔄 최근 UserOrder로 대체:");
                        System.out.println("   - UserOrder ID: " + latestUserOrder.getId());
                        System.out.println("   - User ID: " + userId);

                        if (userId != null) {
                            // 적립금 지급 (실제 결제 금액 기준)
                            Long currentPoints = rewardService.getAvailablePoints(userId);
                            System.out.println("🔍 적립금 지급 전 현재 적립금: " + currentPoints + "원");

                            rewardService.givePurchaseReward(userId, paymentAmount, dto.getOrderId());

                            Long afterPoints = rewardService.getAvailablePoints(userId);
                            Long earnedPoints = rewardService.calculateRewardPoints(paymentAmount);

                            System.out.println("🔍 적립금 지급 후 현재 적립금: " + afterPoints + "원");
                            System.out.println("💰 적립금 지급 완료 (대체) - 사용자ID: " + userId +
                                    ", 구매금액: " + paymentAmount + "원" +
                                    ", 지급된 적립금: " + earnedPoints + "원" +
                                    ", 증가분: " + (afterPoints - currentPoints) + "원");
                        }
                    }
                }

            } catch (Exception e) {
                System.err.println("❌ 적립금 지급 실패: " + e.getMessage());
                e.printStackTrace();
                // 적립금 지급 실패해도 결제 저장은 성공으로 처리
            }

            System.out.println("✅ 결제 완료 - 주문ID: " + dto.getOrderId() + ", 금액: " + dto.getTotalPrice() + "원");

        } catch (Exception e) {
            System.err.println("❌ 결제 처리 실패: " + e.getMessage());
            throw new RuntimeException("결제 처리에 실패했습니다: " + e.getMessage());
        }
    }

    @Override
    public String getImpUid(Long orderId) {
        return paymentRepository.findByOrderId(orderId);
    }

    @Transactional
    @Override
    public IamportResponse<Payment> cancelPayment(String imp_uid) {
        try {
            // 결제 취소 로직 (기존과 동일)
            com.ootd.ootd.model.entity.payment.Payment payment = paymentRepository.findByImpUid(imp_uid);

            if (payment != null) {
                Long paymentAmount = payment.getTotalPrice().longValue();

                // 최근 UserOrder에서 사용자 찾기
                List<UserOrder> recentUserOrders = userOrderRepository.findTop5ByOrderByCreatedAtDesc();
                if (!recentUserOrders.isEmpty()) {
                    UserOrder latestUserOrder = recentUserOrders.get(0);
                    Long userId = latestUserOrder.getUserId();

                    if (userId != null) {
                        Long earnedPoints = rewardService.calculateRewardPoints(paymentAmount);
                        rewardService.refundUsedReward(userId, earnedPoints, payment.getOrderId());
                        System.out.println("💸 적립금 환원 완료 - 사용자ID: " + userId + ", 환원금액: " + earnedPoints + "원");
                    }
                }
            }

            CancelData cancelData = new CancelData(imp_uid, true);
            IamportResponse<Payment> response = iamportClient.cancelPaymentByImpUid(cancelData);

            if (response != null && response.getResponse() != null) {
                int result = paymentRepository.changeStatus(imp_uid);
                orderRepository.cancelOrderStatus(imp_uid);
                System.out.println("🎉 결제 취소 완료 - imp_uid: " + imp_uid);
            }

            return response;
        } catch(Exception e) {
            System.err.println("❌ 결제 취소 실패: " + e.getMessage());
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public boolean checkUserPay(Long userId) {
        return paymentRepository.findbyUserId(userId);
    }
}