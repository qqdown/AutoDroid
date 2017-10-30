package edu.nju.autodroid.windowtransaction;

import edu.nju.autodroid.hierarchyHelper.Action;

import java.util.HashSet;

/**
 * Created by ysht on 2016/8/11 0011.
 */
public class Transaction<TWindow extends IWindow> {
    private TWindow fromWindow;
    private TWindow toWindow;
    private HashSet<Action> actions;

    public Transaction(){}

    public Transaction(TWindow fromWindow, TWindow toWindow, HashSet<Action> actions){
        this.fromWindow = fromWindow;
        this.toWindow = toWindow;
        this.actions = actions;
    }

}
