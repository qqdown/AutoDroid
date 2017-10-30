package edu.nju.autodroid.windowtransaction;

import edu.nju.autodroid.hierarchyHelper.Action;

import java.util.List;

/**
 * Created by ysht on 2016/3/7 0007.
 * 用于记录Window转移情况的接口
 */
public interface IWindowTransaction<TWindow extends IWindow> {
    /**
     * 添加窗口，窗口是Window Graph的基本单位
     * @param window 窗口
     */
    void addWindow(TWindow window);

    /**
     * 添加行为
     * @param fromId 源窗口id
     * @param toId 目标窗口id
     * @param action 触发转移的行为
     */
    void addTransaction(String fromId, String toId, Action action);

    /**
     * 通过id查找窗口
     * @param id 窗口id
     * @return 窗口，不存在则返回null
     */
    TWindow getWindow(String id);

    /**
     * 获取所有Window的个数
     * @return Window个数
     */
    int getWindowSize();

    /**
     * 返回Window数组
     * @return
     */
    TWindow[] getWindows();

    /**
     * 返回所有的边
     * @return
     */
    List<Transaction<TWindow>> getTransactions();

    /**
     * 查询和comparedWindow相似的Window
     * @param comparedWindow 待比较的window
     * @param similarity 要求相似度大于该值（取值为0-1）
     * @return 相似的window，不存在则返回null
     */
    TWindow findSimilarityWindow(TWindow comparedWindow, double similarity);

    /**
     * 将WindowTransaction中的内容保存在文件中
     * @param pathToSave 文件夹名，在这个文件夹中会创建多个文件
     */
    void writeToFile(String pathToSave);
}
