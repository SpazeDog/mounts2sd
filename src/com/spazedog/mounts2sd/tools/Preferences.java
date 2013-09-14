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

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
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
import android.os.UserManager;
import android.text.TextUtils;

import com.spazedog.lib.rootfw3.RootFW;
import com.spazedog.lib.rootfw3.extenders.FileExtender;
import com.spazedog.lib.rootfw3.extenders.FileExtender.FileStat;
import com.spazedog.lib.rootfw3.extenders.FilesystemExtender.DiskStat;
import com.spazedog.lib.rootfw3.extenders.FilesystemExtender.MountStat;
import com.spazedog.lib.rootfw3.extenders.MemoryExtender.SwapStat;
import com.spazedog.lib.rootfw3.extenders.ShellExtender.ShellResult;
import com.spazedog.mounts2sd.R;
import com.spazedog.mounts2sd.tools.containers.ApplicationSession;
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
	
	private ApplicationSession mApplicationSession;
	
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
	
	public ApplicationSession session() {
		if (mApplicationSession == null) {
			mApplicationSession = new ApplicationSession();
		}
		
		return mApplicationSession;
	}
	
	public Boolean loadAll(Boolean forceCheck) {
		return loadDeviceSetup(forceCheck) && 
				loadDeviceConfig(forceCheck) && 
					loadDeviceProperties(forceCheck);
	}
	
	public Boolean checkAll() {
		return checkDeviceSetup() && 
				checkDeviceConfig() && 
					checkDeviceProperties();
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
	
	public Boolean loadDeviceSetup(Boolean forceCheck) {
		synchronized(oLock) {
			if (!checkDeviceSetup() || forceCheck) {
				Bundle setupData = new Bundle();
				RootFW rootfw = Root.open();
				
				String pathBusybox = mContext.getResources().getString(R.string.config_path_busybox);
				
				if (settings().use_builtin_busybox()) {
					FileExtender.File busyboxFile = rootfw.file(pathBusybox);
					
					if (!busyboxFile.exists() || 
							(!mContext.getResources().getString(R.string.config_busybox_checksum).equals(busyboxFile.getChecksum()) && busyboxFile.remove())) {
						
						busyboxFile.extractFromResource(mContext, "busybox", "0777", "0", "0");
					}
					
					setupData.putBoolean("environment_busybox_internal", busyboxFile.exists());
					
				} else {
					setupData.putBoolean("environment_busybox_internal", rootfw.file(pathBusybox).exists());
				}
				
				if (rootfw.busybox().exists()) {
					String[] loopContainer;
					FileExtender.File scriptFile = rootfw.file( mContext.getResources().getString(R.string.config_path_script) );
					
					if (rootfw.filesystem("/system").addMount(new String[]{"remount", "rw"}) && rootfw.file("/system/S_On.test").write("1") && "1".equals(rootfw.file("/system/S_On.test").readOneLine())) {
						rootfw.file("/system/S_On.test").remove();
						setupData.putBoolean("environment_secure_flag_off", true);
					}
					rootfw.filesystem("/system").addMount(new String[]{"remount", "ro"});
					
					if (scriptFile.exists() && rootfw.file( mContext.getResources().getString(R.string.config_path_runner) ).exists()) {
						String scriptId = scriptFile.readOneMatch("@id");
						String scriptVersion = scriptFile.readOneMatch("@version");
						
						setupData.putBoolean("environment_startup_script", true);
						
						try {
							if (scriptId != null) 
								setupData.putInt("id_startup_script", Integer.valueOf( scriptId.trim().substring( scriptId.trim().lastIndexOf(" ")+1 ) ));
							
						} catch (Throwable e) {}
						
						if (scriptVersion != null) 
							setupData.putString("version_startup_script", scriptVersion.substring( scriptVersion.lastIndexOf(" ") + 1 ));
					}
					
					for (int i=0; i < (loopContainer = new String[]{"/system/xbin/busybox", "/system/bin/busybox", "/system/sbin/busybox", "/sbin/busybox"}).length; i++) {
						if (rootfw.file(loopContainer[i]).exists()) {
							setupData.putBoolean("environment_multiple_binaries", true); break;
						}
					}
					
					for (int i=0; i < 10; i++) {
						FileExtender.File mmcTypeFile = rootfw.file("/sys/block/mmcblk" + i + "/device/type");
						
						if (mmcTypeFile.exists()) {
							String mmcType = mmcTypeFile.readOneLine();
							
							if ("MMC".equals(mmcType)) {
								setupData.putString("path_device_map_immc", "/dev/block/mmcblk" + i);
								
							} else if ("SD".equals(mmcType)) {
								setupData.putString("path_device_map_emmc", "/dev/block/mmcblk" + i);
								
								if (rootfw.file("/dev/block/mmcblk" + i + "p2").exists()) {
									setupData.putString("path_device_map_sdext", "/dev/block/mmcblk" + i + "p2"); 
									setupData.putString("type_device_sdext", rootfw.filesystem("/dev/block/mmcblk" + i + "p2").fsType(true));
								}
								
								if (rootfw.file("/dev/block/mmcblk" + i + "p3").exists())
									setupData.putString("path_device_map_swap", "/dev/block/mmcblk" + i + "p3");
							}
							
						} else {
							if (setupData.getString("path_device_map_immc") == null) {
								if (rootfw.file("/dev/block/mtdblock0").exists()) {
									setupData.putString("path_device_map_immc", "/dev/block/mtdblock0");
									
								} else if (rootfw.file("/dev/block/mtdblock0").exists()) {
									setupData.putString("path_device_map_immc", "/dev/block/bml0!c");
								}
							}
							
							break;
						}
					}
					
					for (int i=0; i < (loopContainer = new String[]{"/data", "/cache"}).length; i++) {
						MountStat stat = rootfw.filesystem(loopContainer[i]).statMount();
						
						if (stat == null || stat.device().equals(setupData.getString("path_device_map_sdext")) || (i > 0 && stat.device().equals(setupData.getString("path_device_map_data")))) {
							stat = rootfw.filesystem(loopContainer[i]).statFstab();
						}
						
						setupData.putString(i == 0 ? "path_device_map_data" : "path_device_map_cache", stat.device());
					}
					
					for (int i=0; i < (loopContainer = new String[]{setupData.getString("path_device_map_immc"), setupData.getString("path_device_map_emmc")}).length; i++) {
						if (loopContainer[i] != null) {
							FileStat stat = rootfw.file(loopContainer[i]).getDetails();
							
							if (stat != null) {
								String readaheadPath = "/sys/devices/virtual/bdi/" + stat.mm() + "/read_ahead_kb";
								String schedulerPath = "/sys/block/" + loopContainer[i].substring(loopContainer[i].lastIndexOf("/") + 1) + "/queue/scheduler";
								
								setupData.putString(i == 0 ? "path_device_readahead_immc" : "path_device_readahead_emmc", (rootfw.file(readaheadPath).exists() ? readaheadPath : null));
								setupData.putString(i == 0 ? "path_device_scheduler_immc" : "path_device_scheduler_emmc", (rootfw.file(schedulerPath).exists() ? schedulerPath : null));
							}
						}
					}
					
					for (int i=0; i < (loopContainer = new String[]{"/dev/block/zram0", "/system/lib/modules/zram.ko", "/system/lib/modules/ramzswap.ko", "/dev/block/ramzswap0"}).length; i++) {
						if (rootfw.file(loopContainer[i]).exists()) {
							setupData.putString("path_device_map_zram", i < 2 ? "/dev/block/zram0" : (rootfw.binary("rzscontrol").exists() ? "/dev/block/ramzswap0" : null));
						}
					}
					
					for (int i=0; i < (loopContainer = new String[]{"tune2fs", "sqlite3", "e2fsck"}).length; i++) {
						setupData.putBoolean("support_binary_" + loopContainer[i], rootfw.binary(loopContainer[i]).exists());
					}
					
					if ((loopContainer = rootfw.file("/system").getList()) != null) {
						List<String> systemDirs = new ArrayList<String>();
						
						for (int i=0; i < loopContainer.length; i++) {
							if (!loopContainer[i].equals(".") && !loopContainer[i].equals("..") && rootfw.file("/data/" + loopContainer[i] + "_s").isDirectory()) {
								systemDirs.add(loopContainer[i] + "_s");
							}
						}
						
						if (systemDirs.size() > 0) {
							setupData.putString("paths_directory_system", TextUtils.join(",", systemDirs));
						}
					}
					
					setupData.putBoolean("environment_busybox", true);
					setupData.putBoolean("support_option_swap", rootfw.file("/proc/swaps").exists() && setupData.getString("path_device_map_swap") != null);
					setupData.putBoolean("support_option_zram", rootfw.file("/proc/swaps").exists() && setupData.getString("path_device_map_zram") != null);
					setupData.putBoolean("support_directory_library", rootfw.file("/data/app-lib").isDirectory());
					setupData.putBoolean("support_directory_user", rootfw.file("/data/user").isDirectory());
					setupData.putBoolean("support_directory_media", rootfw.file("/data/media").isDirectory());
					setupData.putBoolean("support_directory_system", setupData.getString("paths_directory_system") != null);
					setupData.putBoolean("support_directory_cmdalvik", rootfw.file("/cache/dalvik-cache").isDirectory() && !"1".equals(rootfw.property().get("dalvik.vm.dexopt-data-only")));
					setupData.putBoolean("support_device_mtd", rootfw.file("/proc/mtd").exists());
					setupData.putBoolean("safemode", "1".equals(rootfw.file(mContext.getResources().getString(R.string.config_dir_tmp) + "/safemode.result").readOneLine()) ? true : false);
					setupData.putString("init_implementation", "service".equals(rootfw.file(mContext.getResources().getString(R.string.config_dir_tmp) + "/init.type").readOneLine()) ? "service" : "internal");
					
					try {
						setupData.putInt("log_level", Integer.parseInt( rootfw.file(mContext.getResources().getString(R.string.config_dir_tmp) + "/log.level").readOneLine() ));
						
					} catch (Throwable e) {}
					
					cached().putBoolean("DeviceSetup.Loaded", true);
					
					cached("DeviceSetup").putAll(setupData);
					
					Root.close();
					
					return true;
					
				} else {
					cached("DeviceSetup").putBoolean("environment_busybox", false);
				}
				
				Root.close();
				
			} else {
				return true;
			}
			
			return false;
		}
	}

	public boolean loadDeviceConfig(Boolean forceCheck) {
		synchronized(oLock) {
			if (checkDeviceSetup()) {
				if (!checkDeviceConfig() || forceCheck) {
					Bundle configData = new Bundle();
					RootFW rootfw = Root.open();
					DeviceSetup deviceSetup = deviceSetup();
					String[] loopContainer;
					
					String sdextLocation = mContext.getResources().getString(R.string.config_dir_sdext);
					DiskStat stat = rootfw.filesystem("/data").statDisk();
					
					if (stat != null) {
						String sdextDevice = deviceSetup.path_device_map_data().equals( stat.device() ) ? deviceSetup.path_device_map_sdext() : deviceSetup.path_device_map_data();
						
						configData.putString(deviceSetup.path_device_map_data().equals( stat.device() ) ? 
								"location_storage_data" : "location_storage_sdext", "/data");
						
						configData.putLong(deviceSetup.path_device_map_data().equals( stat.device() ) ? 
								"size_storage_data" : "size_storage_sdext", stat.size());
						
						configData.putLong(deviceSetup.path_device_map_data().equals( stat.device() ) ? 
								"usage_storage_data" : "usage_storage_sdext", stat.usage());
						
						if (sdextDevice != null && (sdextDevice.equals( deviceSetup.path_device_map_data() ) || rootfw.filesystem(sdextDevice).isMounted())) {
							stat = rootfw.filesystem(sdextLocation).statDisk();
							
							/* For some reason, some Android versions makes a restructured copy of /proc/mounts for application processes. 
							 * This means that some times, the mount order has been altered so we no longer know which location was mounted at first (Original location), 
							 * when working with --bind mounted locations. So we could end up getting for an example /sd-ext/dalvik-cache as the sd-ext location. 
							 * And since some script also uses alternative location for sd-ext, like Link2SD which uses /data/sdext, we can't just check if the device is mounted 
							 * and then assume it's on /sd-ext. So if the device is mounted but not on /sd-ext, we make a second mount location on /sd-ext that we can use to access sd-ext
							 * no mater where it is originally located (Our own little entry point). 
							 */
							if (stat == null || !stat.device().equals(sdextDevice)) {
								rootfw.filesystem("/").addMount(new String[]{"remount", "rw"});
								rootfw.file("/sd-ext").createDirectory();
								rootfw.filesystem("/").addMount(new String[]{"remount", "ro"});
								
								/* Toolbox mostly does not allow double mounting of a device. 
								 * So if we do not have busybox available, we need a fallback. 
								 * We cannot always trust an app process version of /proc/mounts, 
								 * but it is better than nothing. 
								 */
								if(!rootfw.filesystem(sdextDevice).addMount(sdextLocation)) {
									try {
										sdextLocation = rootfw.filesystem(sdextDevice).statMount().location();
										
									} catch(Throwable e) {}
								}
								
								stat = rootfw.filesystem(sdextLocation).statDisk();
							}
							
							if (stat != null) {
								configData.putString(deviceSetup.path_device_map_data().equals( stat.device() ) ? 
										"location_storage_data" : "location_storage_sdext", sdextLocation);
								
								configData.putLong(deviceSetup.path_device_map_data().equals( stat.device() ) ? 
										"size_storage_data" : "size_storage_sdext", stat.size());
								
								configData.putLong(deviceSetup.path_device_map_data().equals( stat.device() ) ? 
										"usage_storage_data" : "usage_storage_sdext", stat.usage());
							}
						}
					}
					
					if ((stat = rootfw.filesystem("/cache").statDisk()) != null) {
						configData.putString("location_storage_cache", stat.device().equals(deviceSetup.path_device_map_sdext()) ? 
								configData.getString("location_storage_sdext") + "/cache" : 
									stat.device().equals(deviceSetup.path_device_map_data()) ? 
											configData.getString("location_storage_data") + "/cache" : "/cache");
						
						configData.putLong("size_storage_cache", stat.size());
						configData.putLong("usage_storage_cache", stat.usage());
					}
					
					List<String[]> mountLooper = new ArrayList<String[]>();
					
					mountLooper.add(new String[]{"apps", "app", "app-private", "app-asec", "app-system"});
					mountLooper.add(new String[]{"dalvik", "dalvik-cache"});
					mountLooper.add(deviceSetup.support_directory_user() ? new String[]{"data", "data", "user"} : new String[]{"data", "data"});
					
					if (deviceSetup.support_directory_library()) {
						mountLooper.add(new String[]{"libs", "app-lib"});
					}
					
					if (deviceSetup.support_directory_media()) {
						mountLooper.add(new String[]{"media", "media"});
					}
					
					if (deviceSetup.support_directory_system()) {
						String[] sysDirectories = deviceSetup.paths_directory_system();
						
						if (sysDirectories != null) {
							String[] sysList = new String[ sysDirectories.length ];
							
							sysList[0] = "system";
							
							for (int i=0; i < sysDirectories.length; i++) {
								sysList[i+1] = sysDirectories[i];
							}
							
							mountLooper.add(sysList);
						}
					}
					
					for (int i=0; i < mountLooper.size(); i++) {
						String[] curOption = mountLooper.get(i);
						Integer curState = null;
						Long curUSage = 0L;
						
						for (int x=1; x < curOption.length; x++) {
							if (deviceSetup.path_device_map_sdext() != null && configData.getString("location_storage_sdext") != null) {
								DiskStat curStat = rootfw.filesystem("/data/" + curOption[x]).statDisk();
								
								if (curStat != null) {
									if (curState == null || curState >= 0) {
										curState = deviceSetup.path_device_map_data().equals(curStat.device()) ? 
												(curState == null || curState == 0 ? 0 : -1) : 
													(curState == null || curState == 1 ? 1 : -1);
									}
								}
								
							} else {
								curState = 0;
							}
							
							curUSage += rootfw.file("/data/" + curOption[x]).fileSize();
						}
						
						configData.putInt("status_content_" + curOption[0], curState);
						configData.putLong("usage_content_" + curOption[0], curUSage);
					}
					
					if (deviceSetup.path_device_map_sdext() != null) {
						if (deviceSetup.support_binary_e2fsck()) {
							String result = rootfw.file(mContext.getResources().getString(R.string.config_dir_tmp) + "/e2fsck.result").readOneLine();
							
							if (result != null) {
								try {
									configData.putInt("level_filesystem_fschk", Integer.parseInt(result));
									
								} catch(Throwable e) {}
							}
						}
						
						if (deviceSetup.support_binary_tune2fs() && "ext4".equals(rootfw.filesystem(deviceSetup.path_device_map_sdext()).fsType(true))) {
							ShellResult result = rootfw.shell().run("tune2fs -l '" + deviceSetup.path_device_map_sdext() + "'");
							
							configData.putInt("status_filesystem_journal", result.wasSuccessful() ? 
									( result.getString().contains("has_journal") ? 1 : 0 ) : 
										-1);
						}
					}
					
					if (configData.getString("location_storage_sdext") != null) {
						MountStat mountStat = rootfw.filesystem(deviceSetup.path_device_map_sdext()).statMount();
						
						if (mountStat != null) {
							configData.putString("type_filesystem_driver", mountStat.fstype());
						}
					}

					for (int i=0; i < (loopContainer = new String[]{deviceSetup.path_device_scheduler_immc(), deviceSetup.path_device_scheduler_emmc(), deviceSetup.path_device_readahead_immc(), deviceSetup.path_device_readahead_emmc()}).length; i++) {
						String line;
						
						if (loopContainer[i] != null && (line = rootfw.file(loopContainer[i]).readOneLine()) != null) {
							switch(i) {
								case 0: configData.putString("value_immc_scheduler", line.substring(line.indexOf("[")+1, line.lastIndexOf("]"))); break;
								case 1: configData.putString("value_emmc_scheduler", line.substring(line.indexOf("[")+1, line.lastIndexOf("]"))); break;
								case 2: configData.putString("value_immc_readahead", line); break;
								case 3: configData.putString("value_emmc_readahead", line);
							}
						}
					}
					
					if (deviceSetup.support_option_swap()) {
						SwapStat[] swapList = rootfw.memory().listSwaps();
						
						if (swapList != null) {
							for (int i=0; i < swapList.length; i++) {
								if (deviceSetup.support_option_swap() && swapList[i].device().equals(deviceSetup.path_device_map_swap())) {
									configData.putLong("size_memory_swap", swapList[i].size());
									configData.putLong("usage_memory_swap", swapList[i].usage());
									
								} else if (deviceSetup.support_option_zram() && swapList[i].device().equals(deviceSetup.path_device_map_zram())) {
									configData.putLong("size_memory_zram", swapList[i].size());
									configData.putLong("usage_memory_zram", swapList[i].usage());
								}
							}
						}
						
						try {
							configData.putInt("level_memory_swappiness", Integer.parseInt( rootfw.file("/proc/sys/vm/swappiness").readOneLine() ));
							
						} catch(Throwable e) {}
					}
					
					if (deviceSetup.support_binary_sqlite3()) {
						ShellResult result = rootfw.shell().run("sqlite3 /data/data/com.android.providers.settings/databases/settings.db \"select value from secure where name = 'sys_storage_threshold_percentage'\"");
						
						if (result.wasSuccessful()) {
							try {
								configData.putLong("size_storage_threshold", ((Double) (((Long) configData.getLong("size_storage_data")).doubleValue() * (Double.parseDouble( result.getLine() ) / 100))).longValue());
								
							} catch(Throwable e) {}
						}
					}

					cached().putBoolean("DeviceConfig.Loaded", true);
					
					cached("DeviceConfig").putAll(configData);
					
					Root.close();
					
					return true;
					
				} else {
					return true;
				}
			}
			
			return false;
		}
	}
	
	public boolean loadDeviceProperties(Boolean forceCheck) {
		synchronized(oLock) {
			if (checkDeviceSetup()) {
				if (!checkDeviceProperties() || forceCheck) {
					RootFW rootfw = Root.open();
					Bundle propData = new Bundle();
					DeviceSetup deviceSetup = deviceSetup();
					String dirProperty = mContext.getResources().getString(R.string.config_dir_properties);
					String[] loopContainer = new String[]{"move_apps", "move_dalvik", "move_data", "move_libs", "move_media", "move_system", "enable_cache", "enable_swap", "enable_sdext_journal", "enable_debug", "set_swap_level", "set_sdext_fstype", "run_sdext_fschk", "set_storage_threshold", "set_zram_compression", "set_emmc_readahead", "set_emmc_scheduler", "set_immc_readahead", "set_immc_scheduler", "disable_safemode"};
					
					for (int i=0; i < loopContainer.length; i++) {
						FileExtender.File propFile = rootfw.file(dirProperty + "/m2sd." + loopContainer[i]);
						String value = propFile.readOneLine();
						
						if (value == null) {
							if (loopContainer[i].equals("move_apps") || loopContainer[i].equals("disable_safemode")) {
								value = "1";
								
							} else if (loopContainer[i].equals("enable_swap")) {
								value = (deviceSetup.path_device_map_swap() != null && deviceSetup.support_option_swap()) ? "1" : "0";
								
							} else if (loopContainer[i].equals("enable_sdext_journal")) {
								value = deviceSetup.support_binary_tune2fs() ? "2" : "1";
								
							} else if (loopContainer[i].equals("set_sdext_fstype")) {
								value = rootfw.filesystem().hasTypeSupport("ext4") ? "ext4" : "auto";
								
							} else if (loopContainer[i].equals("run_sdext_fschk")) {
								value = deviceSetup.support_binary_e2fsck() ? "1" : "0";
								
							} else if (loopContainer[i].equals("set_storage_threshold")) {
								value = deviceSetup.support_binary_sqlite3() ? "1" : "0";
								
							} else if (loopContainer[i].equals("set_zram_compression")) {
								value = deviceSetup.support_option_zram() ? "18" : "0";
								
							} else if (loopContainer[i].equals("set_emmc_readahead")) {
								value = "512";
								
							} else if (loopContainer[i].equals("set_emmc_scheduler")) {
								value = "cfq";
								
							} else if (loopContainer[i].equals("set_immc_readahead")) {
								value = deviceSetup.support_device_mtd() ? "4" : "128";
								
							} else if (loopContainer[i].equals("set_immc_scheduler")) {
								value = deviceSetup.support_device_mtd() ? "deadline" : "cfq";
								
							} else {
								value = "0";
							}

							propFile.write(value);
						}

						propData.putString(loopContainer[i], value);
					}
					
					cached("DeviceProperties").putAll(propData);
					
					cached().putBoolean("DeviceProperties.Loaded", true);
					
					if ("1".equals(rootfw.file(dirProperty + "/m2sd.enable_reversed_mount").readOneLine())) {
						deviceProperties().move_apps( !"1".equals(propData.getString("move_apps")) );
						deviceProperties().move_dalvik( !"1".equals(propData.getString("move_dalvik")) );
						deviceProperties().move_data( !"1".equals(propData.getString("move_data")) );
						
						rootfw.file(dirProperty + "/m2sd.enable_reversed_mount").remove();
						
						saveDeviceProperties();
					}

					Root.close();
					
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
				RootFW rootfw = Root.open();
				String dirProperty = mContext.getResources().getString(R.string.config_dir_properties);
				String name;
				
				while ((name = deviceProperties.nextUpdated()) != null) {
					rootfw.file(dirProperty + "/m2sd." + name).write("" + cached("DeviceProperties").getString(name));
				}
				
				Root.close();
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
		
		if (!oCacheChecked && Root.isConnected()) {
			String tmpDir = mContext.getResources().getString(R.string.config_dir_tmp);
			RootFW rootfw = Root.open();
			String appid = mContext.getResources().getString(R.string.config_application_id);
			
			if (!rootfw.file(tmpDir + "/application.lock").exists() || 
					!appid.equals("" + preferences.getInt("android.appId", 0))) {
				
				Editor edit = preferences.edit();
				
				edit.clear();
				edit.putInt("android.appId", Integer.parseInt(appid));
				edit.commit();
				
				rootfw.filesystem("/").addMount(new String[]{"remount", "rw"});
				rootfw.file(tmpDir).createDirectory();
				rootfw.file(tmpDir + "/application.lock").write("1");
				rootfw.filesystem("/").addMount(new String[]{"remount", "ro"});
			}
			
			Root.close();
			
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
