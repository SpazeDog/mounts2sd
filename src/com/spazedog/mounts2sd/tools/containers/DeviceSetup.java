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

public final class DeviceSetup {

	private PersistentPreferences mCached;
	
	private final static String oPreferenceName = "DeviceSetup";
	
	public DeviceSetup(Preferences aPref) {
		mCached = aPref.cached(oPreferenceName);
	}
	
	public Object find(String aKey) {
		if (aKey.equals("support_option_swap")) return (Object) support_option_swap();
		else if (aKey.equals("support_option_zram")) return (Object) support_option_zram();
		else if (aKey.equals("support_binary_e2fsck")) return (Object) support_binary_e2fsck();
		else if (aKey.equals("support_binary_sqlite3")) return (Object) support_binary_sqlite3();
		else if (aKey.equals("support_binary_tune2fs")) return (Object) support_binary_tune2fs();
		else if (aKey.equals("support_directory_library")) return (Object) support_directory_library();
		else if (aKey.equals("support_directory_user")) return (Object) support_directory_user();
		else if (aKey.equals("support_directory_media")) return (Object) support_directory_media();
		else if (aKey.equals("support_directory_system")) return (Object) support_directory_system();
		else if (aKey.equals("support_directory_cmdalvik")) return (Object) support_directory_cmdalvik();
		else if (aKey.equals("support_device_mtd")) return (Object) support_device_mtd();
		else if (aKey.equals("path_device_map_emmc")) return (Object) path_device_map_emmc();
		else if (aKey.equals("path_device_map_immc")) return (Object) path_device_map_immc();
		else if (aKey.equals("path_device_map_sdext")) return (Object) path_device_map_sdext();
		else if (aKey.equals("path_device_map_swap")) return (Object) path_device_map_swap();
		else if (aKey.equals("path_device_map_data")) return (Object) path_device_map_data();
		else if (aKey.equals("path_device_map_cache")) return (Object) path_device_map_cache();
		else if (aKey.equals("path_device_map_zram")) return (Object) path_device_map_zram();
		else if (aKey.equals("path_device_readahead_immc")) return (Object) path_device_readahead_immc();
		else if (aKey.equals("path_device_readahead_emmc")) return (Object) path_device_readahead_emmc();
		else if (aKey.equals("path_device_scheduler_immc")) return (Object) path_device_scheduler_immc();
		else if (aKey.equals("path_device_scheduler_emmc")) return (Object) path_device_scheduler_emmc();
		else if (aKey.equals("paths_directory_system")) return (Object) paths_directory_system();
		else if (aKey.equals("init_implementation")) return (Object) init_implementation();
		else if (aKey.equals("type_device_sdext")) return (Object) type_device_sdext();
		else if (aKey.equals("safemode")) return (Object) safemode();
		else if (aKey.equals("environment_busybox")) return (Object) environment_busybox();
		else if (aKey.equals("environment_multiple_binaries")) return (Object) environment_multiple_binaries();
		else if (aKey.equals("environment_startup_script")) return (Object) environment_startup_script();
		else if (aKey.equals("id_startup_script")) return (Object) id_startup_script();
		else if (aKey.equals("version_startup_script")) return (Object) version_startup_script();
		else if (aKey.equals("log_level")) return (Object) log_level();
		
		return null;
	}
	
	public Boolean support_option_swap() {
		return mCached.getBoolean("support_option_swap", false);
	}
	
	public Boolean support_option_zram() {
		return mCached.getBoolean("support_option_zram", false);
	}
	
	public Boolean support_binary_e2fsck() {
		return mCached.getBoolean("support_binary_e2fsck", false);
	}
	
	public Boolean support_binary_sqlite3() {
		return mCached.getBoolean("support_binary_sqlite3", false);
	}
	
	public Boolean support_binary_tune2fs() {
		return mCached.getBoolean("support_binary_tune2fs", false);
	}
	
	public Boolean support_directory_library() {
		return mCached.getBoolean("support_directory_library", false);
	}
	
	public Boolean support_directory_user() {
		return mCached.getBoolean("support_directory_user", false);
	}
	
	public Boolean support_directory_media() {
		return mCached.getBoolean("support_directory_media", false);
	}
	
	public Boolean support_directory_system() {
		return mCached.getBoolean("support_directory_system", false);
	}
	
	public Boolean support_directory_cmdalvik() {
		return mCached.getBoolean("support_directory_cmdalvik", false);
	}
	
	public Boolean support_device_mtd() {
		return mCached.getBoolean("support_device_mtd", false);
	}
	
	public String path_device_map_emmc() {
		return mCached.getString("path_device_map_emmc");
	}
	
	public String path_device_map_immc() {
		return mCached.getString("path_device_map_immc");
	}
	
	public String path_device_map_sdext() {
		return mCached.getString("path_device_map_sdext");
	}
	
	public String path_device_map_swap() {
		return mCached.getString("path_device_map_swap");
	}
	
	public String path_device_map_data() {
		return mCached.getString("path_device_map_data");
	}
	
	public String path_device_map_cache() {
		return mCached.getString("path_device_map_cache");
	}
	
	public String path_device_map_zram() {
		return mCached.getString("path_device_map_zram");
	}

	public String path_device_readahead_immc() {
		return mCached.getString("path_device_readahead_immc");
	}
	
	public String path_device_readahead_emmc() {
		return mCached.getString("path_device_readahead_emmc");
	}
	
	public String path_device_scheduler_immc() {
		return mCached.getString("path_device_scheduler_immc");
	}
	
	public String path_device_scheduler_emmc() {
		return mCached.getString("path_device_scheduler_emmc");
	}
	
	public String[] paths_directory_system() {
		String content = mCached.getString("paths_directory_system");
		
		if (content != null) {
			return content.split(",");
		}
		
		return null;
	}
	
	public String init_implementation() {
		return mCached.getString("init_implementation");
	}
	
	public String type_device_sdext() {
		return mCached.getString("type_device_sdext");
	}
	
	public Boolean safemode() {
		return mCached.getBoolean("safemode", false);
	}
	
	public Boolean environment_busybox() {
		return mCached.getBoolean("environment_busybox", false);
	}
	
	public Boolean environment_multiple_binaries() {
		return mCached.getBoolean("environment_multiple_binaries", false);
	}
	
	public Boolean environment_startup_script() {
		return mCached.getBoolean("environment_startup_script", false);
	}
	
	public Integer id_startup_script() {
		return mCached.getInteger("id_startup_script");
	}
	
	public String version_startup_script() {
		return mCached.getString("version_startup_script");
	}
	
	public Integer log_level() {
		return mCached.getInteger("log_level", 0);
	}
}
