package com.dsavitskiy.orderservice.mapper;

import com.dsavitskiy.orderservice.dto.OrderItemCreateDto;
import com.dsavitskiy.orderservice.dto.OrderItemDisplayDto;
import com.dsavitskiy.orderservice.entity.OrderItem;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(
    componentModel = "spring",
    uses = ItemMapper.class
)
public interface OrderItemMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "order", ignore = true)
    @Mapping(target = "item", ignore = true)
    OrderItem toEntity(OrderItemCreateDto dto);

    OrderItemDisplayDto toDisplayDto(OrderItem entity);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "order", ignore = true)
    @Mapping(target = "item", ignore = true)
    void updateEntity(OrderItemCreateDto dto,
                      @MappingTarget OrderItem entity);
}