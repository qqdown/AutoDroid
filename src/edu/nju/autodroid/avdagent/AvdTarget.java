package edu.nju.autodroid.avdagent;

import edu.nju.autodroid.utils.Utils;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by ysht on 2016/4/19 0019.
 */
public class AvdTarget {
    public int id = -1;
    public String name;
    public int apiLevel;
    public List<String> ABIList;

    private AvdTarget(){}

    public AvdTarget(int id, String name){
        this.id = id;
        this.name = name;
        this.apiLevel = getApiLevel(name);
    }

    public static int getApiLevel(String name) {
        int index = name.lastIndexOf(':');
        if (index < 0)
            index = name.lastIndexOf('-');

        return Utils.parseInt(name.substring(index + 1));
    }

    @Override
    public String toString() {
        return String.format("id:%d\nName:%s\nAPI-Level:%d", id, name, apiLevel);
    }


    public static AvdTarget parseAvdTarget(String s){
        String[] lines = s.split("\n");
        AvdTarget avdTarget = new AvdTarget();
        for(String line : lines){
            line = line.trim();
            if(line.startsWith("id:")){
                avdTarget.id = Utils.parseInt(line.substring(3));
            }
            else if(line.startsWith("Name:")){
                avdTarget.name = line.substring(5).trim();
            }
            else if(line.startsWith("API level:")){
                avdTarget.apiLevel = Utils.parseInt(line.substring(10));
            }
            else if(line.startsWith("Based on Android")){
                int i = line.indexOf("API level");
                avdTarget.apiLevel = Utils.parseInt(line.substring(i+10));
            }
            else if(line.startsWith("Tag/ABIs :")){
                avdTarget.ABIList = new ArrayList<String>();
                String[] abis = line.substring(10).split(",");
                for(String abi : abis){
                    abi = abi.trim();
                    if(!abi.isEmpty())
                        avdTarget.ABIList.add(abi);
                }
            }
        }
        if(avdTarget.id < 0)
            return null;
        return avdTarget;
    }

}
