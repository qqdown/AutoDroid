package edu.nju.autodroid.uiautomator;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import com.android.uiautomator.core.UiDevice;
import com.android.uiautomator.core.UiObject;
import com.android.uiautomator.core.UiObjectNotFoundException;
import com.android.uiautomator.core.UiScrollable;
import com.android.uiautomator.core.UiSelector;

import android.os.Environment;

public class CommandHandler {
    public static Command Handle(Command cmd){
        Command backCmd = new Command();
        backCmd.cmd = cmd.cmd;
        switch(backCmd.cmd){
            case Command.cmdPressHome:
                backCmd.params = new String[1];
                backCmd.params[0] = Boolean.toString(pressHome());
                break;
            case Command.cmdPressBack:
                backCmd.params = new String[1];
                backCmd.params[0] = Boolean.toString(pressBack());
                break;

            case Command.cmdGetLayout:
                backCmd.params = new String[1];
                backCmd.params[0] = getLayout();
                break;
            case Command.cmdGetActivity:
                backCmd.params = new String[1];
                backCmd.params[0] = getActivity();
                break;
            case Command.cmdGetPackage:
                backCmd.params = new String[1];
                backCmd.params[0] = getPackage();
                break;

            case Command.cmdDoClick:
                backCmd.params = new String[1];
                backCmd.params[0] = doClick(cmd.params[0]).toString();
                break;
            case Command.cmdDoSetText:
                backCmd.params = new String[1];
                backCmd.params[0] = doSetText(cmd.params[0], cmd.params[1]).toString();
                break;
            case Command.cmdDoLongClick:
                backCmd.params = new String[1];
                backCmd.params[0] = doLongClick(cmd.params[0]).toString();
                break;
            case Command.cmdDoClickAndWaitForNewWindow:
                backCmd.params = new String[1];
                backCmd.params[0] = doClickAndWaitForNewWindow(cmd.params[0], Long.parseLong(cmd.params[1])).toString();
                break;

            case Command.cmdDoScrollBackward:
                backCmd.params = new String[1];
                backCmd.params[0] = doScrollBackBackward(cmd.params[0], Integer.parseInt(cmd.params[1])).toString();
                break;
            case Command.cmdDoScrollForward:
                backCmd.params = new String[1];
                backCmd.params[0] = doScrollForward(cmd.params[0], Integer.parseInt(cmd.params[1])).toString();
                break;
            case Command.cmdDoScrollToEnd:
                backCmd.params = new String[1];
                backCmd.params[0] = doScrollToEnd(cmd.params[0], Integer.parseInt(cmd.params[1]), Integer.parseInt(cmd.params[2])).toString();
                break;
            case Command.cmdDoScrollToBeginning:
                backCmd.params = new String[1];
                backCmd.params[0] = doScrollToBeginning(cmd.params[0], Integer.parseInt(cmd.params[1]), Integer.parseInt(cmd.params[2])).toString();
                break;
            case Command.cmdDoScrollIntoView:
                backCmd.params = new String[1];
                backCmd.params[0] = doScrollIntoView(cmd.params[0], cmd.params[1]).toString();
                break;

            default:
                backCmd.cmd = Command.cmdUnknown;
                break;
        }

        return backCmd;
    }

    //0x0001
    private static Boolean pressHome(){
        return UiDevice.getInstance().pressHome();
    }

    private static Boolean pressBack(){
        return UiDevice.getInstance().pressBack();
    }

    //0x1001
    private static String getLayout(){

        //simulator-local/tmp/dump.xml    real device-/local/tmp/local/tmp/dump.xml
        File dumpFile = new File(Environment.getDataDirectory().getAbsolutePath() + "/local/tmp/dump.xml");
        //System.out.println("dumpFile " + dumpFile.getAbsolutePath() + " | " + dumpFile.getName());
        if(!dumpFile.getParentFile().exists())
            dumpFile.getParentFile().mkdirs();
        if(!dumpFile.exists())
            try {
                dumpFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }

        String layoutStr = null;
        try {
            UiDevice.getInstance().dumpWindowHierarchy(dumpFile.getName());
            Long filelength = dumpFile.length();     //鑾峰彇鏂囦欢闀垮害
            byte[] filecontent = new byte[filelength.intValue()];
            FileInputStream fis = new FileInputStream(dumpFile);
            fis.read(filecontent);
            fis.close();
            layoutStr = new String(filecontent);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return layoutStr;
    }

    //0x1002
    @Deprecated
    private static String getActivity(){
        return UiDevice.getInstance().getCurrentActivityName();
    }

    //0x1003
    private static String getPackage(){
        return UiDevice.getInstance().getCurrentPackageName();
    }

    //0x2001
    private static Boolean doClick(String btnPath){
        long milli = System.currentTimeMillis();
        UiObject btn = getObject(btnPath);
        System.out.println("getObject " + (System.currentTimeMillis()-milli)/1000.0);
        if(btn == null)
            return false;
        try {
            milli = System.currentTimeMillis();
            boolean res = btn.click();
            System.out.println("click " + (System.currentTimeMillis()-milli)/1000.0);
            return res;

        } catch (UiObjectNotFoundException e) {
            e.printStackTrace();
        }
        return false;
    }

    //0x2002
    private static Boolean doSetText(String xPath, String content){
        UiObject obj = getObject(xPath);
        if(obj == null)
            return false;
        try {
            return obj.setText(content);
        } catch (UiObjectNotFoundException e) {
            e.printStackTrace();
        }
        return false;
    }

    private static Boolean doLongClick(String xPath){
        UiObject obj = getObject(xPath);
        if(obj == null)
            return false;
        try {
            return obj.longClick();
        } catch (UiObjectNotFoundException e) {
            e.printStackTrace();
        }
        return false;
    }

    private static Boolean doClickAndWaitForNewWindow(String xPath, long timeout){
        UiObject obj = getObject(xPath);
        if(obj == null)
            return false;
        try {
            return obj.clickAndWaitForNewWindow(timeout);
        } catch (UiObjectNotFoundException e) {
            e.printStackTrace();
        }
        return false;
    }

    //steps every step5ms default 55
    private static Boolean doScrollBackBackward(String xPath, int steps){
        UiObject obj = getObject(xPath);
        if(obj == null)
            return false;
        try {
            UiScrollable scroll = new UiScrollable(obj.getSelector());
            return scroll.scrollBackward(steps);
        } catch (UiObjectNotFoundException e) {
            e.printStackTrace();
        }
        return false;
    }

    private static Boolean doScrollForward(String xPath, int steps){
        UiObject obj = getObject(xPath);
        if(obj == null)
            return false;
        try {
            UiScrollable scroll = new UiScrollable(obj.getSelector());
            return scroll.scrollForward(steps);
        } catch (UiObjectNotFoundException e) {
            e.printStackTrace();
        }
        return false;
    }

    private static Boolean doScrollToEnd(String xPath, int maxSwipes, int steps){
        UiObject obj = getObject(xPath);
        if(obj == null)
            return false;
        try {
            UiScrollable scroll = new UiScrollable(obj.getSelector());
            return scroll.scrollToEnd(maxSwipes, steps);
        } catch (UiObjectNotFoundException e) {
            e.printStackTrace();
        }
        return false;
    }

    private static Boolean doScrollToBeginning(String xPath, int maxSwipes, int steps){
        UiObject obj = getObject(xPath);
        if(obj == null)
            return false;
        try {
            UiScrollable scroll = new UiScrollable(obj.getSelector());
            return scroll.scrollToBeginning(maxSwipes, steps);
        } catch (UiObjectNotFoundException e) {
            e.printStackTrace();
        }
        return false;
    }

    private static Boolean doScrollIntoView(String xPath, String objXPath){
        UiObject o = getObject(xPath);
        UiObject obj = getObject(objXPath);
        if(o == null || obj == null)
            return false;
        try {
            UiScrollable scroll = new UiScrollable(o.getSelector());
            return scroll.scrollIntoView(obj);
        } catch (UiObjectNotFoundException e) {
            e.printStackTrace();
        }
        return false;
    }

    public static UiObject getObject(String indexXPath){
        String[] indexStrs = indexXPath.split(" ");
        int[] indexes = new int[indexStrs.length];
        for(int i=0; i<indexes.length; i++){
            indexes[i] = Integer.parseInt(indexStrs[i]);
        }
        UiObject obj = new UiObject(new UiSelector().index(indexes[0]));
        for(int i=1; i<indexes.length; i++){
            try {
                obj = obj.getChild(new UiSelector().index(indexes[i]));
            } catch (UiObjectNotFoundException e) {
                e.printStackTrace();
                return null;
            }
        }
        if(!obj.exists())
            return null;
        return obj;
    }
}