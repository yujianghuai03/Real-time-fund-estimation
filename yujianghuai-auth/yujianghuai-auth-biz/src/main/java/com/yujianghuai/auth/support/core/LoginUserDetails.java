package com.yujianghuai.auth.support.core;

import com.yujianghuai.common.utils.CurrentUserInfo;
import java.util.Collection;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;

/**
 * 登录用户信息，额外携带可写入令牌的基础用户资料。
 *
 * @author yxh
 * @date 2026/5/8
 */
public class LoginUserDetails extends User {

    private final CurrentUserInfo currentUserInfo;

    public LoginUserDetails(String username,
                            String password,
                            boolean enabled,
                            Collection<? extends GrantedAuthority> authorities,
                            CurrentUserInfo currentUserInfo) {
        super(username, password, enabled, true, true, true, authorities);
        this.currentUserInfo = currentUserInfo;
    }

    public CurrentUserInfo getCurrentUserInfo() {
        return currentUserInfo;
    }
}
