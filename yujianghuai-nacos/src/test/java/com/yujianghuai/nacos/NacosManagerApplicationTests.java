package com.yujianghuai.nacos;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(properties = {
        "nacos.manager.home=",
        "nacos.manager.auto-start=true"
})
class NacosManagerApplicationTests {

    @Test
    void contextLoadsWhenNacosHomeMissing() {
    }
}
