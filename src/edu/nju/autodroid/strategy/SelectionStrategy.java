package edu.nju.autodroid.strategy;

import android.view.InputQueue;
import com.android.ddmlib.AdbCommandRejectedException;
import com.android.ddmlib.ShellCommandUnresponsiveException;
import com.android.ddmlib.TimeoutException;
import edu.nju.autodroid.androidagent.IAndroidAgent;
import edu.nju.autodroid.hierarchyHelper.Action;
import edu.nju.autodroid.hierarchyHelper.AndroidWindow;
import edu.nju.autodroid.hierarchyHelper.LayoutTree;
import edu.nju.autodroid.windowtransaction.IWindow;
import edu.nju.autodroid.windowtransaction.SimpleWindow;
import edu.nju.autodroid.utils.AdbTool;
import edu.nju.autodroid.utils.Logger;
import edu.nju.autodroid.windowtransaction.IWindowTransaction;
import edu.nju.autodroid.windowtransaction.SimpleWindowTransaction;

import java.io.IOException;
import java.util.List;

/**
 * Created by ysht on 2016/3/7 0007.
 */
public abstract class SelectionStrategy<TWindow extends IWindow> implements IStrategy {
    protected String runtimePackage;
    protected IAndroidAgent androidAgent;
    private Action lastAction = null;
    private IWindowTransaction<TWindow> windowTransaction = null;
    protected int maxSteps;
    protected String startActivity;
    private int currentSteps;
    private Logger actionLogger = null;

    public SelectionStrategy(IAndroidAgent androidAgent, int maxSteps, String startActivity, Logger actionLogger){
        this.androidAgent = androidAgent;
        this.maxSteps = maxSteps;
        this.startActivity = startActivity;
        currentSteps = 0;
        this.actionLogger = actionLogger;
    }

    @Override
    public boolean run() {
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
                return false;
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        catch (NullPointerException e){
            e.printStackTrace();
            return false;
        }

        //获取当前运行package
        runtimePackage = androidAgent.getRuntimePackage();
        windowTransaction = createWindowTransactionInstance();
       //
        try{
            TWindow currentWindow = getCurrentWindow(windowTransaction);

            int scrollCount = 5;
            while(scrollCount-->0)//尝试右滑scrollCount次
            {
                int[] windowSize = currentWindow.getLayout().getScreenSize();
                AdbTool.doSwipe(androidAgent.getDevice(), windowSize[0], windowSize[1]/2, 0, windowSize[1]/2);
                Thread.sleep(1000);
            }

            while(currentWindow != null && currentSteps <= maxSteps){

                Action action = getNextAction(currentWindow, lastAction);
                if(action == null)
                    break;
                currentSteps++;
                switch (action.actionType){
                    case NoAction:
                        break;

                    case NoMoreAction:
                        Logger.logInfo("已经无更多操作！");
                        return true;

                    default:
                        doAction(action, currentWindow);
                        TWindow tempWindow = currentWindow;
                        currentWindow = getCurrentWindow(windowTransaction);
                        if(currentWindow == null)
                        {
                            Logger.logInfo(androidAgent.getDevice().getSerialNumber() + " 无法获取当前窗口！");
                            return false;
                        }
                        windowTransaction.addTransaction(tempWindow.getId(), currentWindow.getId(), action);
                        if(action.actionNode != null)
                            actionLogger.info(currentSteps + "\t" + tempWindow.getId()+"\t" + currentWindow.getId()+"\t"+ action.actionType.name()+"\t" + action.actionNode.className + "\t" + action.actionNode.indexXpath.replace(' ',';') + "\t" + getDumpedWindowString()+"\t" + androidAgent.getLayout());
                        else
                            actionLogger.info(currentSteps + "\t" + tempWindow.getId()+"\t" + currentWindow.getId()+"\t"+ action.actionType.name()+ "\tnull\tnull\t" + getDumpedWindowString()+"\t" + androidAgent.getLayout());
                        //当程序已经跳出当前package，且无法恢复，那么策略结束，且返回false表示未正常结束
                        if((currentWindow = tryStayingCurrentApplication(currentWindow)) == null)
                        {
                            Logger.logInfo(androidAgent.getDevice().getSerialNumber() + " 已经跳出当前package");
                            return false;
                        }
                        afterAction(action, tempWindow, currentWindow, windowTransaction);
                        break;
                }
                lastAction = action;
            }
        }
        catch (IOException e){
            Logger.logException(androidAgent.getDevice().getSerialNumber() + e.getMessage());
            return false;
        } catch (AdbCommandRejectedException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ShellCommandUnresponsiveException e) {
            e.printStackTrace();
        } catch (TimeoutException e) {
            e.printStackTrace();
        }

        return true;
    }

    private String getDumpedWindowString(){
        String str = "";
        List<AndroidWindow> androidWindowList = androidAgent.getAndroidWindows();
        while(androidWindowList == null){
            try {
                Logger.logInfo(androidAgent.getDevice().getSerialNumber() + " Wait for androidWindowList!");
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            androidWindowList = androidAgent.getAndroidWindows();
        }
        for(AndroidWindow aw : androidWindowList){
            str += aw.id+":"+aw.activityName+":"+aw.session+";";
        }
        return str.replace(' ','-');
    }


    @Override
    public String getRuntimePackageName() {
        return runtimePackage;
    }

    @Override
    public void writeToFile(String fileName) {
        windowTransaction.writeToFile(fileName);
     }

    /**
     * 获取下一个可能的动作
     * @param curWindow 当前窗口
     * @param lastAction 上一个动作
     * @return
     */
    public abstract Action getNextAction(TWindow curWindow, Action lastAction);

    /**
     * 但完成一个action之后，要进行的操作
     * 只有在有action之后才会被调用
     * @param actionWindow
     * @param afterActionWindow
     * @param windowTransaction
     * @return
     */
    public abstract void afterAction(Action action, TWindow actionWindow, TWindow afterActionWindow, IWindowTransaction<TWindow> windowTransaction);

    //获取当前Window
    public abstract TWindow getCurrentWindow(IWindowTransaction<TWindow> windowTransaction);

    /**
     * 判断两个window是否一样
     * @param win1 第一个window
     * @param win2 第二个window
     * @return 是否一样
     */
    public abstract boolean areWindowSame(TWindow win1, TWindow win2);

    public abstract IWindowTransaction<TWindow> createWindowTransactionInstance();

    protected TWindow tryStayingCurrentApplication(TWindow currentWindow){
        if(androidAgent.getRuntimePackage().equals(runtimePackage)){
            return currentWindow;
        }
        else{
            androidAgent.pressBack();
            if(androidAgent.getRuntimePackage().equals(runtimePackage)){
                return getCurrentWindow(windowTransaction);
            }
            else{
                int tryCount = 3;
                while (tryCount-- >= 0) {
                    androidAgent.startActivity(startActivity);
                    try {
                        Thread.sleep(3000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    if (androidAgent.getRuntimePackage().equals(runtimePackage)) {
                        return getCurrentWindow(windowTransaction);
                    }

                    androidAgent.pressBack();
                    androidAgent.pressHome();
                    if(tryCount <= 0)
                    {
                        try {
                            AdbTool.killTask(androidAgent.getDevice(), AdbTool.getTaskId(androidAgent.getDevice(), androidAgent.getRuntimePackage()));
                        } catch (TimeoutException | AdbCommandRejectedException | IOException | ShellCommandUnresponsiveException e) {
                            Logger.logException(androidAgent.getDevice().getSerialNumber() + e.getMessage());
                        }
                    }
                }

                    Logger.logInfo(androidAgent.getDevice().getSerialNumber() +" Try restart but failed");
                    return null;

            }
        }
    }

/*
    private SimpleWindow createCurrentWindow(){
        String layoutXML = androidAgent.getLayout();
        while(layoutXML == null)
        {
            layoutXML = androidAgent.getLayout();
            Logger.logInfo("Waiting for layout。。。");
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        LayoutTree lt = new LayoutTree(androidAgent.getLayout());
        if(lt == null)
            Logger.logError("Current layout is null");
        //return new SimpleWindow(lt, androidAgent.getFocusedActivity(), androidAgent.getTopActivityId());
        return new SimpleWindow(lt, androidAgent.getTopActivity(), androidAgent.getTopActivityId());
    }*/

    private void doAction(Action action, IWindow window){
        switch (action.actionType){
            case Click:
                Logger.logInfo(androidAgent.getDevice().getSerialNumber() + " Step " + currentSteps + "\tWindowCount " + windowTransaction.getWindowSize() + "\tClick");
                androidAgent.doClick(action.actionNode);
                break;
            case LongClick:
                Logger.logInfo(androidAgent.getDevice().getSerialNumber() + " Step " + currentSteps + "\tWindowCount " + windowTransaction.getWindowSize() + "\tLongClick" );
                androidAgent.doLongClick(action.actionNode);
                break;
            /*case SetText:
                Logger.logInfo(androidAgent.getDevice().getSerialNumber() + " Step " + currentSteps + "\tWindowCount " + windowTransaction.getWindowSize() + "	SetText");
                androidAgent.doSetText(action.actionNode, "123");
                break;*/
            case ScrollBackward:
                Logger.logInfo(androidAgent.getDevice().getSerialNumber() + " Step " + currentSteps + "\tWindowCount " + windowTransaction.getWindowSize() + "	ScrollBackward");
                androidAgent.doScrollBackward(action.actionNode, 55);
                break;
            case ScrollForward:
                Logger.logInfo(androidAgent.getDevice().getSerialNumber() + " Step " + currentSteps + "\tWindowCount " + windowTransaction.getWindowSize() + "	ScrollForward");
                androidAgent.doScrollForward(action.actionNode, 55);
                break;
            case Back:
                Logger.logInfo(androidAgent.getDevice().getSerialNumber() + " Step " + currentSteps + "\tWindowCount " + windowTransaction.getWindowSize() + "\tBack" );
                androidAgent.pressBack();
                break;
        }
    }
}
