package gitlet;

import java.io.File;
import java.io.IOException;
import java.util.*;

import static gitlet.Utils.*;

public class RepoHelper {
    /*工具类方法*/
    public static final File CWD = Repository.CWD;
    /** The .gitlet directory. */
    public static final File GITLET_DIR = Repository.GITLET_DIR;
    /* checkout helper*/
    /*
            查找.gitlet/ref/heads/下的指针，
            如果查找不到该branch,则报错
            如果该branch就是当前HEAD指向的branch则输出提示
            如果在当前branch（还没进行branch变更）下有未提交的文档，并且进行checkout后会被删除（没有被变更后的branch跟踪），则报错
            修改完工作目录下的文档后HEAD应指向该branch（不应该提前修改），并且清空saddstage以及rmstage,如果切换了branch
     */

    public static void clearStage(String content) {
        //清空addstage
        File f = join(GITLET_DIR,"addstage");
        List<String> add = plainFilenamesIn(f);
        for (String addname : add) {
            f = join(GITLET_DIR,"addstage", addname);
            restrictedDelete(f);
        }
        //清空rmstage
        f = join(GITLET_DIR,"rmstage");
        List<String> rm = plainFilenamesIn(f);
        for (String rmname : rm) {
            f = join(GITLET_DIR,"rmstage", rmname);
            restrictedDelete(f);
        }
        //修改头指针
        f = join(GITLET_DIR,"HEAD");
        writeContents(f,content);
    }

    public static void changeCWD(Commit commit) {
        Commit head = getHead();
        //先遍历CWD中的文档，
        // 复制一个commit跟踪的blobs，如果该文档在被跟踪的blobs中则将复制的blobs中的该文档删除
        List<String> filename = plainFilenamesIn(CWD);
        Map<String,Blob> blobs = new HashMap<>(commit.blobs);//复制一份blobs，直接获取指针会影响本来的map
        for (int i = 0; i < filename.size(); i++) {
            String fname = filename.get(i);
            File cwd = join(CWD, fname);
            String content = readContentsAsString(cwd);
            boolean inHead = head.blobs.containsKey(fname) && head.blobs.get(fname).contents.equals(content) ,
                    inCommit = commit.blobs.containsKey(fname) && commit.blobs.get(fname).contents.equals(content);
            File f = join(CWD,fname);
            if (inHead && inCommit) {
                writeContents(f,commit.blobs.get(fname).contents);
                blobs.remove(fname);
            }else if (inHead) {
                restrictedDelete(f);
            }else if (inCommit) {
                writeContents(f,commit.blobs.get(fname).contents);
                blobs.remove(fname);
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


    public static Commit findCommitById(String commitId) {
        // 获取 object 目录下的所有文件（所有潜在的提交）
        File objectDir = join(GITLET_DIR, "object");
        List<String> objectFiles = plainFilenamesIn(objectDir);
        if (objectFiles != null && objectFiles.isEmpty()) {
            return null;
        }
        // 寻找以 commitId 开头的对象文件
        for (String objectFile : objectFiles) {
            if (objectFile.startsWith(commitId.substring(0,7))) {
                // 找到匹配的提交，读取并返回
                File commitFile = join(objectDir, objectFile);
                if (commitFile.exists()) {
                    return readObject(commitFile, Commit.class);
                }
            }
        }
        return null;
    }

    public static void findFileInCommit(Commit commit, String filename) {
        //通过filename获取CWD下该文档的内容，转为对应的hash值进行查找,
        //如果commit中有存在的文档名，若工作目录下存在该文档则覆盖，若没有则创建并将内容写入
        //不一定CWD下存在该文档
        Map<String,Blob> entry = commit.blobs;
        if (entry.containsKey(filename)) {
            File f = join(CWD, filename);
            try {
                if (!f.exists()) {
                    f.createNewFile();
                }
                writeContents(f, entry.get(filename).contents);
            } catch (IOException ignore) {}
        }else {
            System.out.println("File does not exist in that commit.");
        }
    }

    public static Commit getHead() {
        File f = join(GITLET_DIR,"HEAD");
        String head = readContentsAsString(f);
        f = join(GITLET_DIR,"object",head);
        return readObject(f, Commit.class);
    }

    /* checkout helper end*/

    public static boolean inCommit(Blob blob) {//检查该文档是否在当前HEAD指向的commit中
        Commit cur = getHead();//查找到当前指向的commit
        Map<String,Blob> blobs = cur.blobs;
        if (blobs.containsKey(blob.filename)) {
            return blobs.get(blob.filename).contents.equals(blob.contents);
        }else {
            return false;
        }
    }

    public static boolean inAddStage(Blob blob) {//检查addstage中是否已经在addstage中
        File addstage = join(Repository.GITLET_DIR, "addstage");
        List<String> addStage = plainFilenamesIn(addstage);
        for (String s : addStage) {
            if (s.equals(blob.hashvalue)) {//当前addstage中已经保存了该文档
                return true;
            }
        }
        return false;
    }

    public static boolean inRmStage(Blob blob) {
        File rmstage = join(Repository.GITLET_DIR, "rmstage");
        List<String> rmStage = plainFilenamesIn(rmstage);
        for (String s : rmStage) {
            if (s.equals(blob.hashvalue)) {
                return true;
            }
        }
        return false;
    }

    public static void addBlob(Blob blob, File f) {//f对应需要保存到的目录的路径
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

    public static void rmBlob(Blob blob, File f) {
        if (!f.isDirectory()) {
            throw
                    new IllegalArgumentException("cannot remove a file from a file");
        }
        File file = join(f,blob.hashvalue);
        restrictedDelete(file);
    }

    public static Commit findSplitPoint(Commit other) {
        Commit cur = getHead();
        Commit copyother = other;
        List<Commit> curbranch = new ArrayList<>();
        List<Commit> otherbranch = new ArrayList<>();
        while (cur.parents != null) {
            curbranch.add(cur.parents.get(0));
            cur = cur.parents.get(0);
        }
        curbranch.add(cur);
        while (copyother.parents != null) {
            otherbranch.add(copyother.parents.get(0));
            copyother = copyother.parents.get(0);
        }
        otherbranch.add(copyother);
        ListIterator<Commit> it1 = curbranch.listIterator(curbranch.size());
        ListIterator<Commit> it2 = otherbranch.listIterator(otherbranch.size());
        while (it1.hasPrevious() && it2.hasPrevious()) {
            Commit element1 = it1.previous();
            Commit element2 = it2.previous();
            if (!element1.hashname.equals(element2.hashname)) {
                break;
            }
            cur = element1;
        }
        return cur;
    }
}
