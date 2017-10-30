package edu.nju.autodroid.strategy;

import edu.nju.autodroid.androidagent.IAndroidAgent;
import edu.nju.autodroid.hierarchyHelper.*;
import edu.nju.autodroid.utils.Logger;
import edu.nju.autodroid.windowtransaction.IWindow;
import edu.nju.autodroid.windowtransaction.IWindowTransaction;
import edu.nju.autodroid.windowtransaction.SimpleWindow;
import edu.nju.autodroid.windowtransaction.SimpleWindowTransaction;

import java.util.List;
import java.util.Random;
import java.util.function.Predicate;

/**
 * Created by ysht on 2016/3/8 0008.
 */
public class SimpleWindowRandomSelectionStrategy extends SelectionStrategy<SimpleWindow> {
    public SimpleWindowRandomSelectionStrategy(IAndroidAgent androidAgent, int maxSteps, String startActivity) {
        super(androidAgent, maxSteps, startActivity, null);
    }

    @Override
    public Action getNextAction(SimpleWindow curWindow, Action lastAction) {
        LayoutTree lt = curWindow.getLayout();
        Action action = new Action();
        action.actionType = Action.ActionType.NoAction;
        List<LayoutNode> nodeList = lt.findAll(new Predicate<LayoutNode>() {
            @Override
            public boolean test(LayoutNode node) {
                return node.clickable || node.checkable || node.scrollable || node.focusable;
            }
        }, TreeSearchOrder.DepthFirst);

        int randInt = new Random().nextInt((int)((nodeList.size()+1)*1.0));
        if(randInt >= nodeList.size()){
            if(nodeList.size() >= 1)
            {
                action.actionType = Action.ActionType.Back;
                action.actionNode = null;
            }
            else{
                action.actionType = Action.ActionType.Click;
                action.actionNode = null;
            }

        }
        else {
            action.actionNode = nodeList.get(randInt);
            if(action.actionNode.scrollable){
                int ri = new Random().nextInt(3);
                if(ri == 2)
                    action.actionType = Action.ActionType.ScrollBackward;
                else
                    action.actionType = Action.ActionType.ScrollForward;
            }
            else{
                int ri = new Random().nextInt(5);
                if(ri <= 2)
                    action.actionType = Action.ActionType.Click;
                else if(ri <= 4)
                    action.actionType = Action.ActionType.LongClick;
            }
        }
        return action;
    }

    @Override
    public void afterAction(Action action, SimpleWindow actionWindow, SimpleWindow afterActionWindow, IWindowTransaction<SimpleWindow> windowTransaction) {

    }

    @Override
    public SimpleWindow getCurrentWindow(IWindowTransaction<SimpleWindow> windowTransaction) {
         if(!androidAgent.getRuntimePackage().equals(runtimePackage))//不再当前程序中
            return SimpleWindow.OutWindow;
        SimpleWindow currentWindow = createCurrentWindow(androidAgent);
        for(SimpleWindow win : windowTransaction.getWindows()){
            if(win.getLayout() != null && areWindowSame(win, currentWindow)){
                win.setLayout(currentWindow.getLayout());
                return  win;
            }
        }
        windowTransaction.addWindow(currentWindow);
        return currentWindow;
    }

    @Override
    public boolean areWindowSame(SimpleWindow win1, SimpleWindow win2) {
        if(win1.getId().equals(win2.getId()))//id相同，则为同一个
            return true;
        if(win1.getWindowDumpId() != null && win1.getWindowDumpId().equals(win2.getWindowDumpId()))
            return true;
        if(!win1.getActivityName().equals(win2.getActivityName()))//activityName不同，则一定不为同一个
            return false;

        return win1.getLayout().similarityWith(win2.getLayout())>=0.9;
    }

    @Override
    public IWindowTransaction createWindowTransactionInstance() {
        //选择的WT
        IWindowTransaction<SimpleWindow> windowTransaction = new SimpleWindowTransaction();
        windowTransaction.addWindow(SimpleWindow.OutWindow);
        return windowTransaction;
    }

    protected SimpleWindow createCurrentWindow(IAndroidAgent androidAgent) {
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
    }

    @Override
    public String getStrategyName() {
        return "SimpleWindow 随机搜索策略";
    }

    @Override
    public String getStrategyDescription(){
        return "SimpleWindow 随机搜索策略";
    }

}
