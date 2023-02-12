package com.pfl.ssfmall.product.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.pfl.common.constant.ProductConstant;
import com.pfl.common.to.SkuEsModel;
import com.pfl.common.to.SkuReductionTo;
import com.pfl.common.to.SkuStockVo;
import com.pfl.common.to.SpuBoundsTo;
import com.pfl.common.utils.R;
import com.pfl.ssfmall.product.dao.SpuInfoDao;
import com.pfl.ssfmall.product.entity.*;
import com.pfl.ssfmall.product.feign.CouponFeignService;
import com.pfl.ssfmall.product.feign.SearchFeignService;
import com.pfl.ssfmall.product.feign.WareFeignService;
import com.pfl.ssfmall.product.service.*;
import com.pfl.ssfmall.product.vo.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.pfl.common.utils.PageUtils;
import com.pfl.common.utils.Query;

import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;

@Slf4j
@Service("spuInfoService")
public class SpuInfoServiceImpl extends ServiceImpl<SpuInfoDao, SpuInfoEntity> implements SpuInfoService {

    @Resource
    private SpuInfoDescService spuInfoDescService;
    @Resource
    private SpuImagesService spuImagesService;
    @Resource
    private ProductAttrValueService productAttrValueService;
    @Resource
    private AttrService attrService;
    @Resource
    private SkuInfoService skuInfoService;
    @Resource
    private SkuImagesService skuImagesService;
    @Resource
    private SkuSaleAttrValueService skuSaleAttrValueService;
    @Resource
    private CouponFeignService couponFeignService;
    @Resource
    private BrandService brandService;
    @Resource
    private CategoryService categoryService;
    @Resource
    private WareFeignService wareFeignService;
    @Resource
    private SearchFeignService searchFeignService;


    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<SpuInfoEntity> page = this.page(
                new Query<SpuInfoEntity>().getPage(params),
                new QueryWrapper<SpuInfoEntity>()
        );

        return new PageUtils(page);
    }

    /**
     * 保存 spu info
     *
     * @param vo
     */
    @Transactional
    @Override
    public void saveSpuInfo(SpuSaveVo vo) {
        // 1. 保存 spu 基本信息
        SpuInfoEntity spuInfoEntity = new SpuInfoEntity();
        BeanUtils.copyProperties(vo, spuInfoEntity);
        spuInfoEntity.setCreateTime(new Date());
        spuInfoEntity.setUpdateTime(new Date());
        this.baseMapper.insert(spuInfoEntity);

        // 2. 保存 spu 的描述图片 pms_spu_info_desc
        List<String> decript = vo.getDecript();
        SpuInfoDescEntity spuInfoDescEntity = new SpuInfoDescEntity();
        spuInfoDescEntity.setSpuId(spuInfoEntity.getId());
        spuInfoDescEntity.setDecript(String.join(",", decript));
        spuInfoDescService.save(spuInfoDescEntity);

        // 3. 保存 spu 图集 pms_spu_images
        List<String> images = vo.getImages();
        List<SpuImagesEntity> imagesEntities = images.stream().map(item -> {
            SpuImagesEntity spuImagesEntity = new SpuImagesEntity();
            spuImagesEntity.setSpuId(spuInfoEntity.getId());
            spuImagesEntity.setImgUrl(item);
            return spuImagesEntity;
        }).filter(entity -> !StringUtils.isEmpty(entity.getImgUrl())).collect(Collectors.toList());
        spuImagesService.saveBatch(imagesEntities);

        // 4. 保存 spu 规格参数 pms_product_attr_value
        List<BaseAttrs> baseAttrs = vo.getBaseAttrs();
        List<ProductAttrValueEntity> attrValueEntities = baseAttrs.stream().map(item -> {
            ProductAttrValueEntity productAttrValueEntity = new ProductAttrValueEntity();
            productAttrValueEntity.setSpuId(spuInfoEntity.getId());
            productAttrValueEntity.setAttrId(item.getAttrId());
            AttrEntity id = attrService.getById(item.getAttrId());
            productAttrValueEntity.setAttrName(id.getAttrName());
            productAttrValueEntity.setAttrValue(item.getAttrValues());
            productAttrValueEntity.setQuickShow(item.getShowDesc());
            return productAttrValueEntity;
        }).collect(Collectors.toList());
        productAttrValueService.saveBatch(attrValueEntities);

        // 5. 保存 spu 的积分信息 ssfmall_sms -> sms_spu_bounds
        Bounds bounds = vo.getBounds();
        // 需要添加 spu 信息
        SpuBoundsTo spuBoundsTo = new SpuBoundsTo();
        BeanUtils.copyProperties(bounds, spuBoundsTo);
        spuBoundsTo.setSpuId(spuInfoEntity.getId());
        R r = couponFeignService.saveBounds(spuBoundsTo);
        if (r.getCode() != 0) {
            log.error("远程保存spu积分信息失败");
        }

        // 6. 保存当前 spu 对应的所有 sku 信息
        List<Skus> skus = vo.getSkus();
        if (skus != null && skus.size() > 0) {
            skus.forEach(item -> {
                SkuInfoEntity skuInfoEntity = new SkuInfoEntity();
                BeanUtils.copyProperties(item, skuInfoEntity);
                skuInfoEntity.setBrandId(spuInfoEntity.getBrandId());
                skuInfoEntity.setCatalogId(spuInfoEntity.getCatalogId());
                skuInfoEntity.setSaleCount(0L);
                skuInfoEntity.setSpuId(spuInfoEntity.getId());

                String defaultImage = "";
                // 设置默认图片
                for (Images image : item.getImages()) {
                    if (image.getDefaultImg() == 1) {
                        defaultImage = image.getImgUrl();
                    }
                }
                skuInfoEntity.setSkuDefaultImg(defaultImage);

                // 6.1 sku 基本信息 pms_sku_info
                skuInfoService.save(skuInfoEntity);

                // 6.2 sku 图片信息 pms_sku_images
                List<SkuImagesEntity> skuImagesEntities = item.getImages().stream().map(img -> {
                    SkuImagesEntity skuImagesEntity = new SkuImagesEntity();
                    skuImagesEntity.setSkuId(skuInfoEntity.getSkuId());
                    skuImagesEntity.setImgUrl(img.getImgUrl());
                    skuImagesEntity.setDefaultImg(img.getDefaultImg());
                    return skuImagesEntity;
                }).collect(Collectors.toList());
                skuImagesService.saveBatch(skuImagesEntities);

                // 6.3 sku 销售属性信息 pms_sku_sale_attr_value
                List<Attr> attr = item.getAttr();
                List<SkuSaleAttrValueEntity> skuSaleAttrValueEntities = attr.stream().map(a -> {
                    SkuSaleAttrValueEntity skuSaleAttrValueEntity = new SkuSaleAttrValueEntity();
                    BeanUtils.copyProperties(a, skuSaleAttrValueEntity);
                    skuSaleAttrValueEntity.setSkuId(skuInfoEntity.getSkuId());
                    return skuSaleAttrValueEntity;
                }).collect(Collectors.toList());
                skuSaleAttrValueService.saveBatch(skuSaleAttrValueEntities);

                // 6.4 sku的优惠、满减等信息；ssfmall_sms->sms_sku_ladder\sms_sku_full_reduction\sms_member_price
                SkuReductionTo skuReductionTo = new SkuReductionTo();
                BeanUtils.copyProperties(item, skuReductionTo);
                skuReductionTo.setSkuId(skuInfoEntity.getSkuId());
                if (skuReductionTo.getFullCount() > 0 || skuReductionTo.getFullPrice().compareTo(new BigDecimal("0")) == 1) {
                    couponFeignService.saveReduction(skuReductionTo);
                }
            });
        }
    }

    /**
     * spu 检索
     */
    @Override
    public PageUtils queryPageDetails(Map<String, Object> params) {
        QueryWrapper<SpuInfoEntity> queryWrapper = new QueryWrapper<>();

        String key = (String) params.get("key");
        if (!StringUtils.isEmpty(key)) {
            queryWrapper.and(w -> w.eq("id", key).or().like("spu_name", key));
        }

//        status: 0
//        key:
//        brandId: 7
//        catelogId: 225
        String status = (String) params.get("status");
        if (!StringUtils.isEmpty(status)) {
            queryWrapper.eq("publish_status", status);
        }
        String brandId = (String) params.get("brandId");
        if (!StringUtils.isEmpty(brandId) && !"0".equalsIgnoreCase(brandId)) {
            queryWrapper.eq("brand_id", brandId);
        }
        String catelogId = (String) params.get("catelogId");
        if (!StringUtils.isEmpty(catelogId) && !"0".equalsIgnoreCase(catelogId)) {
            queryWrapper.eq("catalog_id", catelogId);
        }
        IPage<SpuInfoEntity> page = this.page(
                new Query<SpuInfoEntity>().getPage(params),
                queryWrapper
        );

        return new PageUtils(page);
    }

    /**
     * 商品上架
     *
     * @param spuId
     * @return
     */
    @Override
    public void up(Long spuId) {
        // 封装 es 存储的数据类型

        // 1. 根据 spuId 查询 sku 基本信息
        List<SkuInfoEntity> skus = skuInfoService.getSkuBySpuId(spuId);


        // 2.  查询当前 sku 所有可以检索的规格属性
        List<ProductAttrValueEntity> attrValueEntityList = productAttrValueService.baseListForSpu(spuId);

        List<ProductAttrValueEntity> collect = attrValueEntityList.stream().filter(item ->
                item.getQuickShow() == 1
        ).collect(Collectors.toList());

        // 考虑到系统的性能，将多个查询合并为一个（使用集合）查询的方式
        List<Long> skuIds = skus.stream().map(SkuInfoEntity::getSkuId).collect(Collectors.toList());
        R r;
        List<SkuStockVo> skuStockVos;
        Map<Long, Boolean> skuStockMap = null;
        try {
            r = wareFeignService.hasStock(skuIds);
            skuStockVos = r.getData("data", new TypeReference<List<SkuStockVo>>(){});
            skuStockMap = skuStockVos.stream().collect(Collectors.toMap(SkuStockVo::getSkuId, SkuStockVo::getHasStock));

        } catch (Exception e) {
            log.error("库存系统查询失败，原因：{}", e.getMessage());
        }

        Map<Long, Boolean> finalSkuStockMap = skuStockMap;

        List<SkuEsModel> upSkuModels = skus.stream().map(sku -> {
            SkuEsModel skuEsModel = new SkuEsModel();
            BeanUtils.copyProperties(sku, skuEsModel);
            skuEsModel.setSkuPrice(sku.getPrice());
            skuEsModel.setSkuImg(sku.getSkuDefaultImg());
            // 向库存系统发送远程调用，查询 sku 是否有库存
            if (finalSkuStockMap == null) {
                skuEsModel.setHasStock(true);
            }
            skuEsModel.setHasStock(finalSkuStockMap.get(sku.getSkuId()));
            skuEsModel.setHotScore(0L);

            // 查询品牌信息
            BrandEntity brand = brandService.getById(skuEsModel.getBrandId());
            skuEsModel.setBrandImg(brand.getLogo());
            skuEsModel.setBrandName(brand.getName());
            // 查询分类信息
            CategoryEntity category = categoryService.getById(skuEsModel.getCatalogId());
            skuEsModel.setCatalogName(category.getName());
            // 查询规格属性信息
            List<SkuEsModel.Attrs> attrsList = collect.stream().map(item -> {
                SkuEsModel.Attrs attrs = new SkuEsModel.Attrs();
                BeanUtils.copyProperties(item, attrs);
                return attrs;
            }).collect(Collectors.toList());
            skuEsModel.setAttrs(attrsList);
            return skuEsModel;
        }).collect(Collectors.toList());

        // skuEsModels 提交给 search 模块
        R res = new R();
        try {
            res = searchFeignService.productUp(upSkuModels);
        } catch (Exception e) {
            log.error("商品上架失败，原因：{}", e.getMessage());
        }
        // 更改商品状态
        if (res.getCode() == 0) {
            baseMapper.updateSpuStatus(spuId, ProductConstant.StatusType.SPU_UP.getCode());
        }
    }
}