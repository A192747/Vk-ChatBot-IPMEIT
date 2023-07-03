package api.longpoll.bots.internalParts;

import api.longpoll.bots.model.objects.additional.Keyboard;
import api.longpoll.bots.model.objects.additional.buttons.*;
import com.google.gson.JsonObject;
import java.util.Arrays;
import java.util.List;
import java.util.Collections;
public class MyKeyBoard {

    private static final Keyboard keyboard = new Keyboard(null);

    //start
    private static final Button start = new TextButton(Button.Color.POSITIVE, new TextButton.Action(
            "Начать",
            new JsonObject()));
    //greetings
    private static final Button faq = new TextButton(Button.Color.POSITIVE, new TextButton.Action(
            "Частые вопросы",
            new JsonObject()));
    private static final Button schedule = new TextButton(Button.Color.PRIMARY, new TextButton.Action(
            "Расписание",
            new JsonObject()));
    private static final Button settings = new TextButton(Button.Color.NEGATIVE, new TextButton.Action(
            "Настройки",
            new JsonObject()));
    private static final Button writeToCommunity = new TextButton(Button.Color.SECONDARY, new TextButton.Action(
            "Написать сообществу",
            new JsonObject()));
    //faq
    private static final Button stipend = new TextButton(Button.Color.SECONDARY, new TextButton.Action(
            "Стипендия и льготы",
            new JsonObject()));
    private static final Button studentStatus = new TextButton(Button.Color.SECONDARY, new TextButton.Action(
            "Изменение статуса студента",
            new JsonObject()));
    private static final Button referencesAndBSC = new TextButton(Button.Color.SECONDARY, new TextButton.Action(
            "Справки и БСК",
            new JsonObject()));
    private static final Button directorate = new TextButton(Button.Color.SECONDARY, new TextButton.Action(
            "Дирекция и контакты",
            new JsonObject()));
    private static final Button hostel = new TextButton(Button.Color.SECONDARY, new TextButton.Action(
            "Общежития",
            new JsonObject()));
    private static final Button persDepartment = new TextButton(Button.Color.SECONDARY, new TextButton.Action(
            "Студенческий отдел кадров",
            new JsonObject()));
    private static final Button usefulGroups = new TextButton(Button.Color.SECONDARY, new TextButton.Action(
            "Полезные ссылочки",
            new JsonObject()));
    private static final Button schoolGroups = new TextButton(Button.Color.SECONDARY, new TextButton.Action(
            "Высшие школы",
            new JsonObject()));

    //schedule
    private static final Button findTeacher = new TextButton(Button.Color.SECONDARY, new TextButton.Action(
            "Найти преподавателя",
            new JsonObject()));
    private static final Button lastGroup = new TextButton(Button.Color.POSITIVE, new TextButton.Action(
            "Последняя группа",
            new JsonObject()));

    //teachSearch

    private static final Button findTeacherSchedule = new TextButton(Button.Color.SECONDARY, new TextButton.Action(
            "Расписание преподавателя",
            new JsonObject()));
    private static final Button findTeacherCard = new TextButton(Button.Color.SECONDARY, new TextButton.Action(
            "Контакты преподавателя",
            new JsonObject()));

    //settings
    private static final Button subscribe = new TextButton(Button.Color.POSITIVE, new TextButton.Action(
            "Подписаться на рассылку",
            new JsonObject()));
    private static final Button unSubscribe = new TextButton(Button.Color.POSITIVE, new TextButton.Action(
            "Отписаться от рассылки",
            new JsonObject()));
    private static final Button makeNews = new TextButton(Button.Color.PRIMARY, new TextButton.Action( // for admin
            "Создать новость",
            new JsonObject()));
    private static final Button support = new TextButton(Button.Color.NEGATIVE, new TextButton.Action(
            "Поддержка",
            new JsonObject()));

    //newsSend
    private static final Button publish = new TextButton(Button.Color.PRIMARY, new TextButton.Action(
            "Опубликовать",
            new JsonObject()));
    //work buttons
    private static final Button back = new TextButton(Button.Color.PRIMARY, new TextButton.Action(
            "Назад",
            new JsonObject()));
    private static final Button cancel = new TextButton(Button.Color.PRIMARY, new TextButton.Action(
            "Отмена",
            new JsonObject()));
    private static final Button finishDialogue = new TextButton(Button.Color.PRIMARY, new TextButton.Action(
            "Завершить диалог",
            new JsonObject()));
    //----------------------------------------------------------------------------------------------------
    private static final List<Button> schedulePlusSettings = Arrays.asList(schedule, settings);
    private static final List<Button> stipendPlusReferencesAndBSC = Arrays.asList(stipend, referencesAndBSC);
    private static final List<Button> studentStatusPlusDirectorate = Arrays.asList(studentStatus, directorate);
    private static final List<Button> hostelPlusPersDepartment = Arrays.asList(hostel, persDepartment);
    private static final List<Button> usefulGroupsPlusSchoolGroups = Arrays.asList(usefulGroups, schoolGroups);
    private static final List<Button> subscribeAndUnSubscribe = Arrays.asList(subscribe, unSubscribe);
    private static final List<Button> makeNewsPlusSupport = Arrays.asList(makeNews, support);

    //-----------------------------------------------------------------------------------------------------
    private static final Keyboard keyboardEmpty = new Keyboard(List.of());
    public static final Keyboard keyboardStart = new Keyboard(List.of(Collections.singletonList(start)));
    private static final Keyboard keyboardMain = new Keyboard(Arrays.asList(
            Collections.singletonList(writeToCommunity),
            Collections.singletonList(faq),
            schedulePlusSettings
    ));
    private static final Keyboard keyboardFAQ = new Keyboard(Arrays.asList(
            stipendPlusReferencesAndBSC,
            studentStatusPlusDirectorate,
            hostelPlusPersDepartment,
            usefulGroupsPlusSchoolGroups,
            Collections.singletonList(back)
    ));
    private static final Keyboard keyboardBack = new Keyboard(List.of(Collections.singletonList(back)));
    private static final Keyboard keyboardSettingsForUser = new Keyboard(Arrays.asList(
            subscribeAndUnSubscribe,
            Collections.singletonList(support),
            Collections.singletonList(back)
    ));
    private static final Keyboard keyboardSettingsForAdmin = new Keyboard(Arrays.asList(
            subscribeAndUnSubscribe,
            makeNewsPlusSupport,
            Collections.singletonList(back)
    ));
    private static final Keyboard keyboardSenderForAdmin = new Keyboard(Arrays.asList(
            Collections.singletonList(publish),
            Collections.singletonList(cancel)
    ));
    private static final Keyboard keyboardSchedule = new Keyboard(Arrays.asList(
            Collections.singletonList(lastGroup),
            Collections.singletonList(findTeacher),
            Collections.singletonList(back)
    ));
    private static final Keyboard keyboardDialogueWithCom = new Keyboard(List.of(
            Collections.singletonList(finishDialogue)
    ));
    private static final Keyboard keyboardTeachSearch = new Keyboard(Arrays.asList(
            Collections.singletonList(findTeacherSchedule),
            Collections.singletonList(findTeacherCard),
            Collections.singletonList(back)
    ));
    //-----------------------------------------------------------------------------------------------------
    public static Keyboard setKeyboard(String name){
        return switch (name) {
            case "Empty" -> keyboardEmpty;
            case "Start" -> keyboardStart;
            case "Main" -> keyboardMain;
            case "FAQ" -> keyboardFAQ;
            case "Schedule" -> keyboardSchedule;
            case "SettingsForAdmin" -> keyboardSettingsForAdmin;
            case "SettingsForUser" -> keyboardSettingsForUser;
            case "SenderForAdmin" -> keyboardSenderForAdmin;
            case "Back" -> keyboardBack;
            case "DialogueWithCommunity" -> keyboardDialogueWithCom;
            case "TeacherSearch" -> keyboardTeachSearch;
            default -> keyboard;
        };
    }

}