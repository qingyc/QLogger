package com.qingyc.qlogger;

import android.text.TextUtils;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * 类说明: QLogger
 *
 * @author qing
 * @time 2018/7/6 11:15
 */
public final class QLogger {

    /**
     * Android's max limit for a log entry is ~4076 bytes,
     * so 4000 bytes is used as chunk size since default charset
     * is UTF-8
     */
    private static final int CHUNK_SIZE = 3600;

    /**
     * It is used for json pretty print
     */
    private static final int JSON_INDENT = 4;

    /**
     * In order to prevent readability, max method count is restricted with 5
     */
    private static final int MAX_METHOD_COUNT = 5;

    /**
     * It is used to determine log settings such as method count, thread info visibility
     */
    private static final Settings settings = new Settings();

    /**
     * Drawing toolbox
     */
    private static final char TOP_LEFT_CORNER = '╔';
    private static final char BOTTOM_LEFT_CORNER = '╚';
    private static final char MIDDLE_CORNER = '╟';
    private static final char HORIZONTAL_DOUBLE_LINE = '║';
    private static final String DOUBLE_DIVIDER = "════════════════════════════════════════════";
    private static final String SINGLE_DIVIDER = "────────────────────────────────────────────";
    private static final String TOP_BORDER = TOP_LEFT_CORNER + DOUBLE_DIVIDER + DOUBLE_DIVIDER;
    private static final String BOTTOM_BORDER = BOTTOM_LEFT_CORNER + DOUBLE_DIVIDER + DOUBLE_DIVIDER;
    private static final String MIDDLE_BORDER = MIDDLE_CORNER + SINGLE_DIVIDER + SINGLE_DIVIDER;

    /**
     * TAG is used for the Log, the name is a little different
     * in order to differentiate the logs easily with the filter
     */
    private static String TAG = "Q_LOGGER_";


    public static synchronized void d(String message) {
        d(TAG, message);
    }

    public static void d(String tag, String message) {
        d(tag, message, settings.methodCount);
    }

    public static void d(String message, int methodCount) {
        d(TAG, message, methodCount);
    }

    public static void d(String tag, String message, int methodCount) {
        validateMethodCount(methodCount);
        log(Log.DEBUG, tag, message, methodCount);
    }

    public static void e(String message) {
        e(TAG, message);
    }

    public static void e(String tag, String message) {
        e(tag, message, null, settings.methodCount);
    }

    public static void e(Throwable e) {
        if (e != null) {
            e(TAG, e.getMessage());
        }
    }

    public static void e(String tag, Exception e) {
        e(tag, null, e, settings.methodCount);
    }

    public static void e(String message, int methodCount) {
        validateMethodCount(methodCount);
        e(message, null, methodCount);
    }

    public static void e(String tag, String message, int methodCount) {
        validateMethodCount(methodCount);
        e(tag, message, null, methodCount);
    }

    public static void e(String tag, String message, Exception e) {
        e(tag, message, e, settings.methodCount);
    }

    public static void e(String tag, String message, Exception e, int methodCount) {
        validateMethodCount(methodCount);
        if (e != null && message != null) {
            message += " : " + e.toString();
        }
        if (e != null && message == null) {
            message = e.toString();
        }
        if (message == null) {
            message = "No message/exception is set";
        }
        log(Log.ERROR, tag, message, methodCount);
    }

    public static void w(String message) {
        w(TAG, message);
    }

    public static void w(String tag, String message) {
        w(tag, message, settings.methodCount);
    }

    public static void w(String message, int methodCount) {
        w(TAG, message, methodCount);
    }

    public static void w(String tag, String message, int methodCount) {
        validateMethodCount(methodCount);
        log(Log.WARN, tag, message, methodCount);
    }

    public static void i(Object... msg) {
        if (msg != null) {
            i(TAG, buildString(msg));
        }

    }

    public static void i(String tag, String message) {
        i(tag, message, settings.methodCount);
    }

    public static void i(String message, int methodCount) {
        i(TAG, message, methodCount);
    }

    public static void i(String tag, String message, int methodCount) {
        validateMethodCount(methodCount);
        log(Log.INFO, tag, message, methodCount);
    }

    public static void v(String message) {
        v(TAG, message);
    }

    public static void v(String tag, String message) {
        v(tag, message, settings.methodCount);
    }

    public static void v(String message, int methodCount) {
        v(TAG, message, methodCount);
    }

    public static void v(String tag, String message, int methodCount) {
        validateMethodCount(methodCount);
        log(Log.VERBOSE, tag, message, methodCount);
    }

    public static void wtf(String message) {
        wtf(TAG, message);
    }

    public static void wtf(String tag, String message) {
        wtf(tag, message, settings.methodCount);
    }

    public static void wtf(String message, int methodCount) {
        wtf(TAG, message, methodCount);
    }

    public static void wtf(String tag, String message, int methodCount) {
        validateMethodCount(methodCount);
        log(Log.ASSERT, tag, message, methodCount);
    }

    /**
     * Formats the json content and print it
     *
     * @param json the json content
     */
    public static void json(String json) {
        json(TAG, json);
    }

    public static void json(String tag, String json) {
        json(tag, json, settings.methodCount);
    }

    public static void json(String json, int methodCount) {
        json(TAG, json, methodCount);
    }

    /**
     * Formats the json content and print it
     *
     * @param json        the json content
     * @param methodCount number of the method that will be printed
     */
    public static void json(String tag, String json, int methodCount) {
        validateMethodCount(methodCount);
        if (TextUtils.isEmpty(json)) {
            d(tag, "Empty/Null json content", methodCount);
            return;
        }
        try {
            if (json.startsWith("{")) {
                JSONObject jsonObject = new JSONObject(json);
                String message = jsonObject.toString(JSON_INDENT);
                d(tag, message, methodCount);
                return;
            }
            if (json.startsWith("[")) {
                JSONArray jsonArray = new JSONArray(json);
                String message = jsonArray.toString(JSON_INDENT);
                d(tag, message, methodCount);
            }
        } catch (JSONException e) {
            d(tag, e.getCause().getMessage() + "\n" + json, methodCount);
        }
    }

    private static void log(int logType, String tag, String message, int methodCount) {
        try {
            if (settings.logLevel == LogLevel.NONE) {
                return;
            }

            StringBuffer logContent = new StringBuffer();
            //添加日志头
            String headerContent = logHeaderContent(methodCount);
            logContent.append(headerContent);

            byte[] bytes = message.getBytes();
            int length = bytes.length;

            if (methodCount > 0) {
                logContent.append(MIDDLE_BORDER).append("\n");
            }
            for (int i = 0; i < length; i += CHUNK_SIZE) {
                int count = Math.min(length - i, CHUNK_SIZE);
                String[] lines = new String(bytes, i, count).split(System.getProperty("line.separator"));
                for (String line : lines) {
                    if (!TextUtils.isEmpty(line.trim())) {
                        logContent.append(HORIZONTAL_DOUBLE_LINE + " " + line).append("\n");
                        if (logContent.length() > CHUNK_SIZE) {
                            logChunk(logType, tag, logContent.toString());
                            logContent = new StringBuffer();
                        }
                    }
                }
            }
            logContent.append(BOTTOM_BORDER).append("\n\n ");
            logChunk(logType, tag, logContent.toString());
        } catch (Exception e) {
            //
        }
    }


    private static String logHeaderContent(int methodCount) {
        StringBuilder headerContent = new StringBuilder();
        StackTraceElement[] trace = Thread.currentThread().getStackTrace();
        if (settings.showThreadInfo) {
            String chunk = HORIZONTAL_DOUBLE_LINE + " Thread: " + Thread.currentThread().getName();
            //logChunk(logType, tag, chunk);
            //logDivider(logType, tag);
            headerContent.append("  \n")
                    .append(TOP_BORDER)
                    .append("\n")
                    .append(chunk)
                    .append("\n")
                    .append(MIDDLE_BORDER)
                    .append("\n");
        }
        String level = "";
        for (int i = methodCount; i > 0; i--) {
            int stackIndex = i + 5;
            StringBuilder builder = new StringBuilder();
            builder.append("║ ")
                    .append(level)
                    .append(getSimpleClassName(trace[stackIndex].getClassName()))
                    .append(".")
                    .append(trace[stackIndex].getMethodName())
                    .append(" ")
                    .append(" (")
                    .append(trace[stackIndex].getFileName())
                    .append(":")
                    .append(trace[stackIndex].getLineNumber())
                    .append(")");
            level += "   ";
            headerContent.append(builder.toString()).append("\n");
        }

        return headerContent.toString();
    }


    private static void logChunk(int logType, String tag, String chunk) {

        if (!BuildConfig.DEBUG) {
            return;
        }
        String finalTag = formatTag(tag) + "";
        if (TextUtils.isEmpty(tag)) {
            finalTag = "";
        }
        if (!chunk.startsWith("\n")) {
            chunk = " \n" + chunk;
        }
        switch (logType) {
            case Log.ERROR:
                Log.e(finalTag, chunk);
                break;
            case Log.INFO:
                Log.i(finalTag, chunk);
                break;
            case Log.VERBOSE:
                Log.v(finalTag, chunk);
                break;
            case Log.WARN:
                Log.w(finalTag, chunk);
                break;
            case Log.ASSERT:
                Log.wtf(finalTag, chunk);
                break;
            case Log.DEBUG:
                // Fall through, log debug by default
            default:
                Log.d(finalTag, chunk);
                break;
        }
    }

    private static String getSimpleClassName(String name) {
        int lastIndex = name.lastIndexOf(".");
        return name.substring(lastIndex + 1);
    }

    private static void validateMethodCount(int methodCount) {
        if (methodCount < 0 || methodCount > MAX_METHOD_COUNT) {
            throw new IllegalStateException("methodCount must be > 0 and < 5");
        }
    }

    private static String formatTag(String tag) {
        if (!TextUtils.isEmpty(tag) && !TextUtils.equals(TAG, tag)) {
            return TAG + "  " + BuildConfig.APPLICATION_ID + " : " + tag;
        }
        return TAG;
    }

    public static class Settings {
        int methodCount = 2;
        boolean showThreadInfo = true;

        /**
         * Determines how logs will printed
         */
        LogLevel logLevel = LogLevel.FULL;

        public Settings hideThreadInfo() {
            showThreadInfo = false;
            return this;
        }

        public Settings setMethodCount(int methodCount) {
            validateMethodCount(methodCount);
            this.methodCount = methodCount;
            return this;
        }

        public Settings setLogLevel(LogLevel logLevel) {
            this.logLevel = logLevel;
            return this;
        }
    }

    public enum LogLevel {

        /**
         * Prints all logs
         */
        FULL,

        /**
         * No log will be printed
         */
        NONE
    }


    public static String buildString(Object... element) {
        StringBuffer sb = new StringBuffer();
        if (element != null) {
            for (Object str : element) {
                sb.append(str);
            }
        }
        return sb.toString();
    }

}
