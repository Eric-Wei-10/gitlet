package gitlet;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import static gitlet.Utils.*;

/** Represents a gitlet repository.
 *  TODO: It's a good idea to give a description here of what else this Class
 *  does at a high level.
 *
 *  @author TODO
 */
public class Repository {
    /**
     *
     *
     * List all instance variables of the Repository class here with a useful
     * comment above them describing what that variable represents and how that
     * variable is used. We've provided two examples for you.
     */

    /** The current working directory. */
    public static final File CWD = new File(System.getProperty("user.dir"));
    /** The .gitlet directory. */
    public static final File GITLET_DIR = join(CWD, ".gitlet");
    /** The object directory. */
    public static final File OBJECT_DIR = join(GITLET_DIR, "object");
    /** The stage directory. */
    public static final File STAGE_ADD_DIR = join(GITLET_DIR, "staged_for_addition");
    /** The stage removal directory. */
    public static final File STAGE_RM_DIR = join(GITLET_DIR, "staged_for_removal");
    /** The commit history directory. */
    public static final File COMMIT_DIR = join(GITLET_DIR, "commit");
    /** The HEAD file. */
    public static final File HEAD_FILE = join(GITLET_DIR, "HEAD");

    /** Implement init command. */
    public static void init() {
        try {
            if (GITLET_DIR.exists()) {
                throw new GitletException("A Gitlet version-control system already exists in the current directory.");
            }

            GITLET_DIR.mkdir();
            OBJECT_DIR.mkdir();
            STAGE_ADD_DIR.mkdir();
            STAGE_RM_DIR.mkdir();
            COMMIT_DIR.mkdir();

            Commit commit = new Commit();
            String sha1Code = commit.getHash();  // Get sha1 code of the Commit object
            File commitFile = join(OBJECT_DIR, sha1Code);
            /** Update the commit file. */
            commit.updateCommit(commitFile, sha1Code);

            /** Create and set HEAD pointer. */
            HEAD_FILE.createNewFile();
            writeContents(HEAD_FILE, sha1Code);
            /** Save the commit object into COMMIT_DIR. */
            File commitObject = join(COMMIT_DIR, sha1Code);
            writeObject(commitObject, commit);
        } catch (GitletException e) {
            System.out.println(e.getMessage());
        } catch (Exception e) {
            System.out.println("Cannot create HEAD file.");
        }
    }

    /** Implement add command. */
    public static void add(String fileName) {
        try {
            File file = join(CWD, fileName);
            /** If there is no such file in the CWD. */
            if (!file.exists()) {
                throw new GitletException("File does not exist.");
            }

            /** Get the new file's information. */
            String content = readContentsAsString(file);
            String sha1Code = sha1(content);

            File stagedFile = join(STAGE_ADD_DIR, fileName);
            File commitFile = join(OBJECT_DIR, sha1Code);
            /** If the file has been committed. */
            if (commitFile.exists()) {
                if (stagedFile.exists()) {
                    stagedFile.delete();
                }
            } else {
                if (!stagedFile.exists()) {
                    stagedFile.createNewFile();
                }
                writeContents(stagedFile, content);
            }
        } catch (GitletException e) {
            System.out.println(e.getMessage());
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    public static void commit(String message) {
        /** Initialize new Commit. */
        String parent = readContentsAsString(HEAD_FILE);
        Commit newCommit = new Commit(message, parent);
        newCommit.updateMap();

        String sha1Code = newCommit.getHash();
        File commitFile = join(OBJECT_DIR, sha1Code);
        newCommit.updateCommit(commitFile, sha1Code);
        newCommit.updateHEAD(sha1Code);
        newCommit.saveCommit(sha1Code);
    }

    public static void rm(String fileName) {
        try {
            /**
             * If there is such file in the staged_for_addition area, delete
             * it in this area.
             */
            File stagedFile = join(STAGE_ADD_DIR, fileName);
            System.out.println(stagedFile.toString());
            if (stagedFile.exists()) {
                stagedFile.delete();
                return;
            }

            /**
             * If the file has been tracked in the current commit.
             */
            Commit lastCommit = Commit.getLastCommit();
            Map<String, String> fileMap = new HashMap<>(lastCommit.getFileMap());
            if (fileMap.containsKey(fileName)) {
                File file = new File(fileName);
                file.delete();
                File stagedForRemovalFile = join(STAGE_RM_DIR, fileName);
                stagedForRemovalFile.createNewFile();
                return;
            }

            /**
             * The file is neither staged nor tracked by the head commit.
             */
            throw new GitletException("No reason to remove the file.");
        } catch (GitletException e) {
            System.out.println(e.getMessage());
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    /**
     * TODO: Not implement the case of two parent
     */
    public static void log() {
        String commitPointer = readContentsAsString(HEAD_FILE);
        Commit commit = Commit.getCommitFromSha1(commitPointer);

        while (commit.getParent() != null) {
            System.out.println("===");
            System.out.println(commit.getContent(commitPointer));
            System.out.println();

            commitPointer = commit.getParent();
            commit = Commit.getCommitFromSha1(commitPointer);
        }

        System.out.println("===");
        System.out.println(commit.getContent(commitPointer));
    }

    public static void global_log() {
        String[] commitObjectSha1CodeList = COMMIT_DIR.list();
        for (String commitObjectSha1Code: commitObjectSha1CodeList) {
            File commitFile = join(OBJECT_DIR, commitObjectSha1Code);
            String commitContent = readContentsAsString(commitFile);

            System.out.println("===");
            System.out.println(commitContent);
            System.out.println();
        }
    }
}
