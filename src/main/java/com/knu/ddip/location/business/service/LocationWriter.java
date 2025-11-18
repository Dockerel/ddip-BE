package com.knu.ddip.location.business.service;

import java.util.List;

public interface LocationWriter {
    void deleteAll();

    void saveAll(List<String> cellIds);

    String saveUserIdByCellIdAtomic(String newCellId, boolean cellIdNotInTargetArea, String encodedUserId);

    void cleanupExpiredUserLocations(long now);
}
