package com.example.connection;

import java.net.UnknownHostException;

import org.springframework.context.annotation.Bean;
import org.springframework.data.mongodb.MongoDbFactory;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.SimpleMongoDbFactory;
import org.springframework.data.mongodb.core.convert.DefaultDbRefResolver;
import org.springframework.data.mongodb.core.convert.DefaultMongoTypeMapper;
import org.springframework.data.mongodb.core.convert.MappingMongoConverter;
import org.springframework.data.mongodb.core.mapping.MongoMappingContext;

import com.mongodb.DB;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import com.mongodb.MongoException;

public class DBConnection {
/**
 
  *Basic Mongo DB Connection method. MongoTempplate also defined here.
  *
  *
  *
  *
  **/

	private String mongoDBNm = "<----Your DbName----->";

	private String mongoURI = "<-----Your mongo URI-------->";

	public MongoClient mongoClient = null;

	public DB connectToMongo() throws UnknownHostException {
		try {
			if (null == mongoClient) {
				MongoClientURI uri = new MongoClientURI(mongoURI);
				mongoClient = new MongoClient(uri);
				DB db = mongoClient.getDB(mongoDBNm);
				return db;

			} else {
				return mongoClient.getDB(mongoDBNm);
			}
		} catch (NumberFormatException e) {
			e.printStackTrace();
		}

		return null;
	}

	@Bean
	public MongoDbFactory mongoDbFactory() throws MongoException, UnknownHostException {

		MongoClientURI uri = new MongoClientURI(mongoURI);
		MongoClient mongoClient = null;
		mongoClient = new MongoClient(uri);

		return new SimpleMongoDbFactory(mongoClient, mongoDBNm);
	}

	@Bean
	public MongoTemplate mongoTemplate() throws Exception {
		MappingMongoConverter converter = new MappingMongoConverter(new DefaultDbRefResolver(mongoDbFactory()),
				new MongoMappingContext());
		converter.setTypeMapper(new DefaultMongoTypeMapper(null));

		return new MongoTemplate(mongoDbFactory(), converter);
	}

}
