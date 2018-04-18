package edu.nju.autodroid.obfuscation;

import brut.androlib.Androlib;
import brut.androlib.AndrolibException;
import brut.androlib.ApkDecoder;
import brut.androlib.ApkOptions;
import brut.common.BrutException;
import brut.directory.DirectoryException;
import edu.nju.autodroid.utils.CmdExecutor;
import edu.nju.autodroid.utils.Configuration;
import org.dom4j.Attribute;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.DOMReader;
import org.dom4j.io.SAXReader;
import org.dom4j.io.XMLWriter;

import java.io.File;
import java.io.FileFilter;
import java.io.FileWriter;
import java.io.IOException;

/**
 * Created by ysht on 2018/3/27.
 * 将layout中的所有view镜像布局，当然也有可能打乱，可能会导致布局出现变化，因此一般仅用于攻击使用
 */
public class MirrorViewPositionObfuscator implements IObfuscator {

    static final String tempDir = "temp/MirrorViewPositionObfuscator/";

    static final String layoutDir = "/res/layout/";

    static final String apktool = "libs/apktool.bat";

    @Override
    public String getName() {
        return "MirrorViewPositionObfuscator";
    }

    @Override
    public boolean obfuscate(File inputApk, File outputPath) {
        if(inputApk == null || outputPath == null)
            return false;

        File tempDirFile = new File(tempDir);
        if(!tempDirFile.exists())
            tempDirFile.mkdirs();

        File extractDir = extractApk(inputApk, new File(tempDir + inputApk.getName()));
        if(extractDir == null){
            System.out.println("解压APK失败！" + inputApk.getName());
            return false;
        }

        File layoutDirFile = new File(extractDir.getPath() + layoutDir);
        if(!layoutDirFile.exists()){
            System.out.println("不存在layout文件夹！" + layoutDirFile.getPath());
            return false;
        }

        for (File xmlFile : layoutDirFile.listFiles(new FileFilter() {
            @Override
            public boolean accept(File pathname) {
                return pathname.getName().endsWith(".xml");
            }
        })){
            try {
                modifyXML(xmlFile);
            } catch (DocumentException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        if(buildApk(extractDir, outputPath))
        {
            signApk(outputPath);
            return true;
        }
        return false;
    }

    //返回解压的文件夹
    private File extractApk(File apkFile, File extractOutputFile){
        CmdExecutor.execCmd(apktool + " d -f -s -o " + Configuration.toLegalPath(extractOutputFile.getPath()) + " " + Configuration.toLegalPath(apkFile.getPath()));
        return extractOutputFile;
    }

    private boolean buildApk(File projectDir, File outputFile){
        CmdExecutor.execCmd(apktool + " b -f -o " + Configuration.toLegalPath(outputFile.getPath()) + " " + Configuration.toLegalPath(projectDir.getPath()));
        return true;
    }

    private void modifyXML(File xmlFile) throws DocumentException, IOException {
        SAXReader reader = new SAXReader ();
        Document document = reader.read(xmlFile);
        Element root = document.getRootElement();
        for (Element ele : root.elements()){
            recursiveModifyNode(ele);
        }

        FileWriter writer = new FileWriter(xmlFile);
        XMLWriter xmlWriter = new XMLWriter(writer);
        xmlWriter.write(document);
        xmlWriter.close();
    }

    private void recursiveModifyNode(Element element){
        if(element == null)
            return;
        Attribute attr = element.attribute("gravity");
        if(attr == null){
            element.addAttribute("android:gravity", "right");
        }
        else{
            attr.setValue(getOppositeGravity(attr.getText()));
        }

        for(Element child : element.elements()){
            recursiveModifyNode(child);
        }
    }

    private String getOppositeGravity(String gravity){
        if(gravity.equals("left"))
            return "right";
        if(gravity.equals("right"))
            return "left";

        //其余情况暂时不考虑
        return gravity;
    }

    private void signApk(File apkFile){
        CmdExecutor.execCmd(Configuration.getJarSignerPath() + " -sigalg SHA1withRSA -digestalg SHA1 -keystore tools/ks_1234asdf -storepass 1234asdf -keypass 1234asdf " + Configuration.toLegalPath(apkFile.getPath()) + " 1234asdf");
    }
}
