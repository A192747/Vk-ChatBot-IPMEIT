package api.longpoll.bots.internalParts.LinkGenerators;

import api.longpoll.bots.internalParts.JsonWorker;
import api.longpoll.bots.internalParts.LogMaster;
import org.json.simple.JSONObject;
import org.json.simple.parser.ParseException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.jsoup.nodes.Element;

import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.*;


import java.io.*;

public class teacherScheduleSearcher {
    private static final Map<String, Elements> cacheSchedule = new ConcurrentHashMap<>();
    private static JSONObject jsonMainArray;

    public static void clearTimer() {
        Calendar calendarTimer = new GregorianCalendar();
        if (calendarTimer.get(Calendar.DAY_OF_WEEK) == Calendar.MONDAY
                && calendarTimer.get(Calendar.HOUR_OF_DAY) == 0
                && calendarTimer.get(Calendar.MINUTE) == 1) {
            cacheSchedule.clear();
            LogMaster.log("[INFO]: Clearing teacher schedule cache at time: "
                    + calendarTimer.get(Calendar.HOUR_OF_DAY) + ":"
                    + calendarTimer.get(Calendar.MINUTE) + ":"
                    + calendarTimer.get(Calendar.SECOND));
        }
    }

    public static String find(String searchLine) {
        try {
            jsonMainArray = JsonWorker.renewAnswersJson();
        } catch (IOException | ParseException e) {
            LogMaster.error(e.getMessage());
        }
        // Проверяем, есть ли данные в кэше
        StringBuilder link = new StringBuilder();
        String[] arr = searchLine.split(" ");
        String cacheKey = String.join("_", arr);
        if (cacheSchedule.containsKey(cacheKey)) {
            return formatResult(cacheSchedule.get(cacheKey), String.valueOf(link));
        }
        // Формируем ссылку для поиска и запрашиваем данные с сервера
        link = new StringBuilder("https://ruz.spbstu.ru/search/teacher?q=");
        for (int i = 0; i < Math.min(3, arr.length); ++i){
            link.append(arr[i]);
            if (i != arr.length - 1)
                link.append("%20");
        }
        try {
            Document document = Jsoup.connect(String.valueOf(link)).timeout(60000).get();
            Elements elements = document.select("a[class=\"search-result__link\"]");

            // Сохраняем данные в кэше
            cacheSchedule.put(cacheKey, elements);
            return formatResult(elements, String.valueOf(link));
        } catch (IOException e) {
            LogMaster.error(e.getMessage());
        }
        return "";
    }

    private static String formatResult(Elements elements, String link) {
        if (elements.size() == 0) {
            return ((JSONObject)(jsonMainArray.get("teacherInfo"))).get("noHuman").toString();
        }

        if (Objects.requireNonNull(elements.first()).text().equals(Objects.requireNonNull(elements.last()).text())
                && elements.size() != 1) {
            return ((JSONObject)(jsonMainArray.get("teacherInfo"))).get("sameFIO").toString() + link;
        }

        StringBuilder result = new StringBuilder();
        boolean show = true;
        if (elements.size() > 1) {
            result = new StringBuilder(((JSONObject) (jsonMainArray.get("teacherInfo"))).get("manyResults").toString());
            show = false;
        }
        for (Element el: elements) {
            result.append(el.text());
            if (show) {
                result.append("\nhttps://ruz.spbstu.ru").append(el.attr("href"));
            }
            result.append('\n');
        }
        if (!show) {
            result.append(((JSONObject) (jsonMainArray.get("teacherInfo"))).get("refineRequest").toString());
        }
        return String.valueOf(result);
    }
}