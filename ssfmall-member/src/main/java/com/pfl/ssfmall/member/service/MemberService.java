package com.pfl.ssfmall.member.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.pfl.common.utils.PageUtils;
import com.pfl.ssfmall.member.entity.MemberEntity;
import com.pfl.ssfmall.member.entity.MemberReceiveAddressEntity;
import com.pfl.ssfmall.member.exception.PhoneExistException;
import com.pfl.ssfmall.member.exception.UserNameExistException;
import com.pfl.ssfmall.member.vo.MemberLoginVo;
import com.pfl.ssfmall.member.vo.MemberRegisterVo;
import com.pfl.ssfmall.member.vo.SocialUser;

import java.util.List;
import java.util.Map;

/**
 * 会员
 *
 * @author ssf
 * @email ${email}
 * @date 2022-06-02 10:56:36
 */
public interface MemberService extends IService<MemberEntity> {

    PageUtils queryPage(Map<String, Object> params);

    void register(MemberRegisterVo vo);

    void checkPhoneUnique(String phone) throws PhoneExistException;

    void checkUsernameUnique(String username) throws UserNameExistException;

    MemberEntity login(MemberLoginVo vo);

    MemberEntity socialLogin(SocialUser user) throws Exception;


}

