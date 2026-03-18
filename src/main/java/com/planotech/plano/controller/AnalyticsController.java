package com.planotech.plano.controller;

import com.planotech.plano.auth.UserPrincipal;
import com.planotech.plano.response.EventAnalyticsDTO;
import com.planotech.plano.response.UserAnalyticsDTO;
import com.planotech.plano.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
public class AnalyticsController {

    private final DashboardService dashboardService;

    @GetMapping("/event/{eventId}")
    public ResponseEntity<EventAnalyticsDTO> getEventDashboard(
            @PathVariable Long eventId
    ) {
        return ResponseEntity.ok(
                dashboardService.getEventDashboard(eventId)
        );
    }

    @GetMapping("/user")
    public ResponseEntity<UserAnalyticsDTO> getUserAnalytics(@AuthenticationPrincipal UserPrincipal userPrincipal) {
        return ResponseEntity.ok(
                dashboardService.getUserAnalytics(userPrincipal.getUser())
        );
    }

    @GetMapping
    public ResponseEntity<UserAnalyticsDTO> getDashboardAnalytics(@AuthenticationPrincipal UserPrincipal userPrincipal) {
        return ResponseEntity.ok(
                dashboardService.getDashboardAnalytics(userPrincipal.getUser())
        );
    }
}
