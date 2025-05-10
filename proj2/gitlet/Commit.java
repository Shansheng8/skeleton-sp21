package gitlet;

// TODO: any imports you need here

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

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
    public final String message;//log信息
    public final String date;//创建时间
    public  List<Commit> parents;//父提交
    public  Map<String, Blob> blobs;//被跟踪的文档（工作目录下）到.gitlet下存储的blob之间的映射
    //专门用于checkout，从object目录下查找commit
    public  String hashname;
    /* TODO: fill in the rest of this class. */

    /* 用于构建Commit实例 */
    public Commit(String message, String date) {
        this.message = message;
        this.date = date;
        this.parents = new ArrayList<>();
        this.blobs = new HashMap<>();
    }

    private static Commit getParentCommit() {//获取父提交
        File f = join(Repository.GITLET_DIR,"HEAD");
        String head = readContentsAsString(f);
        f = join(Repository.GITLET_DIR,"object",head);
        return readObject(f,Commit.class);
    }

    private void updateParents() {//更新父提交
        this.parents.add(getParentCommit());
    }

    private void updateBlobs() {//更新Blob列表
        this.blobs = getParentCommit().blobs;
        File fr = join(Repository.GITLET_DIR,"rmstage");
        File fa = join(Repository.GITLET_DIR,"addstage");
        List<String> addstage = plainFilenamesIn(fa);
        List<String> rm = plainFilenamesIn(fr);
        if ((rm == null || rm.isEmpty()) && (addstage == null || addstage.isEmpty())) {
            System.out.println("No changes added to the commit.");
            return;
        }
        if (rm != null) {
            for (String r : rm) {
                fr = join(Repository.GITLET_DIR,"rmstage",r);
                Blob tmp = readObject(fr, Blob.class);
                this.blobs.remove(tmp.filename);
            }
        }
        if (addstage != null) {
            for (String a : addstage) {
                fa = join(Repository.GITLET_DIR,"addstage",a);
                Blob tmp = readObject(fa, Blob.class);
                this.blobs.put(tmp.filename,tmp);
            }
        }
    }

    public static String dateToString(Date date) {
        DateFormat dateFormat = new SimpleDateFormat("EEE MMM d HH:mm:ss yyyy Z", Locale.US);
        dateFormat.setTimeZone(TimeZone.getTimeZone("America/Los_Angeles"));
        return dateFormat.format(date);
    }

    /* 用于init */
    public static void initialCommit() {//初始化第一个commit
        String date = dateToString(new Date(0));
        Commit init = new Commit("initial commit",date);
        init.commit();
    }

    public void updateCommit() {
        this.updateParents();
        this.updateBlobs();
    }

    /* 用于提交commit*/
    /*
        commit的属性有：提交的日志（log） 提交的时间（date）对应的父提交（可能有两个）对应的blobs（一般会包含多个）
        完成提交的完整过程：复制其父提交，扫描addstage与rmstage区域修改对应的blobs，得到一个新的commit,
        将其属性内容转为字符串后使用sha1加密得到对应的hash值，将该值作为文档名保存到object目录下
     */
    public void commit() {//传入commit，将commit保存到object目录中进行存储
        //令获得的sha1值唯一
        String parent = "";
        if (!this.parents.isEmpty()) {
            parent = this.parents.get(0).hashname;
        }
        String filename = sha1(this.message,this.date,parent,this.blobs.toString());
        this.hashname = filename;
        File f = join(Repository.GITLET_DIR,"object",filename);//通过SHA-1加密后的字符串作为文档的名称
        if (!f.exists()) {
            try {
                f.createNewFile();
            }catch (IOException ignore){
            }
        }
        writeObject(f,this);//将commit的信息保存到对应名称为sha1字符串的文档中
        f = join(Repository.GITLET_DIR,"ref","heads");//修改当前分支的末端所指向的commit
        List<String> cur = plainFilenamesIn(f);
        String curname = cur.get(0);
        f = join(Repository.GITLET_DIR,"ref","heads",curname);
        writeContents(f,filename);
        f = join(Repository.GITLET_DIR,"HEAD");
        writeContents(f,filename);//修改HEAD指针指向的commit
        //清理addstage区域
        f = join(Repository.GITLET_DIR,"addstage");
        List<String> add = plainFilenamesIn(f);
        for (String addname : add) {
            f = join(Repository.GITLET_DIR,"addstage", addname);
            restrictedDelete(f);
        }
        //清理rmstage区域
        f = join(Repository.GITLET_DIR,"rmstage");
        List<String> rm = plainFilenamesIn(f);
        for (String rmname : rm) {
            f = join(Repository.GITLET_DIR,"rmstage", rmname);
            restrictedDelete(f);
        }
    }
}
