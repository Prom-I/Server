package com.example.tomeettome.Utility;


import java.util.UUID;

public class ConvertFactory {
    public static String getIcsFileNameFromUserId(String userId){
        StringBuilder sb = new StringBuilder();
        sb.append(userId);
        sb.append(".ics");
        return sb.toString();
    }

    public static String getUserIdFromIcsFileName(String icsFileName) {
        return icsFileName.substring(0, icsFileName.length() - 4);
    }

    public static String generateTeamIcsFilename(){
        return UUID.randomUUID() +".ics";
    }
}
