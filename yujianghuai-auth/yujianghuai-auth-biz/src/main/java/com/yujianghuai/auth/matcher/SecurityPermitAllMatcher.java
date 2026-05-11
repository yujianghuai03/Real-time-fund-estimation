package com.yujianghuai.auth.matcher;

import com.yujianghuai.auth.config.SecurityPermitAllProperties;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.util.AntPathMatcher;

import java.util.Collections;
import java.util.List;


/**
 * 安全放行路径匹配器。
 *
 * <p>
 * 用于统一判断当前请求是否属于 permit-all 白名单路径。
 * 命中白名单的请求会跳过部分安全校验逻辑，例如 token Redis 校验过滤器。
 * </p>
 *
 * <p>
 * 支持两类白名单配置：
 * 1. 不区分请求方式的通用放行路径；
 * 2. 按 GET、POST、PUT、DELETE 等请求方式单独配置的放行路径。
 * </p>
 * @author yxh
 * @date 2026/5/11 15:19
 */
public class SecurityPermitAllMatcher {

    /**
     * permit-all 白名单配置属性。
     *
     * <p>
     * 从配置文件中读取通用放行路径和按请求方式区分的放行路径。
     * </p>
     */
    private final SecurityPermitAllProperties properties;

    /**
     * Spring 提供的 Ant 风格路径匹配器。
     *
     * <p>
     * 支持 /oauth2/**、/admin-api/code/* 等 Ant 风格路径匹配规则。
     * </p>
     */
    private final AntPathMatcher antPathMatcher = new AntPathMatcher();

    /**
     * 构造安全放行路径匹配器。
     *
     * @param properties permit-all 白名单配置属性
     */
    public SecurityPermitAllMatcher(SecurityPermitAllProperties properties) {
        this.properties = properties;
    }

    /**
     * 判断当前请求是否命中放行白名单。
     *
     * <p>
     * 匹配顺序：
     * 1. 先匹配不区分请求方式的通用放行路径；
     * 2. 如果未命中，再根据当前请求方式匹配对应方法下的放行路径。
     * </p>
     *
     * @param request 当前 HTTP 请求
     * @return true 表示命中白名单，可以放行；false 表示未命中，需要继续执行安全校验
     */
    public boolean matches(HttpServletRequest request) {
        String requestUri = request.getRequestURI();
        String method = request.getMethod();

        // 优先匹配通用放行路径，不区分 GET、POST 等请求方式
        if (matches(properties.getPaths(), requestUri)) {
            return true;
        }

        // 获取当前请求方式对应的放行路径，例如 GET、POST、PUT、DELETE
        List<String> methodPaths = properties.getMethods()
                .getOrDefault(method.toUpperCase(), Collections.emptyList());

        // 匹配当前请求方式下配置的放行路径
        return matches(methodPaths, requestUri);
    }

    /**
     * 判断请求 URI 是否匹配指定的路径规则列表。
     *
     * <p>
     * 任意一个路径规则匹配成功，即认为当前请求命中白名单。
     * </p>
     *
     * @param patterns 白名单路径规则列表
     * @param requestUri 当前请求 URI
     * @return true 表示匹配成功，false 表示未匹配
     */
    private boolean matches(List<String> patterns, String requestUri) {
        return patterns != null && patterns.stream()
                .anyMatch(pattern -> antPathMatcher.match(pattern, requestUri));
    }
}