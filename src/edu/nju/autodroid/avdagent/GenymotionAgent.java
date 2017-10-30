package edu.nju.autodroid.avdagent;

/**
 * Created by ysht on 2016/5/6 0006.
 */
public class GenymotionAgent implements IAvdAgent {
    @Override
    public boolean createAvd(AvdTarget target, AvdDevice device, String avdName) {
        return false;
    }

    @Override
    public boolean cleanAvdImg(String avdName) {
        return false;
    }

    @Override
    public boolean deleteAvd(String avdName) {
        return false;
    }

    @Override
    public boolean startAvd(String avdName) {
        return false;
    }
}
