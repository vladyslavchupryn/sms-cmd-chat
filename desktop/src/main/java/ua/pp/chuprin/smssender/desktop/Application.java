package ua.pp.chuprin.smssender.desktop;

public class Application {
    static {
        System.loadLibrary("alljoyn_java");
    }

    public static void main(String ... args) {
        if (args.length == 2) {
            new Connection(args[0], args[1]).run();
        } else {
            System.out.println("USAGE: <pin> <contact>");
        }
    }
}
