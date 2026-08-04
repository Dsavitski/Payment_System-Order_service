package com.dsavitskiy.orderservice.exception;

import com.dsavitskiy.orderservice.dto.ErrorResponseDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.time.ZoneId;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {
    private static final String LOG_WARNS = "HTTP {}: {}";
    private static final String MINSK_TIME_ZONE = "Europe/Minsk";
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponseDto> resourceNotFoundException(ResourceNotFoundExeption ex) {
      log.warn(LOG_WARNS, HttpStatus.NOT_FOUND.value(),ex.getMessage());
      ErrorResponseDto response =new ErrorResponseDto(
          LocalDateTime.now(ZoneId.of(MINSK_TIME_ZONE)),
          HttpStatus.NOT_FOUND.value(),
          HttpStatus.NOT_FOUND.getReasonPhrase(),
          ex.getMessage());
      return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
    }
}
