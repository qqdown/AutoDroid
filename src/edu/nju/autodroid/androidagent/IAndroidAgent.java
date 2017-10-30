package edu.nju.autodroid.androidagent;
import com.android.ddmlib.IDevice;
import edu.nju.autodroid.hierarchyHelper.AndroidWindow;
import edu.nju.autodroid.hierarchyHelper.LayoutNode;

import java.util.List;

/**
 * Created by ysht on 2016/3/8 0008.
 */
public interface IAndroidAgent {
    /**
     * 初始化adb
     * @return 初始化是否成功
     */
    boolean init();

    /**
     * 终止adb
     */
    void terminate();

    IDevice getDevice();

    /**
     * 向设备安装apk
     * @param apkFilePath apk文件路径
     * @return 是否安装成功
     */
    boolean installApk(String apkFilePath);

    /**
     * 获得当前Activity名
     * @return activity名
     */
    String getFocusedActivity();

    /**
     * 启动程序
     * @param activityName 完整的activity名，格式为packageName/.activityName
     * @return 是否成功启动
     */
    boolean startActivity(String activityName);

    /**
     * 停止应用
     * @param packageName 应用包名
     */
    boolean stopApplication(String packageName);

    /**
     * 获取当前正在运行的Activity，返回包含Activity名的List，顺序为运行栈顶-》栈底
     * @return 包含Activity名的List
     */
    List<String> getRunningActivities();

    /**
     * 获取设备中的AndroidWindow列表，通过dumpsys获取。
     * 注意！若无法获取到信息，该函数会循环尝试直到获取信息，每次循环间隔为500ms
     * @return AndroidWindow列表
     * @throws InterruptedException
     */
    List<AndroidWindow> getAndroidWindows();

    /**
     * 获取当前Task的id
     * @return Task id
     */
    int getFocusedTaskId();

    void pressHome();

    void pressBack();

    String getLayout();

    String getTopActivityId();

    String getTopActivity();

    String getRuntimePackage();

    boolean doClick(LayoutNode btn);

    boolean doSetText(LayoutNode node, String content);

    boolean doLongClick(LayoutNode node);

    boolean doClickAndWaitForWindow(LayoutNode node);

    boolean doScrollBackward(LayoutNode node, int steps);

    boolean doScrollForward(LayoutNode node, int steps);

    boolean doScrollToEnd(LayoutNode node, int maxSwipes, int steps);

    boolean doScrollToBeginning(LayoutNode node, int maxSwipes, int steps);

    boolean doScrollIntoView(LayoutNode node, LayoutNode viewObj);

    boolean doSwipeToLeft(LayoutNode node);

    boolean doSwipeToRight(LayoutNode node);
}
