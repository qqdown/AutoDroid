package edu.nju.autodroid.strategy;

import edu.nju.autodroid.hierarchyHelper.LayoutTree;
import edu.nju.autodroid.windowtransaction.IWindow;
import edu.nju.autodroid.windowtransaction.PagedWindow;

/**
 * Created by ysht on 2017/1/7.
 */
public class GL<TWindow extends IWindow>
{
    public TWindow G = null;
    public LayoutTree L = null;
}