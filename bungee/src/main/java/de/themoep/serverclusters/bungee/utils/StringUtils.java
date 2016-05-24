package de.themoep.serverclusters.bungee.utils;

public class StringUtils {

    public static String join(String[] args, String delimiter) {
        if(args.length == 0)
            return "";
        StringBuilder builder = new StringBuilder(args[0]);
        for(int i = 1; i < args.length; i++) {
            builder.append(delimiter).append(args[i]);
        }
        return builder.toString();
    }
}
