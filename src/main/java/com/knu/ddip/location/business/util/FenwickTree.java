package com.knu.ddip.location.business.util;

import com.knu.ddip.location.business.service.LocationReader;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class FenwickTree {

    private final LocationReader locationReader;

    private int[] tree;
    private int size;

    @PostConstruct
    public void init() {
        List<String> cellIds = locationReader.findAllCellIds();

        size = cellIds.size();
        tree = new int[size + 1];
    }

    // 값 업데이트
    public void update(int idx, int delta) {
        while (idx <= size) {
            tree[idx] += delta;
            idx += (idx & -idx); // 가장 낮은 1비트만큼 인덱스 증가
        }
    }

    // prefix sum: 1~idx까지 합
    public int prefixSum(int idx) {
        int sum = 0;
        while (idx > 0) {
            sum += tree[idx];
            idx -= (idx & -idx); // 가장 낮은 1비트만큼 인덱스 감소
        }
        return sum;
    }

    // 구간합: l~r
    public int rangeSum(int l, int r) {
        return prefixSum(r) - prefixSum(l - 1);
    }

    public void clear() {
        tree = new int[size + 1];
    }

}