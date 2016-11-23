package net.lastrik.botTest;

public class Main {

    public static void main(String[] args) {
        if (args.length < 1) {
            System.out.println("Please enter the token as an argument");
        } else {
            Bot bot = new Bot(args[0]);
        }
    }

}
