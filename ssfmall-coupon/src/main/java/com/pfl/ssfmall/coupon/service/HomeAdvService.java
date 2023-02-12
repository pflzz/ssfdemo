package com.pfl.ssfmall.coupon.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.pfl.common.utils.PageUtils;
import com.pfl.ssfmall.coupon.entity.HomeAdvEntity;

import java.util.Map;

/**
 * 首页轮播广告
 *
 * @author ssf
 * @email ${email}
 * @date 2022-06-01 23:03:48
 */
public interface HomeAdvService extends IService<HomeAdvEntity> {

    PageUtils queryPage(Map<String, Object> params);
}

