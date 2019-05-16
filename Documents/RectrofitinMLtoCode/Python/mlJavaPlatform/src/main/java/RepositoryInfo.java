import org.apache.commons.lang3.tuple.ImmutablePair;

import java.util.ArrayList;
import java.util.HashMap;

public class RepositoryInfo {
    private String name;
    private String url;
    private HashMap<ImmutablePair<String,String>, ArrayList<String>> importInfo =
            new HashMap<>();


    public RepositoryInfo(String name,String url) {
        this.name = name;
        this.url = url;
    }

    public String getName() {
        return name;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public HashMap<ImmutablePair<String, String>, ArrayList<String>> getImports() {
        return importInfo;
    }

    public void setImports(HashMap<ImmutablePair<String, String>, ArrayList<String>> imports) {
        this.importInfo = imports;


    }

    public void setImports(ImmutablePair<String, String> stringStringImmutablePair, ArrayList<String> imports) {

        importInfo.put(stringStringImmutablePair,imports);

    }
}
