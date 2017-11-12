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

    /**
     * KM2算法求解二分图最大匹配
     * @param maxsum 求最大匹配还是最小匹配
     * @param weight 权重，weight[][j]代表图1节点i和图2节点j之间的权重，注意，图1和图2的节点必须个数相同，如果不同，则补0
     * @param match 匹配结果，match[i]代表图1节点match[i]和图2节点i之间存在匹配
     * @return 最大匹配和
     */
    public static double biGraph(boolean maxsum, double[][] weight, int[] match){
        int i,j;

        int m = weight.length;
        if(m==0)
            return 0;
        int n = weight[0].length;
        double[] lx = new double[m],ly = new double[n];
        //int[] match = new int[n];
        if(!maxsum){
            for(i=0; i<m; i++){
                for(j=0; j<n; j++){
                    weight[i][j] = -weight[i][j];
                }
            }
        }

        // 初始化标号
        for (i = 0; i < m; i ++)
        {
            lx [i] = -0x1FFFFFFF;
            ly [i] = 0;
            for (j = 0; j < n; j ++)
                if (lx [i] < weight [i] [j])
                    lx [i] = weight [i] [j];
        }

        for(i=0; i<match.length; i++) match[i] = -1;
        boolean[] sx = new boolean[m], sy = new boolean[n];
        for (int u = 0; u < n; u ++)
            while (true)
            {
                for(i=0; i<sx.length; i++) sx[i] = false;
                for(i=0; i<sy.length; i++) sy[i] = false;
                if (path (u, sx, sy, lx,ly, weight, match))
                    break;
                // 修改标号
                //int dx = 0x7FFFFFFF;
                double dx = Double.MAX_VALUE;
                for (i = 0; i < m; i ++)
                    if (sx [i])
                        for (j = 0; j < n; j ++)
                            if(!sy [j])
                                dx = Math.min(lx[i] + ly [j] - weight [i] [j], dx);
                for (i = 0; i < m; i ++)
                {
                    if (sx [i])
                        lx [i] -= dx;
                    if (sy [i])
                        ly [i] += dx;
                }
            }
        double sum = 0;
        for (i = 0; i < m; i ++)
            sum += weight [match [i]] [i];
        if (!maxsum)
        {
            sum = -sum;
            for (i = 0; i < m; i ++)
                for (j = 0; j < n; j ++)
                    weight [i] [j] = -weight [i] [j];         // 如果需要保持 weight [ ] [ ] 原来的值，这里需要将其还原
        }
        return sum;
    }

    private static boolean path(int u, boolean[] sx, boolean[] sy, double[] lx, double[] ly, double[][] weight, int[] match){
        sx[u] = true;
        for(int v =0; v<sy.length; v++){
            if(!sy[v] && Math.abs(lx[u]+ly[v] - weight[u][v]) <= 0.00000001){//
                sy[v] = true;
                if(match[v] == -1 || path(match[v],sx,sy,lx,ly,weight,match)){
                    match[v] = u;
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * 获取两个矩形区域的重叠面积，bound四个数依次表示左上右下的位置，构成左上角和右下角坐标
     * @param bound1
     * @param bound2
     * @return 重叠面积
     */
    public static int getOverlapArea(int[] bound1, int[] bound2){
        if(bound1[0]>bound2[2] || bound1[2]<bound1[0] || bound1[1]>bound2[3] || bound1[3]<bound2[1])
            return 0;
        int overlapX = (bound1[2]-bound1[0])+(bound2[2]-bound2[0]) - ( Math.max(bound1[2],bound2[2]) - Math.min(bound1[0],bound2[0]) );
        int overlapY = (bound1[3]-bound1[1])+(bound2[3]-bound2[1]) - ( Math.max(bound1[3],bound2[3]) - Math.min(bound1[1],bound2[1]) );
        if(overlapX<0 || overlapY<0)
            return 0;
        return overlapX*overlapY;
    }

    /**
     * 获取两个矩形区域的归一化重叠面积，bound四个数依次表示左上右下的位置，构成左上角和右下角坐标
     * @param bound1
     * @param bound2
     * @return 归一化面积，即重叠面积/两个区域的最大值
     */
    public static double getNormalizedOverlapArea(int[] bound1, int[] bound2) {
        int overlapSize = getOverlapArea(bound1, bound2);
        if(overlapSize<=0)
            return  0;
        int size1 = (bound1[2]-bound1[0])*(bound1[3]-bound1[1]);
        int size2 = (bound2[2]-bound2[0])*(bound2[3]-bound2[1]);
        if(size1<=0 || size2<=0)
            return  0;
        return overlapSize*1.0/(Math.max(size1,size2));
    }

}
