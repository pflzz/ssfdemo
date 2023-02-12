package com.pfl.common.to;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class SpuBoundsTo {
    private BigDecimal buyBounds;
    private BigDecimal growBounds;
    private Long spuId;
}
