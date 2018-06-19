package edu.nju.autodroid.utils;

import com.android.ddmlib.*;
import com.sun.corba.se.impl.javax.rmi.CORBA.Util;
import com.sun.istack.internal.Nullable;
import edu.nju.autodroid.hierarchyHelper.AndroidWindow;
import edu.nju.autodroid.utils.CmdExecutor;
import edu.nju.autodroid.utils.Configuration;
import edu.nju.autodroid.utils.Logger;
import org.apache.commons.net.telnet.TelnetClient;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

/**
 * 利用Adb的工具类
 * Created by ysht on 2016/3/7 0007.
 */
public class AdbTool {
    //用于同步
    private final static Object sSync = new Object();
    protected static boolean adbInitialized = false;
    protected static AndroidDebugBridge adb = null;

    protected IDevice device;

    protected AdbTool(){}

    /**
     * 初始化adb,可以重复初始化，函数会自动判断是否已经初始化
     * @return 初始化是否成功
     */
    public static boolean initializeBridge() {
        synchronized (sSync) {
            if (!adbInitialized) {
                try {
                    AndroidDebugBridge.init(false);
                    //AndroidDebugBridge.init(true);
                    adb = AndroidDebugBridge.createBridge(
                            Configuration.getADBPath(), true);
                    waitForInitialDeviceList();
                    adbInitialized = true;
                    Logger.logInfo("Init Bridge successfully!");
                } catch (Exception e) {
                    Logger.logException(e);
                }
            }
            return adbInitialized;
        }
    }

    /**
     * 终止adb
     */
    public static void terminateBridge()
    {
        if(!adbInitialized)
            return;

        synchronized (sSync) {
            AndroidDebugBridge.terminate();
            adbInitialized = false;
        }
    }

    //等待设备响应
    private static boolean waitForInitialDeviceList()
    {
        int count = 0;
        while (!adb.hasInitialDeviceList())
        {
            try
            {
                Thread.sleep(100);
                count++;
            }
            catch (InterruptedException e)
            {
                Logger.logException(e.getMessage());
                return false;
            }

            if (count > 100)
            {
                Logger.logError("获取设备超时");
                return false;
            }
        }
        return true;
    }

    /**
     * 获取默认（第一个）设备，如果无设备，返回null
     * @return 设备
     */
    public static IDevice getDefaultDevice(){
        assert (adbInitialized);
        synchronized (sSync) {
            IDevice[] recognizedDevices = adb.getDevices();
            if(recognizedDevices == null || recognizedDevices.length==0)
                return null;
            return recognizedDevices[0];
        }
    }

    /**
     * 获取设备列表
     * @return 设备列表
     */
    public static List<IDevice> getDevices(){
        assert (adbInitialized);
        synchronized (sSync) {
            return Arrays.asList(adb.getDevices());
        }
    }

    /**
     * 获取设备名列表
     * @return 设备名列表
     */
    public static List<String> getDeviceNames()
    {
        assert (adbInitialized);
        List<String> deviceNames = new ArrayList<String>();
        synchronized (sSync) {
            IDevice[] recognizedDevices = adb.getDevices();
            for (IDevice currDev : recognizedDevices) {
                if (currDev.isOnline()) {
                    deviceNames.add(currDev.getName());
                }
            }
            return deviceNames;
        }
    }

    /**
     * 获取指定的Device
     * @param deviceName 设备名
     * @return 设备
     */
    public static IDevice getIDevice(String deviceName) {
        assert (adbInitialized);
        //assert (!isDeviceBusy(deviceName));
        synchronized (sSync) {
            IDevice targetDevice = null;
            IDevice[] recognizedDevices = adb.getDevices();
            for (IDevice currDev : recognizedDevices) {
                if (currDev.isOnline()
                        && currDev.toString().equalsIgnoreCase(deviceName)) {
                    targetDevice = currDev;
                    break;
                }
            }
            return targetDevice;
        }
    }

    public static IDevice getIDeviceByAvd(String avdName){
        assert (adbInitialized);
        //assert (!isDeviceBusy(deviceName));
        synchronized (sSync) {
            IDevice targetDevice = null;
            IDevice[] recognizedDevices = adb.getDevices();
            for (IDevice currDev : recognizedDevices) {
                if (currDev.isOnline() &&
                       currDev.getAvdName() != null && currDev.getAvdName().equalsIgnoreCase(avdName)) {
                    targetDevice = currDev;
                    break;
                }
            }
            return targetDevice;
        }
    }

    /**
     * 获取指定index的设备
     * @param deviceIndex 第deviceIndex个设备
     * @return 设备
     */
    public static IDevice getIDevice(int deviceIndex)
    {
        assert (adbInitialized);
        synchronized (sSync) {
            IDevice[] recognizedDevices = adb.getDevices();
            if(deviceIndex >= recognizedDevices.length || deviceIndex < 0)
                return null;
            return recognizedDevices[deviceIndex];
        }
    }

    /**
     * 向设备安装apk
     * @param deviceSerial 目标设备
     * @param apkFilePath apk文件路径
     * @return 是否安装成功
     */
    public static boolean installApk(String deviceSerial, String apkFilePath){
        File apkFile = new File(apkFilePath);
        if(!apkFile.exists())
            return false;
        String output = CmdExecutor.execCmd(Configuration.getADBPath() + " -s " + deviceSerial +  " install -r \"" + apkFile.getAbsolutePath() + "\"");
        System.out.println(output);
        boolean success = output.contains("Success");
        if(success){
            List<String> permissions = getPermissionsFromApk(apkFilePath);
            String packageName = getPackageFromApk(apkFilePath);
            for (String permission : permissions){
                CmdExecutor.execCmd(Configuration.getADBPath() + " shell pm grant " + packageName + " " + permission);
            }

            return true;
        }
        return false;
    }

    public static boolean unInstallApk(String deviceSerial, String packageName){
        String output = CmdExecutor.execCmd(Configuration.getADBPath() + " -s " + deviceSerial +  " uninstall " + packageName);
        return output.contains("Success");
    }

    public static String getPackageFromApk(String apkFilePath){
        File apkFile = new File(apkFilePath);
        if(!apkFile.exists())
            return null;
        String output = CmdExecutor.execCmd(Configuration.getAaptPath() + " dump badging \"" + apkFilePath + "\"");
        String[] lines = output.split("\n");
        for (String line : lines){
            if(line.startsWith("package:")){
                int l = line.indexOf("name='");
                String str = line.substring(l+6);
                int r = str.indexOf("'");
                str = str.substring(0, r);
                return str;
            }
        }
        return "";
    }

    public static List<String> getPermissionsFromApk(String apkFilePath){
        File apkFile = new File(apkFilePath);
        if(!apkFile.exists())
            return null;
        List<String> permissions = new ArrayList<String>();
        String output = CmdExecutor.execCmd(Configuration.getAaptPath() + " d permissions \"" + apkFilePath + "\"");
        String[] lines = output.split("\n");
        final String prefix = "uses-permission: name='";
        for(String line : lines){
            if(line.trim().startsWith(prefix)){
                String permission = line.substring(prefix.length());
                int i = permission.indexOf("'");
                if(i>=0)
                    permission = permission.substring(0, i);
                if(permission.startsWith("android.permission."))
                    permissions.add(permission);
            }
        }
        return permissions;
    }

    public static String getLaunchableAcvivity(String apkFilePath){
        File apkFile = new File(apkFilePath);
        if(!apkFile.exists())
            return null;
        String output = CmdExecutor.execCmd(Configuration.getAaptPath() + " dump badging \"" + apkFilePath + "\"");
        String[] lines = output.split("\n");
        String activity = "";
        String packageName = "";
        for (String line : lines){
            if(line.startsWith("launchable-activity:")){
                int l = line.indexOf("name='");
                String str = line.substring(l+6);
                int r = str.indexOf("'");
                activity = str.substring(0, r);
            }
            else if(line.startsWith("package:")){
                int l = line.indexOf("name='");
                String str = line.substring(l+6);
                int r = str.indexOf("'");
                packageName = str.substring(0, r);
            }
        }
        return packageName + "/" + activity;
    }

    public static boolean hasInstalledPackage(IDevice device, String packageName) throws TimeoutException, AdbCommandRejectedException, ShellCommandUnresponsiveException, IOException {
        boolean[] result = new boolean[1];
        result[0] = false;

        if(device != null) {
            device.executeShellCommand("pm list package", new IShellOutputReceiver() {
                @Override
                public void addOutput(byte[] bytes, int i, int i1) {
                    /*
                    if(bytes.length > 0)
                        result[0] = true;
                    else
                        result[0] = false;*/
                    String output = new String(bytes);
                    if(output.contains("package:"+packageName))
                        result[0] = true;
                    else
                        result[0] = false;
                }
                @Override
                public void flush() {}
                @Override
                public boolean isCancelled() {return false;}
            });
        }
        return result[0];
    }


    /**
     * 获得当前Activity名
     * @param device 目标设备
     * @return activity名
     * @throws TimeoutException
     * @throws AdbCommandRejectedException
     * @throws ShellCommandUnresponsiveException
     * @throws IOException
     */
    public static String getFocusedActivity(IDevice device) throws TimeoutException, AdbCommandRejectedException, ShellCommandUnresponsiveException, IOException {
        final String[] result = new String[1];
        result[0] = null;
        if(device != null) {
            device.executeShellCommand("dumpsys activity activities | grep mFocusedActivity", new IShellOutputReceiver() {
                @Override
                public boolean isCancelled() {return false;}
                @Override
                public void flush() {}
                @Override
                public void addOutput(byte[] arg0, int arg1, int arg2) {
                    String output = new String(arg0);
                    int i1, i2;
                    i1 = output.indexOf('{');
                    i2 = output.indexOf('}');
                    if (i1 < 0 || i2 < 0)
                        return;
                    output = output.substring(i1 + 1, i2);
                    result[0] = output.split(" ")[2];
                }
            });
        }
        else
            Logger.logError("设备为空！");
        return result[0];
    }

    /**
     * 启动程序
     * @param device 目标设备
     * @param activityName 完整的activity名，格式为packageName/.activityName
     * @return 是否成功启动
     * @throws TimeoutException
     * @throws AdbCommandRejectedException
     * @throws ShellCommandUnresponsiveException
     * @throws IOException
     */
    public static boolean startActivity(IDevice device, String activityName) throws TimeoutException, AdbCommandRejectedException, ShellCommandUnresponsiveException, IOException{
        boolean[] result = new boolean[1];
        result[0] = false;
        if(device != null) {
            device.executeShellCommand("am start -n " + activityName, new IShellOutputReceiver() {
                @Override
                public boolean isCancelled() {return false;}
                @Override
                public void flush() {}
                @Override
                public void addOutput(byte[] arg0, int arg1, int arg2) {
                    String output = new String(arg0);
                    if(output.contains("Error"))
                    {
                        result[0] = false;
                        Logger.logError("startActivity " +  activityName + ": " + device.getName() + " " + output);
                    }
                    else
                        result[0] = true;
                }
            });
        }
        else
            Logger.logError("设备为空！");
        return result[0];
    }

    /**
     * 停止应用
     * @param device 目标设备
     * @param packageName 应用包名
     * @throws TimeoutException
     * @throws AdbCommandRejectedException
     * @throws ShellCommandUnresponsiveException
     * @throws IOException
     */
    public static void stopApplication(IDevice device, String packageName) throws TimeoutException, AdbCommandRejectedException, ShellCommandUnresponsiveException, IOException{
        if(device != null) {
            device.executeShellCommand("am force-stop " + packageName, new IShellOutputReceiver() {
                @Override
                public boolean isCancelled() {return false;}
                @Override
                public void flush() {}
                @Override
                public void addOutput(byte[] arg0, int arg1, int arg2) {
                    String output = new String(arg0);
                    Logger.logInfo("stop application: " + output);
                }
            });
        }
        else
            Logger.logError("设备为空！");
    }

    /**
     * 获取当前正在运行的Activity，返回包含Activity名的List，顺序为运行栈顶-》栈底
     * @param device 目标设备
     * @return 包含Activity名的List
     * @throws TimeoutException
     * @throws AdbCommandRejectedException
     * @throws ShellCommandUnresponsiveException
     * @throws IOException
     */
    public static List<String>  getRunningActivities(IDevice device) throws TimeoutException, AdbCommandRejectedException, ShellCommandUnresponsiveException, IOException {
        final List<String> result = new ArrayList<String>();
        if(device != null) {
            device.executeShellCommand("dumpsys activity | grep 'Run #'", new IShellOutputReceiver() {
                @Override
                public boolean isCancelled() {return false;}
                @Override
                public void flush() {}
                @Override
                public void addOutput(byte[] arg0, int arg1, int arg2) {
                    String output = new String(arg0);
                    String[] lines = output.split("\n");

                    for(String line : lines) {
                        if (line.isEmpty())
                            continue;
                        int l, r;
                        l = line.indexOf("{");
                        r = line.indexOf("}");
                        if (l < 0 || r < 0)
                            continue;
                        result.add(line.substring(l + 1, r).split(" ")[2]);
                    }
                }
            });
        }
        else
            Logger.logError("设备为空！");
        return result;
    }

    /**
     * 获取设备中的AndroidWindow列表，通过dumpsys获取。
     * 注意！若无法获取到信息，该函数会循环尝试直到获取信息，每次循环间隔为500ms
     * @param device 目标设备
     * @return AndroidWindow列表
     * @throws TimeoutException
     * @throws AdbCommandRejectedException
     * @throws ShellCommandUnresponsiveException
     * @throws IOException
     * @throws InterruptedException
     */
    public static List<AndroidWindow> getAndroidWindows(IDevice device) throws TimeoutException, AdbCommandRejectedException, ShellCommandUnresponsiveException, IOException, InterruptedException{
        boolean[] success = new boolean[1];
        final List<AndroidWindow> result = new ArrayList<AndroidWindow>();
        if(device == null)
        {
            Logger.logError("设备为空！");
            return result;
        }
        success[0] = false;
        while(!success[0])
        {
            Logger.logInfo("Wait for getAndroidWindows success!");
            device.executeShellCommand("dumpsys window windows", new  IShellOutputReceiver() {
                @Override
                public void addOutput(byte[] arg0, int arg1, int arg2) {
                    String output = new String(arg0);
                    String[] lines = output.split("\n");
                    AndroidWindow window = null;
                    for(String line : lines){
                        if(getPrefixSpaceLength(line) == 2){//2个空格起头的行认为是window起始行
                            if(line.contains("Window #"))
                            {
                                window = new AndroidWindow();
                                result.add(window);
                                int l = line.indexOf('{');
                                int r = line.indexOf('}');
                                if(l<0 || r<=l){
                                    return;
                                }
                                String[] winStr = line.substring(l+1, r).split(" ");
                                window.id = winStr[0];
                                //window.id = winStr[2];//把activity全名作为id
                                if(winStr.length >= 3)//可能不会有第三个值
                                    window.activityName = winStr[2];
                                success[0] = true;
                            }
                            else
                            {
                                window = null;
                            }
                        }

                        if(window != null){
                            String sessionKey = "mSession=Session{";
                            if(line.contains(sessionKey)){
                                int l = line.indexOf(sessionKey);
                                String subStr = line.substring(l);
                                int r = subStr.indexOf('}');
                                if(r>0)
                                    window.session = subStr.substring(sessionKey.length(), r);
                            }
                        }
                    }
                }
                @Override
                public void flush() {}
                @Override
                public boolean isCancelled() {return false;}
            });
            if(!success[0]){//用于等待windowList
                Logger.logInfo("Wait for windowlist");
                Thread.sleep(1000);
            }
        }
        return result;
    }

    /**
     * 获取当前Task的id
     * @param device 目标设备
     * @return Task id
     * @throws TimeoutException
     * @throws AdbCommandRejectedException
     * @throws ShellCommandUnresponsiveException
     * @throws IOException
     */
    public static int getFocusedTaskId(IDevice device) throws TimeoutException, AdbCommandRejectedException, ShellCommandUnresponsiveException, IOException{
        final int[] id = new int[1];
        id[0] = -1;
        if(device == null)
        {
            Logger.logError("设备为空！");
            return id[0];
        }
        String focusedPackage = getFocusedActivity(device).split("/")[0];

        device.executeShellCommand("ps | grep \"" + focusedPackage + "\"", new  IShellOutputReceiver() {
            @Override
            public void addOutput(byte[] arg0, int arg1, int arg2) {
                String output = new String(arg0);
                int l = output.indexOf(' ');
                for(; l<output.length(); l++){
                    if(output.charAt(l) != ' ')
                        break;
                }
                String idStr = output.substring(l).split(" ")[0];
                id[0] = Integer.parseInt(idStr);
            }
            @Override
            public void flush() {}
            @Override
            public boolean isCancelled() {return false;}
        });
        return id[0];
    }

    public static int getTaskId(IDevice device, String taskName) throws TimeoutException, AdbCommandRejectedException, ShellCommandUnresponsiveException, IOException {

        if(device == null )
        {
            Logger.logError("设备为空！");
            return -1;
        }
        if(device.isOffline()){
            Logger.logInfo(device.getAvdName() + " 设备未上线！");
            return -1;
        }
        int[] id = new int[1];
        id[0] = -1;
        device.executeShellCommand("ps | grep '"+ taskName + "'", new  IShellOutputReceiver() {
            @Override
            public void addOutput(byte[] arg0, int arg1, int arg2) {
                String output = new String(arg0);
                for(String line : output.split("\n")){
                    String[] params = line.split(" +");
                    if(params.length == 9 && params[8].contains(taskName)){
                        id[0] = Integer.parseInt(params[1]);
                        return;
                    }
                }
            }
            @Override
            public void flush() {}
            @Override
            public boolean isCancelled() {return false;}
        });
        return id[0];
    }

    public static void killTask(IDevice device, int taskId) throws TimeoutException, AdbCommandRejectedException, ShellCommandUnresponsiveException, IOException {
        if(device == null)
        {
            Logger.logError("设备为空！");
            return;
        }
        device.executeShellCommand("kill " + taskId, new  IShellOutputReceiver() {
            @Override
            public void addOutput(byte[] arg0, int arg1, int arg2) {}
            @Override
            public void flush() {}
            @Override
            public boolean isCancelled() {return false;}
        });
        return;
    }


    public static void doPress(IDevice device, int x, int y) throws TimeoutException, AdbCommandRejectedException, ShellCommandUnresponsiveException, IOException {
        if(device == null)
        {
            Logger.logError("设备为空！");
            return;
        }

        device.executeShellCommand(String.format("input tap %d %d", x,y), new  IShellOutputReceiver() {
            @Override
            public void addOutput(byte[] arg0, int arg1, int arg2) {}
            @Override
            public void flush() {}
            @Override
            public boolean isCancelled() {return false;}
        });
    }

    public static void doKeyEvent(IDevice device, int keyCode) throws TimeoutException, AdbCommandRejectedException, ShellCommandUnresponsiveException, IOException {
        if(device == null)
        {
            Logger.logError("设备为空！");
            return;
        }

        device.executeShellCommand(String.format("input keyevent %d", keyCode), new  IShellOutputReceiver() {
            @Override
            public void addOutput(byte[] arg0, int arg1, int arg2) {}
            @Override
            public void flush() {}
            @Override
            public boolean isCancelled() {return false;}
        });
    }

    /**
     * 长按操作 有问题，4.4一下系统不可用
     * @param device 设备
     * @param x 坐标x
     * @param y 坐标y
     * @param duration 长按时间（毫秒），默认建议设置为1000
     * @throws TimeoutException
     * @throws AdbCommandRejectedException
     * @throws ShellCommandUnresponsiveException
     * @throws IOException
     */
    public static void doLongPress(IDevice device, int x, int y, int duration) throws TimeoutException, AdbCommandRejectedException, ShellCommandUnresponsiveException, IOException {
        if(device == null)
        {
            Logger.logError("设备为空！");
            return;
        }

        device.executeShellCommand(String.format("input swipe %d %d %d %d %d", x,y,x,y,duration), new  IShellOutputReceiver() {
            @Override
            public void addOutput(byte[] arg0, int arg1, int arg2) {}
            @Override
            public void flush() {}
            @Override
            public boolean isCancelled() {return false;}
        });
    }

    public static void doSwipe(IDevice device, int fx, int fy, int tx, int ty) throws TimeoutException, AdbCommandRejectedException, ShellCommandUnresponsiveException, IOException {
        if(device == null)
        {
            Logger.logError("设备为空！");
            return;
        }
        if(fx>tx+1)
        {
            fx -= 1;
            tx += 1;
        }
        else if(tx>fx+1)
        {
            tx -= 1;
            fx += 1;
        }
        device.executeShellCommand(String.format("input swipe %d %d %d %d", fx,fy, tx,ty), new  IShellOutputReceiver() {
            @Override
            public void addOutput(byte[] arg0, int arg1, int arg2) {}
            @Override
            public void flush() {}
            @Override
            public boolean isCancelled() {return false;}
        });
    }


    public static boolean areActivityNameSame(String activity1, String activity2){
        String[] ac1 = activity1.split("/");
        String[] ac2 = activity2.split("/");
        if(ac1.length != ac2.length)
            return false;
        if(ac1.length == 1)
            return activity1.equals(activity2);
        else if(ac1.length == 2 && ac1[0].equals(ac2[0])){//package名相同
            return ac1[1].endsWith(ac2[1]) || ac2[1].endsWith(ac1[1]);
        }
        return false;
    }

    public static void stopDevice(IDevice device){
        if(device == null)
        {
            Logger.logError("设备为空！");
            return;
        }
        try {
            String deviceName = device.getName();
            int port = Utils.parseInt(deviceName.substring(deviceName.indexOf('-')+1));
            TelnetClient telnetClient = new TelnetClient();
            telnetClient.connect("localhost", port);
            PrintStream ps = new PrintStream(telnetClient.getOutputStream(), true);
            ps.println("kill");
            ps.close();
            telnetClient.disconnect();

        } catch (IOException e) {
            e.printStackTrace();
            Logger.logException(e);
        }
    }

    public static String getLayout(IDevice device) throws TimeoutException, AdbCommandRejectedException, ShellCommandUnresponsiveException, IOException {
        if(device == null)
        {
            Logger.logError("设备为空！");
            return null;
        }

        String[] result = new String[1];
        device.executeShellCommand("uiautomator dump /sdcard/autodroid_dump.xml", new  IShellOutputReceiver() {
            @Override
            public void addOutput(byte[] arg0, int arg1, int arg2) {
                String output  = new String(arg0);
                if(output.trim().equals("UI hierchary dumped to: /sdcard/autodroid_dump.xml")){
                    result[0] = "true";
                }
                else
                    result[0] = "false";
            }
            @Override
            public void flush() {}
            @Override
            public boolean isCancelled() {return false;}
        });

        if(result[0].equals("true")){
            StringBuilder sb = new StringBuilder();
            device.executeShellCommand("cat /sdcard/autodroid_dump.xml", new  IShellOutputReceiver() {
                @Override
                public void addOutput(byte[] arg0, int arg1, int arg2) {
                    String output  = new String(arg0, arg1, arg2);
                   sb.append(output);
                }
                @Override
                public void flush() {}
                @Override
                public boolean isCancelled() {return false;}
            });

            return sb.toString();
        }
        return null;
    }

    public static void screenCapture(IDevice device, String savedPath) throws TimeoutException, AdbCommandRejectedException, ShellCommandUnresponsiveException, IOException {
        if(device == null)
        {
            Logger.logError("设备为空！");
            return;
        }

        String[] result = new String[1];
        device.executeShellCommand("screencap -p /sdcard/screen.png", new  IShellOutputReceiver() {
            @Override
            public void addOutput(byte[] arg0, int arg1, int arg2) {

            }
            @Override
            public void flush() {}
            @Override
            public boolean isCancelled() {return false;}
        });

        CmdExecutor.execCmd(Configuration.getADBPath() + " pull /sdcard/screen.png " + savedPath);
    }

    //获取str开头的空格个数
    private static int getPrefixSpaceLength(String str){
        int i;
        for(i=0; i<str.length(); i++){
            if(str.charAt(i) != ' ')
                break;
        }
        return i;
    }
}
