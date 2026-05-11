package com.yujianghuai.auth.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author yxh
 * @date 2026/5/11 14:59
 */
@ConfigurationProperties(prefix = "app.security.permit-all")
public class SecurityPermitAllProperties {
    /**
     * 不区分请求方法的放行路径。
     */
    private List<String> paths = new ArrayList<>();

    /**
     * 按请求方法区分的放行路径。
     * 例如 OPTIONS -> /**
     */
    private Map<String, List<String>> methods = new HashMap<>();

    public List<String> getPaths() {
        return paths;
    }

    public void setPaths(List<String> paths) {
        this.paths = paths;
    }

    public Map<String, List<String>> getMethods() {
        return methods;
    }

    public void setMethods(Map<String, List<String>> methods) {
        this.methods = methods;
    }
}