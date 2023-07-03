## Примеры кода, на основе которых делался этот бот:
[Отсюда взята структура кода (есть пояснения)](https://github.com/yvasyliev/java-vk-bots-long-poll-api)  
[Примеры использования функций (на основе структуры нашего кода)](https://github.com/yvasyliev/java-vk-bots-long-poll-api-examples)

## Все самостоятельно реализованные функции лежат в папке internalParts

## Инструкции
[Создание карусели](#Создание_карусели)


## Создание карусели
<a name="Создание_карусели"></a>
<details>
<summary>Подробнее ...</summary>

[Пример (с недочетами)](https://github.com/yvasyliev/java-vk-bots-long-poll-api-examples/blob/master/src/main/java/bot/longpoll/examples/messages/CarouselExample.java)
<br/>Изменить файл ...\model\objects\additional\carousel\Elements.java
``` java
package api.longpoll.bots.model.objects.additional.carousel;

import api.longpoll.bots.model.objects.additional.buttons.Button;
import com.google.gson.annotations.SerializedName;

import java.util.Arrays;
import java.util.List;

/**
 * Describes carousel element.
 */
public class Element {
    /**
     * Title, maximum 80 characters.
     */
    @SerializedName("title")
    private String title;

    /**
     * Subtitle, maximum 80 characters.
     */
    @SerializedName("description")
    private String description;

    /**
     * ID of an image that needs to be attached.
     */
    @SerializedName("photo_id")
    private String photoId;

    /**
     * List of buttons. One carousel element can contain up to 3 buttons.
     */
    @SerializedName("buttons")
    private List<Button> buttons;

    /**
     * An object describing the action that needs to happen after a carousel element is clicked.
     */
    @SerializedName("action")
    private Action action;

    /**
     * Describes carousel action.
     */
    public static abstract class Action {
        /**
         * Carousel action type.
         */
        @SerializedName("type")
        private final String type;

        public Action(String type) {
            this.type = type;
        }

        @Override
        public String toString() {
            return "Action{" +
                    "type='" + type + '\'' +
                    '}';
        }
    }

    /**
     * Opens a link from the "link" field.
     */
    public static class OpenLink extends Action {
        /**
         * Link to be opened.
         */
        @SerializedName("link")
        private String link;

        public OpenLink(String link) {
            super("open_link");
            this.link = link;
        }

        public String getLink() {
            return link;
        }

        public void setLink(String link) {
            this.link = link;
        }


        @Override
        public String toString() {
            return "OpenLink{" +
                    "link='" + link + '\'' +
                    "} " + super.toString();
        }
    }

    /**
     * Opens an image from the current carousel element.
     */
    public static class OpenPhoto extends Action {
        public OpenPhoto() {
            super("open_photo");
        }
    }

    public String getTitle() {
        return title;
    }

    public Element setTitle(String title) {
        this.title = title;
        return this;
    }

    public String getDescription() {
        return description;
    }

    public Element setDescription(String description) {
        this.description = description;
        return this;
    }

    public String getPhotoId() {
        return photoId;
    }

    public Element setPhotoId(int ownerId, int mediaId) {
        this.photoId = ownerId + "_" + mediaId;
        return this;
    }

    public List<Button> getButtons() {
        return buttons;
    }

    public Element setButtons(Button... buttons) {
        return setButtons(Arrays.asList(buttons));
    }

    public Element setButtons(List<Button> buttons) {
        this.buttons = buttons;
        return this;
    }

    public Action getAction() {
        return action;
    }

    public Element setAction(Action action) {
        this.action = action;
        return this;
    }

    @Override
    public String toString() {
        return "Element{" +
                "title='" + title + '\'' +
                ", description='" + description + '\'' +
                ", photoId='" + photoId + '\'' +
                ", buttons=" + buttons +
                ", action=" + action +
                '}';
    }
}

```
Изменить файл ...\model\objects\additional\carousel\Carousel.java
``` java
package api.longpoll.bots.model.objects.additional.carousel;

import api.longpoll.bots.model.objects.additional.Template;
import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;

import java.util.Arrays;
import java.util.List;

/**
 * Describes carousel template.
 */
public class Carousel extends Template {
    /**
     * A list of carousel elements.
     */
    @SerializedName("elements")
    private List<Element> elements;

    public Carousel(Element... elements) {
        this(Arrays.asList(elements));
    }

    public Carousel(List<Element> elements) {
        super("carousel");
        this.elements = elements;
    }

    public List<Element> getElements() {
        return elements;
    }

    public void setElements(List<Element> elements) {
        this.elements = elements;
    }

    public String toJson() {
        return new Gson().toJson(this);
    }

    @Override
    public String toString() {
        return toJson();
    }
}

```
Реализация карусели с подгрузкой фото:
``` java
private SaveMessagesPhoto.Response uploadPhoto(File photo, int id) throws VkApiException {
        try {
            GetMessagesUploadServer.Response.ResponseObject uploadServer = vkBotsApi.photos().getMessagesUploadServer()
                    .setPeerId(id)
                    .executeAsync()
                    .get()
                    .getResponseObject();
            String str = uploadServer.getUploadUrl();
            UploadPhoto.Response uploadedPhoto = new UploadPhoto()
                    .setUploadUrl(uploadServer.getUploadUrl())
                    .setFile(photo)
                    .executeAsync()
                    .get();

            return vkBotsApi.photos().saveMessagesPhoto()
                    .setServer(uploadedPhoto.getServer())
                    .setPhoto(uploadedPhoto.getPhoto())
                    .setHash(uploadedPhoto.getHash())
                    .executeAsync().get();
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
        }
    }
    
     public void onMessageNew(MessageNewEvent messageNew) throws IOException {
            if (message.getText().contains("Начать")) {
                SaveMessagesPhoto.Response savedPhoto = null;
                try {
                    savedPhoto = uploadPhoto(new File("workFiles/1.jpg"), message.getFromId());

                } catch (VkApiException e) {
                    //обработка ошибки
                }
                Button hi = new TextButton(Button.Color.PRIMARY, new TextButton.Action(
                        "Дороу",
                        new JsonObject()));
                Button no = new TextButton(Button.Color.NEGATIVE, new TextButton.Action(
                        "кака",
                        new JsonObject()));


                Element element1 = new Element()
                        .setPhotoId(savedPhoto.getResponseObject().get(0).getOwnerId(), savedPhoto.getResponseObject().get(0).getId())
                        .setTitle("Строка с Заголовком")
                        .setButtons(hi)
                        .setDescription("Иностранный язык. Профессионально-ориентированный курс\n" +
                                "++++++++++++");

                Element element2 = new Element()
                        .setTitle("Строка с заголовком2")
                        .setPhotoId(savedPhoto.getResponseObject().get(0).getOwnerId(), savedPhoto.getResponseObject().get(0).getId())
                        .setButtons(no)
                        .setDescription("Иностранный язык. Профессионально-ориентированный курс" +
                                        "++++++++++++++++++++++\n"
                                );

                Template carousel = new Carousel(Arrays.asList(element1, element2));
                vkBotsApi.messages().send()
                        .setPeerId(message.getPeerId())
                        .setMessage("Карусель")
                        .setTemplate(carousel)
                        .setAttachments(photo)
                        .executeAsync();
     }
```
</details>

