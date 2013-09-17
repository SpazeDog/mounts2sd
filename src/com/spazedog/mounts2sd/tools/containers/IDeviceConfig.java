package com.spazedog.mounts2sd.tools.containers;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import com.spazedog.mounts2sd.tools.Preferences.IPersistentPreferences;

public class IDeviceConfig {
	protected IPersistentPreferences mPersistentStorage;
	
	public Object find(String method) {
		try {
			return (Object) getClass().getMethod(method).invoke(this);
			
		} catch (Throwable e) {
			return null;
		}
	}
	
	public Method[] listAllOptions() {
		Method[] methods = getClass().getMethods();
		List<Method> list = new ArrayList<Method>();
		
		for (int i=0; i < methods.length; i++) {
			if (methods[i].getDeclaringClass().equals(IDeviceConfig.class) 
					&& methods[i].getName().contains("_")
					&& !methods[i].getReturnType().equals(Void.TYPE)) {
				
				list.add(methods[i]);
			}
		}
		
		return list.toArray(new Method[list.size()]);
	}
	
	public String location_storage_data() {
		return mPersistentStorage.getString("location_storage_data");
	}
	
	public void location_storage_data(String value) {
		mPersistentStorage.edit().putString("location_storage_data", value).commit();
	}
	
	public String location_storage_sdext() {
		return mPersistentStorage.getString("location_storage_sdext");
	}
	
	public void location_storage_sdext(String value) {
		mPersistentStorage.edit().putString("location_storage_sdext", value).commit();
	}
	
	public String location_storage_cache() {
		return mPersistentStorage.getString("location_storage_cache");
	}
	
	public void location_storage_cache(String value) {
		mPersistentStorage.edit().putString("location_storage_cache", value).commit();
	}
	
	public String type_filesystem_driver() {
		return mPersistentStorage.getString("type_filesystem_driver");
	}
	
	public void type_filesystem_driver(String value) {
		mPersistentStorage.edit().putString("type_filesystem_driver", value).commit();
	}

	public String value_immc_scheduler() {
		return mPersistentStorage.getString("value_immc_scheduler");
	}
	
	public void value_immc_scheduler(String value) {
		mPersistentStorage.edit().putString("value_immc_scheduler", value).commit();
	}
	
	public String value_emmc_scheduler() {
		return mPersistentStorage.getString("value_emmc_scheduler");
	}
	
	public void value_emmc_scheduler(String value) {
		mPersistentStorage.edit().putString("value_emmc_scheduler", value).commit();
	}
	
	public String value_immc_readahead() {
		return mPersistentStorage.getString("value_immc_readahead");
	}
	
	public void value_immc_readahead(String value) {
		mPersistentStorage.edit().putString("value_immc_readahead", value).commit();
	}
	
	public String value_emmc_readahead() {
		return mPersistentStorage.getString("value_emmc_readahead");
	}
	
	public void value_emmc_readahead(String value) {
		mPersistentStorage.edit().putString("value_emmc_readahead", value).commit();
	}
	
	public Integer status_content_apps() {
		return mPersistentStorage.getInteger("status_content_apps", 0);
	}
	
	public void status_content_apps(Integer value) {
		mPersistentStorage.edit().putInteger("status_content_apps", value).commit();
	}
	
	public Integer status_content_sysapps() {
		return mPersistentStorage.getInteger("status_content_sysapps", 0);
	}
	
	public void status_content_sysapps(Integer value) {
		mPersistentStorage.edit().putInteger("status_content_sysapps", value).commit();
	}
	
	public Integer status_content_dalvik() {
		return mPersistentStorage.getInteger("status_content_dalvik", 0);
	}
	
	public void status_content_dalvik(Integer value) {
		mPersistentStorage.edit().putInteger("status_content_dalvik", value).commit();
	}
	
	public Integer status_content_data() {
		return mPersistentStorage.getInteger("status_content_data", 0);
	}
	
	public void status_content_data(Integer value) {
		mPersistentStorage.edit().putInteger("status_content_data", value).commit();
	}
	
	public Integer status_content_libs() {
		return mPersistentStorage.getInteger("status_content_libs", 0);
	}
	
	public void status_content_libs(Integer value) {
		mPersistentStorage.edit().putInteger("status_content_libs", value).commit();
	}
	
	public Integer status_content_media() {
		return mPersistentStorage.getInteger("status_content_media", 0);
	}
	
	public void status_content_media(Integer value) {
		mPersistentStorage.edit().putInteger("status_content_media", value).commit();
	}
	
	public Integer status_content_system() {
		return mPersistentStorage.getInteger("status_content_system", 0);
	}
	
	public void status_content_system(Integer value) {
		mPersistentStorage.edit().putInteger("status_content_system", value).commit();
	}
	
	public Integer status_filesystem_journal() {
		return mPersistentStorage.getInteger("status_filesystem_journal", -1);
	}
	
	public void status_filesystem_journal(Integer value) {
		mPersistentStorage.edit().putInteger("status_filesystem_journal", value).commit();
	}

	public Long usage_content_apps() {
		return mPersistentStorage.getLong("usage_content_apps", 0L);
	}
	
	public void usage_content_apps(Long value) {
		mPersistentStorage.edit().putLong("usage_content_apps", value).commit();
	}
	
	public Long usage_content_sysapps() {
		return mPersistentStorage.getLong("usage_content_sysapps", 0L);
	}
	
	public void usage_content_sysapps(Long value) {
		mPersistentStorage.edit().putLong("usage_content_sysapps", value).commit();
	}
	
	public Long usage_content_dalvik() {
		return mPersistentStorage.getLong("usage_content_dalvik", 0L);
	}
	
	public void usage_content_dalvik(Long value) {
		mPersistentStorage.edit().putLong("usage_content_dalvik", value).commit();
	}
	
	public Long usage_content_data() {
		return mPersistentStorage.getLong("usage_content_data", 0L);
	}
	
	public void usage_content_data(Long value) {
		mPersistentStorage.edit().putLong("usage_content_data", value).commit();
	}
	
	public Long usage_content_libs() {
		return mPersistentStorage.getLong("usage_content_libs", 0L);
	}
	
	public void usage_content_libs(Long value) {
		mPersistentStorage.edit().putLong("usage_content_libs", value).commit();
	}
	
	public Long usage_content_media() {
		return mPersistentStorage.getLong("usage_content_media", 0L);
	}
	
	public void usage_content_media(Long value) {
		mPersistentStorage.edit().putLong("usage_content_media", value).commit();
	}
	
	public Long usage_content_system() {
		return mPersistentStorage.getLong("usage_content_system", 0L);
	}
	
	public void usage_content_system(Long value) {
		mPersistentStorage.edit().putLong("usage_content_system", value).commit();
	}

	public Long size_memory_swap() {
		return mPersistentStorage.getLong("size_memory_swap", 0L);
	}
	
	public void size_memory_swap(Long value) {
		mPersistentStorage.edit().putLong("size_memory_swap", value).commit();
	}
	
	public Long size_memory_zram() {
		return mPersistentStorage.getLong("size_memory_zram", 0L);
	}
	
	public void size_memory_zram(Long value) {
		mPersistentStorage.edit().putLong("size_memory_zram", value).commit();
	}

	public Long size_storage_data() {
		return mPersistentStorage.getLong("size_storage_data", 0L);
	}
	
	public void size_storage_data(Long value) {
		mPersistentStorage.edit().putLong("size_storage_data", value).commit();
	}
	
	public Long size_storage_sdext() {
		return mPersistentStorage.getLong("size_storage_sdext", 0L);
	}
	
	public void size_storage_sdext(Long value) {
		mPersistentStorage.edit().putLong("size_storage_sdext", value).commit();
	}
	
	public Long size_storage_cache() {
		return mPersistentStorage.getLong("size_storage_cache", 0L);
	}
	
	public void size_storage_cache(Long value) {
		mPersistentStorage.edit().putLong("size_storage_cache", value).commit();
	}

	public Long usage_storage_data() {
		return mPersistentStorage.getLong("usage_storage_data", 0L);
	}
	
	public void usage_storage_data(Long value) {
		mPersistentStorage.edit().putLong("usage_storage_data", value).commit();
	}
	
	public Long usage_storage_sdext() {
		return mPersistentStorage.getLong("usage_storage_sdext", 0L);
	}
	
	public void usage_storage_sdext(Long value) {
		mPersistentStorage.edit().putLong("usage_storage_sdext", value).commit();
	}
	
	public Long usage_storage_cache() {
		return mPersistentStorage.getLong("usage_storage_cache", 0L);
	}
	
	public void usage_storage_cache(Long value) {
		mPersistentStorage.edit().putLong("usage_storage_cache", value).commit();
	}

	public Long usage_memory_swap() {
		return mPersistentStorage.getLong("usage_memory_swap", 0L);
	}
	
	public void usage_memory_swap(Long value) {
		mPersistentStorage.edit().putLong("usage_memory_swap", value).commit();
	}
	
	public Long usage_memory_zram() {
		return mPersistentStorage.getLong("usage_memory_zram", 0L);
	}
	
	public void usage_memory_zram(Long value) {
		mPersistentStorage.edit().putLong("usage_memory_zram", value).commit();
	}
	
	public Long size_storage_threshold() {
		return mPersistentStorage.getLong("size_storage_threshold", 0L);
	}
	
	public void size_storage_threshold(Long value) {
		mPersistentStorage.edit().putLong("size_storage_threshold", value).commit();
	}
	
	public Integer level_memory_swappiness() {
		return mPersistentStorage.getInteger("level_memory_swappiness", 0);
	}
	
	public void level_memory_swappiness(Integer value) {
		mPersistentStorage.edit().putInteger("level_memory_swappiness", value).commit();
	}
	
	public Integer level_filesystem_fschk() {
		return mPersistentStorage.getInteger("level_filesystem_fschk", 0);
	}
	
	public void level_filesystem_fschk(Integer value) {
		mPersistentStorage.edit().putInteger("level_filesystem_fschk", value).commit();
	}
}
