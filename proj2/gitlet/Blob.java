package gitlet;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import static gitlet.Utils.*;
/*
    当文档未被进行提交时，就以Blob类的形态进行存在于addstage或者rmstage中，
    这就是Blob类被定义出来的意义
 */
public class Blob implements Serializable {
    public final File file;//文档对应路径(工作路径)
    public final String hashvalue;//文档内容的hash值
    public final String contents;//文档内容

    public Blob(String filename) {
        File f = join(Repository.CWD,filename);
        if (!f.exists()) {
            System.out.println("File does not exist.");
            System.exit(0);
        }
        this.file = f;
        this.contents = readContentsAsString(f);
        String content = readContentsAsString(f);
        this.hashvalue = sha1(content);
    }

    public boolean inAddStage(Blob blob) {//检查addstage中是否已经在addstage中
        File addstage = join(Repository.GITLET_DIR, "addstage");
        List<String> addStage = plainFilenamesIn(addstage);
        for (String s : addStage) {
            if (s.equals(blob.hashvalue)) {//当前addstage中已经保存了该文档
                return true;
            }
        }
        return false;
    }
}
