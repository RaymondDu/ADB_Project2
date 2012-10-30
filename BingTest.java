import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import org.json.simple.*;
import org.json.simple.parser.ContainerFactory;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import org.apache.commons.codec.binary.Base64;
public class BingTest {
	public static String getResult(String q) throws IOException {
		
		String bingUrl = "https://api.datamarket.azure.com/Data.ashx/Bing/SearchWeb/v1/Web?Query=%27"+q+"%27&$top=10&$format=Json";
		//Provide your account key here.
		String accountKey = "wRccq1TMy476bqFdC1GrKeHeJ33Fm+hmzSwYWgmtSrM=";
		
		byte[] accountKeyBytes = Base64.encodeBase64((accountKey + ":" + accountKey).getBytes());
		String accountKeyEnc = new String(accountKeyBytes);
        
		URL url = new URL(bingUrl);
		URLConnection urlConnection = url.openConnection();
		urlConnection.setRequestProperty("Authorization", "Basic " + accountKeyEnc);
        
		InputStream inputStream = (InputStream) urlConnection.getContent();
		byte[] contentRaw = new byte[urlConnection.getContentLength()];
		inputStream.read(contentRaw);
		String content = new String(contentRaw);
        
		return content;
	}
    
	public static HashMap<String, Integer> wordCount(String str) {
		// split string, remove some punctations
		String pattern = "([\\:|.,\"()'])";
	    str = str.replaceAll(pattern, "");
        /* might be problematic */
        str = str.toLowerCase();
        
	    String[] splitString = (str.split("\\s+"));
	    
        
		HashMap<String, Integer> tfMap = new HashMap<String, Integer>();
		
		for (String word : splitString) {
			if(!tfMap.containsKey(word)) {
				tfMap.put(word, 1);
			} else {
				tfMap.put(word, tfMap.get(word)+1);
			}
		}
		return tfMap;
	}
	
	public static ArrayList<ArrayList<String>> parseJSON(String jsonStr) {
        
		ArrayList<ArrayList<String>> result = new ArrayList<ArrayList<String>>();
		for(int i=0; i<10; i++){
            
            result.add(new ArrayList<String>());
		}
        
		// find title
		JSONParser parser = new JSONParser();
		KeyFinder finder = new KeyFinder();
		finder.setMatchKey("Title");
		try{
			int count = 0;
		    while(!finder.isEnd()){
                parser.parse(jsonStr, finder, true);
                if(finder.isFound()){
                    finder.setFound(false);
                    String s = finder.getValue().toString();
                    result.get(count).add(s);
                    count++;
                    
                }
		    }
            
            if(count < 10) {
                System.out.println("Number of retrieved results by Bing is less than 10 !");
                System.exit(0);
                
            }
        }
	    catch(ParseException pe){
		    pe.printStackTrace();
        }
        
		// find Description
		JSONParser parser2 = new JSONParser();
		KeyFinder finder2 = new KeyFinder();
		finder2.setMatchKey("Description");
		try{
		    int count = 0;
		    while(!finder2.isEnd()){
                parser2.parse(jsonStr, finder2, true);
                if(finder2.isFound()){
                    finder2.setFound(false);
                    String s = finder2.getValue().toString();
                    result.get(count).add(s);
                    count++;
                    
                }
		    }
        }
	    catch(ParseException pe){
		    pe.printStackTrace();
        }
        
		// find DisplayURl
		JSONParser parser3 = new JSONParser();
		KeyFinder finder3 = new KeyFinder();
		finder3.setMatchKey("DisplayUrl");
		try{
			int count = 0;
		    while(!finder3.isEnd()){
                parser3.parse(jsonStr, finder3, true);
                if(finder3.isFound()){
                    finder3.setFound(false);
                    String s = finder3.getValue().toString();
                    result.get(count).add(s);
                    count++;
                    
                }
		    }
        }
	    catch(ParseException pe){
		    pe.printStackTrace();
        }
		
        return result;
	}
	
    /* return the number of articles that are relevant */
    public static int Query(ArrayList<ArrayList<String>> result) {
        int y=0;
        for (int i = 0; i < 10; i++){
            System.out.println("Result "+(i+1));
            System.out.println("[");
            System.out.println("URL: "+result.get(i).get(2));
            System.out.println("Title: "+result.get(i).get(0));
            System.out.println("Summary: "+result.get(i).get(1));
            System.out.println("]");
            System.out.println();
            System.out.print("Relevant (Y/N)?");
            BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
            try {
                String s = in.readLine();
                while (!s.equals("y") && !s.equals("Y") && !s.equals("n") && !s.equals("N")) {
                    System.out.print("Please say Y/N:");
                    
                    s = in.readLine();
                }
                if (s.equals("y") || s.equals("Y")) {
                    result.get(i).add("1");
                    y++;
                } else {
                    result.get(i).add("0");
                }
                System.out.println("Precision: "+y+"/10");
                System.out.println("-----------------------------------------");
            } catch (IOException e){
                System.err.println(e.getMessage());
                System.exit(1);
            }
        }
        return y;
    }
    
   
    
    
    public static void AddNewKeyword(ArrayList<ArrayList<String>> result,ArrayList<String> keywords){
        ArrayList<HashMap<String,Integer>> wordcounts = new ArrayList<HashMap<String,Integer>>();
        HashMap<String,Double> globalWeights = new HashMap<String,Double>();
        HashMap<String,Double> idf = new HashMap<String,Double>();
        // merge title and description together
        for (int i = 0; i < 10; i++){
            wordcounts.add(wordCount(result.get(i).get(0)+result.get(i).get(1)));
        }
        
        for (int i = 0; i < 10; i++){
            if (result.get(i).get(3).equals("1")){
                calculateWeights(wordcounts, i, globalWeights, idf);
            }
        }
        double firstWeight=0;
        String firstKey=null;
        double secondWeight=0;
        String secondKey=null;
        /* might be problematic */
        
        for (String s : keywords) {
            if(globalWeights.containsKey(s.toLowerCase())) {
                globalWeights.remove(s.toLowerCase());
            }
        }
        
        
        
        for(String key : globalWeights.keySet()){
            if (globalWeights.get(key)>firstWeight){
                secondWeight = firstWeight;
                secondKey = firstKey;
                firstWeight =globalWeights.get(key);
                firstKey = key;
            } else if (globalWeights.get(key)>secondWeight){
                secondWeight = globalWeights.get(key);
                secondKey = key;
            }
        }
        
        // add expansion keywords to the query
        keywords.add(firstKey);
        keywords.add(secondKey);
        
        System.out.println("=================================");
        System.out.println("Augmented Query Keywords: "+firstKey+" "+secondKey);
        
        
    }
    
    public static void calculateWeights(ArrayList<HashMap<String,Integer>> wordcounts,
                                        int i, HashMap<String,Double> globalWeights, HashMap<String,Double> idfTable){
        for(String key : wordcounts.get(i).keySet()) {
            int value = wordcounts.get(i).get(key);
            double tf = 1 + Math.log(value);
            double idf;
            if (idfTable.containsKey(key)){
                idf = idfTable.get(key);
            } else {
                idf = Math.log(10f/occurence(key,wordcounts));
                idfTable.put(key, idf);
            }
            double weight = tf * idf;
            if (globalWeights.containsKey(key)){
                globalWeights.put(key, globalWeights.get(key)+weight);
            } else {
                globalWeights.put(key, weight);
            }
        }
    }
    
    public static int occurence(String key, ArrayList<HashMap<String,Integer>> wordcounts){
        int retval = 0;
        for (int i = 0; i<10; i++){
            if (wordcounts.get(i).containsKey(key)){
                retval++;
            }
        }
        return retval;
    }
    
    public static String KeywordsToString(ArrayList<String> keywords){
        StringBuilder key = new StringBuilder();
        key.append(keywords.get(0));
        
        for (int i = 1; i < keywords.size(); i++){
            key.append(" "+keywords.get(i));
        }
        
        return key.toString();
    }

    
/* main thread */
    
	public static void main(String[] args) throws IOException {
        
        ArrayList<String> keywords = new ArrayList<String>();
        
        float targetPrecision = 0f;
        float precision = 0f;
        
        System.out.println("Please input query:");
		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
		String query = null;
		try {
			query = br.readLine();
			
		} catch (IOException ioe) {
			System.out.println("IO error trying to read your Query!");
			System.exit(1);
		}
        
        
	    String[] splitQuery = (query.split("\\s+"));
        for(String q : splitQuery) {
            keywords.add(q);
        }
    
        System.out.println("Please input your desired precision:");
		BufferedReader br2 = new BufferedReader(new InputStreamReader(System.in));
		String precisionString = null;
		try {
			precisionString = br2.readLine();
			
		} catch (IOException ioe) {
			System.out.println("IO error trying to read your precision!");
			System.exit(1);
		}
        
        try {
            targetPrecision = Float.parseFloat(precisionString);
            
        } catch (NumberFormatException e){
            System.err.println(e.getMessage());
            System.exit(1);
        }
        
        
        
        
        while (precision < targetPrecision) {
            String key = KeywordsToString(keywords);
            
            System.out.println("Query: "+key);
            key = java.net.URLEncoder.encode(key, "utf8");
            System.out.println("=================================");
            
            
            String content = getResult(key);
            ArrayList<ArrayList<String>> result = parseJSON(content);
            int y = Query(result);
            if (y == 0){
                System.out.println("No relevant results, cannot proceed.");
                System.exit(0);
            } else {
                precision = y/10f;
            }
            
            if(precision < targetPrecision) AddNewKeyword(result, keywords);
        }
        System.out.println("Target precision reached, exit.");
        
        
	}
    
        
}
