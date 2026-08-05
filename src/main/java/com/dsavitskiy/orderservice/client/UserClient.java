package com.dsavitskiy.orderservice.client;

import com.dsavitskiy.orderservice.config.FeignConfig;
import com.dsavitskiy.orderservice.dto.UserDisplayDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.UUID;


@FeignClient(
    name = "user-service",
    url = "${services.user-service-url}",
    configuration = FeignConfig.class
)
public interface UserClient {

    @GetMapping("/api/users/email/{email}")
    UserDisplayDto getUserByEmail(
        @PathVariable("email") String email);

    @GetMapping("/api/users/{id}")
    UserDisplayDto getUserById(
        @PathVariable("id") UUID id);
}