package edu.nju.autodroid.main;

import com.android.ddmlib.AdbCommandRejectedException;
import com.android.ddmlib.IDevice;
import com.android.ddmlib.ShellCommandUnresponsiveException;
import com.android.ddmlib.TimeoutException;
import com.android.ddmuilib.SysinfoPanel;
import edu.nju.autodroid.utils.AdbTool;

import java.io.*;
import java.util.Scanner;

/**
 * Created by ysht on 2018/4/10.
 */
public class Main_Check {
    public static void main(String[] args) throws TimeoutException, AdbCommandRejectedException, ShellCommandUnresponsiveException, IOException, InterruptedException {
        AdbTool.initializeBridge();
        Scanner scanner = new Scanner(System.in);
        IDevice device = AdbTool.getDefaultDevice();
        File dir = new File("apks/Wandoujia");
        BufferedWriter bw = new BufferedWriter(new FileWriter("record.txt"));
        for(File apk : dir.listFiles(new FileFilter() {
            @Override
            public boolean accept(File pathname) {
                return pathname.getName().endsWith(".apk");
            }
        })){
            System.out.println(apk.getName());
            String packageName = AdbTool.getPackageFromApk(apk.getPath());
            String startActivity = AdbTool.getLaunchableAcvivity(apk.getPath());
            AdbTool.installApk(device.getSerialNumber(), apk.getPath());
            AdbTool.startActivity(device, startActivity);
            Thread.sleep(3000);
            AdbTool.screenCapture(device, dir.getPath() + "/" + apk.getName().replace(".apk", ".png"));
            bw.write(apk.getName());
            bw.newLine();
            bw.flush();
            AdbTool.unInstallApk(device.getSerialNumber(), packageName);
        }

        bw.close();
        scanner.close();
        AdbTool.terminateBridge();
    }
}
