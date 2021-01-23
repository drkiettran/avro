package com.drkiettran.avro;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.avro.generic.GenericRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

@RestController
public class UserController {
	private static Logger logger = LoggerFactory.getLogger(UserController.class);
	@Autowired
	private AvroHelper avroHelper;

	/**
	 * Good post:
	 * 
	 * <pre>
	 * {@code
	 	curl --location --request POST 'localhost:8080/users' \
	 	--header 'Content-Type: application/json' \
		--header 'Accept: application/json' \
		--data-raw '
			{ 
	    		"users" : [{
	        	"name": "Last, First",
	        	"favorite_number": 12345,
	        	"favorite_color": "Red, White and Blue"
	    	},
	    	{
	        	"name": "Doe, John",
	        	"favorite_number": 23456,
	        	"favorite_color": "Red, White and Blue"
	    	}]
		}'
	 * }
	 * </pre>
	 * 
	 * @param req
	 * @param resp
	 * @param body
	 * @param contentType
	 * @param accept
	 * @return
	 * @throws IOException
	 */
	@PostMapping(value = "/users")
	public ResponseEntity<Users> addUsers(HttpServletRequest req, HttpServletResponse resp, @RequestBody String body,
			@RequestHeader(value = "Content-Type") String contentType, @RequestHeader(value = "Accept") String accept)
			throws IOException {
		logger.info("content-type: {}", contentType);
		logger.info("accept: {}", accept);
		if (!verifyHeaders(contentType, accept)) {
			Users users = getHeaderErrorMessage();
			return ResponseEntity.badRequest().body(users);
		}
		Users users = getUsers(body);
		Users storedUsers = storeUsers(users);
		storedUsers.setErrorMessage(String.format("%d users stored!", storedUsers.getUsers().size()));
		storedUsers.setResultCode("OK");
		return ResponseEntity.ok(storedUsers);

	}

	/**
	 * Example of a good get:
	 * 
	 * <pre>
	 * {@code
	 * 	curl --location --request GET 'localhost:8080/users' \
	 * 	--header 'Accept: application/json' \
	 * 	--header 'Content-Type: application/json'
	 * }
	 * </pre>
	 * 
	 * @param req
	 * @param resp
	 * @param contentType
	 * @param accept
	 * @return
	 * @throws IOException
	 */
	@GetMapping(value = "/users")
	public ResponseEntity<Users> getUsers(HttpServletRequest req, HttpServletResponse resp,
			@RequestHeader(value = "Content-Type") String contentType, @RequestHeader(value = "Accept") String accept)
			throws Exception {
		logger.info("content-type: {}", contentType);
		logger.info("accept: {}", accept);
		if (!verifyHeaders(contentType, accept)) {
			Users users = getHeaderErrorMessage();
			return ResponseEntity.badRequest().body(users);
		}
		Users users = avroHelper.getUsers();
		users.setErrorMessage(String.format("%d users found", users.getUsers().size()));
		users.setResultCode("OK");

		return ResponseEntity.ok(users);
	}

	/**
	 * Good get user:
	 * 
	 * <pre>
	 * {@code
	 	curl --location --request GET 'localhost:8080/user?user=Last,%20First' \
		--header 'Content-Type: application/json' \
		--header 'Accept: application/json'
	 *}
	 * </pre>
	 * 
	 * @param req
	 * @param resp
	 * @param user
	 * @param contentType
	 * @param accept
	 * @return
	 * @throws Exception
	 */
	@GetMapping("/user")
	private ResponseEntity<Users> getUser(HttpServletRequest req, HttpServletResponse resp,
			@RequestParam(value = "user") String user, @RequestHeader(value = "Content-Type") String contentType,
			@RequestHeader(value = "Accept") String accept) throws Exception {
		logger.info("content-type: {}", contentType);
		logger.info("accept: {}", accept);
		if (!verifyHeaders(contentType, accept)) {
			Users users = getHeaderErrorMessage();
			return ResponseEntity.badRequest().body(users);
		}
		Users users = avroHelper.getUser(user);
		users.setErrorMessage(String.format("%d users found", users.getUsers().size()));
		users.setResultCode("OK");

		return ResponseEntity.ok(users);

	}

	private Users getHeaderErrorMessage() {
		logger.error("errors in headers");
		Users users = new Users();
		users.setResultCode("Error");
		users.setErrorMessage("errors in headers");
		return users;
	}

	private Users storeUsers(Users users) throws IOException {
		Users storedUsers = new Users();
		for (User user : users.getUsers()) {
			GenericRecord rec = avroHelper.serializeUser(user);
			if (rec != null) {
				storedUsers.getUsers().add(user);
			}
		}

		return storedUsers;
	}

	private Users getUsers(String body) throws JsonParseException, JsonMappingException, IOException {

		logger.info("body: {}", body);
		ObjectMapper om = new ObjectMapper();
		return om.readValue(body.getBytes(), Users.class);
	}

	private Boolean verifyHeaders(String contentType, String accept) {
		return contentType.equals(MediaType.APPLICATION_JSON.toString())
				&& accept.equals(MediaType.APPLICATION_JSON.toString());
	}

}
