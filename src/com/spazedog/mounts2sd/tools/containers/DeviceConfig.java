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

public final class DeviceConfig {
	
	private PersistentPreferences mCached;
	
	private final static String oPreferenceName = "DeviceConfig";
	
	public DeviceConfig(Preferences aPref) {
		mCached = aPref.cached(oPreferenceName);
	}
	
	public Object find(String aKey) {
		if (aKey.equals("location_storage_data")) return (Object) location_storage_data();
		else if (aKey.equals("location_storage_sdext")) return (Object) location_storage_sdext();
		else if (aKey.equals("location_storage_cache")) return (Object) location_storage_cache();
		else if (aKey.equals("type_filesystem_driver")) return (Object) type_filesystem_driver();
		else if (aKey.equals("value_immc_scheduler")) return (Object) value_immc_scheduler();
		else if (aKey.equals("value_emmc_scheduler")) return (Object) value_emmc_scheduler();
		else if (aKey.equals("value_immc_readahead")) return (Object) value_immc_readahead();
		else if (aKey.equals("value_emmc_readahead")) return (Object) value_emmc_readahead();
		else if (aKey.equals("status_content_apps")) return (Object) status_content_apps();
		else if (aKey.equals("status_content_dalvik")) return (Object) status_content_dalvik();
		else if (aKey.equals("status_content_data")) return (Object) status_content_data();
		else if (aKey.equals("status_content_libs")) return (Object) status_content_libs();
		else if (aKey.equals("status_content_media")) return (Object) status_content_media();
		else if (aKey.equals("status_content_system")) return (Object) status_content_system();
		else if (aKey.equals("status_filesystem_journal")) return (Object) status_filesystem_journal();
		else if (aKey.equals("usage_content_apps")) return (Object) usage_content_apps();
		else if (aKey.equals("usage_content_dalvik")) return (Object) usage_content_dalvik();
		else if (aKey.equals("usage_content_data")) return (Object) usage_content_data();
		else if (aKey.equals("usage_content_libs")) return (Object) usage_content_libs();
		else if (aKey.equals("usage_content_media")) return (Object) usage_content_media();
		else if (aKey.equals("usage_content_system")) return (Object) usage_content_system();
		else if (aKey.equals("size_memory_swap")) return (Object) size_memory_swap();
		else if (aKey.equals("size_memory_zram")) return (Object) size_memory_zram();
		else if (aKey.equals("usage_memory_swap")) return (Object) usage_memory_swap();
		else if (aKey.equals("usage_memory_zram")) return (Object) usage_memory_zram();
		else if (aKey.equals("level_memory_swappiness")) return (Object) level_memory_swappiness();
		else if (aKey.equals("level_filesystem_fschk")) return (Object) level_filesystem_fschk();
		else if (aKey.equals("level_storage_threshold")) return (Object) size_storage_threshold();
		
		return null;
	}
	
	public String location_storage_data() {
		return mCached.getString("location_storage_data");
	}
	
	public String location_storage_sdext() {
		return mCached.getString("location_storage_sdext");
	}
	
	public String location_storage_cache() {
		return mCached.getString("location_storage_cache");
	}
	
	public String type_filesystem_driver() {
		return mCached.getString("type_filesystem_driver");
	}

	public String value_immc_scheduler() {
		return mCached.getString("value_immc_scheduler");
	}
	
	public String value_emmc_scheduler() {
		return mCached.getString("value_emmc_scheduler");
	}
	
	public String value_immc_readahead() {
		return mCached.getString("value_immc_readahead");
	}
	
	public String value_emmc_readahead() {
		return mCached.getString("value_emmc_readahead");
	}
	
	public Boolean status_content_apps() {
		return mCached.getBoolean("status_content_apps", false);
	}
	
	public Boolean status_content_dalvik() {
		return mCached.getBoolean("status_content_dalvik", false);
	}
	
	public Boolean status_content_data() {
		return mCached.getBoolean("status_content_data", false);
	}
	
	public Boolean status_content_libs() {
		return mCached.getBoolean("status_content_libs", false);
	}
	
	public Boolean status_content_media() {
		return mCached.getBoolean("status_content_media", false);
	}
	
	public Boolean status_content_system() {
		return mCached.getBoolean("status_content_system", false);
	}
	
	public Boolean status_filesystem_journal() {
		return mCached.getBoolean("status_filesystem_journal", false);
	}

	public Long usage_content_apps() {
		return mCached.getLong("usage_content_apps", 0L);
	}
	
	public Long usage_content_dalvik() {
		return mCached.getLong("usage_content_dalvik", 0L);
	}
	
	public Long usage_content_data() {
		return mCached.getLong("usage_content_data", 0L);
	}
	
	public Long usage_content_libs() {
		return mCached.getLong("usage_content_libs", 0L);
	}
	
	public Long usage_content_media() {
		return mCached.getLong("usage_content_media", 0L);
	}
	
	public Long usage_content_system() {
		return mCached.getLong("usage_content_system", 0L);
	}

	public Long size_memory_swap() {
		return mCached.getLong("size_memory_swap", 0L);
	}
	
	public Long size_memory_zram() {
		return mCached.getLong("size_memory_zram", 0L);
	}
	
	public Long usage_memory_swap() {
		return mCached.getLong("usage_memory_swap", 0L);
	}
	
	public Long usage_memory_zram() {
		return mCached.getLong("usage_memory_zram", 0L);
	}
	
	public Long size_storage_threshold() {
		return mCached.getLong("size_storage_threshold", 0L);
	}
	
	public Integer level_memory_swappiness() {
		return mCached.getInteger("level_memory_swappiness", 0);
	}
	
	public Integer level_filesystem_fschk() {
		return mCached.getInteger("level_filesystem_fschk", 0);
	}
}
