package com.pfl.ssfmall.product.service.impl;

import com.pfl.ssfmall.product.dao.SkuSaleAttrValueDao;
import com.pfl.ssfmall.product.entity.SkuSaleAttrValueEntity;
import com.pfl.ssfmall.product.service.SkuSaleAttrValueService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.pfl.common.utils.PageUtils;
import com.pfl.common.utils.Query;


@Service("skuSaleAttrValueService")
public class SkuSaleAttrValueServiceImpl extends ServiceImpl<SkuSaleAttrValueDao, SkuSaleAttrValueEntity> implements SkuSaleAttrValueService {

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<SkuSaleAttrValueEntity> page = this.page(
                new Query<SkuSaleAttrValueEntity>().getPage(params),
                new QueryWrapper<SkuSaleAttrValueEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    public List<String> getSaleAttrNameWithValuesAsList(Long skuId) {

        SkuSaleAttrValueDao baseMapper = this.baseMapper;
        List<String> res = baseMapper.getSaleAttrNameWithValuesAsList(skuId);
        return res;
    }

}