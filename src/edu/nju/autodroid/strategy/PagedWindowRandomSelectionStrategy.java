package edu.nju.autodroid.strategy;

import edu.nju.autodroid.androidagent.IAndroidAgent;
import edu.nju.autodroid.hierarchyHelper.Action;
import edu.nju.autodroid.hierarchyHelper.LayoutNode;
import edu.nju.autodroid.hierarchyHelper.LayoutTree;
import edu.nju.autodroid.hierarchyHelper.TreeSearchOrder;
import edu.nju.autodroid.utils.Logger;
import edu.nju.autodroid.windowtransaction.IWindowTransaction;
import edu.nju.autodroid.windowtransaction.PagedWindow;
import edu.nju.autodroid.windowtransaction.PagedWindowTransaction;

import java.util.List;
import java.util.Random;
import java.util.function.Predicate;

/**
 * Created by ysht on 2016/5/26 0026.
 */
public class PagedWindowRandomSelectionStrategy extends SelectionStrategy<PagedWindow> {
    public PagedWindowRandomSelectionStrategy(IAndroidAgent androidAgent, int maxSteps, String startActivity, Logger actionLogger) {
        super(androidAgent, maxSteps, startActivity, actionLogger);
}

    @Override
    public Action getNextAction(PagedWindow curWindow, Action lastAction) {
        LayoutTree lt = getCurrentLayout(androidAgent);
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
    public void afterAction(Action action, PagedWindow actionWindow, PagedWindow afterActionWindow, IWindowTransaction<PagedWindow> windowTransaction) {
        return;
    }

    @Override
    public PagedWindow getCurrentWindow(IWindowTransaction<PagedWindow> windowTransaction) {
        String currentRuntimePackage = androidAgent.getRuntimePackage();
        if(currentRuntimePackage == null || !currentRuntimePackage.equals(runtimePackage))//不再当前程序中
            return PagedWindow.OutWindow;
        String windowId = androidAgent.getTopActivityId();

        while(windowId == null){
            try {
                Logger.logInfo("Wating for window Id");
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            windowId = androidAgent.getTopActivityId();

        }
        String activityName = androidAgent.getTopActivity();
        while(activityName == null){
            try {
                Thread.sleep(1000);
                Logger.logInfo("Wating for activity name");
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            activityName = androidAgent.getTopActivity();
        }
        if(activityName == null)//打不开窗口，出错！
            return null;
        PagedWindow currentWindow = new PagedWindow(windowId, activityName);
        LayoutTree currentLayout = getCurrentLayout(androidAgent);
        for(PagedWindow win : windowTransaction.getWindows()){
            if(win.contains(currentLayout)){//界面相同
                win.addLayout(currentLayout);
                return win;
            }
            else if(currentWindow.getId().equals(win.getId())){//id相同，界面不同
                win.addLayout(currentLayout);
                return  win;
            }
        }

        currentWindow.addLayout(currentLayout);
        windowTransaction.addWindow(currentWindow);
        return currentWindow;
    }

    @Override
    public boolean areWindowSame(PagedWindow win1, PagedWindow win2) {
        return win1.equals(win2);
    }

    @Override
    public IWindowTransaction<PagedWindow> createWindowTransactionInstance() {
        IWindowTransaction<PagedWindow> windowTransaction = new PagedWindowTransaction();
        windowTransaction.addWindow(PagedWindow.OutWindow);
        return windowTransaction;
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

    @Override
    public String getStrategyName() {
        return "PagedWindow 随机搜索策略";
    }

    @Override
    public String getStrategyDescription() {
        return "PagedWindow 随机搜索策略";
    }
}
