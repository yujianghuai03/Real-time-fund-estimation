package com.yujianghuai.common.constant;

/**
 * @author yxh
 * @date 2026/5/8 10:03
 */

public interface SecurityConstants {

    String AUTH_UNAUTHORIZED_MESSAGE = "权限不足，请登录后再试！";

    String AUTH_ACCESS_DENIED_MESSAGE = "无权限访问该接口";

    String AUTH_INVALID_CLIENT_MESSAGE = "客户端认证失败";

    String AUTH_UNAUTHORIZED_CLIENT_MESSAGE = "客户端不支持当前授权方式";

    String AUTH_INVALID_REQUEST_MESSAGE = "认证请求参数不合法";

    String AUTH_INVALID_SCOPE_MESSAGE = "授权范围不合法";

    String AUTH_INVALID_GRANT_MESSAGE = "登录凭证无效";

    String AUTH_BAD_CREDENTIALS_MESSAGE = "用户名或密码错误";

    String AUTH_USER_NOT_FOUND_MESSAGE = "用户不存在或已被禁用";

    String AUTH_NO_LOGIN_PERMISSION_MESSAGE = "没有登录权限";

    String AUTH_TOKEN_INVALID_MESSAGE = "登录状态无效，请重新登录";

    String AUTH_TOKEN_EXPIRED_MESSAGE = "登录状态已过期，请重新登录";

    String AUTH_EMAIL_CODE_REQUIRED_MESSAGE = "邮箱或验证码不能为空";

    String AUTH_EMAIL_CODE_INVALID_MESSAGE = "邮箱验证码错误或已过期";
}
