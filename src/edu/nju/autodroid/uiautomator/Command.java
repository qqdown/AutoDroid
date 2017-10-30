package edu.nju.autodroid.uiautomator;

import java.io.Serializable;

import com.android.uiautomator.core.UiObject;

public class Command implements Serializable {
	private static final long serialVersionUID = 1L;
	
	public static final int cmdUnknown = 0x0000;
	
	public static final int cmdPressHome = 0x0001;
	public static final int cmdPressBack = 0x0002;
	
	public static final int cmdGetLayout = 0x1001;
	@Deprecated  //uiautomator不再使用这个命令
	public static final int cmdGetActivity = 0x1002;
	public static final int cmdGetPackage = 0x1003;
	
	public static final int cmdDoClick = 0x2001;
	public static final int cmdDoSetText = 0x2002;
	public static final int cmdDoLongClick = 0x2003;
	public static final int cmdDoClickAndWaitForNewWindow = 0x2004;
	
	public static final int cmdDoScrollBackward = 0x2101;
	public static final int cmdDoScrollForward = 0x2102;
	public static final int cmdDoScrollToEnd = 0x2103;
	public static final int cmdDoScrollToBeginning = 0x2104;
	public static final int cmdDoScrollIntoView = 0x2105;
	
	public Command(){
		params = null;
		objs = null;
	}
	
	public int cmd;
	public String[] params;
	public UiObject[] objs;
	
	@Override
	public String toString() {
		// TODO Auto-generated method stub
		String str = cmd + "";
		for(String p : params){
			str += " " + p;
		}
		return str;
	}
}
