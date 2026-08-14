package com.dsavitskiy.orderservice.exception;

import com.dsavitskiy.orderservice.dto.ErrorResponseDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.time.ZoneId;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {
    private static final String LOG_MESSAGE = "HTTP {}: {}";
    private static final String MINSK_TIME_ZONE = "Europe/Minsk";

    @ExceptionHandler(ResourceNotFoundExeption.class)
    public ResponseEntity<ErrorResponseDto> resourceNotFoundException(ResourceNotFoundExeption ex) {
      log.warn(LOG_MESSAGE, HttpStatus.NOT_FOUND.value(),ex.getMessage());
      ErrorResponseDto response =new ErrorResponseDto(
          LocalDateTime.now(ZoneId.of(MINSK_TIME_ZONE)),
          HttpStatus.NOT_FOUND.value(),
          HttpStatus.NOT_FOUND.getReasonPhrase(),
          ex.getMessage());
      return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
    }
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponseDto> methodArgumentNotValidException(
        MethodArgumentNotValidException ex) {
        log.info(LOG_MESSAGE, HttpStatus.BAD_REQUEST.value(), ex.getMessage());
        ErrorResponseDto response =new ErrorResponseDto(
            LocalDateTime.now(ZoneId.of(MINSK_TIME_ZONE)),
            HttpStatus.BAD_REQUEST.value(),
            HttpStatus.BAD_REQUEST.getReasonPhrase(),
            ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponseDto> accessDeniedException(AccessDeniedException ex) {
        log.info(LOG_MESSAGE, HttpStatus.FORBIDDEN.value(), ex.getMessage());
        ErrorResponseDto response =new ErrorResponseDto(
            LocalDateTime.now(ZoneId.of(MINSK_TIME_ZONE)),
            HttpStatus.FORBIDDEN.value(),
            HttpStatus.FORBIDDEN.getReasonPhrase(),
            ex.getMessage());
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponseDto> unexpectedException(Exception ex) {
        log.info(LOG_MESSAGE, HttpStatus.INTERNAL_SERVER_ERROR.value(), ex.getMessage());
        ErrorResponseDto response =new ErrorResponseDto(
            LocalDateTime.now(ZoneId.of(MINSK_TIME_ZONE)),
            HttpStatus.INTERNAL_SERVER_ERROR.value(),
            HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase(),
            ex.getMessage());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }

    @ExceptionHandler(UserServiceException.class)
    public ResponseEntity<ErrorResponseDto> userServiceException(UserServiceException ex){
        log.info(LOG_MESSAGE, HttpStatus.SERVICE_UNAVAILABLE.value(), ex.getMessage());
        ErrorResponseDto response = new ErrorResponseDto(
            LocalDateTime.now(ZoneId.of(MINSK_TIME_ZONE)),
            HttpStatus.SERVICE_UNAVAILABLE.value(),
            HttpStatus.SERVICE_UNAVAILABLE.getReasonPhrase(),
            ex.getMessage());
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(response);
    }





}
