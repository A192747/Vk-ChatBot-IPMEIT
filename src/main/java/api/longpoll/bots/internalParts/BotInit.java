package api.longpoll.bots.internalParts;

import api.longpoll.bots.internalParts.LinkGenerators.scheduleLinkGenerator;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.json.simple.JSONArray;

import java.io.*;
import java.util.ArrayList;
import java.util.Properties;
import java.util.Stack;

public class BotInit {
    final private Properties properties;
    private String[] namesSurnamesArray;
    final private ArrayList<Integer> adminsId = new ArrayList<>();

    public BotInit(Properties prop) throws IOException {
        properties = prop;
        LogMaster.log("[INIT START]");
        createDir("tempFiles");
        checkMainFiles();
        JsonWorker.initPathAnswersJson("workFiles/answers.json");
    }

    private void loadSimpleNamesSurnames() throws IOException {
        try (BufferedReader br = new BufferedReader(new FileReader("workFiles/simpleNamesSurnames.txt"))) {
            Stack<String> arr = new Stack<>();
            String line = br.readLine();
            while (line != null) {
                arr.add(line);
                line = br.readLine();
            }
            int size = arr.size();
            namesSurnamesArray = new String[size];
            for (int i = 0; i < size; ++i) {
                namesSurnamesArray[i] = arr.pop();
            }
        }
    }

    public void loadArrays() throws IOException{
        JsonWorker.initPathUsersJson("workFiles/users.json");
        JsonWorker.initUsersJson();
        loadAdminIds();
        loadSimpleNamesSurnames();
        createAdminsDirs();
        scheduleLinkGenerator.renewDataBase();
        LogMaster.log("[INIT COMPLETE]");
        LogMaster.log("[START]: Bot is working...");
    }

    public static void createDir(String str) {
        File tempFilesDir = new File(str);
        if (!tempFilesDir.exists()) {
            if (tempFilesDir.mkdirs()) {
                LogMaster.log("Directory " + str + "/ created successfully");
            }
        }
    }

    private void checkMainFiles() throws IOException {
        String pathToFile = "workFiles/config.properties";
        File file = new File(pathToFile);
        if (file.exists()) {
            if (file.length() == 0) {
                LogMaster.error("[ERROR]: File config.properties is empty. Please fill it in with the text!\n" +
                        "[Example]:\nAccessToken=vk1.a.5Lhasdbtrs138...asrb6\nAdminsIds=123, 456, 789\n" +
                        "GroupId=123456\nLinkToSchedule=https://ruz.spbstu.ru/faculty/100/groups/\n" +
                        "[STOP]: The Bot is stopped!");
                System.exit(1);
            }
            if (!scheduleLinkGenerator.initScheduleLink()) {
                LogMaster.error("[ERROR]: File config.properties does not have a filled field " +
                        "for the group schedule. Fill in the LinkToSchedule field and restart the bot\n" +
                        "[Example]: LinkToSchedule=https://ruz.spbstu.ru/faculty/100/groups/\n" +
                        "[STOP]: The Bot is stopped!");
                System.exit(1);
            }
            if (!JsonWorker.initImageQuality()) {
                LogMaster.error("[ERROR]: File config.properties does not have a correctly filled field " +
                        "for the image quality. Fill in the UploadPhotoQuality field and restart the bot\n" +
                        "[Example]: UploadPhotoQuality=medium\n" +
                        "[STOP]: The Bot is stopped!");
                System.exit(1);
            }
        } else {
            LogMaster.error("[ERROR]: File config.properties does not exist. " +
                    "Please create it and fill it with text!\n" +
                    "[STOP]: The Bot is stopped!");
            System.exit(1);
        }
        if (new File("workFiles/answers.json").exists()) {
            if (new File("workFiles/answers.json").length() == 0) {
                LogMaster.error("[ERROR]: File answers.json is empty. Please fill it in with the text!\n");
                System.exit(1);
            }
        } else {
            LogMaster.error("[ERROR]: File answers.json does not exist. " +
                    "Please create it and fill it with text!\n" +
                    "[STOP]: The Bot is stopped!");
            System.exit(1);
        }
        //need
        if (new File("workFiles/users.json").createNewFile()) {
            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            String jsonString = gson.toJson(new JSONArray());
            try (FileWriter fileSaver = new FileWriter("workFiles/users.json")) {
                fileSaver.write(jsonString);
                fileSaver.flush();
            }
            LogMaster.log("[INFO]: File users.json was created");
        }
        if (new File("workFiles/scheduleMatcher.json").createNewFile()) {
            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            String jsonString = gson.toJson(new JSONArray());
            try (FileWriter fileSaver = new FileWriter("workFiles/scheduleMatcher.json")) {
                fileSaver.write(jsonString);
                fileSaver.flush();
            }
            LogMaster.log("[INFO]: File scheduleMatcher.json was created");
        }
        pathToFile = "workFiles/mainPict.jpg";
        file = new File(pathToFile);
        if (!file.exists()) {
            LogMaster.error("[ERROR]: File mainPict.jpg does not exist. " +
                    "Please put it in to folder workFiles/\n" +
                    "[STOP]: The Bot is stopped!");
            System.exit(1);
        }
        file = new File("workFiles/simpleNamesSurnames.txt");
        if (file.exists()) {
            if (file.length() == 0) {
                LogMaster.error("[ERROR]: File simpleNamesSurnames.txt is empty. " +
                        "Please fill it in with the text!\n" +
                        "[STOP]: The Bot is stopped!");
                System.exit(1);
            }
        } else {
            LogMaster.error("[ERROR]: File simpleNamesSurnames.txt does not exist. " +
                    "Please create it and fill it with text!\n" +
                    "[STOP]: The Bot is stopped!");
            System.exit(1);
        }
    }

    private void loadAdminIds() throws IOException {
        properties.load(new FileInputStream("workFiles/config.properties"));
        properties.getProperty("AdminsIds");
        String[] stringAdminsId = properties.getProperty("AdminsIds").split(", ");
        for (String id: stringAdminsId) {
            adminsId.add(Integer.parseInt(id));
        }
    }

    private void createAdminsDirs() {
        for (int adminID: adminsId) {
            createDir("tempFiles/" + adminID);
        }
    }

    public ArrayList<Integer> getAdminsId(){
        return adminsId;
    }

    public String[] getNamesSurnames() {
        return namesSurnamesArray;
    }
}
