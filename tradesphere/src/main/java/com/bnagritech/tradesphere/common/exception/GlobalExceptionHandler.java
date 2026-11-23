package com.bnagritech.tradesphere.common.exception;

import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.BindException;
import org.springframework.validation.BindingResult;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.multipart.support.MissingServletRequestPartException;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(UserAlreadyExistsException.class)
    public ResponseEntity<?> handleUserAlreadyExists(
            UserAlreadyExistsException ex) {

        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of(
                        "message", ex.getMessage()));
    }
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<?> handleResourceNotFound(ResourceNotFoundException ex){
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of(
                "message",ex.getMessage()));
    }

    @ExceptionHandler(TerritoryAlreadyExistException.class)
    public ResponseEntity<?>handleTerritoryAlreadyExists(TerritoryAlreadyExistException ex){
        return ResponseEntity.status((HttpStatus.BAD_REQUEST))
                .body(Map.of(
                        "message",ex.getMessage()));
    }

    @ExceptionHandler(TerritoryNotFoundException.class)
    public ResponseEntity<?>handleTerritoryNotFound(TerritoryNotFoundException ex){
        return ResponseEntity.status((HttpStatus.BAD_REQUEST))
                .body(Map.of(
                        "message",ex.getMessage()
                ));
    }
    @ExceptionHandler(EmployeeNotFoundException.class)
    public ResponseEntity<?>handleUserNameNotFound(EmployeeNotFoundException ex){
        return ResponseEntity.status((HttpStatus.BAD_REQUEST))
                .body(Map.of(
                        "message",ex.getMessage()));
    }
    @ExceptionHandler(EmployeeAlreadyExistsException.class)
    public ResponseEntity<?>handleEmployeeAlreadyExists(EmployeeAlreadyExistsException ex){
        return ResponseEntity.status((HttpStatus.BAD_REQUEST))
                .body(Map.of(
                        "message",ex.getMessage()));
    }
    @ExceptionHandler(PromoterNotFoundException.class)
    public ResponseEntity<?>handlePromoterNotFound(PromoterNotFoundException ex){
        return ResponseEntity.status((HttpStatus.BAD_REQUEST))
                .body(Map.of(
                        "message",ex.getMessage()));
    }
    @ExceptionHandler(ResourceAlreadyExistsException.class)
    public ResponseEntity<?>handleResourceAlreadyExists(ResourceAlreadyExistsException ex){
        return ResponseEntity.status((HttpStatus.BAD_REQUEST))
                .body(Map.of(
                        "message",ex.getMessage()));
    }
    @ExceptionHandler(UserInactiveException.class)
    public ResponseEntity<?>handleUserInactive(UserInactiveException ex){
        return ResponseEntity.status((HttpStatus.BAD_REQUEST))
                .body(Map.of(
                        "message",ex.getMessage()));
    }
    @ExceptionHandler(InvalidCredentialsException.class)
    public ResponseEntity<?>handleInvalidCredential(InvalidCredentialsException ex){
        return ResponseEntity.status((HttpStatus.BAD_REQUEST))
                .body(Map.of(
                        "message",ex.getMessage()));
    }
    @ExceptionHandler(AccountLockedException.class)
    public ResponseEntity<?>handleAccountLocked(AccountLockedException ex){
        return ResponseEntity.status((HttpStatus.BAD_REQUEST))
                .body(Map.of(
                        "message",ex.getMessage()));
    }
    @ExceptionHandler(InvalidOTPException.class)
    public ResponseEntity<?>handleInvalidOTP(InvalidOTPException ex){
        return ResponseEntity.status((HttpStatus.BAD_REQUEST))
                .body(Map.of(
                        "message",ex.getMessage()));
    }
    @ExceptionHandler(PasswordNotMatchException.class)
    public ResponseEntity<?>handlePasswordNotMatch(PasswordNotMatchException ex){
        return ResponseEntity.status((HttpStatus.BAD_REQUEST))
                .body(Map.of(
                        "message",ex.getMessage()));
    }
    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<?>handleBadRequest(BadRequestException ex){
        return ResponseEntity.status((HttpStatus.BAD_REQUEST))
                .body(Map.of(
                        "message",ex.getMessage()));
    }

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<ApiError> handleStatus(ResponseStatusException e, HttpServletRequest request) {
        int code = e.getStatusCode().value();
        HttpStatus status = HttpStatus.resolve(code);
        String message = e.getReason() != null ? e.getReason() : (status != null ? status.getReasonPhrase() : "Error");
        if (code >= 500) {
            log.error("{} {} failed with {}: {}", request.getMethod(), request.getRequestURI(), code, message, e);
        }
        return build(code, message, request, null, null);
    }

    // ---------- wrong input ----------

    /** A @Valid request body (MethodArgumentNotValidException is a BindException) or a query-parameter object. */
    @ExceptionHandler(BindException.class)
    public ResponseEntity<ApiError> handleBind(BindException e, HttpServletRequest request) {
        BindingResult result = e.getBindingResult();
        List<ApiError.FieldIssue> issues = result.getFieldErrors().stream()
                .map(fe -> ApiError.FieldIssue.builder()
                        .field(fe.getField())
                        .message(fe.isBindingFailure()
                                ? "Invalid value '" + fe.getRejectedValue() + "'"
                                : fe.getDefaultMessage())
                        .build())
                .toList();
        String message = issues.isEmpty() ? "Validation failed"
                : issues.size() == 1 ? issues.get(0).getMessage()
                  : issues.size() + " fields are invalid. First: " + issues.get(0).getMessage();
        return build(400, message, request, issues, null);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiError> handleConstraint(ConstraintViolationException e, HttpServletRequest request) {
        List<ApiError.FieldIssue> issues = e.getConstraintViolations().stream()
                .map(v -> ApiError.FieldIssue.builder().field(v.getPropertyPath().toString()).message(v.getMessage()).build())
                .toList();
        String message = issues.isEmpty() ? "Validation failed" : issues.get(0).getMessage();
        return build(400, message, request, issues, null);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiError> handleUnreadable(HttpMessageNotReadableException e, HttpServletRequest request) {
        String message = "The request body is missing or is not valid JSON";
        if (e.getCause() instanceof InvalidFormatException ife) {
            String field = ife.getPath().stream()
                    .map(ref -> ref.getFieldName() != null ? ref.getFieldName() : "[" + ref.getIndex() + "]")
                    .collect(Collectors.joining("."));
            Class<?> target = ife.getTargetType();
            message = "Invalid value '" + ife.getValue() + "' for '" + field + "'";
            if (target != null && target.isEnum()) {
                message += ". Allowed values: " + Arrays.toString(target.getEnumConstants());
            }
        }
        return build(400, message, request, null, null);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ApiError> handleTypeMismatch(MethodArgumentTypeMismatchException e, HttpServletRequest request) {
        Class<?> type = e.getRequiredType();
        String message = "Invalid value '" + e.getValue() + "' for '" + e.getName() + "'";
        if (type != null && type.isEnum()) {
            message += ". Allowed values: " + Arrays.toString(type.getEnumConstants());
        }
        return build(400, message, request, null, null);
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ApiError> handleMissingParameter(MissingServletRequestParameterException e, HttpServletRequest request) {
        return build(400, "Missing parameter: " + e.getParameterName(), request, null, null);
    }

    @ExceptionHandler(MissingServletRequestPartException.class)
    public ResponseEntity<ApiError> handleMissingPart(MissingServletRequestPartException e, HttpServletRequest request) {
        return build(400, "Missing part of the form: " + e.getRequestPartName(), request, null, null);
    }

    // ---------- wrong request ----------

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ApiError> handleMethodNotSupported(HttpRequestMethodNotSupportedException e, HttpServletRequest request) {
        String supported = e.getSupportedHttpMethods() == null ? "" : " Supported: " + e.getSupportedHttpMethods();
        return build(405, "Method " + e.getMethod() + " is not supported for this endpoint." + supported, request, null, null);
    }

    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    public ResponseEntity<ApiError> handleMediaType(HttpMediaTypeNotSupportedException e, HttpServletRequest request) {
        return build(415, "Content type is not supported. Send JSON with the header Content-Type: application/json", request, null, null);
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ApiError> handleNoResource(NoResourceFoundException e, HttpServletRequest request) {
        return build(404, "No such endpoint: " + request.getRequestURI(), request, null, null);
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<ApiError> handleTooLarge(MaxUploadSizeExceededException e, HttpServletRequest request) {
        return build(413, "The file is too large", request, null, null);
    }

    // ---------- data conflicts ----------

    @ExceptionHandler(OptimisticLockingFailureException.class)
    public ResponseEntity<ApiError> handleOptimisticLock(OptimisticLockingFailureException e, HttpServletRequest request) {
        return build(409, "This record was changed by someone else a moment ago. Reload it and try again.", request, null, null);
    }

    @ExceptionHandler(DuplicateKeyException.class)
    public ResponseEntity<ApiError> handleDuplicateKey(DuplicateKeyException e, HttpServletRequest request) {
        return build(409, "A record with the same unique value already exists", request, null, null);
    }

    // ---------- security ----------

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiError> handleAccessDenied(AccessDeniedException e, HttpServletRequest request) {
        return build(403, "Access denied", request, null, null);
    }

    // ---------- everything else ----------

    /** An unexpected bug. The user gets a short reference code; the full stack trace is in the server log. */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> handleUnexpected(Exception e, HttpServletRequest request) {
        String referenceId = UUID.randomUUID().toString().substring(0, 8);
        log.error("Unexpected error [{}] on {} {}", referenceId, request.getMethod(), request.getRequestURI(), e);
        return build(500, "Something went wrong on the server. Quote reference " + referenceId + " when you report it.",
                request, null, referenceId);
    }

    // ---------- builder ----------

    private ResponseEntity<ApiError> build(int status, String message, HttpServletRequest request,
                                           List<ApiError.FieldIssue> issues, String referenceId) {
        HttpStatus resolved = HttpStatus.resolve(status);
        ApiError body = ApiError.builder()
                .timestamp(Instant.now())
                .status(status)
                .error(resolved != null ? resolved.getReasonPhrase() : "Error")
                .message(message)
                .path(request.getRequestURI())
                .referenceId(referenceId)
                .errors(issues == null || issues.isEmpty() ? null : issues)
                .build();
        return ResponseEntity.status(status).body(body);
    }



}
