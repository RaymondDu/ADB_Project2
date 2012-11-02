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
import java.util.HashSet;
import java.util.TreeMap;
import java.util.Map;
import java.util.Set;
import java.util.Iterator;
import org.json.simple.*;
import org.json.simple.parser.ContainerFactory;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.apache.commons.codec.binary.Base64;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;

public class BingTest {
	public static String getJSONResults(String query) throws IOException {
		
        String bingURL = "https://api.datamarket.azure.com/Data.ashx/Bing/SearchWeb/v1/Composite?Query=%27site%3a"+query+"%27&$top=4&$format=Json";
		//account key here
		String accountKey = "wRccq1TMy476bqFdC1GrKeHeJ33Fm+hmzSwYWgmtSrM=";
		
		byte[] accountKeyBytes = Base64.encodeBase64((accountKey + ":" + accountKey).getBytes());
		String accountKeyEnc = new String(accountKeyBytes);
        
		URL url = new URL(bingURL);
		URLConnection urlConnection = url.openConnection();
		urlConnection.setRequestProperty("Authorization", "Basic " + accountKeyEnc);
        
		InputStream inputStream = (InputStream) urlConnection.getContent();
		byte[] contentRaw = new byte[urlConnection.getContentLength()];
		inputStream.read(contentRaw);
		String content = new String(contentRaw);
        
		return content;
	}
    
    
    /* retrieve all the page urls related to a query, return number of match results */
	public static Integer parseJSON(TreeNode node, String jsonStr) {
        
		// find DisplayURl, and add the urls of all the returned pages to node.URL
		JSONParser parser3 = new JSONParser();
		KeyFinder finder3 = new KeyFinder();
		finder3.setMatchKey("Url");
        int count = 0;
		try{
		    	while(!finder3.isEnd()){
                		parser3.parse(jsonStr, finder3, true);
                		if(finder3.isFound() && count<4){
                                count++;
                    			finder3.setFound(false);
                    			String s = finder3.getValue().toString();
                    			node.URL.add(s);
                		}
		    	}
        }
        catch (ParseException pe){
			pe.printStackTrace();
        }


        String result = "";
		JSONParser parser = new JSONParser();
		KeyFinder finder = new KeyFinder();
		finder.setMatchKey("WebTotal");
		try{
			
		    while(!finder.isEnd()){
                parser.parse(jsonStr, finder, true);
                if(finder.isFound()){
                    finder.setFound(false);
                    result = finder.getValue().toString();
                    break;
                }
		    }
            
        }
	    catch(ParseException pe){
		    pe.printStackTrace();
        }
        
				
        return Integer.parseInt(result);
	}
	
    /* given a site url, a list of prob queries
     * return the estimated coverage of the given node 
     */
    public static int calcCoverage(TreeNode node, String site, ArrayList<String> query){
        int coverage = 0;
        // loop through all the queries, issue each to the database(site url)
        // sum up the number of match results
        for (int i =0; i< query.size();i++){
            try {
                String q = site+" "+query.get(i);
                String key = java.net.URLEncoder.encode(q, "utf8");
                String content = getJSONResults(key);
                coverage = coverage + parseJSON(node, content);
            }
            catch (Exception e){
                
            }
        }
        return coverage;
    }
    
    
    // classification algorithm in figure 4
    public static ArrayList<TreeNode> Classify(TreeNode category, String site, Double spec, int coverage) {
        ArrayList<TreeNode> result = new ArrayList<TreeNode>();
        
        if (category.children==null){
            result.add(category);
            return result;
        }
        // calculate the ECoverage of TreeNode category
        int sumCoverage = 0;
        
        for (int i = 0; i < category.children.size(); i++){
            int tmp = calcCoverage(category,site, category.words.get(i));
            category.coverage.add(tmp);
            sumCoverage = sumCoverage + tmp;
        }
      
        Double parentSpecifity = 1.0;
        if (category.parent!= null){
            parentSpecifity = category.parent.specifity.get(category.parent.children.indexOf(category));
        }
        // calculate the specifity vector
        for (int i = 0; i < category.children.size(); i++){
            if (sumCoverage >0){
                category.specifity.add(parentSpecifity*category.coverage.get(i)/sumCoverage);
            } else {
                category.specifity.add(0.0);
            }
        }
        // loop through all subcategories Ci of C
        for (int i = 0; i< category.children.size(); i++){
            if (category.coverage.get(i)>coverage && category.specifity.get(i)>spec){
                category.match = true;
                result.addAll(Classify(category.children.get(i),site,spec,coverage));
            }
        }
        if (result.size()==0){
            result.add(category);	    
        }
        return result;
    }
    
    
    
	public static void getContentSummary(TreeNode node,String site){
        // loop through all the children node, if a children node is big enough
        // in coverage and specifity, then recursively explore this children node
        // therefore get all the related urls
		for (int i = 0; i<node.children.size();i++){
			if (node.children.get(i).match == true){
				getContentSummary(node.children.get(i),site);
				node.URL.addAll(node.children.get(i).URL);
			}
		}
        System.out.println("===========================================");
        System.out.println("===========================================");
		System.out.println("Constructing content summary for: "+node.name);
        
		TreeMap<String, Integer> wordCount = new TreeMap<String, Integer>();
		Iterator<String> i = node.URL.iterator();
		int count = 0;
		while (i.hasNext()){
			String tmp = i.next();
			count++;
			System.out.println(count +"/" + node.URL.size()+" Getting Page "+tmp);
			Set local = getWordsLynx.runLynx(tmp);
			Iterator j = local.iterator();
			while(j.hasNext()){
				String word = j.next().toString();
				if (wordCount.containsKey(word)){
					wordCount.put(word, wordCount.get(word)+1);
				} else {
					wordCount.put(word, 1);
				}
			}
		}
		try {
			File file = new File(node.name+"-"+site+".txt");
			BufferedWriter writer = new BufferedWriter(new FileWriter(file));
			Set<Map.Entry<String,Integer>> setWordCount= wordCount.entrySet(); 
			Iterator<Map.Entry<String,Integer>> k = setWordCount.iterator();
			while (k.hasNext()){
				Map.Entry next = k.next();
				writer.write(next.getKey()+"#"+next.getValue()+"\n");
				writer.flush();
			}
			writer.close();
		} catch (Exception e){
			System.err.println("Cannot write to file");
		}


	}

/* main thread */
    
	public static void main(String[] args) throws IOException {
        
        System.out.println("Please input site:");
		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        String site = null;
		try {
			site = br.readLine();
			
		} catch (IOException ioe) {
			System.out.println("IO error trying to read your site!");
			System.exit(1);
		}
        
        System.out.println("Please input coverage:");
		BufferedReader br2 = new BufferedReader(new InputStreamReader(System.in));
        String coverageString = null;
        int coverage = 0;
		try {
			coverageString = br2.readLine();
            coverage = Integer.parseInt(coverageString);
		} catch (IOException ioe) {
			System.out.println("IO error trying to read your input!");
			System.exit(1);
		} catch (NumberFormatException nfe) {
            System.out.println("Not a integer");
			System.exit(1);
        }
        
        
        System.out.println("Please input specifity:");
		BufferedReader br3 = new BufferedReader(new InputStreamReader(System.in));
        String specifityString = null;
        double specifity = 0;
		try {
			specifityString = br2.readLine();
            specifity = Double.parseDouble(specifityString);
		} catch (IOException ioe) {
			System.out.println("IO error trying to read your input!");
			System.exit(1);
		} catch (NumberFormatException nfe) {
            System.out.println("Not a double");
			System.exit(1);
        }
        
        
        System.out.println("Classifying ... Please be patient, grab a cup of coffee and then come back ...");
        ArrayList<TreeNode> classificationResult = Classify(Tree.getTree(), site, specifity, coverage);
                
        
        for (int i = 0; i < classificationResult.size(); i++){
            System.out.println("Classification Path: ");
            TreeNode tmp = classificationResult.get(i);
            String s = tmp.name;
            while (tmp.parent != null){
                tmp = tmp.parent;
                s = tmp.name + "/" +s; 
            }
            System.out.println(s);
            
        }


        //Get Content Summary
        getContentSummary(Tree.getTree(),site);
	}
    
        
}
