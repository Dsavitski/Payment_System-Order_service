package com.dsavitskiy.orderservice.mapper;

import com.dsavitskiy.orderservice.dto.ItemCreateDto;
import com.dsavitskiy.orderservice.dto.ItemDisplayDto;
import com.dsavitskiy.orderservice.entity.Item;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface ItemMapper {

    Item toEntity(ItemCreateDto itemCreateDto);

    ItemDisplayDto toDisplayDto(Item item);

    void updateEntity(ItemCreateDto itemCreateDto, @MappingTarget Item item);

}
