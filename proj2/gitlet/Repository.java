package gitlet;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;

import static gitlet.Utils.*;
// TODO: any imports you need here

/** Represents a gitlet repository.
 *  TODO: It's a good idea to give a description here of what else this Class
 *  does at a high level.
 *
 *  @author TODO
 */
public class Repository {
    /**
     * TODO: add instance variables here.
     *
     * List all instance variables of the Repository class here with a useful
     * comment above them describing what that variable represents and how that
     * variable is used. We've provided two examples for you.
     */

    /** The current working directory. */
    public static final File CWD = new File(System.getProperty("user.dir"));
    /** The .gitlet directory. */
    public static final File GITLET_DIR = join(CWD, ".gitlet");

    /* TODO: fill in the rest of this class. */

    /*
       初始化本地仓库，若没有.gitlet这个目录则创建，若有则报错
       并且创建头指针、用于储存指针的目录以及储存文件的区域
       同时提交一个没有任何内容的commit（带有message timestamp）
       目录结构如下：
       |.gitlet/
       |    -HEAD 头指针
       |    -object 用于存储提交与文件
       |          -commits and blobs
       |    -ref/ 用于存储每个分支末端的指针
       |        -heads/
       |              -master
       |              -...
       |    -addstage 暂存区
       |    -rmstage  删除区
       |

     */

    /* 用于init*/
    public static void initializeRepo() {
        if (GITLET_DIR.exists()) {
            System.out.println("A Gitlet version-control system already exists in the current directory.");
            System.exit(0);
        } else {
            GITLET_DIR.mkdir();
        }
        File f = join(GITLET_DIR, "HEAD");//创建HEAD指针，用文档进行储存
        if (!f.exists()) {
            try {
                f.createNewFile();
            }catch (IOException ignore){
            }
        }
        f = join(GITLET_DIR,"ref");//创建ref目录
        f.mkdir();
        f = join(f,"heads");//创建heads目录
        f.mkdir();
        f = join(f,"master");//创建master指针
        if (!f.exists()) {
            try {
                f.createNewFile();
            }catch (IOException ignore){
            }
        }
        f = join(GITLET_DIR,"object");//创建object目录
        f.mkdir();
        f = join(GITLET_DIR,"addstage");//创建addstage目录来储存添加的文档
        f.mkdir();
        f = join(GITLET_DIR,"rmstage");//创建rmstage目录来储存被删除的文档
        f.mkdir();
        Commit.initialCommit();//进行初始commit
    }
    /* 用于add*/

    /*
        添加文档，若文档不在addstage并且不在当前head指向的commit中则添加到addstage
        若文档与当前head指向的commit中的文档一样则不添加到addstage中，
        但是若此时addstage中已经添加了该文档（当文档被修改再改回到原样时会发生）则应该删除addstage中的该文档
     */
    public static void addFile(String filename) {
        Blob blob = new Blob(filename);//通过传入的文档名创建对应的Blob实例
        //先检查当前commit中是否已经记录了该文档
        boolean inCommit = inCommit(blob), inAddStage = inAddStage(blob);
        if (inCommit) {
            if (inAddStage) {//同时在commit与addstage中应该删除addstage中的该文档
                File f = join(GITLET_DIR, "addstage");
                rmBlob(blob,f);
            }
        }else {
            if (!inAddStage) {
                File f = join(GITLET_DIR,"addstage");//以文档内容对应的hash值作为文档名保存到addstage中
                addBlob(blob,f);
            }
        }
        // 注意：object是存储提交后的commits以及blobs的，若没进行提交是不会存到object目录下的
    }
    /* 用与rm*/
    /*  删除文档有三种情况
        第一种情况，当文档刚进addstage中不在当前head指向的commit中时,只需要删除addstage中的文档
        第二种情况，当文档在addstage也在当前head指向的commit中时，既需要删除addstage中的文档也要添加到rmstage中，以便于在下一次commit时记录
        第三种情况，当文档不在addstage但是在当前head指向的commit中时，需要添加到rmstage中
        第四种情况，都不在，报错

        是否要考虑rmstage已经添加了该文档？不需要rmstage的功能就是将已经被commit跟踪的blob进行删除
     */
    public static void rmFile(String filename) {
        Blob blob = new Blob(filename);
        boolean inCommit = inCommit(blob), inAddStage = inAddStage(blob);
        if (inCommit) {
            if (inAddStage) {//文档在addstage也在当前head指向的commit中既需要删除addstage中的文档也要添加到rmstage中
                File f = join(GITLET_DIR, "addstage");
                rmBlob(blob,f);
                f = join(GITLET_DIR, "rmstage");
                addBlob(blob,f);
            }else {//文档不在addstage但是在当前head指向的commit中时，需要添加到rmstage中
                File f = join(GITLET_DIR, "rmstage");
                addBlob(blob,f);
            }
        }else {
            if (inAddStage) {//文档刚进addstage中不在当前head指向的commit中,只需要删除addstage中的文档
                File f = join(GITLET_DIR, "addstage");
                rmBlob(blob,f);
            }else {//既不在addstage中也不在当前commit中，报错
                System.out.println("No reason to remove the file.");
            }
        }
    }

    public static void log() {
        File f = join(GITLET_DIR,"HEAD");
        String head = readContentsAsString(f);
        f = join(GITLET_DIR,"object",head);
        Commit commit = readObject(f, Commit.class);
        while (commit != null && !commit.date.equals("08:00:00 UTC, Thursday, 1 January 1970")) {
            printLog(commit);
            commit = commit.parents.get(0);
        }
        printLog(commit);
    }

    /*工具类方法*/
    private static String readCommitAsString(Commit commit) {
        return sha1(commit.message,commit.date,commit.parents.toString(),commit.blobs.toString());
    }

    private static void printmergeLog(Commit commit) {
        System.out.println("===");
        System.out.println("commit " + readCommitAsString(commit));
        String c1 = readCommitAsString(commit.parents.get(0)), c2 = readCommitAsString(commit.parents.get(1));
        String parentsinfo = c1.substring(0,7) + " " + c2.substring(0,7);
        System.out.println("Merge: " + parentsinfo);
        System.out.println("Date: " + commit.date);
        System.out.println(commit.message);
        System.out.println();
    }

    private static void printLog(Commit commit) {
        System.out.println("===");
        System.out.println("commit " + readCommitAsString(commit));
        System.out.println("Date: " + commit.date);
        System.out.println(commit.message);
        System.out.println();
    }

    private static boolean inCommit(Blob blob) {//检查该文档是否在当前HEAD指向的commit中
        File head = join(Repository.GITLET_DIR, "HEAD");
        String Head = readContentsAsString(head);
        File f = join(Repository.GITLET_DIR,"object",Head);
        Commit cur = readObject(f,Commit.class);//查找到当前指向的commit
        Map<String,Blob> blobs = cur.blobs;
        return blobs.containsKey(blob.hashvalue);
    }

    private static boolean inAddStage(Blob blob) {//检查addstage中是否已经在addstage中
        File addstage = join(Repository.GITLET_DIR, "addstage");
        List<String> addStage = plainFilenamesIn(addstage);
        for (String s : addStage) {
            if (s.equals(blob.hashvalue)) {//当前addstage中已经保存了该文档
                return true;
            }
        }
        return false;
    }

    private static void addBlob(Blob blob, File f) {//f对应需要保存到的目录的路径
        if (!f.isDirectory()) {
            throw
                    new IllegalArgumentException("cannot create a new file in a file");
        }
        File file = join(f,blob.hashvalue);//以文档内容对应的hash值作为文档名保存到addstage中
        if (!file.exists()) {
            try {
                file.createNewFile();
            }catch (IOException ignore){
            }
        }
        writeObject(file,blob);//向addstage中写入对应的内容
    }

    private static void rmBlob(Blob blob, File f) {
        if (!f.isDirectory()) {
            throw
                    new IllegalArgumentException("cannot remove a file from a file");
        }
        File file = join(f,blob.hashvalue);
        restrictedDelete(file);
    }
}
