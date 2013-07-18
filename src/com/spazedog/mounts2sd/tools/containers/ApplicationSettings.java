/*
 * This file is part of the Mounts2SD Project: https://github.com/spazedog/mounts2sd
 *  
 * Copyright (c) 2013 Daniel Bergløv
 *
 * Mounts2SD is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.

 * Mounts2SD is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.

 * You should have received a copy of the GNU General Public License
 * along with Mounts2SD. If not, see <http://www.gnu.org/licenses/>
 */

package com.spazedog.mounts2sd.tools.containers;

import com.spazedog.mounts2sd.tools.Preferences;
import com.spazedog.mounts2sd.tools.Preferences.PersistentPreferences;

public class ApplicationSettings {
	
	private PersistentPreferences mStored;
	
	private final static String oPreferenceName = "AppSettings";
	
	public ApplicationSettings(Preferences aPref) {
		mStored = aPref.stored(oPreferenceName);
	}
	
	public Boolean use_builtin_busybox() {
		return mStored.getBoolean("use_builtin_busybox", true);
	}
	
	public void use_builtin_busybox(Boolean aValue) {
		mStored.putBoolean("use_builtin_busybox", aValue);
	}
	
	public Boolean use_dark_theme() {
		return mStored.getBoolean("use_dark_theme", false);
	}
	
	public void use_dark_theme(Boolean aValue) {
		mStored.putBoolean("use_dark_theme", aValue);
	}

	public Boolean use_tablet_settings() {
		return mStored.getBoolean("use_tablet_settings", false);
	}
	
	public void use_tablet_settings(Boolean aValue) {
		mStored.putBoolean("use_tablet_settings", aValue);
	}
	
	
	public Boolean use_global_settings_style() {
		return mStored.getBoolean("use_global_settings_style", false);
	}
	
	public void use_global_settings_style(Boolean aValue) {
		mStored.putBoolean("use_global_settings_style", aValue);
	}
}
