import com.github.gumtreediff.utils.Pair;
import org.apache.commons.io.IOUtils;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.ObjectLoader;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevTree;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;
import org.eclipse.jgit.treewalk.TreeWalk;
import org.eclipse.jgit.treewalk.filter.PathFilter;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Optional;

public class GitRepo {
    public String  repoPath;
    private Git git;
    private Repository repo;
    public GitRepo() {
    }

    public void init(String repopath) throws IOException {
        repoPath=repopath;
        FileRepositoryBuilder builder = new FileRepositoryBuilder();
        repo = builder.setGitDir(new File(repoPath + "/.git")).setMustExist(true)
                .build();
        git = new Git(repo);
    }

    public Map<String, Pair<String,String>> getOldNewChangedFiles(String hashID) throws IOException {
        RevCommit newCommit;
        RevWalk walk = new RevWalk(repo);
        newCommit = walk.parseCommit(repo.resolve(hashID));
        RevCommit prevCommit = getPrevHash(newCommit);
        System.out.println("LogCommit: " + newCommit);
       // System.out.println("Prv Commit: "+prevCommit);
        String logMessage = newCommit.getFullMessage();
        System.out.println("LogMessage: " + logMessage);
        //Print diff of the commit with the previous one.
        return getChangedFiles(newCommit,prevCommit);

    }

    private Map<String, Pair<String,String>> getChangedFiles(RevCommit newCommit, RevCommit prevCommit) throws IOException {
        Map<String, String> newFile =  getChangedFiles(newCommit);
        Map<String, Pair<String,String>> fil_changes = new HashMap<>();
        Iterator<Map.Entry<String, String> > itr = newFile.entrySet().iterator();
        while(itr.hasNext())
        {
            Map.Entry<String, String> entry = itr.next();
            String file_name = entry.getKey();
            String file_content = getFileOfCommit(repo,prevCommit,file_name);
            Pair<String,String> p =  new Pair<>(file_content,entry.getValue());
            fil_changes.put(file_name,p);
        }
        return fil_changes;
    }

    private static String getFileOfCommit(Repository repository,RevCommit commitID,String path) throws IOException {
        RevWalk revWalk = new RevWalk(repository);
        RevCommit commit = revWalk.parseCommit(commitID);
        RevTree tree = commit.getTree();
        TreeWalk treeWalk = new TreeWalk(repository);
        treeWalk.addTree(tree);
        treeWalk.setRecursive(true);
        treeWalk.setFilter(PathFilter.create(path));
        if (!treeWalk.next()) {
            return null;
        }
        ObjectId objectId = treeWalk.getObjectId(0);
        ObjectLoader loader = repository.open(objectId);
        InputStream in = loader.openStream();
        StringWriter writer = new StringWriter();
        IOUtils.copy(loader.openStream(), writer);
        return Optional.ofNullable(writer.toString()).orElse("");
    }

    private Map<String, String> getChangedFiles(RevCommit commit) throws IOException {
        final RevTree parentTree = commit.getTree();
        TreeWalk treeWalk = new TreeWalk(repo);
        Map<String, String> files = new HashMap<>();
        treeWalk.addTree(parentTree);
        treeWalk.setRecursive(true);
        while (treeWalk.next()) {
            String pathString = treeWalk.getPathString();
            if(pathString.endsWith(".py")) {
                final String fileName = pathString;
                files.put(fileName,getFileContent(repo,treeWalk));
            }
        }
        return files;

    }

    private static String getFileContent(Repository repository, TreeWalk treeWalk) throws IOException {
        ObjectId objectId = treeWalk.getObjectId(0);
        ObjectLoader loader = repository.open(objectId);
        StringWriter writer = new StringWriter();
        IOUtils.copy(loader.openStream(), writer);
        return Optional.ofNullable(writer.toString()).orElse("");
    }

    //Helper function to get the previous commit.
    public RevCommit getPrevHash(RevCommit commit)  throws  IOException {
        RevWalk walk = new RevWalk(repo);
        walk.markStart(commit);
        int count = 0;
        for (RevCommit rev : walk) {
            // got the previous commit.
            if (count == 1) {
                return rev;
            }
            count++;
        }
        walk.dispose();
        return null;
    }
}
