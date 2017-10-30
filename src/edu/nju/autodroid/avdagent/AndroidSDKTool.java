package edu.nju.autodroid.avdagent;

import edu.nju.autodroid.utils.CmdExecutor;
import edu.nju.autodroid.utils.Configuration;
import edu.nju.autodroid.utils.Logger;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by ysht on 2016/4/19 0019.
 */
public class AndroidSDKTool {
    protected static final String android = Configuration.getAndroidPath();

    public static List<AvdDevice> getDeviceList(){
        List<AvdDevice> deviceList = new ArrayList<AvdDevice>();
        String content = CmdExecutor.execCmd(android + " list device -c");
        String[] deviceNameList = content.split("\n");
        for(int i=0; i<deviceNameList.length; i++){
            deviceList.add(new AvdDevice(i, deviceNameList[i]));
        }
        return deviceList;
    }

    public static List<AvdTarget> getTargetList(){
        List<AvdTarget> targetList = new ArrayList<AvdTarget>();
        String content = CmdExecutor.execCmd(android + " list target");
        String[] targets = content.split("----------");
        for(int i=0; i<targets.length; i++){
            AvdTarget avdTarget = AvdTarget.parseAvdTarget(targets[i]);
            if(avdTarget != null)
                targetList.add(avdTarget);
        }
        return targetList;
    }

}
