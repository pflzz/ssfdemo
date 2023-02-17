package com.pfl.common.exception;

/**
 * 统一定义错误状态码
 */
public enum BizCodeEnum {
    UNKNOW_EXCEPTION(10000, "系统未知异常"),
    VALID_EXCEPTION(10001, "参数格式校验失败"),
    SMS_CODE_EXCEPTION(10002, "验证码请求过于频繁，请稍后再试"),
    PRODUCT_UP_EXCEPTION(11000, "商品上架异常"),
    USER_EXIST_EXCEPTION(15001, "该用户名已被注册"),
    PHONE_EXIST_EXCEPTION(15002, "该手机号已被注册"),
    ACCOUNT_PASSWORD_VALID_EXCEPTION(15003, "账号密码错误"),
    WEIBO_AUTH_EXCEPTION(15004, "微博授权失败"),
    WARE_NOSTOCK_EXCEPTION(21001, "库存不足");


    private Integer code;
    private String message;

    BizCodeEnum(Integer code, String message) {
        this.code = code;
        this.message = message;
    }

    public Integer getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }
}
