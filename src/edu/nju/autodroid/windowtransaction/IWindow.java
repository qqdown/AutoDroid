package edu.nju.autodroid.windowtransaction;

import edu.nju.autodroid.hierarchyHelper.LayoutTree;

/**
 * Created by ysht on 2016/5/24 0024.
 */
public interface IWindow {
    String getId();

    LayoutTree getLayout();

    void setLayout(LayoutTree layout);

    String getActivityName();
}
