package com.spazedog.mounts2sd.tools.containers;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import com.spazedog.mounts2sd.tools.Preferences.IPersistentPreferences;

public class IDeviceSetup {
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
			if (methods[i].getDeclaringClass().equals(IDeviceSetup.class) 
					&& methods[i].getName().contains("_")
					&& !methods[i].getReturnType().equals(Void.TYPE)) {
				
				list.add(methods[i]);
			}
		}
		
		return list.toArray(new Method[list.size()]);
	}
	
	public Boolean support_option_swap() {
		return mPersistentStorage.getBoolean("support_option_swap");
	}

	public void support_option_swap(Boolean value) {
		mPersistentStorage.edit().putBoolean("support_option_swap", value).commit();
	}
	
	public Boolean support_option_zram() {
		return mPersistentStorage.getBoolean("support_option_zram");
	}
	
	public void support_option_zram(Boolean value) {
		mPersistentStorage.edit().putBoolean("support_option_zram", value).commit();
	}
	
	public Boolean support_binary_e2fsck() {
		return mPersistentStorage.getBoolean("support_binary_e2fsck");
	}
	
	public void support_binary_e2fsck(Boolean value) {
		mPersistentStorage.edit().putBoolean("support_binary_e2fsck", value).commit();
	}
	
	public Boolean support_binary_sqlite3() {
		return mPersistentStorage.getBoolean("support_binary_sqlite3");
	}
	
	public void support_binary_sqlite3(Boolean value) {
		mPersistentStorage.edit().putBoolean("support_binary_sqlite3", value).commit();
	}
	
	public Boolean support_binary_tune2fs() {
		return mPersistentStorage.getBoolean("support_binary_tune2fs");
	}
	
	public void support_binary_tune2fs(Boolean value) {
		mPersistentStorage.edit().putBoolean("support_binary_tune2fs", value).commit();
	}
	
	public Boolean support_directory_library() {
		return mPersistentStorage.getBoolean("support_directory_library");
	}
	
	public void support_directory_library(Boolean value) {
		mPersistentStorage.edit().putBoolean("support_directory_library", value).commit();
	}
	
	public Boolean support_directory_asec() {
		return mPersistentStorage.getBoolean("support_directory_asec");
	}
	
	public void support_directory_asec(Boolean value) {
		mPersistentStorage.edit().putBoolean("support_directory_asec", value).commit();
	}
	
	public Boolean support_directory_user() {
		return mPersistentStorage.getBoolean("support_directory_user");
	}
	
	public void support_directory_user(Boolean value) {
		mPersistentStorage.edit().putBoolean("support_directory_user", value).commit();
	}
	
	public Boolean support_directory_media() {
		return mPersistentStorage.getBoolean("support_directory_media");
	}
	
	public void support_directory_media(Boolean value) {
		mPersistentStorage.edit().putBoolean("support_directory_media", value).commit();
	}
	
	public Boolean support_directory_system() {
		return mPersistentStorage.getBoolean("support_directory_system");
	}
	
	public void support_directory_system(Boolean value) {
		mPersistentStorage.edit().putBoolean("support_directory_system", value).commit();
	}
	
	public Boolean support_directory_cmdalvik() {
		return mPersistentStorage.getBoolean("support_directory_cmdalvik");
	}
	
	public void support_directory_cmdalvik(Boolean value) {
		mPersistentStorage.edit().putBoolean("support_directory_cmdalvik", value).commit();
	}
	
	public Boolean support_device_mtd() {
		return mPersistentStorage.getBoolean("support_device_mtd");
	}
	
	public void support_device_mtd(Boolean value) {
		mPersistentStorage.edit().putBoolean("support_device_mtd", value).commit();
	}
	
	public Boolean safemode() {
		return mPersistentStorage.getBoolean("safemode");
	}
	
	public void safemode(Boolean value) {
		mPersistentStorage.edit().putBoolean("safemode", value).commit();
	}
	
	public Boolean environment_busybox() {
		return mPersistentStorage.getBoolean("environment_busybox");
	}
	
	public void environment_busybox(Boolean value) {
		mPersistentStorage.edit().putBoolean("environment_busybox", value).commit();
	}
	
	public Boolean environment_busybox_internal() {
		return mPersistentStorage.getBoolean("environment_busybox_internal");
	}
	
	public void environment_busybox_internal(Boolean value) {
		mPersistentStorage.edit().putBoolean("environment_busybox_internal", value).commit();
	}
	
	public Boolean environment_multiple_binaries() {
		return mPersistentStorage.getBoolean("environment_multiple_binaries");
	}

	public void environment_multiple_binaries(Boolean value) {
		mPersistentStorage.edit().putBoolean("environment_multiple_binaries", value).commit();
	}
	
	public Boolean environment_startup_script() {
		return mPersistentStorage.getBoolean("environment_startup_script");
	}

	public void environment_startup_script(Boolean value) {
		mPersistentStorage.edit().putBoolean("environment_startup_script", value).commit();
	}

	public Boolean environment_secure_flag_off() {
		return mPersistentStorage.getBoolean("environment_secure_flag_off");
	}

	public void environment_secure_flag_off(Boolean value) {
		mPersistentStorage.edit().putBoolean("environment_secure_flag_off", value).commit();
	}

	public Integer log_level() {
		return mPersistentStorage.getInteger("log_level", 0);
	}

	public void log_level(Integer value) {
		mPersistentStorage.edit().putInteger("log_level", value).commit();
	}
	
	public Integer id_startup_script() {
		return mPersistentStorage.getInteger("id_startup_script");
	}
	
	public void id_startup_script(Integer value) {
		mPersistentStorage.edit().putInteger("id_startup_script", value).commit();
	}
	
	public String version_startup_script() {
		return mPersistentStorage.getString("version_startup_script");
	}
	
	public void version_startup_script(String value) {
		mPersistentStorage.edit().putString("version_startup_script", value).commit();
	}
	
	public String path_device_map_immc() {
		return mPersistentStorage.getString("path_device_map_immc");
	}
	
	public void path_device_map_immc(String value) {
		mPersistentStorage.edit().putString("path_device_map_immc", value).commit();
	}
	
	public String path_device_map_emmc() {
		return mPersistentStorage.getString("path_device_map_emmc");
	}
	
	public void path_device_map_emmc(String value) {
		mPersistentStorage.edit().putString("path_device_map_emmc", value).commit();
	}
	
	public String path_device_map_sdext() {
		return mPersistentStorage.getString("path_device_map_sdext");
	}
	
	public void path_device_map_sdext(String value) {
		mPersistentStorage.edit().putString("path_device_map_sdext", value).commit();
	}

	public String path_device_map_swap() {
		return mPersistentStorage.getString("path_device_map_swap");
	}
	
	public void path_device_map_swap(String value) {
		mPersistentStorage.edit().putString("path_device_map_swap", value).commit();
	}
	
	public String path_device_map_data() {
		return mPersistentStorage.getString("path_device_map_data");
	}
	
	public void path_device_map_data(String value) {
		mPersistentStorage.edit().putString("path_device_map_data", value).commit();
	}
	
	public String path_device_map_cache() {
		return mPersistentStorage.getString("path_device_map_cache");
	}
	
	public void path_device_map_cache(String value) {
		mPersistentStorage.edit().putString("path_device_map_cache", value).commit();
	}
	
	public String path_device_map_zram() {
		return mPersistentStorage.getString("path_device_map_zram");
	}
	
	public void path_device_map_zram(String value) {
		mPersistentStorage.edit().putString("path_device_map_zram", value).commit();
	}

	public String path_device_readahead_immc() {
		return mPersistentStorage.getString("path_device_readahead_immc");
	}
	
	public void path_device_readahead_immc(String value) {
		mPersistentStorage.edit().putString("path_device_readahead_immc", value).commit();
	}
	
	public String path_device_readahead_emmc() {
		return mPersistentStorage.getString("path_device_readahead_emmc");
	}
	
	public void path_device_readahead_emmc(String value) {
		mPersistentStorage.edit().putString("path_device_readahead_emmc", value).commit();
	}
	
	public String path_device_scheduler_immc() {
		return mPersistentStorage.getString("path_device_scheduler_immc");
	}
	
	public void path_device_scheduler_immc(String value) {
		mPersistentStorage.edit().putString("path_device_scheduler_immc", value).commit();
	}
	
	public String path_device_scheduler_emmc() {
		return mPersistentStorage.getString("path_device_scheduler_emmc");
	}
	
	public void path_device_scheduler_emmc(String value) {
		mPersistentStorage.edit().putString("path_device_scheduler_emmc", value).commit();
	}
	
	public String[] paths_directory_system() {
		return mPersistentStorage.getStringArray("paths_directory_system");
	}
	
	public void paths_directory_system(String[] value) {
		mPersistentStorage.edit().putStringArray("paths_directory_system", value).commit();
	}
	
	public String init_implementation() {
		return mPersistentStorage.getString("init_implementation");
	}
	
	public void init_implementation(String value) {
		mPersistentStorage.edit().putString("init_implementation", value).commit();
	}
	
	public String type_device_sdext() {
		return mPersistentStorage.getString("type_device_sdext");
	}
	
	public void type_device_sdext(String value) {
		mPersistentStorage.edit().putString("type_device_sdext", value).commit();
	}
}
