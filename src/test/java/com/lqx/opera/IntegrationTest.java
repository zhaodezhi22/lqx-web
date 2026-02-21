package com.lqx.opera;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lqx.opera.common.dto.CreateMallOrderItem;
import com.lqx.opera.common.dto.CreateMallOrderRequest;
import com.lqx.opera.common.dto.LoginRequest;
import com.lqx.opera.common.dto.RegisterRequest;
import com.lqx.opera.entity.MallOrder;
import com.lqx.opera.entity.PerformanceEvent;
import com.lqx.opera.entity.Product;
import com.lqx.opera.service.MallOrderService;
import com.lqx.opera.service.PerformanceEventService;
import com.lqx.opera.service.ProductService;
import com.lqx.opera.utils.JwtUtils;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
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
    private ProductService productService;

    @Autowired
    private MallOrderService mallOrderService;

    @Autowired
    private JwtUtils jwtUtils;

    private String registerAndLogin(String username, int role) throws Exception {
        RegisterRequest registerReq = new RegisterRequest();
        registerReq.setUsername(username);
        registerReq.setPassword("password");
        registerReq.setRole(role);
        
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
        return root.path("data").path("token").asText();
    }

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

        String token = registerAndLogin("testuser_event_" + System.currentTimeMillis(), 0);

        // Now use token to access performance event
        mockMvc.perform(get("/api/events/" + eventId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    void testMallOrderFlow() throws Exception {
        // 1. Prepare Product
        Product product = new Product();
        product.setName("Test Product");
        product.setPrice(new BigDecimal("50.00"));
        product.setStock(10);
        product.setStatus(1); // On sale
        productService.save(product);
        Long productId = product.getProductId();

        // 2. User Register & Login
        String userToken = registerAndLogin("customer_" + System.currentTimeMillis(), 0);
        
        // 3. Admin Login (Role 2)
        String adminToken = registerAndLogin("admin_" + System.currentTimeMillis(), 2);

        // 4. User Create Order
        CreateMallOrderRequest createReq = new CreateMallOrderRequest();
        CreateMallOrderItem item = new CreateMallOrderItem();
        item.setProductId(productId);
        item.setQuantity(2);
        createReq.setItems(Collections.singletonList(item));
        createReq.setAddress("Test Address");

        String createResp = mockMvc.perform(post("/api/mall/order/create")
                        .header("Authorization", "Bearer " + userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andReturn().getResponse().getContentAsString();
        
        JsonNode createRoot = objectMapper.readTree(createResp);
        String orderNo = createRoot.path("data").asText();
        
        // Get Order ID
        MallOrder order = mallOrderService.lambdaQuery().eq(MallOrder::getOrderNo, orderNo).one();
        Long orderId = order.getId();

        // Verify Stock Deducted
        Product pAfterOrder = productService.getById(productId);
        assertEquals(8, pAfterOrder.getStock());

        // 5. Ship Order (Expect Fail - Order not Paid)
        mockMvc.perform(post("/api/mall/ship/" + orderId)
                        .header("Authorization", "Bearer " + adminToken)
                        .param("deliveryCompany", "SF")
                        .param("deliveryNo", "SF123456"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(500)); // Should fail

        // 6. Simulate Pay (Update status to 1)
        order.setStatus(1);
        mallOrderService.updateById(order);

        // 7. Ship Order (Success)
        mockMvc.perform(post("/api/mall/ship/" + orderId)
                        .header("Authorization", "Bearer " + adminToken)
                        .param("deliveryCompany", "SF")
                        .param("deliveryNo", "SF123456"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        // Verify Status 2 and Delivery Info
        order = mallOrderService.getById(orderId);
        assertEquals(2, order.getStatus());
        assertEquals("SF", order.getDeliveryCompany());
        assertEquals("SF123456", order.getDeliveryNo());

        // 8. Admin Confirm Refund (Refund Logic Test)
        // Reset to Paid (1) or Refund Pending (4) to test refund
        order.setStatus(4);
        mallOrderService.updateById(order);

        mockMvc.perform(post("/api/mall/refund/confirm/" + orderId)
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        // Verify Status 3 and Stock Rollback
        order = mallOrderService.getById(orderId);
        assertEquals(3, order.getStatus());
        
        Product pAfterRefund = productService.getById(productId);
        assertEquals(10, pAfterRefund.getStock()); // Should be back to 10
    }
}
