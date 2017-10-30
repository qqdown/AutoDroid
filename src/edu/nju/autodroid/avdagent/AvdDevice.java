package edu.nju.autodroid.avdagent;

import edu.nju.autodroid.utils.Utils;
import org.omg.CORBA.Environment;

import java.util.List;

/**
 * Created by ysht on 2016/4/19 0019.
 */
public class AvdDevice {
    public int id;
    public String name;

    private AvdDevice(){}

    public AvdDevice(int id, String name){
        this.id = id;
        this.name = name;
    }

    public static AvdDevice parseFrom(String parsedString){
        AvdDevice device = new AvdDevice();
        String[] lines = parsedString.split("\n");
        for(String line : lines){
            line = line.trim();
            if(line.startsWith("id:")){
                device.id = Utils.parseInt(line.substring(3));
            }
            else if(line.startsWith("Name:")){
                device.name = line.substring(5).trim();
            }

        }
        return device;
    }

    @Override
    public String toString() {
        return String.format("id:%d\nName:%s", id, name);
    }
}
