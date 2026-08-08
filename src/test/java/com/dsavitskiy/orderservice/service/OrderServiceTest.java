package com.dsavitskiy.orderservice.service;

import com.dsavitskiy.orderservice.client.UserClient;
import com.dsavitskiy.orderservice.dto.*;
import com.dsavitskiy.orderservice.entity.Item;
import com.dsavitskiy.orderservice.entity.Order;
import com.dsavitskiy.orderservice.exception.ResourceNotFoundExeption;
import com.dsavitskiy.orderservice.exception.UserServiceException;
import com.dsavitskiy.orderservice.mapper.OrderMapper;
import com.dsavitskiy.orderservice.repository.ItemRepository;
import com.dsavitskiy.orderservice.repository.OrderRepository;
import com.dsavitskiy.orderservice.util.SecurityUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;
    @Mock
    private ItemRepository itemRepository;
    @Mock
    private OrderMapper orderMapper;
    @Mock
    private UserClient userClient;

    private OrderService orderService;
    private UUID userId;

    @BeforeEach
    void setUp() {
        orderService = new OrderService(orderRepository, itemRepository, orderMapper, userClient);
        userId = UUID.randomUUID();
    }

    @Test
    void createOrder_ShouldCreateOrder() {
        OrderCreateDto dto = new OrderCreateDto(List.of(new OrderItemCreateDto(1L, 2)));

        Order order = new Order();

        Item item = new Item();
        item.setId(1L);
        item.setPrice(BigDecimal.TEN);

        Order saved = new Order();
        saved.setUserId(userId);

        UserDisplayDto user = new UserDisplayDto(
            userId,
            "John",
            "Doe",
            "john@mail.com"
        );

        when(orderMapper.toEntity(dto)).thenReturn(order);
        when(itemRepository.findById(1L)).thenReturn(Optional.of(item));
        when(orderRepository.save(any(Order.class))).thenReturn(saved);
        when(userClient.getUserById(userId)).thenReturn(user);
        when(orderMapper.toDisplayDto(saved)).thenReturn(mock(OrderDisplayDto.class));

        try (MockedStatic<SecurityUtil> security = mockStatic(SecurityUtil.class)) {
            security.when(SecurityUtil::getCurrentUserId).thenReturn(userId);

            OrderResponseDto response = orderService.createOrder(dto);

            assertNotNull(response);
            verify(orderRepository).save(any(Order.class));
        }
    }

    @Test
    void getOrderById_ShouldReturnOrder() {
        Order order = new Order();
        order.setId(1L);
        order.setUserId(userId);

        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        when(orderMapper.toDisplayDto(order)).thenReturn(mock(OrderDisplayDto.class));
        when(userClient.getUserById(userId)).thenReturn(mock(UserDisplayDto.class));

        try (MockedStatic<SecurityUtil> security = mockStatic(SecurityUtil.class)) {
            security.when(SecurityUtil::isAdmin).thenReturn(true);

            OrderResponseDto response = orderService.getOrderById(1L);

            assertNotNull(response);
        }
    }

    @Test
    void getOrderById_ShouldThrowNotFound() {
        when(orderRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundExeption.class,
            () -> orderService.getOrderById(1L));
    }

    @Test
    void deleteOrder_ShouldMarkDeleted() {
        Order order = new Order();
        order.setUserId(userId);

        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

        try (MockedStatic<SecurityUtil> security = mockStatic(SecurityUtil.class)) {
            security.when(SecurityUtil::isAdmin).thenReturn(true);

            orderService.deleteOrder(1L);

            assertTrue(order.isDeleted());
        }
    }

    @Test
    void updateOrder_ShouldThrowAccessDenied() {
        Order order = new Order();
        order.setUserId(UUID.randomUUID());

        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

        try (MockedStatic<SecurityUtil> security = mockStatic(SecurityUtil.class)) {
            security.when(SecurityUtil::isAdmin).thenReturn(false);
            security.when(SecurityUtil::getCurrentUserId).thenReturn(userId);

            OrderCreateDto dto = new OrderCreateDto(List.of());

            assertThrows(
                AccessDeniedException.class,
                () -> orderService.updateOrder(1L, dto)
            );
        }
    }

    @Test
    void fallbackUserByEmail_ShouldThrowUserServiceException() {
        assertThrows(
            UserServiceException.class,
            () -> orderService.fallbackUserByEmail(
                "mail@test.com",
                new RuntimeException("service down")
            )
        );
    }

    @Test
    void fallbackUserById_ShouldThrowUserServiceException() {
        assertThrows(
            UserServiceException.class,
            () -> orderService.fallbackUserById(
                userId,
                new RuntimeException("service down")
            )
        );
    }
}