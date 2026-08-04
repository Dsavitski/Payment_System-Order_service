package com.dsavitskiy.orderservice.mapper;

import com.dsavitskiy.orderservice.dto.OrderCreateDto;
import com.dsavitskiy.orderservice.dto.OrderDisplayDto;
import com.dsavitskiy.orderservice.entity.Order;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(
    componentModel = "spring",
    uses = OrderItemMapper.class
)
public interface OrderMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "userId", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "totalPrice", ignore = true)
    @Mapping(target = "deleted", ignore = true)
    @Mapping(target = "orderItems", ignore = true)
    Order toEntity(OrderCreateDto dto);

    @Mapping(source = "orderItems", target = "items")
    OrderDisplayDto toDisplayDto(Order entity);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "userId", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "totalPrice", ignore = true)
    @Mapping(target = "deleted", ignore = true)
    @Mapping(target = "orderItems", ignore = true)
    void updateEntity(OrderCreateDto dto,
                      @MappingTarget Order entity);
}