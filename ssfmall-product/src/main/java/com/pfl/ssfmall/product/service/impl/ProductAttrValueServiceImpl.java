package com.pfl.ssfmall.product.service.impl;

import com.pfl.ssfmall.product.dao.ProductAttrValueDao;
import com.pfl.ssfmall.product.entity.ProductAttrValueEntity;
import com.pfl.ssfmall.product.service.ProductAttrValueService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.pfl.common.utils.PageUtils;
import com.pfl.common.utils.Query;
import org.springframework.transaction.annotation.Transactional;


@Service("productAttrValueService")
public class ProductAttrValueServiceImpl extends ServiceImpl<ProductAttrValueDao, ProductAttrValueEntity> implements ProductAttrValueService {

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<ProductAttrValueEntity> page = this.page(
                new Query<ProductAttrValueEntity>().getPage(params),
                new QueryWrapper<ProductAttrValueEntity>()
        );

        return new PageUtils(page);
    }

    /**
     * 获取spu规格
     * @param spuId
     * @return
     */
    @Override
    public List<ProductAttrValueEntity> baseListForSpu(Long spuId) {
        List<ProductAttrValueEntity> productAttrValueEntities = this.baseMapper.selectList(new QueryWrapper<ProductAttrValueEntity>().eq("spu_id", spuId));
        return productAttrValueEntities;
    }
    /**
     * 根据 spuId 修改商品规格
     * @param spuId
     * @param list
     * @return
     */
    @Transactional
    @Override
    public void updateBySpuId(Long spuId, List<ProductAttrValueEntity> list) {
        // 删除这个商品之前的所有属性
        this.baseMapper.delete(new QueryWrapper<ProductAttrValueEntity>().eq("spu_id", spuId));

        // 给传来的属性赋上 这个商品的 spId
        List<ProductAttrValueEntity> collect = list.stream().map(item -> {
            item.setSpuId(spuId);
            return item;
        }).collect(Collectors.toList());
        this.saveBatch(collect);
    }

}