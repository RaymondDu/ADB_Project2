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
	public static String getMatchResultNum(String query) throws IOException {
		
        String bingURL = "https://api.datamarket.azure.com/Data.ashx/Bing/SearchWeb/v1/Composite?Query=%27site%3a"+query+"%27&$top=10&$format=Json";
		//Provide your account key here.
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
    
    
    
		public static Integer parseJSON(String jsonStr) {
        
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
	
    public static int calcCoverage(String site, ArrayList<String> query){
        int coverage = 0;
        for (int i =0; i< query.size();i++){
            try {
                String q = site+" "+query.get(i);
                String key = java.net.URLEncoder.encode(q, "utf8");
                String content = getMatchResultNum(key);
                coverage = coverage + parseJSON(content);
            }
            catch (Exception e){
                
            }
        }
        return coverage;
    }
    
    public static ArrayList<TreeNode> Classify(TreeNode category, String site, Double spec, int coverage) {
        ArrayList<TreeNode> result = new ArrayList<TreeNode>();
        if (category.children==null){
            result.add(category);
            return result;
        }
        int sumCoverage = 0;
        for (int i = 0; i < category.children.size(); i++){
            category.coverage.add(calcCoverage(site, category.words.get(i)));
            sumCoverage = sumCoverage + calcCoverage(site,category.words.get(i));
        }
        Double parentSpecifity = 1.0;
        if (category.parent!= null){
            parentSpecifity = category.parent.specifity.get(category.parent.children.indexOf(category));
        }
        for (int i = 0; i < category.children.size(); i++){
            if (sumCoverage >0){
                category.specifity.add(parentSpecifity*category.coverage.get(i)/sumCoverage);
            } else {
                category.specifity.add(0.0);
            }
        }
        for (int i = 0; i< category.children.size(); i++){
            if (category.coverage.get(i)>coverage && category.specifity.get(i)>spec){
                result.addAll(Classify(category.children.get(i),site,spec,coverage));
            }
        }
        if (result.size()==0){
            result.add(category);
        }
        return result;
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
        
        
        
        ArrayList<TreeNode> classficationResult = Classify(Tree.getTree(), site, specifity, coverage);
        //Tree.printTree();
        System.out.println("Classsification:");
        for (int i = 0; i < classficationResult.size(); i++){
            TreeNode tmp = classficationResult.get(i);
            String s = tmp.name;
            while (tmp.parent != null){
                tmp = tmp.parent;
                s = tmp.name + "/" +s; 
            }
            System.out.println(s);
        }
        /*
        System.out.println("Please input site and query:");
		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        String query = null;
		try {
			query = br.readLine();
			
		} catch (IOException ioe) {
			System.out.println("IO error trying to read your Query!");
			System.exit(1);
		}
        
        
        String key = java.net.URLEncoder.encode(query, "utf8");
        String content = getMatchResultNum(key);
        int match_num = parseJSON(content);
        System.out.println("We get "+match_num+" matching results.");
        System.exit(0);
        */
	}
    
        
}
