package edu.nju.autodroid.windowtransaction;

import edu.nju.autodroid.windowtransaction.IWindow;

/**
 * Created by ysht on 2016/3/7 0007.
 */
public class WTVertex<TWindow extends IWindow> {
    private TWindow window;

    public WTVertex(TWindow window){
        this.window = window;
    }

    public String getId(){
        return window.getId();
    }

    public TWindow getWindow(){
        return  window;
    }

}
