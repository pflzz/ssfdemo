package com.pfl.ssfmall.order.model.dao;

import com.pfl.ssfmall.order.entity.OrderEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 订单
 * 
 * @author ssf
 * @email ${email}
 * @date 2022-06-02 11:11:58
 */
@Mapper
public interface OrderDao extends BaseMapper<OrderEntity> {
	
}
