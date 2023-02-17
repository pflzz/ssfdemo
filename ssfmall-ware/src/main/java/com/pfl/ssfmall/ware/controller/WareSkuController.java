package com.pfl.ssfmall.ware.controller;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import com.pfl.common.exception.BizCodeEnum;
import com.pfl.ssfmall.ware.exception.NoStockException;
import com.pfl.ssfmall.ware.model.dto.WareSkuLockedTo;
import com.pfl.ssfmall.ware.model.vo.SkuStockVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.pfl.ssfmall.ware.model.entity.WareSkuEntity;
import com.pfl.ssfmall.ware.service.WareSkuService;
import com.pfl.common.utils.PageUtils;
import com.pfl.common.utils.R;



/**
 * 商品库存
 *
 * @author ssf
 * @email ${email}
 * @date 2022-06-02 11:22:38
 */
@RestController
@RequestMapping("/ware/waresku")
public class WareSkuController {
    @Autowired
    private WareSkuService wareSkuService;

    @PostMapping("/orderLockStock")
    public R orderLockStock(@RequestBody WareSkuLockedTo wareSkuLockedTo) {
        try {
            Boolean res = wareSkuService.lockStock(wareSkuLockedTo);
            return R.ok();
        } catch (NoStockException e) {
            return R.error(BizCodeEnum.WARE_NOSTOCK_EXCEPTION.getCode(), BizCodeEnum.WARE_NOSTOCK_EXCEPTION.getMessage());
        }


    }


    /**
     * 检索该商品是否有库存
     */
    @PostMapping("/hasStock")
    public R hasStock(@RequestBody List<Long> skuIds) {
        List<SkuStockVo> list = wareSkuService.hasStock(skuIds);

       return R.ok().setData(list);
    }


    /**
     * 列表
     */
    @RequestMapping("/list")
    //@RequiresPermissions("ware:waresku:list")
    public R list(@RequestParam Map<String, Object> params){
        PageUtils page = wareSkuService.queryPage(params);

        return R.ok().put("page", page);
    }


    /**
     * 信息
     */
    @RequestMapping("/info/{id}")
    //@RequiresPermissions("ware:waresku:info")
    public R info(@PathVariable("id") Long id){
		WareSkuEntity wareSku = wareSkuService.getById(id);

        return R.ok().put("wareSku", wareSku);
    }

    /**
     * 保存
     */
    @RequestMapping("/save")
    //@RequiresPermissions("ware:waresku:save")
    public R save(@RequestBody WareSkuEntity wareSku){
		wareSkuService.save(wareSku);

        return R.ok();
    }

    /**
     * 修改
     */
    @RequestMapping("/update")
    //@RequiresPermissions("ware:waresku:update")
    public R update(@RequestBody WareSkuEntity wareSku){
		wareSkuService.updateById(wareSku);

        return R.ok();
    }

    /**
     * 删除
     */
    @RequestMapping("/delete")
  //  @RequiresPermissions("ware:waresku:delete")
    public R delete(@RequestBody Long[] ids){
		wareSkuService.removeByIds(Arrays.asList(ids));

        return R.ok();
    }

}
