package edu.nju.autodroid.androidagent;

import com.android.ddmlib.IDevice;

/**
 * Created by ysht on 2017/10/30 0030.
 */
public class UiAutomationAgent extends AdbAgent {
    public UiAutomationAgent(IDevice device, int localPort, int phonePort) {
        super(device, localPort, phonePort);
    }
}
