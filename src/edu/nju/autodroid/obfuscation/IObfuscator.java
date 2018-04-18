package edu.nju.autodroid.obfuscation;

import java.io.File;

/**
 * Created by ysht on 2018/3/27.
 */
public interface IObfuscator {
    String getName();

    boolean obfuscate(File inputApk, File outputPath);
}
