package edu.nju.autodroid.strategy;

import com.android.ddmlib.AdbCommandRejectedException;
import com.android.ddmlib.ShellCommandUnresponsiveException;
import com.android.ddmlib.TimeoutException;
import edu.nju.autodroid.androidagent.IAndroidAgent;
import edu.nju.autodroid.hierarchyHelper.*;
import edu.nju.autodroid.utils.AdbTool;
import edu.nju.autodroid.utils.Configuration;
import edu.nju.autodroid.utils.Logger;
import edu.nju.autodroid.windowtransaction.*;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.Random;
import java.util.function.Predicate;

/**
 * Created by ysht on 2017/1/6.
 */
public class GroupWeightedSelectionStrategy implements IStrategy{
    private GroupTransaction groupTransaction = new GroupTransaction();

    protected String runtimePackage;
    protected IAndroidAgent androidAgent;
    private Action lastAction = null;
    protected int maxSteps;
    protected String startActivity;
    private int currentSteps;
    private Logger actionLogger = null;
    private  int lastGraphVetexCount = 0;
    private  int lastGraphEdgeCount = 0;
    private BufferedWriter bw;
    protected int MaxNoChangCount = 500;
    protected double similarityThreshold = 0.8;

    public GroupWeightedSelectionStrategy(IAndroidAgent androidAgent, int maxSteps, String startActivity, Logger actionLogger){
        this.MaxNoChangCount = Configuration.getDeltaC();
        this.similarityThreshold = Configuration.getDeltaL();
        this.androidAgent = androidAgent;
        this.maxSteps = maxSteps;
        this.startActivity = startActivity;
        currentSteps = 0;
        this.actionLogger = actionLogger;
        groupTransaction.addWindow(Group.OutWindow);
        File dir = new File("NoChangeCountLogger");
        if(!dir.exists())
            dir.mkdirs();
        try {
            bw = new BufferedWriter(new FileWriter(dir.getAbsolutePath() + "/" + new File(actionLogger.getLoggerFilePath()).getName()));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public String getStrategyName() {
        return "Layout group strategy";
    }

    @Override
    public String getStrategyDescription() {
        return "Layout group strategy";
    }

    @Override
    public String getRuntimePackageName() {
        return runtimePackage;
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

        try{
            int scrollCount = 5;
            while(scrollCount-->0)//尝试右滑scrollCount次
            {
                int[] windowSize = getCurrentLayout(androidAgent).getScreenSize();
                AdbTool.doSwipe(androidAgent.getDevice(), windowSize[0], windowSize[1]/2, 0, windowSize[1]/2);
                Thread.sleep(1000);
            }
            GL<Group> gl = getCurrentGL();
            int unChangedCount = 0;
            while(currentSteps <= maxSteps){
                try {
                    bw.write(unChangedCount+"");
                    bw.newLine();
                    bw.flush();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                Action action = new Action();
                action.actionType = Action.ActionType.NoMoreAction;
                if(unChangedCount>=MaxNoChangCount)
                   break;
                if(gl.L == null){
                    action.actionType = Action.ActionType.NoAction;
                }
                else
                {
                    List<LayoutNode> nodeList = gl.L.findAll(new Predicate<LayoutNode>() {
                        @Override
                        public boolean test(LayoutNode node) {
                            return node.clickable||node.longClickable||node.focusable||node.scrollable||node.checkable;
                        }
                    }, TreeSearchOrder.DepthFirst);
                    if(nodeList.size()>0) {
                        LayoutNode nodeSelected = weightedRandSelected(nodeList);
                        int actioType = weightedActionType(nodeSelected.weight_actionType);
                        nodeSelected.weight -= 1;
                        if (nodeSelected.weight < 0)
                            nodeSelected.weight = 0;
                        nodeSelected.weight_actionType[actioType] -= 1;
                        if (nodeSelected.weight_actionType[actioType] < 0)
                            nodeSelected.weight_actionType[actioType] = 0;
                        action.actionNode = nodeSelected;
                        action.actionType = Action.ActionType.values()[actioType];
                    }else{
                        action.actionType = Action.ActionType.NoAction;
                    }
                }

                if(action.actionType == Action.ActionType.NoAction){
                    unChangedCount++;
                    action.actionType = Action.ActionType.Back;
                }

                if(action.actionType == Action.ActionType.NoMoreAction)
                    break;
                else{
                    doAction(action);
                    GL<Group> gl_p = gl;
                    gl = getCurrentGL();
                    groupTransaction.addTransaction(gl_p.G.getId(), gl.G.getId(), action);
                    if(action.actionNode != null)
                        actionLogger.info(currentSteps + "\t" + gl_p.G.getId()+"\t" + gl.G.getId()+"\t"+ action.actionType.name()+"\t" + action.actionNode.className + "\t" + action.actionNode.indexXpath.replace(' ',';') + "\t" + getDumpedWindowString()+"\t" + androidAgent.getLayout());
                    else
                        actionLogger.info(currentSteps + "\t" + gl_p.G.getId()+"\t" + gl_p.G.getId()+"\t"+ action.actionType.name()+ "\tnull\tnull\t" + getDumpedWindowString()+"\t" + androidAgent.getLayout());

                    if(isChanged()){
                        unChangedCount = 0;
                        action.actionNode.weight += 2;
                        action.actionNode.weight_actionType[action.actionType.ordinal()] += 2;
                    }else{
                        unChangedCount++;
                    }
                    System.out.println("unChangedCount " + unChangedCount);
                }
                if(tryStayingCurrentApplication()){
                    gl = getCurrentGL();
                }else{
                    Logger.logInfo(androidAgent.getDevice().getSerialNumber() + " 已经跳出当前package");
                    return false;
                }
                currentSteps++;
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


    private boolean isChanged(){
        int vertexCount = groupTransaction.getWindows().length;
        int edgeCount = groupTransaction.getTransactions().size();
        boolean changed;
        if(vertexCount == lastGraphVetexCount && edgeCount == lastGraphEdgeCount){
            changed =  false;

        }
        else{
            changed = true;
        }
        lastGraphEdgeCount = edgeCount;
        lastGraphVetexCount = vertexCount;
        return changed;
    }

    protected LayoutNode weightedRandSelected(List<LayoutNode> nodeList){
        int weightSum = 0;
        for(LayoutNode n : nodeList){
            weightSum += n.weight;
        }
        LayoutNode nodeToChoose = null;
        if(weightSum == 0){
            nodeToChoose = nodeList.get(new Random().nextInt(nodeList.size()));
        }
        else{
            int weightStep = 0;
            for(int i=0; i<nodeList.size(); i++){
                weightStep += nodeList.get(i).weight;
                if(new Random(new Date().getTime()).nextDouble() <= weightStep*1.0/weightSum){
                    nodeToChoose = nodeList.get(i);
                    break;
                }
            }
        }
        return nodeToChoose;
    }

    protected int weightedActionType(double[] weight_actiontype){
        int weightSum = 0;
        for(double w : weight_actiontype){
            weightSum += w;
        }
        int actioTypeIndex = 0;
        if(weightSum == 0){
            actioTypeIndex = new Random().nextInt(weight_actiontype.length);
        }else{
            int weightStep = 0;
            for(int i=0; i<weight_actiontype.length; i++){
                weightStep += weight_actiontype[i];
                if(new Random(new Date().getTime()).nextDouble() <= weightStep*1.0/weightSum){
                    actioTypeIndex = i;
                    break;
                }
            }
        }
        return actioTypeIndex;
    }


    protected LayoutTree getCurrentLayout(IAndroidAgent androidAgent) {
        String layoutXML = androidAgent.getLayout();
        while(layoutXML == null)
        {
            layoutXML = androidAgent.getLayout();
            Logger.logInfo(androidAgent.getDevice().getSerialNumber() + " Waiting for layout。。。");
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        LayoutTree lt = new LayoutTree(layoutXML);
        return lt;
    }

    protected GL<Group> getCurrentGL(){
        GL<Group> gl;
        if(androidAgent.getRuntimePackage().equals(runtimePackage)) {
            Group newGroup = new Group(new Date().getTime() + "");
            LayoutTree lc = getCurrentLayout(androidAgent);
            gl = getSimilaryLayout(lc);
            if (gl.G == null) {
                gl.G = newGroup;
                gl.L = lc;
                newGroup.addLayout(lc);
            } else {
                double sim = lc.similarityWith(gl.L);
                if (sim >= 0.99) {
                    return gl;
                } else if (sim >= similarityThreshold) {
                    MergeWeight(gl.L, lc);
                    gl.G.addLayout(lc);
                } else {
                    gl.G = newGroup;
                    gl.G.addLayout(lc);
                }
            }
        }else{
            gl = new GL<Group>();
            gl.G = Group.OutWindow;
        }
        if(groupTransaction.getWindow(gl.G.getId()) == null){
            groupTransaction.addWindow(gl.G);
        }
        return gl;
    }

    private GL<Group> getSimilaryLayout(LayoutTree lc){
        double max=-1;
        GL gl = new GL();
        for(Group win : groupTransaction.getWindows()){
            for(LayoutTree l :win.getLayouts()){
                double sim = l.similarityWith(lc);
                if(sim>max){
                    gl.L = l;
                    gl.G = win;
                    max = sim;
                }
            }
        }
        return gl;
    }

    //lm的内容传给lc
    private void MergeWeight(LayoutTree lm, LayoutTree lc){
        for(LayoutNode n : lm.findAll(new Predicate<LayoutNode>() {
            @Override
            public boolean test(LayoutNode node) {
                return true;
            }
        }, TreeSearchOrder.DepthFirst))
        {
            LayoutNode nc = lc.getNodeByXPath(n.indexXpath);
            if(nc!= null)
            {
                nc.weight =n.weight;
                for(int i=0; i<nc.weight_actionType.length; i++)
                    nc.weight_actionType[i] = n.weight_actionType[i];
            }
        }
    }

    @Override
    public void writeToFile(String fileName) {
        groupTransaction.writeToFile(fileName);
    }

    protected boolean tryStayingCurrentApplication(){
        if(androidAgent.getRuntimePackage().equals(runtimePackage)){
            return true;
        }
        else{
            androidAgent.pressBack();
            if(androidAgent.getRuntimePackage().equals(runtimePackage)){
                return true;
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
                        return true;
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
                return false;

            }
        }
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
    protected void doAction(Action action){
        switch (action.actionType){
            case Click:
                Logger.logInfo(androidAgent.getDevice().getSerialNumber() + " Step " + currentSteps + "\tWindowCount " + groupTransaction.getWindowSize() + "\tClick");
                androidAgent.doClick(action.actionNode);
                break;
            case LongClick:
                Logger.logInfo(androidAgent.getDevice().getSerialNumber() + " Step " + currentSteps + "\tWindowCount " + groupTransaction.getWindowSize() + "\tLongClick" );
                androidAgent.doLongClick(action.actionNode);
                break;
            /*case SetText:
                Logger.logInfo(androidAgent.getDevice().getSerialNumber() + " Step " + currentSteps + "\tWindowCount " + windowTransaction.getWindowSize() + "	SetText");
                androidAgent.doSetText(action.actionNode, "123");
                break;*/
            case ScrollBackward:
                Logger.logInfo(androidAgent.getDevice().getSerialNumber() + " Step " + currentSteps + "\tWindowCount " + groupTransaction.getWindowSize() + "	ScrollBackward");
                androidAgent.doScrollBackward(action.actionNode, 55);
                break;
            case ScrollForward:
                Logger.logInfo(androidAgent.getDevice().getSerialNumber() + " Step " + currentSteps + "\tWindowCount " + groupTransaction.getWindowSize() + "	ScrollForward");
               androidAgent.doScrollForward(action.actionNode, 55);
                break;
            case Back:
                Logger.logInfo(androidAgent.getDevice().getSerialNumber() + " Step " + currentSteps + "\tWindowCount " + groupTransaction.getWindowSize() + "\tBack" );
                androidAgent.pressBack();
                break;
            case Home:
                Logger.logInfo(androidAgent.getDevice().getSerialNumber() + " Step " + currentSteps + "\tWindowCount " + groupTransaction.getWindowSize() + "\tHome" );
                androidAgent.pressHome();
                break;
            case SwipeToLeft:
                Logger.logInfo(androidAgent.getDevice().getSerialNumber() + " Step " + currentSteps + "\tWindowCount " + groupTransaction.getWindowSize() + "\tSwipeToLeft" );
                androidAgent.doSwipeToLeft(action.actionNode);
                break;
            case SwipeToRight:
                Logger.logInfo(androidAgent.getDevice().getSerialNumber() + " Step " + currentSteps + "\tWindowCount " + groupTransaction.getWindowSize() + "\tSwipeToRight" );
                androidAgent.doSwipeToRight(action.actionNode);
                break;
        }
    }
}
