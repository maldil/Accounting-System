import com.jasongoodwin.monads.Try;
import com.sun.source.tree.ClassTree;
import com.sun.source.tree.CompilationUnitTree;
import com.sun.source.tree.ImportTree;
import com.sun.source.tree.PackageTree;
import com.sun.source.util.JavacTask;

import com.sun.source.util.TreeScanner;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.filefilter.DirectoryFileFilter;
import org.apache.commons.io.filefilter.RegexFileFilter;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.eclipse.jgit.api.Git;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.*;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.function.BinaryOperator;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.eclipse.egit.github.core.SearchRepository;
import org.eclipse.egit.github.core.client.GitHubClient;
import org.eclipse.egit.github.core.service.RepositoryService;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import javax.tools.*;

import static java.util.stream.Collectors.groupingBy;


public class GitRepoInfoUpdater {
    public static String directory = "/Users/malinda/Documents/RectrofitinMLtoCode/Python/Python_Projectsv2/";
    private static List<RepositoryInfo> finalResult = new ArrayList<>();
    private static HashMap<String,ArrayList<ImmutablePair<String,String>>> cloneUrls = new HashMap<>();

    private static Connection conn;
    static class JFileObj extends SimpleJavaFileObject {
        private String text;

        protected JFileObj(String text) {
            super(URI.create(""), JavaFileObject.Kind.SOURCE);
            this.text = text;
        }

        @Override
        public CharSequence getCharContent(boolean ignoreEncodingErrors) {
            return text;
        }
    }

    public static void main(String[] args) {
        init();
        //searchProjectsInGitHub() to search new projects
        //prepareforParrarleExecutionToGetSDKInfo() to find SDK information of Android applications


    }

    public static void searchProjectsInGitHub()
    {
        Random rand = new Random();
        int j =0;
        while(j<400000)
        {
            int n = rand.nextInt(2000) + 20;

            String quarry1 = "https://api.github.com/search/repositories?q=language:java+stars:<"+n+"&page=";
            // String quarry2 = "https://api.github.com/search/repositories?q=language:Kotlin&sort=star&order=asc&page=";
           // String quarry2 = "https://api.github.com/search/repositories?q=language:Kotlin&sort=star&order=asc&page=";
            HashMap<String,JSONObject> repoInfo = searchrepo(quarry1);
            System.out.println(repoInfo.size()+" Projects detected");
            Map<String,JSONObject> freshProjects = removeExistData(repoInfo);

            System.out.println(freshProjects.size()+" Fresh Projects detected");

            downloadAndAnaAllProject(freshProjects);

            try {
                TimeUnit.SECONDS.sleep(60);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            repoInfo.clear();
            System.out.println(n + "Random number");
        }

    }

    public static void prepareforParrarleExecutionToGetSDKInfo()
    {

        String sql = "select ProjectID from AndroidProjectsV1 where (Processed IS NULL and ProDescription Like '%Android%');";
        Statement st = null;
        List<String> projects = new ArrayList<>();
        try {
            st = conn.createStatement();
            ResultSet rs = st.executeQuery(sql);
            while (rs.next())
            {
                projects.add(rs.getString("ProjectID"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        projects.parallelStream().forEach(x->updateProjectsWithSDKInfo(x));

    }

    public static void updateProjectsWithSDKInfo(String proId)
    {
        try {
            File file = new File("/Users/malinda/Documents/RectrofitinMLtoCode/Java_Repo_Info/"+proId+"/"+proId+".json");
            String content = FileUtils.readFileToString(file, "utf-8");
            JSONObject tomJsonObject = new JSONObject(content);
            String clone_url = (String) tomJsonObject.get("clone_url");
            System.out.println(clone_url);
            tryCloningRepo(proId ,clone_url,"/Users/malinda/Documents/RectrofitinMLtoCode/Java/");



            GradleParser GP = new GradleParser();
            Optional<List<Path>> paths= GP.getGradleFIle("/Users/malinda/Documents/RectrofitinMLtoCode/Java/"+proId);
            System.out.println(paths);

            List<Optional<Map<String,String>>> properties = new ArrayList<>();
            if (paths.isPresent())
                paths.get().forEach(x->
                        properties.add(GP.parseFile(x))
                );
            Map<String,String> all_properties = new HashMap<>();
            System.out.println(properties);
            for (Optional<Map<String,String>> x : properties){

                if (x.isPresent()) {
                    Map<String, String> options = x.get();
                    System.out.println(options);
                    System.out.println(options.get("targetSdkVersion"));


                    if (options.get("targetSdkVersion") != null)
                    {
                        if (all_properties.get("targetSdkVersion") != null)
                            all_properties.put("targetSdkVersion",
                                    all_properties.get("targetSdkVersion") + "," + options.get("targetSdkVersion"));

                        else
                            all_properties.put("targetSdkVersion", options.get("targetSdkVersion"));
                    }
                    if (options.get("compileSdkVersion")!=null) {
                        if (all_properties.get("compileSdkVersion") != null) {
                            all_properties.put("compileSdkVersion",
                                    all_properties.get("compileSdkVersion") + "," + options.get("compileSdkVersion"));
                        } else {
                            all_properties.put("compileSdkVersion", options.get("compileSdkVersion"));
                        }
                    }
                    if (options.get("minSdkVersion")!=null) {
                        if (all_properties.get("minSdkVersion") != null) {
                            all_properties.put("minSdkVersion",
                                    all_properties.get("minSdkVersion") + "," + options.get("minSdkVersion"));
                        } else
                            all_properties.put("minSdkVersion", options.get("minSdkVersion"));
                    }
                }
            }


            PreparedStatement preparedStmt = null;
            try {
                preparedStmt = conn.prepareStatement("update AndroidProjectsV1 set " +
                        "TargetSdkVersion = ?, CompileSdkVersion = ?,MinSdkVersion = ? , Processed = ? where ProjectID ="+proId);
                preparedStmt.setString(1,all_properties.get("targetSdkVersion"));
                preparedStmt.setString(2,all_properties.get("compileSdkVersion"));
                preparedStmt.setString(3,all_properties.get("minSdkVersion"));
                preparedStmt.setString(4,"YES");

                preparedStmt.executeUpdate();
            } catch (SQLException e) {
                e.printStackTrace();
            }

            System.out.println("all_properties");
            System.out.println(all_properties);

            File del_file = new File("/Users/malinda/Documents/RectrofitinMLtoCode/Java/"+proId);
            try {
                FileUtils.deleteDirectory(del_file);
            } catch (IOException e) {
                System.out.println("GGGGGGG");
                e.printStackTrace();
            }
//
//                    Object obj = parser.parse(new FileReader("" +
//                            "/Users/malinda/Documents/RectrofitinMLtoCode/Java_Repo_Info/"+proId+"/"+proId+".json"));
//                    JSONObject jsonObject =  (JSONObject) obj;
//
//                    String clone_url = (String) jsonObject.get("clone_url");
//                    System.out.println(clone_url);


        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public static List<String> analyze(String pPath)
    {
        List<File> files = (List<File>) FileUtils.listFiles(
                new File(pPath),
                new RegexFileFilter("([^\\s]+(?=\\.(java))\\.\\2)"),
                DirectoryFileFilter.DIRECTORY
        );


        return isAndroid(files.stream().map(x->x.getAbsolutePath()).collect(Collectors.toList()));
    }

    public static <U> U nullHandleReduce(U u1, U u2, BinaryOperator<U> binOp, U empty){

        if((null == u1 && null == u2)){
            return empty;
        }
        else if(u1 == null || u1.equals(empty)){
            return u2;
        }
        else if(u2 == null || u2.equals(empty)){
            return u1;
        }
        else if((u1.equals(empty) || u2.equals(empty))){
            return empty;
        }
        else
            return binOp.apply(u1,u2);
    }
    private static void downloadAndAnaProject(JSONObject obj){
        String url = (String)obj.get("clone_url");
        System.out.println(url+":"+obj.get("id"));
        System.out.println(url);
        tryCloningRepo(String.valueOf(obj.get("id"))+String.valueOf(obj.get("name")) ,(String)obj.get("clone_url"),"/Users/malinda/Documents/RectrofitinMLtoCode/Java/");
        List<String> imports = analyze("/Users/malinda/Documents/RectrofitinMLtoCode/Java/"+String.valueOf(obj.get("id"))+String.valueOf(obj.get("name")));

        String updateString = "";
        if(imports.stream().anyMatch(x->x.contains("Android")))
            updateString = updateString+"Android,";
        if(imports.stream().anyMatch(x->x.contains("Weka")))
            updateString = updateString+"weka,";
        if(imports.stream().anyMatch(x->x.contains("Torch")))
            updateString = updateString+"Torch,";
        if(imports.stream().anyMatch(x->x.contains("Firebase.ml")))
            updateString = updateString+"Firebase.ml,";
        if(imports.stream().anyMatch(x->x.contains("Tensorflow")))
            updateString = updateString+"Tensorflow,";
        if(imports.stream().anyMatch(x->x.contains("Caffe")))
            updateString = updateString+"Caffe,";
        if(imports.stream().anyMatch(x->x.contains("Deeplearning4j")))
            updateString = updateString+"Deeplearning4j,";

        updateDataBase(obj,updateString);



        writeToFile("/Users/malinda/Documents/RectrofitinMLtoCode/Java_Repo_Info/"+String.valueOf(obj.get("id")),obj,imports);
        if (updateString.contains("Android"))
        {
            updateProjectsWithSDKInfo(String.valueOf(obj.get("id")));
        }
        File file = new File("/Users/malinda/Documents/RectrofitinMLtoCode/Java/"+String.valueOf(obj.get("id"))+String.valueOf(obj.get("name")));
        try {
            FileUtils.deleteDirectory(file);
        } catch (IOException e) {
            System.out.println("GGGGGGG");
            e.printStackTrace();
        }
    }

    static List<String> isAndroid(List<String> paths){
        TreeScanner<Optional<String>, Void> androidScanner = new TreeScanner<Optional<String>, Void>() {
            @Override
            public Optional<String> visitImport(ImportTree node, Void o) {
                String s = node.getQualifiedIdentifier().toString();
                if(s.contains("android"))
                    return Optional.of("Android");
                else if(s.contains("weka"))
                    return Optional.of("Weka");
                else if (s.contains("torchandroid"))
                    return Optional.of("Torch");
                else if (s.contains("firebase.ml"))
                    return Optional.of("Firebase.ml");
                else if (s.contains("tensorflow"))
                    return Optional.of("Tensorflow");
                else if (s.contains("caffe_android_lib"))
                    return Optional.of("Caffe");
                else if (s.contains("deeplearning4j"))
                    return Optional.of("Deeplearning4j");
                else
                    return Optional.empty();
            }

            @Override
            public Optional<String> visitPackage(PackageTree node, Void o){
                return Optional.of("Package = " + node.getPackageName().toString());
            }

            @Override
            public Optional<String> visitClass(ClassTree node, Void o){
                return Optional.of("Class = " + node.getSimpleName().toString());
            }

            @Override
            public Optional<String> reduce(Optional<String> a, Optional<String> b) {
                return  nullHandleReduce(a,b, (r1, r2) ->
                            Optional.of(r1.get() + ", " + r2.get()), Optional.empty());
            }
        };



        List<String> imports = paths.parallelStream()
                .flatMap(p -> Try.ofFailable(() -> androidScanner.scan(getCompilationUnitTree(p), null).stream())
                        .onFailure(e -> e.printStackTrace())
                        .orElse(Stream.empty())).collect(Collectors.toList());

        return imports;

//        imports .stream().filter(x -> x.contains("Firebase"))
//                .collect(groupingBy(x->Arrays.stream(x.split(",")).filter(z -> z.contains("Class")).findFirst().get()))
//                .forEach((k,v) -> System.out.println(k + "     " + v));


               // .forEach(System.out::println);


        //return imports.stream().anyMatch(x->x.contains("Android"));


//        if(imports.stream().anyMatch(x -> x.contains("android"))){
//            paths.parallelStream()
//                    .map()
//        }

    }

    static String readFile(String path)
            throws IOException
    {
        byte[] encoded = Files.readAllBytes(Paths.get(path));
        return new String(encoded, StandardCharsets.UTF_8);
    }


    static CompilationUnitTree getCompilationUnitTree(String path) throws IOException, URISyntaxException {
        final String code = readFile(path);
        JavaFileObject jobj = new JFileObj(code);
        final JavaCompiler tool = ToolProvider.getSystemJavaCompiler();
        final JavaFileManager fm = tool.getStandardFileManager(null, null, null);
        JavacTask ct = (JavacTask) tool.getTask(null, fm, null, null,
                null, Arrays.asList(jobj));
        return ct.parse().iterator().next();
    }

    private static Map<String,JSONObject> removeExistData(HashMap<String,JSONObject> repolist)
    {
        String sql = "select ProjectID from AndroidProjectsV1;";
        List<String> proList = new ArrayList<String>();

        try {
            Statement st = conn.createStatement();
            ResultSet rs = st.executeQuery(sql);
            while (rs.next()) {
                String proId = rs.getString("ProjectID");
                proList.add(proId);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return repolist.entrySet().stream()
                .filter(map -> !proList.contains(String.valueOf(map.getKey())))
                .collect(Collectors.toMap( p-> p.getKey(), p -> p.getValue()));

    }


    private static void getadditionalInfo()
    {
        String sql = "select ProjectID,WebURL,IsFrok,IsArchived from Projects;";
        try {
            Statement st = conn.createStatement();
            ResultSet rs = st.executeQuery(sql);
            while (rs.next()) {
                String url = rs.getString("WebURL");
                int proID = rs.getInt("ProjectID");

                String query = new StringBuilder(url).insert(8,"api.").insert(23, "repos/").toString();

                int isArchived = rs.getInt("IsArchived");

                if (rs.wasNull())
                {
                    Object val = getRepoAdditionalInfo(query,"archived");
                    if (val == null)
                        continue;
                    Boolean arch= (Boolean) val;
                    PreparedStatement preparedStmt = null;
                    if (arch == false)
                        preparedStmt = conn.prepareStatement("update projects set IsArchived = 0 where ProjectID ="+proID);
                    else
                        preparedStmt = conn.prepareStatement("update projects set IsArchived = 1 where ProjectID ="+proID);
                    preparedStmt.execute();
                }
                else
                {
                    System.out.println(proID + "isArchived "+isArchived);
                }

                int frok = rs.getInt("IsFrok");
                if (rs.wasNull())
                {
                    System.out.println(proID);
                    Object val = getRepoAdditionalInfo(query,"fork");
                    if (val == null)
                        continue;
                    Boolean isfroke = (Boolean) val;
                    PreparedStatement preparedStmt = null;
                    if (isfroke == false)
                        preparedStmt = conn.prepareStatement("update projects set IsFrok = 0 where ProjectID ="+proID);
                    else
                        preparedStmt = conn.prepareStatement("update projects set IsFrok = 1 where ProjectID ="+proID);
                    preparedStmt.execute();

                }
                else
                    System.out.println(proID+" Frok "+frok);
            }


        } catch (SQLException e) {
            e.printStackTrace();
        }


    }

    private static Object getRepoAdditionalInfo(String query, String Property)
    {
        try {
            TimeUnit.SECONDS.sleep(2);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        InputStream is;
        try {
            is = new URL(query).openStream();
            String jsonTxt = IOUtils.toString(is);
            System.out.println(jsonTxt);
            JSONObject json = new JSONObject(jsonTxt);
            return json.get(Property);

        } catch (IOException e) {
            System.err.println(e);
        }
        catch (NullPointerException e)
        {
            return null;
        }

        return null;
    }


    private static void getRepoInfo(List<String> quarries)
    {
        System.out.println(quarries);
        quarries.forEach(x-> {
            try {
                TimeUnit.SECONDS.sleep(5);
                searchrepo(x);
            }  catch (InterruptedException e) {
                e.printStackTrace();
            }
        });
    }

    private static HashMap<String,JSONObject> searchrepo(String quarry)  {
        HashMap<String,JSONObject> repoInfo = new HashMap<>();
        System.out.println(quarry);
        for (int k = 0; k < 34; k++) {
            InputStream is;
            try {
                is = new URL(quarry + k + "&per_page=100").openStream();
            } catch (IOException e) {
                System.err.println(e);
                continue;
            }

            String jsonTxt = null;
            try {
                jsonTxt = IOUtils.toString(is);
            } catch (IOException e) {
                e.printStackTrace();
            }
            JSONObject json = new JSONObject(jsonTxt);
            JSONArray items = (JSONArray) json.get("items");

            for (int i = 0; i < 100; i++) { // the result gives 30 results per page
                if (!items.isNull(i)) {
                    JSONObject obj = items.getJSONObject(i);
                    if (!repoInfo.containsKey(Integer.toString((Integer) obj.get("id")))) {
                        repoInfo.put(Integer.toString((Integer) obj.get("id")), obj);
                    } else
                        System.err.println("Same ID exist" + obj.getString("clone_url") + " AND " +
                                repoInfo.get(Integer.toString((Integer) obj.get("id"))).getString("clone_url"));
                }
            }
        }
        return repoInfo;

    }

    private static void checkIfExistinDB (){

    }

    private static void downloadAndAnaAllProject(Map<String,JSONObject> repoInfo)
    {
        repoInfo.entrySet().parallelStream().forEach(k->downloadAndAnaProject(k.getValue()));
  //      repoInfo.forEach((k,v)->updateDataBase(v,chara));
    }

    private static void downloadProjects(Map<String,JSONObject> repoInfo,String Location)
    {
        repoInfo.entrySet().parallelStream().forEach(k->tryCloningRepo(String.valueOf(k.getValue().get("id"))+String.valueOf(k.getValue().get("name")) ,
                (String)k.getValue().get("clone_url"),Location));

    }




    private static void writeToFile(String folderPath, JSONObject obj,List<String> imports){
        File dir = new File(folderPath);
        dir.mkdir();

        try (FileWriter file = new FileWriter(folderPath+"/"+obj.get("id")+".json")) {

            file.write(obj.toString());
            file.flush();

        } catch (IOException e) {
            System.out.println("HHHHHH");
            e.printStackTrace();
        }

        try (FileWriter file = new FileWriter(folderPath+"/"+obj.get("id")+".txt")) {

            for(String str: imports) {
                file.write(str);
                file.write("\n");
            }
            file.flush();
        }
        catch (IOException e) {
            System.out.println("JJJJJ");
            e.printStackTrace();
        }
        

    }

    static Try<Git> tryCloningRepo(String projectName, String cloneLink, String path) {
        return Try.ofFailable(() -> Git.open(new File(path + projectName)))
                .onFailure(e -> System.out.println("Did not find " + projectName + " at" + path))
                .orElseTry(() ->
                        Git.cloneRepository().setURI(cloneLink).setDirectory(new File(path + projectName)).call())
                .onFailure(e -> System.out.println("Could not clone " + projectName));

    }

    private static void init()
    {
        try
        {
            Class.forName("com.mysql.jdbc.Driver");
            conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/MachineLearningAnalysis?user=root&password=123456");

        }
        catch (Exception e)
        {
            System.err.println("Got an exception!");
            System.err.println(e);
        }
    }

    private static void updateDataBase(JSONObject obj,String charac)
    {
        String query = " insert into AndroidProjectsV1 (ProjectID, ProName, ProAuthor,ProDescription,Stars)"
                + " values (?,?,?,?,?)";
        PreparedStatement preparedStmt = null;
        try {
            preparedStmt = conn.prepareStatement(query);
            preparedStmt.setInt (1, (Integer) obj.get("id"));
            preparedStmt.setString (2, (String)obj.get("full_name"));
//            if (obj.get("description")!= JSONObject.NULL) {
//                preparedStmt.setString(3, (String) obj.get("description"));
//
//                System.out.println((String) obj.get("description"));
//            }
//            else
//                preparedStmt.setString (3, (String)obj.get("owner").get(""));
            preparedStmt.setString(3,"");
            preparedStmt.setString(4,charac);
            preparedStmt.setInt(5,(Integer) obj.get("stargazers_count"));

//            preparedStmt.setString (4, (String)obj.get("created_at"));
//            preparedStmt.setString    (5,  (String)obj.get("updated_at"));
//            preparedStmt.setString    (6,  (String)obj.get("pushed_at"));
//            preparedStmt.setInt     (7,  (Integer) obj.get("watchers_count"));
//            preparedStmt.setString    (8,  (String)obj.get("svn_url"));
//            preparedStmt.setString    (9,  (String)obj.get("clone_url"));
//            preparedStmt.setInt    (10,  (Integer) obj.get("open_issues_count"));
//            preparedStmt.setInt    (11,  (Integer) obj.get("stargazers_count"));
//            Boolean isfroke = (Boolean) obj.get("fork");
//            if (isfroke==true)
//                preparedStmt.setInt(12,1);
//            else
//                preparedStmt.setInt(12,0);
//            Boolean isArchived = (Boolean) obj.get("archived");
//            if (isArchived==true)
//                preparedStmt.setInt(13,1);
//            else
//                preparedStmt.setInt(13,0);
            preparedStmt.execute();
        } catch (SQLException e) {
            if (!e.getMessage().contains("Duplicate entry"))
                e.printStackTrace();
        }
    }

}
