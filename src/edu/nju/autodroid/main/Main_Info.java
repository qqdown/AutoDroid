package edu.nju.autodroid.main;

import edu.nju.autodroid.utils.AdbTool;

import java.io.*;

/**
 * Created by ysht on 2016/11/19.
 */
public class Main_Info {
    public static void main(String[] args) throws IOException {
        String dirPath = "E:\\APKs\\Wandoujia\\合集";

        File dir = new File(dirPath);
        File[] apks = dir.listFiles(new FileFilter() {
            @Override
            public boolean accept(File pathname) {
                return pathname.getName().endsWith(".apk");
            }
        });

        System.out.println("获取Apk" + apks.length + "个");
        BufferedWriter bw = new BufferedWriter(new FileWriter("outputInfo.txt"));
        for(File apk : apks){
            String fileName = apk.getName().substring(0, apk.getName().length()-4);
            String packageName = AdbTool.getPackageFromApk(apk.getAbsolutePath());
            String output = fileName + " " + packageName;
            System.out.println(output);
            bw.write(output);
            bw.newLine();
        }
        bw.close();
    }
}
