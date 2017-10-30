package edu.nju.autodroid.strategy;

/**
 * 策略接口
 * Created by ysht on 2016/3/7 0007.
 */
public interface IStrategy {
    /**
     * 获取当前策略的名字
     * @return 策略名
     */
    String getStrategyName();

    /**
     * 获取当前策略的介绍信息
     * @return 介绍信息
     */
    String getStrategyDescription();

    /**
     * 获取当前运行的包名
     * @return 包名
     */
    String getRuntimePackageName();

    /**
     * 开始策略
     * @return 策略是否成功
     */
    boolean run();

    /**
     * 将结果输出
     * @param fileName 输出文件名
     */
    void writeToFile(String fileName);
}
