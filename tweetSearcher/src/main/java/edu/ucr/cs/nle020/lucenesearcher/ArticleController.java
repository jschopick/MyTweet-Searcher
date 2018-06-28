package edu.ucr.cs.nle020.lucenesearcher;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.springframework.web.bind.annotation.*;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Scanner;

@RestController
@RequestMapping("/api")
@CrossOrigin("*")
public class ArticleController {
    static List<Article> articles;

    @GetMapping("/articles")
    public List<Article> searchArticles(
            @RequestParam(required=false, defaultValue="") String query) throws IOException, org.apache.lucene.queryparser.classic.ParseException {

        List<Article> matches = new ArrayList<>();
        matches = search(query);
        return matches;
    }
    
    public List<Article> search(String q) throws IOException, org.apache.lucene.queryparser.classic.ParseException {
		//***********************************************//
    	String fs = System.getProperty("file.separator");
		final String INDEX_DIRECTORY = "." + fs + "Index";
		//**********************************************//
		
		if(!(new File(INDEX_DIRECTORY).exists())) {
			System.out.println("Creating Index...");
			new File(INDEX_DIRECTORY).mkdirs();
			createIndex(INDEX_DIRECTORY);
		}

		Analyzer analyzer = new StandardAnalyzer();
		Directory directory = FSDirectory.open(Paths.get(INDEX_DIRECTORY));
		//Ask user for input:
		
		String finalQuery = createQuery(q);
		  
     // Now search the index:
        DirectoryReader indexReader = DirectoryReader.open(directory);
        IndexSearcher indexSearcher = new IndexSearcher(indexReader);
        // Parse a simple query :
        QueryParser lparser = new QueryParser("text", analyzer);
        
        System.out.println(finalQuery);
        Query query = lparser.parse(finalQuery);
        int topHitCount = 10;
        ScoreDoc[] hits = indexSearcher.search(query, topHitCount).scoreDocs;

        // Iterate through the results:
        String results = "";
        articles = new ArrayList<>();
        for (int rank = 0; rank < hits.length; ++rank) {
            Document hitDoc = indexSearcher.doc(hits[rank].doc);
            String sc = "";
            if(hits[rank].score > 2.5) {
            	//Integer score, String text, String username, String location, String link
            	//articles.add(new Article(rank, hitDoc.get("username"), hitDoc.get("text") + " || location: " + hitDoc.get("location") + " || on: " + hitDoc.get("createdAt")));
            	articles.add(new Article(sc.valueOf(hits[rank].score), hitDoc.get("text"), hitDoc.get("username"), hitDoc.get("location"), hitDoc.get("linkTitle"), hitDoc.get("createdAt")));
            			  //              "Class Overview, Overview of Information Retrieval and Search Engines"));
            	//results = results + (rank + 1) + " (score:" + hits[rank].score + ") --> " +
                //           hitDoc.get("text") + ", from: " + hitDoc.get("timestamp") + "\n";
            }
           // System.out.println(indexSearcher.explain(query, hits[rank].doc));
        }
        System.out.println(results);
        indexReader.close();
        directory.close();
        return articles;
	}
	
	public static String createQuery(String Query) {
		//Scanner scanner = new Scanner(System.in);
		//System.out.println("Enter Query: ");
			System.out.println(Query);
			//String Query = scanner.nextLine();
			Scanner scanner = new Scanner(Query);
			
			String q_text = "";
			String q_location = "";
			String q_hashtags = "";
			String q_username = "";
			String q_title = "";
			String raw = "";
				
			while(scanner.hasNext()) {
				raw = scanner.next();
				//Parse Hashtags
				if(raw.startsWith("_")) {
					q_hashtags = q_hashtags + raw.substring(1) + " ";
					q_text = q_text + "#" + raw.substring(1) + " ";
				}
				//Parse usernanes
				else if (raw.startsWith("@")) {
					q_username = q_username + raw.substring(1) + " ";
					q_text = q_text + raw + " ";
				}
				//Parse Locations
				else if (raw.startsWith("loc:")) {
					q_location = raw.substring(4);
					while(scanner.hasNext()) {
						raw = scanner.next();
						if(raw.startsWith("title:")) {
							q_title = raw.substring(6);
							q_title = q_title + " " + scanner.nextLine();
						}else {
							q_location = q_location + " " + raw;
						}
					}
				//Parse Titles
				}else if (raw.startsWith("title:")) {
					q_title = raw.substring(6);
					while(scanner.hasNext()) {
						raw = scanner.next();
						if(raw.startsWith("loc:")) {
							q_location = raw.substring(4);
							q_location = q_location + " " + scanner.nextLine();
						}else {
							q_title = q_title + " " + raw;
						}
					}
				}
				//Break loop if empty
				else if (raw == "") {
					break;
				}
				//Parse Text
				else{
					//System.out.println("Enter Text Parse");
					q_text = q_text + raw + " ";
				}
				
			}
			
			System.out.println("Query info: text: " + q_text + ", q_location: " + q_location + ", q_hashtags: " + q_hashtags
					+ ", qusername: " + q_username + ", qtitle: " + q_title);
		        
	        String finalQuery = "";
	        Long millis = Instant.now().toEpochMilli();
	     
	        //Since our tweets are stale, I'm basing it off of the timestamp of our newest tweet, otherwise we would use the code above
	        String curr_time_millis = "152644*******";
	        if(!q_text.equals("")) {
	        	finalQuery = finalQuery + "text:(" + q_text + ")^1.0 ";
	        }
	        if(!q_location.equals("")) {
	        	finalQuery = finalQuery + "location:(" + q_location + ")^2.0 ";
	        }
	        if(!q_hashtags.equals("")) {
	        	finalQuery = finalQuery + "hashtags:(" + q_hashtags + ")^1.5 ";
	        }
	        if(!q_username.equals("")) {
	        	finalQuery = finalQuery + "username:(" + q_username + ")^1.75 ";
	        }
	        if(!q_title.equals("")) {
	        	finalQuery = finalQuery + "linkTitle:(" + q_title + ")^1.25 ";
	        }
	        finalQuery = finalQuery + "timestamp:(" + curr_time_millis + ")^2.5";
			
	        return finalQuery;
	}
	
	public static void createIndex(String INDEX_DIRECTORY) throws IOException, org.apache.lucene.queryparser.classic.ParseException{
		JSONParser parser = new JSONParser();
		Analyzer analyzer = new StandardAnalyzer();
		
        // Store the index in memory:
        //Directory directory = new RAMDirectory();
        // To store an index on disk, use this instead:
        Directory directory = FSDirectory.open(Paths.get(INDEX_DIRECTORY));
        IndexWriterConfig config = new IndexWriterConfig(analyzer);
        IndexWriter indexWriter = new IndexWriter(directory, config);
		
        Integer file_number = 1;
        
		try
		{
			//**********************************************//
			String fs = System.getProperty("file.separator");
			String file = "." + fs + "StoredTweets" + fs + "Document" + file_number.toString() + ".json";
			//**********************************************//
			System.out.println(file);
			Path path = Paths.get(file);
			while(Files.exists(path)) {
				FileReader fileReader = new FileReader(file);
				BufferedReader bufferedReader = new BufferedReader(fileReader);
				String line;
				while((line = bufferedReader.readLine()) != null) {
					line = line.substring(1);
					if(!line.equals("")) {
						Object obj = parser.parse(line);
						JSONObject jsonObject = (JSONObject) obj;
						
						//Get Tweet ID
						String id = (String) jsonObject.get("id_str");
						
						//Get Tweet Geo-Location (Latitude and Longitude)
						JSONObject place = (JSONObject) jsonObject.get("place");
						String coord;
						String loc = "";
						if(place != null) {
							JSONObject bounding_box = (JSONObject) place.get("bounding_box");
							JSONArray coordinates = (JSONArray) bounding_box.get("coordinates");
							Iterator coordIter = coordinates.iterator();
							coord = String.valueOf(coordIter.next());
							coord = coord.substring(2,coord.indexOf("]") );
							loc = (String) place.get("full_name");
							loc = loc + ", " + ((String) place.get("country"));
						}else {
							//Accounting for extending tweets
							JSONObject coordinates = (JSONObject) jsonObject.get("coordinates");
							coord = String.valueOf(coordinates.get(coordinates));
						}
						
						
						//Get Tweet Text
						String text = (String) jsonObject.get("text");
						
						//Get Tweet timestamp
						String timestamp = (String) jsonObject.get("timestamp_ms");
						String createdAt = (String) jsonObject.get("created_at");
						
						//Get Hashtags
						JSONObject entities = (JSONObject) jsonObject.get("entities");
						JSONArray hashtags_arr = (JSONArray) entities.get("hashtags");
						Iterator hashTagIter = hashtags_arr.iterator();
						String hashtags = "";
						while (hashTagIter.hasNext()) {
							JSONObject hash_json = (JSONObject) hashTagIter.next();
					        hashtags = hashtags + ((String) hash_json.get("text"));
					    }
						
						//Get link title
						String linkTitle = (String) jsonObject.get("linkTitle");
						if (linkTitle == null) {
							linkTitle = "";
						}
						
						//Get username
						JSONObject user = (JSONObject) jsonObject.get("user");
						String username = (String) user.get("screen_name");
						
						//System.out.println("id:" + id + ", geo: [" + coord + "]"
						//		+ ", text: " + text + ", timestamp: " + timestamp + ",hashtags: " + hashtags
						//		+ ", username: " + username + ", linkTitle: " + linkTitle);
						
						//Tweet twitterDoc = new Tweet(id, coord, text, timestamp, createdAt, hashtags, username, linkTitle);
						
						Document doc = new Document();
						doc.add(new Field("id", id, TextField.TYPE_STORED));
						doc.add(new Field("coord", coord, TextField.TYPE_STORED));
						doc.add(new Field("location", loc, TextField.TYPE_STORED));
						doc.add(new Field("text", text, TextField.TYPE_STORED));
						doc.add(new Field("timestamp", timestamp, TextField.TYPE_STORED));
						doc.add(new Field("createdAt", createdAt, TextField.TYPE_STORED));
						doc.add(new Field("hashtags", hashtags, TextField.TYPE_STORED));
						doc.add(new Field("username", username, TextField.TYPE_STORED));
						doc.add(new Field("linkTitle", linkTitle, TextField.TYPE_STORED));
						
						indexWriter.addDocument(doc);
					}
				}
				bufferedReader.close();
				file_number = file_number + 1;
				//**********************************************//
				file = "." + fs + "StoredTweets" + fs + "Document" + file_number.toString() + ".json";
				//**********************************************//
				path = Paths.get(file);
				System.out.println("fileno: " + file_number);
			}
			
			indexWriter.close();
			
		}
		
		//catch (FileNotFoundException e) {e.printStackTrace();}
		//catch (IOException e) {e.printStackTrace();}
		catch (ParseException e) {e.printStackTrace();}
		catch (Exception e) {e.printStackTrace();}
		System.out.println("end index creation");
	}
}
