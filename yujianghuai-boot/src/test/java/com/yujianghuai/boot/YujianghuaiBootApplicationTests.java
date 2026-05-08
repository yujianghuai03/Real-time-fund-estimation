package com.yujianghuai.boot;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class YujianghuaiBootApplicationTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void contextLoads() {
    }

    @Test
    void oauth2TokenEndpointShouldWork() throws Exception {
        MvcResult tokenResult = mockMvc.perform(post("/oauth2/token")
                        .contentType("application/x-www-form-urlencoded")
                        .param("grant_type", "password")
                        .param("client_id", "yujianghuai-client")
                        .param("client_secret", "yujianghuai-secret")
                        .param("username", "admin")
                        .param("password", "123456")
                        .param("scope", "email")
                        .param("TENANT-ID", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.access_token").isNotEmpty())
                .andExpect(jsonPath("$.refresh_token").isNotEmpty())
                .andReturn();

        JsonNode tokenPayload = objectMapper.readTree(tokenResult.getResponse().getContentAsString());
        String refreshToken = tokenPayload.get("refresh_token").asText();

        mockMvc.perform(post("/oauth2/token")
                        .contentType("application/x-www-form-urlencoded")
                        .param("grant_type", "refresh_token")
                        .param("client_id", "yujianghuai-client")
                        .param("client_secret", "yujianghuai-secret")
                        .param("refresh_token", refreshToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.access_token").isNotEmpty());
    }
}
