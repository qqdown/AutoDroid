package edu.nju.autodroid.hierarchyHelper;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.util.*;

/**
 * Created by ysht on 2016/3/7 0007.
 * 用于保存LayoutNode列表
 */
public class LayoutNodeList implements Iterable<LayoutNode> {

    private List<LayoutNode> nodeList;
    private LayoutNodeList(){}
    public LayoutNodeList(List<LayoutNode> nodeList){
        this.nodeList = new ArrayList<LayoutNode>();
        this.nodeList.addAll(nodeList);
    }

    public LayoutNode get(int index){return nodeList.get(index);}

    public int size() {
        return nodeList.size();
    }

    public boolean isEmpty() {
        return size()==0;
    }

    public boolean contains(Object o) {
        return nodeList.contains(o);
    }

    public boolean containsAll(Collection<?> c) {
        return nodeList.containsAll(c);
    }

    @Override
    public boolean equals(Object o) {
        return nodeList.equals(o);
    }

    @Override
    public int hashCode() {
        return nodeList.hashCode();
    }

    @Override
    public Iterator<LayoutNode> iterator() {
        return nodeList.iterator();
    }
}
