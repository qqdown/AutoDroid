package edu.nju.autodroid.avdagent;

import java.io.File;

/**
 * Created by ysht on 2016/4/19 0019.
 */
public interface IAvdAgent {
    boolean createAvd(AvdTarget target,AvdDevice device, String avdName);

    boolean cleanAvdImg(String avdName);

    boolean deleteAvd(String avdName);

    boolean startAvd(String avdName);
}
