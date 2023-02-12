package com.pfl.ssfmall.product.controller;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import com.pfl.ssfmall.product.entity.ProductAttrValueEntity;
import com.pfl.ssfmall.product.service.ProductAttrValueService;
import com.pfl.ssfmall.product.vo.AttrResponseVo;
import com.pfl.ssfmall.product.vo.AttrVo;
import org.springframework.web.bind.annotation.*;

import com.pfl.ssfmall.product.service.AttrService;
import com.pfl.common.utils.PageUtils;
import com.pfl.common.utils.R;

import javax.annotation.Resource;


/**
 * 商品属性
 *
 * @author ssf
 * @email ${email}
 * @date 2022-06-01 18:18:46
 */
@RestController
@RequestMapping("product/attr")
public class AttrController {
    @Resource
    private AttrService attrService;
    @Resource
    private ProductAttrValueService productAttrValueService;


    /**
     * 根据 spuId 修改商品规格
     * @param spuId
     * @return
     */
    // /product/attr/update/{spuId}
    @PostMapping("/update/{spuId}")
    public R updateBySpuId(@PathVariable("spuId") Long spuId, List<ProductAttrValueEntity> list) {
        productAttrValueService.updateBySpuId(spuId, list);
        return R.ok();
    }

    /**
     * 获取spu规格
     * @param spuId
     * @return
     */
    // /product/attr/base/listforspu/{spuId}
    @GetMapping("/base/listforspu/{spuId}")
    public R baseListForSpu(@PathVariable("spuId") Long spuId) {
        List<ProductAttrValueEntity> list = productAttrValueService.baseListForSpu(spuId);
        return R.ok().put("data", list);
    }


    /**
     * 列表
     */
    @RequestMapping("/list")
    //@RequiresPermissions("product:attr:list")
    public R list(@RequestParam Map<String, Object> params){
        PageUtils page = attrService.queryPage(params);
        return R.ok().put("page", page);
    }

    /**
     * 获取分类规格参数
     * @param params 分页参数
     * @param catelogId 分类id
     * @return
     */
    @GetMapping("/{attrType}/list/{catelogId}")
    public R baseAttrList(@RequestParam Map<String, Object> params, @PathVariable("catelogId") Long catelogId,
                          @PathVariable("attrType") String attrType) {
        PageUtils page = attrService.getBaseAttrList(params, catelogId, attrType);
        return R.ok().put("page", page);
    }

    /**
     * 查询属性详情
     */
    @RequestMapping("/info/{attrId}")
    //@RequiresPermissions("product:attr:info")
    public R info(@PathVariable("attrId") Long attrId){
		AttrResponseVo attr = attrService.getAttrDetails(attrId);

        return R.ok().put("attr", attr);
    }

    /**
     * 保存
     */
    @RequestMapping("/save")
    //@RequiresPermissions("product:attr:save")
    public R save(@RequestBody AttrVo attrVo){
        // 保存属性和属性分组之间的关联信息
		attrService.saveAttrVo(attrVo);

        return R.ok();
    }

    /**
     * 修改
     */
    @RequestMapping("/update")
    //@RequiresPermissions("product:attr:update")
    public R update(@RequestBody AttrVo attr){
		attrService.updateAttr(attr);

        return R.ok();
    }

    /**
     * 删除
     */
    @RequestMapping("/delete")
  //  @RequiresPermissions("product:attr:delete")
    public R delete(@RequestBody Long[] attrIds){
		attrService.removeByIds(Arrays.asList(attrIds));

        return R.ok();
    }

}
