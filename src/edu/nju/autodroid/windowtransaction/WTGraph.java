package edu.nju.autodroid.windowtransaction;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;

/**
 * Created by ysht on 2016/3/7 0007.
 */
public class WTGraph<TWindow extends IWindow>{
    //id与Vertex的字典，实际上是id与Vertex中Window的字典
    private HashMap<String, WTVertex<TWindow>> vertexHashMap = new HashMap<String, WTVertex<TWindow>>();
    //邻接表，用于保存所有的邻接边,id（String）为Vertex的id
    private HashMap<String, HashSet<WTEdge>> edgeHashMap = new HashMap<String, HashSet<WTEdge>>();

    /**
     * 添加节点，以节点id为key，如果已经存在该id的节点，则原节点将被替换
     * @param vertex 添加的节点
     */
    public void addVertex(WTVertex<TWindow> vertex){
        vertexHashMap.put(vertex.getId(), vertex);
    }

    public void addEdge(WTEdge edge){
        if(vertexHashMap.containsKey(edge.fromId) && vertexHashMap.containsKey(edge.toId)){//存在该vertex才可以添加边
            if(!edgeHashMap.containsKey(edge.fromId))
                edgeHashMap.put(edge.fromId, new HashSet<WTEdge>());
            HashSet<WTEdge> edges = edgeHashMap.get(edge.fromId);
            Iterator<WTEdge> it = edges.iterator();
            while(it.hasNext()){
                WTEdge e = it.next();
                if(e.equals(edge)){//遇到相同边就合并
                    e.addActions(edge.getActions());
                    return;
                }
            }
            //否则添加一个新边
            edgeHashMap.get(edge.fromId).add(edge);
        }
        else {
            System.err.println("addEdge error! edge (" + edge.fromId + ","+ edge.toId + ")不存在！");
        }
    }

    public boolean containsVertex(String id){
        return vertexHashMap.containsKey(id);
    }

    public WTVertex<TWindow> getVertex(String id){
        return vertexHashMap.get(id);
    }

    public WTVertex<TWindow>[] getVertexs(){
        return (WTVertex<TWindow>[])(vertexHashMap.values().toArray(new WTVertex[0]));
    }

    /**
     * 获取图节点个数
     * @return 节点个数
     */
    public int getVertexSize(){
        return vertexHashMap.size();
    }

    public HashSet<WTEdge> getEdge(String vertexId){
        return edgeHashMap.get(vertexId);
    }
}
