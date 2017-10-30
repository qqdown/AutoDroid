package edu.nju.autodroid.windowtransaction;

import edu.nju.autodroid.hierarchyHelper.LayoutTree;
import edu.nju.autodroid.hierarchyHelper.LayoutTreeList;
import edu.nju.autodroid.utils.Logger;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.function.Consumer;

/**
 * Created by ysht on 2016/5/25 0025.
 */
public class PagedWindow implements IWindow {

    //同一个window可能包含多个layout
    private List<LayoutTree> layoutList = new ArrayList<LayoutTree>();
    private LayoutTree currentLayout = null;//windows总当前的layout
    private String id;
    private String activityName;

    public PagedWindow(String id, String activityName){
        if(id == null){
            Logger.logException("PagedWindow id cannot be null!");
        }
        if(activityName == null){
            Logger.logException("PagedWindow activityName cannot be null!");
        }
        this.id = id;
        this.activityName = activityName;
    }

    @Override
    public String getId() {
        return id;
    }

    /**
     * 返回当前/最后一次被激活的layout
     * 该layout会在addlayout或者setlayout时被更新
     */
    @Override
    public LayoutTree getLayout() {
       return currentLayout;
    }

    public LayoutTreeList getLayoutList(){
        return new LayoutTreeList(layoutList);
    }

    /**
     * 由于PagedWindow是可能包含多个layout的，所以要用addLayout,这个函数内部调用了addLayout
     * @param layout
     */
    @Override
    @Deprecated
    public void setLayout(LayoutTree layout) {
        addLayout(layout);
    }

    /**
     * 将一个layout添加进该window，如果该window中存在相同的layout（用similarityWith判断），则不添加
     * @param layout
     */
    public void addLayout(LayoutTree layout){
        if(layout == null)
        {
            Logger.logInfo("PagedWindow addLayout layout is NULL");
            return;
        }
        Iterator<LayoutTree> it = layoutList.iterator();
        while(it.hasNext()){
            LayoutTree lt = it.next();
            if(lt.similarityWith(layout) >= 0.9){
                currentLayout = lt;
                return;//相同的话就不重复插入了
            }
        }
        layoutList.add(layout);
        currentLayout = layout;
    }

    @Override
    public String getActivityName() {
        return activityName;
    }

    /**
     * 判断当前window中是否存在该layout，除了完全相同，相似的layout也会认为相同（相似度为90%）
     * @param layoutTree
     * @return
     */
    public boolean contains(LayoutTree layoutTree){
        if(layoutTree == null)
            return false;
        Iterator<LayoutTree> it = layoutList.iterator();
        while(it.hasNext()){
            if(layoutTree.similarityWith(it.next()) >= 0.9)
                return true;
        }
        return false;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        PagedWindow that = (PagedWindow) o;

        return id.equals(that.id);

    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }

    public static PagedWindow OutWindow = new PagedWindow(null, "");
    static
    {OutWindow.id = "-1";}
}