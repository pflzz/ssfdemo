package com.pfl.ssfmall.ware.service.impl;

import com.pfl.common.constant.WareConstant;
import com.pfl.ssfmall.ware.entity.PurchaseDetailEntity;
import com.pfl.ssfmall.ware.service.PurchaseDetailService;
import com.pfl.ssfmall.ware.service.WareSkuService;
import com.pfl.ssfmall.ware.vo.PurchaseDoneVo;
import com.pfl.ssfmall.ware.vo.PurchaseItemDoneVo;
import com.pfl.ssfmall.ware.vo.PurchaseMergeVo;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.pfl.common.utils.PageUtils;
import com.pfl.common.utils.Query;

import com.pfl.ssfmall.ware.dao.PurchaseDao;
import com.pfl.ssfmall.ware.entity.PurchaseEntity;
import com.pfl.ssfmall.ware.service.PurchaseService;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;


@Service("purchaseService")
public class PurchaseServiceImpl extends ServiceImpl<PurchaseDao, PurchaseEntity> implements PurchaseService {

    @Resource
    private PurchaseDetailService purchaseDetailService;
    @Resource
    private WareSkuService wareSkuService;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        QueryWrapper<PurchaseEntity> queryWrapper = new QueryWrapper<>();
        /**
         * key:
         * status: 0
         * wareId: 2
         */
        String key = (String) params.get("key");
        if (!StringUtils.isEmpty(key)) {
            queryWrapper.eq("purchase_id", key).or().eq("sku_id", key);
        }
        String status = (String) params.get("status");
        if (!StringUtils.isEmpty(status)) {
            queryWrapper.eq("status", status);
        }
        String wareId = (String) params.get("wareId");
        if (!StringUtils.isEmpty(wareId)) {
            queryWrapper.eq("ware_id", wareId);
        }
        IPage<PurchaseEntity> page = this.page(
                new Query<PurchaseEntity>().getPage(params),
                queryWrapper
        );

        return new PageUtils(page);
    }

    /**
     * 查询未领取的采购单
     * @return
     */
    @Override
    public PageUtils queryPageWithUnreceiveList(Map<String, Object> params) {
        QueryWrapper<PurchaseEntity> queryWrapper = new QueryWrapper<>();
        IPage<PurchaseEntity> page = this.page(
                new Query<PurchaseEntity>().getPage(params),
                queryWrapper.eq("status", 0).or().eq("status", 1)
        );
        return new PageUtils(page);
    }

    /**
     * 合并采购需求 到采购单
     * @return
     */
    @Transactional
    @Override
    public void mergePurchase(PurchaseMergeVo purchaseMergeVo) {
        Long purchaseId = purchaseMergeVo.getPurchaseId();
        // 当 PurchaseId 为空时 创建一个新的采购单
        if (purchaseMergeVo.getPurchaseId() == null) {
            PurchaseEntity purchaseEntity = new PurchaseEntity();
            purchaseEntity.setStatus(WareConstant.PurchaseStatusEnum.CREATED.getCode());
            purchaseEntity.setCreateTime(new Date());
            purchaseEntity.setUpdateTime(new Date());
            this.save(purchaseEntity);
            purchaseId = purchaseEntity.getId();
        }
        Long finalPurchaseId = purchaseId;
        PurchaseEntity purchaseEntity = this.getById(finalPurchaseId);
        // 只有当采购单的状态为 0 / 1 时，才可以进行合并
        if (purchaseEntity.getStatus() == WareConstant.PurchaseStatusEnum.CREATED.getCode()
                || purchaseEntity.getStatus() == WareConstant.PurchaseStatusEnum.ASSIGNED.getCode()) {
            // 批量保存采购需求
            List<Long> items = purchaseMergeVo.getItems();
            List<PurchaseDetailEntity> collect = items.stream().map(item -> {
                PurchaseDetailEntity purchaseDetailEntity = new PurchaseDetailEntity();
                purchaseDetailEntity.setId(item);
                purchaseDetailEntity.setPurchaseId(finalPurchaseId);
                purchaseDetailEntity.setStatus(WareConstant.PurchaseDetailsStatusEnum.ASSIGNED.getCode());
                return purchaseDetailEntity;
            }).collect(Collectors.toList());
            purchaseDetailService.updateBatchById(collect);


        }
        // 操作完成以后 应该更改 采购单的更新时间
        PurchaseEntity purchaseEntity1 = new PurchaseEntity();
        purchaseEntity1.setId(finalPurchaseId);
        purchaseEntity1.setUpdateTime(new Date());
        this.updateById(purchaseEntity1);
    }

    /**
     * 领取采购单
     * @param ids 采购单id
     * @return
     */
    @Transactional
    @Override
    public void received(List<Long> ids) {
        // 1. 确认当前采购单是新建或者已分配状态
        List<PurchaseEntity> collect = ids.stream().map(id -> {
            PurchaseEntity purchaseEntity = this.getById(id);
            return purchaseEntity;
        }).filter(item -> {
            if (item.getStatus() == WareConstant.PurchaseStatusEnum.CREATED.getCode()
                    || item.getStatus() == WareConstant.PurchaseStatusEnum.ASSIGNED.getCode()) {
                return true;
            }
            return false;
        }).map(item -> {
            item.setStatus(WareConstant.PurchaseStatusEnum.RECEIVED.getCode());
            item.setUpdateTime(new Date());
            return item;
        }).collect(Collectors.toList());

        // 2、改变采购单的状态
        this.updateBatchById(collect);
        //3、改变采购项的状态

        collect.forEach(item -> {
            // 根据采购单id 获取采购单中的所有采购项
            List<PurchaseDetailEntity> entities = purchaseDetailService.GetPurchaseDetailById(item.getId());
            List<PurchaseDetailEntity> detailEntities = entities.stream().map(entity -> {
                PurchaseDetailEntity purchaseDetailEntity = new PurchaseDetailEntity();
                purchaseDetailEntity.setStatus(WareConstant.PurchaseDetailsStatusEnum.BUYING.getCode());
                purchaseDetailEntity.setId(entity.getId());
                return purchaseDetailEntity;
            }).collect(Collectors.toList());
            purchaseDetailService.updateBatchById(detailEntities);
        });
    }

    /**
     * 完成采购
     * @return
     */
    @Transactional
    @Override
    public void done(PurchaseDoneVo vo) {
        // 1. 修改采购项状态

        // 判断是否所有采购项都采购完成，若有一项采购失败 则将采购单状态设置为有异常
        boolean flag = true;
        List<PurchaseItemDoneVo> items = vo.getItems();
        List<PurchaseDetailEntity> update = new ArrayList<>();
        for (PurchaseItemDoneVo item : items) {
            PurchaseDetailEntity purchaseDetailEntity = new PurchaseDetailEntity();
            purchaseDetailEntity.setId(item.getItemId());
            // 采购失败
            if (item.getStatus() == WareConstant.PurchaseDetailsStatusEnum.HASERROR.getCode()) {
                flag = false;
                purchaseDetailEntity.setStatus(item.getStatus());
                purchaseDetailService.updateById(purchaseDetailEntity);
            }  else {
                // 采购完成
                purchaseDetailEntity.setStatus(WareConstant.PurchaseDetailsStatusEnum.FINISHED.getCode());
                purchaseDetailService.updateById(purchaseDetailEntity);
                // 3. 修改库存表状态 将采购成功的采购项入库
                PurchaseDetailEntity detailEntity = purchaseDetailService.getById(item.getItemId());
                // 完成采购后 添加 / 修改 库存信息
                wareSkuService.addStock(detailEntity.getSkuId(), detailEntity.getWareId(), detailEntity.getSkuNum());
            }
        }
        purchaseDetailService.updateBatchById(update);
        // 2. 修改采购单状态
        PurchaseEntity purchaseEntity = new PurchaseEntity();
        purchaseEntity.setId(vo.getId());
        purchaseEntity.setStatus(flag ? WareConstant.PurchaseStatusEnum.FINISHED.getCode() : WareConstant.PurchaseStatusEnum.HASERROR.getCode());
        purchaseEntity.setUpdateTime(new Date());
        this.updateById(purchaseEntity);
    }

}