package com.spazedog.mounts2sd.tools;

import com.spazedog.lib.rootfw3.RootFW;
import com.spazedog.lib.rootfw3.RootFW.ConnectionController;
import com.spazedog.lib.rootfw3.RootFW.ConnectionListener;

public class Root {
	
	private static Boolean mLocked = false;
	
	private static Boolean mIsConnected = false;
	
	static {
		/* Make sure that either our busybox or the ROM's has first priority above possible existing /sbin */
		RootFW.Config.PATH.add("/data/local");
		RootFW.Config.PATH.add("/system/xbin");
		
		RootFW.Config.LOG = RootFW.E_DEBUG|RootFW.E_ERROR|RootFW.E_INFO|RootFW.E_WARNING;
		RootFW.Config.Connection.TIMEOUT = 10000;
		
		RootFW.getSharedRoot().addInstanceController(new ConnectionController(){
			@Override
			public Boolean onConnectionEstablishing(RootFW instance) {
				return true;
			}

			@Override
			public Boolean onConnectionClosing(RootFW instance) {
				return !mLocked;
			}
		});
		
		RootFW.getSharedRoot().addInstanceListener(new ConnectionListener(){
			@Override
			public void onConnectionEstablished(RootFW instance) {
				mIsConnected = true;
			}

			@Override
			public void onConnectionFailed(RootFW instance) {
				mIsConnected = false;
			}

			@Override
			public void onConnectionClosed(RootFW instance) {
				mIsConnected = false;
			}
		});
		
		mIsConnected = RootFW.getSharedRoot().isConnected();
	}
	
	public static RootFW initiate() {
		return (RootFW) RootFW.getSharedRoot().addLock();
	}
	
	public static void release() {
		RootFW.getSharedRoot().removeLock().disconnect();
	}
	
	public static void lock() {
		mLocked = true;
	}
	
	public static void unlock() {
		mLocked = false;
	}
	
	public static Boolean isConnected() {
		return mIsConnected;
	}
}
