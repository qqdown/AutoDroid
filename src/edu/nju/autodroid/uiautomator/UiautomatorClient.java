package edu.nju.autodroid.uiautomator;


import com.android.uiautomator.testrunner.UiAutomatorTestCase;


import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * Created by ysht on 2016/3/12 0012.
 */
public class UiautomatorClient extends UiAutomatorTestCase {
public static int PHONE_PORT = 22223;//this will be changed by file io

    public static void start(String deviceName, int port){
        String jarName = "autodroidclient";
        String testClass = "edu.nju.autodroid.uiautomator.UiautomatorClient";
        String testName = "testClientMain";
        String androidId = "6";
        UiAutomatorHelper automatorHelper = new UiAutomatorHelper(deviceName, jarName, testClass, testName, androidId);
    }

    public void testClientMain() throws IOException {
        Socket client = null;
        ServerSocket server = new ServerSocket(PHONE_PORT);

        while(true){
            System.out.println("Waiting for new server! Waiting port is: " + PHONE_PORT);
            try {
                client = server.accept();
                System.out.println("Get a connection from "
                        + client.getRemoteSocketAddress().toString());
                ObjectInputStream ois = new ObjectInputStream(
                        new BufferedInputStream(client.getInputStream()));

                ObjectOutputStream oos = new ObjectOutputStream(
                        new BufferedOutputStream(client.getOutputStream()));
                while(true){
                    Command cmd = (Command)ois.readObject();
                    //System.out.println("Get command " + cmd.cmd);
                    Command backCmd = CommandHandler.Handle(cmd);
                    oos.writeObject(backCmd);
                    oos.flush();
                    oos.reset();
                    //System.out.println("Send command " + backCmd.cmd);
                }
            } catch (Exception e){
                e.printStackTrace();
                System.out.println("UiautomatorClient testClientMain: client error " + e.getMessage());
            }
        }
    }
}
