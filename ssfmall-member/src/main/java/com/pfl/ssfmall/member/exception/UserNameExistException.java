package com.pfl.ssfmall.member.exception;

public class UserNameExistException extends RuntimeException{
    public UserNameExistException() {
        super("用户名已被注册");
    }
}
