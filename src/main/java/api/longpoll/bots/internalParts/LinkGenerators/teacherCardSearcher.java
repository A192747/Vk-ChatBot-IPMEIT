package api.longpoll.bots.internalParts.LinkGenerators;

import api.longpoll.bots.internalParts.JsonWorker;
import api.longpoll.bots.internalParts.LogMaster;
import org.json.simple.JSONObject;
import org.json.simple.parser.ParseException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class teacherCardSearcher {
    private static final Map<String, Elements> cacheCard = new ConcurrentHashMap<>();
    private static JSONObject jsonMainArray;

    public static void clearTimer() {
        Calendar calendarTimer = new GregorianCalendar();
        if (calendarTimer.get(Calendar.DAY_OF_WEEK) == Calendar.MONDAY
                && calendarTimer.get(Calendar.HOUR_OF_DAY) == 0
                && calendarTimer.get(Calendar.MINUTE) == 1) {
            cacheCard.clear();
            LogMaster.log("[INFO]: Clearing teacher card cache at time: "
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
        if (cacheCard.containsKey(cacheKey)) {
            try {
                return formatResult(cacheCard.get(cacheKey), String.valueOf(link), cacheKey);
            } catch (IOException | ParseException | URISyntaxException e) {
                LogMaster.error(e.getMessage());
                return "";
            }
        }
        // Формируем ссылку для поиска и запрашиваем данные с сервера
        link = new StringBuilder("https://www.spbstu.ru/university/" +
                "about-the-university/personalities/?arrFilter_ff%5BNAME%5D=");
        for (int i = 0; i < Math.min(3, arr.length); ++i) {
            link.append(arr[i]);
            if (i != arr.length - 1) {
                link.append("+");
            }
        }
        link.append("&arrFilter_pf%5BPOSITION%5D=&arrFilter_pf%5BSCIENCE_TITLE%5D=&arrFilter_" +
                "pf%5BSECTION_ID_1%5D=&arrFilter_pf%5BSECTION_ID_2%5D=&arrFilter_" +
                "pf%5BSECTION_ID_3%5D=&set_filter=Найти");
        try {
            Document document = Jsoup.connect(String.valueOf(link)).timeout(60000).get();

            Elements elements = document.select("h3 > a[href^=\"/university/" +
                    "about-the-university/personalities/\"]");
            // Сохраняем данные в кэше
            cacheCard.put(cacheKey, elements);

            return formatResult(elements, String.valueOf(link), cacheKey);
        } catch (IOException | ParseException | URISyntaxException e) {
            LogMaster.error(e.getMessage());
            return "";
        }
    }

    private static String formatResult(Elements elements, String link, String cacheKey)
            throws IOException, ParseException, URISyntaxException {
        if (elements.size() == 0) {
            return ((JSONObject)(jsonMainArray.get("teacherInfo"))).get("noInfo").toString();
        }

        if (Objects.requireNonNull(elements.first()).text().
                equals(Objects.requireNonNull(elements.last()).text()) && elements.size() != 1) {
            URL url = new URI("http://tinyurl.com/api-create.php?url=" + link).toURL();
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            link = new java.util.Scanner(connection.getInputStream()).useDelimiter("\\A").next();
            cacheCard.remove(cacheKey); //иначе мы запомним не то, что нам нужно

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
                result.append("\nwww.spbstu.ru").append(el.attr("href"));
            }
            result.append('\n');
        }
        if (!show) {
            result.append(((JSONObject) (jsonMainArray.get("teacherInfo"))).get("refineRequest").toString());
        }
        return String.valueOf(result);
    }
}