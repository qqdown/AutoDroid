package edu.nju.autodroid.utils;

import java.io.*;
import java.net.Socket;

import com.android.ddmlib.IDevice;

import edu.nju.autodroid.uiautomator.Command;
import edu.nju.autodroid.hierarchyHelper.LayoutNode;

public class UiAutomatorTool {
	private Socket mSocket;
	private IDevice mDevice;
	private ObjectOutputStream oos = null;
	private ObjectInputStream ois = null;

	public boolean initializeConnection(IDevice device, int localPort, int phonePort) {
		mDevice = device;
		if(mDevice == null){
			Logger.logError("设备为空！");
			return false;
		}
		try {
			mDevice.createForward(localPort, phonePort);
			mSocket = new Socket("localhost", localPort);
			Logger.logInfo("UiAutomatorTool initializeConnection创建成功！");
			
		} catch (Exception e) {
			Logger.logError("UiAutomatorTool initializeConnection: server error " + e.getMessage());
			return false;
		}
		
		return true;
	}
	
	public void terminateConnection(){
		try{			
			if(mSocket != null)
				mSocket.close();
		}catch (Exception e){
			e.printStackTrace();
		}
		
	}

	public void sendCommand(Command cmd){
		try {
			if(oos == null)
				oos =  new ObjectOutputStream(mSocket.getOutputStream());
			oos.writeObject(cmd);
		} catch (IOException e) {
			System.out.println("UiAutomatorTol sendCommand: server error " + e.getMessage());
		}
	}
	
	/**
	 * 接受并返回命令，该命令为阻塞函数，直到收到命令才返回。
	 * @return
	 */
	public Command receiveCommand(){
		 try {
			if(ois == null)
				ois = new ObjectInputStream(mSocket.getInputStream());
			 long milli = System.currentTimeMillis();
			 Command cmd = (Command)ois.readObject();
			 Logger.logInfo("Receive " + (System.currentTimeMillis()-milli)/1000.0 + "");
			 return cmd;
		} catch (ClassNotFoundException | IOException e) {
			System.out.println("UiAutomatorTol ReceiveCommand error: PC error " + e.getMessage());
			return null;
		}
	}
	
	/*
	 * 获取简单的命令结果
	 * 命令仅仅包含1个cmd，无任何其它参数
	 * 结果包含且仅包含1个String param
	 */
	private String getSimpleString(int cmdI){
		Command cmd = new Command();
		cmd.cmd = cmdI;
		sendCommand(cmd);
		cmd = receiveCommand();
		if(cmd.cmd != cmdI)
			return null;
		return cmd.params[0];
	}
	
	public void pressHome(){
		Command cmd = new Command();
		cmd.cmd = Command.cmdPressHome;
		sendCommand(cmd);
		cmd = receiveCommand();
	}
	
	public void pressBack(){
		Command cmd = new Command();
		cmd.cmd = Command.cmdPressBack;
		sendCommand(cmd);
		cmd = receiveCommand();
	}
	
	public String getLayout(){
		return getSimpleString(Command.cmdGetLayout);
	}
	
	@Deprecated
	public String getActivity(){
		return getSimpleString(Command.cmdGetActivity);
	}
	
	public String getPackage(){
		return getSimpleString(Command.cmdGetPackage);
	}
	
	public boolean doClick(LayoutNode btn){
		Command cmd = new Command();
		cmd.cmd = Command.cmdDoClick;
		cmd.params = new String[]{btn.indexXpath};
		sendCommand(cmd);
		cmd = receiveCommand();
		return Boolean.parseBoolean(cmd.params[0]);
	}
	
	public boolean doSetText(LayoutNode node, String content){
		Command cmd = new Command();
		cmd.cmd = Command.cmdDoSetText;
		cmd.params = new String[]{node.indexXpath, content};
		sendCommand(cmd);
		cmd = receiveCommand();
		return Boolean.parseBoolean(cmd.params[0]);
	}
	
	public boolean doLongClick(LayoutNode node){
		Command cmd = new Command();
		cmd.cmd = Command.cmdDoLongClick;
		cmd.params = new String[]{node.indexXpath};
		sendCommand(cmd);
		cmd = receiveCommand();
		return Boolean.parseBoolean(cmd.params[0]);
	}
	
	//默认为5秒
	public boolean doClickAndWaitForNewWindow(LayoutNode node){
		return doClickAndWaitForNewWindow(node, 5000);
	}
	
	public boolean doClickAndWaitForNewWindow(LayoutNode node, long timeout){
		Command cmd = new Command();
		cmd.cmd = Command.cmdDoClickAndWaitForNewWindow;
		cmd.params = new String[]{node.indexXpath, timeout+""};
		sendCommand(cmd);
		cmd = receiveCommand();
		return Boolean.parseBoolean(cmd.params[0]);
	}
	
	//默认55步 每步5ms
	public boolean doScrollBackward(LayoutNode node){
		return doScrollBackward(node, 55);
	}
	
	//每步5ms
	public boolean doScrollBackward(LayoutNode node, int steps){
		Command cmd = new Command();
		cmd.cmd = Command.cmdDoScrollBackward;
		cmd.params = new String[]{node.indexXpath, steps+""};
		sendCommand(cmd);
		cmd = receiveCommand();
		return Boolean.parseBoolean(cmd.params[0]);
	}

	//默认55步 每步5ms
	public boolean doScrollForward(LayoutNode node){
		return doScrollForward(node, 55);
	}

	//每步5ms
	public boolean doScrollForward(LayoutNode node, int steps){
		Command cmd = new Command();
		cmd.cmd = Command.cmdDoScrollForward;
		cmd.params = new String[]{node.indexXpath, steps+""};
		sendCommand(cmd);
		cmd = receiveCommand();
		return Boolean.parseBoolean(cmd.params[0]);
	}


	
	public boolean doScrollToEnd(LayoutNode node, int maxSwipes, int steps){
		Command cmd = new Command();
		cmd.cmd = Command.cmdDoScrollToEnd;
		cmd.params = new String[]{node.indexXpath, maxSwipes+"", steps+""};
		sendCommand(cmd);
		cmd = receiveCommand();
		return Boolean.parseBoolean(cmd.params[0]);
	}
	
	public boolean doScrollToBeginning(LayoutNode node, int maxSwipes, int steps){
		Command cmd = new Command();
		cmd.cmd = Command.cmdDoScrollToBeginning;
		cmd.params = new String[]{node.indexXpath, maxSwipes+"", steps+""};
		sendCommand(cmd);
		cmd = receiveCommand();
		return Boolean.parseBoolean(cmd.params[0]);
	}
	
	public boolean doScrollIntoView(LayoutNode node, LayoutNode viewObj){
		Command cmd = new Command();
		cmd.cmd = Command.cmdDoScrollIntoView;
		cmd.params = new String[]{node.indexXpath, viewObj.indexXpath};
		sendCommand(cmd);
		cmd = receiveCommand();
		return Boolean.parseBoolean(cmd.params[0]);
	}
}
