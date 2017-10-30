package edu.nju.autodroid.strategy;

import edu.nju.autodroid.androidagent.IAndroidAgent;
import edu.nju.autodroid.hierarchyHelper.Action;
import edu.nju.autodroid.hierarchyHelper.LayoutNode;
import edu.nju.autodroid.hierarchyHelper.LayoutTree;
import edu.nju.autodroid.hierarchyHelper.TreeSearchOrder;
import edu.nju.autodroid.utils.Logger;
import edu.nju.autodroid.windowtransaction.IWindowTransaction;
import edu.nju.autodroid.windowtransaction.PagedWindow;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.Random;
import java.util.function.Predicate;

/**
 * Created by ysht on 2016/8/11 0011.
 */
public class PagedWindowWeightSelectionStrategy2 extends PagedWindowRandomSelectionStrategy2 {

    private int lastTransactionEdgeCount = 0;
    private int lastTransactionVertexCount = 0;
    private int noChangeCount = 0;//图收敛的次数
    private BufferedWriter bw;
    IWindowTransaction<PagedWindow> windowTransaction;

    public PagedWindowWeightSelectionStrategy2(IAndroidAgent androidAgent, int maxSteps, String startActivity, Logger actionLogger) {
        super(androidAgent, maxSteps, startActivity, actionLogger);
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
    public Action getNextAction(PagedWindow curWindow, Action lastAction) {
        try {
            bw.write(noChangeCount+"");
            bw.newLine();
            bw.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }

        if(noChangeCount %50 == 0 && noChangeCount != 0){
            Action a = new Action();
            a.actionType = Action.ActionType.Back;
            return  a;
        }
        if(noChangeCount >= 500){
            Action a = new Action();
            a.actionType = Action.ActionType.NoMoreAction;
            try {
                bw.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return  a;
        }
        LayoutTree lt = curWindow.getLayout();
        if(lt == null)
            lt = getCurrentLayout(androidAgent);
        Action action = new Action();
        action.actionType = Action.ActionType.NoAction;
        List<LayoutNode> nodeList = lt.findAll(new Predicate<LayoutNode>() {
            @Override
            public boolean test(LayoutNode node) {
                return node.clickable || node.checkable || node.scrollable || node.focusable || node.longClickable;
            }
        }, TreeSearchOrder.DepthFirst);

        if(nodeList.size()>0){
            int rand = new Random().nextInt((int)((nodeList.size()+1)*1.0));
            if(rand >= nodeList.size()){//返回按钮
                    action.actionType = Action.ActionType.Back;
                    action.actionNode = null;
               // Logger.logInfo("点击返回按钮");
            }
            else {
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


                nodeToChoose.weight--;
                if(nodeToChoose.weight<1)
                    nodeToChoose.weight = 1;
                Logger.logInfo("Weight\t" + nodeToChoose.weight);
                action.actionNode = nodeToChoose;
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
        }else{//没有可以点的，就返回
            action.actionType = Action.ActionType.Back;
        }

        return action;
    }




    @Override
    public void afterAction(Action action, PagedWindow actionWindow, PagedWindow afterActionWindow, IWindowTransaction<PagedWindow> windowTransaction) {
        this.windowTransaction = windowTransaction;
        int transactionVertexCount = windowTransaction.getWindows().length;
        int transactionEdgeCount = windowTransaction.getTransactions().size();
        if(transactionEdgeCount == lastTransactionEdgeCount && transactionVertexCount == lastTransactionVertexCount){//边的个数和节点个数没有变化
            if(action.actionNode != null){
                action.actionNode.weight -= 1;
                if(action.actionNode.weight < 1)
                    action.actionNode.weight = 1;
            }
            noChangeCount ++;
            Logger.logInfo("noChangeCount " + noChangeCount);
        }
        else
        {
            if(action.actionNode != null)
            {
                action.actionNode.weight += 2;
                LayoutTree lt = afterActionWindow.getLayout();
                if(lt != null)
                {
                    int totalWeight = 0;

                    List<LayoutNode> nodeList = lt.findAll(new Predicate<LayoutNode>() {
                        @Override
                        public boolean test(LayoutNode node) {
                            return node.clickable || node.checkable || node.scrollable || node.focusable || node.longClickable;
                        }
                    }, TreeSearchOrder.DepthFirst);
                    for(LayoutNode n : nodeList){
                        totalWeight += n.weight;
                    }
                    if(nodeList.size() > 0)
                        action.actionNode.weight += (int)(totalWeight/nodeList.size()+1);
                }
            }
            noChangeCount = 0;
        }
        lastTransactionEdgeCount = transactionEdgeCount;
        lastTransactionVertexCount = transactionVertexCount;
    }

    @Override
    public String getStrategyName() {
        return "PagedWindow 带权重搜索策略";
    }

    @Override
    public String getStrategyDescription() {
        return "PagedWindow 带权重搜索策略" +
                "该策略根据某个action是否会出发页面跳转来决定选择的权重";
    }
}

