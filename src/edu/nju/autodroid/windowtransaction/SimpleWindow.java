package edu.nju.autodroid.windowtransaction;

import edu.nju.autodroid.hierarchyHelper.LayoutTree;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * 用于表示当前屏幕中的内容
 * Created by ysht on 2016/3/7 0007.
 */
public class SimpleWindow implements IWindow {
    private LayoutTree layout;
    private String activityName;
    private String id;
    private String windowDumpId;//从dumpsys 获取得到的id，id相同一定是同一个window，反之则不一定

    public SimpleWindow(LayoutTree layout, String activityName){
        this.layout = layout;
        this.activityName = activityName==null?"":activityName;
        this.id = new SimpleDateFormat("yyyyMMddhhmmssSSS").format(new Date());
        this.windowDumpId = "";
    }

    public SimpleWindow(LayoutTree layout, String activityName, String windowDumpId){
        this.layout = layout;
        this.activityName = activityName==null?"":activityName;
        this.id = new SimpleDateFormat("yyyyMMddhhmmssSSS").format(new Date());
        this.windowDumpId = windowDumpId;
    }

    public String getActivityName() {
        return activityName;
    }

    @Override
    public String getId() {
        return id;
    }

    public String getWindowDumpId(){
        return windowDumpId;
    }

    @Override
    public LayoutTree getLayout() {
        return layout;
    }

    @Override
    public void setLayout(LayoutTree layout){
        this.layout = layout;
    }

    public static SimpleWindow OutWindow = new SimpleWindow(null, "");
    static
    {OutWindow.id = "-1";}


}
