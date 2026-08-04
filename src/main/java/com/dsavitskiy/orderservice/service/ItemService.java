package com.dsavitskiy.orderservice.service;

import com.dsavitskiy.orderservice.dto.ItemCreateDto;
import com.dsavitskiy.orderservice.dto.ItemDisplayDto;
import com.dsavitskiy.orderservice.entity.Item;
import com.dsavitskiy.orderservice.exception.ResourceNotFoundExeption;
import com.dsavitskiy.orderservice.mapper.ItemMapper;
import com.dsavitskiy.orderservice.repository.ItemRepository;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;


@Service
@RequiredArgsConstructor
public class ItemService {
    private static final String NOT_FOUND_MESSAGE = "Item not found with id: ";
    private final ItemMapper itemMapper;
    private final ItemRepository itemRepository;

    @Transactional
    public ItemDisplayDto createItem(ItemCreateDto itemCreateDto){
        Item item = itemMapper.toEntity(itemCreateDto);
        return itemMapper.toDisplayDto(itemRepository.save(item));
    }
    @Transactional(readOnly = true)
    public ItemDisplayDto getItemById(Long id){
        Item item = itemRepository.findById(id).orElseThrow(
            ()-> new ResourceNotFoundExeption(NOT_FOUND_MESSAGE + id));
        return itemMapper.toDisplayDto(item);
    }
    @Transactional(readOnly = true)
    public Page<ItemDisplayDto> getAllItems(Pageable pageable){
        return itemRepository.findAll(pageable).map(itemMapper::toDisplayDto);
    }
    @Transactional
    public ItemDisplayDto updateItem(Long id, ItemCreateDto itemCreateDto){
        Item item = itemRepository.findById(id).orElseThrow(
            ()-> new ResourceNotFoundExeption(NOT_FOUND_MESSAGE + id));
        itemMapper.updateEntity(itemCreateDto, item);
        return itemMapper.toDisplayDto(itemRepository.save(item));
    }

    @Transactional
    public void deleteItem(Long id){
        Item item = itemRepository.findById(id).orElseThrow(
            ()-> new ResourceNotFoundExeption(NOT_FOUND_MESSAGE + id));
        itemRepository.delete(item);
    }
}
