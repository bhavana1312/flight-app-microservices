package com.flightapp.bookingservice.exception;

import org.junit.jupiter.api.Test;
import org.springframework.core.MethodParameter;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.validation.*;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    void testValidationErrorHandler() throws NoSuchMethodException {

        BindingResult result = new BeanPropertyBindingResult("obj", "obj");
        result.addError(new FieldError("obj", "email", "Invalid email"));

        MethodParameter methodParameter = new MethodParameter(
                this.getClass().getDeclaredMethod("testValidationErrorHandler"), -1);

        MethodArgumentNotValidException ex =
                new MethodArgumentNotValidException(methodParameter, result);

        ResponseEntity<Map<String, String>> resp = handler.handleValidationErrors(ex);

        assertEquals(400, resp.getStatusCode().value());
        assertEquals("Invalid email", resp.getBody().get("email"));
    }

    @Test
    void testRuntimeExceptionHandler() {
        RuntimeException ex = new RuntimeException("Something failed");

        ResponseEntity<String> resp = handler.handleRuntimeErrors(ex);

        assertEquals("Something failed", resp.getBody());
        assertEquals(400, resp.getStatusCode().value());
    }
}
