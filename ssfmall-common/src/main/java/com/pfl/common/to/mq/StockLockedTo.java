package com.pfl.common.to.mq;

import lombok.Data;

import java.util.List;

@Data
public class StockLockedTo {
    private Long id; // 库存工作单的 id
    private List<Long> detailId; //工作详情的所有 id
}
