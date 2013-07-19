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

package com.spazedog.mounts2sd.tools;

import java.io.File;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Map;

import android.annotation.TargetApi;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Build;
import android.os.Bundle;
import android.os.SystemClock;
import android.os.UserManager;
import android.util.Log;

import com.spazedog.lib.rootfw.container.Data;
import com.spazedog.lib.rootfw.container.DiskStat;
import com.spazedog.lib.rootfw.container.FileStat;
import com.spazedog.lib.rootfw.container.FstabEntry;
import com.spazedog.lib.rootfw.container.ShellResult;
import com.spazedog.lib.rootfw.container.SwapStat;
import com.spazedog.mounts2sd.R;
import com.spazedog.mounts2sd.tools.containers.ApplicationSettings;
import com.spazedog.mounts2sd.tools.containers.DeviceConfig;
import com.spazedog.mounts2sd.tools.containers.DeviceProperties;
import com.spazedog.mounts2sd.tools.containers.DeviceSetup;

public class Preferences {
	
	private final static Object oLock = new Object();
	
	private DeviceSetup mDeviceSetup;
	private DeviceConfig mDeviceConfig;
	private DeviceProperties mDeviceProperties;
	
	private ApplicationSettings mApplicationSettings;
	
	private static Boolean oCacheChecked = false;
	
	private static Integer oTheme;
	
	private Context mContext;

	public Preferences(Context aContext) {
		mContext = aContext;
	}
	
	public Integer theme() {
		if (oTheme == null) {
			oTheme = settings().use_dark_theme() ? 
					(settings().use_global_settings_style() ? R.style.Theme_Dark_Settings : R.style.Theme_Dark) : 
						(settings().use_global_settings_style() ? R.style.Theme_Settings : R.style.Theme);
		}
		
		return oTheme;
	}
	
	public void restartApplication() {
		Intent intent = mContext.getPackageManager().getLaunchIntentForPackage( mContext.getPackageName() );
		PendingIntent pending = PendingIntent.getActivity(mContext, 82806298, intent, PendingIntent.FLAG_CANCEL_CURRENT);
		
		((AlarmManager) mContext.getSystemService(Context.ALARM_SERVICE)).set(AlarmManager.RTC, System.currentTimeMillis() + 100, pending);
		
		System.exit(0);
	}
	
	public ApplicationSettings settings() {
		if (mApplicationSettings == null) {
			mApplicationSettings = new ApplicationSettings(this);
		}
		
		return mApplicationSettings;
	}
	
	public Boolean checkDeviceSetup() {
		return cached().getBoolean("DeviceSetup.Loaded", false);
	}
	
	public Boolean checkDeviceConfig() {
		return cached().getBoolean("DeviceConfig.Loaded", false);
	}
	
	public Boolean checkDeviceProperties() {
		return cached().getBoolean("DeviceProperties.Loaded", false);
	}
	
	public DeviceSetup deviceSetup() {
		if (mDeviceSetup == null) {
			mDeviceSetup = new DeviceSetup(this);
		}
		
		return mDeviceSetup;
	}
	
	public DeviceConfig deviceConfig() {
		if (mDeviceConfig == null) {
			mDeviceConfig = new DeviceConfig(this);
		}
		
		return mDeviceConfig;
	}
	
	public DeviceProperties deviceProperties() {
		if (mDeviceProperties == null) {
			mDeviceProperties = new DeviceProperties(this);
		}
		
		return mDeviceProperties;
	}
	
	public Boolean loadDeviceSetup(Boolean aForce) {
		synchronized(oLock) {
			if (!checkDeviceSetup() || aForce) {
				Bundle lBundle = new Bundle();
				
				if (settings().use_builtin_busybox()) {
					String pathBusybox = mContext.getResources().getString(R.string.config_path_busybox);
					
					if (!(new File(pathBusybox)).isFile()) {
						Shell.connection.file.copyResource(mContext, "busybox", pathBusybox, "0777", "0", "0");
					}
				}
				
				if (Shell.connection.binary.exists("busybox")) {
					lBundle.putBoolean("environment_busybox", true);
					
					String[] looper;
					String scriptPath = mContext.getResources().getString(R.string.config_path_script);
					
					if ((new File(scriptPath)).isFile()) {
						lBundle.putBoolean("environment_startup_script", true);
						
						Data id = Shell.connection.file.grep(scriptPath, "@id", true);
						Data version = Shell.connection.file.grep(scriptPath, "@version", true);
						
						if (id != null) {
							lBundle.putInt("id_startup_script", Integer.valueOf( id.line().substring( id.line().lastIndexOf(" ") + 1 ) ));
						}
						
						if (version != null) {
							lBundle.putString("version_startup_script", version.line().substring( version.line().lastIndexOf(" ") + 1 ) );
						}
					}
					
					for (int i=0; i < (looper = new String[]{"/system/xbin/busybox", "/system/bin/busybox", "/system/sbin/busybox", "/sbin/busybox"}).length; i++) {
						if ((new File(looper[i])).isFile()) {
							lBundle.putBoolean("environment_multiple_binaries", true); break;
						}
					}
					
					for (int i=0; i < 10; i++) {
						String mmcType = Shell.connection.file.readLine("/sys/block/mmcblk" + i + "/device/type");
						
						if (mmcType != null) {
							if ("MMC".equals(mmcType)) {
								lBundle.putString("path_device_map_immc", "/dev/block/mmcblk" + i);
								
							} else if ("SD".equals(mmcType)) {
								lBundle.putString("path_device_map_emmc", "/dev/block/mmcblk" + i);
								
								for (int x=2; x < 4; x++) {
									if ((new File("/dev/block/mmcblk" + i + "p" + x)).exists()) {
										switch(x) {
											case 2: 
												lBundle.putString("path_device_map_sdext", "/dev/block/mmcblk" + i + "p" + x); 
												lBundle.putString("type_device_sdext", Shell.connection.filesystem.getType(lBundle.getString("path_device_map_sdext"))); break;
												
											case 3: lBundle.putString("path_device_map_swap", "/dev/block/mmcblk" + i + "p" + x);
										}
									}
								}
							}
							
						} else {
							if (lBundle.getString("path_device_map_immc") == null) {
								if ((new File("/dev/block/mtdblock0")).exists()) {
									lBundle.putString("path_device_map_immc", "/dev/block/mtdblock0");
									
								} else if ((new File("/dev/block/bml0!c")).exists()) {
									lBundle.putString("path_device_map_immc", "/dev/block/bml0!c");
								}
							}
							
							break;
						}
					}
					
					for (int i=0; i < (looper = new String[]{"/data", "/cache"}).length; i++) {
						FstabEntry fstab = Shell.connection.filesystem.statFstab(looper[i]);
						String device = null;
						
						if (fstab != null) {
							device = fstab.device();
							
						} else {
							DiskStat diskstat = Shell.connection.filesystem.statDisk(looper[i]);
							
							if (diskstat != null) {
								device = diskstat.device();
							}
						}
	
						switch(i) {
							case 0: lBundle.putString("path_device_map_data", device); break;
							case 1: lBundle.putString("path_device_map_cache", device);
						}
					}
					
					for (int i=0; i < (looper = new String[]{lBundle.getString("path_device_map_immc"), lBundle.getString("path_device_map_emmc")}).length; i++) {
						if (looper[i] != null) {
							FileStat filestat = Shell.connection.file.stat(looper[i]);
							
							if (filestat != null) {
								String lReadaheadFile = "/sys/devices/virtual/bdi/" + filestat.mm() + "/read_ahead_kb";
								String lSchedulerFile = "/sys/block/" + looper[i].substring(looper[i].lastIndexOf("/") + 1) + "/queue/scheduler";
								
								switch(i) { 
									case 0: 
										lBundle.putString("path_device_readahead_immc", (new File(lReadaheadFile)).exists() ? lReadaheadFile : null);
										lBundle.putString("path_device_scheduler_immc", (new File(lSchedulerFile)).exists() ? lSchedulerFile : null); break;
										
									case 1: 
										lBundle.putString("path_device_readahead_emmc", (new File(lReadaheadFile)).exists() ? lReadaheadFile : null);
										lBundle.putString("path_device_scheduler_emmc", (new File(lSchedulerFile)).exists() ? lSchedulerFile : null);
								}
							}
						}
					}
					
					for (int i=0; i < (looper = new String[]{"/dev/block/zram0", "/system/lib/modules/zram.ko", "/system/lib/modules/ramzswap.ko", "/dev/block/ramzswap0"}).length; i++) {
						if ((new File(looper[i])).exists()) {
							lBundle.putString("path_device_map_zram", i < 2 ? "/dev/block/zram0" : (Shell.connection.binary.exists("rzscontrol") ? "/dev/block/ramzswap0" : null)); break;
						}
					}
					
					for (int i=0; i < (looper = new String[]{"tune2fs", "sqlite3", "e2fsck"}).length; i++) {
						lBundle.putBoolean("support_binary_" + looper[i], Shell.connection.binary.exists(looper[i]));
					}
					
					for (int i=0; i < (looper = Shell.connection.file.list("/system")).length; i++) {
						if ((new File("/data/" + looper[i] + "_s")).isDirectory()) {
							lBundle.putString("paths_directory_system", (lBundle.containsKey("paths_directory_system") ? lBundle.getString("paths_directory_system") + "," : "") + looper[i] + "_s");
						}
					}
					
					lBundle.putBoolean("support_option_swap", (new File("/proc/swaps")).exists() && lBundle.getString("path_device_map_swap") != null);
					lBundle.putBoolean("support_option_zram", (new File("/proc/swaps")).exists() && lBundle.getString("path_device_map_zram") != null);
					lBundle.putBoolean("support_directory_library", (new File("/data/app-lib")).isDirectory());
					lBundle.putBoolean("support_directory_user", (new File("/data/user")).isDirectory());
					lBundle.putBoolean("support_directory_media", (new File("/data/media")).isDirectory());
					lBundle.putBoolean("support_directory_system", lBundle.getString("paths_directory_system") != null);
					lBundle.putBoolean("support_directory_cmdalvik", (new File("/cache/dalvik-cache")).isDirectory() && !"1".equals(Shell.connection.shell.execute("getprop dalvik.vm.dexopt-data-only").output().toString(true)));
					lBundle.putBoolean("support_device_mtd", (new File("/proc/mtd")).exists());
					
					lBundle.putString("init_implementation", "service".equals( Shell.connection.file.readLine( mContext.getResources().getString(R.string.config_dir_tmp) + "/init.type" ) ) ? "service" : "internal");
					lBundle.putBoolean("safemode", "1".equals( Shell.connection.file.readLine( mContext.getResources().getString(R.string.config_dir_tmp) + "/safemode.result" ) ) ? true : false);
					
					cached().putBoolean("DeviceSetup.Loaded", true);
					
					cached("DeviceSetup").putAll(lBundle);
					
					return true;
					
				} else {
					cached("DeviceSetup").putBoolean("environment_busybox", false);
				}
				
			} else {
				return true;
			}
			
			return false;
		}
	}

	public boolean loadDeviceConfig(Boolean aForce) {
		synchronized(oLock) {
			if (checkDeviceSetup()) {
				if (!checkDeviceConfig() || aForce) {
					Bundle lBundle = new Bundle();
					
					DeviceSetup deviceSetup = deviceSetup();
					String[] looper;
					Boolean reversedMount = false;
					
					for (int i=0; i < (looper = new String[]{mContext.getResources().getString(R.string.config_dir_sdext), "/data", "/cache"}).length; i++) {
						/* We cannot just use stat.location() as we cannot trust the order in which devices are located in the /proc/mounts version
						 * available to app processes. Android does not provide the original version, but a re-structured one. For an example
						 * you could end up getting /sd-ext/dalvik-cache as the sd-ext mount location, because this has been placed before /sd-ext in /proc/mounts.
						 * So we will have to investigate a little bit in order to locate the correct locations. 
						 */
						DiskStat stat = Shell.connection.filesystem.statDisk(looper[i]);
						
						if (stat != null) {
							switch(i) {
								case 0: 
									/* M2SD no longer support reversing the mount points, but if this app was just installed, another script might be loaded that does */
									lBundle.putString("location_storage_sdext", stat.device().equals(deviceSetup.path_device_map_data()) ? "/data" : 
										/* We cannot be sure that sd-ext is mounted at /sd-ext. Some scripts uses alternative locations */
										Shell.connection.filesystem.checkMount(deviceSetup.path_device_map_sdext()) ? mContext.getResources().getString(R.string.config_dir_sdext) : null); break;
										
								case 1: 
									lBundle.putString("location_storage_data", (reversedMount = stat.device().equals(deviceSetup.path_device_map_sdext())) ? mContext.getResources().getString(R.string.config_dir_sdext) : "/data"); break;
									
								case 2: 
									lBundle.putString("location_storage_cache", stat.device().equals(deviceSetup.path_device_map_sdext()) ? 
											mContext.getResources().getString(R.string.config_dir_sdext) + "/cache" : 
												stat.device().equals(deviceSetup.path_device_map_data()) ? "/data/cache" : "/cache");
							}
						}
					}

					for (int i=0; i < (looper = new String[]{"app", "dalvik-cache", (deviceSetup.support_directory_user() ? "user" : "data"), (deviceSetup.support_directory_library() ? "app-lib" : null), (deviceSetup.support_directory_media() ? "media" : null), (deviceSetup.support_directory_system() ? deviceSetup.paths_directory_system()[0] : null)}).length; i++) {
						if (looper[i] != null) {
							DiskStat stat = null;
							
							if (lBundle.getString("location_storage_sdext") == null || (stat = Shell.connection.filesystem.statDisk("/data/" + looper[i])) != null) {
								Boolean status = lBundle.getString("location_storage_sdext") == null ? false : stat.device().equals(deviceSetup.path_device_map_sdext()) && !reversedMount;
								
								switch(i) {
									case 0: 
										lBundle.putBoolean("status_content_apps", status);
										lBundle.putLong("usage_content_apps", Shell.connection.file.diskUsage(new String[]{"/data/app", "/data/app-private", "/data/app-asec", "/data/app-system"})); break;
										
									case 1: 
										lBundle.putBoolean("status_content_dalvik", status);
										lBundle.putLong("usage_content_dalvik", Shell.connection.file.diskUsage("/data/dalvik-cache")); break;
										
									case 2: 
										lBundle.putBoolean("status_content_data", status);
										lBundle.putLong("usage_content_data", Shell.connection.file.diskUsage(new String[]{"/data/data", "/data/user"})); break;
										
									case 3: 
										lBundle.putBoolean("status_content_libs", status);
										lBundle.putLong("usage_content_libs", Shell.connection.file.diskUsage("/data/app-lib")); break;
										
									case 4: 
										lBundle.putBoolean("status_content_media", status);
										lBundle.putLong("usage_content_media", Shell.connection.file.diskUsage("/data/media")); break;
										
									case 5: 
										lBundle.putBoolean("status_content_system", status);
										lBundle.putLong("usage_content_system", deviceSetup.paths_directory_system() == null ? 0L : Shell.connection.file.diskUsage(deviceSetup.paths_directory_system()));
								}
							}
						}
					}
						
					if (deviceSetup.path_device_map_sdext() != null) {
						if (deviceSetup.support_binary_e2fsck()) {
							String iResult = Shell.connection.file.readLine("/tmp/e2fsck.result");
							lBundle.putInt("level_filesystem_fschk", iResult != null ? Integer.parseInt(iResult) : -1);
						}
						
						if (deviceSetup.support_binary_tune2fs() && "ext4".equals(deviceSetup.type_device_sdext())) {
							ShellResult result = Shell.connection.shell.execute("tune2fs -l " + deviceSetup.path_device_map_sdext());
							
							if (result != null && result.code() == 0) {
								lBundle.putBoolean("status_filesystem_journal", result.output().toString().contains("has_journal"));
							}
						}
					}
					
					if (lBundle.getString("location_storage_sdext") != null) {
						try {
							lBundle.putString("type_filesystem_driver", Shell.connection.filesystem.statMount(deviceSetup.path_device_map_sdext()).fstype());
							
						} catch (Throwable e) {}
					}
					
					for (int i=0; i < (looper = new String[]{deviceSetup.path_device_scheduler_immc(), deviceSetup.path_device_scheduler_emmc(), deviceSetup.path_device_readahead_immc(), deviceSetup.path_device_readahead_emmc()}).length; i++) {
						if (looper[i] != null) {
							String lLine = Shell.connection.file.readLine(looper[i]);
							
							if (lLine != null) {
								switch(i) {
									case 0: lBundle.putString("value_immc_scheduler", lLine.substring(lLine.indexOf("[")+1, lLine.lastIndexOf("]"))); break;
									case 1: lBundle.putString("value_emmc_scheduler", lLine.substring(lLine.indexOf("[")+1, lLine.lastIndexOf("]"))); break;
									case 2: lBundle.putString("value_immc_readahead", lLine); break;
									case 3: lBundle.putString("value_emmc_readahead", lLine);
								}
							}
						}
					}
					
					if (deviceSetup.support_option_swap()) {
						ArrayList<SwapStat> stat = Shell.connection.memory.swaps();
						
						if (stat != null) {
							for (int i=0; i < stat.size(); i++) {
								if (deviceSetup.path_device_map_swap() != null && stat.get(i).device().equals(deviceSetup.path_device_map_swap())) {
									lBundle.putLong("size_memory_swap", stat.get(i).size());
									lBundle.putLong("usage_memory_swap", stat.get(i).usage());
									
								} else if (deviceSetup.support_option_zram() && stat.get(i).device().equals(deviceSetup.path_device_map_zram())) {
									lBundle.putLong("size_memory_zram", stat.get(i).size());
									lBundle.putLong("usage_memory_zram", stat.get(i).usage());
								}
							}
						}
						
						lBundle.putInt("level_memory_swappiness", Integer.parseInt(Shell.connection.file.readLine("/proc/sys/vm/swappiness")));
					}
					
					if (deviceSetup.support_binary_sqlite3()) {
						ShellResult result = Shell.connection.shell.execute("sqlite3 /data/data/com.android.providers.settings/databases/settings.db \"select value from secure where name = 'sys_storage_threshold_percentage'\"");
						
						if (result != null && result.code() == 0 && result.output().length() > 0) {
							Double threshold = Double.parseDouble( result.output().line() ) / 100;
							Double size = Utils.getDiskTotal("/data") * threshold;
							
							lBundle.putLong("size_storage_threshold", size.longValue());
						}
					}
					
					cached().putBoolean("DeviceConfig.Loaded", true);
					
					cached("DeviceConfig").putAll(lBundle);
					
					return true;
					
				} else {
					return true;
				}
			}
			
			return false;
		}
	}
	
	public boolean loadDeviceProperties(Boolean aForce) {
		synchronized(oLock) {
			if (checkDeviceSetup()) {
				if (!checkDeviceProperties() || aForce) {
					Bundle lBundle = new Bundle();
					
					DeviceSetup deviceSetup = deviceSetup();
					String dirProperty = mContext.getResources().getString(R.string.config_dir_properties);
					String[] looper = new String[]{"move_apps", "move_dalvik", "move_data", "move_libs", "move_media", "move_system", "enable_cache", "enable_swap", "enable_sdext_journal", "enable_debug", "set_swap_level", "set_sdext_fstype", "run_sdext_fschk", "set_storage_threshold", "set_zram_compression", "set_emmc_readahead", "set_emmc_scheduler", "set_immc_readahead", "set_immc_scheduler", "disable_safemode"};
					
					for (int i=0; i < looper.length; i++) {
						String lLine = Shell.connection.file.readLine(dirProperty + "/m2sd." + looper[i]);
						
						if (lLine == null) {
							if (looper[i].equals("move_apps") || looper[i].equals("disable_safemode")) {
								lLine = "1";
								
							} else if (looper[i].equals("enable_swap")) {
								lLine = (deviceSetup.path_device_map_swap() != null && deviceSetup.support_option_swap()) ? "1" : "0";
								
							} else if (looper[i].equals("enable_sdext_journal")) {
								lLine = deviceSetup.support_binary_tune2fs() ? "2" : "1";
								
							} else if (looper[i].equals("set_sdext_fstype")) {
								lLine = Shell.connection.filesystem.typeSupported("ext4") ? "ext4" : "auto";
								
							} else if (looper[i].equals("run_sdext_fschk")) {
								lLine = deviceSetup.support_binary_e2fsck() ? "1" : "0";
								
							} else if (looper[i].equals("set_storage_threshold")) {
								lLine = deviceSetup.support_binary_sqlite3() ? "1" : "0";
								
							} else if (looper[i].equals("set_zram_compression")) {
								lLine = deviceSetup.support_option_zram() ? "18" : "0";
								
							} else if (looper[i].equals("set_emmc_readahead")) {
								lLine = "512";
								
							} else if (looper[i].equals("set_emmc_scheduler")) {
								lLine = "cfq";
								
							} else if (looper[i].equals("set_immc_readahead")) {
								lLine = deviceSetup.support_device_mtd() ? "4" : "128";
								
							} else if (looper[i].equals("set_immc_scheduler")) {
								lLine = deviceSetup.support_device_mtd() ? "deadline" : "cfq";
								
							} else {
								lLine = "0";
							}
							
							Shell.connection.file.write(dirProperty + "/m2sd." + looper[i], lLine);
						}
						
						lBundle.putString(looper[i], lLine);
					}
					
					cached().putBoolean("DeviceProperties.Loaded", true);

					cached("DeviceProperties").putAll(lBundle);
					
					return true;
					
				} else {
					return true;
				}
			}
			
			return false;
		}
	}
	
	public void saveDeviceProperties() {
		synchronized(oLock) {
			DeviceProperties deviceProperties = deviceProperties();
			
			if (deviceProperties != null && deviceProperties.hasUpdated()) {
				String dirProperty = mContext.getResources().getString(R.string.config_dir_properties);
				String name;
				
				while ((name = deviceProperties.nextUpdated()) != null) {
					Shell.connection.file.write(dirProperty + "/m2sd." + name, "" + cached("DeviceProperties").getString(name));
				}
			}
		}
	}
	
	public String getSelectorValue(String aSelector, String aValue) {
		Integer iSelectorNames = mContext.getResources().getIdentifier("selector_" + aSelector + "_names", "array", mContext.getPackageName());
		Integer iSelectorValues = mContext.getResources().getIdentifier("selector_" + aSelector + "_values", "array", mContext.getPackageName());
		
		if (iSelectorNames != 0 && iSelectorValues != 0) {
			String[] lSelectorNames = mContext.getResources().getStringArray(iSelectorNames);
			String[] lSelectorValues = mContext.getResources().getStringArray(iSelectorValues);
			
			for (int i=0; i < lSelectorValues.length; i++) {
				if (lSelectorValues[i].equals(aValue)) {
					return lSelectorNames[i];
				}
			}
		}
		
		return mContext.getResources().getString(R.string.status_unknown);
	}
	
	public PersistentPreferences stored() {
		return stored("Preferences");
	}
	
	public PersistentPreferences stored(String aName) {
		return new PersistentPreferences(aName, mContext.getSharedPreferences("settings", 0x00000000));
	}
	
	public PersistentPreferences cached() {
		return cached("Preferences");
	}
	
	public PersistentPreferences cached(String aName) {
		SharedPreferences preferences = mContext.getSharedPreferences("cache", 0x00000000);
		
		if (!oCacheChecked) {
			String appid = mContext.getResources().getString(R.string.config_application_id);

			if (!Shell.connection.file.check("/tmp/application.lock", "e") || !appid.equals("" + preferences.getInt("android.appId", 0))) {
				Editor edit = preferences.edit();
				
				edit.clear();
				edit.putInt("android.appId", Integer.parseInt(appid));
				edit.commit();
				
				Shell.connection.filesystem.mount("/", new String[]{"remount", "rw"});
				if (!Shell.connection.file.check("/tmp", "d")) {
					Shell.connection.file.create("/tmp");
				}
				Shell.connection.file.write("/tmp/application.lock", "1");
				Shell.connection.filesystem.mount("/", new String[]{"remount", "ro"});
			}
			
			oCacheChecked = true;
		}

		return new PersistentPreferences(aName, preferences);
	}
	
	public static class PersistentPreferences {
		private SharedPreferences mSharedPreferences;
		private String mName;
		
		public PersistentPreferences(String aName, SharedPreferences aSharedPreferences) {
			mSharedPreferences = aSharedPreferences;
			mName = aName;
		}
		
		public Object find(String aName) {
			if (mSharedPreferences.contains(aName)) {
				return mSharedPreferences.getAll().get(mName + ":" + aName);
			}
			
			return null;
		}
		
		public String getString(String aName) {
			return mSharedPreferences.getString(mName + ":" + aName, null);
		}
		
		public String getString(String aName, String aDefault) {
			return mSharedPreferences.getString(mName + ":" + aName, aDefault);
		}
		
		public Boolean getBoolean(String aName) {
			return mSharedPreferences.getBoolean(mName + ":" + aName, false);
		}
		
		public Boolean getBoolean(String aName, Boolean aDefault) {
			return mSharedPreferences.getBoolean(mName + ":" + aName, aDefault);
		}
		
		public Integer getInteger(String aName) {
			return mSharedPreferences.getInt(mName + ":" + aName, -1);
		}
		
		public Integer getInteger(String aName, Integer aDefault) {
			return mSharedPreferences.getInt(mName + ":" + aName, aDefault);
		}
		
		public Long getLong(String aName, Long aDefault) {
			return mSharedPreferences.getLong(mName + ":" + aName, aDefault);
		}
		
		public Bundle getAll() {
			if (mSharedPreferences.getAll().size() > 0) {
				Bundle bundle = new Bundle();
				Integer count = 0;
				
				for(Map.Entry<String,?> entry : ((Map<String,?>) mSharedPreferences.getAll()).entrySet()) {
					String key = entry.getKey();
					
					if (key.startsWith(mName + ":")) {
						key = key.substring(mName.length()+1);
						count += 1;
						
						if (entry.getValue() instanceof Integer) {
							bundle.putInt(key, (Integer) entry.getValue());
							
						} else if (entry.getValue() instanceof Long) {
							bundle.putLong(key, (Long) entry.getValue());
							
						}  else if (entry.getValue() instanceof Boolean) {
							bundle.putBoolean(key, (Boolean) entry.getValue());
							
						} else {
							bundle.putString(key, (String) entry.getValue());
						}
					}
				}
				
				return count > 0 ? bundle : null;
			}
			
			return null;
		}
		
		public PersistentPreferences putString(String aName, String aValue) {
			mSharedPreferences.edit().putString(mName + ":" + aName, aValue).commit();
			
			return this;
		}
		
		public PersistentPreferences putBoolean(String aName, Boolean aValue) {
			mSharedPreferences.edit().putBoolean(mName + ":" + aName, aValue).commit();
			
			return this;
		}
		
		public PersistentPreferences putInteger(String aName, Integer aValue) {
			mSharedPreferences.edit().putInt(mName + ":" + aName, aValue).commit();
			
			return this;
		}
		
		public PersistentPreferences putLong(String aName, Long aValue) {
			mSharedPreferences.edit().putLong(mName + ":" + aName, aValue).commit();
			
			return this;
		}
		
		public PersistentPreferences putAll(Bundle aBundle) {
			Editor editor = mSharedPreferences.edit();
			
			for (String key : aBundle.keySet()) {
				if (aBundle.get(key) instanceof Integer) {
					editor.putInt(mName + ":" + key, (Integer) aBundle.get(key));
					
				} else if (aBundle.get(key) instanceof Long) {
					editor.putLong(mName + ":" + key, (Long) aBundle.get(key));
					
				} else if (aBundle.get(key) instanceof Boolean) {
					editor.putBoolean(mName + ":" + key, (Boolean) aBundle.get(key));
					
				} else if (aBundle.get(key) instanceof String) {
					editor.putString(mName + ":" + key, (String) aBundle.get(key));
				}
			}
			
			editor.commit();
			
			return this;
		}
	}
	
	@SuppressWarnings("deprecation")
	@TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
	public boolean isUserOwner() {
		if (Integer.valueOf(android.os.Build.VERSION.SDK) >= 17) {
		    try {
		        Method getUserHandle = UserManager.class.getMethod("getUserHandle");
		        int userHandle = (Integer) getUserHandle.invoke(mContext.getSystemService(Context.USER_SERVICE));
		        
		        return userHandle == 0;
		        
		    } catch (Exception e) {}
		}
		
		return true;
	}
}
