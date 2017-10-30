package edu.nju.autodroid.hierarchyHelper;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by ysht on 2016/3/7 0007.
 */
public class LayoutNode {
    //保存的信息
    public int index;
    public String text;
    public String className;
    public String packageName;
    public String contentDesc;
    public boolean checkable;
    public boolean checked;
    public boolean clickable;
    public boolean enabled;
    public boolean focusable;
    public boolean focuesd;
    public boolean scrollable;
    public boolean longClickable;
    public boolean password;
    public boolean selected;
    public int[] bound;
    public String indexXpath;//节点在树中xPath

    public double weight = 10;//用于带Weight的策略

    /***
     * actiontype的权值，0-click，1-longclick，2-Home, 3-Back,4-ScrollBackward, 5-ScrollForward, 6-SwipeToRight, 7-SwipeTOLeft
     * Click,
     LongClick,
     //SetText,
     Home,
     Back,
     ScrollBackward,
     ScrollForward,
     SwipeToRight,
     SwipeToLeft,
     */
    public double[] weight_actionType;
    //树结构
    private LayoutNode parent;
    private List<LayoutNode> children;
    private int totalChildrenCount;//包括所有的子节点个数

    public LayoutNode(){
        parent = null;
        bound = new int[4];
        children = new ArrayList<LayoutNode>();
        totalChildrenCount = 0;
        //去掉最后2个noaction和nomoreaction
        weight_actionType = new double[Action.ActionType.values().length-2];
        for(int i=0; i<weight_actionType.length; i++)
            weight_actionType[i] = 10;
    }

    public void addChild(LayoutNode child) {
        children.add(child);
        child.parent = this;
        totalChildrenCount++;
        LayoutNode par = this.parent;
        while(par != null){
            par.totalChildrenCount++;
            par = par.parent;
        }
    }

    public void replaceChild(LayoutNode newChild, LayoutNode oldChild){
        int index = children.indexOf(oldChild);
        if(index>=0) {
            children.remove(index);
            children.add(index, newChild);
            newChild.parent = this;
            oldChild.parent = null;
            totalChildrenCount = totalChildrenCount - oldChild.totalChildrenCount + newChild.totalChildrenCount;
            LayoutNode par = this.parent;
            while(par != null){
                par.totalChildrenCount= par.totalChildrenCount - oldChild.totalChildrenCount + newChild.totalChildrenCount;
                par = par.parent;
            }
        }
    }

    public int getChildrenCount(){
        return children.size();
    }

    public int getTotalChildrenCount(){
        return totalChildrenCount;
    }

    public LayoutNode getParent(){
        return parent;
    }

    public LayoutNodeList getChildren(){
        return new LayoutNodeList(children);
    }

    @Override
    public String toString() {
        return "LayoutNode{" +
                "index=" + index +
                ", text='" + text + '\'' +
                ", className='" + className + '\'' +
                ", packageName='" + packageName + '\'' +
                ", contentDesc='" + contentDesc + '\'' +
                ", checkable=" + checkable +
                ", checked=" + checked +
                ", clickable=" + clickable +
                ", enabled=" + enabled +
                ", focusable=" + focusable +
                ", focuesd=" + focuesd +
                ", scrollable=" + scrollable +
                ", longClickable=" + longClickable +
                ", password=" + password +
                ", selected=" + selected +
                ", bound=" + Arrays.toString(bound) +
                '}';
    }

    /**
     * 与另一个LayoutNode的相似程度，现在认为，className相同为1，否则为0
     * @param node
     * @return 相似程度0或1
     */
    public double similarityWith(LayoutNode node){
        if(node == null)
            return 0;
        if(node.className.equals(this.className))
            return 1;//相同
        return 0;//不同
    }

}
