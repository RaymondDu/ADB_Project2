
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.HashSet;

public class Tree {
    private static TreeNode tree = null;
    
    private Tree(){
    }
        
    public static TreeNode getTree(){
        if (tree==null){
            
            tree = Build("Root",null);
            
            return tree;
        } else {
            return tree;
        }
    }
    public static void printTree(){
        printTreeRec(tree);
    }
    public static void printTreeRec(TreeNode tmp){
        System.out.println(tmp.name);
        
        if (tmp.words != null){
            for (int i = 0; i<tmp.words.size();i++){
                System.out.println("\t"+tmp.children.get(i).name);
                if (tmp.coverage!=null && tmp.coverage.size()>0){
                    System.out.println("\t"+"Coverage:"+tmp.coverage.get(i)+" Specifity: "+tmp.specifity.get(i));
                }
                for (int j = 0; j < tmp.words.get(i).size();j++){
                    System.out.println("\t\t"+tmp.words.get(i).get(j));
                }
            }
        }
        if (tmp.children != null){
            for (int i = 0; i < tmp.children.size();i++){
                printTreeRec(tmp.children.get(i));
            }
        }
    }
    
    
    
    private static TreeNode Build(String name, TreeNode parent){
        //System.out.println("Build:"+name);
        TreeNode node = new TreeNode();
	node.match = false;
        node.name = name;
        node.parent = parent;
        node.children = null;
        node.words = null;
        node.coverage = null;
        node.specifity = null;
	node.URL=null;
        File f = new File(name.toLowerCase()+".txt");
        //System.out.println(f.exists());

        //System.out.println(f.exists());
        if (f.exists()){
            node.children = new ArrayList<TreeNode>();
            node.words = new ArrayList<ArrayList<String>>();
            try{
                BufferedReader reader = new BufferedReader(new FileReader(f));
                
                
                
                String line = null;
                while ((line=reader.readLine()) != null) {
                    String type = line.substring(0,line.indexOf(" "));
                    String keyword = line.substring(line.indexOf(" ")+1);
                    

                    if (node.children.size()==0){
                       
                        node.children.add(Build(type, node));
                        node.words.add(new ArrayList<String>());
                        node.words.get(node.words.size()-1).add(keyword);
                    } else {
                        if (type.equals(node.children.get(node.children.size()-1).name)){
                            node.words.get(node.words.size()-1).add(keyword);
                        } else {
                            node.children.add(Build(type, node));
                            node.words.add(new ArrayList<String>());
                            node.words.get(node.words.size()-1).add(keyword);
                        }
                    }
      
                }
                reader.close();
                node.coverage = new ArrayList<Integer>(node.children.size());
                node.specifity = new ArrayList<Double>(node.children.size());
		node.URL = new HashSet<String>();
            } catch (Exception e){
                System.out.println(e.getMessage());
            }
            
            
        }
        return node;
    }
}
