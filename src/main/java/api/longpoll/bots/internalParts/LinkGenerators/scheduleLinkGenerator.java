package api.longpoll.bots.internalParts.LinkGenerators;

import api.longpoll.bots.internalParts.LogMaster;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.jsoup.Jsoup;

import java.io.*;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Properties;

public class scheduleLinkGenerator {
    private static JSONArray scheduleLinkMatcher = null;
    private static String ScheduleLink = null;

    public static boolean initScheduleLink(){
        Properties properties = new Properties();
        try {
            properties.load(new FileInputStream("workFiles/config.properties"));
            ScheduleLink = properties.getProperty("LinkToSchedule");

            if (ScheduleLink.equals("")) {
                return false;
            }
        } catch (IOException e) {
            return false;
        }
        return true;
    }

    public static void updateTimer() throws IOException {
        Calendar calendarTimer = new GregorianCalendar();
        if (calendarTimer.get(Calendar.HOUR_OF_DAY) == 7 && calendarTimer.get(Calendar.MINUTE) == 0) {
            renewDataBase();
            LogMaster.log("[INFO]: Renewing groups database at time: "
                    + calendarTimer.get(Calendar.HOUR_OF_DAY) + ":"
                    + calendarTimer.get(Calendar.MINUTE) + ":"
                    + calendarTimer.get(Calendar.SECOND));
        }
    }

    public static void renewDataBase() throws IOException {
        String page = Jsoup.connect(ScheduleLink).timeout(60000).get().toString();
        page = page.substring(page.indexOf("\"name\"") - 13);
        page = page.substring(0, page.indexOf("]"));
        String[] lines = page.split("}");

        scheduleLinkMatcher = new JSONArray();

        for (String line: lines){
            JSONObject newElement = new JSONObject();
            newElement.put("number", line.substring(21, line.indexOf("\",\"l")));
            newElement.put("id", line.substring(7, 12));
            scheduleLinkMatcher.add(newElement);
        }
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        String jsonString = gson.toJson(scheduleLinkMatcher);
        try (FileWriter file = new FileWriter("workFiles/scheduleMatcher.json")) {
            file.write(jsonString);
            file.flush();
        }
    }

    public String getLinkFromGroupNumber(String number) {
        return (String) scheduleLinkMatcher.stream()
                .filter(element -> ((JSONObject) element).get("number").equals(number))
                .map(obj -> ScheduleLink +((JSONObject) obj).get("id").toString())
                .findFirst()
                .orElse(null);
    }
}
