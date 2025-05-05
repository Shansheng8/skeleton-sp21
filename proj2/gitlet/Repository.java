package gitlet;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
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
    /* 用于log*/
    public static void log() {
        File f = join(GITLET_DIR,"HEAD");
        String head = readContentsAsString(f);
        f = join(GITLET_DIR,"object",head);
        Commit commit = readObject(f, Commit.class);
        while (commit != null && !commit.date.equals("08:00:00 UTC, Thursday, 1 January 1970")) {
            if (commit.parents.size() == 1) {
                printLog(commit);
            }else {
                printmergeLog(commit);
            }
            commit = commit.parents.get(0);
        }
        printLog(commit);
    }
    /* 用于checkout*/
    /*
        checkout存在三种使用情况
        注意checkout的目的是修改工作目录下的文档，因此不会涉及修改.gitlet目录下的文件
        1. checkout -- [file name]
            通过HEAD获取当前正在跟踪的commit,查找其中的blobs,
            如果commit中没有对应的文档名，则报错
            如果commit中有存在的文档名，若工作目录下存在该文档则覆盖，若没有则创建并将内容写入
        2. checkout [commit id] -- [file name]
            通过commit id找到对应的commit（此处会进行递归查找，注意base case）
            如果没有查找到对应的commit,则报错
            如果查找到则接下来过程与1.相同（也会有报错）
        3. checkout [branch name]
            查找.gitlet/ref/heads/下的指针，
            如果查找不到该branch,则报错
            如果该branch就是当前HEAD指向的branch则输出提示
            如果在当前branch（还没进行branch变更）下有未提交的文档，并且进行checkout后会被删除（没有被变更后的branch跟踪），则报错
            修改完工作目录下的文档后HEAD应指向该branch（不应该提前修改），并且清空saddstage以及rmstage,如果切换了branch
     */

    public static void checkoutFile(String filename) {
        Commit cur = getHead();
        //遍历map搜索符合对应filename的blob
        findFileInCommit(cur, filename);
    }
    //TODO:还没考虑存在两个父提交的情况
    public static void checkoutCommitFile(String commitname, String filename) {
        //先向上查找对应的commit（使用的name是对应的hash值）
        Commit cur = getHead();
        while (!cur.parents.isEmpty()) {
            if (readCommitAsString(cur).equals(commitname)) {
                findFileInCommit(cur, filename);
                System.exit(0);
            }
            cur = cur.parents.get(0);
        }
        System.out.println("No commit with that id exists.");
    }
    /*
            查找.gitlet/ref/heads/下的指针，
            如果查找不到该branch,则报错
            如果该branch就是当前HEAD指向的branch则输出提示
            如果在当前branch（还没进行branch变更）下有未提交的文档，并且进行checkout后会被删除（没有被变更后的branch跟踪），则报错
            修改完工作目录下的文档后HEAD应指向该branch（不应该提前修改），并且清空saddstage以及rmstage,如果切换了branch
     */

    private static void changeCWD(Commit commit) {
        Commit head = getHead();
        //先遍历CWD中的文档，
        // 复制一个commit跟踪的blobs，如果该文档在被跟踪的blobs中则将复制的blobs中的该文档删除
        List<String> filename = plainFilenamesIn(CWD);
        Map<String,Blob> blobs = new HashMap<>(commit.blobs);//复制一份blobs，直接获取指针会影响本来的map
        //都转为hash值
        for (int i = 0; i < filename.size(); i++) {
            String hashname = readFileInCWDWithSHA1(filename.get(i));
            boolean inHead = head.blobs.containsKey(hashname), inCommit = head.blobs.containsKey(hashname);
            File f = join(CWD,filename.get(i));
            if (inHead && inCommit) {
                writeContents(f,commit.blobs.get(hashname).contents);
                blobs.remove(hashname);
            }else if (inHead) {
                restrictedDelete(f);
            }else if (inCommit) {
                writeContents(f,commit.blobs.get(hashname).contents);
                blobs.remove(hashname);
            }else {//两者都未进行跟踪，报错
                System.out.println("There is an untracked file in the way; delete it, or add and commit it first.");
                System.exit(0);
            }
        }
        //若当前branch跟踪了但是CWD中没有还得进行添加
        blobs.forEach((k,v)->{
            File f = join(CWD,k);
            try{
                f.createNewFile();
            }catch (IOException ignore){}
            writeContents(f,v.contents);
        });
    }

    public static void checkoutBranch(String branchname) {
        File f = join(GITLET_DIR,"ref","heads");
        List<String> ref = plainFilenamesIn(f);
        for (String refname : ref) {
            if (refname.equals(branchname)) {
                f = join(GITLET_DIR,"HEAD");
                String head = readContentsAsString(f);
                if (refname.equals(head)) {
                    System.out.println("No need to checkout the current branch.");
                    System.exit(0);
                }
                //获取要更改的branch指向的commit
                f = join(GITLET_DIR,"object",refname);
                Commit commit = readObject(f, Commit.class);
                //修改CWD
                changeCWD(commit);
                //先通过.gitlet/ref/heads/branchname获取指向该commit的hash值，再将HEAD指向该commit
                f  = join(GITLET_DIR,"ref","heads",refname);
                String branch = readContentsAsString(f);
                f = join(GITLET_DIR,"HEAD");
                writeContents(f,branch);
                //清空addstage
                f = join(GITLET_DIR,"addstage");
                List<String> add = plainFilenamesIn(f);
                for (String addname : add) {
                    f = join(f, addname);
                    restrictedDelete(f);
                }
                //清空rmstage
                f = join(GITLET_DIR,"rmstage");
                List<String> rm = plainFilenamesIn(f);
                for (String rmname : rm) {
                    f = join(f, rmname);
                    restrictedDelete(f);
                }
            }
        }
        System.out.println("No such branch exists.");
    }
    /* 用于branch*/
    public static void branch(String name) {
        File f = join(GITLET_DIR,"ref","heads",name);
        if (f.exists()) {
            System.out.println("A branch with that name already exists.");
            System.exit(0);
        }
        try {
            f.createNewFile();
        }catch (IOException ignore) {
        }
    }
    /* 用于rm-branch*/
    /*
        当不存在该分支时，报错
        当该分支为当前工作分支时，报错
     */
    public static void rmbranch(String name) {
        File f = join(GITLET_DIR,"ref","heads",name);
        if (!f.exists()) {
            System.out.println("A branch with that name does not exist.");
            System.exit(0);
        }
        File hd = join(GITLET_DIR,"HEAD");
        String head = readContentsAsString(hd);
        if (name.equals(head)) {
            System.out.println("Cannot remove the current branch.");
            System.exit(0);
        }
        restrictedDelete(f);
    }
    /*工具类方法*/

    /* checkout helper*/

    private static String readFileInCWDWithSHA1(String filename) {
        File f = join(CWD,filename);
        if (!f.exists()) {
            System.out.println("File does not exist in the CWD.");
            System.exit(0);
        }
        String content = readContentsAsString(f);
        return sha1(content);
    }

    private static void findFileInCommit(Commit commit, String filename) {
        //通过filename获取CWD下该文档的内容，转为对应的hash值进行查找,
        //如果commit中有存在的文档名，若工作目录下存在该文档则覆盖，若没有则创建并将内容写入
        //TODO：不一定CWD下存在该文档

        commit.blobs.forEach((k,v)->{
            if (v.filename.equals(filename)) {
                File f = join(CWD,filename);
                if (!f.exists()) {
                    try{
                        f.createNewFile();
                    }catch (IOException ignore) {}
                }
                writeContents(f,v.contents);
                System.exit(0);
            }
        });
        System.out.println("File does not exist in that commit.");
        System.exit(0);
    }

    private static Commit getHead() {
        File f = join(GITLET_DIR,"HEAD");
        String head = readContentsAsString(f);
        f = join(GITLET_DIR,"object",head);
        return readObject(f, Commit.class);
    }

    /* checkout helper end*/

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
