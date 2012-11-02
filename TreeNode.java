import java.util.ArrayList;
import java.util.HashSet;

public class TreeNode{
    boolean match;
    String name;
    TreeNode parent;
    ArrayList<TreeNode> children;
    ArrayList<ArrayList<String>> words;
    ArrayList<Integer> coverage;
    ArrayList<Double> specifity;
    HashSet<String> URL;
}
