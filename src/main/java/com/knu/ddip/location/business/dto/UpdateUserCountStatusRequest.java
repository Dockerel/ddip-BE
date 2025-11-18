package com.knu.ddip.location.business.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
public class UpdateUserCountStatusRequest {
    private String prevCellId;
    private String currentCellId;

    @Builder
    public UpdateUserCountStatusRequest(String prevCellId, String currentCellId) {
        this.prevCellId = prevCellId;
        this.currentCellId = currentCellId;
    }
}
