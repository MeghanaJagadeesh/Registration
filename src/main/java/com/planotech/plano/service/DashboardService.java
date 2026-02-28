package com.planotech.plano.service;

import com.planotech.plano.model.Event;
import com.planotech.plano.model.User;
import com.planotech.plano.repository.CheckpointLogRepository;
import com.planotech.plano.repository.EventRepository;
import com.planotech.plano.repository.EventUserRepository;
import com.planotech.plano.repository.RegistrationEntryRepository;
import com.planotech.plano.response.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DashboardService {

    private final EventRepository eventRepository;
    private final RegistrationEntryRepository registrationRepository;
    private final CheckpointLogRepository checkpointLogRepository;
    private final EventUserRepository eventUserRepository;

    public EventAnalyticsDTO getEventDashboard(Long eventId) {

        LocalDate today = LocalDate.now();
        LocalDateTime startOfDay = today.atStartOfDay();
        LocalDateTime endOfDay = today.atTime(23, 59, 59);

        // --------------------------------
        // KPI
        // --------------------------------

        long totalRegistrations =
                registrationRepository.countByEvent_EventId(eventId);

        long todayRegistrations =
                registrationRepository.countTodayRegistrations(
                        eventId, startOfDay, endOfDay
                );

        long totalCheckIns =
                registrationRepository.countByEvent_EventIdAndCheckedInTrue(eventId);

        long todayCheckIns =
                registrationRepository.countTodayCheckIns(
                        eventId, startOfDay, endOfDay
                );

        long totalScans =
                checkpointLogRepository.countTotalScans(eventId);

        long todayScans =
                checkpointLogRepository.countTodayScans(
                        eventId, startOfDay, endOfDay
                );

        double checkInRate =
                totalRegistrations == 0 ? 0 :
                        (totalCheckIns * 100.0) / totalRegistrations;

        // --------------------------------
        // Registration Trend (Lifetime)
        // --------------------------------

        List<RegistrationTrendDTO> registrationTrend =
                registrationRepository.getRegistrationTrend(eventId)
                        .stream()
                        .map(r -> new RegistrationTrendDTO(
                                ((java.sql.Date) r[0]).toLocalDate(),
                                ((Number) r[1]).longValue()
                        ))
                        .toList();


        List<CheckpointDailyUsageDTO> checkpointDailyUsage =
                checkpointLogRepository.getCheckpointDailyUsageDetailed(eventId)
                        .stream()
                        .map(r -> new CheckpointDailyUsageDTO(
                                ((java.sql.Date) r[0]).toLocalDate(),
                                (String) r[1],
                                ((Number) r[2]).longValue()
                        ))
                        .toList();

        List<CheckpointUsageDTO> checkpointTotalUsage =
                checkpointLogRepository
                        .getCheckpointTotalUsage(eventId)
                        .stream()
                        .map(r -> new CheckpointUsageDTO(
                                (String) r[0],
                                ((Number) r[1]).longValue(),
                                ((Number) r[2]).longValue(),
                                ((Number) r[3]).longValue()
                        ))
                        .toList();

        String mostActiveCheckpoint =
                checkpointTotalUsage.isEmpty()
                        ? null
                        : checkpointTotalUsage.get(0).getCheckpointName();

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime lastWeek = now.minusDays(7);
        LocalDateTime previousWeek = now.minusDays(14);

        long currentWeek =
                registrationRepository.countBetween(eventId, lastWeek, now);

        long previousWeekCount =
                registrationRepository.countBetween(eventId, previousWeek, lastWeek);

        double growthPercentage;
        String direction;

        if (previousWeekCount == 0 && currentWeek == 0) {
            growthPercentage = 0;
            direction = "STABLE";
        } else if (previousWeekCount == 0) {
            growthPercentage = 100;
            direction = "UP";
        } else {
            growthPercentage =
                    ((currentWeek - previousWeekCount) * 100.0) / previousWeekCount;

            if (growthPercentage > 0) direction = "UP";
            else if (growthPercentage < 0) direction = "DOWN";
            else direction = "STABLE";
        }

        RegistrationGrowthDTO growth =
                RegistrationGrowthDTO.builder()
                        .percentage(growthPercentage)
                        .direction(direction)
                        .build();

        return EventAnalyticsDTO.builder()
                .totalRegistrations(totalRegistrations)
                .todayRegistrations(todayRegistrations)
                .totalCheckIns(totalCheckIns)
                .todayCheckIns(todayCheckIns)
                .checkInRate(checkInRate)
                .totalScans(totalScans)
                .todayScans(todayScans)
                .registrationGrowth(growth)
                .registrationTrend(registrationTrend)
                .checkpointDailyUsage(checkpointDailyUsage)
                .checkpointTotalUsage(checkpointTotalUsage)
                .mostActiveCheckpoint(mostActiveCheckpoint)
                .build();
    }

    public UserAnalyticsDTO getUserAnalytics(User user) {

        Long userId = user.getUserId();
        List<Long> eventIds =
                eventUserRepository.findAssignedEventIds(userId);

        if (eventIds.isEmpty()) {
            return UserAnalyticsDTO.builder()
                    .totalAssignedEvents(0)
                    .build();
        }

        long totalAssignedEvents = eventIds.size();

        long totalRegistrations =
                registrationRepository.countByEvent_EventIdIn(eventIds);

        long totalCheckIns =
                registrationRepository.countByEvent_EventIdInAndCheckedInTrue(eventIds);

        long totalScans =
                checkpointLogRepository.countTotalScansForEvents(eventIds);

        double overallCheckInRate =
                totalRegistrations == 0 ? 0 :
                        (totalCheckIns * 100.0) / totalRegistrations;

        LocalDate today = LocalDate.now();
        LocalDateTime start = today.atStartOfDay();
        LocalDateTime end = today.atTime(23, 59, 59);

        long todayScans =
                checkpointLogRepository.countTodayScansByUser(
                        userId,
                        start,
                        end
                );
        List<EventPerformanceDTO> eventPerformance = new ArrayList<>();

        String bestPerformingEvent = null;
        String worstPerformingEvent = null;
        String mostActiveEvent = null;

        double bestRate = -1;
        double worstRate = 101;
        long maxScans = -1;

        for (Long eventId : eventIds) {

            Event event = eventRepository.findById(eventId).orElse(null);
            if (event == null) continue;

            long registrations =
                    registrationRepository.countByEvent_EventId(eventId);

            long checkIns =
                    registrationRepository.countByEvent_EventIdAndCheckedInTrue(eventId);

            long scans =
                    checkpointLogRepository.countTotalScans(eventId);

            double rate = registrations == 0 ? 0 :
                    (checkIns * 100.0) / registrations;

            eventPerformance.add(
                    new EventPerformanceDTO(
                            eventId,
                            event.getName(),
                            registrations,
                            checkIns,
                            rate,
                            scans
                    )
            );

            if (rate > bestRate) {
                bestRate = rate;
                bestPerformingEvent = event.getName();
            }

            if (rate < worstRate) {
                worstRate = rate;
                worstPerformingEvent = event.getName();
            }

            if (scans > maxScans) {
                maxScans = scans;
                mostActiveEvent = event.getName();
            }
        }

        return UserAnalyticsDTO.builder()
                .totalAssignedEvents(totalAssignedEvents)
                .totalRegistrations(totalRegistrations)
                .totalCheckIns(totalCheckIns)
                .totalScans(totalScans)
                .todayScans(todayScans)
                .overallCheckInRate(overallCheckInRate)
                .eventPerformance(eventPerformance)
                .bestPerformingEvent(bestPerformingEvent)
                .worstPerformingEvent(worstPerformingEvent)
                .mostActiveEvent(mostActiveEvent)
                .build();
    }
}
