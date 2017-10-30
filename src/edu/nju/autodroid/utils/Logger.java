package edu.nju.autodroid.utils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintStream;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * 用于系统的日志记录
 * Created by ysht on 2016/3/7 0007.
 */
public class Logger {
    //以下为静态方法，适合全局日志
    private static PrintStream s_logobject = System.out;
    private static boolean s_isInitialized = false;
    private static final String infoPrefix = "INFO:";
    private static final String errorPrefix = "ERROR:";
    private static final String exceptionPrefix = "EXCEPTION:";
    private static final String warningPrefix = "WARNING:";
    private static final SimpleDateFormat DateFORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    /**
     * 初始化logger，将log信息保存在logFileName中，如果logFile为null，则不写入文件
     * @param logFileName 日志文件名
     */
    public static final synchronized void initalize(String logFileName) {
        if (!s_isInitialized) {
            if (logFileName != null) {
                try {
                    s_logobject = new PrintStream(new File(logFileName));
                    s_isInitialized = true;
                } catch (Exception e) {
                    s_logobject = System.out;
                }
            } else{
                s_logobject = System.out;
            }
        }
    }

    /**
     * 结束日志，保证日志文件可以被正确的关闭和保存
     */
    public static final synchronized void endLogging() {
        if (s_logobject != System.out && s_logobject != null) {
            try {
                s_logobject.close();
            } catch (Exception e) {

            }
        }
        s_isInitialized = false;
    }

    /***
     * This is the method that is used to log the provided msg as info
     * @param msg target message that needs to be logged
     */
    public static final void logInfo(String msg) {
        writeMsg(infoPrefix + msg, s_logobject);
    }

    /***
     * Use this to log error message
     * @param msg target message that needs to be logged
     */
    public static final void logError(String msg) {
        writeMsg(errorPrefix + msg, s_logobject);
    }

    /***
     * Use this to log exception
     * @param msg target Message
     */
    public static final void logException(String msg) {
        writeMsg(exceptionPrefix + msg, s_logobject);
    }

    /**
     * This method is to log the provided exception
     * @param e the target exception that needs to be logged
     */
    public static final synchronized void logException(Exception e) {
        if (!s_isInitialized ) {
            System.out.println(exceptionPrefix + " Stack Trace:");
            e.printStackTrace(System.out);
            System.out.println(e.getMessage());
            System.out.flush();
        } else {
            e.printStackTrace(System.out);
            e.printStackTrace(s_logobject);
            s_logobject.println(DateFORMAT.format(new Date()) + "\t" + e.getMessage());
            s_logobject.flush();
        }
    }

    private static final synchronized void writeMsg(String msg, PrintStream logObject) {
        msg = DateFORMAT.format(new Date()) + "\t" + msg;
        if (logObject == null) {
            System.out.printf("%s\n", msg);
            System.out.flush();
        } else {
            System.out.printf("%s\n", msg);
            System.out.flush();
            logObject.printf("%s\n", msg);
            logObject.flush();
        }
    }

    //以下为可实例化日志部分
    private PrintStream logObject = null;
    private String loggerName = "";
    private boolean isInitialized = false;
    private String loggerFilePath = null;

    public Logger(String loggerName){
        this.loggerName = loggerName;
    }

    public Logger(String loggerName, String loggerFilePath) {
        this.loggerName = loggerName;
        this.loggerFilePath = loggerFilePath;
        try {
            File loggerFile = new File(loggerFilePath);
            if(!loggerFile.exists()){
                loggerFile.getParentFile().mkdirs();
                loggerFile.createNewFile();
            }
            this.logObject = new PrintStream(new File(loggerFilePath));
            isInitialized = true;
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String getLoggerFilePath(){
        return loggerFilePath;
    }
    /***
     * This is the method that is used to log the provided msg as info
     * @param msg target message that needs to be logged
     */
    public void info(String msg) {
        writeMsg(loggerName + " " + infoPrefix + msg, logObject);
    }

    /***
     * Use this to log error message
     * @param msg target message that needs to be logged
     */
    public void error(String msg) {
        writeMsg(loggerName + " " + errorPrefix + msg, logObject);
    }

    /***
     * Use this to log exception
     * @param msg target Message
     */
    public void exception(String msg) {
        writeMsg(loggerName + " " + exceptionPrefix + msg, logObject);
    }

    @Override
    protected void finalize() throws Throwable {
        logObject.close();
        super.finalize();
    }
}
