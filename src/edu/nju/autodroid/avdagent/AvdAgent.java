package edu.nju.autodroid.avdagent;


import edu.nju.autodroid.utils.*;

import java.io.File;
import java.util.Date;

/**
 * Created by ysht on 2016/4/19 0019.
 */
public class AvdAgent implements IAvdAgent {

    protected static AvdAgent avdAgent;

    protected final String android = Configuration.getAndroidPath();

    static {
        avdAgent = new AvdAgent();
    }

    protected AvdAgent(){}

    @Override
    public boolean createAvd(AvdTarget target, AvdDevice device, String avdName) {
        Logger.logInfo("start create Avd " + avdName);
        String cmd;
        cmd = String.format("%s create avd -t %d -n %s -b %s -d %d -f", android, target.id, avdName, target.ABIList.get(target.ABIList.size()-1), device.id);
        String result = CmdExecutor.execCmd(cmd);
        Logger.logInfo("finish create Avd " + avdName);
        if(!result.contains(String.format("Created AVD '%s'", avdName)))
            return false;

        //替换userdata-qemu.img
        /*
        File myUserDataImg = new File("tools/avd/userdata-qemu.img");
        if(!myUserDataImg.exists()){
            Logger.logError("createAvd: userdataImg doesn't exist!");
        }
        else{
            String destUserDataImgPath = Configuration.toLegalPath(Configuration.getDotAndroidPath()+"/avd/"+avdName+".avd/userdata-qemu.img");
            File destFile = new File(destUserDataImgPath);
            if(!destFile.exists()){
                Logger.logError("createAvd: There should be a userdataimg file: " + destFile.getAbsolutePath());
            }
            else{
                destFile.delete();
            }
            Utils.copyFile(myUserDataImg, destFile);
        }*/
        return true;
    }

    @Override
    public boolean cleanAvdImg(String avdName) {
        File myUserDataImg = new File("tools/avd/userdata-qemu.img");
        if(!myUserDataImg.exists()){
            Logger.logError("createAvd: userdataImg doesn't exist!");
            return false;
        }
        else{
            String destUserDataImgPath = Configuration.toLegalPath(Configuration.getDotAndroidPath()+"/avd/"+avdName+".avd/userdata-qemu.img");
            File destFile = new File(destUserDataImgPath);
            if(!destFile.exists()){
                Logger.logError("createAvd: There should be a userdataimg file: " + destFile.getAbsolutePath());
            }
            else{
                destFile.delete();
            }
            Utils.copyFile(myUserDataImg, destFile);
            return true;
        }
    }

    @Override
    public boolean deleteAvd(String avdName) {
        String result = CmdExecutor.execCmd(android + " delete avd -n " + avdName);
        if(result.contains("AVD '" + avdName + "' deleted"))
        {
            Logger.logInfo("Delete avd " + avdName + " successfully");
            return true;
        }
        else {
            Logger.logError("Delete avd " + avdName + " failed\n" + result);
            return false;
        }
    }

    @Override
    public boolean startAvd(String avdName) {
        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                File oldFile = new File(Configuration.getDotAndroidPath()+"\\avd\\" + avdName+".avd\\userdata.img");
                File newFile = new File("D:\\avd\\" + avdName+".avd\\userdata_"+new Date().getTime()+".img");
                File oldsdcardFile = new File(Configuration.getDotAndroidPath()+"\\avd\\" + avdName+".avd\\sdcard.img");
                File newsdcardFile = new File("D:\\avd\\" + avdName+".avd\\sdcard"+new Date().getTime()+".img");
                Utils.copyFile( oldFile, newFile);
                Utils.copyFile(oldsdcardFile, newsdcardFile);
                CmdExecutor.execCmd("emulator -avd " + avdName + " -data " + newFile.getAbsolutePath() + " -sdcard " + newsdcardFile.getAbsolutePath(), 60);
            }
        });
        t.start();
        boolean startSuccess = waitForAvdOnline(avdName, 60);
        if(startSuccess)
            Logger.logInfo("startAvd: Start avd successfully!");
        else
            Logger.logInfo("startAvd: Start avd unsuccessfully!");

        return startSuccess;
    }

    private boolean waitForAvdOnline(String avdName, int maxWaitSeconds){
        Date startTime = new Date();
        while(new Date().getTime() - startTime.getTime() <= maxWaitSeconds*1000){
            if(AdbTool.getIDeviceByAvd(avdName) != null && AdbTool.getIDeviceByAvd(avdName).isOnline())
                return true;
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    public static AvdAgent Get(){
        return avdAgent;
    }

}
