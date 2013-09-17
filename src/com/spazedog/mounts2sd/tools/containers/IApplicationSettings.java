package com.spazedog.mounts2sd.tools.containers;

import com.spazedog.mounts2sd.tools.Preferences.IPersistentPreferences;

public class IApplicationSettings {
	protected IPersistentPreferences mPersistentStorage;

	public Boolean use_builtin_busybox() {
		return mPersistentStorage.getBoolean("use_builtin_busybox", true);
	}
	
	public void use_builtin_busybox(Boolean aValue) {
		mPersistentStorage.edit().putBoolean("use_builtin_busybox", aValue).commit();
	}
	
	public Boolean use_dark_theme() {
		return mPersistentStorage.getBoolean("use_dark_theme", false);
	}
	
	public void use_dark_theme(Boolean aValue) {
		mPersistentStorage.edit().putBoolean("use_dark_theme", aValue).commit();
	}

	public Boolean use_tablet_settings() {
		return mPersistentStorage.getBoolean("use_tablet_settings", false);
	}
	
	public void use_tablet_settings(Boolean aValue) {
		mPersistentStorage.edit().putBoolean("use_tablet_settings", aValue).commit();
	}
	
	public Boolean use_global_settings_style() {
		return mPersistentStorage.getBoolean("use_global_settings_style", false);
	}
	
	public void use_global_settings_style(Boolean aValue) {
		mPersistentStorage.edit().putBoolean("use_global_settings_style", aValue).commit();
	}
}
