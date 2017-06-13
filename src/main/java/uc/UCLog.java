package uc;

import org.apache.commons.lang3.exception.ExceptionUtils;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.logging.*;

/**
 * Created by keltp on 2017-06-10.
 */
public class UCLog {
    private static final SimpleDateFormat fFormat = new SimpleDateFormat("yyyy-MM-dd hh.mm.ss.SSS");
    private static final SimpleDateFormat lFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss.SSS");
    private static Logger logger;

    static {
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(System.currentTimeMillis());
        String fn = fFormat.format(cal.getTime()) + ".log";
        File logDir = new File("./log");

        if(!logDir.exists()) {
            logDir.mkdirs();
        }

        try {
            Formatter formatter = new Formatter() {
                @Override
                public String format(LogRecord record) {
                    Calendar cal = Calendar.getInstance();
                    cal.setTimeInMillis(record.getMillis());
                    Throwable t = record.getThrown();
                    if(t == null) {
                        return String.format("[%s/%s] %s\r\n", lFormat.format(cal.getTime()), record.getLevel().getName(), record.getMessage());
                    } else {
                        String trace = ExceptionUtils.getStackTrace(t);
                        return String.format("[%s/%s] %s\r\n%s", lFormat.format(cal.getTime()), record.getLevel().getName(), record.getMessage(), trace);
                    }
                }
            };

            ConsoleHandler cHandler = new ConsoleHandler();
            cHandler.setFormatter(formatter);

            FileHandler fHandler = new FileHandler(logDir.getCanonicalPath() + File.separator + fn, true);
            fHandler.setFormatter(formatter);

            logger = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);
            logger.setLevel(Level.ALL);
            logger.setUseParentHandlers(false);
            logger.addHandler(fHandler);
            logger.addHandler(cHandler);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void i(String msg) {
        logger.log(Level.INFO, msg);
    }

    public static void w(String msg) {
        logger.log(Level.WARNING, msg);
    }

    public static void e(String msg) {
        logger.log(Level.SEVERE, msg);
    }

    public static void e(String msg, Throwable t) {
        logger.log(Level.SEVERE, msg, t);
    }
}
