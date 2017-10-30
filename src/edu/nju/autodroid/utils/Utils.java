package edu.nju.autodroid.utils;

import java.io.*;

public class Utils {

    //编辑距离算法，返回两个数组之间的编辑距离
    public static <T> int EditDistance(T[] arr1, T[] arr2){
        int n = arr1.length;
        int m = arr2.length;
        if(n==0)
            return m;
        if(m==0)
            return n;

        int[][] matrix = new int[n+1][m+1];
        for(int i=0; i<=n; i++)
        {
            matrix[i][0] = i;
        }
        for(int j=0; j<=m; j++)
        {
            matrix[0][j] = j;
        }

        for(int i=1; i<=n; i++){
            for(int j=1; j<=m; j++){
                int min = matrix[i-1][j-1] + (arr1[i-1].equals(arr2[j-1])?0:1);
                int temp = matrix[i-1][j] + 1;
                if(temp<min)
                    min = temp;
                temp = matrix[i][j-1] + 1;
                if(temp<min)
                    min = temp;
                matrix[i][j] = min;
            }
        }
        return matrix[n][m];
    }

    public static void copyFile(String oldPath, String newPath){
        copyFile(new File(oldPath), new File(newPath));
    }

    public static void copyFile(File oldFile, File newFile){
        try {
            if(!newFile.getParentFile().exists()){
                newFile.getParentFile().mkdirs();
            }
            int bytesum = 0;
            int byteread = 0;
            if (oldFile.exists()) { //文件存在时
                InputStream inStream = new FileInputStream(oldFile); //读入原文件
                FileOutputStream fs = new FileOutputStream(newFile);
                byte[] buffer = new byte[1444];
                while ( (byteread = inStream.read(buffer)) != -1) {
                    bytesum += byteread; //字节数 文件大小
                    fs.write(buffer, 0, byteread);
                }
                fs.close();
                inStream.close();
            }
        }
        catch (Exception e) {
            Logger.logError("复制文件"+oldFile.getPath()+"出错！");
            e.printStackTrace();
        }
    }

    public static void copyFolder(String oldFolderPath, String newFolderPath){
        File oldFolder = new File(oldFolderPath);
        File newFolder = new File(newFolderPath);
        if(!oldFolder.exists())
        {
            Logger.logError("复制文件出错，不存在文件夹" + oldFolderPath);
            return;
        }

        if(!newFolder.exists()){
            newFolder.mkdirs();
        }

        File[] files = oldFolder.listFiles();
        for(File file : files){
            if(file.isFile()){
                File targetFile = new File(newFolder.getAbsolutePath()+File.separator+file.getName());
                copyFile(file, targetFile);
            }
            else if(file.isDirectory()){
                File targetFolder = new File(newFolder.getAbsolutePath() + File.separator + file.getName());
                copyFolder(file.getAbsolutePath(), targetFolder.getAbsolutePath());
            }
        }
    }

    public static void writeText(String path, String content){
        File dirFile = new File(path);

        if (!dirFile.exists()) {
            dirFile.mkdir();
        }
        try {
            // new FileWriter(path + "t.txt", true) 这里加入true 可以不覆盖原有TXT文件内容 续写
            BufferedWriter bw1 = new BufferedWriter(new FileWriter(path));
            bw1.write(content);
            bw1.flush();
            bw1.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static int parseInt(String s){
        s = s.trim();
        if(s.length() == 0)
            throw new NumberFormatException("For empty string");
        int sign = 1;
        int startIndex = 0;
        if(s.charAt(0) == '+') {
            startIndex = 1;
            sign = 1;
        }
        else if(s.charAt(0) == '-'){
            startIndex = 1;
            sign = -1;
        }
        if(startIndex>=s.length() && !Character.isDigit(s.charAt(startIndex))){
            throw new NumberFormatException("For input string \"" + s + "\"");
        }
        int res = 0;
        for(int i=startIndex; i<s.length(); i++){
            if(!Character.isDigit(s.charAt(i)))
                break;
            res = res*10 + (s.charAt(i)-'0');
        }
        return res*sign;
    }
}
