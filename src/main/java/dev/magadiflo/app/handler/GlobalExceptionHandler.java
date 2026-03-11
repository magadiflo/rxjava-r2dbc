package dev.magadiflo.app.handler;

import dev.magadiflo.app.exception.EmployeeNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.stream.Collectors;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(EmployeeNotFoundException.class)
    public ResponseEntity<ProblemDetail> handleEmployeeNotFoundException(Exception ex) {
        log.debug("handleEmployeeNotFoundException: {}", ex.getMessage());
        var response = this.build(HttpStatus.NOT_FOUND, ex, problemDetail ->
                problemDetail.setTitle("Empleado no encontrado"));
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ProblemDetail> handleMethodArgumentNotValidException(MethodArgumentNotValidException ex) {
        log.debug("MethodArgumentNotValidException: {}", ex.getMessage());
        Map<String, List<String>> errorsByField = ex.getFieldErrors().stream()
                .collect(Collectors.groupingBy(
                        FieldError::getField,
                        Collectors.mapping(FieldError::getDefaultMessage, Collectors.toList())
                ));
        var response = this.build(HttpStatus.BAD_REQUEST, ex, problemDetail -> {
            problemDetail.setTitle("Error de validación de campos");
            problemDetail.setProperty("errors", errorsByField);
        });
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ProblemDetail> handleException(Exception ex) {
        log.debug("handleException: {}", ex.getMessage(), ex);
        var response = this.build(HttpStatus.INTERNAL_SERVER_ERROR, ex, problemDetail ->
                problemDetail.setTitle("Ocurrió un error en el servidor. Por favor, contacta al administrador."));
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }


    private ProblemDetail build(HttpStatus status, Exception ex, Consumer<ProblemDetail> detailConsumer) {
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(status, ex.getMessage());
        detailConsumer.accept(problemDetail);
        return problemDetail;
    }
}
