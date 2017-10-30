package edu.nju.autodroid.utils;

import java.io.*;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by ysht on 2016/3/7 0007.
 */
public class CmdExecutor {
    /**
     * 需求：执行cmd命令，且输出信息到控制台
     * @param cmd
     */
    public static String execCmd(String cmd) {
        return execCmd(cmd, -1);
    }

    /**
    * 执行cmd命令，且输出信息到控制台, 当执行时间到达timeout时，强制关闭该命令进程
     * 执行时间并不会精确，主要是用来保证该命令进程肯定会被结束掉
    * @param cmd
     * @param timeout 单位为秒
    */
    public static String execCmd(String cmd, int timeout){
        Logger.logInfo("ExecCmd:  " + cmd);
        try {
            Process p = Runtime.getRuntime().exec(cmd);
            if(timeout > 0){
                Timer waitTimer = new Timer();
                waitTimer.schedule(new TimerTask() {
                    @Override
                    public void run() {
                        //if(p.isAlive()){
                            Logger.logInfo("ExecCmd: Destory process cmd: " + cmd);
                            //try{
                                p.destroy();
                            //}
                        //}
                    }
                }, timeout*1000);
            }
            //正确输出流
            InputStream input = p.getInputStream();
            BufferedReader reader = new BufferedReader(new InputStreamReader(
                    input));
            String line = "";
            String result = "";
            while ((line = reader.readLine()) != null) {
                result += line + "\n";
                Logger.logInfo(cmd + ": " + line);
            }

            //错误输出流
            InputStream errorInput = p.getErrorStream();
            BufferedReader errorReader = new BufferedReader(new InputStreamReader(errorInput));
            String eline = "";
            while ((eline = errorReader.readLine()) != null) {
                result += eline +"\n";
                Logger.logError(cmd + ": " + eline);
            }
            return result;
        } catch (IOException e) {
            e.printStackTrace();
            return e.getMessage();
        }
    }
}
