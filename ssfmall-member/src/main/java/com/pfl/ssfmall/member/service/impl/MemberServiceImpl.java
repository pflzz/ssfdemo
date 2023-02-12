package com.pfl.ssfmall.member.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import com.pfl.common.utils.HttpUtils;
import com.pfl.ssfmall.member.entity.MemberLevelEntity;
import com.pfl.ssfmall.member.entity.MemberReceiveAddressEntity;
import com.pfl.ssfmall.member.exception.PhoneExistException;
import com.pfl.ssfmall.member.exception.UserNameExistException;
import com.pfl.ssfmall.member.service.MemberLevelService;
import com.pfl.ssfmall.member.vo.MemberLoginVo;
import com.pfl.ssfmall.member.vo.MemberRegisterVo;
import com.pfl.ssfmall.member.vo.SocialUser;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.util.EntityUtils;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.pfl.common.utils.PageUtils;
import com.pfl.common.utils.Query;

import com.pfl.ssfmall.member.dao.MemberDao;
import com.pfl.ssfmall.member.entity.MemberEntity;
import com.pfl.ssfmall.member.service.MemberService;

import javax.annotation.Resource;


@Service("memberService")
public class MemberServiceImpl extends ServiceImpl<MemberDao, MemberEntity> implements MemberService {

    @Resource
    private MemberLevelService memberLevelService;


    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<MemberEntity> page = this.page(
                new Query<MemberEntity>().getPage(params),
                new QueryWrapper<MemberEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    public void register(MemberRegisterVo vo) {

        MemberEntity memberEntity = new MemberEntity();
        // 设置默认会员等级
        MemberLevelEntity defaultStatus = memberLevelService.getOne(new QueryWrapper<MemberLevelEntity>().
                eq("default_status", 1));
        memberEntity.setLevelId(defaultStatus.getId());
        // 检查用户名和手机号的唯一性 让 controller 去感知异常
        checkPhoneUnique(vo.getPhone());
        checkUsernameUnique(vo.getUsername());

        memberEntity.setMobile(vo.getPhone());
        memberEntity.setUsername(vo.getUsername());

        // 密码进行加密存储
        BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
        String encode = passwordEncoder.encode(vo.getPassword());
        memberEntity.setPassword(encode);
        this.save(memberEntity);
    }

    /**
     * 检查手机号是否被注册
     *
     * @param phone
     */
    @Override
    public void checkPhoneUnique(String phone) throws PhoneExistException {
        MemberDao baseMapper = this.baseMapper;
        Long count = baseMapper.selectCount(new QueryWrapper<MemberEntity>().eq("mobile", phone));
        if (count > 0) {
            throw new PhoneExistException();
        }

    }

    /**
     * 检查用户名是否被注册
     *
     * @param username
     */
    @Override
    public void checkUsernameUnique(String username) throws UserNameExistException {
        MemberDao baseMapper = this.baseMapper;
        Long count = baseMapper.selectCount(new QueryWrapper<MemberEntity>().eq("username", username));
        if (count > 0) {
            throw new UserNameExistException();
        }

    }

    @Override
    public MemberEntity login(MemberLoginVo vo) {
        String password = vo.getPassword();
        String account = vo.getUserAccount();

        // 判断账号是否存在
        MemberEntity member = this.getOne(new QueryWrapper<MemberEntity>().eq("mobile", account).or().eq("username", account));
        if (member == null) {
            return null;
        } else {
            // 判断密码是否正确
            BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
            boolean matches = passwordEncoder.matches(password, member.getPassword());
            if (matches) {
                return member;
            } else {
                return null;
            }
        }
    }

    @Override
    public MemberEntity socialLogin(SocialUser user) throws Exception {
        String uid = user.getUid();
        String accessToken = user.getAccess_token();
        Long expiresIn = user.getExpires_in();

        // 根据 uid 判断是否有这个用户
        MemberEntity memberEntity = this.getOne(new QueryWrapper<MemberEntity>().eq("uid", uid));
        if (memberEntity != null) {
            // 如果有这个用户则进行更新 访问令牌
            MemberEntity update = new MemberEntity();
            update.setAccessToken(accessToken);
            update.setId(memberEntity.getId());
            update.setExpiresIn(expiresIn);

            this.updateById(update);

            memberEntity.setAccessToken(accessToken);
            memberEntity.setExpiresIn(user.getExpires_in());
            return memberEntity;
        } else {
            // 如果没有这个用户则注册
            MemberEntity member = new MemberEntity();

            // 如果远程请求基本信息失败，也不能影响整个业务逻辑
            try {
                Map<String, String> query = new HashMap<>();
                query.put("access_token", accessToken);
                query.put("uid", uid);
                // 给微博服务器发送请求 获取用户的社交账号哦的基本信息
                HttpResponse response = HttpUtils.doGet("https://api.weibo.com", "/2/users/show.json", "get", new HashMap<>(), query);
                if (response.getStatusLine().getStatusCode() == 200) {
                    String json = EntityUtils.toString(response.getEntity());
                    JSONObject jsonObject = JSON.parseObject(json);
                    String name = jsonObject.getString("name");
                    String gender = jsonObject.getString("gender");

                    member.setGender("m".equals(gender) ? 1 : 0);
                    member.setNickname(name);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            member.setUid(uid);
            member.setAccessToken(accessToken);
            member.setExpiresIn(expiresIn);
            this.save(member);
            return member;
        }






    }


}