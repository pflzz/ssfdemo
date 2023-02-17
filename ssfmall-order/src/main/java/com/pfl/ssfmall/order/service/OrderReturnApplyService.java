package com.pfl.ssfmall.order.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.pfl.common.utils.PageUtils;
import com.pfl.ssfmall.order.model.entity.OrderReturnApplyEntity;

import java.util.Map;

/**
 * 订单退货申请
 *
 * @author ssf
 * @email ${email}
 * @date 2022-06-02 11:11:58
 */
public interface OrderReturnApplyService extends IService<OrderReturnApplyEntity> {

    PageUtils queryPage(Map<String, Object> params);
}

