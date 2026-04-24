package com.yujianghuai.nacos.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import com.yujianghuai.nacos.lifecycle.NacosProcessManager;
import java.util.Map;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/nacos")
@Tag(name = "Nacos 管理", description = "Nacos 进程管理接口")
public class NacosManagerController {

    private final NacosProcessManager processManager;

    public NacosManagerController(NacosProcessManager processManager) {
        this.processManager = processManager;
    }

    /**
     * 查询 Nacos 进程状态。
     */
    @GetMapping("/status")
    @Operation(summary = "查询 Nacos 状态", description = "返回当前 Nacos 进程状态和最近一次错误信息")
    public Map<String, Object> status() {
        return Map.of(
                "code", 200,
                "message", "success",
                "data", Map.of(
                        "status", processManager.status(),
                        "lastError", processManager.lastError() == null ? "" : processManager.lastError()
                )
        );
    }

    /**
     * 启动 Nacos。
     */
    @PostMapping("/start")
    @Operation(summary = "启动 Nacos", description = "启动本地 Nacos 进程并返回最新状态")
    public Map<String, Object> start() {
        processManager.startNacos();
        return status();
    }

    /**
     * 停止 Nacos。
     */
    @PostMapping("/stop")
    @Operation(summary = "停止 Nacos", description = "停止本地 Nacos 进程并返回最新状态")
    public Map<String, Object> stop() {
        processManager.stopNacos();
        return status();
    }
}
