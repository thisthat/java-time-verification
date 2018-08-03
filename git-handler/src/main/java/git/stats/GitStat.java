package git.stats;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.internal.storage.file.FileRepository;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.revwalk.RevCommit;
import sql.SQLManager;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class GitStat {

    private final FileRepository repository;
    private final Git git;
    private final String projectFolder;
    private final String projectName;
    private final int prjID;

    public static void main(String[] args) throws Exception {
        GitStat stat = new GitStat(args[0], args[1], args[2]);
        System.out.println("#Commit " + stat.getnCommit());
        System.out.println("#Branch " + stat.getnBranch());
        stat.storeCommitInfo();
    }


    public GitStat(String projectName, String folder, String idProject) throws Exception {
        this.projectName = projectName;
        this.projectFolder = folder;
        this.repository = new FileRepository(new File(this.projectFolder, ".git"));
        this.git = new Git(this.repository);
        this.prjID = Integer.parseInt(idProject);
    }

    public void storeCommitInfo() throws Exception {
        SQLManager sql = new SQLManager();
        Iterable<RevCommit> logs = git.log().all().call();
        int processed = 0;
        for (RevCommit rev : logs) {
            processed++;
            if(processed%1000==0) System.out.println("Processed #" + processed);
            String hash = rev.getName();
            long time = rev.getCommitterIdent().getWhen().getTime();
            sql.addCommitInfo(hash, time, prjID);
        }
        sql.close();
    }

    public void updateCommitInfo() throws Exception {
        SQLManager sql = new SQLManager();
        Iterable<RevCommit> logs = git.log().all().call();
        List<String> empty = new ArrayList<>();
        int processed = 0;
        for (RevCommit rev : logs) {
            processed++;
            if(processed%100==0) System.out.println("Processed #" + processed);
            String hash = rev.getName();
            long time = rev.getCommitterIdent().getWhen().getTime();
            sql.updateCommitDate(hash, time);
        }
        System.out.println("Empty : " + empty.size());
        sql.close();
        System.out.println("________________");
        for(String e : empty){
            System.out.println(e);
        }
    }

    public Ref findCommitBranch(String hash) throws Exception {
        try {
            List<Ref> branches = git.branchList().setContains(hash).call();
            if(branches.size() > 0)
                return branches.get(0);
        } catch (Exception ex){
            System.out.println(ex.getMessage());
        }
        return null;
    }

    public int getnCommit() throws Exception {
        Iterable<RevCommit> logs = git.log().all().call();
        int n = 0;
        for (RevCommit rev : logs) {
            n++;
        }
        return n;
    }

    public int getnBranch() throws Exception {
        return git.branchList().call().size();
    }
}
