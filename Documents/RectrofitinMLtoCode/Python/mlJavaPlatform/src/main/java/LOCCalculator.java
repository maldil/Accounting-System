import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

import javax.lang.model.element.Element;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
//import javax.xml.soap.Node;
import java.io.*;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.util.Arrays;



public class LOCCalculator {
    static String  projectFolder = "/Users/malinda/Documents/RectrofitinMLtoCode/Python/Pro25/Project_Clone/";
    static String LOCFolder = "/Users/malinda/Documents/RectrofitinMLtoCode/Python/LOC/";
    private static Connection conn;
    public static void main(String[] args) {
        init();

        File[] directories = new File(projectFolder).
                listFiles(File::isDirectory);


        Arrays.stream(directories).forEach(x-> {
            String s = null;
            String path = LOCFolder+x.getPath().substring(x.getPath().lastIndexOf('/')+1)
                    +".xml";
            File tempFile = new File(path);
            boolean exists = tempFile.exists();


            if (!exists)
            {
                try {
                    Process p = Runtime.getRuntime().exec("cloc --sum-one --xml --report-file="+path+
                            " "+x.getPath());
                    BufferedReader stdInput = new BufferedReader(new
                            InputStreamReader(p.getInputStream()));
                    BufferedReader stdError = new BufferedReader(new
                            InputStreamReader(p.getErrorStream()));

                    // read the output from the command
                    System.out.println("Here is the standard output of the command:\n");
                    while ((s = stdInput.readLine()) != null) {
                        System.out.println(s);
                    }

                    // read any errors from the attempted command
                    System.out.println("Here is the standard error of the command (if any):\n");
                    while ((s = stdError.readLine()) != null) {
                        System.out.println(s);
                    }
                    String project = x.getPath().substring(x.getPath().lastIndexOf('/')+1);
                    if (!(project.equals("2417218") ||project.equals("855151") || project.equals("1955800")
                    || project.equals("194807") || project.equals("65687570")))
                        parseFile(project,path);

                }
                catch (IOException e) {
                    System.out.println("exception happened - here's what I know: ");
                    e.printStackTrace();
                }
                System.out.println("+++++++++++++++++++++++++++++++++++++++++++");
            }
        });
    }

    private static void parseFile(String project,String path)
    {
        try {

            InputStream inputStream = new FileInputStream(path);
            DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory
                    .newInstance();
            DocumentBuilder docBuilder = docBuilderFactory.newDocumentBuilder();
            Document doc = docBuilder.parse(inputStream);
            doc.getDocumentElement().normalize();
            NodeList nListL = doc.getElementsByTagName("language");
            for (int temp = 0; temp < nListL.getLength(); temp++) {
                PreparedStatement preparedStmt1 = null;
                String langage= nListL.item(temp).getAttributes().getNamedItem("name").getNodeValue();
                int loc = Integer.parseInt(nListL.item(temp).getAttributes().getNamedItem("code").getNodeValue());
                preparedStmt1 = conn.prepareStatement("INSERT INTO LOC (ProjectID, Langage, LOC)" +
                        "VALUES (?,?,?);");
                preparedStmt1.setInt(1,Integer.parseInt(project));
                preparedStmt1.setString(2,langage);
                preparedStmt1.setInt(3,loc);
                preparedStmt1.execute();
            }
            NodeList nListT = doc.getElementsByTagName("total");
            int totalLOC = Integer.parseInt(nListT.item(0).getAttributes().getNamedItem("code").getNodeValue());

            PreparedStatement preparedStmt1 = null;
            preparedStmt1 = conn.prepareStatement("update projects set LOC ="  +totalLOC+" where ProjectID ="+Integer.parseInt(project));
            System.out.println(preparedStmt1);
            preparedStmt1.execute();

        } catch (Exception e) {
            File file = new File(path);
            if(file.delete()){
                System.out.println(path+" File deleted");
            }else System.out.println(path+" doesn't exists");

            e.printStackTrace();
        }
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

}

