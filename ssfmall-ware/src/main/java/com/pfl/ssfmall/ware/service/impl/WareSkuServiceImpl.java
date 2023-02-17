package com.pfl.ssfmall.ware.service.impl;

import com.pfl.common.utils.R;
import com.pfl.ssfmall.ware.exception.NoStockException;
import com.pfl.ssfmall.ware.feign.ProductFeignService;
import com.pfl.ssfmall.ware.model.dto.WareSkuLockedTo;
import com.pfl.ssfmall.ware.model.vo.OrderItemVo;
import com.pfl.ssfmall.ware.model.vo.SkuStockVo;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.pfl.common.utils.PageUtils;
import com.pfl.common.utils.Query;

import com.pfl.ssfmall.ware.dao.WareSkuDao;
import com.pfl.ssfmall.ware.model.entity.WareSkuEntity;
import com.pfl.ssfmall.ware.service.WareSkuService;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;


@Service("wareSkuService")
public class WareSkuServiceImpl extends ServiceImpl<WareSkuDao, WareSkuEntity> implements WareSkuService {

    @Resource
    private WareSkuDao wareSkuDao;
    @Resource
    private ProductFeignService productFeignService;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        QueryWrapper<WareSkuEntity> queryWrapper = new QueryWrapper<>();

        /**
         * skuId: 1
         * wareId: 2
         */
        String skuId = (String) params.get("skuId");
        if (!StringUtils.isEmpty(skuId)) {
            queryWrapper.eq("sku_id", skuId);
        }
        String wareId = (String) params.get("wareId");
        if (!StringUtils.isEmpty(wareId)) {
            queryWrapper.eq("ware_id", wareId);
        }
        IPage<WareSkuEntity> page = this.page(
                new Query<WareSkuEntity>().getPage(params),
                queryWrapper
        );

        return new PageUtils(page);
    }

    /**
     * 完成采购后 添加 / 修改 库存信息
     *
     * @param skuId  采购项 id
     * @param wareId 仓库 id
     * @param skuNum 采购数量
     */
    @Override
    public void addStock(Long skuId, Long wareId, Integer skuNum) {

        List<WareSkuEntity> entities = wareSkuDao.selectList(new QueryWrapper<WareSkuEntity>().eq("sku_id", skuId).eq("ware_id", wareId));
        WareSkuEntity entity = new WareSkuEntity();
        if (entities != null && entities.size() != 0) {
            // 修改库存信息
            wareSkuDao.addStock(skuId, wareId, skuNum);
        } else {
            // 获取 sku 名称
            R info = productFeignService.info(skuId);
            Map<String, Object> data = (Map<String, Object>) info.get("skuInfo");

            if (info.getCode() == 0) {
                entity.setSkuName((String) data.get("skuName"));
            }
            // 新增库存信息
            entity.setSkuId(skuId);
            entity.setWareId(wareId);
            entity.setStock(skuNum);
            ;
            entity.setStockLocked(0);
            wareSkuDao.insert(entity);
        }
    }

    /**
     * 检索该商品是否有库存
     */
    @Override
    public List<SkuStockVo> hasStock(List<Long> skuIds) {

        List<SkuStockVo> collect = skuIds.stream().map(skuId -> {
            SkuStockVo stockVo = new SkuStockVo();
            Long stock = wareSkuDao.hasStock(skuId);
            stockVo.setHasStock(stock != null && stock > 0);
            stockVo.setSkuId(skuId);
            return stockVo;
        }).collect(Collectors.toList());
        return collect;
    }

    @Transactional
    @Override
    public Boolean lockStock(WareSkuLockedTo wareSkuLockedTo) {
        List<OrderItemVo> orderItemVos = wareSkuLockedTo.getLocks();
        List<SkuHashStock> hashStocks = orderItemVos.stream().map(item -> {
            SkuHashStock skuHashStock = new SkuHashStock();
            skuHashStock.setSkuId(item.getSkuId());
            // 查询哪些仓库有库存
            List<Long> wareIds = wareSkuDao.wareHasStock(item.getSkuId(), item.getCount());
            skuHashStock.setWareIds(wareIds);
            skuHashStock.setCount(item.getCount());
            return skuHashStock;
        }).collect(Collectors.toList());

        for (SkuHashStock hashStock : hashStocks) {
            Long skuId = hashStock.getSkuId();
            List<Long> wareIds = hashStock.getWareIds();
            if (wareIds == null || wareIds.size() == 0) {
                throw new NoStockException(skuId);
            }
            boolean locked = false;
            // 遍历仓库锁定库存
            for (Long wareId : wareIds) {
                Long count = wareSkuDao.lockStock(skuId, wareId, hashStock.getCount());
                if (count > 0) {
                    locked = true;
                    break;
                }
            }
            if (!locked) {
                throw new NoStockException(skuId);
            }
        }
        return true;

    }

    @Data
    class SkuHashStock {
        private Long skuId;
        private List<Long> wareIds;
        private Integer count;
    }

}