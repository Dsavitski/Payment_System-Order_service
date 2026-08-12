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

    Order toEntity(OrderCreateDto dto);

    @Mapping(source = "orderItems", target = "items")
    OrderDisplayDto toDisplayDto(Order entity);


    void updateEntity(OrderCreateDto dto,
                      @MappingTarget Order entity);
}