import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.*;
import java.net.URL;
import java.sql.*;
import java.util.*;
import java.util.concurrent.TimeUnit;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class GetRepoInfo {

    public static void main(String[] args) {
        Random rand = new Random();
        int j =0;
        while(j<400000)
        {
            int n = rand.nextInt(5000) + 25;
            String quarry1 = "https://api.github.com/search/repositories?q=language:python+stars:<"+1000+"&page=";
            searchrepo(quarry1);
            try {
                TimeUnit.SECONDS.sleep(60);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            System.out.println(n + "Random number");
            j++;
        }
        openjsonfile();
    }

    private static void searchrepo(String quarry)  {
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
            if (items.length()>0) {
                try (FileWriter file = new FileWriter("file1.txt",true)) {
                    items.forEach(x->
                    {
                        try {
                            file.write(x.toString());
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    });

                    System.out.println(items.toString());
                    System.out.println("Successfully Copied JSON Object to File...");
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

        }

    }

    private static void openjsonfile () {

        try (FileReader reader = new FileReader("file1.txt"))
        {
            //Read JSON file
            JSONParser jsonParser = new JSONParser();
            Object obj = jsonParser.parse(reader);

            JSONArray employeeList = (JSONArray) obj;
            System.out.println(employeeList);

            //Iterate over employee array
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ParseException e) {
            e.printStackTrace();
        }

    }
}
