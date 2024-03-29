package com.pfl.ssfmall.product.service.impl;

import com.pfl.ssfmall.product.dao.AttrAttrgroupRelationDao;
import com.pfl.ssfmall.product.entity.AttrAttrgroupRelationEntity;
import com.pfl.ssfmall.product.service.AttrAttrgroupRelationService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.pfl.common.utils.PageUtils;
import com.pfl.common.utils.Query;


@Service("attrAttrgroupRelationService")
public class AttrAttrgroupRelationServiceImpl extends ServiceImpl<AttrAttrgroupRelationDao, AttrAttrgroupRelationEntity> implements AttrAttrgroupRelationService {

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<AttrAttrgroupRelationEntity> page = this.page(
                new Query<AttrAttrgroupRelationEntity>().getPage(params),
                new QueryWrapper<AttrAttrgroupRelationEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    public void relationDelete(List<AttrAttrgroupRelationEntity> attrGroupRelationEntity) {
        this.baseMapper.deleteBatch(attrGroupRelationEntity);
    }

}