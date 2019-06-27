package cf.xmon.chat.utils;


import java.util.Arrays;

public final class Logger
{
    public static void info(final String... logs) {
        Arrays.stream(logs).forEach(x ->{
            System.out.println(x);
        });
    }

    public static void warning(final String... logs) {
        Arrays.stream(logs).forEach(x ->{
            System.out.println(x);
        });
    }

    public static void severe(final String... logs) {
        Arrays.stream(logs).forEach(x ->{
            System.out.println(x);
        });
    }
}
