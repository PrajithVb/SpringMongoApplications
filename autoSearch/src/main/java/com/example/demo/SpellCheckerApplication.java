package com.example.demo;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.TextCriteria;
import org.springframework.data.mongodb.core.query.TextQuery;

import com.example.connection.DBConnection;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import com.swabunga.spell.engine.SpellDictionaryHashMap;
import com.swabunga.spell.engine.Word;
import com.swabunga.spell.event.SpellCheckEvent;
import com.swabunga.spell.event.SpellCheckListener;
import com.swabunga.spell.event.SpellChecker;
import com.swabunga.spell.event.StringWordTokenizer;
import com.swabunga.spell.event.TeXWordFinder;




@SpringBootApplication
public class SpellCheckerApplication implements SpellCheckListener {

	/**
	 * 
	 * Simple Spring Application for auto search
	 * 
	 * It will replace the misspelled word and put meaningful word instead and search against mongodb
	 * 
	 **/
	
	
	
	private SpellChecker spellChecker;
	private List<String> misspelledWords;
	private List<String> suggestionsOpt;
	private int options = 0;

	/**
	 * get a list of misspelled words from the text
	 * 
	 * @param text
	 */
	public List<String> getMisspelledWords(String text) {
		StringWordTokenizer texTok = new StringWordTokenizer(text, new TeXWordFinder());
		spellChecker.checkSpelling(texTok);
		return misspelledWords;
	}

	private static SpellDictionaryHashMap dictionaryHashMap;
	{
		Resource resource = new ClassPathResource("dictionary");

		File dict = null;
		try {
			dict = resource.getFile();
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		try {
			dictionaryHashMap = new SpellDictionaryHashMap(dict);
		} catch (FileNotFoundException e) {

			e.printStackTrace();
		} catch (IOException e) {

			e.printStackTrace();
		}
	}

	private void initialize() {
		spellChecker = new SpellChecker(dictionaryHashMap);
		spellChecker.addSpellCheckListener(this);
	}

	public SpellCheckerApplication() {

		misspelledWords = new ArrayList<String>();
		initialize();
	}

	/**
	 * correct the misspelled words in the input string and return the result
	 */
	public String getCorrectedLine(String line) {
		List<String> misSpelledWords = getMisspelledWords(line);
		
		int suggSize = 0;
		for (String misSpelledWord : misSpelledWords) {
			suggestionsOpt = getSuggestions(misSpelledWord);
			suggSize = suggestionsOpt.size();
			if (suggSize == 0)
				continue;

			if (options >= suggSize) {
				return null;
			}

			String bestSuggestion = suggestionsOpt.get(options);

			line = line.replace(misSpelledWord, bestSuggestion);
		}

		options++;
		return line;
	}

	public List<String> getSuggestions(String misspelledWord) {

		@SuppressWarnings("unchecked")
		List<Word> tempSugg = spellChecker.getSuggestions(misspelledWord, 0);
		List<String> suggestions = new ArrayList<String>();
		for (Word suggestion : tempSugg) {

			suggestions.add(suggestion.getWord());
		}

		return suggestions;
	}

	@Override
	public void spellingError(SpellCheckEvent event) {
		event.ignoreWord(true);
		misspelledWords.add(event.getInvalidWord());
	}

	public void dbOperation() throws Exception {
		SpellCheckerApplication jazzySpellChecker = new SpellCheckerApplication();
		List<String> result = new ArrayList<String>();
		
		/*Input block */
		Scanner sc = new Scanner(System.in);
		System.out.println("Enter your Search Term");

		String searchTerm = sc.nextLine();
		String line = searchTerm;
		sc.close();
		/*End */
		List<DBObject> productList = new ArrayList<DBObject>();
		do {
			/**
			 *  Putting "/* " make the search more meaningful as it will search whole the line you are passing 
			 *  
			 *  if you search word "blue shirt" without '/*'mongdb will search for blue or shirt
			 */

			TextCriteria criteria = TextCriteria.forDefaultLanguage().matchingAny("\"" + line + "\"");

			Query query = TextQuery.queryText(criteria).sortByScore().with(new PageRequest(0, 5));
			DBConnection connect = new DBConnection();
			String collectionName= "Your_Collection_Name ";
			productList = connect.mongoTemplate().find(query, DBObject.class, collectionName);
			int count = productList.size();
			BasicDBObject resultObj = new BasicDBObject();
			resultObj.put("SearchTerm", line);
			resultObj.put("Result", productList);
			resultObj.put("Count", count);
			result.add(resultObj.toString());

			line = jazzySpellChecker.getCorrectedLine(searchTerm);

		} while (productList.size() == 0 && line != null);

		System.out.println(result);
	}

	public static void main(String[] args) throws Exception {

		SpellCheckerApplication test = new SpellCheckerApplication();
		test.dbOperation();
	}

}
