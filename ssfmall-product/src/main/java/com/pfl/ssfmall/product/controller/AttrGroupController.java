package com.pfl.ssfmall.product.controller;

import java.util.Arrays;
import java.util.List;
import java.util.Map;


import com.pfl.ssfmall.product.entity.AttrAttrgroupRelationEntity;
import com.pfl.ssfmall.product.entity.AttrEntity;
import com.pfl.ssfmall.product.service.AttrAttrgroupRelationService;
import com.pfl.ssfmall.product.service.AttrService;
import com.pfl.ssfmall.product.service.CategoryService;
import com.pfl.ssfmall.product.vo.AttrGroupWithAttrsVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.pfl.ssfmall.product.entity.AttrGroupEntity;
import com.pfl.ssfmall.product.service.AttrGroupService;
import com.pfl.common.utils.PageUtils;
import com.pfl.common.utils.R;

import javax.annotation.Resource;


/**
 * 属性分组
 *
 * @author ssf
 * @email ${email}
 * @date 2022-06-01 18:18:46
 */
@RestController
@RequestMapping("product/attrgroup")
public class AttrGroupController {
    @Autowired
    private AttrGroupService attrGroupService;
    @Resource
    private CategoryService categoryService;
    @Resource
    private AttrService attrService;
    @Resource
    private AttrAttrgroupRelationService attrAttrgroupRelationService;


    /**
     * 获取分类下所有分组&关联属性
     * @param catelogId 分类 id
     * @return
     */
    // /product/attrgroup/{catelogId}/withattr
    @GetMapping("/{catelogId}/withattr")
    public R getAttrGroupWithAttrs(@PathVariable("catelogId") Long catelogId) {

        List<AttrGroupWithAttrsVo> vos = attrGroupService.getAttrGroupWithAttrs(catelogId);
        return R.ok().put("data", vos);
    }


    // /product/attrgroup/attr/relation
    @PostMapping("/attr/relation")
    public R addRelation(@RequestBody List<AttrAttrgroupRelationEntity> entities) {
        attrAttrgroupRelationService.saveBatch(entities);
        return R.ok();
    }


    // /product/attrgroup/attr/relation/delete
    @PostMapping("/attr/relation/delete")
    public R relationDelete(@RequestBody List<AttrAttrgroupRelationEntity> attrAttrgroupRelationEntity) {
        attrAttrgroupRelationService.relationDelete(attrAttrgroupRelationEntity);
        return R.ok();
    }



    /**
     * 获取属性分组里面 还没有 关联的本分类里面的其他基本属性，方便添加新的关联
     * @param params 分页数据
     * @param attrgroupId 分组id
     * @return
     */
    // /product/attrgroup/{attrgroupId}/noattr/relation
    @GetMapping("/{attrgroupId}/noattr/relation")
    public R noattrrelation(@RequestParam Map<String, Object> params, @PathVariable("attrgroupId") Long attrgroupId) {
        PageUtils attrEntities = attrService.getNoattrRelation(params, attrgroupId);
        return R.ok().put("page", attrEntities);
    }

    /**
     * 获取属性分组的关联的所有属性
     * @param attrgroupId 获取该分组的所有属性
     * @return
     */
    // /product/attrgroup/{attrgroupId}/attr/relation
    @GetMapping("/{attrgroupId}/attr/relation")
    public R attrRelation(@PathVariable("attrgroupId") Long attrgroupId) {

        List<AttrEntity> attrEntities = attrService.getAttrRelation(attrgroupId);
        return R.ok().put("data", attrEntities);
    }


    /**
     * 根据 三级分类 id 获取对应属性分组信息
     * @param params 分页格式信息
     * @param catelogId 三级分类 id
     * @return
     */
    @RequestMapping("/list/{catelogId}")
    //@RequiresPermissions("product:attrgroup:list")
    public R list(@RequestParam Map<String, Object> params, @PathVariable("catelogId") Long catelogId){
        PageUtils page = attrGroupService.queryCategoryPage(params, catelogId);
        return R.ok().put("page", page);
    }


    /**
     * 返回分类详情（包含分类完整的父路径信息）
     */
    @RequestMapping("/info/{attrGroupId}")
    //@RequiresPermissions("product:attrgroup:info")
    public R info(@PathVariable("attrGroupId") Long attrGroupId){
		AttrGroupEntity attrGroup = attrGroupService.getById(attrGroupId);
        // 获取分类完整的父路径信息
        Long[] catePath = categoryService.getFullPath(attrGroup.getCatelogId());
        attrGroup.setCatelogPath(catePath);
        return R.ok().put("attrGroup", attrGroup);
    }

    /**
     * 保存
     */
    @RequestMapping("/save")
    //@RequiresPermissions("product:attrgroup:save")
    public R save(@RequestBody AttrGroupEntity attrGroup){
		attrGroupService.save(attrGroup);

        return R.ok();
    }

    /**
     * 修改
     */
    @RequestMapping("/update")
    //@RequiresPermissions("product:attrgroup:update")
    public R update(@RequestBody AttrGroupEntity attrGroup){
		attrGroupService.updateById(attrGroup);

        return R.ok();
    }

    /**
     * 删除
     */
    @RequestMapping("/delete")
  //  @RequiresPermissions("product:attrgroup:delete")
    public R delete(@RequestBody Long[] attrGroupIds){
		attrGroupService.removeByIds(Arrays.asList(attrGroupIds));

        return R.ok();
    }

}
