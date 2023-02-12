package com.pfl.ssfmall.order.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.pfl.common.utils.PageUtils;
import com.pfl.ssfmall.order.entity.OrderEntity;
import com.pfl.ssfmall.order.vo.OrderConfirmVo;
import com.pfl.ssfmall.order.vo.OrderSubmitVo;
import com.pfl.ssfmall.order.vo.SubmitRespVo;

import java.util.Map;

/**
 * 订单
 *
 * @author ssf
 * @email ${email}
 * @date 2022-06-02 11:11:58
 */
public interface OrderService extends IService<OrderEntity> {

    PageUtils queryPage(Map<String, Object> params);

    OrderConfirmVo getConfirmOrderData();

    SubmitRespVo orderSubmit(OrderSubmitVo vo);
}

