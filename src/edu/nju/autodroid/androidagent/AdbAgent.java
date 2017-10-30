package edu.nju.autodroid.androidagent;

import com.android.ddmlib.*;
import edu.nju.autodroid.hierarchyHelper.AndroidWindow;
import edu.nju.autodroid.hierarchyHelper.LayoutNode;
import edu.nju.autodroid.utils.AdbTool;

import java.io.IOException;
import java.util.List;

/**
 * Created by ysht on 2016/3/8 0008.
 */
public class AdbAgent extends UiAutomatorAndroidAgent {
    public AdbAgent(IDevice device, int localPort, int phonePort) {
        super(device, localPort, phonePort);
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
            AdbTool.doPress(device, x, y);
        } catch (TimeoutException | AdbCommandRejectedException | IOException | ShellCommandUnresponsiveException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

/*    @Override
    public String getLayout() {

        try {
            return AdbTool.getLayout(device);
        } catch (TimeoutException | AdbCommandRejectedException | IOException | ShellCommandUnresponsiveException e) {
            e.printStackTrace();
        }
        return null;
    }*/
}
