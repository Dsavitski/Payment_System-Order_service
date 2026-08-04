package com.dsavitskiy.orderservice.controller;

import com.dsavitskiy.orderservice.dto.ItemCreateDto;
import com.dsavitskiy.orderservice.dto.ItemDisplayDto;
import com.dsavitskiy.orderservice.service.ItemService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/items")
public class ItemController {
    private final ItemService itemService;

    @PostMapping
    public ResponseEntity<ItemDisplayDto> createItem(
        @Valid @RequestBody ItemCreateDto itemCreateDto) {
        ItemDisplayDto item = itemService.createItem(itemCreateDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(item);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ItemDisplayDto> getItemById(
        @PathVariable Long id) {
        return ResponseEntity.ok(itemService.getItemById(id));
    }
    @GetMapping
    public ResponseEntity<Page<ItemDisplayDto>> getAllItems(
        @PageableDefault(size = 10) Pageable pageable) {
        return ResponseEntity.ok(itemService.getAllItems(pageable));
    }
    @PutMapping("/{id}")
    public ResponseEntity<ItemDisplayDto> updateItem(
        @PathVariable Long id,
        @Valid @RequestBody ItemCreateDto itemCreateDto) {
        return ResponseEntity.ok(itemService.updateItem(id, itemCreateDto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteItem(
        @PathVariable Long id) {
        itemService.deleteItem(id);
        return ResponseEntity.noContent().build();
    }
}