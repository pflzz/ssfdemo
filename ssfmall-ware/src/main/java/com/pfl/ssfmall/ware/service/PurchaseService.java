package com.pfl.ssfmall.ware.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.pfl.common.utils.PageUtils;
import com.pfl.ssfmall.ware.model.entity.PurchaseEntity;
import com.pfl.ssfmall.ware.model.vo.PurchaseDoneVo;
import com.pfl.ssfmall.ware.model.vo.PurchaseMergeVo;

import java.util.List;
import java.util.Map;

/**
 * 采购信息
 *
 * @author ssf
 * @email ${email}
 * @date 2022-06-02 11:22:38
 */
public interface PurchaseService extends IService<PurchaseEntity> {

    PageUtils queryPage(Map<String, Object> params);

    /**
     * 查询未领取的采购单
     * @return
     */
    PageUtils queryPageWithUnreceiveList(Map<String, Object> params);

    /**
     * 合并采购需求 到采购单
     * @return
     */
    void mergePurchase(PurchaseMergeVo purchaseMergeVo);


    /**
     * 领取采购单
     * @param ids 采购单id
     * @return
     */
    void received(List<Long> ids);

    /**
     * 完成采购
     * @return
     */
    void done(PurchaseDoneVo vo);
}

