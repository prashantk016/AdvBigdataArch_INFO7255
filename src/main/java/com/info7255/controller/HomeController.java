package com.info7255.controller;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Base64;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
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

import com.info7255.beans.EtagManager;
import com.info7255.beans.JSONValidator;
import com.info7255.beans.JedisBean;

@RestController
public class HomeController {

	@Autowired
	private JSONValidator validator;
	@Autowired
	private JedisBean jedisBean;

	@Autowired
	private EtagManager etagManager;

	private String key = "ssdkF$HUy2A#D%kd";
	private String algorithm = "AES";

	Map<String, Object> m = new HashMap<String, Object>();

	@RequestMapping("/")
	public String home() {
		return "Welcome!";
		
		
	}

	@GetMapping(value = "/plan/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<Map<String, Object>> read(@PathVariable(name = "id", required = true) String id,
			@RequestHeader HttpHeaders requestHeaders) {
		m.clear();
		if (!authorize(requestHeaders)) {
			m.put("message", "Authorization failed");
			return new ResponseEntity<Map<String, Object>>(m, HttpStatus.UNAUTHORIZED);
		}

		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);

		JSONObject jsonString = jedisBean.read(id);
		if (jsonString != null) {
			String etag = etagManager.getETag(jsonString);
			if (!etagManager.verifyETag(jsonString, requestHeaders.getIfNoneMatch())) {
				headers.setETag(etag);
				return new ResponseEntity<Map<String, Object>>(jsonString.toMap(), headers, HttpStatus.OK);
			} else {
				headers.setETag(etag);
				return new ResponseEntity<Map<String, Object>>(m, headers, HttpStatus.NOT_MODIFIED);
			}
		} else {
			m.put("message", "Read unsuccessful. Invalid Id.");
			return new ResponseEntity<>(m, headers, HttpStatus.NOT_FOUND);
		}

	}

	@PostMapping(value = "/plan", produces = MediaType.APPLICATION_JSON_VALUE, consumes = "application/json")
	public ResponseEntity<Map<String, Object>> insert(@RequestBody(required = true) String body,
			@RequestHeader HttpHeaders requestHeaders) {
		m.clear();
		if (!authorize(requestHeaders)) {
			m.put("message", "Authorization failed");
			return new ResponseEntity<Map<String, Object>>(m, HttpStatus.UNAUTHORIZED);
		}

		Schema schema = validator.getSchema();
		if (schema == null) {
			m.put("message", "No Schema found");
			return new ResponseEntity<Map<String, Object>>(m, HttpStatus.NOT_FOUND);
		}
		
		JSONObject jsonObject = validator.getJsonObjectFromString(body);

		if (validator.validate(jsonObject)) {
			String uuid = jedisBean.insert(jsonObject);
			m.put("message", "Added successfully");
			m.put("id", uuid);
			return new ResponseEntity<Map<String, Object>>(m, HttpStatus.CREATED);
		} else {
			m.put("message", "Validation failed");
			return new ResponseEntity<Map<String, Object>>(m, HttpStatus.BAD_REQUEST);
		}

	}

	@DeleteMapping(value = "/plan", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<Map<String, Object>> delete(@RequestBody(required = true) String body,
			@RequestHeader HttpHeaders requestHeaders) {
		m.clear();
		if (!authorize(requestHeaders)) {
			m.put("message", "Authorization failed");
			return new ResponseEntity<Map<String, Object>>(m, HttpStatus.UNAUTHORIZED);
		}
		
		if (jedisBean.delete(body)) {
			m.put("message", "Deleted successfully");
			return new ResponseEntity<Map<String, Object>>(m, HttpStatus.NO_CONTENT);
		} else {
			m.put("message", "Delete failed");
			return new ResponseEntity<Map<String, Object>>(m, HttpStatus.BAD_REQUEST);
		}
	}

	@PutMapping(value = "/plan", produces = MediaType.APPLICATION_JSON_VALUE, consumes = "application/json")
	public ResponseEntity<Map<String, Object>> update(@RequestBody(required = true) String body,
			@RequestHeader HttpHeaders requestHeaders) {
		m.clear();
		if (!authorize(requestHeaders)) {
			m.put("message", "Authorization failed");
			return new ResponseEntity<Map<String, Object>>(m, HttpStatus.UNAUTHORIZED);
		}

		Schema schema = validator.getSchema();
		if (schema == null) {	
			m.put("message", "No schema found!");
			return new ResponseEntity<Map<String, Object>>(m, HttpStatus.NOT_FOUND);

		}
		
		
		JSONObject jsonObject = validator.getJsonObjectFromString(body);

		HttpHeaders responseHeaders = new HttpHeaders();
		responseHeaders.setContentType(MediaType.APPLICATION_JSON);
		
		JSONObject planJSON=jedisBean.read(jsonObject.getString("PlanID"));
		if(planJSON!=null)
		{
			String etag = etagManager.getETag(planJSON);
			if (etagManager.verifyETag(planJSON, requestHeaders.getIfMatch())) {
			
				if (!jedisBean.update(jsonObject)) {
					m.put("message", "Update failed");
					return new ResponseEntity<Map<String, Object>>(m, HttpStatus.BAD_REQUEST);
				}
				String newETag=etagManager.getETag(jedisBean.read(jsonObject.getString("PlanID")));
				responseHeaders.setETag(newETag);
				return new ResponseEntity<Map<String, Object>>(m, responseHeaders, HttpStatus.NO_CONTENT);
			} else {
				if(requestHeaders.getIfMatch().isEmpty()) {
					m.put("message","If-Match ETag required");
					return new ResponseEntity<Map<String, Object>>(m, responseHeaders, HttpStatus.PRECONDITION_REQUIRED);
				}
				else {
					responseHeaders.setETag(etag);
					return new ResponseEntity<Map<String, Object>>(m, responseHeaders, HttpStatus.PRECONDITION_FAILED);
			
				}
			}
		}
		else {

		m.put("message", "Invalid Plan Id");
		return new ResponseEntity<Map<String, Object>>(m, HttpStatus.BAD_REQUEST);
		}
	}

	@GetMapping(value = "/token", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<Map<String, Object>> createToken() {
		m.clear();
		JSONObject jsonToken = new JSONObject();
		jsonToken.put("Issuer", "Prashant");

		TimeZone tz = TimeZone.getTimeZone("UTC");
		// yyyy-MM-dd'T'HH:mm'Z'
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
			m.put("token", encoded);
			return new ResponseEntity<Map<String, Object>>(m, HttpStatus.ACCEPTED);

		} catch (Exception e) {
			e.printStackTrace();
			m.put("message", "Token creation failed. Please try again.");
			return new ResponseEntity<Map<String, Object>>(m, HttpStatus.UNAUTHORIZED);
		}

	}

	public boolean authorize(HttpHeaders headers) {
		if (headers.getFirst("Authorization") == null)
			return false;

		String token = headers.getFirst("Authorization").substring(7);
		byte[] decrToken = Base64.getDecoder().decode(token);
		SecretKey spec = loadKey();
		try {
			Cipher c = Cipher.getInstance(algorithm);
			c.init(Cipher.DECRYPT_MODE, spec);
			String tokenString = new String(c.doFinal(decrToken));
			JSONObject jsonToken = new JSONObject(tokenString);
			
			String ttldateAsString = jsonToken.get("expiry").toString();
			Date currentDate = Calendar.getInstance().getTime();

			TimeZone tz = TimeZone.getTimeZone("UTC");
			DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm'Z'");
			formatter.setTimeZone(tz);

			Date ttlDate = formatter.parse(ttldateAsString);
			currentDate = formatter.parse(formatter.format(currentDate));

			if (currentDate.after(ttlDate)) {
				return false;
			}

		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}
	
	private SecretKey loadKey() {
		return new SecretKeySpec(key.getBytes(), algorithm);
	}

}
