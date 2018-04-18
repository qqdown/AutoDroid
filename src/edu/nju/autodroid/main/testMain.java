package edu.nju.autodroid.main;

import com.android.ddmlib.*;
import com.github.davidmoten.rtree.RTree;
import com.github.davidmoten.rtree.geometry.Rectangle;
import edu.nju.autodroid.androidagent.AdbAgent;
import edu.nju.autodroid.androidagent.IAndroidAgent;
import edu.nju.autodroid.avdagent.AvdAgent;
import edu.nju.autodroid.avdagent.IAvdAgent;
import edu.nju.autodroid.hierarchyHelper.LayoutNode;
import edu.nju.autodroid.hierarchyHelper.LayoutTree;
import edu.nju.autodroid.hierarchyHelper.TreeSearchOrder;
import edu.nju.autodroid.strategy.DepthGroupWeightedStrategy;
import edu.nju.autodroid.strategy.IStrategy;
import edu.nju.autodroid.strategy.SimpleWindowRandomSelectionStrategy;
import edu.nju.autodroid.uiautomator.UiautomatorClient;
import edu.nju.autodroid.utils.AdbTool;
import edu.nju.autodroid.utils.Configuration;
import edu.nju.autodroid.utils.Logger;

import javax.swing.tree.TreeNode;
import java.io.*;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.function.Predicate;

/**
 * Created by ysht on 2016/4/19 0019.
 */
public class testMain {
    public static void main(String[] args) throws TimeoutException, AdbCommandRejectedException, ShellCommandUnresponsiveException, IOException, InterruptedException {

        BufferedReader br = new BufferedReader(new FileReader("dump.uix"));

        String xml = br.readLine();
        br.close();

        LayoutTree layoutTree = new LayoutTree(xml);

        RTree<LayoutNode, Rectangle> layoutRTree = layoutTree.createRTree();
        layoutRTree.visualize(1080, 1920).save("rtree.png");
        System.out.println(layoutRTree.asString());
    }
}
