package api.longpoll.bots;

import api.longpoll.bots.internalParts.LinkGenerators.scheduleLinkGenerator;
import api.longpoll.bots.internalParts.LinkGenerators.teacherCardSearcher;
import api.longpoll.bots.internalParts.LinkGenerators.teacherScheduleSearcher;
import api.longpoll.bots.client.DefaultLongPollClient;
import api.longpoll.bots.client.LongPollClient;
import api.longpoll.bots.exceptions.VkApiException;
import api.longpoll.bots.handlers.update.LongPollBotEventHandler;
import api.longpoll.bots.handlers.update.VkEventHandler;
import api.longpoll.bots.internalParts.LogMaster;
import api.longpoll.bots.model.events.VkEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;


/**
 * Bots Long Poll entry point. Takes {@link LongPollBot} and listens to VK server.
 */
public class BotsLongPoll {
    /**
     * Logger object.
     */
    private static final Logger log = LoggerFactory.getLogger(BotsLongPoll.class);

    /**
     * Infinite loop exit condition.
     */
    private boolean running = true;

    /**
     * Running bot.
     */
    private final LongPollBot bot;

    /**
     * Listens to VK Long Poll events.
     */
    private LongPollClient longPollClient;

    /**
     * Handles VK events.
     */
    private VkEventHandler vkEventHandler;

    public BotsLongPoll(LongPollBot bot) {
        this.bot = bot;
    }

    /**
     * Starts listening to VK server.
     *
     * @throws VkApiException if errors occur.
     */
    public void run() throws VkApiException, IOException {
        run(0);
    }

    /**
     * Starts listening to VK server with delay.
     *
     * @param delay listening delay.
     * @throws VkApiException if errors occur.
     */

    private final Object mutex = new Object();
    private List<VkEvent> events = null;
    public void run(long delay) throws VkApiException, IOException {
        //LogMaster.log("Starting bot [group_id = {" + bot.getGroupId() + "}]...");

        while (running && sleep(delay)) {
            scheduleLinkGenerator.updateTimer();
            teacherCardSearcher.clearTimer();
            teacherScheduleSearcher.clearTimer();
            //getVkEventHandler().handle(getLongPollClient().getEvents());

            Thread eventsGetter = new Thread(() -> {
                try {
                    events = getLongPollClient().getEvents();
                } catch (VkApiException e) {
                    LogMaster.error("VkApiException in BotsLongPoll eventsGetter " + e);
                }
            });

            Thread trackingThread = new Thread(() -> {
                synchronized (mutex) {
                    try {
                        long startTime = System.currentTimeMillis();
                        events = null;
                        eventsGetter.start();

                        while (!eventsGetter.isInterrupted()) {
                            long elapsedTime = System.currentTimeMillis() - startTime;
                            Thread.sleep(200);

                            if (elapsedTime < 60000) {
                                if (events != null)
                                    break;
                            } else {
                                break;
                            }
                        }

                        if (!eventsGetter.isInterrupted()) {
                            eventsGetter.interrupt();
                        }
                    } catch (InterruptedException e) {
                        LogMaster.error("InterruptedException in BotsLongPoll trackingThread" + e);
                    }
                    mutex.notify();
                }
            });
            trackingThread.start();

            synchronized (mutex) {
                try {
                    mutex.wait();
                    if(!trackingThread.isInterrupted()) {
                        trackingThread.interrupt();
                    }
                    if(events != null) {
                        getVkEventHandler().handle(events);
                    }
                } catch (InterruptedException e) {
                    LogMaster.log("InterruptedException in BotsLongPoll MainThread" + e);
                }
            }
        }
    }

    private boolean sleep(long delay) throws VkApiException {
        try {
            Thread.sleep(delay);
            return true;
        } catch (InterruptedException e) {
            throw new VkApiException(e);
        }
    }

    public void stop() {
        running = false;
    }

    public LongPollClient getLongPollClient() {
        if (longPollClient == null) {
            longPollClient = new DefaultLongPollClient(bot);
        }
        return longPollClient;
    }

    public void setLongPollClient(LongPollClient longPollClient) {
        this.longPollClient = longPollClient;
    }

    public VkEventHandler getVkEventHandler() {
        if (vkEventHandler == null) {
            vkEventHandler = new LongPollBotEventHandler(bot);
        }
        return vkEventHandler;
    }

    public void setVkEventHandler(VkEventHandler vkEventHandler) {
        this.vkEventHandler = vkEventHandler;
    }
}
