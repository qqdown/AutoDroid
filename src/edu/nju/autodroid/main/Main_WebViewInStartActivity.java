package edu.nju.autodroid.main;

import com.android.ddmlib.*;
import edu.nju.autodroid.androidagent.AdbAgent;
import edu.nju.autodroid.androidagent.IAndroidAgent;
import edu.nju.autodroid.hierarchyHelper.LayoutNode;
import edu.nju.autodroid.hierarchyHelper.LayoutTree;
import edu.nju.autodroid.hierarchyHelper.TreeSearchOrder;
import edu.nju.autodroid.strategy.DepthGroupWeightedStrategy;
import edu.nju.autodroid.strategy.IStrategy;
import edu.nju.autodroid.uiautomator.UiautomatorClient;
import edu.nju.autodroid.utils.AdbTool;
import edu.nju.autodroid.utils.Configuration;
import edu.nju.autodroid.utils.Logger;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.Exchanger;
import java.util.function.Predicate;

/**
 * Created by ysht on 2017/10/25.
 */
public class Main_WebViewInStartActivity {
    public static void main(String[] args) throws TimeoutException, AdbCommandRejectedException, ShellCommandUnresponsiveException, IOException, InterruptedException {
        if(args.length != 1 ){
            System.out.println("Usage: java -jar AutoDroid.jar <APK-Folder-path>");
            return;
        }

        DdmPreferences.setTimeOut(10000);
        AdbTool.initializeBridge();

        List<String> apkFileList = Main.getApkFileList(args[0]);
        Logger.logInfo("Total Apk counts：" + apkFileList.size());

        IDevice device = AdbTool.getDefaultDevice();//使用默认的device

        BufferedWriter bw = new BufferedWriter(new FileWriter("WebViewCount.txt"));
        for(String apkFilePath : apkFileList){
            List<String> finishedList = Main.getFinishedList("finishedList.txt");
            if(finishedList.contains(apkFilePath))
                continue;
            int uiautomatorTaskId = AdbTool.getTaskId(device, "uiautomator");
            if(uiautomatorTaskId > 0)
                AdbTool.killTask(device, uiautomatorTaskId);
            Thread.sleep(2000);
            if(AdbTool.getTaskId(device, "uiautomator") < 0) {//如果uiautomator没有启动
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        UiautomatorClient.start(device.getSerialNumber(), UiautomatorClient.PHONE_PORT);
                    }
                }).start();
            }
            while (AdbTool.getTaskId(device, "uiautomator") < 0){//等待uiautomator
                Logger.logInfo("Waiting for Uiautomator...");
                Thread.sleep(1000);
            }
            Logger.logInfo("UiAutomator start successfully!");



            File apkFile = new File(apkFilePath);
            IAndroidAgent agent = new AdbAgent(device, UiautomatorClient.PHONE_PORT, UiautomatorClient.PHONE_PORT);
            boolean result;
            result = agent.init();
            Logger.logInfo("Init agent："+result);
            String packageName = AdbTool.getPackageFromApk(apkFilePath);
            // if(!AdbTool.hasInstalledPackage(agent.getDevice(), packageName))
            {
                result = AdbTool.installApk(agent.getDevice().getSerialNumber(), apkFilePath);
                Logger.logInfo("Install apk："+result);
            }

            if(result){
                String laubchableActivity = AdbTool.getLaunchableAcvivity(apkFilePath);

                if(!laubchableActivity.endsWith("/")) {
                    String apkName = apkFile.getName().substring(0, apkFile.getName().lastIndexOf('.'));
                    agent.startActivity(laubchableActivity);
                    waitForStart(agent, laubchableActivity);
                    skipWelcome(agent);

                    LayoutTree lt = getCurrentLayout(agent);
                    int totalCount = lt.getTreeSize();
                    int webViewCount = lt.findAll(new Predicate<LayoutNode>() {
                        @Override
                        public boolean test(LayoutNode node) {
                            return node.className.contains(".WebView");
                        }
                    }, TreeSearchOrder.BoardFirst).size();

                    bw.write(apkFile.getName() + " " + webViewCount + " " + totalCount);
                    bw.newLine();
                }else{
                    Logger.logInfo("Can not get Launchable Activity");
                }

                AdbTool.unInstallApk(agent.getDevice().getSerialNumber(), packageName);
            }

            agent.terminate();
            finishedList.add(apkFilePath);
            Main.setFinishedList("finishedList.txt", finishedList);

            uiautomatorTaskId = AdbTool.getTaskId(device, "uiautomator");
            if(uiautomatorTaskId > 0)
                AdbTool.killTask(device, uiautomatorTaskId);
            Thread.sleep(2000);
        }
        bw.close();
        AdbTool.terminateBridge();
        Logger.endLogging();

    }

    static void waitForStart(IAndroidAgent androidAgent, String startActivity){
        try {
            String packageName = androidAgent.getRuntimePackage();
            int packageCount = 10;
            while((packageName == null || !startActivity.contains(packageName)) && packageCount-->0){
                androidAgent.startActivity(startActivity);
                Thread.sleep(1000);
                Logger.logInfo(androidAgent.getDevice().getSerialNumber() + " Wait for activity start");
                packageName = androidAgent.getRuntimePackage();
            }
            if(packageName == null)
                return;
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        catch (NullPointerException e){
            e.printStackTrace();
            return;
        }
    }

    static void skipWelcome(IAndroidAgent androidAgent){
        try {
            int scrollCount = 5;
            while (scrollCount-- > 0)//尝试右滑scrollCount次
            {
                int[] windowSize = getCurrentLayout(androidAgent).getScreenSize();
                AdbTool.doSwipe(androidAgent.getDevice(), windowSize[0], windowSize[1] / 2, 0, windowSize[1] / 2);
                Thread.sleep(1000);
            }
        }
        catch (Exception ex){
            return;
        }
    }

    static LayoutTree getCurrentLayout(IAndroidAgent androidAgent){
        String layoutXML = androidAgent.getLayout();
        LayoutTree lt = null;
        while(lt == null) {
            while (layoutXML == null || layoutXML.isEmpty()) {
                Logger.logInfo(androidAgent.getDevice().getSerialNumber() + " Waiting for layout。。。");
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                layoutXML = androidAgent.getLayout();
            }

            lt = new LayoutTree(layoutXML);
        }
        return lt;
    }
}
