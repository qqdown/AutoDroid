package edu.nju.autodroid.windowtransaction;

import edu.nju.autodroid.hierarchyHelper.*;
import edu.nju.autodroid.utils.Logger;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

/**
 * Created by ysht on 2016/3/7 0007.
 */
public class SimpleWindowTransaction implements IWindowTransaction<SimpleWindow> {
    private WTGraph<SimpleWindow> graph = new WTGraph<SimpleWindow>();

    @Override
    public void addWindow(SimpleWindow window) {
        WTVertex vertex = new WTVertex(window);
        graph.addVertex(vertex);
    }

    @Override
    public void addTransaction(String fromId, String toId, Action action) {
        WTEdge edge = new WTEdge(fromId, toId, action);
        graph.addEdge(edge);
    }

    @Override
    public SimpleWindow getWindow(String id) {
        WTVertex<SimpleWindow> vertex = graph.getVertex(id);
        if(vertex == null)
            return null;
        return vertex.getWindow();
    }

    @Override
    public int getWindowSize() {
        return graph.getVertexSize();
    }

    @Override
    public SimpleWindow[] getWindows() {
        SimpleWindow[] windows = new SimpleWindow[graph.getVertexSize()];
        WTVertex<SimpleWindow>[] vertices = graph.getVertexs();
        for(int i=0; i<windows.length; i++){
            windows[i] = vertices[i].getWindow();
        }
        return windows;
    }

    @Override
    public List<Transaction<SimpleWindow>> getTransactions() {
        List<Transaction<SimpleWindow>> transactionList = new ArrayList<Transaction<SimpleWindow>>();
        for(WTVertex<SimpleWindow> v : graph.getVertexs()){

            for(WTEdge edge : graph.getEdge(v.getId())){
                Transaction<SimpleWindow> transaction = new Transaction<SimpleWindow>(graph.getVertex(edge.getFromId()).getWindow(), graph.getVertex(edge.getToId()).getWindow(), edge.getActions());
                transactionList.add(transaction);
            }
        }
        return transactionList;
    }

    @Override
    public SimpleWindow findSimilarityWindow(SimpleWindow comparedWindow, double similarity) {
        return null;
    }

    @Override
    public void writeToFile(String pathToSave) {

        try {
            File directory = new File(pathToSave);
            if (directory.exists())
                directory.delete();
            directory.mkdirs();
            WTVertex[] vertexs = graph.getVertexs();
            File graphFile = new File(directory.getAbsolutePath() + "\\graph_output.txt");
            if(graphFile.exists()){
                graphFile.delete();
            }
            graphFile.createNewFile();
            FileWriter graphFw = new FileWriter(graphFile);
            BufferedWriter graphBw = new BufferedWriter(graphFw);

            for ( WTVertex vertex : vertexs) {
                if(vertex == null)
                    continue;;
                //以邻接表的形式输出graph
                HashSet<WTEdge> edges = graph.getEdge(vertex.getId());
                if(edges != null)
                {
                    for(WTEdge edge : edges){
                        graphBw.write(edge.getFromId()+" "+edge.getToId());
                        HashSet<Action> actions = edge.getActions();
                        for(Action action : actions){
                            graphBw.write(" " + action.actionType.toString());
                        }
                        graphBw.newLine();
                    }
                }
                if(vertex.getWindow() != null && vertex.getWindow().getLayout() != null) {
                    File file = new File(directory.getAbsolutePath() + "\\" + vertex.getWindow().getId() + ".txt");
                    if (file.exists()) {
                        file.delete();
                    }
                    file.createNewFile();
                    //输出window内容
                    FileWriter fw = new FileWriter(file);
                    BufferedWriter bw = new BufferedWriter(fw);
                    if(vertex.getWindow() != null)
                    {
                        bw.write(vertex.getWindow().getActivityName());
                        bw.newLine();
                        bw.write(vertex.getWindow().getLayout().getLayoutXML());
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
