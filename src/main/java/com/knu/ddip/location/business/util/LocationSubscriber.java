package com.knu.ddip.location.business.util;

import com.knu.ddip.location.business.dto.UpdateUserCountStatusRequest;
import com.knu.ddip.location.business.service.CellStatusService;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class LocationSubscriber {

    private final CellStatusService cellStatusService;

    @RabbitListener(queues = "#{@dynamicUpdateUserCountStatusQueueName}")
    public void consumePublishNotificationMessage(UpdateUserCountStatusRequest request) {
        if (request.getPrevCellId() != null) {
            cellStatusService.updateUserCountByCellId(request.getPrevCellId(), -1);
        }
        if (request.getCurrentCellId() != null) {
            cellStatusService.updateUserCountByCellId(request.getCurrentCellId(), 1);
        }
    }

}
