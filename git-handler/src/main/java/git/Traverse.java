package git;

import org.apache.commons.io.FileUtils;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.ResetCommand;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.JGitInternalException;
import org.eclipse.jgit.errors.LockFailedException;
import org.eclipse.jgit.internal.storage.file.FileRepository;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;

import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Traverse {

    public static void main(String[] args) throws Exception {
        File projectFolder = new File(args[0]);

        File gitDir = new File(projectFolder, ".git.git");
        org.eclipse.jgit.lib.Repository repo = new FileRepository(gitDir);
        Git git = new Git(repo);

        Iterable<RevCommit> logs = git.log().all().call();

        //just print the list
        for (RevCommit rev : logs) {
            System.out.println(rev.getName() + "  -->  " + formatDate(rev.getCommitterIdent().getWhen()));
            RevCommit[] parents = rev.getParents();
            for(int i = 0; i < parents.length; i++){
                RevCommit p = parents[i];
                System.out.println("\t" + p.getName());
            }
        }

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
}