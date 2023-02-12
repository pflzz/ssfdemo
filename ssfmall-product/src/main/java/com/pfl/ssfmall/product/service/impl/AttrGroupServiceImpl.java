package com.pfl.ssfmall.product.service.impl;

import com.pfl.common.constant.ProductConstant;
import com.pfl.ssfmall.product.dao.AttrAttrgroupRelationDao;
import com.pfl.ssfmall.product.dao.AttrDao;
import com.pfl.ssfmall.product.dao.AttrGroupDao;
import com.pfl.ssfmall.product.entity.AttrAttrgroupRelationEntity;
import com.pfl.ssfmall.product.entity.AttrEntity;
import com.pfl.ssfmall.product.entity.AttrGroupEntity;
import com.pfl.ssfmall.product.service.AttrGroupService;
import com.pfl.ssfmall.product.service.AttrService;
import com.pfl.ssfmall.product.vo.AttrGroupWithAttrsVo;
import com.pfl.ssfmall.product.vo.SpuItemGroupAttrVo;
import com.sun.javafx.scene.shape.PathUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.pfl.common.utils.PageUtils;
import com.pfl.common.utils.Query;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;


@Service("attrGroupService")
public class AttrGroupServiceImpl extends ServiceImpl<AttrGroupDao, AttrGroupEntity> implements AttrGroupService {

    @Resource
    private AttrAttrgroupRelationDao attrAttrgroupRelationDao;
    @Resource
    private AttrDao attrDao;
    @Resource
    private AttrGroupDao attrGroupDao;
    @Resource
    private AttrService attrService;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<AttrGroupEntity> page = this.page(
                new Query<AttrGroupEntity>().getPage(params),
                new QueryWrapper<AttrGroupEntity>()
        );

        return new PageUtils(page);
    }

    /**
     * 根据 三级分类 id 获取对应属性分组信息
     *
     * @param params    分页格式信息
     * @param catelogId 三级分类 id
     * @return
     */
    @Override
    public PageUtils queryCategoryPage(Map<String, Object> params, Long catelogId) {
        // 如果在 params 中 携带了 key 参数，则进行模糊查询
        QueryWrapper<AttrGroupEntity> wrapper = new QueryWrapper<>();
        String key = (String) params.get("key");
        if (!StringUtils.isEmpty(key)) {
            wrapper.and((obj) -> {
                obj.eq("attr_group_id", key).or().like("attr_group_name", key);
            });
        }
        // 如果不携带三级分类 id 则查询所有
        if (catelogId == 0) {
            IPage<AttrGroupEntity> page = this.page(
                    new Query<AttrGroupEntity>().getPage(params),
                    wrapper
            );
            return new PageUtils(page);
        } else {
            wrapper.eq("catelog_id", catelogId);
            IPage<AttrGroupEntity> page = this.page(
                    new Query<AttrGroupEntity>().getPage(params),
                    wrapper
            );
            return new PageUtils(page);
        }

    }

    /**
     * 获取分类下所有分组&关联属性
     *
     * @param catelogId 分类 id
     * @return
     */
    @Override
    public List<AttrGroupWithAttrsVo> getAttrGroupWithAttrs(Long catelogId) {

        // 查询该分类下的所有属性分组
        List<AttrGroupEntity> attrGroupEntities = this.list(new QueryWrapper<AttrGroupEntity>()
                .eq("catelog_id", catelogId));
        List<AttrGroupWithAttrsVo> collect = attrGroupEntities.stream().map(item -> {
            AttrGroupWithAttrsVo vo = new AttrGroupWithAttrsVo();
            BeanUtils.copyProperties(item, vo);
            Long attrGroupId = item.getAttrGroupId();
            List<AttrEntity> attr = attrService.getAttrRelation(attrGroupId);
            vo.setAttrs(attr);
            return vo;
        }).collect(Collectors.toList());
        return collect;
    }

    /**
     * 根据分类 id 和 spuId 获取该分类（spu）属性分组及其属性值列表
     * @param catalogId
     * @param spuId
     * @return
     */
    @Override
    public List<SpuItemGroupAttrVo> getAttrGroupWithAttr(Long catalogId, Long spuId) {
        List<SpuItemGroupAttrVo> spuItemGroupAttrVos = attrDao.getAttrGroupWithAttr(catalogId, spuId);
        return spuItemGroupAttrVos;
    }

}