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
                validateNumArgs("init",args,1);
                Repository.initializeRepo();
                break;
            case "add":
                // handle the `add [filename]` command
                validateNumArgs("add",args,2);
                Repository.addFile(args[1]);
                break;
            // TODO: FILL THE REST IN
            case "commit":
                validateNumArgs("commit",args,2);
                Commit commit = new Commit(args[1]);
                commit.updateCommit();
                commit.commit();
                break;
            case "rm":
                validateNumArgs("rm",args,1);
                Repository.rmFile(args[1]);
                break;
            case "log":
                validateNumArgs("log",args,1);
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
                validateNumArgs("branch",args,2);
                Repository.branch(args[1]);
                break;
            case "rm-branch":
                // rm-branch [branch name]
                validateNumArgs("rm-branch",args,2);
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

    public static void validateNumArgs(String cmd, String[] args, int n) {
        if (args.length != n) {
            throw new RuntimeException(
                    String.format("Invalid number of arguments for: %s.", cmd));
        }
    }
}
