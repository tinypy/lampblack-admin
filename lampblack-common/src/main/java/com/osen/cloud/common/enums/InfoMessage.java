package com.osen.cloud.common.enums;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

/**
 * User: PangYi
 * Date: 2019-08-28
 * Time: 17:46
 * Description: 统一信息体
 */
@NoArgsConstructor
@AllArgsConstructor
public enum InfoMessage {

    /**
     * 异常信息体
     */
    UnknownSystem_Error(4001, "系统未知异常"),
    NoFound_Error(40004,"页面不存在"),

    /**
     * 成功信息体
     */
    Success_OK(2001, "成功请求"),
    Refresh_OK(2002,"令牌刷新成功"),

    /**
     * 提示信息体
     */
    User_Need_Authorization(3001, "用户未登录"),
    User_Login_Failed(3002,"账号或密码错误"),
    User_Login_Success(3003,"登录成功"),
    User_NO_Access(3004,"用户无权访问"),
    User_Logout_Success(3005,"用户已成功退出登录"),
    User_Logout_Failed(3006,"退出登录异常"),
    User_Login_Guoqi(3007,"登录状态过期");

    private Integer code;

    private String message;

    public Integer getCode() {
        return code;
    }

    public void setCode(Integer code) {
        this.code = code;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
