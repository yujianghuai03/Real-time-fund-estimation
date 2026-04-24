package com.yujianghuai.biz.demo.service;

import com.yujianghuai.common.mq.MqProducer;
import com.yujianghuai.common.tenant.TenantContext;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

@Service
public class DemoService {

    private final RedisTemplate<String, Object> redisTemplate;
    private final MqProducer mqProducer;

    public DemoService(RedisTemplate<String, Object> redisTemplate, MqProducer mqProducer) {
        this.redisTemplate = redisTemplate;
        this.mqProducer = mqProducer;
    }

    public Map<String, Object> scaffoldInfo() {
        String tenantId = TenantContext.getRequiredTenantId();
        String cacheKey = "demo:tenant:" + tenantId;
        redisTemplate.opsForValue().set(cacheKey, LocalDateTime.now().toString());
        mqProducer.send("tenant=" + tenantId + " triggered demo event");

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("tenantId", tenantId);
        data.put("redisKey", cacheKey);
        data.put("message", "backend scaffold is ready");
        return data;
    }
}
