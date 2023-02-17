package com.pfl.ssfmall.order.model.dto;

import com.pfl.ssfmall.order.model.vo.MemberAddressVo;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class FareTo {
    // 运费金额
    private BigDecimal fare;
    // 地址信息
    private MemberAddressVo memberAddressVo;
}
