package com.dsavitskiy.orderservice.service;

import com.dsavitskiy.orderservice.dto.ItemCreateDto;
import com.dsavitskiy.orderservice.dto.ItemDisplayDto;
import com.dsavitskiy.orderservice.entity.Item;
import com.dsavitskiy.orderservice.exception.ResourceNotFoundExeption;
import com.dsavitskiy.orderservice.mapper.ItemMapper;
import com.dsavitskiy.orderservice.repository.ItemRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ItemServiceTest {

    @Mock
    private ItemRepository itemRepository;

    @Mock
    private ItemMapper itemMapper;

    @InjectMocks
    private ItemService itemService;

    private Item item;
    private ItemCreateDto createDto;
    private ItemDisplayDto displayDto;

    @BeforeEach
    void setUp() {
        item = new Item();
        item.setId(1L);
        item.setName("Coffee");
        item.setPrice(BigDecimal.TEN);

        createDto = mock(ItemCreateDto.class);
        displayDto = mock(ItemDisplayDto.class);
    }

    @Test
    void createItem_ShouldReturnDisplayDto() {
        when(itemMapper.toEntity(createDto)).thenReturn(item);
        when(itemRepository.save(item)).thenReturn(item);
        when(itemMapper.toDisplayDto(item)).thenReturn(displayDto);

        ItemDisplayDto result = itemService.createItem(createDto);

        assertEquals(displayDto, result);
        verify(itemMapper).toEntity(createDto);
        verify(itemRepository).save(item);
        verify(itemMapper).toDisplayDto(item);
    }

    @Test
    void getItemById_ShouldReturnDisplayDto() {
        when(itemRepository.findById(1L)).thenReturn(Optional.of(item));
        when(itemMapper.toDisplayDto(item)).thenReturn(displayDto);

        ItemDisplayDto result = itemService.getItemById(1L);

        assertEquals(displayDto, result);
        verify(itemRepository).findById(1L);
    }

    @Test
    void getItemById_ShouldThrowResourceNotFoundException() {
        when(itemRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(
            ResourceNotFoundExeption.class,
            () -> itemService.getItemById(1L)
        );

        verify(itemRepository).findById(1L);
    }

    @Test
    void getAllItems_ShouldReturnPage() {
        Page<Item> page = new PageImpl<>(List.of(item));
        Pageable pageable = PageRequest.of(0, 10);

        when(itemRepository.findAll(pageable)).thenReturn(page);
        when(itemMapper.toDisplayDto(item)).thenReturn(displayDto);

        Page<ItemDisplayDto> result = itemService.getAllItems(pageable);

        assertEquals(1, result.getTotalElements());
        assertEquals(displayDto, result.getContent().getFirst());

        verify(itemRepository).findAll(pageable);
    }

    @Test
    void updateItem_ShouldReturnUpdatedDto() {
        when(itemRepository.findById(1L)).thenReturn(Optional.of(item));
        when(itemRepository.save(item)).thenReturn(item);
        when(itemMapper.toDisplayDto(item)).thenReturn(displayDto);

        ItemDisplayDto result = itemService.updateItem(1L, createDto);

        assertEquals(displayDto, result);

        verify(itemMapper).updateEntity(createDto, item);
        verify(itemRepository).save(item);
    }

    @Test
    void updateItem_ShouldThrowResourceNotFoundException() {
        when(itemRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(
            ResourceNotFoundExeption.class,
            () -> itemService.updateItem(1L, createDto)
        );

        verify(itemMapper, never()).updateEntity(any(), any());
        verify(itemRepository, never()).save(any());
    }

    @Test
    void deleteItem_ShouldDeleteItem() {
        when(itemRepository.findById(1L)).thenReturn(Optional.of(item));

        itemService.deleteItem(1L);

        verify(itemRepository).delete(item);
    }

    @Test
    void deleteItem_ShouldThrowResourceNotFoundException() {
        when(itemRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(
            ResourceNotFoundExeption.class,
            () -> itemService.deleteItem(1L)
        );

        verify(itemRepository, never()).delete(any());
    }
}