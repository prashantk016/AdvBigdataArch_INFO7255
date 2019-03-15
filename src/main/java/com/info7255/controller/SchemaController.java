package com.info7255.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.info7255.beans.JedisBean;
import com.info7255.beans.MyJsonValidator;

@RestController
public class SchemaController {
	
	@Autowired
	private MyJsonValidator validator;
	
	@Autowired
	private JedisBean jedisBean;
	
	@PostMapping("/Plan/Schema")
	public ResponseEntity<String> insertSchema(@RequestBody(required=true) String body) {
		if(body == null) {
			return new ResponseEntity<String>("No Schema received", new HttpHeaders(), HttpStatus.BAD_REQUEST);
		}
		// receive token and validate
		
		// set json schema in redis
		if(!jedisBean.insertSchema(body))
			return new ResponseEntity<String>("Schema insertion failed", new HttpHeaders(), HttpStatus.BAD_REQUEST);
		
		validator.refreshSchema();
		return new ResponseEntity<String>("Schema posted successfully", new HttpHeaders(), HttpStatus.ACCEPTED);
	}
	

}
