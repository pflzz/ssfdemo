package com.pfl.ssfmall.ware.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.pfl.common.utils.PageUtils;
import com.pfl.ssfmall.ware.model.entity.UndoLogEntity;

import java.util.Map;

/**
 * 
 *
 * @author ssf
 * @email ${email}
 * @date 2022-06-02 11:22:38
 */
public interface UndoLogService extends IService<UndoLogEntity> {

    PageUtils queryPage(Map<String, Object> params);
}

