package com.info7255.controller;

import java.util.HashMap;
import java.util.Map;

import javax.ws.rs.NotFoundException;
import javax.ws.rs.core.MediaType;

import org.springframework.data.redis.RedisConnectionFailureException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import com.info7255.exception.ApiResouceAlreadyExistException;
import com.info7255.exception.ApiResouceNotFoundException;
import com.info7255.exception.JsonBuildException;
import com.info7255.exception.JsonValidationException;

@ControllerAdvice
@RequestMapping(produces = MediaType.APPLICATION_JSON)
public class HomeControllerAdvice extends ResponseEntityExceptionHandler {

	Map<String, String> m = new HashMap<String, String>();

	@ExceptionHandler(NotFoundException.class)
	public ResponseEntity<Map<String, String>> assertionException(final NotFoundException e) {
		m.clear();
		m.put("error", e.getMessage());
		return new ResponseEntity<>(m, HttpStatus.NOT_FOUND);
	}

	@ExceptionHandler(RedisConnectionFailureException.class)
	public ResponseEntity<Map<String, String>> assertionException(final RedisConnectionFailureException e) {
		m.clear();
		m.put("error", e.getMessage());
		return new ResponseEntity<>(m, HttpStatus.INTERNAL_SERVER_ERROR);
	}

	@ExceptionHandler(IllegalArgumentException.class)
	public ResponseEntity<Map<String, String>> assertionException(final IllegalArgumentException e) {
		m.clear();
		m.put("error", e.getMessage());
		return new ResponseEntity<>(m, HttpStatus.BAD_REQUEST);
	}

	@ExceptionHandler(value = { ApiResouceNotFoundException.class })
	protected ResponseEntity<Object> handleResouceNotFound(RuntimeException ex, WebRequest request) {
		String bodyOfResponse = ex.getMessage();
		return handleExceptionInternal(ex, bodyOfResponse, new HttpHeaders(), HttpStatus.NOT_FOUND, request);
	}

	@ExceptionHandler(value = { JsonValidationException.class, ApiResouceAlreadyExistException.class })
	protected ResponseEntity<Object> handleBadRequest(RuntimeException ex, WebRequest request) {
		String bodyOfResponse = ex.getMessage();
		return handleExceptionInternal(ex, bodyOfResponse, new HttpHeaders(), HttpStatus.BAD_REQUEST, request);
	}

	@ExceptionHandler(value = { JsonBuildException.class })
	protected ResponseEntity<Object> handleInternalServerError(RuntimeException ex, WebRequest request) {
		String bodyOfResponse = ex.getMessage();
		return handleExceptionInternal(ex, bodyOfResponse, new HttpHeaders(), HttpStatus.INTERNAL_SERVER_ERROR,
				request);
	}

}
