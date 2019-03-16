package com.info7255.controller;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

import com.info7255.beans.JedisBean;
import com.info7255.beans.JSONValidator;

@RestController
public class SchemaController {
	
	@Autowired
	private JSONValidator validator;
	
	@Autowired
	private JedisBean jedisBean;
	
	Map<String, String> m = new HashMap<String, String>();
	
	@PostMapping("/plan/schema")
	public  ResponseEntity<Map<String, String>> insertSchema(@RequestBody(required=true) String body, @RequestHeader HttpHeaders requestHeaders) {
		
		m.clear();
		
		if (!new HomeController().authorize(requestHeaders)) {
			m.put("message", "Authorization failed");
			return new ResponseEntity<Map<String, String>>(m, HttpStatus.UNAUTHORIZED);
		}
		
		if(body == null) {
			m.put("message","Schema not recieved");
			return new ResponseEntity<>(m, new HttpHeaders(), HttpStatus.BAD_REQUEST);
		}
		// receive token and validate
		
		// set json schema in redis
		if(!jedisBean.insertSchema(body)) {
			m.put("message","Schema insertion failed");
			return new ResponseEntity<>(m, new HttpHeaders(), HttpStatus.BAD_REQUEST);
		}
		validator.refreshSchema();
		m.put("message","Schema posted successfully");
		return new ResponseEntity<>(m, new HttpHeaders(), HttpStatus.CREATED);
	}
	

}
