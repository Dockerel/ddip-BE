package com.knu.ddip.location.business.util;

import com.knu.ddip.common.config.RabbitMQConfig;
import com.knu.ddip.location.business.dto.UpdateUserCountStatusRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class LocationPublisher {

    private final RabbitTemplate rabbitTemplate;

    public void publishUpdateUserCountStatusMessage(String prevCellId, String currentCellId) {
        UpdateUserCountStatusRequest request = UpdateUserCountStatusRequest.builder()
                .prevCellId(prevCellId)
                .currentCellId(currentCellId)
                .build();
        rabbitTemplate.convertAndSend(RabbitMQConfig.UPDATE_USER_COUNT_STATUS_EXCHANGE, RabbitMQConfig.UPDATE_USER_COUNT_STATUS_QUEUE, request);
    }

}
