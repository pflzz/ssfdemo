package com.pfl.ssfmall.product.controller;

import java.util.Arrays;
import java.util.Map;

import com.pfl.common.group.AddGroup;
import com.pfl.common.valid.UpdateStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.pfl.ssfmall.product.entity.BrandEntity;
import com.pfl.ssfmall.product.service.BrandService;
import com.pfl.common.utils.PageUtils;
import com.pfl.common.utils.R;


/**
 * 品牌
 *
 * @author ssf
 * @email ${email}
 * @date 2022-06-01 18:18:46
 */
@RestController
@RequestMapping("product/brand")
public class BrandController {
    @Autowired
    private BrandService brandService;

    /**
     * 列表
     */
    @RequestMapping("/list")
    //@RequiresPermissions("product:brand:list")
    public R list(@RequestParam Map<String, Object> params){
        PageUtils page = brandService.queryPage(params);

        return R.ok().put("page", page);
    }


    /**
     * 信息
     */
    @RequestMapping("/info/{brandId}")
    //@RequiresPermissions("product:brand:info")
    public R info(@PathVariable("brandId") Long brandId){
		BrandEntity brand = brandService.getById(brandId);

        return R.ok().put("brand", brand);
    }

    /**
     * 保存
     */
    @RequestMapping("/save")
    //@RequiresPermissions("product:brand:save")
    public R save(@Validated(value = {AddGroup.class}) @RequestBody BrandEntity brand /*, BindingResult result */){
//        if (result.hasErrors()) {
//            Map<String, String> map = new HashMap<>();
//            // 1. 获取校验的错误结果
//            result.getFieldErrors().forEach((item) -> {
//                // 获取错误的属性名称
//                String field = item.getField();
//                // 获取错误提示
//                String defaultMessage = item.getDefaultMessage();
//                map.put(field, defaultMessage);
//            });
//            return R.error(400, "提交的数据不合法").put("data", map);
//        }

		brandService.save(brand);
        return R.ok();
    }

    /**
     * 修改状态
     */
    @RequestMapping("/update/status")
    //@RequiresPermissions("product:brand:update")
    public R updateStatus(@Validated(value = {UpdateStatus.class}) @RequestBody BrandEntity brand){
		brandService.updateById(brand);

        return R.ok();
    }

    /**
     * 修改
     * @param brand
     * @return
     */
    @RequestMapping("/update")
    public R update(@Validated(value = {UpdateStatus.class}) @RequestBody BrandEntity brand){
        brandService.updateCascade(brand);
        return R.ok();
    }

    /**
     * 删除
     */
    @RequestMapping("/delete")
  //  @RequiresPermissions("product:brand:delete")
    public R delete(@RequestBody Long[] brandIds){
		brandService.removeByIds(Arrays.asList(brandIds));

        return R.ok();
    }

}
