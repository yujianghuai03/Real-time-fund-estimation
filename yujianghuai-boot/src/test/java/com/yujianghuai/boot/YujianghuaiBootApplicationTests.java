package com.yujianghuai.boot;

import com.yujianghuai.auth.model.LoginRequest;
import com.yujianghuai.auth.service.AuthService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class YujianghuaiBootApplicationTests {

    @Autowired
    private AuthService authService;

    @Autowired
    private MockMvc mockMvc;

    @Test
    void contextLoads() {
    }

    @Test
    void loginShouldWork() {
        LoginRequest request = new LoginRequest();
        request.setTenantId("1");
        request.setUsername("admin");
        request.setPassword("123456");
        authService.login(request);
    }

    @Test
    void authLoginEndpointShouldWork() throws Exception {
        mockMvc.perform(post("/auth/login")
                        .contentType("application/json")
                        .header("TENANT-ID", "1")
                        .content("{\"tenantId\":\"1\",\"username\":\"admin\",\"password\":\"123456\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.accessToken").isNotEmpty());
    }

    @Test
    void oauth2TokenEndpointShouldWork() throws Exception {
        mockMvc.perform(post("/oauth2/token")
                        .contentType("application/x-www-form-urlencoded")
                        .param("grant_type", "password")
                        .param("username", "admin")
                        .param("password", "123456")
                        .param("scope", "openid profile api.read api.write")
                        .param("TENANT-ID", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.access_token").isNotEmpty());
    }
}
