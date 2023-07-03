package api.longpoll.bots.internalParts;

import api.longpoll.bots.exceptions.VkApiException;
import api.longpoll.bots.http.params.MessageDoc;
import api.longpoll.bots.http.params.MessagePhoto;
import api.longpoll.bots.methods.impl.VkBotsApi;
import api.longpoll.bots.methods.impl.messages.GetById;
import api.longpoll.bots.model.objects.additional.PhotoSize;
import api.longpoll.bots.model.objects.basic.Message;
import api.longpoll.bots.model.objects.media.Attachment;
import api.longpoll.bots.model.objects.media.Doc;
import api.longpoll.bots.model.objects.media.Photo;
import com.google.gson.GsonBuilder;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.json.simple.JSONArray;
import com.google.gson.Gson;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Properties;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static api.longpoll.bots.VK_BOT.makeFolderEmpty;

@SuppressWarnings("unchecked")
public class JsonWorker {
    private static String pathToAnswersFile = null;
    private static String pathToUsersFile = null;
    private static JSONArray usersArrayJson;
    private static JSONObject jsonAnswersArray;
    private static String imageQuality;

    public static boolean initImageQuality() {
        Properties properties = new Properties();
        try {
            properties.load(new FileInputStream("workFiles/config.properties"));
            String quality = properties.getProperty("UploadPhotoQuality");
            if ("".equals(quality)) {
                quality = "medium";
            }
            return setImageQuality(quality);
        } catch (IOException e) {
            return false;
        }
    }

    private static boolean setImageQuality (String quality) {
        switch (quality) {
            case "low" -> {
                imageQuality = "x";
                return true;
            }
            case "medium" -> {
                imageQuality = "z";
                return true;
            }
            case "high" -> {
                imageQuality = "w";
                return true;
            }
        }
        return false;
    }

    private static String getJsonFile(String FilePath) throws IOException {
        StringBuilder builder = new StringBuilder();
        List<String> lines = Files.readAllLines(Paths.get(FilePath));
        lines.forEach(builder::append);

        return builder.toString();
    }

    public static JSONObject renewAnswersJson() throws ParseException, IOException {
        JSONParser parser = new JSONParser();
        return (JSONObject) parser.parse(getJsonFile(pathToAnswersFile));
    }

    public static void initPathAnswersJson(String path) {
        pathToAnswersFile = path;
    }

    public static JSONObject getAnswersObjJson(JSONArray JsonArray, String message) {
        return (JSONObject) JsonArray.stream()
                .filter(obj -> ((JSONObject) obj).containsKey("keyword"))
                .filter(obj -> ((JSONObject) obj).get("keyword").equals(message))
                .findFirst()
                .orElseGet(() -> JsonArray.stream()
                        .filter(obj -> ((JSONObject) obj).containsKey("key"))
                        .findFirst()
                        .orElse(null));
    }

    public static void initPathUsersJson(String path) {
        pathToUsersFile = path;
    }

    public static void initUsersJson() {
        try {
            if (!Files.exists(Paths.get(pathToUsersFile))) {
                return;
            }
            usersArrayJson = (JSONArray) new JSONParser().parse(getJsonFile(pathToUsersFile));
        } catch (IOException | ParseException ignored) {}
    }

    private static void writeToUsersFile() throws IOException {
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        String jsonString = gson.toJson(usersArrayJson);
        try (FileWriter file = new FileWriter(pathToUsersFile)) {
            file.write(jsonString);
            file.flush();
        }
    }

    public static void addUserId(int userId) throws IOException { //done
        if (usersArrayJson.stream().noneMatch((obj ->
                (((JSONObject) obj).get("id")).toString().equals(String.valueOf(userId))))) {
            JSONObject newElement = new JSONObject();
            newElement.put("signed", 1);
            newElement.put("id", userId);
            newElement.put("group", "null");
            usersArrayJson.add(newElement);

            writeToUsersFile();
            LogMaster.log("[INFO]: User:" + userId + " added");
        }
    }

    public static void giveSign(int userId, int value) throws IOException {
        if (usersArrayJson.stream().anyMatch(obj ->
                (((JSONObject) obj).get("id")).toString().equals(String.valueOf(userId)) &&
                        (((JSONObject) obj).get("signed")).toString().equals(String.valueOf(value)))) {
            return;
        }
        usersArrayJson.stream()
                .filter(obj -> (((JSONObject) obj).get("id")).toString().equals(String.valueOf(userId)))
                .findFirst()
                .map(obj -> ((JSONObject) obj).put("signed", value));
        writeToUsersFile();
    }

    public static void sendNewsToEveryone(VkBotsApi vk, Message message, Integer userId, String accessToken)
            throws IOException, VkApiException {
        List<MessagePhoto> photos = new ArrayList<>();
        List<MessageDoc> docs = new ArrayList<>();
        int indexPhotos = 0;
        int indexDocs = 0;
        String folderPath = "tempFiles/" + message.getFromId();
        File folder = new File(folderPath);
        File[] files = folder.listFiles();
        if (files != null) {
            for (File file: files) {
                if (file.isFile()) {
                    if (file.getName().equals(indexPhotos + "image" + message.getFromId() + ".jpg")) {
                        photos.add(new MessagePhoto(accessToken, message.getPeerId(),
                                new File("tempFiles/" + message.getFromId() + "/" +
                                        indexPhotos + "image" + message.getFromId() + ".jpg")));
                        indexPhotos++;
                    }
                    if (!(file.getName().matches("[0-9]image[0-9]+\\.jpg")
                            || (file.getName().equals("news" +  message.getFromId() + ".txt")))) {
                        docs.add(new MessageDoc(accessToken, message.getPeerId(),
                                new File("tempFiles/" + message.getFromId() + "/" + file.getName())));
                        indexDocs++;
                    }
                }
            }
        }
        for(int in = indexPhotos; in < 10; in++) {
            photos.add(null);
        }
        for(int in = indexDocs; in < 10; in++) {
            docs.add(null);
        }
        boolean noErrors = true;
        try (Stream<String> stringStream = Files.lines(Paths.get("tempFiles/"
                + message.getFromId() +"/news" +  message.getFromId() +".txt"))) {
            String news = stringStream.collect(Collectors.joining());
            try {
                jsonAnswersArray = JsonWorker.renewAnswersJson();
            } catch (ParseException e) {
                vk.messages().send()
                        .setPeerId(userId)
                        .setMessage("Произошла ошибка при получении файла с ответами для пользователей\uD83D\uDE29")
                        .executeAsync();
                noErrors = false;
            }
            if (noErrors) {
                String newsTitle = ((JSONObject) ((JSONObject) ((JSONArray)
                        (jsonAnswersArray.get("newsCreate"))).get(0)).get("response")).get("Example").toString();
                for (Object user : usersArrayJson) {
                    if (((JSONObject) user).get("signed").toString().equals("1")) {
                        int userID = Integer.parseInt(((JSONObject) user).get("id").toString());
                        vk.messages().send()
                                .setPeerId(userID)
                                .setMessage(newsTitle + news)
                                .setAttachments(photos.get(0), photos.get(1), photos.get(2), photos.get(3),
                                        photos.get(4), photos.get(5), photos.get(6), photos.get(7),
                                        photos.get(8), photos.get(9), docs.get(0), docs.get(1),
                                        docs.get(2), docs.get(3), docs.get(4), docs.get(5),
                                        docs.get(6), docs.get(7), docs.get(8), docs.get(9))
                                .execute();
                    }
                }
            }
        }

        //Freeing up memory
        jsonAnswersArray = null;
        makeFolderEmpty("tempFiles/" + userId);
    }

    public static void rememberGroup(int userId, String groupNumber) throws IOException {
        if (usersArrayJson.stream().anyMatch(obj ->
                (((JSONObject) obj).get("id")).toString().equals(String.valueOf(userId)) &&
                        (((JSONObject) obj).get("group")).toString().equals(groupNumber))) {
            return;
        }
        usersArrayJson.stream()
                .filter(obj -> (((JSONObject) obj).get("id")).toString().equals(String.valueOf(userId)))
                .findFirst()
                .map(obj -> ((JSONObject) obj).put("group", groupNumber));
        writeToUsersFile();
    }

    public static void checkingMessage(VkBotsApi vk, int userId, Message mess, String news, String accessToken) {
        FileWriter writer;
        try {
            jsonAnswersArray = JsonWorker.renewAnswersJson();
            writer = new FileWriter("tempFiles/" + userId + "/news" + userId + ".txt");
            writer.write(news);
            writer.close();

            int indexPhotos = 0;
            int indexDocs = 0;
            List<MessagePhoto> photos = new ArrayList<>();
            List<MessageDoc> docs = new ArrayList<>();
            List<Attachment> attachment;
            String photoUrl;
            if (mess.hasAttachments()) {
                if (mess.isCropped()) {
                    //if message is cropped by vk, we get message without cropping by using message id
                    GetById.Response responseBody = vk.messages()
                            .getById()
                            .setMessageIds(mess.getId())
                            .execute();
                    mess = responseBody.getResponseObject().getItems().get(0);
                }

                attachment = mess.getAttachments();
                LogMaster.error(mess.getAttachments().toString());

                for (Attachment attacedElement : attachment) {
                    LogMaster.log(attacedElement.getType().toString());
                    if (attacedElement.getType().toString().equals("PHOTO")) {
                        Optional<PhotoSize> photoSize = ((Photo) attacedElement.getAttachable())
                                .getPhotoSizes()
                                .stream()
                                .filter(s -> s.getType().equals(imageQuality))
                                .findFirst();

                        if (photoSize.isPresent()) {
                            photoUrl = photoSize.get().getSrc();
                        } else { //photo is too small
                            Optional<PhotoSize> opt = (((Photo) attacedElement.getAttachable())
                                    .getPhotoSizes()
                                    .stream()
                                    .filter(s -> s.getType().equals("x"))
                                    .findFirst());
                            if (opt.isPresent()) {
                                photoUrl = opt.get().getSrc();
                            } else {
                                photoUrl = "";
                            }
                        }

                        LogMaster.log(photoUrl);
                        String destinationFile = "tempFiles/" + userId + "/"
                                + indexPhotos + "image" + userId + ".jpg";
                        File file;
                        try {
                            URL url = new URI(photoUrl).toURL();
                            BufferedImage image = ImageIO.read(url);
                            file = new File(destinationFile);
                            ImageIO.write(image, "jpg", file);
                        } catch (IOException | URISyntaxException e) {
                            LogMaster.error("[ERROR]: Error while saving an image"
                                    + indexPhotos + ": " + e.getMessage());
                        }
                        photos.add(new MessagePhoto(accessToken, userId,
                                new File("tempFiles/" + userId + "/"
                                        + indexPhotos + "image" + userId + ".jpg")));
                        indexPhotos++;
                    }
                    if (attacedElement.getType().toString().contains("DOCUMENT")) {
                        String documentUrl = ((Doc) attacedElement.getAttachable()).getUrl();
                        String documentType = String.valueOf(((Doc) attacedElement.getAttachable()).getTitle());
                        LogMaster.error(documentType);
                        try {
                            URL url = new URI(documentUrl).toURL();
                            URLConnection conn = url.openConnection();
                            InputStream inputStream = conn.getInputStream();
                            FileOutputStream outputStream = new FileOutputStream("tempFiles/"
                                    + userId + "/" + documentType);
                            int bytesRead;
                            byte[] buffer = new byte[1024];
                            while ((bytesRead = inputStream.read(buffer)) != -1) {
                                outputStream.write(buffer, 0, bytesRead);
                            }
                            outputStream.close();
                            inputStream.close();
                        } catch (IOException | URISyntaxException e) {
                            LogMaster.error("[ERROR]: Error while saving a document "
                                    + indexDocs + ": " + e.getMessage());
                        }
                        docs.add(new MessageDoc(accessToken, mess.getPeerId(),
                                new File("tempFiles/" + userId + "/" + documentType)));
                        indexDocs++;
                    }

                }
            }
            for (int i = indexPhotos; i < 10; i++) {
                photos.add(null);
            }
            for (int i = indexDocs; i < 10; i++) {
                docs.add(null);
            }
            //LogMaster.log("[INFO]: User " + mess.getFromId() + " checking message");
            String warningMessage = ((JSONObject) ((JSONObject) ((JSONArray)
                    (jsonAnswersArray.get("newsCreate"))).get(0)).get("response")).get("CheckWarning").toString();
            String example = ((JSONObject) ((JSONObject) ((JSONArray)
                    (jsonAnswersArray.get("newsCreate"))).get(0)).get("response")).get("Example").toString();
            vk.messages().send()
                    .setMessage(warningMessage)
                    .setPeerId(mess.getFromId())
                    .execute();

            Thread.sleep(500);
            /*
            wait 0.5 seconds.
            VK sometimes does not send messages with document,
            that little time has passed
            (or because if other reasons... I did not find out until the end)
            */

            vk.messages().send()
                    .setMessage(example + news)
                    .setPeerId(mess.getFromId())
                    .setAttachments(photos.get(0), photos.get(1), photos.get(2), photos.get(3),
                            photos.get(4), photos.get(5), photos.get(6), photos.get(7),
                            photos.get(8), photos.get(9), docs.get(0), docs.get(1),
                            docs.get(2), docs.get(3), docs.get(4), docs.get(5),
                            docs.get(6), docs.get(7), docs.get(8), docs.get(9))
                    .setKeyboard(MyKeyBoard.setKeyboard(((JSONObject) ((JSONArray)
                            (jsonAnswersArray.get("newsCreate"))).get(0)).get("keyboard").toString()))
                    .execute();
        } catch (IOException | ParseException | VkApiException | InterruptedException e) {
            LogMaster.error("[ERROR]: Error occurred while writing to the file." + e);
        }
    }

    public static boolean hasGroupNumber(int userId){
        return usersArrayJson.stream()
                .filter(obj -> (((JSONObject) obj).get("id")).toString().equals(String.valueOf(userId)))
                .noneMatch(obj -> ((JSONObject) obj).get("group").toString().equals("null"));
    }
    public static String getGroupNumber(int userId){
        Optional<?> opt = usersArrayJson.stream()
                .filter(obj -> (((JSONObject) obj).get("id")).toString().equals(String.valueOf(userId)))
                .findFirst();
        if (opt.isPresent()) {
            return ((JSONObject) opt.get()).get("group").toString();
        }
        return "";
    }
}
