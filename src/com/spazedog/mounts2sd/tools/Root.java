package com.spazedog.mounts2sd.tools;

import java.util.HashSet;
import java.util.Set;

import com.spazedog.lib.rootfw3.RootFW;
import com.spazedog.lib.rootfw3.extenders.InstanceExtender;
import com.spazedog.lib.rootfw3.extenders.InstanceExtender.InstanceCallback;

public class Root {
	private static InstanceExtender.Instance oInstance;
	
	private static Set<String> oLocks = new HashSet<String>();
	
	private static Boolean oConnected = false;
	
	static {
		/* Make sure that either our busybox or the ROM's has first priority above possible existing /sbin */
		RootFW.Config.PATH.add("/data/local");
		RootFW.Config.PATH.add("/system/xbin");
		
		RootFW.Config.LOG = RootFW.E_DEBUG|RootFW.E_ERROR|RootFW.E_INFO|RootFW.E_WARNING;
	}
	
	public static Boolean isConnected() {
		if (oInstance == null) {
			oInstance = RootFW.rootInstance();
			oConnected = oInstance.get().isConnected();
			
			oInstance.addCallback(new InstanceCallback() {
				@Override
				public void onConnect(RootFW instance) {
					oConnected = true;
				}
				
				@Override
				public void onDisconnect(RootFW instance) {
					oConnected = false;
				}
				
				@Override
				public void onFailed(RootFW instance) {
					oConnected = false;
				}
			});
			
		} else if (!oConnected) {
			oInstance.get().connect();
		}
		
		return oConnected;
	}
	
	public static RootFW open() {
		if (isConnected()) {
			return oInstance.get();
		}
		
		return null;
	}
	
	public static void close() {
		if (oLocks.size() == 0) {
			oInstance.get().disconnect();
		}
	}
	
	public static Boolean lock(String name) {
		if (isConnected() && !oLocks.contains(name)) {
			oLocks.add(name);
			oInstance.lock();
			
			return true;
		}
		
		return false;
	}
	
	public static Boolean unlock(String name) {
		if (oConnected && oLocks.contains(name)) {
			oLocks.remove(name);
			oInstance.unlock();
			
			return true;
		}
		
		return false;
	}
}
