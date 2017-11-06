package edu.nju.autodroid.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Properties;

/**
 * Created by ysht on 2016/3/7 0007.
 */
public class Configuration {
    private static String propPath =  "autodroid.properties";
    private static Properties prop;

    static {
        prop = new Properties();
        try{
            InputStream in = new FileInputStream(propPath);
            prop.load(in);
            in.close();
            Logger.logInfo("Read configuraion successfully！");
        }
        catch (Exception e){
            Logger.logException("Fail to read configuraion！\n"+ new File(propPath).getAbsolutePath());
            Logger.logException(e);
        }
    }

    protected static String getProperty(String key)
    {
        return prop.getProperty(key);
    }

    protected static String getAndroidSDKPath()
    {
        return prop.getProperty("android_sdk_path");
    }

    protected static String getAntRootPath(){
        return prop.getProperty("ant_root_path");
    }

    public static String toLegalPath(String path){
        if(path.contains(" ")){//如果包含空格，则windows下用引号引起来，而linux下则用\ 表示
            if(System.getProperty("os.name").contains("Linux"))
                return path.replace(" ", "\\ ");
            else
                return "\""+path+"\"";
        }
        else
            return path;
    }
    public static String getADBPath()
    {
        String path;
        if(System.getProperty("os.name").contains("Linux"))
            path = getAndroidSDKPath() + "/platform-tools/adb";
        else
            path = getAndroidSDKPath() + "/platform-tools/adb.exe";
        return toLegalPath(path);
    }

    public static String getAaptPath() {
        String path = getProperty("aapt_path");
        return toLegalPath(path);
    }

    public static String getDexPath(){
        String path = getProperty("dex_path");
        return toLegalPath(path);
    }

    public static String getAndroidPath()
    {
        String path;
        if(System.getProperty("os.name").contains("Linux"))
            path = getAndroidSDKPath() + "/tools/android";
        else
            path = getAndroidSDKPath() + "/tools/android.bat";
        return toLegalPath(path);
    }

    public static String getAntPath(){
        String path;
        if(System.getProperty("os.name").contains("Linux"))
            path = getAntRootPath() + "/bin/ant";
        else
            path = getAntRootPath() + "/bin/ant.bat";
        return toLegalPath(path);
    }

    public static  String getWorkspacePath(){
        File directory = new File("");
        return toLegalPath(directory.getAbsolutePath());
    }

    public static String getDotAndroidPath(){
        return prop.getProperty("dot_android_path");
    }

    public static int getParallelCount(){return Integer.parseInt(prop.getProperty("parallel_count"));}

    public static int getMaxStep(){return Integer.parseInt(prop.getProperty("max_step"));}

    public static double getDeltaL(){return Double.parseDouble(prop.getProperty("delta_l"));}

    public static int getDeltaC(){return Integer.parseInt(prop.getProperty("delta_c"));}
}
