package com.pfl.ssfmall.order.model.vo;

import com.pfl.ssfmall.order.model.entity.OrderEntity;
import lombok.Data;

@Data
public class SubmitRespVo {
    private OrderEntity order;
    private Integer code;
}
