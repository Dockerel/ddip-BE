package com.knu.ddip.location.business.service;

import java.util.List;

public interface LocationReader {
    void validateLocationByCellId(String cellId);

    List<String> findAllLocationsByCellIdIn(List<String> cellIds);

    List<String> findUserIdsByCellIds(List<String> targetCellIds);

    boolean isCellIdNotInTargetArea(String cellId);

    List<String> findAllCellIds();

    int getUsersCountByCellId(String cellId);
}
