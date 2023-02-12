package com.pfl.ssfmall.ware.controller;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import com.pfl.ssfmall.ware.vo.PurchaseDoneVo;
import com.pfl.ssfmall.ware.vo.PurchaseMergeVo;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.pfl.ssfmall.ware.entity.PurchaseEntity;
import com.pfl.ssfmall.ware.service.PurchaseService;
import com.pfl.common.utils.PageUtils;
import com.pfl.common.utils.R;


/**
 * 采购信息
 *
 * @author ssf
 * @email ${email}
 * @date 2022-06-02 11:22:38
 */
@RestController
@RequestMapping("ware/purchase")
public class PurchaseController {
    @Autowired
    private PurchaseService purchaseService;


    /**
     * 完成采购
     * @return
     */
    // /ware/purchase/done
    @PostMapping("/done")
    public R done(@RequestBody PurchaseDoneVo vo) {
        purchaseService.done(vo);
        return R.ok();
    }


    /**
     * 领取采购单
     *
     * @param ids 采购单id
     * @return
     */
    @PostMapping("/received")
    public R received(@RequestBody List<Long> ids) {
        purchaseService.received(ids);
        return R.ok();
    }

    /**
     * 合并采购需求 到采购单
     *
     * @return
     */
    // /ware/purchase/merge
    @PostMapping("/merge")
    public R mergePurchase(@RequestBody PurchaseMergeVo purchaseMergeVo) {
        purchaseService.mergePurchase(purchaseMergeVo);
        return R.ok();
    }

    /**
     * 查询未领取的采购单
     *
     * @return
     */
    // /ware/purchase/unreceive/list
    @GetMapping("unreceive/list")
    public R getUnreceiveList(@RequestParam Map<String, Object> params) {
        PageUtils page = purchaseService.queryPageWithUnreceiveList(params);
        return R.ok().put("page", page);
    }


    /**
     * 列表
     */
    @RequestMapping("/list")
    //@RequiresPermissions("ware:purchase:list")
    public R list(@RequestParam Map<String, Object> params) {
        PageUtils page = purchaseService.queryPage(params);

        return R.ok().put("page", page);
    }


    /**
     * 信息
     */
    @RequestMapping("/info/{id}")
    //@RequiresPermissions("ware:purchase:info")
    public R info(@PathVariable("id") Long id) {
        PurchaseEntity purchase = purchaseService.getById(id);

        return R.ok().put("purchase", purchase);
    }

    /**
     * 保存
     */
    @RequestMapping("/save")
    //@RequiresPermissions("ware:purchase:save")
    public R save(@RequestBody PurchaseEntity purchase) {
        purchaseService.save(purchase);

        return R.ok();
    }

    /**
     * 修改
     */
    @RequestMapping("/update")
    //@RequiresPermissions("ware:purchase:update")
    public R update(@RequestBody PurchaseEntity purchase) {
        purchaseService.updateById(purchase);

        return R.ok();
    }

    /**
     * 删除
     */
    @RequestMapping("/delete")
    //  @RequiresPermissions("ware:purchase:delete")
    public R delete(@RequestBody Long[] ids) {
        purchaseService.removeByIds(Arrays.asList(ids));

        return R.ok();
    }

}
