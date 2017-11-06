package edu.nju.autodroid.androidagent;

import com.android.ddmlib.AdbCommandRejectedException;
import com.android.ddmlib.IDevice;
import com.android.ddmlib.ShellCommandUnresponsiveException;
import com.android.ddmlib.TimeoutException;
import edu.nju.autodroid.hierarchyHelper.AndroidWindow;
import edu.nju.autodroid.hierarchyHelper.LayoutNode;
import edu.nju.autodroid.uiautomator.UiautomatorClient;
import edu.nju.autodroid.utils.AdbTool;
import edu.nju.autodroid.utils.Logger;
import edu.nju.autodroid.utils.UiAutomatorTool;

import java.io.IOException;
import java.util.List;

/**
 * Created by ysht on 2016/3/8 0008.
 */
public class UiAutomatorAndroidAgent implements IAndroidAgent {
    private UiAutomatorTool uiautomator = new UiAutomatorTool();
    protected IDevice device;
    protected int localPort;
    protected int phonePort;

    public UiAutomatorAndroidAgent(IDevice device, int localPort, int phonePort){
        this.device = device;
        this.localPort = localPort;
        this.phonePort = phonePort;
    }

    @Override
    public boolean init() {
        boolean result;
        //初始化adb
        result = AdbTool.initializeBridge();
        if(!result)
            return false;

        //启动uiautomator
        try {
            int uiautomatorTaskId = AdbTool.getTaskId(device, "uiautomator");
            if (uiautomatorTaskId > 0)
                AdbTool.killTask(device, uiautomatorTaskId);
            Thread.sleep(2000);
            if (AdbTool.getTaskId(device, "uiautomator") < 0) {//如果uiautomator没有启动
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        UiautomatorClient.start(device.getSerialNumber(), UiautomatorClient.PHONE_PORT);
                    }
                }).start();
            }
            while (AdbTool.getTaskId(device, "uiautomator") < 0) {//等待uiautomator
                Logger.logInfo("Waiting for Uiautomator...");
                Thread.sleep(1000);
            }
            Logger.logInfo("UiAutomator start successfully!");
        }catch (Exception ex){
            ex.printStackTrace();
            return false;
        }

        //初始化uiautomator
        result = uiautomator.initializeConnection(device, localPort, phonePort);
        return result;
    }

    @Override
    public void terminate() {
        uiautomator.terminateConnection();

        //关闭uiautomator
        int uiautomatorTaskId = 0;
        try {
            uiautomatorTaskId = AdbTool.getTaskId(device, "uiautomator");
            if(uiautomatorTaskId > 0)
                AdbTool.killTask(device, uiautomatorTaskId);
        } catch (TimeoutException | AdbCommandRejectedException | IOException | ShellCommandUnresponsiveException e) {
            e.printStackTrace();
        }
    }

    @Override
    public IDevice getDevice() {
        return device;
    }

    @Override
    public boolean installApk(String apkFilePath) {
        return AdbTool.installApk(device.getSerialNumber(), apkFilePath);
    }

    @Override
    public String getFocusedActivity() {
        try {
            return AdbTool.getFocusedActivity(device);
        } catch (TimeoutException | AdbCommandRejectedException | ShellCommandUnresponsiveException | IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public boolean startActivity(String activityName) {
        try {
            return AdbTool.startActivity(device, activityName);
        } catch (TimeoutException | AdbCommandRejectedException | IOException | ShellCommandUnresponsiveException e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public boolean stopApplication(String packageName) {
        try {
            AdbTool.stopApplication(device, packageName);
            return true;
        } catch (TimeoutException | AdbCommandRejectedException | IOException | ShellCommandUnresponsiveException e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public List<String> getRunningActivities() {
        try {
            return AdbTool.getRunningActivities(device);
        } catch (TimeoutException | AdbCommandRejectedException | IOException | ShellCommandUnresponsiveException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public List<AndroidWindow> getAndroidWindows() {
        try {
            return AdbTool.getAndroidWindows(device);
        } catch (TimeoutException | AdbCommandRejectedException | IOException | ShellCommandUnresponsiveException | InterruptedException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public int getFocusedTaskId() {
        try {
            return AdbTool.getFocusedTaskId(device);
        } catch (TimeoutException | AdbCommandRejectedException | IOException | ShellCommandUnresponsiveException e) {
            e.printStackTrace();
        }
        return -1;
    }

    @Override
    public void pressHome() {
        uiautomator.pressHome();
    }

    @Override
    public void pressBack() {
        uiautomator.pressBack();
    }

    @Override
    public String getLayout() {
        return uiautomator.getLayout();
    }

    @Override
    public String getTopActivityId() {
        try {
            List<AndroidWindow> awList = AdbTool.getAndroidWindows(device);
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
                List<AndroidWindow> awList = AdbTool.getAndroidWindows(device);
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
        return uiautomator.getPackage();
    }

    @Override
    public boolean doClick(LayoutNode btn) {
        return uiautomator.doClick(btn);
    }

    @Override
    public boolean doSetText(LayoutNode node, String content) {
        return uiautomator.doSetText(node, content);
    }

    @Override
    public boolean doLongClick(LayoutNode node) {
        return uiautomator.doLongClick(node);
    }

    @Override
    public boolean doClickAndWaitForWindow(LayoutNode node) {
        return uiautomator.doClickAndWaitForNewWindow(node);
    }

    @Override
    public boolean doScrollForward(LayoutNode node, int steps) {
        return uiautomator.doScrollForward(node, steps);
    }

    @Override
    public boolean doScrollBackward(LayoutNode node, int steps) {
        return uiautomator.doScrollBackward(node, steps);
    }

    @Override
    public boolean doScrollToEnd(LayoutNode node, int maxSwipes, int steps) {
        return uiautomator.doScrollToEnd(node, maxSwipes, steps);
    }

    @Override
    public boolean doScrollToBeginning(LayoutNode node, int maxSwipes, int steps) {
        return uiautomator.doScrollToBeginning(node, maxSwipes, steps);
    }

    @Override
    public boolean doScrollIntoView(LayoutNode node, LayoutNode viewObj) {
        return uiautomator.doScrollIntoView(node, viewObj);
    }

    @Override
    public boolean doSwipeToLeft(LayoutNode node) {
        if(node == null)
            return false;
        int[] bound = node.bound;
        int x = (bound[0]+bound[2])/2;
        int y = (bound[1]+bound[3])/2;
        try {
            AdbTool.doSwipe(device, bound[2], y, bound[0], y);
        } catch (TimeoutException | AdbCommandRejectedException | ShellCommandUnresponsiveException | IOException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    @Override
    public boolean doSwipeToRight(LayoutNode node) {
        if(node == null)
            return false;
        int[] bound = node.bound;
        int x = (bound[0]+bound[2])/2;
        int y = (bound[1]+bound[3])/2;
        try {
            AdbTool.doSwipe(device, bound[0], y, bound[2], y);
        } catch (TimeoutException | AdbCommandRejectedException | ShellCommandUnresponsiveException | IOException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }
}
