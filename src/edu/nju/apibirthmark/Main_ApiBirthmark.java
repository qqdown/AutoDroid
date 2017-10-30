package edu.nju.apibirthmark;

import com.android.ddmlib.*;
import edu.nju.autodroid.main.Main;
import edu.nju.autodroid.utils.AdbTool;
import edu.nju.autodroid.utils.CmdExecutor;
import edu.nju.autodroid.utils.Configuration;
import edu.nju.autodroid.utils.Logger;

import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 * Created by ysht on 2016/11/7.
 */
public class Main_ApiBirthmark {
    public static void main(String[] args) throws TimeoutException, AdbCommandRejectedException, ShellCommandUnresponsiveException, IOException, InterruptedException {
        DdmPreferences.setTimeOut(10000);
        AdbTool.initializeBridge();
        List<String> apkFileList = Main.getApkFileList("E:\\APKs\\Wandoujia\\合集");
        Logger.logInfo("共获得APK文件：" + apkFileList.size() + "个");

        IDevice device = AdbTool.getDefaultDevice();//使用默认的device


        for(String apkFilePath : apkFileList) {

            List<String> finishedList = Main.getFinishedList("finishedList.txt");
            if(finishedList.contains(apkFilePath))
                continue;
            if(getMinSdk(new File(apkFilePath)) > 10)
            {
                finishedList.add(apkFilePath);
                Main.setFinishedList("finishedList.txt", finishedList);
                continue;
            }
            boolean result = true;
            String packageName = AdbTool.getPackageFromApk(apkFilePath);
            if(!AdbTool.hasInstalledPackage(device, packageName))
            {
                result = AdbTool.installApk(device.getSerialNumber(), apkFilePath);
                Logger.logInfo("安装apk："+result);
            }
            File apkFile = new File(apkFilePath);
            String apkName = apkFile.getName().substring(0, apkFile.getName().lastIndexOf('.'));
            if(result) {
                String laubchableActivity = AdbTool.getLaunchableAcvivity(apkFilePath);
                if(!laubchableActivity.endsWith("/")) {
                    AdbTool.startActivity(device, laubchableActivity);
                    Thread.sleep(1000);
                    int pid = -1;
                    while ((pid = getTaskID(device, packageName)) == -1) {
                        Thread.sleep(1000);//等待启动
                    }
                    System.out.println(pid);
                    Thread.sleep(1000);
                    CmdExecutor.execCmd("adb shell am profile " + pid + " start /mnt/sdcard/bmark.trace");
                    Thread.sleep(1000);
                    CmdExecutor.execCmd("adb shell monkey -p " + packageName + " -s 100 --ignore-crashes --ignore-security-exceptions 500");
                    Thread.sleep(1000);
                    CmdExecutor.execCmd("adb shell am profile " + pid + " stop");
                    Thread.sleep(1000);
                    File outputDir = new File("outputTrace/" + apkName);
                    if (!outputDir.exists())
                        outputDir.mkdirs();
                    CmdExecutor.execCmd("adb pull /mnt/sdcard/bmark.trace " + outputDir.getAbsolutePath());
                }else{
                    Logger.logInfo("无法获取Launchable Activity");
                }
            }

            AdbTool.unInstallApk(device.getSerialNumber(), packageName);
            finishedList.add(apkFilePath);
            Main.setFinishedList("finishedList.txt", finishedList);
        }
        AdbTool.terminateBridge();
    }

    private static int getTaskID(IDevice device, String packageName){
        String ps = CmdExecutor.execCmd("adb shell ps");
        String[] lines = ps.split("\n");
        for(String line : lines)
        {
            if(line.contains(packageName)){
                String[] datas = line.split("\\s+");
                return Integer.parseInt(datas[1]);
            }
        }
        return -1;
    }
    private static int getMinSdk(File apkFile){
        String version = null;
        String aaptResult = CmdExecutor.execCmd(Configuration.getAaptPath()+" dump badging \"" +apkFile.getAbsolutePath()+"\""  );
        String[] lines = aaptResult.split("\n");
        for(String line : lines){
            if(line.contains("minSdkVersion:")){
                int index = line.indexOf("minSdkVersion:'");
                version = line.substring(index+15);
                version = version.substring(0, version.indexOf("'"));
                return Integer.parseInt(version);
            }
            else if(line.contains("sdkVersion:'")){
                int index = line.indexOf("sdkVersion:'");
                version = line.substring(index+12);
                version = version.substring(0, version.indexOf("'"));
                return Integer.parseInt(version);
            }
        }
        return -1;
    }
}
