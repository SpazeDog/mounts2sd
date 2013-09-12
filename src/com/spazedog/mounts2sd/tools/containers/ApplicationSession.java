package com.spazedog.mounts2sd.tools.containers;

public class ApplicationSession {
	private static Boolean mUnlocked = false;
	
	public void is_unlocked(Boolean status) {
		mUnlocked = status;
	}
	
	public Boolean is_unlocked() {
		return mUnlocked;
	}
}
