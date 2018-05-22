package git;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.internal.storage.file.FileRepository;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevWalk;

import java.io.File;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class GitTag {


    String projectName;
    long IDProject = 0;
    File projectFolder;
    Repository repository;
    Git git;

    public GitTag(String projectName, File projectFolder) throws Exception {
        this.projectName = projectName;
        this.projectFolder = projectFolder;
        this.repository = new FileRepository(new File(this.projectFolder, ".git"));
        this.git = new Git(this.repository);
    }

    public GitTag(String projectName, String projectFolder) throws Exception {
        this(projectName, new File(projectFolder));
    }

    private static String formatDate(Date when) {
        DateFormat df = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
        return df.format(when);
    }

    public static void main(String[] args) throws Exception {
        GitTag traverse = new GitTag(args[0], args[1]);
        traverse.storeProjectInformation();
    }


    public void storeProjectInformation() throws Exception {
        Iterable<RevCommit> logs = git.log().all().call();
        ArrayList<Ref> tags = (ArrayList<Ref>) git.tagList().call();
        final RevWalk walk = new RevWalk(repository);
        for(Ref ref : tags){
            long time = walk.parseTag(ref.getObjectId()).getTaggerIdent().getWhen().getTime();
            System.out.println(time);
        }
    }

}