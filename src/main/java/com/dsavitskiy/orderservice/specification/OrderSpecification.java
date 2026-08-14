package com.dsavitskiy.orderservice.specification;

import com.dsavitskiy.orderservice.entity.Order;
import com.dsavitskiy.orderservice.entity.OrderStatus;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDateTime;
import java.util.List;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class OrderSpecification {

    public static Specification<Order> createdAfter(LocalDateTime from) {
        if (from == null) {
            return (root, query, cb) -> cb.conjunction();
        }

        return (root, query, cb) ->
            cb.greaterThanOrEqualTo(root.get("createdAt"), from);
    }

    public static Specification<Order> createdBefore(LocalDateTime to) {
        if (to == null) {
            return (root, query, cb) -> cb.conjunction();
        }

        return (root, query, cb) ->
            cb.lessThanOrEqualTo(root.get("createdAt"), to);
    }

    public static Specification<Order> hasStatuses(List<OrderStatus> statuses) {
        if (statuses == null || statuses.isEmpty()) {
            return (root, query, cb) -> cb.conjunction();
        }

        return (root, query, cb) ->
            root.get("status").in(statuses);
    }

    public static Specification<Order> notDeleted() {
        return (root, query, cb) ->
            cb.isFalse(root.get("deleted"));
    }
}