package gitlet;

import gitlet.Repository.*;

/** Driver class for Gitlet, a subset of the Git version-control system.
 *  @author TODO
 */
public class Main {

    /** Usage: java gitlet.Main ARGS, where ARGS contains
     *  <COMMAND> <OPERAND1> <OPERAND2> ... 
     */
    public static void main(String[] args) {
        if (args.length == 0) {
            System.out.println("Please enter a command.");
            System.exit(0);
        }

        int argsLength = args.length;
        String firstArg = args[0];
        switch(firstArg) {
            case "init":
                assert (argsLength == 1): "Incorrect operands.";
                Repository.init();
                break;
            case "add":
                assert (argsLength == 2): "Incorrect operands.";
                Repository.add(args[1]);
                break;
            case "commit":
                assert (argsLength == 2): "Incorrect operands.";
                Repository.commit(args[1]);
                break;
            case "rm":
                assert (argsLength == 2): "Incorrect operands.";
                Repository.rm(args[1]);
                break;
            case "log":
                assert (argsLength == 1): "Incorrect operands.";
                Repository.log();
                break;
            case "global-log":
                assert (argsLength == 1): "Incorrect operands.";
                Repository.global_log();
                break;
            default:
                System.out.println("No command with that name exists.");
                System.exit(0);
        }
    }
}
