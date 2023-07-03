package api.longpoll.bots;

import api.longpoll.bots.internalParts.BotInit;
import api.longpoll.bots.internalParts.JsonWorker;
import api.longpoll.bots.http.params.AttachableParam;
import api.longpoll.bots.http.params.MessagePhoto;
import api.longpoll.bots.internalParts.LinkGenerators.scheduleLinkGenerator;
import api.longpoll.bots.internalParts.LinkGenerators.teacherCardSearcher;
import api.longpoll.bots.internalParts.LinkGenerators.teacherScheduleSearcher;
import api.longpoll.bots.internalParts.LogMaster;
import api.longpoll.bots.internalParts.MyKeyBoard;
import api.longpoll.bots.model.objects.additional.Keyboard;

import api.longpoll.bots.exceptions.VkApiException;
import api.longpoll.bots.model.events.messages.*;
import api.longpoll.bots.model.objects.basic.Message;

import java.io.*;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.json.simple.JSONObject;
import org.json.simple.JSONArray;
import org.json.simple.parser.ParseException;

import java.io.File;
import java.io.IOException;

public class VK_BOT extends LongPollBot {
    static Properties properties = new Properties();
    static ArrayList<Integer> adminsId = new ArrayList<>();
    static String[] namesSurnamesArray;
    private static JSONArray startJsonArray;
    private static JSONArray greetingsJsonArray;
    private static JSONArray FAQJsonArray;
    private static JSONArray dialogueJsonArray;
    private static JSONArray scheduleJsonArray;
    private static JSONArray teachSearchJsonArray;
    private static JSONArray teacherScheduleSearchJsonArray;
    private static JSONArray teacherCardSearcherJsonArray;
    private static JSONArray newsSendJsonArray;
    private static JSONArray settingsJsonArray;
    private enum State {
        start,
        greetings,
        schedule,
        faq,
        teachSearch,
        settings,
        newsCreate,
        newsSend,
        communityDialogue,
        teacherCardSearch,
        teacherScheduleSearch,
    }

    static private class StateObj {
        private State state;

        public StateObj() {
            state = State.greetings;
        }

        public void setState(State newState) {
            state = newState;
        }

        public void setState(String newState) {
            switch (newState){
                case "start" -> state = State.start;
                case "greetings" -> state = State.greetings;
                case "schedule" -> state = State.schedule;
                case "faq" -> state = State.faq;
                case "teacherSearch" -> state = State.teachSearch;
                case "settings" -> state = State.settings;
                case "newsCreate" -> state = State.newsCreate;
                case "newsSend" -> state = State.newsSend;
                case "communityDialogue" -> state = State.communityDialogue;
                case "teacherCardSearch" -> state = State.teacherCardSearch;
                case "teacherScheduleSearch" -> state = State.teacherScheduleSearch;
            }
        }

        public State getState() {
            return state;
        }
    }
    private static final Map<Integer, StateObj> userState = new HashMap<>();
    private static String news = "";

    private static void initJsonArrays() throws ParseException, IOException {
        JSONObject jsonMainArray = JsonWorker.renewAnswersJson();
        startJsonArray = (JSONArray) jsonMainArray.get("start");
        greetingsJsonArray = (JSONArray) jsonMainArray.get("greetings");
        settingsJsonArray = (JSONArray) jsonMainArray.get("settings");
        FAQJsonArray = (JSONArray) jsonMainArray.get("faq");
        dialogueJsonArray = (JSONArray) jsonMainArray.get("communityDialogue");
        scheduleJsonArray = (JSONArray) jsonMainArray.get("schedule");
        teachSearchJsonArray = (JSONArray) jsonMainArray.get("teachSearch");
        teacherScheduleSearchJsonArray = (JSONArray) jsonMainArray.get("teacherScheduleSearch");
        teacherCardSearcherJsonArray = (JSONArray) jsonMainArray.get("teacherCardSearch");
        newsSendJsonArray = (JSONArray) jsonMainArray.get("newsSend");
    }

    public static void makeFolderEmpty(String str) {
        File folder = new File(str);
        File[] files1 = folder.listFiles();
        if (files1 != null) {
            for (File file : files1) {
                if (file.isFile()) {
                    if (file.delete()) {
                        LogMaster.log("File \"" + file + "\" deleted.");
                    } else {
                        LogMaster.log("File \"" + file + "\" not deleted.");
                    }
                }
            }
        }
    }

    private static void updateUserState(Integer userId) {
        if (userState.get(userId) == null) {
            userState.put(userId, new StateObj());
            userState.get(userId).setState(State.start);
        }
    }

    @Override
    public void onMessageNew(MessageNewEvent messageNew) throws IOException {
        boolean modifiedMsg = false;
        boolean modifiedKeyBoard = false;

        Message message = messageNew.getMessage();
        JSONObject object = JsonWorker.getAnswersObjJson(startJsonArray, message.getText());

        String msg = "";
        Keyboard kb = MyKeyBoard.setKeyboard(object.get("keyboard").toString());
        AttachableParam att = null;

        updateUserState(message.getFromId());
        switch (userState.get(message.getFromId()).getState()) {
            case start -> {
                JsonWorker.addUserId(message.getFromId());
                object = JsonWorker.getAnswersObjJson(startJsonArray, message.getText());
                userState.get(message.getFromId()).setState(object.get("nextState").toString());
                MessagePhoto photo = new MessagePhoto(getAccessToken(),
                        message.getPeerId(), new File("workFiles/mainPict.jpg"));
                att = (object.containsKey("hasImage")) ? photo : null;
            }
            case greetings -> {
                object = JsonWorker.getAnswersObjJson(greetingsJsonArray, message.getText());
                userState.get(message.getFromId()).setState(object.get("nextState").toString());
                if (object.get("keyboard") instanceof JSONObject) {
                    modifiedKeyBoard = true;
                    if (adminsId.stream().anyMatch(element -> element.equals(message.getFromId()))) {
                        kb = MyKeyBoard.setKeyboard(((JSONObject)
                                object.get("keyboard")).get("admin").toString());
                    } else {
                        kb = MyKeyBoard.setKeyboard(((JSONObject)
                                object.get("keyboard")).get("user").toString());
                    }
                }
            }
            case communityDialogue -> {
                object = JsonWorker.getAnswersObjJson(dialogueJsonArray, message.getText());
                if (object != null) {
                    userState.get(message.getFromId()).setState(object.get("nextState").toString());
                } else {
                    return;
                }
            }
            case schedule -> {
                Pattern pattern = Pattern.compile("^[в,з]?[0-9]{7}/[0-9]{5}");
                Matcher matcher = pattern.matcher(message.getText());
                scheduleLinkGenerator linkGen = new scheduleLinkGenerator();
                String link = null;
                if (matcher.find()) {
                    link = linkGen.getLinkFromGroupNumber(matcher.group());
                    if (link != null) {
                        JsonWorker.rememberGroup(message.getFromId(), matcher.group());
                    }
                }
                object = JsonWorker.getAnswersObjJson(scheduleJsonArray, message.getText());
                userState.get(message.getFromId()).setState(object.get("nextState").toString());
                if ((object.get("response") instanceof JSONObject) && !object.containsKey("key")) {
                    modifiedMsg = true;
                    if (!JsonWorker.hasGroupNumber(message.getPeerId())) {
                        msg = ((JSONObject) object.get("response")).get("hasNotGroupNumber").toString();
                    } else {
                        msg = linkGen.getLinkFromGroupNumber(JsonWorker.getGroupNumber(message.getPeerId()));
                        msg += ((JSONObject) object.get("response")).get("hasGroupNumber").toString();
                    }
                } else {
                    if (object.get("response") instanceof JSONObject) {
                        modifiedMsg = true;
                        if (link == null) {
                            msg = ((JSONObject) object.get("response")).get("NoSuchGroup").toString();
                        } else {
                            msg = ((JSONObject) object.get("response")).get("HasSuchGroupFirst").toString() + link;
                            msg += ((JSONObject) object.get("response")).get("HasSuchGroupSecond").toString();
                        }
                    }
                }
            }
            case faq -> {
                object = JsonWorker.getAnswersObjJson(FAQJsonArray, message.getText());
                userState.get(message.getFromId()).setState(object.get("nextState").toString());
            }
            case teachSearch -> {
                object = JsonWorker.getAnswersObjJson(teachSearchJsonArray, message.getText());
                userState.get(message.getFromId()).setState(object.get("nextState").toString());
            }
            case teacherCardSearch -> {
                object = JsonWorker.getAnswersObjJson(teacherCardSearcherJsonArray, message.getText());
                userState.get(message.getFromId()).setState(object.get("nextState").toString());
                updateUserState(message.getFromId());
                if ((message.getText().length() > 1) && object.containsKey("key") &&
                        (Arrays.stream(namesSurnamesArray).noneMatch(element ->
                                (element).equalsIgnoreCase(message.getText()))) &&
                        (Arrays.stream(namesSurnamesArray).noneMatch(element ->
                                (element.toLowerCase()).contains(message.getText().toLowerCase())))) {
                    msg = "";
                    modifiedMsg = true;
                    JSONObject finalObject = object;
                    Runnable func = () ->
                            sendMessage(
                                    message.getFromId(),
                                    teacherCardSearcher.find(message.getText()),
                                    MyKeyBoard.setKeyboard(finalObject.get("keyboard").toString()),
                                    null
                            );
                    new Thread(func).start();
                }
            }
            case teacherScheduleSearch -> {
                object = JsonWorker.getAnswersObjJson(teacherScheduleSearchJsonArray, message.getText());
                userState.get(message.getFromId()).setState(object.get("nextState").toString());
                updateUserState(message.getFromId());
                if ((message.getText().length() > 1) && object.containsKey("key") &&
                        (Arrays.stream(namesSurnamesArray).noneMatch(element ->
                                (element).equalsIgnoreCase(message.getText()))) &&
                        (Arrays.stream(namesSurnamesArray).noneMatch(element ->
                                (element.toLowerCase()).contains(message.getText().toLowerCase())))) {
                    msg = "";
                    modifiedMsg = true;
                    JSONObject finalObject = object;
                    Runnable func = () ->
                            sendMessage(
                                    message.getFromId(),
                                    teacherScheduleSearcher.find(message.getText()),
                                    MyKeyBoard.setKeyboard(finalObject.get("keyboard").toString()),
                                    null
                            );
                    new Thread(func).start();
                }
            }
            case settings -> {
                object = JsonWorker.getAnswersObjJson(settingsJsonArray, message.getText());
                if (adminsId.stream().noneMatch(element -> element.equals(message.getFromId()))
                        && object.containsKey("forAdmin")) {
                    object = JsonWorker.getAnswersObjJson(settingsJsonArray, "");
                }
                userState.get(message.getFromId()).setState(object.get("nextState").toString());
                updateUserState(message.getFromId());
                if (object.containsKey("sub")) {
                    JsonWorker.giveSign(message.getFromId(), 1);
                }
                if (object.containsKey("unsub")) {
                    JsonWorker.giveSign(message.getFromId(), 0);
                }
                if (object.get("keyboard") instanceof JSONObject) {
                    modifiedKeyBoard = true;
                    if (adminsId.stream().anyMatch(element -> element.equals(message.getFromId()))) {
                        kb = MyKeyBoard.setKeyboard(((JSONObject)
                                object.get("keyboard")).get("admin").toString());
                    } else {
                        kb = MyKeyBoard.setKeyboard(((JSONObject)
                                object.get("keyboard")).get("user").toString());
                    }
                }
            }
            case newsCreate -> {
                modifiedMsg = true;
                msg = "";
                modifiedKeyBoard = true;
                kb = null;
                news = message.getText();
                Runnable func = () ->
                        JsonWorker.checkingMessage(vkBotsApi, message.getFromId(), message, news, getAccessToken());
                new Thread(func).start();
                userState.get(message.getFromId()).setState(State.newsSend);
                updateUserState(message.getFromId());
            }
            case newsSend -> {
                object = JsonWorker.getAnswersObjJson(newsSendJsonArray, message.getText());
                userState.get(message.getFromId()).setState(object.get("nextState").toString());
                if (object.containsKey("empty")) {
                    makeFolderEmpty("tempFiles/" + message.getFromId());
                }
                if (object.containsKey("publish")) {
                    new Thread(() -> {
                        try {
                            JsonWorker.sendNewsToEveryone(vkBotsApi, message, message.getFromId(), getAccessToken());
                        } catch (IOException | VkApiException e) {
                            LogMaster.error("Ошибка рассылке сообщения");
                        }
                    }).start();
                }
            }
            default -> LogMaster.log("Unknown User state!");
        }
        if (!modifiedMsg) {
            msg = object.get("response").toString();
        }
        if (!modifiedKeyBoard) {
            kb = MyKeyBoard.setKeyboard(object.get("keyboard").toString());
        }
        updateUserState(message.getFromId());
        vkBotsApi.messages().send()
                .setPeerId(message.getFromId())
                .setMessage(msg)
                .setAttachments(att)
                .setKeyboard(kb)
                .setDontParseLinks(true)
                .executeAsync();
    }

    public String getAccessToken() {
        return properties.getProperty("AccessToken");
    }

    @Override
    public int getGroupId() {
        return Integer.parseInt(properties.getProperty("GroupId"));
    }

    public void sendMessage(int userId, String msg, Keyboard kb, AttachableParam attach) {
        try {
            updateUserState(userId);
            vkBotsApi.messages().send()
                    .setPeerId(userId)
                    .setMessage(msg)
                    .setAttachments(attach)
                    .setKeyboard(kb)
                    .setDontParseLinks(true)
                    .execute();
        } catch (VkApiException e) {
            LogMaster.error(e.getMessage());
        }
    }

    public static void main(String[] args) {
        try {
            properties.load(new FileInputStream("workFiles/config.properties"));

            String path = properties.getProperty("LogAddress");
            File tempFilesDir;
            if (path.isEmpty()) {
                tempFilesDir = new File("log");
                if (!tempFilesDir.exists()) {
                    if (tempFilesDir.mkdirs()) {
                        LogMaster.log("Direction \"" + tempFilesDir + "\" created.");
                    } else {
                        LogMaster.log("Direction \"" + tempFilesDir + "\" not created.");
                    }
                }
                path = "log/";
            }

            LogMaster.initLogger(path);
            BotInit init = new BotInit(properties);
            initJsonArrays();
            init.loadArrays();
            adminsId = init.getAdminsId();
            namesSurnamesArray = init.getNamesSurnames();
            new BotsLongPoll(new VK_BOT()).run();
        } catch (VkApiException | IOException | ParseException e) {
            LogMaster.error("Something went wrong..." + e);
        }
    }
}