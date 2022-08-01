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
    private String date;

    /** The parent node of this Commit. */
    private String parent;

    /** A map from this commit to parent. */
    private Map<String, String> fileMap;

    /**
     * Default constructor function
     */
    Commit() {
        message = "initial commit";
        date = new Date().toString();
        parent = null;
        fileMap = null;
    }

    Commit(String _message, String _parent) {
        message = _message;
        date = new Date().toString();
        parent = _parent;
        fileMap = new HashMap<>();
    }

    /** Get sha1 code of commit. */
    public String getHash() {
        return Utils.sha1(Utils.serialize(this));
    }

    /** Get construct date of commit. */
    public String getDate() {
        return date;
    }

    /** Get message of commit. */
    public String getMessage() {
        return message;
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


            /** 2. Update fileMap using staged files and delete staged files. */
            File stagingArea = new File(Repository.STAGE_DIR.toString());
            String[] stagedFiles = stagingArea.list();
            if (stagedFiles.length == 0) {
                throw new GitletException("No changes added to the commit.");
            }
            for (String file : stagedFiles) {
                File ref = join(Repository.STAGE_DIR, file);
                String content = readContentsAsString(ref);
                String sha1Code = sha1(content);
                File commitFile = join(Repository.OBJECT_DIR, sha1Code);
                writeContents(commitFile, content);
                fileMap.put(file, sha1Code);
                ref.delete();
            }
        } catch (GitletException e) {
            System.out.println(e.getMessage());
        }
    }

    public void updateCommit(File commitFile, String sha1Code) {
        String content = "";
        content = content + "commit " + sha1Code;
        content = content + "\nDate: " + date;
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
}
