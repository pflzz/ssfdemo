package com.pfl.ssfmall.product.service.impl;

import com.pfl.ssfmall.product.dao.SkuInfoDao;
import com.pfl.ssfmall.product.entity.SkuImagesEntity;
import com.pfl.ssfmall.product.entity.SkuInfoEntity;
import com.pfl.ssfmall.product.entity.SpuInfoDescEntity;
import com.pfl.ssfmall.product.service.AttrGroupService;
import com.pfl.ssfmall.product.service.SkuImagesService;
import com.pfl.ssfmall.product.service.SpuInfoDescService;
import com.pfl.ssfmall.product.vo.SkuItemSaleAttrVo;
import com.pfl.ssfmall.product.vo.SkuItemVo;
import com.pfl.ssfmall.product.vo.SpuItemGroupAttrVo;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ThreadPoolExecutor;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.pfl.common.utils.PageUtils;
import com.pfl.common.utils.Query;

import com.pfl.ssfmall.product.service.SkuInfoService;

import javax.annotation.Resource;


@Service("skuInfoService")
public class SkuInfoServiceImpl extends ServiceImpl<SkuInfoDao, SkuInfoEntity> implements SkuInfoService {

    @Resource
    private SkuImagesService skuImagesService;
    @Resource
    private SpuInfoDescService spuInfoDescService;
    @Resource
    private AttrGroupService attrGroupService;
    @Resource
    private SkuInfoService skuInfoService;
    @Resource
    private SkuInfoDao skuInfoDao;
    @Autowired
    private ThreadPoolExecutor executor;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<SkuInfoEntity> page = this.page(
                new Query<SkuInfoEntity>().getPage(params),
                new QueryWrapper<SkuInfoEntity>()
        );

        return new PageUtils(page);
    }

    /**
     * sku 条件检索
     */
    @Override
    public PageUtils queryPageByCondition(Map<String, Object> params) {
        QueryWrapper<SkuInfoEntity> queryWrapper = new QueryWrapper<>();

        /**
         * key:
         * catelogId: 0
         * brandId: 0
         * min: 0
         * max: 0
         */
        String key = (String) params.get("key");
        if (!StringUtils.isEmpty(key)) {
            queryWrapper.and(w -> w.eq("id", key).or().like("spu_name", key));
        }
        String catelogId = (String) params.get("catelogId");
        if (!StringUtils.isEmpty(catelogId) && !"0".equalsIgnoreCase(catelogId)) {
            queryWrapper.eq("catalog_id", catelogId);
        }
        String brandId = (String) params.get("brandId");
        if (!StringUtils.isEmpty(brandId) && !"0".equalsIgnoreCase(brandId)) {
            queryWrapper.eq("brand_id", brandId);
        }
        String min = (String) params.get("min");
        if (!StringUtils.isEmpty(min)) {
            queryWrapper.ge("price", min);
        }
        String max = (String) params.get("max");
        if (!StringUtils.isEmpty(max)) {
            try {
                BigDecimal bigDecimal = new BigDecimal("0");
                if (bigDecimal.compareTo(new BigDecimal(max)) == -1) {
                    queryWrapper.le("price", max);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        IPage<SkuInfoEntity> page = this.page(
                new Query<SkuInfoEntity>().getPage(params),
                queryWrapper
        );

        return new PageUtils(page);
    }

    @Override
    public List<SkuInfoEntity> getSkuBySpuId(Long spuId) {
        List<SkuInfoEntity> list = this.list(new QueryWrapper<SkuInfoEntity>().eq("spu_id", spuId));
        return list;
    }

    /**
     * 获取商品详情页面渲染数据
     *
     * @param skuId 指定的商品
     * @return
     */
    @Override
    public SkuItemVo getSkuItem(Long skuId) throws ExecutionException, InterruptedException {
        SkuItemVo skuItemVo = new SkuItemVo();

        // 有返回结果的异步方法 / executor 使用自己的线程池编排任务
        CompletableFuture<SkuInfoEntity> infoFuture = CompletableFuture.supplyAsync(() -> {
            //1. sku 基本信息获取
            SkuInfoEntity info = getById(skuId);
            skuItemVo.setInfo(info);
            return info;
        }, executor);

        // 可以接受上一步的结果的异步任务
        CompletableFuture<Void> saleAttrFuture = infoFuture.thenAcceptAsync((res) -> {
            // 3. 获取 spu 销售属性组合
            List<SkuItemSaleAttrVo> skuItemSaleAttrVos = skuInfoService.getSkuItemSaleAttr(res.getSpuId());
            skuItemVo.setSaleAttr(skuItemSaleAttrVos);
        }, executor);

        CompletableFuture<Void> desFuture = infoFuture.thenAcceptAsync((res) -> {
            // 4. 获取 spu 介绍
            SpuInfoDescEntity infoDescEntity = spuInfoDescService.getById(res.getSpuId());
            skuItemVo.setDesc(infoDescEntity);
        }, executor);
        CompletableFuture<Void> baseAttrFuture = infoFuture.thenAcceptAsync((res) -> {
            // 5. 获取 spu 的规格参数信息
            List<SpuItemGroupAttrVo> spuItemGroupAttrVos = attrGroupService.getAttrGroupWithAttr(res.getCatalogId(), res.getSpuId());
            skuItemVo.setGroupAttr(spuItemGroupAttrVos);
        }, executor);

        // 异步运行任务，无需参数和返回值
        CompletableFuture<Void> imageFuture = CompletableFuture.runAsync(() -> {
            // 2. sku 图片信息
            List<SkuImagesEntity> skuImagesEntities = skuImagesService.list(new QueryWrapper<SkuImagesEntity>().eq("sku_id", skuId));
            skuItemVo.setImages(skuImagesEntities);
        }, executor);

        // 阻塞等待所有异步任务都完成，返回结果
        CompletableFuture.allOf(saleAttrFuture, baseAttrFuture, desFuture, imageFuture).get();
        return skuItemVo;
    }

    /**
     * 获取 spu 所有的销售属性及其属性值
     * @param spuId
     * @return
     */
    @Override
    public List<SkuItemSaleAttrVo> getSkuItemSaleAttr(Long spuId) {
        List<SkuItemSaleAttrVo> skuItemSaleAttrVos = skuInfoDao.getSkuItemSaleAttr(spuId);
        return skuItemSaleAttrVos;
    }

}