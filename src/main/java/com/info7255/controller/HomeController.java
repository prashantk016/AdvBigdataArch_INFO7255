package com.info7255.controller;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Base64;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import org.everit.json.schema.Schema;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.info7255.beans.JedisBean;
import com.info7255.beans.JSONValidator;

@RestController
public class HomeController {
	
	@Autowired
	private JSONValidator validator;
	@Autowired
	private JedisBean jedisBean;
	
	private String key = "qwertyuiopasdfghjklzxcvbnm";
	private String algorithm = "DESede";
	
	@RequestMapping("/")
	public String home() {
		return "Welcome!";
	}

	
	@GetMapping("/plan/{id}")
	public ResponseEntity<String> read(@PathVariable(name="id", required=true) String id, @RequestHeader HttpHeaders requestHeaders) {
		
		if(!authorize(requestHeaders)) {
			return new ResponseEntity<String>("Token authorization failed", HttpStatus.NOT_ACCEPTABLE);
		}
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		List<String> asd=requestHeaders.getIfNoneMatch();
		if(requestHeaders.getIfNoneMatch().isEmpty() || !requestHeaders.getIfNoneMatch().contains("\"04cb89f6589499c5c86aee10f4bbb287e\""))
		{
			System.out.println("Reading");
			String jsonString = jedisBean.read(id);
			if(jsonString != null)
				return new ResponseEntity<String>(jsonString, headers, HttpStatus.OK);
			else
				return new ResponseEntity<String>("Read unsuccessfull", headers, HttpStatus.BAD_REQUEST);}
		return new ResponseEntity<String>("Read unsuccessfull", headers, HttpStatus.NOT_MODIFIED);

	}
	
	
	@PostMapping("/plan")
	public ResponseEntity<String> insert(@RequestBody(required=true) String body, @RequestHeader HttpHeaders requestHeaders) {
		
		if(!authorize(requestHeaders)) {
			return new ResponseEntity<String>("Token authorization failed", HttpStatus.NOT_ACCEPTABLE);
		}
		
		Schema schema = validator.getSchema();
		if(schema == null)
			return new ResponseEntity<String>("schema file not found exception", HttpStatus.BAD_REQUEST);
		
		JSONObject jsonObject = validator.getJsonObjectFromString(body);
		
		if(validator.validate(jsonObject)) {
			String uuid = jedisBean.insert(jsonObject);
			return new ResponseEntity<String>("Inserted with id "+uuid, HttpStatus.ACCEPTED);
		}
		else {
			return new ResponseEntity<String>("invalid", HttpStatus.BAD_REQUEST);
		}
			
	}
	

	@DeleteMapping("/plan")
	public ResponseEntity<String> delete(@RequestBody(required=true) String body, @RequestHeader HttpHeaders requestHeaders) {
		
		if(!authorize(requestHeaders)) {
			return new ResponseEntity<String>("Token authorization failed", HttpStatus.NOT_ACCEPTABLE);
		}
		
		if (jedisBean.delete(body)) {
			return new ResponseEntity<String>("Deleted successfully", HttpStatus.ACCEPTED);
		}
		else
			return new ResponseEntity<String>("Deletion unsuccessfull", HttpStatus.BAD_REQUEST);
	}
	
	@PutMapping("/plan")
	public ResponseEntity<String> update(@RequestBody(required=true) String body, @RequestHeader HttpHeaders headers) {
		
		if(!authorize(headers)) {
			System.out.println("Authorization passed");
			return new ResponseEntity<String>("Token authorization failed", HttpStatus.NOT_ACCEPTABLE);
		}
		
		Schema schema = validator.getSchema();
		if(schema == null)
			return new ResponseEntity<String>("schema file not found exception", HttpStatus.BAD_REQUEST);
		
		System.out.println("Schema retreived succesfully");
		JSONObject jsonObject = validator.getJsonObjectFromString(body);
		
		if(!jedisBean.update(jsonObject))
			return new ResponseEntity<String>("Failed to update JSON instance in Redis", HttpStatus.BAD_REQUEST);
		
		System.out.println("");
		return new ResponseEntity<String>("JSON instance updated in redis", HttpStatus.ACCEPTED);
	
	}
	
	@GetMapping("/token")
	public ResponseEntity<String> createToken() {
		
		JSONObject jsonToken = new JSONObject ();
		jsonToken.put("Issuer", "Prashant");
		
		TimeZone tz = TimeZone.getTimeZone("UTC");
		DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm'Z'"); 
		df.setTimeZone(tz);
		
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(new Date());            
		calendar.add(Calendar.MINUTE, 30);
		Date date = calendar.getTime();		

		
		jsonToken.put("expiry", df.format(date));
		String token = jsonToken.toString();
		System.out.println(token);
		
		SecretKey spec = loadKey();
		
		try {
			Cipher c = Cipher.getInstance(algorithm);
			c.init(Cipher.ENCRYPT_MODE, spec);
			byte[] encrBytes = c.doFinal(token.getBytes());
			String encoded = Base64.getEncoder().encodeToString(encrBytes);
			return new ResponseEntity<String>(encoded, HttpStatus.ACCEPTED);
			
		} catch (Exception e) {
			e.printStackTrace();
			return new ResponseEntity<String>("Token creation failed", HttpStatus.NOT_ACCEPTABLE);
		}
		
	}
	
	private SecretKey loadKey() {
		return new SecretKeySpec(key.getBytes(), algorithm);
	}
	
	private boolean authorize(HttpHeaders headers) {
		
		String token = headers.getFirst("Authorization").substring(7);
		byte[] decrToken = Base64.getDecoder().decode(token);
		SecretKey spec = loadKey();
		try {
			Cipher c = Cipher.getInstance(algorithm);
			c.init(Cipher.DECRYPT_MODE, spec);
			String tokenString = new String(c.doFinal(decrToken));
			JSONObject jsonToken = new JSONObject(tokenString);
			System.out.println(tokenString);
			System.out.println("Inside authorize");
			System.out.println(jsonToken.toString());
			
			String ttldateAsString = jsonToken.get("expiry").toString();
			Date currentDate = Calendar.getInstance().getTime();
			
			TimeZone tz = TimeZone.getTimeZone("UTC");
			DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm'Z'");
			formatter.setTimeZone(tz);
			
			Date ttlDate = formatter.parse(ttldateAsString);
			currentDate = formatter.parse(formatter.format(currentDate));
			
			if(currentDate.after(ttlDate)) {
				return false;
			}
			
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}


}
