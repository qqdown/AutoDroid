package edu.nju.autodroid.androidagent;

import com.android.ddmlib.AdbCommandRejectedException;
import com.android.ddmlib.IDevice;
import com.android.ddmlib.ShellCommandUnresponsiveException;
import com.android.ddmlib.TimeoutException;
import com.sun.glass.events.KeyEvent;
import com.sun.webkit.ThemeClient;
import edu.nju.autodroid.hierarchyHelper.AndroidWindow;
import edu.nju.autodroid.hierarchyHelper.LayoutNode;
import edu.nju.autodroid.uiautomator.Command;
import edu.nju.autodroid.utils.AdbTool;
import edu.nju.autodroid.utils.CmdExecutor;
import edu.nju.autodroid.utils.Configuration;
import edu.nju.autodroid.utils.Logger;

import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.Date;
import java.util.List;
import java.util.UnknownFormatConversionException;

/**
 * Created by ysht on 2017/10/30 0030.
 */
public class UiAutomationAgent implements IAndroidAgent {

    private static final String dex = Configuration.getDexPath();
    private static final String adb = Configuration.getADBPath();

    private Socket mSocket;

    protected IDevice mDevice;
    protected int localPort;
    protected int phonePort;
    private ObjectOutputStream oos = null;
    private ObjectInputStream ois = null;

    public UiAutomationAgent(IDevice device,  int localPort, int phonePort){
        this.mDevice = device;
        this.localPort = localPort;
        this.phonePort = phonePort;
    }

    @Override
    public boolean init() {
        startUiViewer();

        try {
            Thread.sleep(1000);
            mDevice.createForward(localPort, phonePort);
            mSocket = new Socket("localhost", localPort);
            Logger.logInfo("UiAutomationAgent init成功！");
            return true;
        } catch (IOException|TimeoutException | AdbCommandRejectedException | InterruptedException e) {
            e.printStackTrace();
            Logger.logInfo("UiAutomationAgent init失败！");
            return false;
        }
    }

    private void startUiViewer(){
        try {

            int taskId = AdbTool.getTaskId(mDevice, "/system/bin/app_process");
            if(taskId < 0) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        CmdExecutor.execCmd(adb + " push tools/uiviewer/UiViewer.jar /sdcard/");
                        CmdExecutor.execCmd(adb + " shell CLASSPATH=/sdcard/UiViewer.jar /system/bin/app_process /sdcard/ edu.nju.uiviewer.Main");
                    }
                }).start();
                while (taskId < 0)
                {
                    Thread.sleep(500);
                    Logger.logInfo("等待Uiviewer启动");
                    taskId = AdbTool.getTaskId(mDevice, "/system/bin/app_process");

                }

            }
        } catch (TimeoutException | AdbCommandRejectedException | IOException | ShellCommandUnresponsiveException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }

    public void sendCommand(Command cmd){
        try {
            if(oos == null)
                oos =  new ObjectOutputStream(mSocket.getOutputStream());
            oos.writeObject(cmd);
        } catch (IOException e) {
            System.out.println("UiAutomationAgent sendCommand: server error " + e.getMessage());
        }
    }

    /**
     * 接受并返回命令，该命令为阻塞函数，直到收到命令才返回。
     * @return
     */
    public Command receiveCommand(){
        try {
            if(ois == null)
                ois = new ObjectInputStream(mSocket.getInputStream());
            long milli = System.currentTimeMillis();
            Command cmd = (Command)ois.readObject();
            Logger.logInfo("Receive " + (System.currentTimeMillis()-milli)/1000.0 + "");
            return cmd;
        } catch (ClassNotFoundException | IOException e) {
            System.out.println("UiAutomationAgent ReceiveCommand error: PC error " + e.getMessage());
            return null;
        }
    }

    @Override
    public void terminate() {
        try{
            if(mSocket != null)
                mSocket.close();
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    @Override
    public IDevice getDevice() {
        return mDevice;
    }

    @Override
    public boolean installApk(String apkFilePath) {
        return AdbTool.installApk(mDevice.getSerialNumber(), apkFilePath);

    }

    @Override
    public String getFocusedActivity() {
        try {
            String activity = AdbTool.getFocusedActivity(mDevice);
            int count = 4;
            while (activity == null && count-->=0){
                activity = AdbTool.getFocusedActivity(mDevice);
                Logger.logInfo("waiting for getFocusedActivity");
                Thread.sleep(500);
            }
            if(activity == null)
                return "com.android.launcher3/.Launcher";//如果长时间获取不到activity，我们认为是在桌面
            return activity;
        } catch (TimeoutException | AdbCommandRejectedException | ShellCommandUnresponsiveException | IOException | InterruptedException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public boolean startActivity(String activityName) {
        try {
            return AdbTool.startActivity(mDevice, activityName);
        } catch (TimeoutException | AdbCommandRejectedException | IOException | ShellCommandUnresponsiveException e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public boolean stopApplication(String packageName) {
        try {
            AdbTool.stopApplication(mDevice, packageName);
            return true;
        } catch (TimeoutException | AdbCommandRejectedException | IOException | ShellCommandUnresponsiveException e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public List<String> getRunningActivities() {
        try {
            return AdbTool.getRunningActivities(mDevice);
        } catch (TimeoutException | AdbCommandRejectedException | IOException | ShellCommandUnresponsiveException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public List<AndroidWindow> getAndroidWindows() {
        try {
            return AdbTool.getAndroidWindows(mDevice);
        } catch (TimeoutException | AdbCommandRejectedException | IOException | ShellCommandUnresponsiveException | InterruptedException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public int getFocusedTaskId() {
        try {
            return AdbTool.getFocusedTaskId(mDevice);
        } catch (TimeoutException | AdbCommandRejectedException | IOException | ShellCommandUnresponsiveException e) {
            e.printStackTrace();
        }
        return -1;
    }

    @Override
    public void pressHome() {

        try {
            AdbTool.doKeyEvent(mDevice, android.view.KeyEvent.KEYCODE_HOME);
        } catch (TimeoutException | AdbCommandRejectedException | IOException | ShellCommandUnresponsiveException e) {
            e.printStackTrace();
            return;
        }
    }

    @Override
    public void pressBack() {
        try {
            AdbTool.doKeyEvent(mDevice, android.view.KeyEvent.KEYCODE_BACK);
        } catch (TimeoutException | AdbCommandRejectedException | IOException | ShellCommandUnresponsiveException e) {
            e.printStackTrace();
            return;
        }
    }

    @Override
    public String getLayout() {
        Command cmd = new Command();
        cmd.cmd = Command.cmdGetLayout;
        sendCommand(cmd);
        Command[] receivedCmd = new Command[1];

        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                receivedCmd[0] = receiveCommand();
            }
        });
        thread.start();
        Date dt = new Date();
        while (receivedCmd[0] == null && (new Date().getTime() - dt.getTime() <= 100000)){//100秒没反应
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        thread = null;
        if(receivedCmd[0] == null)
            return null;
        if(receivedCmd[0].cmd != Command.cmdGetLayout)
            return null;
        return receivedCmd[0].params[0];
    }

    @Override
    public String getTopActivityId() {
        try {
            List<AndroidWindow> awList = AdbTool.getAndroidWindows(mDevice);
            String packageName = getRuntimePackage();
            for (int i = 0; i < awList.size(); i++) {
                AndroidWindow aw = awList.get(i);
                if (aw.activityName != null) {
                    int index = aw.activityName.indexOf('/');
                    if (index > 0 && aw.activityName.substring(0, index).contains(packageName)) {
                        return aw.id;
                    }
                }
            }
        } catch (TimeoutException | AdbCommandRejectedException | IOException | ShellCommandUnresponsiveException | InterruptedException e) {
            e.printStackTrace();
        }

        return null;
    }

    @Override
    public String getTopActivity() {
        int tryCount = 10;
        while (tryCount-- > 0) {
            try {
                List<AndroidWindow> awList = AdbTool.getAndroidWindows(mDevice);
                String packageName = getRuntimePackage();
                for (int i = 0; i < awList.size(); i++) {
                    AndroidWindow aw = awList.get(i);
                    if (aw.activityName != null) {
                        int index = aw.activityName.indexOf('/');
                        if (index > 0 && aw.activityName.substring(0, index).contains(packageName)) {
                            Logger.logInfo("Get top activity " + aw.activityName);
                            return aw.activityName;
                        }
                    }
                }
            } catch (TimeoutException | AdbCommandRejectedException | IOException | ShellCommandUnresponsiveException | InterruptedException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    @Override
    public String getRuntimePackage() {
        String activity = getFocusedActivity();
        if(activity == null)
            return null;
        int i = activity.indexOf('/');
        if(i<0)
            return null;
        return activity.substring(0, i);
    }

    @Override
    public boolean doClick(LayoutNode btn) {
        if(btn == null)
            return false;
        int[] bound = btn.bound;
        int x = (bound[0]+bound[2])/2;
        int y = (bound[1]+bound[3])/2;
        try {
            AdbTool.doPress(mDevice, x, y);
        } catch (TimeoutException | AdbCommandRejectedException | IOException | ShellCommandUnresponsiveException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    @Override
    public boolean doSetText(LayoutNode node, String content) {
        throw new UnknownFormatConversionException("暂未实现该方法!");
    }

    @Override
    public boolean doLongClick(LayoutNode node) {
        throw new UnknownFormatConversionException("暂未实现该方法!");
    }

    @Override
    public boolean doClickAndWaitForWindow(LayoutNode node) {
        throw new UnknownFormatConversionException("暂未实现该方法!");
    }

    @Override
    public boolean doScrollBackward(LayoutNode node, int steps) {
        throw new UnknownFormatConversionException("暂未实现该方法!");
    }

    @Override
    public boolean doScrollForward(LayoutNode node, int steps) {
        throw new UnknownFormatConversionException("暂未实现该方法!");
    }

    @Override
    public boolean doScrollToEnd(LayoutNode node, int maxSwipes, int steps) {
        throw new UnknownFormatConversionException("暂未实现该方法!");
    }

    @Override
    public boolean doScrollToBeginning(LayoutNode node, int maxSwipes, int steps) {
        throw new UnknownFormatConversionException("暂未实现该方法!");
    }

    @Override
    public boolean doScrollIntoView(LayoutNode node, LayoutNode viewObj) {
        throw new UnknownFormatConversionException("暂未实现该方法!");
    }

    @Override
    public boolean doSwipeToLeft(LayoutNode node) {
        throw new UnknownFormatConversionException("暂未实现该方法!");
    }

    @Override
    public boolean doSwipeToRight(LayoutNode node) {
        throw new UnknownFormatConversionException("暂未实现该方法!");
    }
}
