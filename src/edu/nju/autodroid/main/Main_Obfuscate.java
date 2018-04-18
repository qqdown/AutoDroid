package edu.nju.autodroid.main;

import edu.nju.autodroid.obfuscation.IObfuscator;
import edu.nju.autodroid.obfuscation.MirrorViewPositionObfuscator;

import java.io.File;
import java.io.FileFilter;

/**
 * Created by ysht on 2018/3/27.
 */
public class Main_Obfuscate {
    public static void main(String[] args){
        IObfuscator obfuscator = new MirrorViewPositionObfuscator();

        System.out.println("当前使用：" + obfuscator.getName());

        File dir = new File("E:\\APKs\\Fdroid\\Fdroid");

        File outputDir = new File("outputObfuscatedAPK/");
        if(!outputDir.exists())
            outputDir.mkdirs();

        for(File file : dir.listFiles(new FileFilter() {
            @Override
            public boolean accept(File pathname) {
                return pathname.getName().endsWith(".apk");
            }
        })){
            File outputPath = new File("outputObfuscatedAPK/" + file.getName());
            boolean result = obfuscator.obfuscate(file, outputPath);
            if(result)
                System.out.println(file.getName() + " 成功");
            else
                System.out.println(file.getName() + " 失败");
        }
    }
}
