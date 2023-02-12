package com.pfl.ssfmall.member.exception;

public class PhoneExistException extends RuntimeException{
    public PhoneExistException() {
        super("手机号已被注册");
    }
}
