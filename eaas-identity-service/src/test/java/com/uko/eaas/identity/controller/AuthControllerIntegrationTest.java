package com.uko.eaas.identity.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.uko.eaas.identity.dto.CustomerRegisterRequest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc
class AuthControllerIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void register_Success() throws Exception {
        CustomerRegisterRequest request = customer("test-integration@example.com", "Integration Test User");

        mockMvc.perform(post("/api/v1/auth/register/customer")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.email").value(request.getEmail()));
    }

    @Test
    void register_EmailAlreadyExists() throws Exception {
        CustomerRegisterRequest request = customer("duplicate@example.com", "Duplicate User");

        mockMvc.perform(post("/api/v1/auth/register/customer")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/v1/auth/register/customer")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict());
    }

    @Test
    void login_Success() throws Exception {
        CustomerRegisterRequest registerRequest = customer("login-test@example.com", "Login Test User");

        mockMvc.perform(post("/api/v1/auth/register/customer")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{" +
                                "\"email\":\"login-test@example.com\"," +
                                "\"password\":\"TestPassword123!\"" +
                                "}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.accessToken").exists())
                .andExpect(jsonPath("$.data.user.email").value(registerRequest.getEmail()));
    }

    @Test
    void login_InvalidCredentials() throws Exception {
        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{" +
                                "\"email\":\"nonexistent@example.com\"," +
                                "\"password\":\"wrongpassword\"" +
                                "}"))
                .andExpect(status().isUnauthorized());
    }

    private static CustomerRegisterRequest customer(String email, String fullName) {
        CustomerRegisterRequest request = new CustomerRegisterRequest();
        request.setEmail(email);
        request.setPassword("TestPassword123!");
        request.setFullName(fullName);
        request.setPhone("+2348012345678");
        request.setTermsAccepted(true);
        request.setDataProcessingConsent(true);
        return request;
    }
}
