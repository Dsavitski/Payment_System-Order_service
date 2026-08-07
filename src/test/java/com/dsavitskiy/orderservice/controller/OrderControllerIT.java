package com.dsavitskiy.orderservice.controller;

import com.dsavitskiy.orderservice.AbstractIntegrationTest;
import com.dsavitskiy.orderservice.dto.OrderResponseDto;
import com.dsavitskiy.orderservice.entity.Item;
import com.dsavitskiy.orderservice.repository.ItemRepository;
import com.dsavitskiy.orderservice.repository.OrderRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.WireMockServer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;

import static com.github.tomakehurst.wiremock.client.WireMock.okJson;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class OrderControllerIT extends AbstractIntegrationTest {

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ItemRepository itemRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private WireMockServer wireMockServer;

    private UUID userId;

    @BeforeEach
    void setUp() {

        orderRepository.deleteAll();
        itemRepository.deleteAll();

        wireMockServer.resetAll();

        userId = UUID.randomUUID();

        stubUser();
    }

    private void stubUser() {

        wireMockServer.stubFor(
            com.github.tomakehurst.wiremock.client.WireMock.get(urlEqualTo("/api/users/" + userId))
                .willReturn(okJson("""
                {
                  "id":"%s",
                  "firstName":"John",
                  "lastName":"Doe",
                  "email":"john@test.com",
                  "phoneNumber":"123456789",
                  "deleted":false
                }
                """.formatted(userId)))
        );

        wireMockServer.stubFor(
            com.github.tomakehurst.wiremock.client.WireMock.get(urlEqualTo("/api/users/email/john@test.com"))
                .willReturn(okJson("""
                {
                  "id":"%s",
                  "firstName":"John",
                  "lastName":"Doe",
                  "email":"john@test.com",
                  "phoneNumber":"123456789",
                  "deleted":false
                }
                """.formatted(userId)))
        );
    }

    private SecurityMockMvcRequestPostProcessors.JwtRequestPostProcessor userJwt() {

        return jwt()
            .jwt(jwt -> jwt
                .subject(userId.toString())
                .claim("sub", userId.toString())
                .claim("realm_access",
                    Map.of("roles", List.of("USER"))))
            .authorities(new SimpleGrantedAuthority("ROLE_USER"));
    }

    private SecurityMockMvcRequestPostProcessors.JwtRequestPostProcessor adminJwt() {

        return jwt()
            .jwt(jwt -> jwt
                .subject(userId.toString())
                .claim("sub", userId.toString())
                .claim("realm_access",
                    Map.of("roles", List.of("ADMIN"))))
            .authorities(new SimpleGrantedAuthority("ROLE_ADMIN"));
    }

    private SecurityMockMvcRequestPostProcessors.JwtRequestPostProcessor userJwt(UUID anotherUser) {

        return jwt()
            .jwt(jwt -> jwt
                .subject(anotherUser.toString())
                .claim("sub", anotherUser.toString())
                .claim("realm_access",
                    Map.of("roles", List.of("USER"))))
            .authorities(new SimpleGrantedAuthority("ROLE_USER"));
    }

    private Item createItem() {

        Item item = new Item();

        item.setName("Phone");
        item.setPrice(BigDecimal.valueOf(1000));

        return itemRepository.save(item);
    }

    private String createOrderJson(Long itemId) {

        return """
        {
          "items": [
            {
              "itemId": %d,
              "quantity": 2
            }
          ]
        }
        """.formatted(itemId);
    }

    private OrderResponseDto createOrder(Item item) throws Exception {

        var mvcResult = mockMvc.perform(
                post("/api/orders")
                    .with(userJwt())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(createOrderJson(item.getId())))
            .andExpect(status().isCreated())
            .andReturn();

        return objectMapper.readValue(
            mvcResult.getResponse().getContentAsString(),
            OrderResponseDto.class
        );
    }

    @Test
    void shouldCreateOrder() throws Exception {

        Item item = createItem();

        OrderResponseDto response = createOrder(item);

        assertThat(response.order().id()).isNotNull();
        assertThat(response.order().userId()).isEqualTo(userId);
        assertThat(response.order().status()).isEqualTo("PENDING");
        assertThat(response.order().totalPrice())
            .isEqualByComparingTo("2000.00");

        assertThat(orderRepository.count()).isEqualTo(1);
    }

    @Test
    void shouldGetOrderById() throws Exception {

        Item item = createItem();

        OrderResponseDto created = createOrder(item);

        var mvcResult = mockMvc.perform(
                get("/api/orders/{id}", created.order().id())
                    .with(userJwt()))
            .andExpect(status().isOk())
            .andReturn();

        OrderResponseDto response =
            objectMapper.readValue(
                mvcResult.getResponse().getContentAsString(),
                OrderResponseDto.class);

        assertThat(response.order().id())
            .isEqualTo(created.order().id());

        assertThat(response.order().userId())
            .isEqualTo(userId);

        assertThat(response.order().status())
            .isEqualTo("PENDING");

        assertThat(response.order().totalPrice())
            .isEqualByComparingTo("2000.00");
    }

    @Test
    void shouldUpdateOrder() throws Exception {

        Item item = createItem();

        OrderResponseDto created = createOrder(item);

        String json = """
        {
          "items":[
            {
              "itemId": %d,
              "quantity": 5
            }
          ]
        }
        """.formatted(item.getId());

        var mvcResult = mockMvc.perform(
                put("/api/orders/{id}", created.order().id())
                    .with(userJwt())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(json))
            .andExpect(status().isOk())
            .andReturn();

        OrderResponseDto updated =
            objectMapper.readValue(
                mvcResult.getResponse().getContentAsString(),
                OrderResponseDto.class);

        assertThat(updated.order().totalPrice())
            .isEqualByComparingTo("5000.00");
    }

    @Test
    void shouldDeleteOrder() throws Exception {

        Item item = createItem();

        OrderResponseDto created = createOrder(item);

        mockMvc.perform(
                delete("/api/orders/{id}", created.order().id())
                    .with(userJwt()))
            .andExpect(status().isNoContent());

        assertThat(
            orderRepository.findById(created.order().id())
                .orElseThrow()
                .isDeleted())
            .isTrue();
    }

    @Test
    void shouldGetOrders() throws Exception {

        Item item = createItem();

        createOrder(item);

        var mvcResult = mockMvc.perform(
                get("/api/orders")
                    .with(adminJwt()))
            .andExpect(status().isOk())
            .andReturn();

        String response = mvcResult.getResponse().getContentAsString();

        assertThat(response).contains("PENDING");
        assertThat(response).contains(userId.toString());
    }

    @Test
    void shouldGetOrdersByUserId() throws Exception {

        Item item = createItem();

        createOrder(item);

        var mvcResult = mockMvc.perform(
                get("/api/orders/user/{userId}", userId)
                    .with(userJwt()))
            .andExpect(status().isOk())
            .andReturn();

        String response = mvcResult.getResponse().getContentAsString();

        assertThat(response).contains(userId.toString());
    }

    @Test
    void shouldGetOrdersByEmail() throws Exception {

        UUID ivanId = UUID.randomUUID();

        wireMockServer.stubFor(
            com.github.tomakehurst.wiremock.client.WireMock.get(urlEqualTo("/api/users/email/ivan@test.com"))
                .willReturn(okJson("""
                {
                  "id":"%s",
                  "firstName":"Ivan",
                  "lastName":"Ivanov",
                  "email":"ivan@test.com",
                  "phoneNumber":"987654321",
                  "deleted":false
                }
                """.formatted(ivanId)))
        );

        Item item = createItem();

        mockMvc.perform(
                post("/api/orders")
                    .with(userJwt(ivanId))
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(createOrderJson(item.getId())))
            .andExpect(status().isCreated());

        var mvcResult = mockMvc.perform(
                get("/api/orders/user/email/ivan@test.com")
                    .with(adminJwt()))
            .andExpect(status().isOk())
            .andReturn();

        String response = mvcResult.getResponse().getContentAsString();

        assertThat(response).contains(ivanId.toString());
    }

    @Test
    void shouldReturnNotFoundWhenOrderDoesNotExist() throws Exception {

        mockMvc.perform(
                get("/api/orders/{id}", 999999L)
                    .with(userJwt()))
            .andExpect(status().isNotFound());
    }

    @Test
    void shouldReturnForbiddenWhenUserRequestsAllOrders() throws Exception {

        mockMvc.perform(
                get("/api/orders")
                    .with(userJwt()))
            .andExpect(status().isForbidden());
    }
}