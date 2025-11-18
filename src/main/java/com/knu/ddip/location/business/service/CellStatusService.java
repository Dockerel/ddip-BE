package com.knu.ddip.location.business.service;

import com.knu.ddip.location.business.util.FenwickTree;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CellStatusService {

    private final LocationReader locationReader;

    private final FenwickTree fenwickTree;

    private final Map<String, Integer> cellIdToIndex = new HashMap<>();

    @PostConstruct
    public void init() {
        List<String> cellIds = locationReader.findAllCellIds();

        for (int i = 0; i < cellIds.size(); i++) {
            cellIdToIndex.put(cellIds.get(i), i + 1);
        }
    }

    public int getAllCurrentUsersCount() {
        return fenwickTree.prefixSum(cellIdToIndex.size());
    }

    public int getCurrentUsersCountByCellId(String cellId) {
        Integer idx = cellIdToIndex.get(cellId);
        return idx == null ? 0 : fenwickTree.rangeSum(idx, idx);
    }

    public void updateUserCountByCellId(String cellId, int count) {
        int index = cellIdToIndex.get(cellId);
        fenwickTree.update(index, count);
    }

}
