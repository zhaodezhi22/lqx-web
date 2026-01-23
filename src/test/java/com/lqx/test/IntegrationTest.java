package com.lqx.test;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.liuqin.opera.LiuqinOperaApplication;
import com.liuqin.opera.common.dto.LoginRequest;
import com.liuqin.opera.common.dto.RegisterRequest;
import com.liuqin.opera.entity.PerformanceEvent;
import com.liuqin.opera.service.PerformanceEventService;
import com.liuqin.opera.utils.JwtUtils;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(classes = LiuqinOperaApplication.class)
@AutoConfigureMockMvc
@TestPropertySource(properties = {
        "spring.datasource.url=jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1;MODE=MySQL",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.datasource.username=SA",
        "spring.datasource.password=",
        "spring.sql.init.mode=always",
        "jwt.secret=liuqinoperasecretkeyshouldbelongenough123456",
        "mybatis-plus.configuration.map-underscore-to-camel-case=true"
})
class IntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private PerformanceEventService performanceEventService;

    @Autowired
    private JwtUtils jwtUtils;

    @Test
    void testRegister() throws Exception {
        RegisterRequest req = new RegisterRequest();
        req.setUsername("testuser_" + System.currentTimeMillis());
        req.setPassword("password");
        req.setRole(0);

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    void testPerformanceEvent() throws Exception {
        // Create an event first
        PerformanceEvent event = new PerformanceEvent();
        event.setTitle("Test Opera");
        event.setVenue("Grand Theater");
        event.setShowTime(LocalDateTime.now().plusDays(1));
        event.setTicketPrice(new BigDecimal("100.00"));
        event.setTotalSeats(500);
        event.setStatus(1);
        performanceEventService.save(event);
        Long eventId = event.getEventId();

        // First register and login to get token
        String username = "testuser_event_" + System.currentTimeMillis();
        RegisterRequest registerReq = new RegisterRequest();
        registerReq.setUsername(username);
        registerReq.setPassword("password");
        registerReq.setRole(0);
        
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerReq)))
                .andExpect(status().isOk());

        LoginRequest loginReq = new LoginRequest();
        loginReq.setUsername(username);
        loginReq.setPassword("password");

        String loginResp = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginReq)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        JsonNode root = objectMapper.readTree(loginResp);
        String token = root.path("data").path("token").asText();

        // Now use token to access performance event
        mockMvc.perform(get("/api/events/" + eventId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }
}
