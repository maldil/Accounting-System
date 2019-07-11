import com.github.gumtreediff.actions.ActionGenerator;
import com.github.gumtreediff.actions.model.Action;
import com.github.gumtreediff.client.Run;
import com.github.gumtreediff.gen.Generators;

import com.github.gumtreediff.matchers.Matcher;
import com.github.gumtreediff.matchers.Matchers;
import com.github.gumtreediff.tree.ITree;
import com.github.gumtreediff.utils.Pair;
import org.eclipse.jgit.api.errors.GitAPIException;


import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;
import java.util.Map;

public class MainDiff {
    public static void main(String[] args) throws IOException, GitAPIException {

//        Run.initGenerators();
//        String file = "/Users/malinda/Documents/RectrofitinMLtoCode/Python/JavaCodeAnalysisTool/src/main/java/GetRepoInfo.java";
//        TreeContext tc = Generators.getInstance().getTree(file); // retrieve the default generator for the file
//        ITree t = tc.getRoot(); // return the root of the tree
//        System.out.println(TreeIoUtils.toLisp(tc).toString()); // displays the tree in LISP syntax
//


        System.setProperty("gt.pp.path", "/Users/malinda/Documents/RectrofitinMLtoCode/Python/PythonDiff/pythonparser");
        Run.initGenerators();
//        String file1 = "/Users/malinda/Documents/RectrofitinMLtoCode/Python/JavaCodeAnalysisTool/src/main/java/GetRepoInfo.java";
//        String file2 = "/Users/malinda/Documents/RectrofitinMLtoCode/Python/JavaCodeAnalysisTool/src/GetRepoInfovc1.java";
        String file1 = "/Users/malinda/Documents/RectrofitinMLtoCode/Python/PythonDiff/src/test/java/source.py";
        String file2 = "/Users/malinda/Documents/RectrofitinMLtoCode/Python/PythonDiff/src/test/java/dest.py";
        ITree src = Generators.getInstance().getTree(file1).getRoot();
        ITree dst = Generators.getInstance().getTree(file2).getRoot();
        Matcher m = Matchers.getInstance().getMatcher(src, dst); // retrieve the default matcher
        m.match();
        ActionGenerator g = new ActionGenerator(src, dst, m.getMappings());
        g.generate();
        List<Action> actions = g.getActions(); // return the actions
        for (Action action : actions) {
            System.out.println(action);
           // System.out.println(action.getNode().toShortString());
        }
        getTwoFiles(59121694,"0f4b236aceded3207d9052c79534fac7341b40cb");

    }

    private static String getRepoPath(Integer projectID) throws FileNotFoundException {
        String RepoPy3 = "/Users/malinda/Documents/RectrofitinMLtoCode/Python/Pancakes_ML/";
        String RepoPy3v2 = "/Users/malinda/Documents/RectrofitinMLtoCode/Python/Pancakes_ML_3p6Set1/";
        String RepoPy2 = "/Users/malinda/Documents/RectrofitinMLtoCode/Python/Pancakes_ML_2point7_set1/";
        String RepoPy2v2 = "/Users/malinda/Documents/RectrofitinMLtoCode/Python/Pancakes_ML_2point7_set2/";
        String winter = "/Users/malinda/Documents/RectrofitinMLtoCode/Python/Winter/";

        if (isValidPath(RepoPy3+projectID.toString()))
            return RepoPy3+projectID.toString();
        else if(isValidPath(RepoPy3v2+projectID.toString()))
            return RepoPy3v2+projectID.toString();
        else if(isValidPath(RepoPy2+projectID.toString()))
            return RepoPy2+projectID.toString();
        else if(isValidPath(RepoPy2v2+projectID.toString()))
            return RepoPy2v2+projectID.toString();
        else if(isValidPath(winter+projectID.toString()))
            return winter+projectID.toString();
        else
            throw new FileNotFoundException(projectID.toString());

    }

    public static boolean isValidPath(String path) {
        File file = new File(path);
        return file.isDirectory();
    }

    public static void getTwoFiles(Integer projectID,String Hash) throws IOException, GitAPIException {
        GitRepo g = new GitRepo();
        g.init(getRepoPath(projectID));
        Map<String, Pair<String,String>> fil_changes = g.getOldNewChangedFiles(Hash);
    }
}
