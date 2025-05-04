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
            case "commit":
                if (args.length != 2) {
                    System.out.println("Please enter a commit message.");
                    System.exit(0);
                }
                Commit commit = new Commit(args[1]);
                commit.updateCommit();
                commit.commit();
                break;
            case "rm":
                Repository.rmFile(args[1]);
                break;
            case "log":
                Repository.log();
                break;

            /*case"global-log":
                break;
            case "find":
                break;
            case "status":
                break;

             */
            case "checkout":
                switch (args.length) {
                    case 2:
                        Repository.checkoutBranch(args[1]);
                        break;
                    case 3:
                        Repository.checkoutFile(args[2]);
                        break;
                    case 4:
                        Repository.checkoutCommitFile(args[1], args[3]);
                        break;
                }
                break;
            case "branch":
                // branch [branch name]
                Repository.branch(args[1]);
                break;
            case "rm-branch":
                // rm-branch [branch name]
                Repository.rmbranch(args[1]);
                break;
            /*
            case "reset":
                break;
            case "merge":
                break;

             */
        }
    }
}
