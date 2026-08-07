package com.dsavitskiy.orderservice.service;

import com.dsavitskiy.orderservice.client.UserClient;
import com.dsavitskiy.orderservice.dto.OrderCreateDto;
import com.dsavitskiy.orderservice.dto.OrderDisplayDto;
import com.dsavitskiy.orderservice.dto.OrderItemCreateDto;
import com.dsavitskiy.orderservice.dto.OrderResponseDto;
import com.dsavitskiy.orderservice.dto.UserDisplayDto;
import com.dsavitskiy.orderservice.entity.Item;
import com.dsavitskiy.orderservice.entity.Order;
import com.dsavitskiy.orderservice.entity.OrderItem;
import com.dsavitskiy.orderservice.exception.ResourceNotFoundExeption;
import com.dsavitskiy.orderservice.mapper.OrderMapper;
import com.dsavitskiy.orderservice.repository.ItemRepository;
import com.dsavitskiy.orderservice.repository.OrderRepository;
import com.dsavitskiy.orderservice.specification.OrderSpecification;
import com.dsavitskiy.orderservice.util.SecurityUtil;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class OrderService {

    private static final String ORDER_NOT_FOUND = "Order not found with id: ";
    private static final String ITEM_NOT_FOUND = "Item not found with id: ";
    private static final String STATUS_PENDING = "PENDING";

    private final OrderRepository orderRepository;
    private final ItemRepository itemRepository;
    private final OrderMapper orderMapper;
    private final UserClient userClient;

    @Transactional
    public OrderResponseDto createOrder(OrderCreateDto dto) {
        Order order = orderMapper.toEntity(dto);
        order.setUserId(SecurityUtil.getCurrentUserId());
        order.setStatus(STATUS_PENDING);
        order.setDeleted(false);
        fillOrder(order, dto.items());
        Order saved = orderRepository.save(order);
        return buildResponse(saved);
    }

    @Transactional(readOnly = true)
    public OrderResponseDto getOrderById(Long id) {
        Order order = orderRepository.findById(id)
            .filter(o -> !o.isDeleted())
            .orElseThrow(() -> new ResourceNotFoundExeption(ORDER_NOT_FOUND + id));
        checkAccess(order);
        return buildResponse(order);
    }

    @Transactional(readOnly = true)
    public Page<OrderResponseDto> getOrders(
        LocalDateTime from,
        LocalDateTime to,
        List<String> statuses,
        Pageable pageable) {
        if (!SecurityUtil.isAdmin()) {
            throw new AccessDeniedException("Access denied");
        }
        Specification<Order> specification = Specification.allOf(
            OrderSpecification.notDeleted(),
            OrderSpecification.createdAfter(from),
            OrderSpecification.createdBefore(to),
            OrderSpecification.hasStatuses(statuses)
        );

        return orderRepository.findAll(specification, pageable)
            .map(this::buildResponse);
    }

    @Transactional(readOnly = true)
    public List<OrderResponseDto> getOrdersByUserId(UUID userId) {
        if (!SecurityUtil.isAdmin()
            && !SecurityUtil.getCurrentUserId().equals(userId)) {
            throw new AccessDeniedException("Access denied");
        }
        return orderRepository.findByUserIdAndDeletedFalse(userId)
            .stream()
            .map(this::buildResponse)
            .toList();
    }

    @Transactional
    public OrderResponseDto updateOrder(Long id, OrderCreateDto dto) {
        Order order = orderRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundExeption(ORDER_NOT_FOUND + id));
        checkAccess(order);
        order.getOrderItems().clear();
        fillOrder(order, dto.items());
        return buildResponse(order);
    }

    @Transactional
    public void deleteOrder(Long id) {
        Order order = orderRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundExeption(ORDER_NOT_FOUND + id));
        checkAccess(order);
        order.setDeleted(true);
    }
    private void fillOrder(
        Order order,
        List<OrderItemCreateDto> items) {
        BigDecimal totalPrice = BigDecimal.ZERO;
        for (OrderItemCreateDto dto : items) {
            Item item = itemRepository.findById(dto.itemId())
                .orElseThrow(() -> new ResourceNotFoundExeption(ITEM_NOT_FOUND + dto.itemId()));
            OrderItem orderItem = new OrderItem();
            orderItem.setOrder(order);
            orderItem.setItem(item);
            orderItem.setQuantity(dto.quantity());
            order.getOrderItems().add(orderItem);
            totalPrice = totalPrice.add(
                item.getPrice().multiply(BigDecimal.valueOf(dto.quantity()))
            );
        }
        order.setTotalPrice(totalPrice);
    }

    private void checkAccess(Order order) {
        if (SecurityUtil.isAdmin()) {
            return;
        }
        if (!order.getUserId().equals(SecurityUtil.getCurrentUserId())) {
            throw new AccessDeniedException("Access denied");
        }
    }

    private OrderResponseDto buildResponse(Order order) {
        UserDisplayDto user = getUserById(order.getUserId());
        OrderDisplayDto orderDto = orderMapper.toDisplayDto(order);
        return new OrderResponseDto(orderDto, user);
    }

    @CircuitBreaker(
        name = "userService",
        fallbackMethod = "fallbackUserByEmail")
    public UserDisplayDto getUserByEmail(String email) {
        return userClient.getUserByEmail(email);
    }

    @CircuitBreaker(
        name = "userService",
        fallbackMethod = "fallbackUserById")
    public UserDisplayDto getUserById(UUID userId) {
        return userClient.getUserById(userId);
    }

    public UserDisplayDto fallbackUserByEmail(
        String email,
        Throwable throwable) {
        log.info("Fallback because: {}", throwable.getMessage());
        return new UserDisplayDto(
            null,
            "Unknown",
            "User",
            email,
            null,
            false
        );
    }

    public UserDisplayDto fallbackUserById(
        UUID userId,
        Throwable throwable) {
        log.info("Fallback because: {}", throwable.getMessage());
        return new UserDisplayDto(
            userId,
            "Unknown",
            "User",
            "unknown@mail.com",
            null,
            false
        );
    }
}