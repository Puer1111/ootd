package com.ootd.ootd.controller.point;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class PointPageController {

    @GetMapping("/points")
    public String pointsPage() {
        return "user/points";
    }

    @GetMapping("/points/history")
    public String pointsHistoryPage() {
        return "user/pointHistory";
    }

    @GetMapping("/points/statistics")
    public String pointsStatisticsPage() {
        return "user/pointStatistics";
    }
}