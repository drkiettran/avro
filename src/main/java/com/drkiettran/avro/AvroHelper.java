package com.drkiettran.avro;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.avro.Schema;
import org.apache.avro.file.DataFileReader;
import org.apache.avro.file.DataFileWriter;
import org.apache.avro.generic.GenericData;
import org.apache.avro.generic.GenericDatumReader;
import org.apache.avro.generic.GenericDatumWriter;
import org.apache.avro.generic.GenericRecord;
import org.apache.avro.io.DatumReader;
import org.apache.avro.io.DatumWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class AvroHelper {
	private Logger logger = LoggerFactory.getLogger(AvroHelper.class);

	private HashMap<String, User> users = new HashMap<String, User>();

	private Schema schema = new Schema.Parser().parse(new File("user.avsc"));
	private File file = new File("users.avro");
	private DatumWriter<GenericRecord> datumWriter = new GenericDatumWriter<GenericRecord>(schema);
	private DataFileWriter<GenericRecord> dataFileWriter = new DataFileWriter<GenericRecord>(datumWriter);

	public AvroHelper() throws IOException {
		logger.info("Constructing ...");
		if (!file.exists()) {
			dataFileWriter.create(schema, file);
		} else {
			DatumReader<GenericRecord> datumReader = new GenericDatumReader<GenericRecord>(schema);
			DataFileReader<GenericRecord> dataFileReader = new DataFileReader<GenericRecord>(file, datumReader);
			dataFileReader.forEach(rec -> {
				User user = deserializeUser(rec);
				logger.info("user: {}", user.getName());
				users.put(user.getName(), user);
			});
			dataFileReader.close();
			dataFileWriter.appendTo(file);
		}
	}

	private User deserializeUser(GenericRecord rec) {
		User user = new User();
		user.setName(String.valueOf(rec.get("name")));
		user.setFavoriteNumber(Integer.valueOf(String.valueOf(rec.get("favorite_number"))));
		user.setFavoriteColor(String.valueOf(rec.get("favorite_color")));
		return user;
	}

	public GenericRecord serializeUser(User user) throws IOException {
		if (exists(user)) {
			logger.info("{} exists!", user.getName());
			return null;
		}
		logger.info("storing user: {}", user.getName());
		GenericRecord rec = new GenericData.Record(schema);
		rec.put("name", user.getName());
		rec.put("favorite_number", user.getFavoriteNumber());
		rec.put("favorite_color", user.getFavoriteColor());
		dataFileWriter.append(rec);
		dataFileWriter.flush();
		return rec;
	}

	private Boolean exists(User user) {
		if (users.get(user.getName()) == null) {
			users.put(user.getName(), user);
			return false;
		}
		return true;
	}

	public Users getUsers() {
		Users users = new Users();
		this.users.forEach((k, v) -> {
			users.getUsers().add(v);
		});

		return users;
	}

	public Users getUser(String userPattern) {
		logger.info("userPattern: {}", userPattern);
		Users users = new Users();
		Pattern r = Pattern.compile(userPattern);
		this.users.forEach((k, v) -> {
			User user = (User) v;

			logger.info("username: {}", user.getName());
			Matcher m = r.matcher(k);
			if (m.find()) {
				users.getUsers().add(this.users.get(k));
			}
		});
		return users;
	}
}
