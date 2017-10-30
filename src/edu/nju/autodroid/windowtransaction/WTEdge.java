package edu.nju.autodroid.windowtransaction;

import edu.nju.autodroid.hierarchyHelper.Action;

import java.util.HashSet;

/**
 * Created by ysht on 2016/3/7 0007.
 */
public class WTEdge {
    protected String fromId;
    protected String toId;
    protected HashSet<Action> actionSet = new HashSet<Action>();

    public WTEdge(String fromId, String toId){
        this.fromId = fromId;
        this.toId = toId;
    }

    public WTEdge(String fromId, String toId, Action action){
        this.fromId = fromId;
        this.toId = toId;
        this.actionSet.add(action);
    }

    /**
     * 添加行为
     * @param action 行为
     */
    public void addAction(Action action){
        this.actionSet.add(action);
    }

    public void addActions(HashSet<Action> actions){
        this.actionSet.addAll(actions);
    }

    public int getActionSize(){
        return actionSet.size();
    }

    public String getFromId() {
        return fromId;
    }

    public String getToId() {
        return toId;
    }

    /**
     * 获取ActionSet的Iterator
     * @return actionSet.iterator()
     */
    public HashSet<Action> getActions(){
        return actionSet;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        WTEdge wtEdge = (WTEdge) o;
        //fromId和toId相同，则认为是同一条边
        if (fromId != null ? !fromId.equals(wtEdge.fromId) : wtEdge.fromId != null) return false;
        if (toId != null ? !toId.equals(wtEdge.toId) : wtEdge.toId != null) return false;
        return true;

    }

    @Override
    public int hashCode() {
        int result = fromId != null ? fromId.hashCode() : 0;
        result = 31 * result + (toId != null ? toId.hashCode() : 0);
        return result;
    }
}
