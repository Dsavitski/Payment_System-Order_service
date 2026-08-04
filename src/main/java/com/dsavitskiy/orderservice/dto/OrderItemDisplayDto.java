package com.dsavitskiy.orderservice.dto;

public record OrderItemDisplayDto (
    Long id,
    ItemDisplayDto item,
    Integer quantity){

}
