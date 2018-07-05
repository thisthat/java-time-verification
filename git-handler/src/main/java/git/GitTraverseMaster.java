package git;

import org.apache.commons.io.FileUtils;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.ResetCommand;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.JGitInternalException;
import org.eclipse.jgit.errors.LockFailedException;
import org.eclipse.jgit.internal.storage.file.FileRepository;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class GitTraverseMaster {

    static String connectionQuery = "jdbc:postgresql://localhost:5432/giovanni?user=giovanni&password=giovanni";
    static String sqlInsert = "INSERT INTO public.\"CommitInfoMaster\"(\"Project\", \"Hash\", \"Date\") VALUES ( ?, ?, ?)";

    String projectName;
    File projectFolder;
    Repository repository;
    Git git;

    public GitTraverseMaster(String projectName, File projectFolder) throws Exception {
        this.projectName = projectName;
        this.projectFolder = projectFolder;
        this.repository = new FileRepository(new File(this.projectFolder, ".git"));
        this.git = new Git(this.repository);
    }

    public GitTraverseMaster(String projectName, String projectFolder) throws Exception {
        this(projectName, new File(projectFolder));
    }

    private static String formatDate(Date when) {
        DateFormat df = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
        return df.format(when);
    }

    private static void resetRepo(String revision, File repoDir) throws IOException, GitAPIException {
        try {
            File gitFolder = new File(repoDir, ".git.git");
            Repository repository = FileRepositoryBuilder.create(gitFolder);
            Git git = new Git(repository);

            git.clean().setCleanDirectories(true).call();

            try {
                git.reset().setMode(ResetCommand.ResetType.HARD).call();
                git.checkout().setName(revision).call();
            } catch (JGitInternalException jgitex) {
                if (jgitex.getCause() instanceof LockFailedException) {
                    //try to remove the lock file
                    File lockFile = new File(gitFolder, "index.lock");
                    FileUtils.forceDelete(lockFile);
                    git.reset().setMode(ResetCommand.ResetType.HARD).call();
                    git.checkout().setName(revision).call();
                } else {
                    //unexpected
                    throw jgitex;
                }
            }

        } catch (Exception e) {
            System.out.println("Could not reset the repository, leaving it untouched and proceed: " + e.getMessage());
            System.out.println("Further info: " + e.getCause().getMessage());
        }
    }

    public static void main(String[] args) throws Exception {
        GitTraverseMaster traverse = new GitTraverseMaster(args[0], args[1]);
        if(args.length < 3){
            traverse.printBranches();
            System.exit(1);
        }
        traverse.storeProjectInformation(args[2]);
    }

    private void printBranches() throws Exception {
        printBranches(false);
    }


    public void printBranches(boolean pull) throws Exception {
        List<Ref> branches = git.branchList().call();
        for(Ref b : branches){
            if(pull || !b.getName().contains("pull-request"))
                System.out.println(b.getName());
        }
    }

    public void storeProjectInformation(String branch) throws Exception {
        // open DB connection
        Connection conn = DriverManager.getConnection(connectionQuery);
        long c = 0;
        System.out.println("Branch: " + branch);
        try {
            Iterable<RevCommit> logs = git.log().add(repository.resolve(branch)).call();
            for (RevCommit rev : logs) {
                String hash = rev.getName();
                long date = rev.getCommitterIdent().getWhen().getTime();
                PreparedStatement st = conn.prepareStatement(sqlInsert);
                st.setString(1, this.projectName);
                st.setString(2, hash);
                st.setLong(3, date);
                st.execute();
                c++;
            }
        } finally {
            conn.close();
            System.out.println(c);
        }
    }

}