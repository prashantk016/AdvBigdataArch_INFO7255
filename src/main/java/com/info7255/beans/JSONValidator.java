package com.info7255.beans;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

import org.everit.json.schema.Schema;
import org.everit.json.schema.ValidationException;
import org.everit.json.schema.loader.SchemaLoader;
import org.json.JSONObject;
import org.json.JSONTokener;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

public class JSONValidator {
	
	private Schema schema;
	private JedisBean jedisBean;
	
	// constructor
	public JSONValidator(JedisBean j) {
		jedisBean = j;
	}
	
	// get schema
	public Schema getSchema() {
		if(schema != null)
			return schema;
		
		refreshSchema();
		return schema;
	}
	
	// refresh schema
	public void refreshSchema() {
		String schemaString = jedisBean.getSchema();
		schema = SchemaLoader.load(new JSONObject(new JSONTokener(new ByteArrayInputStream(schemaString.getBytes()))));
	}
	
	
	// get JSONObject from String
	public JSONObject getJsonObjectFromString(String jsonString) {
		return new JSONObject(jsonString);
	}
	
	// validate jsonObject against schema
	public boolean validate(JSONObject jsonObject) {
		try {
			schema.validate(jsonObject);
			System.out.println("Validation success");
			return true;
		}
		catch (ValidationException e) {
			e.printStackTrace();
			return false;
		}
	}

}
