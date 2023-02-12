package com.pfl.ssfmall.member.controller;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import com.alibaba.fastjson.TypeReference;
import com.pfl.common.exception.BizCodeEnum;
import com.pfl.ssfmall.member.entity.MemberReceiveAddressEntity;
import com.pfl.ssfmall.member.exception.PhoneExistException;
import com.pfl.ssfmall.member.exception.UserNameExistException;
import com.pfl.ssfmall.member.feign.CouponFeignService;
import com.pfl.ssfmall.member.vo.MemberLoginVo;
import com.pfl.ssfmall.member.vo.MemberRegisterVo;
import com.pfl.ssfmall.member.vo.SocialUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.pfl.ssfmall.member.entity.MemberEntity;
import com.pfl.ssfmall.member.service.MemberService;
import com.pfl.common.utils.PageUtils;
import com.pfl.common.utils.R;

import javax.annotation.Resource;


/**
 * 会员
 *
 * @author ssf
 * @email ${email}
 * @date 2022-06-02 10:56:36
 */
@RestController
    @RequestMapping("member/member")
public class MemberController {
    @Autowired
    private MemberService memberService;


    @Resource
    private CouponFeignService couponFeignService;

    @GetMapping("/testFeign")
    public R testFeign() {
        MemberEntity entity = new MemberEntity();
        entity.setNickname("uzi");
        R memberCoupons = couponFeignService.memberCoupons();
        return R.ok().put("member", entity).put("coupons", memberCoupons.get("coupons"));
    }

    @PostMapping("/socialLogin")
    public R socialLogin(@RequestBody SocialUser user) throws Exception {
        MemberEntity entity = memberService.socialLogin(user);
        return R.ok().put("data", entity);
    }

    @PostMapping("/login")
    public R login(@RequestBody MemberLoginVo vo) {

        MemberEntity member = memberService.login(vo);
        if (member == null) {
            return R.error(BizCodeEnum.ACCOUNT_PASSWORD_VALID_EXCEPTION.getCode(), BizCodeEnum.ACCOUNT_PASSWORD_VALID_EXCEPTION.getMessage());
        } else {
            return R.ok();
        }

    }

    /**
     * 会员注册
     *
     * @param vo
     * @return
     */
    @PostMapping("/register")
    public R register(@RequestBody MemberRegisterVo vo) {
        try {
            memberService.register(vo);
        } catch (PhoneExistException e) {
            return R.error(BizCodeEnum.PHONE_EXIST_EXCEPTION.getCode(), BizCodeEnum.PHONE_EXIST_EXCEPTION.getMessage());
        } catch (UserNameExistException e) {
            return R.error(BizCodeEnum.USER_EXIST_EXCEPTION.getCode(), BizCodeEnum.USER_EXIST_EXCEPTION.getMessage());
        }

        return R.ok();
    }


    /**
     * 列表
     */
    @RequestMapping("/list")
    //@RequiresPermissions("member:member:list")
    public R list(@RequestParam Map<String, Object> params) {
        PageUtils page = memberService.queryPage(params);

        return R.ok().put("page", page);
    }


    /**
     * 信息
     */
    @RequestMapping("/info/{id}")
    //@RequiresPermissions("member:member:info")
    public R info(@PathVariable("id") Long id) {
        MemberEntity member = memberService.getById(id);

        return R.ok().put("member", member);
    }

    /**
     * 保存
     */
    @RequestMapping("/save")
    //@RequiresPermissions("member:member:save")
    public R save(@RequestBody MemberEntity member) {
        memberService.save(member);

        return R.ok();
    }

    /**
     * 修改
     */
    @RequestMapping("/update")
    //@RequiresPermissions("member:member:update")
    public R update(@RequestBody MemberEntity member) {
        memberService.updateById(member);

        return R.ok();
    }

    /**
     * 删除
     */
    @RequestMapping("/delete")
    //  @RequiresPermissions("member:member:delete")
    public R delete(@RequestBody Long[] ids) {
        memberService.removeByIds(Arrays.asList(ids));

        return R.ok();
    }

}
