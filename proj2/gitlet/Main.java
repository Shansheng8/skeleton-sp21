package gitlet;

import java.io.File;

/** Driver class for Gitlet, a subset of the Git version-control system.
 *  @author TODO
 */
public class Main {

    /** Usage: java gitlet.Main ARGS, where ARGS contains
     *  <COMMAND> <OPERAND1> <OPERAND2> ... 
     */
    public static void main(String[] args) {
        // TODO: what if args is empty?
        String firstArg = args[0];
        switch(firstArg) {
            case "init":
                //handle the `init` command
                Repository.initializeRepo();
                break;
            case "add":
                // handle the `add [filename]` command
                Repository.addFile(args[1]);
                break;
            // TODO: FILL THE REST IN
            case "commit":
                Commit commit = new Commit(args[1]);
                commit.updateCommit();
                commit.commit();
                break;
            case "rm":
                Repository.rmFile(args[1]);
                break;
            case "log":
                break;

            /*case"global-log":
                break;
            case "find":
                break;
            case "status":
                break;

             */
            case "checkout":
                break;
            /*
            case "branch":
                break;
            case "rm-branch":
                break;
            case "reset":
                break;
            case "merge":
                break;

             */
        }
    }
}
