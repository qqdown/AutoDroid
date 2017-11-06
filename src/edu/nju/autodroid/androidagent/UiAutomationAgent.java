package edu.nju.autodroid.androidagent;

import com.android.ddmlib.AdbCommandRejectedException;
import com.android.ddmlib.IDevice;
import com.android.ddmlib.ShellCommandUnresponsiveException;
import com.android.ddmlib.TimeoutException;
import edu.nju.autodroid.hierarchyHelper.AndroidWindow;
import edu.nju.autodroid.hierarchyHelper.LayoutNode;
import edu.nju.autodroid.uiautomator.Command;
import edu.nju.autodroid.utils.AdbTool;
import edu.nju.autodroid.utils.Logger;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.List;

/**
 * Created by ysht on 2017/10/30 0030.
 */
public class UiAutomationAgent implements IAndroidAgent {

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

        try {
            mDevice.createForward(localPort, phonePort);
            mSocket = new Socket("localhost", localPort);
            Logger.logInfo("UiAutomationAgent init成功！");
        } catch (IOException e) {
            e.printStackTrace();
            Logger.logInfo("UiAutomationAgent init失败！");
        } catch (TimeoutException | AdbCommandRejectedException e) {
            e.printStackTrace();
        }

        return false;
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
            return AdbTool.getFocusedActivity(mDevice);
        } catch (TimeoutException | AdbCommandRejectedException | ShellCommandUnresponsiveException | IOException e) {
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

    }

    @Override
    public void pressBack() {

    }

    @Override
    public String getLayout() {
        Command cmd = new Command();
        cmd.cmd = Command.cmdGetLayout;
        sendCommand(cmd);
        cmd = receiveCommand();
        if(cmd.cmd != Command.cmdGetLayout)
            return null;
        return cmd.params[0];
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
        return false;
    }

    @Override
    public boolean doSetText(LayoutNode node, String content) {
        return false;
    }

    @Override
    public boolean doLongClick(LayoutNode node) {
        return false;
    }

    @Override
    public boolean doClickAndWaitForWindow(LayoutNode node) {
        return false;
    }

    @Override
    public boolean doScrollBackward(LayoutNode node, int steps) {
        return false;
    }

    @Override
    public boolean doScrollForward(LayoutNode node, int steps) {
        return false;
    }

    @Override
    public boolean doScrollToEnd(LayoutNode node, int maxSwipes, int steps) {
        return false;
    }

    @Override
    public boolean doScrollToBeginning(LayoutNode node, int maxSwipes, int steps) {
        return false;
    }

    @Override
    public boolean doScrollIntoView(LayoutNode node, LayoutNode viewObj) {
        return false;
    }

    @Override
    public boolean doSwipeToLeft(LayoutNode node) {
        return false;
    }

    @Override
    public boolean doSwipeToRight(LayoutNode node) {
        return false;
    }
}
