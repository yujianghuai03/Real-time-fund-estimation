package com.yujianghuai.common.utils;

import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.core.AbstractOAuth2Token;
import org.springframework.security.oauth2.core.ClaimAccessor;
import org.springframework.security.oauth2.core.OAuth2AuthenticatedPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.util.StringUtils;

/**
 * Spring Security 登录上下文工具类。
 *
 * @author yxh
 * @date 2026/5/8
 */
public final class SecurityUtils {

    private static final String CLAIM_USERNAME = "username";

    private static final String CLAIM_TENANT_ID = "tenant_id";

    private static final String CLAIM_CURRENT_USER = "current_user";

    private static final String CLAIM_ROLE_IDS = "role_ids";

    private SecurityUtils() {
    }

    public static Authentication getAuthentication() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return isLoginAuthentication(authentication) ? authentication : null;
    }

    public static String getToken() {
        return getToken(getAuthentication());
    }

    public static String getToken(Authentication authentication) {
        if (!isLoginAuthentication(authentication)) {
            return null;
        }
        Object credentials = authentication.getCredentials();
        if (credentials instanceof AbstractOAuth2Token token) {
            return token.getTokenValue();
        }
        if (credentials instanceof String token && StringUtils.hasText(token)) {
            return token;
        }
        Object principal = authentication.getPrincipal();
        if (principal instanceof Jwt jwt) {
            return jwt.getTokenValue();
        }
        return null;
    }

    public static CurrentUserInfo getCurrentUserInfo() {
        return getCurrentUserInfo(getAuthentication());
    }

    public static CurrentUserInfo getCurrentUserInfo(Authentication authentication) {
        if (!isLoginAuthentication(authentication)) {
            return null;
        }
        CurrentUserInfo currentUserInfo = getCurrentUserInfoClaim(authentication);
        if (currentUserInfo != null) {
            return currentUserInfo;
        }
        CurrentUserInfo fallback = new CurrentUserInfo();
        fallback.setUsername(getUsername(authentication));
        fallback.setTenant(toStringValue(getClaim(authentication, CLAIM_TENANT_ID)));
        return fallback;
    }

    public static Set<Long> getRoleIds() {
        return getRoleIds(getAuthentication());
    }

    public static Set<Long> getRoleIds(Authentication authentication) {
        if (!isLoginAuthentication(authentication)) {
            return Set.of();
        }
        return toLongSet(getClaim(authentication, CLAIM_ROLE_IDS));
    }

    private static boolean isLoginAuthentication(Authentication authentication) {
        return authentication != null
                && authentication.isAuthenticated()
                && !(authentication instanceof AnonymousAuthenticationToken);
    }

    private static String getUsername(Authentication authentication) {
        Object principal = authentication.getPrincipal();
        if (principal instanceof UserDetails userDetails) {
            return userDetails.getUsername();
        }
        Object username = getClaim(authentication, CLAIM_USERNAME);
        if (username != null && StringUtils.hasText(username.toString())) {
            return username.toString();
        }
        if (principal instanceof Jwt jwt && StringUtils.hasText(jwt.getSubject())) {
            return jwt.getSubject();
        }
        String name = authentication.getName();
        return StringUtils.hasText(name) ? name : null;
    }

    private static String getTenantId(Authentication authentication) {
        Object tenantId = getClaim(authentication, CLAIM_TENANT_ID);
        return tenantId == null || !StringUtils.hasText(tenantId.toString()) ? null : tenantId.toString();
    }

    private static Object getClaim(Authentication authentication, String claimName) {
        Object principal = authentication.getPrincipal();
        if (principal instanceof ClaimAccessor claimAccessor) {
            return claimAccessor.getClaim(claimName);
        }
        if (principal instanceof OAuth2AuthenticatedPrincipal authenticatedPrincipal) {
            return authenticatedPrincipal.getAttribute(claimName);
        }
        return null;
    }

    private static CurrentUserInfo getCurrentUserInfoClaim(Authentication authentication) {
        Object value = getClaim(authentication, CLAIM_CURRENT_USER);
        if (value instanceof CurrentUserInfo currentUserInfo) {
            return currentUserInfo;
        }
        if (value instanceof Map<?, ?> map) {
            CurrentUserInfo currentUserInfo = new CurrentUserInfo();
            currentUserInfo.setId(toLongValue(map.get("id")));
            currentUserInfo.setUsername(toStringValue(map.get("username")));
            currentUserInfo.setNickname(toStringValue(map.get("nickname")));
            currentUserInfo.setRealName(toStringValue(map.get("realName")));
            currentUserInfo.setTenant(toStringValue(map.get("tenant")));
            return currentUserInfo;
        }
        return null;
    }

    private static Set<Long> toLongSet(Object value) {
        if (value == null) {
            return Set.of();
        }
        Set<Long> roleIds = new LinkedHashSet<>();
        if (value instanceof Collection<?> collection) {
            collection.forEach(item -> addLong(roleIds, item));
            return Set.copyOf(roleIds);
        }
        if (value.getClass().isArray()) {
            int length = Array.getLength(value);
            for (int i = 0; i < length; i++) {
                addLong(roleIds, Array.get(value, i));
            }
            return Set.copyOf(roleIds);
        }
        if (value instanceof String text) {
            Arrays.stream(text.split(","))
                    .map(String::trim)
                    .forEach(item -> addLong(roleIds, item));
            return Set.copyOf(roleIds);
        }
        addLong(roleIds, value);
        return Set.copyOf(roleIds);
    }

    private static void addLong(Set<Long> target, Object value) {
        if (value == null) {
            return;
        }
        if (value instanceof Number number) {
            target.add(number.longValue());
            return;
        }
        String text = value.toString();
        if (!StringUtils.hasText(text)) {
            return;
        }
        try {
            target.add(Long.valueOf(text));
        } catch (NumberFormatException ignored) {
            // 非数字角色ID直接忽略，避免无效 claim 影响业务读取当前用户。
        }
    }

    private static Long toLongValue(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof Number number) {
            return number.longValue();
        }
        String text = value.toString();
        if (!StringUtils.hasText(text)) {
            return null;
        }
        try {
            return Long.valueOf(text);
        } catch (NumberFormatException ignored) {
            return null;
        }
    }

    private static String toStringValue(Object value) {
        return value == null || !StringUtils.hasText(value.toString()) ? null : value.toString();
    }
}
