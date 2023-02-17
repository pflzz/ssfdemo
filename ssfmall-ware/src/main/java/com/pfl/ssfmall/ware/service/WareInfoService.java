package com.pfl.ssfmall.ware.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.pfl.common.utils.PageUtils;
import com.pfl.ssfmall.ware.model.entity.WareInfoEntity;

import java.util.Map;

/**
 * 仓库信息
 *
 * @author ssf
 * @email ${email}
 * @date 2022-06-02 11:22:38
 */
public interface WareInfoService extends IService<WareInfoEntity> {

    PageUtils queryPage(Map<String, Object> params);
}

