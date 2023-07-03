package api.longpoll.bots.internalParts;

import java.io.IOException;
import java.util.Calendar;
import java.util.logging.*;

public class LogMaster {
    private static final Logger logger = Logger.getLogger("VK_BOT_LOGGER");

    private static class LogFormatter extends Formatter {
        @Override
        public String format(LogRecord rec) {
            Calendar cal = Calendar.getInstance();
            cal.setTimeInMillis(rec.getMillis());

            return "[" +
                    cal.get(Calendar.DAY_OF_MONTH) + '/' +
                    cal.get(Calendar.MONTH) + '/' +
                    cal.get(Calendar.YEAR) + ' ' +
                    cal.get(Calendar.HOUR_OF_DAY) + ':' +
                    cal.get(Calendar.MINUTE) + ':' +
                    cal.get(Calendar.SECOND) + "]: " +
                    rec.getMessage();
        }
    }

    public static void initLogger(String path) {
        try {
            StringBuilder str = new StringBuilder(path);
            str.append("botLog").append('-');
            Calendar cal = Calendar.getInstance();
            str.append(cal.get(Calendar.DAY_OF_MONTH)).append('-');
            str.append(cal.get(Calendar.MONTH)).append('-');
            str.append(cal.get(Calendar.YEAR)).append('-');
            str.append(cal.get(Calendar.HOUR_OF_DAY)).append('-');
            str.append(cal.get(Calendar.MINUTE)).append('-');
            str.append(cal.get(Calendar.SECOND)).append(".log");
            Handler handler = new FileHandler(str.toString());
            handler.setFormatter(new LogFormatter());
            logger.setUseParentHandlers(false);
            logger.addHandler(handler);
        } catch (IOException e) {
            System.out.println("Failed to start Logger!");
        }
    }

    public static void log(String message) {
        logger.info(message + '\n');
    }

    public static void error(String message) {
        logger.finer(message + '\n');
    }
}
