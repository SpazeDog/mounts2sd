package com.spazedog.mounts2sd.tools.containers;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import com.spazedog.lib.rootfw3.RootFW;
import com.spazedog.mounts2sd.tools.Preferences.IPersistentPreferences;
import com.spazedog.mounts2sd.tools.Root;

public class IDeviceProperties {
	protected IPersistentPreferences mPersistentStorage;
	
	private final static Object oClassLock = new Object();
	
	public final Integer STATE_OFF = 0;
	public final Integer STATE_ON = 1;
	public final Integer STATE_AUTO = 2;
	
	protected void update(final String name, final String value) {
		synchronized (oClassLock) {
			if (!mPersistentStorage.edit().isLocked()) {
				new Thread() {
					@Override
					public void run() {
						RootFW root = Root.initiate();
						
						if (root.isConnected()) {
							root.file("/data/property/m2sd." + name).write(value);
						}
						
						Root.release();
					}
					
				}.start();
			}
			
			mPersistentStorage.edit().putString(name, value).commit();
		}
	}
	
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
			if (methods[i].getDeclaringClass().equals(IDeviceProperties.class) 
					&& methods[i].getName().contains("_")
					&& !methods[i].getReturnType().equals(Void.TYPE)) {
				
				list.add(methods[i]);
			}
		}
		
		return list.toArray(new Method[list.size()]);
	}
	
	public Boolean move_apps() {
		return "1".equals(mPersistentStorage.getString("move_apps"));
	}
	
	public void move_apps(Boolean value) {
		update("move_apps", (value ? "1" : "0"));
	}
	
	public Boolean move_sysapps() {
		return "1".equals(mPersistentStorage.getString("move_sysapps"));
	}
	
	public void move_sysapps(Boolean value) {
		update("move_sysapps", (value ? "1" : "0"));
	}
	
	public Boolean move_dalvik() {
		return "1".equals(mPersistentStorage.getString("move_dalvik"));
	}
	
	public void move_dalvik(Boolean value) {
		update("move_dalvik", (value ? "1" : "0"));
	}
	
	public Boolean move_data() {
		return "1".equals(mPersistentStorage.getString("move_data"));
	}
	
	public void move_data(Boolean value) {
		update("move_data", (value ? "1" : "0"));
	}
	
	public Boolean move_libs() {
		return "1".equals(mPersistentStorage.getString("move_libs"));
	}
	
	public void move_libs(Boolean value) {
		update("move_libs", (value ? "1" : "0"));
	}
	
	public Boolean move_media() {
		return "1".equals(mPersistentStorage.getString("move_media"));
	}
	
	public void move_media(Boolean value) {
		update("move_media", (value ? "1" : "0"));
	}
	
	public Boolean move_system() {
		return "1".equals(mPersistentStorage.getString("move_system"));
	}
	
	public void move_system(Boolean value) {
		update("move_system", (value ? "1" : "0"));
	}
	
	public Integer enable_cache() {
		return "2".equals(mPersistentStorage.getString("enable_cache")) ? STATE_AUTO : 
			"1".equals(mPersistentStorage.getString("enable_cache")) ? STATE_ON : STATE_OFF;
	}
	
	public void enable_cache(Integer value) {
		if (value == STATE_ON || value == STATE_OFF || value == STATE_AUTO) {
			update("enable_cache", "" + value);
		}
	}
	
	public Boolean enable_swap() {
		return "1".equals(mPersistentStorage.getString("enable_swap"));
	}
	
	public void enable_swap(Boolean value) {
		update("enable_swap", (value ? "1" : "0"));
	}
	
	public Integer enable_sdext_journal() {
		return "2".equals(mPersistentStorage.getString("enable_sdext_journal")) ? STATE_AUTO : 
			"1".equals(mPersistentStorage.getString("enable_sdext_journal")) ? STATE_ON : STATE_OFF;
	}
	
	public void enable_sdext_journal(Integer value) {
		if (value == STATE_ON || value == STATE_OFF || value == STATE_AUTO) {
			update("enable_sdext_journal", "" + value);
		}
	}
	
	public Boolean enable_debug() {
		return "1".equals(mPersistentStorage.getString("enable_debug"));
	}
	
	public void enable_debug(Boolean value) {
		update("enable_debug", (value ? "1" : "0"));
	}
	
	public Integer set_swap_level() {
		try {
			return Integer.valueOf(mPersistentStorage.getString("set_swap_level"));
		
		} catch(NumberFormatException e) {}
		
		return 0;
	}
	
	public void set_swap_level(Integer value) {
		update("set_swap_level", "" + value);
	}
	
	public String set_sdext_fstype() {
		return mPersistentStorage.getString("set_sdext_fstype");
	}
	
	public void set_sdext_fstype(String value) {
		update("set_sdext_fstype", value);
	}
	
	public Boolean run_sdext_fschk() {
		return "1".equals(mPersistentStorage.getString("run_sdext_fschk"));
	}
	
	public void run_sdext_fschk(Boolean value) {
		update("run_sdext_fschk", (value ? "1" : "0"));
	}
	
	public Integer set_storage_threshold() {
		try {
			return Integer.valueOf(mPersistentStorage.getString("set_storage_threshold"));
		
		} catch(NumberFormatException e) {}
		
		return 0;
	}
	
	public void set_storage_threshold(Integer value) {
		if ((int) value <= 25 && (int) value >= 0) {
			update("set_storage_threshold", "" + value);
		}
	}
	
	public Integer set_zram_compression() {
		try {
			return Integer.valueOf(mPersistentStorage.getString("set_zram_compression"));
		
		} catch(NumberFormatException e) {}
		
		return 0;
	}
	
	public void set_zram_compression(Integer value) {
		if ((int) value <= 25 && (int) value >= 0) {
			update("set_zram_compression", "" + value);
		}
	}
	
	public Integer set_emmc_readahead() {
		try {
			return Integer.valueOf(mPersistentStorage.getString("set_emmc_readahead"));
		
		} catch(NumberFormatException e) {}
		
		return 0;
	}
	
	public void set_emmc_readahead(Integer value) {
		update("set_emmc_readahead", "" + value);
	}
	
	public String set_emmc_scheduler() {
		return mPersistentStorage.getString("set_emmc_scheduler");
	}
	
	public void set_emmc_scheduler(String value) {
		update("set_emmc_scheduler", value);
	}
	
	public Integer set_immc_readahead() {
		try {
			return Integer.valueOf(mPersistentStorage.getString("set_immc_readahead"));
		
		} catch(NumberFormatException e) {}
		
		return 0;
	}
	
	public void set_immc_readahead(Integer value) {
		update("set_immc_readahead", "" + value);
	}
	
	public String set_immc_scheduler() {
		return mPersistentStorage.getString("set_immc_scheduler");
	}
	
	public void set_immc_scheduler(String value) {
		update("set_immc_scheduler", value);
	}
	
	public Boolean disable_safemode() {
		return "1".equals(mPersistentStorage.getString("disable_safemode"));
	}
	
	public void disable_safemode(Boolean value) {
		update("disable_safemode", (value ? "1" : "0"));
	}
}
