package gitlet;

// TODO: any imports you need here

import static gitlet.Utils.*;
import java.io.Serializable;
import java.io.File;
import java.util.Date; // TODO: You'll likely use this in this class
import java.util.HashMap;
import java.util.Map;

/** Represents a gitlet commit object.
 *  TODO: It's a good idea to give a description here of what else this Class
 *  does at a high level.
 *
 *  @author TODO
 */
public class Commit implements Serializable {
    /**
     * TODO: add instance variables here.
     *
     * List all instance variables of the Commit class here with a useful
     * comment above them describing what that variable represents and how that
     * variable is used. We've provided one example for `message`.
     */

    /** The message of this Commit. */
    private String message;

    /** The commit time of this Commit. */
    private String timestamp;

    /** The parent node of this Commit. */
    private String parent;

    /** A map from this commit to parent. */
    private Map<String, String> fileMap;

    /**
     * Default constructor function
     */
    Commit() {
        message = "initial commit";
        timestamp = new Date().toString();
        parent = null;
        fileMap = null;
    }

    Commit(String _message, String _parent) {
        message = _message;
        timestamp = new Date().toString();
        parent = _parent;
        fileMap = new HashMap<>();
    }

    /** Get sha1 code of commit. */
    public String getHash() {
        return Utils.sha1(Utils.serialize(this));
    }

    /** Get construct date of commit. */
    public String getTimestamp() {
        return timestamp;
    }

    /** Get message of commit. */
    public String getMessage() {
        return message;
    }

    public String getParent() {
        return parent;
    }

    public Map<String, String> getFileMap() {
        return fileMap;
    }

    public void updateMap() {
        try {
            /** 1. Copy the fileMap from parent commit. */
            File parentCommitFile = join(Repository.COMMIT_DIR, this.parent);
            Commit parentCommit = readObject(parentCommitFile, Commit.class);
            if (parentCommit.getFileMap() == null) {
                fileMap = new HashMap<>();
            } else {
                fileMap = new HashMap<>(parentCommit.getFileMap());
            }


            File stagedArea = new File(Repository.STAGE_ADD_DIR.toString());
            String[] stagedFiles = stagedArea.list();
            File stagedRemoval = new File(Repository.STAGE_RM_DIR.toString());
            String[] stagedRMFiles = stagedRemoval.list();
            /**
             * 2. Update fileMap using staged_for_addition files and delete staged files.
             * */
            if (stagedFiles.length == 0 && stagedRemoval.length() == 0) {
                throw new GitletException("No changes added to the commit.");
            }
            for (String file: stagedFiles) {
                File ref = join(Repository.STAGE_ADD_DIR, file);
                String content = readContentsAsString(ref);
                String sha1Code = sha1(content);
                File commitFile = join(Repository.OBJECT_DIR, sha1Code);
                writeContents(commitFile, content);
                fileMap.put(file, sha1Code);
                ref.delete();
            }

            /**
             * 3. Update fileMap using staged_for_removal files.
             */
            /** Delete relative mapping from fileName to sha1 code. */
            for (String file: stagedRMFiles) {
                File ref = join(Repository.STAGE_RM_DIR, file);
                fileMap.remove(file);
                ref.delete();
            }
        } catch (GitletException e) {
            System.out.println(e.getMessage());
        }
    }

    public void updateCommit(File commitFile, String sha1Code) {
        String content = "";
        content = content + "commit " + sha1Code;
        content = content + "\nDate: " + timestamp;
        content = content + "\n" + message;
        writeContents(commitFile, content);
    }

    public void updateHEAD(String sha1Code) {
        writeContents(Repository.HEAD_FILE, sha1Code);
    }

    public void saveCommit(String sha1Code) {
        File commitObject = join(Repository.COMMIT_DIR, sha1Code);
        writeObject(commitObject, this);
    }

    public static Commit getLastCommit() {
        String lastCommitSha1 = readContentsAsString(Repository.HEAD_FILE);
        File lastCommit = join(Repository.COMMIT_DIR, lastCommitSha1);
        return readObject(lastCommit, Commit.class);
    }

    public static Commit getCommitFromSha1(String sha1Code) {
        File file = join(Repository.COMMIT_DIR, sha1Code);
        return readObject(file, Commit.class);
    }

    public String getContent(String commitPointer) {
        File logFile = join(Repository.OBJECT_DIR, commitPointer);
        return readContentsAsString(logFile);
    }
}
