package com.pfl.ssfmall.order.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.pfl.common.utils.PageUtils;
import com.pfl.ssfmall.order.model.entity.RefundInfoEntity;

import java.util.Map;

/**
 * 退款信息
 *
 * @author ssf
 * @email ${email}
 * @date 2022-06-02 11:11:58
 */
public interface RefundInfoService extends IService<RefundInfoEntity> {

    PageUtils queryPage(Map<String, Object> params);
}

