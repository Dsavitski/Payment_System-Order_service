package com.dsavitskiy.orderservice.controller;

import com.dsavitskiy.orderservice.AbstractIntegrationTest;
import com.dsavitskiy.orderservice.dto.ItemDisplayDto;
import com.dsavitskiy.orderservice.entity.Item;
import com.dsavitskiy.orderservice.repository.ItemRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.http.MediaType;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class ItemControllerIT extends AbstractIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ItemRepository itemRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    void cleanDatabase() {
        itemRepository.deleteAll();
    }

    private SecurityMockMvcRequestPostProcessors.JwtRequestPostProcessor adminJwt() {

        return jwt()
            .jwt(jwt -> jwt
                .subject(UUID.randomUUID().toString())
                .claim("sub", UUID.randomUUID().toString())
                .claim("realm_access", Map.of("roles", List.of("ADMIN"))))
            .authorities(new SimpleGrantedAuthority("ROLE_ADMIN"));
    }

    private SecurityMockMvcRequestPostProcessors.JwtRequestPostProcessor userJwt() {

        return jwt()
            .jwt(jwt -> jwt
                .subject(UUID.randomUUID().toString())
                .claim("sub", UUID.randomUUID().toString())
                .claim("realm_access", Map.of("roles", List.of("USER"))))
            .authorities(new SimpleGrantedAuthority("ROLE_USER"));
    }

    private Item createItem() {

        Item item = new Item();
        item.setName("Phone");
        item.setPrice(BigDecimal.valueOf(1000));

        return itemRepository.save(item);
    }

    @Test
    void shouldCreateItem() throws Exception {

        String json = """
        {
          "name":"Laptop",
          "price":2500.00
        }
        """;

        MvcResult result = mockMvc.perform(
                post("/api/items")
                    .with(adminJwt())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(json))
            .andExpect(status().isCreated())
            .andReturn();

        ItemDisplayDto response = objectMapper.readValue(
            result.getResponse().getContentAsString(),
            ItemDisplayDto.class
        );

        assertThat(response.id()).isNotNull();
        assertThat(response.name()).isEqualTo("Laptop");
        assertThat(response.price()).isEqualByComparingTo("2500.00");
        assertThat(itemRepository.count()).isEqualTo(1);
    }

    @Test
    void shouldGetItemById() throws Exception {

        Item item = createItem();

        MvcResult result = mockMvc.perform(
                get("/api/items/{id}", item.getId())
                    .with(adminJwt()))
            .andExpect(status().isOk())
            .andReturn();

        ItemDisplayDto response = objectMapper.readValue(
            result.getResponse().getContentAsString(),
            ItemDisplayDto.class
        );

        assertThat(response.id()).isEqualTo(item.getId());
        assertThat(response.name()).isEqualTo(item.getName());
        assertThat(response.price()).isEqualByComparingTo(item.getPrice());
    }

    @Test
    void shouldGetAllItems() throws Exception {

        createItem();

        MvcResult result = mockMvc.perform(
                get("/api/items")
                    .with(adminJwt()))
            .andExpect(status().isOk())
            .andReturn();

        String response = result.getResponse().getContentAsString();

        assertThat(response).contains("Phone");
    }

    @Test
    void shouldUpdateItem() throws Exception {

        Item item = createItem();

        String json = """
        {
          "name":"IPhone",
          "price":3500.00
        }
        """;

        MvcResult result = mockMvc.perform(
                put("/api/items/{id}", item.getId())
                    .with(adminJwt())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(json))
            .andExpect(status().isOk())
            .andReturn();

        ItemDisplayDto response = objectMapper.readValue(
            result.getResponse().getContentAsString(),
            ItemDisplayDto.class
        );

        assertThat(response.name()).isEqualTo("IPhone");
        assertThat(response.price()).isEqualByComparingTo("3500.00");

        Item updated = itemRepository.findById(item.getId()).orElseThrow();

        assertThat(updated.getName()).isEqualTo("IPhone");
        assertThat(updated.getPrice()).isEqualByComparingTo("3500.00");
    }

    @Test
    void shouldDeleteItem() throws Exception {

        Item item = createItem();

        mockMvc.perform(
                delete("/api/items/{id}", item.getId())
                    .with(adminJwt()))
            .andExpect(status().isNoContent());

        assertThat(itemRepository.findById(item.getId())).isEmpty();
    }

    @Test
    void shouldReturnNotFoundWhenItemDoesNotExist() throws Exception {

        mockMvc.perform(
                get("/api/items/{id}", 999L)
                    .with(adminJwt()))
            .andExpect(status().isNotFound());
    }

    @Test
    void shouldGetAllItemsAsUser() throws Exception {

        createItem();

        MvcResult result = mockMvc.perform(
                get("/api/items")
                    .with(userJwt()))
            .andExpect(status().isOk())
            .andReturn();

        String response = result.getResponse().getContentAsString();

        assertThat(response).contains("Phone");
    }

    @Test
    void shouldGetItemByIdAsUser() throws Exception {

        Item item = createItem();

        mockMvc.perform(
                get("/api/items/{id}", item.getId())
                    .with(userJwt()))
            .andExpect(status().isOk());
    }

    @Test
    void shouldReturnForbiddenWhenUserCreatesItem() throws Exception {

        String json = """
        {
          "name":"Laptop",
          "price":2500.00
        }
        """;

        mockMvc.perform(
                post("/api/items")
                    .with(userJwt())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(json))
            .andExpect(status().isForbidden());

        assertThat(itemRepository.count()).isZero();
    }

    @Test
    void shouldReturnForbiddenWhenUserUpdatesItem() throws Exception {

        Item item = createItem();

        String json = """
        {
          "name":"IPhone",
          "price":3500.00
        }
        """;

        mockMvc.perform(
                put("/api/items/{id}", item.getId())
                    .with(userJwt())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(json))
            .andExpect(status().isForbidden());
    }

    @Test
    void shouldReturnForbiddenWhenUserDeletesItem() throws Exception {

        Item item = createItem();

        mockMvc.perform(
                delete("/api/items/{id}", item.getId())
                    .with(userJwt()))
            .andExpect(status().isForbidden());

        assertThat(itemRepository.findById(item.getId())).isPresent();
    }

    @Test
    void shouldReturnUnauthorizedWithoutJwt() throws Exception {

        mockMvc.perform(get("/api/items"))
            .andExpect(status().isUnauthorized());
        mockMvc.perform(post("/api/items")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"name\":\"Laptop\",\"price\":2500.00}"))
            .andExpect(status().isUnauthorized());
        mockMvc.perform(delete("/api/items/{id}", 1L))
            .andExpect(status().isUnauthorized());
    }
}