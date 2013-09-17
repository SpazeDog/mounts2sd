package com.spazedog.mounts2sd.tools.containers;

import com.spazedog.mounts2sd.tools.Preferences.IPersistentPreferences;

public class IPersistence {
	protected IPersistentPreferences mPersistentStorage;
	
	public Boolean get(String name) {
		return mPersistentStorage.getBoolean(name);
	}
	
	public void set(String name, Boolean value) {
		mPersistentStorage.edit().putBoolean(name, value).commit();
	}
}
