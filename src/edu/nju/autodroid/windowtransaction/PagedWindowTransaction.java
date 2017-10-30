package edu.nju.autodroid.windowtransaction;

import edu.nju.autodroid.hierarchyHelper.Action;
import edu.nju.autodroid.hierarchyHelper.LayoutTree;
import edu.nju.autodroid.utils.Logger;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

/**
 * Created by ysht on 2016/5/25 0025.
 */
public class PagedWindowTransaction implements IWindowTransaction<PagedWindow> {

    private WTGraph<PagedWindow> graph = new WTGraph<>();

    @Override
    public void addWindow(PagedWindow window) {
        WTVertex vertex = new WTVertex(window);
        graph.addVertex(vertex);
    }

    @Override
    public void addTransaction(String fromId, String toId, Action action) {
        WTEdge edge = new WTEdge(fromId, toId, action);
        graph.addEdge(edge);
    }

    @Override
    public PagedWindow getWindow(String id) {
        WTVertex<PagedWindow> vertex = graph.getVertex(id);
        if (vertex == null)
            return null;
        return vertex.getWindow();
    }

    @Override
    public int getWindowSize() {
        return graph.getVertexSize();
    }

    @Override
    public PagedWindow[] getWindows() {
        PagedWindow[] windows = new PagedWindow[graph.getVertexSize()];
        WTVertex<PagedWindow>[] vertices = graph.getVertexs();
        for (int i = 0; i < windows.length; i++) {
            windows[i] = vertices[i].getWindow();
        }
        return windows;
    }

    @Override
    public List<Transaction<PagedWindow>> getTransactions() {
        List<Transaction<PagedWindow>> transactionList = new ArrayList<Transaction<PagedWindow>>();
        for(WTVertex<PagedWindow> v : graph.getVertexs()){
            HashSet<WTEdge> wtEdgeHashSet = graph.getEdge(v.getId());
            if(wtEdgeHashSet != null){
                for(WTEdge edge : wtEdgeHashSet){
                    Transaction<PagedWindow> transaction = new Transaction<PagedWindow>(graph.getVertex(edge.getFromId()).getWindow(), graph.getVertex(edge.getToId()).getWindow(), edge.getActions());
                    transactionList.add(transaction);
                }
            }

        }
        return transactionList;
    }

    public int getTotalEdgeCount(){
        int count = 0;
        for(WTVertex<PagedWindow> v : graph.getVertexs()){
            count += graph.getEdge(v.getId()).size();
        }
        return count;
    }

    @Override
    public PagedWindow findSimilarityWindow(PagedWindow comparedWindow, double similarity) {
        return null;
    }

    @Override
    public void writeToFile(String pathToSave) {
        try {
            File directory = new File(pathToSave);
            if (directory.exists())
                directory.delete();
            directory.mkdirs();
            WTVertex<PagedWindow>[] vertexs = graph.getVertexs();
            File graphFile = new File(directory.getAbsolutePath() + "\\graph_output.txt");
            if (graphFile.exists()) {
                graphFile.delete();
            }
            graphFile.createNewFile();
            FileWriter graphFw = new FileWriter(graphFile);
            BufferedWriter graphBw = new BufferedWriter(graphFw);

            for (WTVertex<PagedWindow> vertex : vertexs) {
                if (vertex == null)
                    continue;
                ;
                //以邻接表的形式输出graph
                HashSet<WTEdge> edges = graph.getEdge(vertex.getId());
                if (edges != null) {
                    for (WTEdge edge : edges) {
                        graphBw.write(edge.getFromId() + " " + edge.getToId());
                        HashSet<Action> actions = edge.getActions();
                        for (Action action : actions) {
                            graphBw.write(" " + action.actionType.toString());
                        }
                        graphBw.newLine();
                    }
                }
                //输出window
                if (vertex.getWindow() != null && vertex.getWindow().getLayout() != null) {
                    File file = new File(directory.getAbsolutePath() + "\\" + vertex.getWindow().getId() + ".txt");
                    if (file.exists()) {
                        file.delete();
                    }
                    file.createNewFile();
                    //输出window内容
                    FileWriter fw = new FileWriter(file);
                    BufferedWriter bw = new BufferedWriter(fw);
                    if (vertex.getWindow() != null) {
                        bw.write(vertex.getWindow().getActivityName());
                        bw.newLine();
                        for(LayoutTree lt : vertex.getWindow().getLayoutList()){
                            bw.write(lt.getLayoutXML());
                            bw.newLine();
                        }
                    }

                    bw.close();
                    fw.close();
                }
            }

            graphBw.close();
            graphFw.close();
            Logger.logInfo("保存完成！");

        } catch (IOException e) {
            e.printStackTrace();
            Logger.logException(e);
        }
    }

}
