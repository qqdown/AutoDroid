package edu.nju.autodroid.strategy;

import edu.nju.autodroid.androidagent.IAndroidAgent;
import edu.nju.autodroid.hierarchyHelper.LayoutTree;

import java.util.Date;
import java.util.Timer;

/**
 * Created by ysht on 2017/11/6 0006.
 */
public class TestStrategy implements IStrategy {

    protected IAndroidAgent agent;

    @Override
    public String getStrategyName() {
        return "test strategy";
    }

    @Override
    public String getStrategyDescription() {
        return "test strategy";
    }

    @Override
    public String getRuntimePackageName() {
        return "????????????????why";
    }

    public TestStrategy(IAndroidAgent agent){
        this.agent = agent;
    }

    @Override
    public boolean run() {
        int count = 10;
        while (count-->0){
            Date time = new Date();
            String xml = agent.getLayout();
            LayoutTree lt = new LayoutTree(xml);
            Date now = new Date();
            System.out.println("time " + (now.getTime()-time.getTime()) + " layoutLen " + xml.length());
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        return true;
    }

    @Override
    public void writeToFile(String fileName) {

    }
}
