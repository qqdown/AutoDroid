package edu.nju.autodroid.hierarchyHelper;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.function.Consumer;

/**
 * Created by ysht on 2016/5/25 0025.
 */
public class LayoutTreeList implements Iterable<LayoutTree> {

    private List<LayoutTree> layoutTreeList = new ArrayList<LayoutTree>();

    private LayoutTreeList(){}
    public LayoutTreeList(List<LayoutTree> layoutTreeList){
        this.layoutTreeList.addAll(layoutTreeList);
    }

    @Override
    public Iterator<LayoutTree> iterator() {
        return layoutTreeList.iterator();
    }
}
