package gitlet;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static gitlet.Utils.*;
import static gitlet.RepoHelper.*;
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
       |        -heads/ 指向当前分支
       |              -master
       |        -...
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
        Commit commit = getHead();
        while (commit != null && !commit.parents.isEmpty()) {
            if (commit.parents.size() == 1) {
                printLog(commit);
            }else {
                printmergeLog(commit);
            }
            commit = commit.parents.get(0);
        }
        printLog(commit);
    }

    public static void globallog() {
        File f = join(GITLET_DIR,"object");
        List<String> object = plainFilenamesIn(f);
        for (String obj : object) {
            f = join(GITLET_DIR,"object",obj);
            Commit commit = readObject(f, Commit.class);
            printLog(commit);
        }
    }

    private static void printmergeLog(Commit commit) {
        System.out.println("===");
        System.out.println("commit " + commit.hashname);
        String c1 = commit.parents.get(0).hashname, c2 = commit.parents.get(1).hashname;
        String parentsinfo = c1.substring(0,7) + " " + c2.substring(0,7);
        System.out.println("Merge: " + parentsinfo);
        System.out.println("Date: " + commit.date);
        System.out.println(commit.message);
        System.out.println();
    }

    private static void printLog(Commit commit) {
        System.out.println("===");
        System.out.println("commit " + commit.hashname);
        System.out.println("Date: " + commit.date);
        System.out.println(commit.message);
        System.out.println();
    }

    public static void find(String commitmessage) {
        File f = join(GITLET_DIR,"object");
        List<String> object = plainFilenamesIn(f);
        int num = 0;
        for (String obj : object) {
            f = join(GITLET_DIR,"object",obj);
            Commit commit = readObject(f, Commit.class);
            if (commit.message.equals(commitmessage)) {
                num ++;
                System.out.println(commit.hashname);
            }
        }
        if (num == 0) {
            System.out.println("Found no commit with that message.");
        }
    }

    public static void status() {
        printBranches();
        printStagedFiles();
        printRmedFiles();
        System.out.println("=== Modifications Not Staged For Commit ===\n\n=== Untracked Files ===");
    }

    private static void printBranches() {
        System.out.println("=== Branches ===");
        File head  = join(GITLET_DIR,"ref","heads");
        List<String> hd = plainFilenamesIn(head);
        System.out.print("*");
        System.out.println(hd.get(0));
        File f = join(GITLET_DIR,"ref");
        List<String> ref = plainFilenamesIn(f);
        for (String ponit : ref) {
            System.out.println(ponit);
        }
        System.out.println();
    }

    private static void printStagedFiles() {
        System.out.println("=== Staged Files ===");
        File f = join(GITLET_DIR,"addstage");
        List<String> add = plainFilenamesIn(f);
        for (String name : add) {
            f = join(GITLET_DIR,"addstage",name);
            Blob blob = readObject(f, Blob.class);
            System.out.println(blob.filename);
        }
        System.out.println();
    }

    private static void printRmedFiles() {
        System.out.println("=== Removed Files ===");
        File f = join(GITLET_DIR,"rmstage");
        List<String> rm = plainFilenamesIn(f);
        for (String name : rm) {
            f = join(GITLET_DIR,"rmstage",name);
            Blob blob = readObject(f,Blob.class);
            System.out.println(blob.filename);
        }
        System.out.println();
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
        Commit cur = findCommitById(commitname);
        if (cur == null) {
            System.out.println("No commit with that id exists.");
            return;
        }
        findFileInCommit(cur, filename);
    }

    public static void checkoutBranch(String branchname) {
        //branchname不是指指向的commit的hash值！
        File f = join(GITLET_DIR,"ref","heads",branchname);
        if (f.exists()) {
            System.out.println("No need to checkout the current branch.");
            System.exit(0);
        }
        f = join(GITLET_DIR,"ref");
        List<String> ref = plainFilenamesIn(f);
        boolean flag = false;
        //在指针目录下找到目标branch
        for (String refname : ref) {
            if (refname.equals(branchname)) {
                flag = true;
                f = join(GITLET_DIR,"ref",refname);
                String commitname = readContentsAsString(f);
                f = join(GITLET_DIR,"object",commitname);
                Commit commit = readObject(f, Commit.class);
                //修改CWD
                changeCWD(commit);

                //先将heads目录下的文档复制到ref目录下，再删除heads下的文档，将当前branch的文档写入
                f  = join(GITLET_DIR,"ref","heads");
                List<String> head = plainFilenamesIn(f);
                String hd = head.get(0);//获取heads下文档的名称
                //删除旧的head
                f = join(GITLET_DIR,"ref","heads",hd);
                String content = readContentsAsString(f);//获取heads下文档内容
                restrictedDelete(f);//删除heads下的文档
                //将旧head移到ref下
                f = join(GITLET_DIR,"ref",hd);
                try{
                    f.createNewFile();
                }catch (IOException ignore){}
                writeContents(f,content);//在ref下复制一份heads下的文档
                //删除ref下的branch
                f = join(GITLET_DIR,"ref",branchname);
                content = readContentsAsString(f);//获取要转到的branch的文档内容
                restrictedDelete(f);//删除在ref下的该文档
                //移动到heads下
                f = join(GITLET_DIR,"ref","heads",branchname);
                try{
                    f.createNewFile();
                }catch (IOException ignore){}
                writeContents(f,content);//在heads下复制一份该branch的文档内容

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
                break;
            }
        }
        if (!flag) {
            System.out.println("No such branch exists.");
        }
    }

    /* 用于branch*/
    public static void branch(String name) {
        //先检查master是不是该branch
        File f = join(GITLET_DIR,"ref","heads",name);
        if (f.exists()) {
            System.out.println("A branch with that name already exists.");
            System.exit(0);
        }
        //再检查ref下的其他branch
        f = join(GITLET_DIR,"ref",name);
        if (f.exists()) {
            System.out.println("A branch with that name already exists.");
            System.exit(0);
        }
        try {
            f.createNewFile();
        }catch (IOException ignore) {
        }
        File head = join(GITLET_DIR,"HEAD");
        String Head = readContentsAsString(head);
        writeContents(f,Head);
    }
    /* 用于rm-branch*/
    /*
        当不存在该分支时，报错
        当该分支为当前工作分支时，报错
     */
    public static void rmbranch(String name) {
        File f = join(GITLET_DIR,"ref","heads",name);
        if (f.exists()) {
            System.out.println("Cannot remove the current branch.");
            System.exit(0);
        }
        f = join(GITLET_DIR,"ref",name);
        if (!f.exists()) {
            System.out.println("A branch with that name does not exist.");
            System.exit(0);
        }
        restrictedDelete(f);
    }

    public static void reset(String commitid) {
        File f = join(GITLET_DIR,"object",commitid);
        if (!f.exists()) {
            System.out.println("No commit with that id exists.");
            System.exit(0);
        }
        Commit commit = readObject(f, Commit.class);
        changeCWD(commit);
        File head = join(GITLET_DIR,"HEAD");
        writeContents(head,commitid);
    }
}
