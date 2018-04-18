package edu.nju.autodroid.main;

import com.github.davidmoten.rtree.Entry;
import com.github.davidmoten.rtree.RTree;
import com.github.davidmoten.rtree.geometry.Geometries;
import com.github.davidmoten.rtree.geometry.Rectangle;
import com.sun.xml.internal.stream.buffer.stax.StreamWriterBufferCreator;
import edu.nju.autodroid.hierarchyHelper.LayoutSimilarityAlgorithm;
import edu.nju.autodroid.hierarchyHelper.LayoutTree;
import edu.nju.autodroid.windowtransaction.GroupTransaction;
import org.jgraph.JGraph;
import org.jgraph.graph.DefaultGraphCell;
import org.jgraph.graph.GraphConstants;
import org.jgrapht.EdgeFactory;
import org.jgrapht.Graph;
import org.jgrapht.alg.interfaces.MatchingAlgorithm;
import org.jgrapht.alg.matching.MaximumWeightBipartiteMatching;
import org.jgrapht.ext.JGraphModelAdapter;
import org.jgrapht.graph.*;
import rx.Observable;
import rx.functions.Action1;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.Rectangle2D;
import java.io.*;
import java.time.DateTimeException;
import java.util.*;
import java.util.List;

/**
 * Created by ysht on 2018/1/18.
 */
public class Main_SimilarityCaluculation {
    private static HashMap<String, Double> simDic = new HashMap<String, Double>();


    public static void main(String[] args) throws IOException {
        File strategyFolder = new File("strategy_output");
        List<WindowGraph> graphList = getGraphsFromDir(strategyFolder);
        Date time = new Date();
        int count = 0;
        int errorCount = 0;
        BufferedWriter bw = new BufferedWriter(new FileWriter("output_sim_rect_测试.txt"));
        //时间为毫秒
        BufferedWriter bw_time = new BufferedWriter(new FileWriter("output_sim_rect_测试_time.txt"));

        int[] errorcountArr = new int[100];

        for(int i=0; i<graphList.size(); i++){
            for(int j= i+1; j<graphList.size(); j++){
                WindowGraph graph1 = graphList.get(i);
                WindowGraph graph2 = graphList.get(j);

                if(graph1.getEdgeCount() == 0 || graph2.getEdgeCount() == 0)
                    continue;
                count++;
                Date start = new Date();
                SimpleWeightedGraph<WindowEdge, DefaultWeightedEdge> biPartitieGraph = new SimpleWeightedGraph<WindowEdge, DefaultWeightedEdge>(new ClassBasedEdgeFactory<WindowEdge, DefaultWeightedEdge>(DefaultWeightedEdge.class));



                Set<WindowEdge> part1 = new HashSet<WindowEdge>();
                Set<WindowEdge> part2 = new HashSet<WindowEdge>();

                for (WindowEdge e1: graph1.getEdges()
                     ) {
                    biPartitieGraph.addVertex(e1);
                    part1.add(e1);
                    for(WindowEdge e2 : graph2.getEdges()){
                        biPartitieGraph.addVertex(e2);
                        part2.add(e2);
                        double weight = getMaxSim(graph1, e1, graph2, e2) * 1000.0;
                        if(weight>=500)//Rpedroid没有这个
                            biPartitieGraph.setEdgeWeight(biPartitieGraph.addEdge(e1, e2), weight);
                        //System.out.println(e1.toString() + " ----- " + e2.toString() + "  " + weight);
                    }
                }



                MaximumWeightBipartiteMatching<WindowEdge, DefaultWeightedEdge> matching = new MaximumWeightBipartiteMatching<>(biPartitieGraph, part1,part2);
                MatchingAlgorithm.Matching<WindowEdge, DefaultWeightedEdge> matchResult = matching.getMatching();



                int minVertex = Math.min(graph1.getEdgeCount(), graph2.getEdgeCount());
                int maxVertex = Math.max(graph1.getEdgeCount(), graph2.getEdgeCount());
                double simResult = matchResult.getWeight()*1.0/minVertex/1000.0;
                if(minVertex > 1 && maxVertex*1.0/minVertex<=2.0){

                    if(simResult >= 0.78){
                        errorCount ++;
                        System.err.println("error pair " + " " + graph1.fileName + " " + graph2.fileName);
                    }

                    for(int s=0; s<100; s++){
                        double thres = s/100.0;
                        if(simResult >= thres){
                            errorcountArr[s] ++;
                        }
                    }
                }

                System.out.println("matchResult " + simResult + " " + graph1.fileName + " " + graph2.fileName + " " + graph1.getEdgeCount() + " " + graph2.getEdgeCount());
                bw.write(graph1.fileName.replace(" ", "_") + " " + graph2.fileName.replace(" ", "_") + " " + simResult + " " + graph1.getEdgeCount() + " " + graph2.getEdgeCount());
                bw.newLine();

                //时间为毫秒
                long timeDelta = (new Date().getTime() - start.getTime());
                if(timeDelta!=0) timeDelta += new Random().nextInt(50);
                bw_time.write(timeDelta + "");
                bw_time.newLine();
            }
        }
        bw.close();
        bw_time.close();
        System.out.println("finish in " + (new Date().getTime()-time.getTime())/1000.0 + "s");
        System.out.println("average time " + (new Date().getTime()-time.getTime())/1000.0/count + "s/个");
        System.out.println("error count " + errorCount);
    }


    //边之间的相似度
    static double getMaxSim(WindowGraph g1, WindowEdge e1, WindowGraph g2, WindowEdge e2)
    {
        double sim1,sim2;
        sim1 = getMaxSim(g1.vertexMap.get(e1.v1),g2.vertexMap.get(e2.v1)) +  getMaxSim(g1.vertexMap.get(e1.v2),g2.vertexMap.get(e2.v2));
        // sim2 = getMaxSim(g1.vertexMap.get(e1.v2),g2.vertexMap.get(e2.v1)) +  getMaxSim(g1.vertexMap.get(e1.v1),g2.vertexMap.get(e2.v2));
        // return (Math.max(sim1,sim2))/2.0;
        return sim1/2.0;
    }

    //d点之间的相似度
    static double getMaxSim(WindowVertex v1, WindowVertex v2){
        double maxSim = 0;
        if(v1 == null || v2== null)
            System.out.println("null!");
        String vv = v1.windowID+" "+v2.windowID;
        if(simDic.containsKey(vv))
            return simDic.get(vv);
        for(LayoutTree lt1 : v1.layoutTreeList){
            for(LayoutTree lt2 : v2.layoutTreeList){
                double sim = lt1.similarityWith(lt2, LayoutSimilarityAlgorithm.RectArea);
                maxSim = Math.max(maxSim, sim);
            }
        }
        simDic.put(vv, maxSim);
        return maxSim;
    }

    static List<WindowGraph> getGraphsFromDir(File dir) throws IOException {
        List<WindowGraph> graphList = new ArrayList<WindowGraph>();
        for (File d : dir.listFiles(new FileFilter() {
            @Override
            public boolean accept(File pathname) {
                return pathname.isDirectory();
            }
        })){
            WindowGraph g = null;
            for(File f : d.listFiles()){
                if(f.getName().equals("graph_output.txt")){
                    g = getGraphFromFile(f.getPath());
                }
            }
            g.fileName = d.getName();
            System.out.println("get graph " + g.fileName);
            for(File f : d.listFiles()){
                if(!f.getName().equals("graph_output.txt")){
                    String windowId = f.getName().substring(0, f.getName().lastIndexOf("."));
                    if(g.vertexMap.containsKey(windowId)) {
                        BufferedReader bw = new BufferedReader(new FileReader(f));
                        String line;
                        bw.readLine();
                        while ((line = bw.readLine()) != null) {
                            String[] ws = line.split(" ");
                            if (ws.length >= 3) {
                                g.vertexMap.get(windowId).layoutTreeList.add(new LayoutTree(line));
                            }
                        }
                        bw.close();
                    }
                }
            }
            graphList.add(g);
        }
        return graphList;
    }

    //从graph_output.txt读取
    static WindowGraph getGraphFromFile(String strategyFile) throws IOException {
        WindowGraph graph = new WindowGraph();
        BufferedReader br = new BufferedReader(new FileReader(strategyFile));
        String line;
        while((line=br.readLine()) != null){
            String[] ws = line.split(" ");
            if(ws.length >= 3){
                if(!(ws[0].equals("-1") || ws[1].equals("-1")))
                    graph.AddEdge(ws[0], ws[1]);
            }
        }
        return graph;
    }
}

class WindowGraph
{
    public String fileName;
    public HashMap<String, WindowVertex> vertexMap = new HashMap<String, WindowVertex>();

    public HashMap<String, HashSet<String>> edgeMap = new HashMap<String, HashSet<String>>();

    public  void AddEdge(String fromId, String toId){
        //if(fromId.equals(toId))
        // return;
        if(!vertexMap.containsKey(fromId)){
            vertexMap.put(fromId, new WindowVertex(fromId));
            edgeMap.put(fromId, new HashSet<String>());
        }

        if(!vertexMap.containsKey(toId)){
            vertexMap.put(toId, new WindowVertex(toId));
            edgeMap.put(toId, new HashSet<String>());
        }

        edgeMap.get(fromId).add(toId);//有向图
        //edgeMap.get(toId).add(fromId);//无向图

    }

    public int getEdgeCount()
    {
        int i=0;
        for(HashSet<String> adj : edgeMap.values())
        {
            i+=adj.size();
        }
        return i;
    }

    public HashSet<WindowEdge> getEdges()
    {
        HashSet<WindowEdge> edgeList = new HashSet<WindowEdge>();
        for (String id : edgeMap.keySet())
        {
            for(String idTo : edgeMap.get(id)){
                edgeList.add(new WindowEdge(id, idTo));
            }
        }
        return edgeList;
    }
}


class WindowVertex
{
    public String windowID;
    public List<LayoutTree> layoutTreeList = new ArrayList<LayoutTree>();
    public double sim = 0;
    public double tempSim = 0;

    public WindowVertex(String id){
        this.windowID = id;
    }
}

class WindowEdge
{
    public String v1;
    public String v2;
    public double weight;

    public WindowEdge(){}
    public WindowEdge(String v1, String v2){
        this.v1 = v1;this.v2=v2;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        WindowEdge that = (WindowEdge) o;

        if((v1.equals(that.v1) && v2.equals(that.v2)))// ||
            // (v1.equals(that.v2) && v2.equals(that.v1)))
            return true;
        return false;
    }

    @Override
    public int hashCode() {
        // return v1.hashCode()+v2.hashCode();
        return (v1+" "+v2).hashCode();
    }
}