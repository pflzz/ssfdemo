package com.pfl.ssfmall.coupon.controller;

import java.util.Arrays;
import java.util.Map;

import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.web.bind.annotation.*;

import com.pfl.ssfmall.coupon.entity.CouponEntity;
import com.pfl.ssfmall.coupon.service.CouponService;
import com.pfl.common.utils.PageUtils;
import com.pfl.common.utils.R;



/**
 * 优惠券信息
 *
 * @author ssf
 * @email ${email}
 * @date 2022-06-01 23:03:49
 */
@RefreshScope
@RestController
@RequestMapping("/coupon/coupon")
public class CouponController {
    @Autowired
    private CouponService couponService;


//    @Value("${coupon.user.name}")
//    String userName;
//
//    @Value("${coupon.user.age}")
//    int age;
//
//    @GetMapping("/test/config")
//    public R testConfig() {
//        return R.ok().put("username", userName).put("age", age);
//    }


    @GetMapping("/member/list")
    public R memberCoupons() {
        CouponEntity couponEntity = new CouponEntity();
        couponEntity.setCouponName("满10减1");
        return R.ok().put("coupons", Arrays.asList(couponEntity));
    }




    /**
     * 列表
     */
    @RequestMapping("/list")
    //@RequiresPermissions("coupon:coupon:list")
    public R list(@RequestParam Map<String, Object> params){
        PageUtils page = couponService.queryPage(params);

        return R.ok().put("page", page);
    }


    /**
     * 信息
     */
    @RequestMapping("/info/{id}")
    //@RequiresPermissions("coupon:coupon:info")
    public R info(@PathVariable("id") Long id){
		CouponEntity coupon = couponService.getById(id);

        return R.ok().put("coupon", coupon);
    }

    /**
     * 保存
     */
    @RequestMapping("/save")
    //@RequiresPermissions("coupon:coupon:save")
    public R save(@RequestBody CouponEntity coupon){
		couponService.save(coupon);

        return R.ok();
    }

    /**
     * 修改
     */
    @RequestMapping("/update")
    //@RequiresPermissions("coupon:coupon:update")
    public R update(@RequestBody CouponEntity coupon){
		couponService.updateById(coupon);

        return R.ok();
    }

    /**
     * 删除
     */
    @RequestMapping("/delete")
  //  @RequiresPermissions("coupon:coupon:delete")
    public R delete(@RequestBody Long[] ids){
		couponService.removeByIds(Arrays.asList(ids));

        return R.ok();
    }

}
