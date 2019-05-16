import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class GradleParser {

    public Optional<List<Path>> getGradleFIle(String folder)

    {
        List<Path> files_gradle = null;
        List<Path> files_manifest = null;
        try {
            files_gradle = Files.walk(Paths.get(folder))
                        .filter(Files::isRegularFile).filter(x->x.getFileName().endsWith("build.gradle")).collect(Collectors.toList());

            files_manifest = Files.walk(Paths.get(folder))
                    .filter(Files::isRegularFile).filter(x->x.getFileName().endsWith("AndroidManifest.xml")).collect(Collectors.toList());

            List<Path> newList = new ArrayList<Path>(files_gradle);

            newList.addAll(files_manifest);

            return Optional.of(newList);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return Optional.empty();
    }

    public Optional<Map<String,String>> parseFile(Path file_path)
    {
        File file = new File(file_path.toString());
        Scanner scff = null;
        String sc ="";
        try {
            scff = new Scanner(file);
            scff.useDelimiter("\\Z");
            if(scff.hasNext()) {
                sc = scff.next();
            }
      //      System.out.println(sc);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }




        Pattern p1 = Pattern.compile("(targetSdkVersion=\"|targetSdkVersion= \"|targetSdkVersion |targetSdkVersion|targetSdkVersion = |targetSdkVersion =|targetSdkVersion= |targetSdkVersion=)([0-9.'/-]+)");
        Pattern p2 = Pattern.compile("(minSdkVersion=\"|minSdkVersion= \"|minSdkVersion |minSdkVersion|minSdkVersion = |minSdkVersion =|minSdkVersion= |minSdkVersion=)([0-9.'/-]+)");
        Pattern p3 = Pattern.compile("(compileSdkVersion=\"|compileSdkVersion= \"|compileSdkVersion |compileSdkVersion|compileSdkVersion = |compileSdkVersion =|compileSdkVersion= |compileSdkVersion=)([0-9.'/-]+)");
        Matcher m1 = p1.matcher(sc);
        Matcher m2 = p2.matcher(sc);
        Matcher m3 = p3.matcher(sc);

        Map<String,String> map = new HashMap<>();

        if (m1.find()) {
            if (m1.group(1).contains("targetSdkVersion"))
                map.put("targetSdkVersion",m1.group(2));
        }

        if (m2.find()) {
            if (m2.group(1).contains("minSdkVersion"))
                map.put("minSdkVersion",m2.group(2));
        }

        if (m3.find()) {
            if (m3.group(1).contains("compileSdkVersion"))
                map.put("compileSdkVersion",m3.group(2));
        }


        if (map.size()>0)
            return Optional.of(map);
        else
            return Optional.empty();


    }
}
