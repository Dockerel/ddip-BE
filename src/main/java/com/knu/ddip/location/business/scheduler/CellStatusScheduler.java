package com.knu.ddip.location.business.scheduler;

import com.knu.ddip.location.business.service.CellStatusService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CellStatusScheduler {

    private final CellStatusService cellStatusService;

    @Scheduled(cron = "0 0 * * * *") // 매 정시
    public void cleanupUserLocationsOnFenwickTree() {
        cellStatusService.clearUserCount();
    }

}