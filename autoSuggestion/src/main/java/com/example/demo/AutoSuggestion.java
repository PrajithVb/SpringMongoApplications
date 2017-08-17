package com.example.demo;

import java.util.HashSet;
import java.util.List;
import java.util.Scanner;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;

import com.example.connection.DBConnection;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
/**
 * 
 * @author prvb
 * 
 * Basic method for suggest the next word from db
 * 
 * Mainly useful for search API's
 * 
 * For each keystroke UI ,can call this method using API . It will results the words containing
 * the term sent.you can add all the key fields in query Criteria. I have added 3 sample fields
 *
 *
 *Have Included Input and output block for Beginners
 */
public class AutoSuggestion {
	public void getAutoSuggestion() throws Exception {
		Set<String> suggestionsBrand = new HashSet<String>();
		Set<String> suggestionsName = new HashSet<String>();
		Set<String> suggestionsCategory = new HashSet<String>();
		/*Begin INPUT*/
		Scanner sc = new Scanner(System.in);
		System.out.println("Enter your Search Term");

		String searchTerm = sc.nextLine();
		sc.close();
		
		/*End Input*/
		BasicDBObject suggestionsList = new BasicDBObject();
		
		/*Mongo DB Queries for getting  words containing search term from collection*/
		Query query = new Query();
		
		Criteria criteria2 = Criteria.where("Brand").regex(searchTerm, "i");
		Criteria criteria3 = Criteria.where("Product_Name").regex(searchTerm, "i");
		Criteria criteria1 = Criteria.where("Category").regex(searchTerm, "i");
	
		query.addCriteria(new Criteria().orOperator( criteria2, criteria3, criteria1));
		query.fields().include("Brand").include("Product_Name").include("Category");
		
		DBConnection connect = new DBConnection();
		String collectionName="Your_Collection_Name";
		
		List<DBObject> prodResultList = connect.mongoTemplate().find(query, DBObject.class,collectionName );

		/*Assigning to each results in to each map so it will be easy to display in front end*/
		
		String Brand = null;
		String name = null;
		String taxonomy_name = null;
		for (DBObject dbObject : prodResultList) {
			name = (String) dbObject.get("Product_Name");
			if (null != name && StringUtils.containsIgnoreCase(name, searchTerm)) {
				suggestionsName.add(name);
			}
			Brand = (String) dbObject.get("Brand");
			if (null != Brand && StringUtils.containsIgnoreCase(Brand, searchTerm)) {
				suggestionsBrand.add(Brand);
			}
			
			taxonomy_name = (String) dbObject.get("Category");
			if (null != taxonomy_name && StringUtils.containsIgnoreCase(taxonomy_name, searchTerm)) {
				suggestionsCategory.add(taxonomy_name);
			}

		
			suggestionsList.put("Product", suggestionsName);
			suggestionsList.put("Category", suggestionsCategory);
			suggestionsList.put("Brand", suggestionsBrand);
			
			

		}
		/* O/P */
		System.out.println(suggestionsList.toString());
	}

	public static void main(String[] args) throws Exception {

		AutoSuggestion test = new AutoSuggestion();
		test.getAutoSuggestion();
	}
}
