package gitlet;

// TODO: any imports you need here

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date; // TODO: You'll likely use this in this class
import java.util.List;

import static gitlet.Utils.*;

/** Represents a gitlet commit object.
 *  TODO: It's a good idea to give a description here of what else this Class
 *
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
    private final String message;//log信息
    private final String date;//创建时间
    public final List<Commit> parents;//父提交
    public final List<String> blobID;//
    /* TODO: fill in the rest of this class. */

    /* 用于构建Commit实例 */
    public Commit(String message, String date) {
        this.message = message;
        this.date = date;
        this.parents = new ArrayList<>();
        this.blobID = new ArrayList<>();
        getParents();
        getBlobID();
    }
    //TODO：可能有两个父commit
    private void getParents() {
        File f = join(Repository.GITLET_DIR,"ref/heads/master");
        if (readContentsAsString(f).equals("")) {
            return;
        }
        File commitfile = join(Repository.GITLET_DIR,"object",readContentsAsString(f));
        Commit parent = readObject(commitfile,Commit.class);
        this.parents.add(parent);
    }
    //TODO:
    private void getBlobID() {
        return;
    }

    private static String dateToString(Date date) {//将date转为字符串
        SimpleDateFormat dateFormat = new SimpleDateFormat("EEE MMM d HH:mm:ss yyyy Z");
        return dateFormat.format(date);
    }

    /* 用于init */
    public static void initialCommit() {//初始化第一个commit
        String cur = dateToString(new Date(0));
        Commit init = new Commit("Initial commit",cur);
        commit(init);
    }

    /* 用于提交commit*/
    public static void commit(Commit commit) {//传入commit，将commit保存到object目录中进行存储
        String filename = sha1(commit.message,commit.date,commit.parents.toString(),commit.blobID.toString());
        File f = join(Repository.GITLET_DIR,"object",filename);//通过SHA-1加密后的字符串作为文档的名称
        if (!f.exists()) {
            try {
                f.createNewFile();
            }catch (IOException ignore){
            }
        }
        writeObject(f,commit);//将commit的信息保存到对应名称为sha1字符串的文档中
        f = join(Repository.GITLET_DIR,"ref/heads/master");//修改master分支的末端所指向的commit
        writeContents(f,filename);
        f = join(Repository.GITLET_DIR,"HEAD");
        writeContents(f,filename);//修改HEAD指针指向的commit
    }
}
