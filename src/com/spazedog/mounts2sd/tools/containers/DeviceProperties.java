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

import java.util.ArrayList;

import com.spazedog.mounts2sd.tools.Preferences;
import com.spazedog.mounts2sd.tools.Preferences.PersistentPreferences;

public final class DeviceProperties {

	private PersistentPreferences mCached;
	
	private final static Object oLock = new Object();
	
	private final static String oPreferenceName = "DeviceProperties";
	
	private final static ArrayList<String> oUpdated = new ArrayList<String>();
	
	public final Integer STATE_OFF = 0;
	public final Integer STATE_ON = 1;
	public final Integer STATE_AUTO = 2;
	
	public DeviceProperties(Preferences aPref) {
		mCached = aPref.cached(oPreferenceName);
	}
	
	private void update(String aName, String aValue) {
		synchronized (oLock) {
			mCached.putString(aName, aValue);
			
			if (!oUpdated.contains(aName)) {
				oUpdated.add(aName);
			}
		}
	}
	
	public String nextUpdated() {
		while (!oUpdated.isEmpty()) {
			return oUpdated.remove(0);
		}
		
		return null;
	}
	
	public Boolean hasUpdated() {
		return !oUpdated.isEmpty();
	}
	
	public Object find(String aKey) {
		if (aKey.equals("move_apps")) return (Object) move_apps();
		else if (aKey.equals("move_dalvik")) return (Object) move_dalvik();
		else if (aKey.equals("move_data")) return (Object) move_data();
		else if (aKey.equals("move_libs")) return (Object) move_libs();
		else if (aKey.equals("move_media")) return (Object) move_media();
		else if (aKey.equals("move_system")) return (Object) move_system();
		else if (aKey.equals("enable_cache")) return (Object) enable_cache();
		else if (aKey.equals("enable_swap")) return (Object) enable_swap();
		else if (aKey.equals("enable_swap")) return (Object) enable_swap();
		else if (aKey.equals("enable_sdext_journal")) return (Object) enable_sdext_journal();
		else if (aKey.equals("enable_debug")) return (Object) enable_debug();
		else if (aKey.equals("set_swap_level")) return (Object) set_swap_level();
		else if (aKey.equals("set_sdext_fstype")) return (Object) set_sdext_fstype();
		else if (aKey.equals("run_sdext_fschk")) return (Object) run_sdext_fschk();
		else if (aKey.equals("set_storage_threshold")) return (Object) set_storage_threshold();
		else if (aKey.equals("set_zram_compression")) return (Object) set_zram_compression();
		else if (aKey.equals("set_emmc_readahead")) return (Object) set_emmc_readahead();
		else if (aKey.equals("set_emmc_scheduler")) return (Object) set_emmc_scheduler();
		else if (aKey.equals("set_immc_readahead")) return (Object) set_immc_readahead();
		else if (aKey.equals("set_immc_scheduler")) return (Object) set_immc_scheduler();
		else if (aKey.equals("disable_safemode")) return (Object) disable_safemode();
		
		return null;
	}
	
	public Boolean move_apps() {
		return "1".equals(mCached.getString("move_apps"));
	}
	
	public void move_apps(Boolean aValue) {
		update("move_apps", (aValue ? "1" : "0"));
	}
	
	public Boolean move_dalvik() {
		return "1".equals(mCached.getString("move_dalvik"));
	}
	
	public void move_dalvik(Boolean aValue) {
		update("move_dalvik", (aValue ? "1" : "0"));
	}
	
	public Boolean move_data() {
		return "1".equals(mCached.getString("move_data"));
	}
	
	public void move_data(Boolean aValue) {
		update("move_data", (aValue ? "1" : "0"));
	}
	
	public Boolean move_libs() {
		return "1".equals(mCached.getString("move_libs"));
	}
	
	public void move_libs(Boolean aValue) {
		update("move_libs", (aValue ? "1" : "0"));
	}
	
	public Boolean move_media() {
		return "1".equals(mCached.getString("move_media"));
	}
	
	public void move_media(Boolean aValue) {
		update("move_media", (aValue ? "1" : "0"));
	}
	
	public Boolean move_system() {
		return "1".equals(mCached.getString("move_system"));
	}
	
	public void move_system(Boolean aValue) {
		update("move_system", (aValue ? "1" : "0"));
	}
	
	public Integer enable_cache() {
		return "2".equals(mCached.getString("enable_cache")) ? STATE_AUTO : 
			"1".equals(mCached.getString("enable_cache")) ? STATE_ON : STATE_OFF;
	}
	
	public void enable_cache(Integer aValue) {
		if (aValue == STATE_ON || aValue == STATE_OFF || aValue == STATE_AUTO) {
			update("enable_cache", "" + aValue);
		}
	}
	
	public Boolean enable_swap() {
		return "1".equals(mCached.getString("enable_swap"));
	}
	
	public void enable_swap(Boolean aValue) {
		update("enable_swap", (aValue ? "1" : "0"));
	}
	
	public Integer enable_sdext_journal() {
		return "2".equals(mCached.getString("enable_sdext_journal")) ? STATE_AUTO : 
			"1".equals(mCached.getString("enable_sdext_journal")) ? STATE_ON : STATE_OFF;
	}
	
	public void enable_sdext_journal(Integer aValue) {
		if (aValue == STATE_ON || aValue == STATE_OFF || aValue == STATE_AUTO) {
			update("enable_sdext_journal", "" + aValue);
		}
	}
	
	public Boolean enable_debug() {
		return "1".equals(mCached.getString("enable_debug"));
	}
	
	public void enable_debug(Boolean aValue) {
		update("enable_debug", (aValue ? "1" : "0"));
	}
	
	public Integer set_swap_level() {
		try {
			return Integer.valueOf(mCached.getString("set_swap_level"));
		
		} catch(NumberFormatException e) {}
		
		return 0;
	}
	
	public void set_swap_level(Integer aValue) {
		update("set_swap_level", "" + aValue);
	}
	
	public String set_sdext_fstype() {
		return mCached.getString("set_sdext_fstype");
	}
	
	public void set_sdext_fstype(String aValue) {
		update("set_sdext_fstype", aValue);
	}
	
	public Boolean run_sdext_fschk() {
		return "1".equals(mCached.getString("run_sdext_fschk"));
	}
	
	public void run_sdext_fschk(Boolean aValue) {
		update("run_sdext_fschk", (aValue ? "1" : "0"));
	}
	
	public Integer set_storage_threshold() {
		try {
			return Integer.valueOf(mCached.getString("set_storage_threshold"));
		
		} catch(NumberFormatException e) {}
		
		return 0;
	}
	
	public void set_storage_threshold(Integer aValue) {
		if ((int) aValue <= 25 && (int) aValue >= 0) {
			update("set_storage_threshold", "" + aValue);
		}
	}
	
	public Integer set_zram_compression() {
		try {
			return Integer.valueOf(mCached.getString("set_zram_compression"));
		
		} catch(NumberFormatException e) {}
		
		return 0;
	}
	
	public void set_zram_compression(Integer aValue) {
		if ((int) aValue <= 25 && (int) aValue >= 0) {
			update("set_zram_compression", "" + aValue);
		}
	}
	
	public Integer set_emmc_readahead() {
		try {
			return Integer.valueOf(mCached.getString("set_emmc_readahead"));
		
		} catch(NumberFormatException e) {}
		
		return 0;
	}
	
	public void set_emmc_readahead(Integer aValue) {
		update("set_emmc_readahead", "" + aValue);
	}
	
	public String set_emmc_scheduler() {
		return mCached.getString("set_emmc_scheduler");
	}
	
	public void set_emmc_scheduler(String aValue) {
		update("set_emmc_scheduler", aValue);
	}
	
	public Integer set_immc_readahead() {
		try {
			return Integer.valueOf(mCached.getString("set_immc_readahead"));
		
		} catch(NumberFormatException e) {}
		
		return 0;
	}
	
	public void set_immc_readahead(Integer aValue) {
		update("set_immc_readahead", "" + aValue);
	}
	
	public String set_immc_scheduler() {
		return mCached.getString("set_immc_scheduler");
	}
	
	public void set_immc_scheduler(String aValue) {
		update("set_immc_scheduler", aValue);
	}
	
	public Boolean disable_safemode() {
		return "1".equals(mCached.getString("disable_safemode"));
	}
	
	public void disable_safemode(Boolean aValue) {
		update("disable_safemode", (aValue ? "1" : "0"));
	}
}
