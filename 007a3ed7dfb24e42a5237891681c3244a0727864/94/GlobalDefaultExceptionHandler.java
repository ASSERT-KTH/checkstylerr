package com.ctrip.framework.apollo.common.controller;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.HttpMediaTypeException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.client.HttpStatusCodeException;

import com.ctrip.framework.apollo.core.exception.AbstractBaseException;
import com.ctrip.framework.apollo.core.exception.BadRequestException;
import com.ctrip.framework.apollo.core.exception.NotFoundException;
import com.dianping.cat.Cat;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;

import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;
import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.http.MediaType.APPLICATION_JSON;

@ControllerAdvice
public class GlobalDefaultExceptionHandler {

  private Gson gson = new Gson();

  private static Type mapType = new TypeToken<Map<String, Object>>() {}.getType();

  @ExceptionHandler(Exception.class)
  public ResponseEntity<Map<String, Object>> exception(HttpServletRequest request, Exception ex) {
    return handleError(request, INTERNAL_SERVER_ERROR, ex);
  }

  private ResponseEntity<Map<String, Object>> handleError(HttpServletRequest request,
      HttpStatus status, Throwable ex) {
    return handleError(request, status, ex, ex.getMessage());
  }

  private ResponseEntity<Map<String, Object>> handleError(HttpServletRequest request,
      HttpStatus status, Throwable ex, String message) {
    ex = resolveError(ex);
    if (ex.getCause() instanceof HttpStatusCodeException) {
      return restTemplateException(request, (HttpStatusCodeException) ex.getCause());
    }

    Cat.logError(ex);
    
    Map<String, Object> errorAttributes = new LinkedHashMap<>();
    errorAttributes.put("status", status.value());
    errorAttributes.put("message", message);
    errorAttributes.put("timestamp",
        LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
    errorAttributes.put("exception", resolveError(ex).getClass().getName());
    errorAttributes.put("stackTrace", ex.getStackTrace());
    if (ex instanceof AbstractBaseException) {
      errorAttributes.put("errorCode", ((AbstractBaseException) ex).getErrorCode());
    }
    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(APPLICATION_JSON);
    return new ResponseEntity<>(errorAttributes, headers, status);
  }

  @ExceptionHandler({HttpRequestMethodNotSupportedException.class, HttpMediaTypeException.class})
  public ResponseEntity<Map<String, Object>> methodNotSupportedException(HttpServletRequest request,
      ServletException ex) {
    return handleError(request, BAD_REQUEST, ex);
  }

  @ExceptionHandler(NotFoundException.class)
  public ResponseEntity<Map<String, Object>> notFound(HttpServletRequest request,
      NotFoundException ex) {
    return handleError(request, NOT_FOUND, ex);
  }

  @ExceptionHandler(BadRequestException.class)
  public ResponseEntity<Map<String, Object>> badRequest(HttpServletRequest request,
      BadRequestException ex) {
    return handleError(request, BAD_REQUEST, ex);
  }

  @ExceptionHandler(HttpStatusCodeException.class)
  public ResponseEntity<Map<String, Object>> restTemplateException(HttpServletRequest request,
      HttpStatusCodeException ex) {
    Map<String, Object> errorAttributes = gson.fromJson(ex.getResponseBodyAsString(), mapType);
    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(APPLICATION_JSON);
    return new ResponseEntity<>(errorAttributes, headers, ex.getStatusCode());
  }

  private Throwable resolveError(Throwable ex) {
    while (ex instanceof ServletException && ex.getCause() != null) {
      ex = ((ServletException) ex).getCause();
    }
    return ex;
  }
}
