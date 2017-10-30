package edu.nju.autodroid.main;

import com.android.ddmlib.*;
import com.sun.org.apache.xpath.internal.operations.Bool;
import edu.nju.autodroid.androidagent.AdbAgent;
import edu.nju.autodroid.androidagent.IAndroidAgent;
import edu.nju.autodroid.avdagent.*;
import edu.nju.autodroid.strategy.*;
import edu.nju.autodroid.uiautomator.UiautomatorClient;
import edu.nju.autodroid.utils.*;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by ysht on 2016/3/7 0007.
 */
public class Main {
    private static Boolean isStartUiAutomator = false;
    private static HashMap<IDevice, Boolean> startedMap = new HashMap<IDevice, Boolean>();
    private static IDevice[] deviceArray;
    public static void main(String[] args) throws TimeoutException, AdbCommandRejectedException, ShellCommandUnresponsiveException, IOException, InterruptedException {
        if(args.length != 2 ){
            System.out.println("Usage: java -jar AutoDroid.jar <Mode> <APK-Folder-path>");
            return;
        }
        int mode = Integer.parseInt(args[0]);
        if(mode == 1)
        {
            Main_Single.main(args);
            return;
        }
        Logger.initalize("log.txt");
        DdmPreferences.setTimeOut(10000);
        AdbTool.initializeBridge();

        List<String> apkFileList = getApkFileList(args[1]);//getApkFileList("E:\\APKs\\Wandoujia\\合集");

        Logger.logInfo("Total Apk counts：" + apkFileList.size());
        //生成设备
        int deviceCount = Configuration.getParallelCount();
        Logger.logInfo("Run in parallel count: " + deviceCount);
        int[] portArray = new int[deviceCount];
        for(int i=0; i<portArray.length; i++){
            portArray[i] = 22222+i;
        }

        deviceArray = new IDevice[deviceCount];
        List<String> finishedList = getFinishedList("finishedList.txt");
        for(String apkFilePath : apkFileList){

            if(finishedList.contains(apkFilePath))
                continue;

            int tempBeforeCount = AdbTool.getDevices().size();
            IAvdAgent avdAgent = AvdAgent.Get();
            avdAgent.startAvd("AutoDroidAvd");
            Logger.logInfo("Creating new AVD instance...");
            while(AdbTool.getDevices().size() == tempBeforeCount){
                Logger.logInfo("Waiting for AVD start...");
                Thread.sleep(1000);
            }
            List<IDevice> deviceList = AdbTool.getDevices();
            for(IDevice d : deviceList){
                boolean containsDevice = false;
                for(int i=0; i<deviceArray.length; i++){
                    if(deviceArray[i] != null && deviceArray[i].equals(d)){
                        containsDevice = true;
                        break;
                    }
                }
                if(!containsDevice)
                {
                    int[] port = new int[1];
                    port[0] = -1;
                    for(int i=0; i<deviceCount; i++){
                        if(deviceArray[i]== null){
                            port[0] = portArray[i];
                            deviceArray[i] = d;
                            startedMap.put(d, false);
                            break;
                        }
                    }
                    if(port[0] < 0){
                        Logger.logError("Can't get available port！");
                    }
                    while(!d.isOnline()){
                        Logger.logInfo("Waiting for "+d.getName()+" online...");
                        Thread.sleep(1000);
                    }
                    new Thread(new Runnable() {
                        @Override
                        public void run() {

                            try {
                                runAutoDroid(d, apkFilePath, finishedList, port[0]);
                            } catch (TimeoutException e) {
                                e.printStackTrace();
                            } catch (AdbCommandRejectedException e) {
                                e.printStackTrace();
                            } catch (ShellCommandUnresponsiveException e) {
                                e.printStackTrace();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    }).start();
                    break;
                }
            }
            Thread.sleep(1000);
            while(true){
                boolean hasStopedDevice = false;
                for(int i=0; i<deviceArray.length; i++){
                    IDevice d = deviceArray[i];
                    if(d == null || (startedMap.get(d) && !d.isOnline())){
                        deviceArray[i] = null;
                        hasStopedDevice = true;
                        break;
                    }
                }
                if(hasStopedDevice)
                    break;
                else
                    Thread.sleep(1000);
            }
            Thread.sleep(1000);
        }

        AdbTool.terminateBridge();
        Logger.endLogging();
    }

    private static void runAutoDroid(IDevice device, String apkFilePath, List<String> finishedList, int port) throws TimeoutException, AdbCommandRejectedException, ShellCommandUnresponsiveException, IOException {

        while(isStartUiAutomator){
            try {
                Logger.logInfo(device.getName() +"Waiting for Uiautomator in other devices...");
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        synchronized(isStartUiAutomator){
            isStartUiAutomator = true;
        }

        new Thread(new Runnable() {
            @Override
            public void run() {
                Logger.logInfo(device.getName() + " Starting UiAutomator...");
                //通过文件操作，将UiAutomatorClient的端口号
                try {
                    File f = new File("src/edu/nju/autodroid/uiautomator/UiautomatorClient.java");
                    BufferedReader br = new BufferedReader(new FileReader(f));
                    String content = "";
                    String line;
                    while((line=br.readLine()) != null){
                        if(line.contains("public static int PHONE_PORT ="))
                            content += "public static int PHONE_PORT = "+port+";//this will be changed by file io" + "\n";
                        else
                            content += line + "\n";
                    }
                    br.close();
                    BufferedWriter bw = new BufferedWriter(new FileWriter(f));
                    bw.write(content);
                    bw.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                UiautomatorClient.start(device.getSerialNumber(), port);
            }
        }).start();

        while(AdbTool.getTaskId(device, "uiautomator") < 0)//wiat for uiautomator
        {
            try {
                Logger.logInfo(device.getName() + "Waiting for Uiautomator");
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        synchronized(isStartUiAutomator){
            isStartUiAutomator = false;
        }
        Logger.logInfo(device.getName() + " UiAutomator start successfully！");

        File apkFile = new File(apkFilePath);

        IAndroidAgent agent = new AdbAgent(device, port,port);
        boolean result;
        result = agent.init();
        Logger.logInfo("Init agent："+result);
        String packageName = AdbTool.getPackageFromApk(apkFilePath);
        if(!AdbTool.hasInstalledPackage(agent.getDevice(), packageName))
        {
            result = AdbTool.installApk(agent.getDevice().getSerialNumber(), apkFilePath);
            Logger.logInfo("Inistalling apk："+result);
        }

        if(result){
            String laubchableActivity = AdbTool.getLaunchableAcvivity(apkFilePath);

            if(!laubchableActivity.endsWith("/")) {
                String apkName = apkFile.getName().substring(0, apkFile.getName().lastIndexOf('.'));
                IStrategy strategy = new DepthGroupWeightedStrategy(agent, Configuration.getMaxStep(), laubchableActivity, new Logger(apkName, "logger_output\\" + apkName + ".txt"));//"com.financial.calculator/.FinancialCalculators"
                Logger.logInfo("Start strategy：" + strategy.getStrategyName());
                Logger.logInfo("Strategy target：" + apkFilePath);
                startedMap.put(agent.getDevice(), true);
                try{
                    if (strategy.run()) {
                        Logger.logInfo("Strategy finished successfully！");
                    } else {
                        Logger.logInfo("Strategy finished with errors！");
                    }
                    strategy.writeToFile("strategy_output\\" + apkName);
                }
                catch (Exception e){
                    Logger.logException("Strategy can't finish！");
                    e.printStackTrace();
                }
            }else{
                Logger.logError("Can not get Launchable Activity");
            }

        }

        agent.terminate();
        finishedList.add(apkFilePath);
        setFinishedList("finishedList.txt", finishedList);
        Logger.logInfo("Stopping device...");
        if(device != null)
        {
            try {
                AdbTool.stopDevice(device);
                Thread.sleep(5000);
            } catch (Exception e) {
                e.printStackTrace();
            }
            String windowTitle = device.getSerialNumber().substring(device.getSerialNumber().indexOf('-')+1)+":AutoDroidAvd";
            CmdExecutor.execCmd("taskkill /f /fi \"WINDOWTITLE eq " + windowTitle + "\"");
            for(int i=0; i<deviceArray.length; i++){
                if(deviceArray[i] != null && deviceArray[i].equals(device)){
                    deviceArray[i] = null;
                    break;
                }
            }
        }
    }

    public static List<String> getApkFileList(String directoryName){
        List<String> apkFileList = new ArrayList<String>();
        File directory = new File(directoryName);
        if(directory.exists()){
            for(File f : directory.listFiles()){
                if(f.isDirectory()){
                    apkFileList.addAll(getApkFileList(f.getAbsolutePath()));
                }
                else{
                    if(f.getName().endsWith(".apk")){
                        apkFileList.add(f.getAbsolutePath());
                    }
                }
            }
        }
        return  apkFileList;
    }

    public static  List<String> getFinishedList(String fileName){
        List<String> filePathList = new ArrayList<String>();
        if(!new File(fileName).exists()){
            return  filePathList;
        }
        try {
            FileReader fr = new FileReader(fileName);
            BufferedReader br = new BufferedReader(fr);
            String line;
            while((line=br.readLine()) != null){
                filePathList.add(line);
            }
            br.close();
            fr.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return filePathList;
    }

    public static void setFinishedList(String fileNameToSave, List<String> filePathList){
        try{
            FileWriter fw = new FileWriter(fileNameToSave);
            BufferedWriter bw = new BufferedWriter(fw);
            for(String filePath : filePathList){
                bw.write(filePath);
                bw.newLine();
            }
            bw.close();
            fw.close();
        }catch (IOException e){

        }
    }
}
