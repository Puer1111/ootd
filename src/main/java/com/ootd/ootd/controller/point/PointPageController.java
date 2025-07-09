package com.ootd.ootd.controller.point;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * 적립금 페이지 컨트롤러
 * 경로: src/main/java/com/ootd/ootd/controller/point/PointPageController.java
 * 페이지 경로: /points
 */
@Controller
public class PointPageController {

    /**
     * 적립금 메인 페이지
     * GET /points
     */
    @GetMapping("/points")
    public String pointsPage() {
        return "user/points";
    }

    /**
     * 적립금 내역 페이지
     * GET /points/history
     */
    @GetMapping("/points/history")
    public String pointsHistoryPage() {
        return "user/pointHistory";
    }

    /**
     * 적립금 통계 페이지
     * GET /points/statistics
     */
    @GetMapping("/points/statistics")
    public String pointsStatisticsPage() {
        return "user/pointStatistics";
    }
}