package com.yujianghuai.api.demo.controller;

import com.yujianghuai.biz.demo.service.DemoService;
import com.yujianghuai.common.web.R;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.Map;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/demo")
@Tag(name = "演示接口", description = "演示与健康检查相关接口")
public class DemoController {

    private final DemoService demoService;

    public DemoController(DemoService demoService) {
        this.demoService = demoService;
    }

    /**
     * 演示联通性检测。
     */
    @GetMapping("/ping")
    @Operation(summary = "演示联通性检测", description = "返回脚手架基础信息用于联通性测试")
    public R<Map<String, Object>> ping() {
        return R.ok(demoService.scaffoldInfo());
    }
}
