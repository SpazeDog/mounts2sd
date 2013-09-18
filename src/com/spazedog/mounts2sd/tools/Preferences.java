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

import java.lang.ref.WeakReference;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Build;
import android.os.UserManager;
import android.util.Log;

import com.spazedog.lib.rootfw3.RootFW;
import com.spazedog.lib.rootfw3.RootFW.ConnectionListener;
import com.spazedog.lib.rootfw3.extenders.FileExtender;
import com.spazedog.lib.rootfw3.extenders.FileExtender.FileStat;
import com.spazedog.lib.rootfw3.extenders.FilesystemExtender.DiskStat;
import com.spazedog.lib.rootfw3.extenders.FilesystemExtender.MountStat;
import com.spazedog.lib.rootfw3.extenders.InstanceExtender.SharedRootFW;
import com.spazedog.lib.rootfw3.extenders.MemoryExtender.SwapStat;
import com.spazedog.lib.rootfw3.extenders.ShellExtender.ShellResult;
import com.spazedog.mounts2sd.R;
import com.spazedog.mounts2sd.tools.containers.IApplicationSession;
import com.spazedog.mounts2sd.tools.containers.IApplicationSettings;
import com.spazedog.mounts2sd.tools.containers.IDeviceConfig;
import com.spazedog.mounts2sd.tools.containers.IDeviceProperties;
import com.spazedog.mounts2sd.tools.containers.IDeviceSetup;
import com.spazedog.mounts2sd.tools.containers.IPersistence;

public final class Preferences {
	
	private final static Object oClassLock = new Object();
	private final Object mInstanceLock = new Object();
	
	private static Integer oTheme;
	
	private final static Map<String, Boolean> oClassChecks = new HashMap<String, Boolean>();
	private final Map<String, SharedPreferences> mSharedPreferences = new HashMap<String, SharedPreferences>();
	
	private static WeakReference<Context> oContextReference;
	private static WeakReference<Preferences> oClassReference;
	
	public final DeviceSetup deviceSetup = new DeviceSetup();
	public final DeviceConfig deviceConfig = new DeviceConfig();
	public final DeviceProperties deviceProperties = new DeviceProperties();
	public final ApplicationSettings applicationSettings = new ApplicationSettings();
	public final ApplicationSession applicationSession = new ApplicationSession();
	public final Persistence persistence = new Persistence();
	
	static {
		oClassChecks.put("loaded.device.setup", false);
		oClassChecks.put("loaded.device.config", false);
		oClassChecks.put("loaded.device.properties", false);
		oClassChecks.put("initiated.cache", false);
	}
	
	public static Preferences getInstance(Context context) {
		synchronized (oClassLock) {
			Preferences tmpPreferences = oClassReference != null ? 
					oClassReference.get() : null;
					
			Context tmpContext = oContextReference != null ? 
					oContextReference.get() : null;
			
			if (tmpContext == null)
				oContextReference = new WeakReference<Context>( (tmpContext = context.getApplicationContext()) );
			
			if (tmpPreferences == null)
				oClassReference = new WeakReference<Preferences>( (tmpPreferences = new Preferences()) );
			
			if (tmpPreferences.mSharedPreferences.size() == 0) {
				tmpPreferences.mSharedPreferences.put("cache", tmpContext.getSharedPreferences("cache", 0x00000000));
				tmpPreferences.mSharedPreferences.put("persistent", tmpContext.getSharedPreferences("persistent", 0x00000000));
			}
			
			if (!oClassChecks.get("initiated.cache")) {
				String appid = tmpContext.getResources().getString(R.string.config_application_id);
				SharedPreferences sharedPreferences = tmpPreferences.mSharedPreferences.get("cache");
				
				if (!appid.equals("" + sharedPreferences.getInt("android.appId", 0)) || !new java.io.File("/boot.chk").exists()) {
					RootFW root = Root.initiate();
					Editor edit = sharedPreferences.edit();
					
					edit.clear();
					edit.putInt("android.appId", Integer.parseInt(appid));
					edit.commit();
						
					if (root.isConnected()) {
						root.filesystem("/").addMount(new String[]{"remount", "rw"});
						root.file("/boot.chk").write("1");
						root.filesystem("/").addMount(new String[]{"remount", "ro"});
						
					} else {
						((SharedRootFW) root).addInstanceListener(new ConnectionListener(){

							@Override
							public void onConnectionEstablished(RootFW instance) {
								instance.filesystem("/").addMount(new String[]{"remount", "rw"});
								instance.file("/boot.chk").write("1");
								instance.filesystem("/").addMount(new String[]{"remount", "ro"});
								
								((SharedRootFW) instance).removeInstanceListener(this);
							}

							@Override
							public void onConnectionFailed(RootFW instance) {}

							@Override
							public void onConnectionClosed(RootFW instance) {}
						});
					}
					
					Root.release();
				}
				
				oClassChecks.put("initiated.cache", true);
			}
			
			return tmpPreferences;
		}
	}
	
	private Preferences() {}

	public Context getContext() {
		return oContextReference.get();
	}
	
	public Integer theme() {
		if (oTheme == null) {
			oTheme = applicationSettings.use_dark_theme() ? 
					(applicationSettings.use_global_settings_style() ? R.style.Theme_Dark_Settings : R.style.Theme_Dark) : 
						(applicationSettings.use_global_settings_style() ? R.style.Theme_Settings : R.style.Theme);
		}
		
		return oTheme;
	}
	
	@SuppressWarnings("deprecation")
	@TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
	public boolean isUserOwner() {
		if (Integer.valueOf(android.os.Build.VERSION.SDK) >= 17) {
		    try {
		        Method getUserHandle = UserManager.class.getMethod("getUserHandle");
		        int userHandle = (Integer) getUserHandle.invoke(getContext().getSystemService(Context.USER_SERVICE));
		        
		        return userHandle == 0;
		        
		    } catch (Exception e) {}
		}
		
		return true;
	}
	
	public final class Persistence extends IPersistence {
		private Persistence() {
			mPersistentStorage = new IPersistentPreferences("cache", "Persistence");
		}
	}
	
	public final class ApplicationSession extends IApplicationSession {
		@Override
		protected Context getContext() {
			return oContextReference.get();
		}
	}
	
	public final class ApplicationSettings extends IApplicationSettings {
		private ApplicationSettings() {
			mPersistentStorage = new IPersistentPreferences("persistent", "AppSettings");
		}
	}
	
	public final class DeviceSetup extends IDeviceSetup {
		private DeviceSetup() {
			mPersistentStorage = new IPersistentPreferences("cache", "DeviceSetup");
		}
		
		public Boolean isLoaded() {
			synchronized (mInstanceLock) {
				if (!oClassChecks.get("loaded.device.setup")) {
					if (!mPersistentStorage.getBoolean("isLoaded")) {
						return false;
					}
					
					oClassChecks.put("loaded.device.setup", true);
				}
				
				return true;
			}
		}
		
		public Boolean load(Boolean force) {
			synchronized (mInstanceLock) {
				RootFW root = Root.initiate();
				
				if (root.isConnected() && (force || !isLoaded())) {
					mPersistentStorage.edit().lockEditor();
					
					String[] loopContainer;

					/* ================================================================
					 * Handle the Application Busybox binary
					 */
					String configPathBusybox = getContext().getResources().getString(R.string.config_path_busybox);
					
					environment_busybox_internal( root.file(configPathBusybox).exists() );
					
					if (applicationSettings.use_builtin_busybox() && !environment_busybox_internal()) {
						FileExtender.File busyboxFile = root.file(configPathBusybox);
						busyboxFile.extractFromResource(getContext(), "busybox", "0777", "0", "0");
						
						environment_busybox_internal(busyboxFile.exists());
					}
					
					for (int i=0; i < (loopContainer = new String[]{"/system/xbin/busybox", "/system/bin/busybox", "/system/sbin/busybox", "/sbin/busybox"}).length; i++) {
						if (root.file(loopContainer[i]).exists()) {
							environment_multiple_binaries(true); break;
						}
					}

					if (root.busybox().exists()) {
						environment_busybox(true);
						
						/* ================================================================
						 * Check the device for S-On protection
						 */
						FileExtender.File sOnFile = root.file("/system/S_On.test");
						
						root.filesystem("/system").addMount(new String[]{"remount", "rw"});
						
						environment_secure_flag_off(
							sOnFile.write("1") 
								&& "1".equals(sOnFile.readOneLine())
						);
						sOnFile.remove();
						
						root.filesystem("/system").addMount(new String[]{"remount", "ro"});
						/*
						 * ----------------------------------------------------------------
						   ================================================================
						 * Check the startup scripts
						 */
						String configPathScript = getContext().getResources().getString(R.string.config_path_script);
						String configPathRunner = getContext().getResources().getString(R.string.config_path_runner);
						
						FileExtender.File scriptFile = root.file( configPathScript );
						FileExtender.File runnerFile = root.file( configPathRunner );
						
						if (scriptFile.exists() && runnerFile.exists()) {
							environment_startup_script(true);
							
							String scriptId = scriptFile.readOneMatch("@id");
							String scriptVersion = scriptFile.readOneMatch("@version");
							
							if (scriptId != null)
								id_startup_script(Integer.parseInt( ("" + scriptId.substring(scriptId.lastIndexOf(" "))).trim() ));
								
							if (scriptVersion != null)
								version_startup_script( scriptVersion.substring(scriptVersion.lastIndexOf(" ")) );
						}
						/*
						 * ----------------------------------------------------------------
						   ================================================================
						 * Locate all of the partitions
						 */
						for (int i=0; i < 10; i++) {
							FileExtender.File mmcTypeFile = root.file("/sys/block/mmcblk" + i + "/device/type");
							
							if (mmcTypeFile.exists()) {
								String mmcType = mmcTypeFile.readOneLine();
								
								if ("MMC".equals(mmcType)) {
									path_device_map_immc("/dev/block/mmcblk" + i);
									
								} else if ("SD".equals(mmcType)) {
									path_device_map_emmc("/dev/block/mmcblk" + i);
									
									if (root.file("/dev/block/mmcblk" + i + "p2").exists()) {
										path_device_map_sdext("/dev/block/mmcblk" + i + "p2");
										type_device_sdext(root.filesystem("/dev/block/mmcblk" + i + "p2").fsType(true));
									}
									
									if (root.file("/dev/block/mmcblk" + i + "p3").exists())
										path_device_map_swap("/dev/block/mmcblk" + i + "p3");
								}
								
							} else {
								if (path_device_map_immc() == null) {
									if (root.file("/dev/block/mtdblock0").exists()) {
										path_device_map_immc("/dev/block/mtdblock0");
										
									} else if (root.file("/dev/block/mtdblock0").exists()) {
										path_device_map_immc("/dev/block/bml0!c");
									}
								}
								
								break;
							}
						}
						
						for (int i=0; i < (loopContainer = new String[]{"/data", "/cache"}).length; i++) {
							MountStat mountStat = root.filesystem(loopContainer[i]).statMount();
							
							if (mountStat == null
									|| mountStat.device() == null
									|| mountStat.device().equals(path_device_map_sdext())
									|| (i > 0 && mountStat.device().equals(path_device_map_data()))) {
								
								mountStat = root.filesystem(loopContainer[i]).statFstab();
							}
							
							if (i == 0 && mountStat != null) {
								path_device_map_data(mountStat.device());
							
							} else if (mountStat != null) {
								path_device_map_cache(mountStat.device());
							}
						}
						
						for (int i=0; i < (loopContainer = new String[]{"/dev/block/zram0", "/system/lib/modules/zram.ko", "/system/lib/modules/ramzswap.ko", "/dev/block/ramzswap0"}).length; i++) {
							if (root.file(loopContainer[i]).exists()) {
								path_device_map_zram(
									i < 2 ? "/dev/block/zram0"
										: (root.binary("rzscontrol").exists() ? "/dev/block/ramzswap0" : null)
								);
								
								break;
							}
						}
						/*
						 * ----------------------------------------------------------------
						   ================================================================
						 * Locate scheduler and readahead files for the EMMC and IMMC
						 */
						for (int i=0; i < (loopContainer = new String[]{path_device_map_immc(), path_device_map_emmc()}).length; i++) {
							if (loopContainer[i] != null) {
								FileStat fileStat = root.file(loopContainer[i]).getDetails();
								
								if (fileStat != null) {
									FileExtender.File readaheadFile = root.file("/sys/devices/virtual/bdi/" + fileStat.mm() + "/read_ahead_kb");
									FileExtender.File schedulerFile = root.file("/sys/block/" + loopContainer[i].substring(loopContainer[i].lastIndexOf("/") + 1) + "/queue/scheduler");

									if (i == 0) {
										path_device_readahead_immc(readaheadFile.exists() ? readaheadFile.getResolvedPath() : null);
										path_device_scheduler_immc(schedulerFile.exists() ? schedulerFile.getResolvedPath() : null);
										
									} else {
										path_device_readahead_emmc(readaheadFile.exists() ? readaheadFile.getResolvedPath() : null);
										path_device_scheduler_emmc(schedulerFile.exists() ? schedulerFile.getResolvedPath() : null);
									}
								}
							}
						}
						/*
						 * ----------------------------------------------------------------
						   ================================================================
						 * Collect system folders on /data
						 */
						if ((loopContainer = root.file("/system").getList()) != null) {
							List<String> systemDirs = new ArrayList<String>();
							
							for (int i=0; i < loopContainer.length; i++) {
								if (!loopContainer[i].equals(".") 
										&& !loopContainer[i].equals("..") 
										&& root.file("/data/" + loopContainer[i] + "_s").isDirectory()) {
									
									systemDirs.add(loopContainer[i] + "_s");
								}
							}
							
							if (systemDirs.size() > 0) {
								paths_directory_system(systemDirs.toArray(new String[systemDirs.size()]));
								support_directory_system(true);
							}
						}
						/*
						 * ----------------------------------------------------------------
						   ================================================================
						 * Handle memory devices
						 */
						if (root.file("/proc/swaps").exists()) {
							support_option_swap(path_device_map_swap() != null);
							support_option_zram(path_device_map_zram() != null);
						}
						/*
						 * ---------------------------------------------------------------- */
						String configFileTmp = getContext().getResources().getString(R.string.config_dir_tmp);

						support_binary_tune2fs(root.binary("tune2fs").exists());
						support_binary_sqlite3(root.binary("sqlite3").exists());
						support_binary_e2fsck(root.binary("e2fsck").exists());
						support_directory_asec(root.file("/data/app-asec").isDirectory());
						support_directory_library(root.file("/data/app-lib").isDirectory());
						support_directory_user(root.file("/data/user").isDirectory());
						support_directory_media(root.file("/data/media").isDirectory());
						support_directory_cmdalvik(root.file("/cache/dalvik-cache").isDirectory() && !"1".equals(root.property().get("dalvik.vm.dexopt-data-only")));
						support_device_mtd(root.file("/proc/mtd").exists());
						safemode("1".equals(root.file(configFileTmp + "/safemode.result").readOneLine()));
						init_implementation("service".equals(root.file(configFileTmp + "/init.type").readOneLine()) ? "service" : "internal");
						
						mPersistentStorage.edit().putBoolean("isLoaded", true);
						oClassChecks.put("loaded.device.setup", true);
					}
					
					mPersistentStorage.edit().unlockEditor();
				}
				
				Root.release();

				return isLoaded();
			}
		}
	}
	
	public final class DeviceConfig extends IDeviceConfig {
		private DeviceConfig() {
			mPersistentStorage = new IPersistentPreferences("cache", "DeviceConfig");
		}
		
		public Boolean isLoaded() {
			synchronized (mInstanceLock) {
				if (!oClassChecks.get("loaded.device.config")) {
					if (!mPersistentStorage.getBoolean("isLoaded")) {
						return false;
					}
					
					oClassChecks.put("loaded.device.config", true);
				}
				
				return true;
			}
		}
		
		public Boolean load(Boolean force) {
			synchronized (mInstanceLock) {
				RootFW root = Root.initiate();
				
				if (root.isConnected() && (force || !isLoaded()) && deviceSetup.isLoaded()) {
					String[] loopContainer;
					
					mPersistentStorage.edit().lockEditor();
					
					/* ================================================================
					 * Locate device mount points
					 */
					String configDirSdext = getContext().getResources().getString(R.string.config_dir_sdext);
					DiskStat diskStat = null;
					String additLocationDevice = null;
					
					for (int i=0; i <= (loopContainer = new String[]{"/data", configDirSdext, null}).length; i++) {
						if (loopContainer[i] != null) {
							diskStat = root.filesystem(loopContainer[i]).statDisk();
						}
					
						if (diskStat != null && diskStat.device().equals(deviceSetup.path_device_map_data())) {
							location_storage_data(loopContainer[i]);
							size_storage_data(diskStat.size());
							usage_storage_data(diskStat.usage());
							
						} else if (diskStat != null && diskStat.device().equals(deviceSetup.path_device_map_sdext())) {
							location_storage_sdext(loopContainer[i]);
							size_storage_sdext(diskStat.size());
							usage_storage_sdext(diskStat.usage());
							
						} else if (additLocationDevice != null) {
							if (root.filesystem(additLocationDevice).isMounted()) {
								diskStat = root.filesystem(additLocationDevice).statDisk();
								
								if (diskStat != null) {
									loopContainer[i] = diskStat.location();

									continue;
								}
							}
						}
						
						if (additLocationDevice == null) {
							additLocationDevice = diskStat == null || diskStat.device().equals(deviceSetup.path_device_map_data()) ? deviceSetup.path_device_map_sdext() : deviceSetup.path_device_map_data(); continue;
						}
							
						break;
					}

					if ((diskStat = root.filesystem("/cache").statDisk()) != null) {
						location_storage_cache(
							diskStat.device().equals(deviceSetup.path_device_map_sdext()) ? location_storage_sdext() + "/cache" : 
								diskStat.device().equals(deviceSetup.path_device_map_data()) ? location_storage_data() + "/cache" : "/cache"
						);
						
						size_storage_cache(diskStat.size());
						usage_storage_cache(diskStat.usage());
					}
					/*
					 * ----------------------------------------------------------------
					   ================================================================
					 * Get folder status and usage
					 */
					List<String[]> mountLooper = new ArrayList<String[]>();
					
					mountLooper.add(deviceSetup.support_directory_asec() ? new String[]{"app", "app-private", "app-asec"} : new String[]{"app", "app-private"});
					mountLooper.add(new String[]{"app-system"});
					mountLooper.add(new String[]{"dalvik-cache"});
					mountLooper.add(deviceSetup.support_directory_user() ? new String[]{"data", "user"} : new String[]{"data"});
					
					if (deviceSetup.support_directory_library()) {
						mountLooper.add(new String[]{"app-lib"});
					}
					
					if (deviceSetup.support_directory_media()) {
						mountLooper.add(new String[]{"media"});
					}
					
					if (deviceSetup.support_directory_system()) {
						mountLooper.add(deviceSetup.paths_directory_system());
					}
					
					for (int i=0; i < mountLooper.size(); i++) {
						Boolean sdextMounted = deviceSetup.path_device_map_sdext() != null 
								&& location_storage_sdext() != null;
						
						String[] curFolders = mountLooper.get(i);
						Integer curState = 0;
						Long curUsage = 0L;
						
						for (int x=0; x < curFolders.length; x++) {
							if (sdextMounted && curState >= 0) {
								DiskStat curStat = root.filesystem("/data/" + curFolders[x]).statDisk();
								
								curState = curStat == null || curStat.device().equals(deviceSetup.path_device_map_data()) ? 
										(x == 0 || curState == 0 ? 0 : -1) : 
											(x == 0 || curState == 1 ? 1 : -1);
							}
							
							curUsage += root.file("/data/" + curFolders[x]).fileSize();
						}
						
						switch (i) {
							case 0: 
								status_content_apps(curState);
								usage_content_apps(curUsage); break;
								
							case 1: 
								status_content_sysapps(curState);
								usage_content_sysapps(curUsage); break;
								
							case 2: 
								status_content_dalvik(curState);
								usage_content_dalvik(curUsage); break;
								
							case 3: 
								status_content_data(curState);
								usage_content_data(curUsage); break;
								
							case 4: 
								status_content_libs(curState);
								usage_content_libs(curUsage); break;
								
							case 5: 
								status_content_media(curState);
								usage_content_media(curUsage); break;
								
							case 6: 
								status_content_system(curState);
								usage_content_system(curUsage);
						}
					}
					/*
					 * ----------------------------------------------------------------
					   ================================================================
					 * Get SD-EXT information
					 */
					if (deviceSetup.path_device_map_sdext() != null) {
						String configDirTmp = getContext().getResources().getString(R.string.config_dir_tmp);
						
						if (deviceSetup.support_binary_e2fsck()) {
							String result = root.file(configDirTmp + "/e2fsck.result").readOneLine();
							
							if (result != null) {
								level_filesystem_fschk(Integer.parseInt(result));
							}
						}
						
						if (deviceSetup.support_binary_tune2fs() 
								&& "ext4".equals(deviceSetup.type_device_sdext())) {
							
							ShellResult result = root.shell().run("tune2fs -l '" + deviceSetup.path_device_map_sdext() + "'");
							
							status_filesystem_journal(
								result.wasSuccessful() ? 
									( ("" + result.getString()).contains("has_journal") ? 1 : 0 ) : -1
							);
						}
					}

					if (location_storage_sdext() != null) {
						MountStat mountStat = root.filesystem(deviceSetup.path_device_map_sdext()).statMount();
						
						if (mountStat != null) {
							type_filesystem_driver(mountStat.fstype());
						}
					}
					/*
					 * ----------------------------------------------------------------
					   ================================================================
					 * Get MMC scheduler and readahead information
					 */
					for (int i=0; i < (loopContainer = new String[]{deviceSetup.path_device_scheduler_immc(), deviceSetup.path_device_scheduler_emmc(), deviceSetup.path_device_readahead_immc(), deviceSetup.path_device_readahead_emmc()}).length; i++) {
						String line = loopContainer[i] == null ? 
								null : root.file(loopContainer[i]).readOneLine();
						
						if (line != null) {
							switch(i) {
								case 0: value_immc_scheduler(line.substring(line.indexOf("[")+1, line.lastIndexOf("]"))); break;
								case 1: value_emmc_scheduler(line.substring(line.indexOf("[")+1, line.lastIndexOf("]"))); break;
								case 2: value_immc_readahead(line); break;
								case 3: value_emmc_readahead(line);
							}
						}
					}
					/*
					 * ----------------------------------------------------------------
					   ================================================================
					 * Get memory information
					 */
					if (deviceSetup.support_option_swap()) {
						SwapStat[] swapList = root.memory().listSwaps();
						String swappiness = root.file("/proc/sys/vm/swappiness").readOneLine();
						
						if (swapList != null) {
							for (int i=0; i < swapList.length; i++) {
								if (swapList[i].device().equals(deviceSetup.path_device_map_swap())) {
									size_memory_swap(swapList[i].size());
									usage_memory_swap(swapList[i].usage());
									
								} else if (deviceSetup.support_option_zram() 
										&& swapList[i].device().equals(deviceSetup.path_device_map_zram())) {
									
									size_memory_zram(swapList[i].size());
									usage_memory_zram(swapList[i].usage());
								}
							}
						}

						if (swappiness != null) {
							level_memory_swappiness(Integer.parseInt(swappiness));
						}
					}
					/*
					 * ----------------------------------------------------------------
					   ================================================================
					 * Get storage threshold
					 */
					if (deviceSetup.support_binary_sqlite3()) {
						ShellResult result = root.shell().run("sqlite3 /data/data/com.android.providers.settings/databases/settings.db \"select value from secure where name = 'sys_storage_threshold_percentage'\"");
						
						if (result.wasSuccessful()) {
							try {
								size_storage_threshold(((Double) (((Long) size_storage_data()).doubleValue() * (Double.parseDouble( result.getLine() ) / 100))).longValue());
								
							} catch(Throwable e) {}
						}
					}
					/*
					 * ---------------------------------------------------------------- */
					
					oClassChecks.put("loaded.device.config", true);
					mPersistentStorage.edit().putBoolean("isLoaded", true);
					mPersistentStorage.edit().unlockEditor();
				}
				
				Root.release();

				return isLoaded();
			}
		}
	}
	
	public final class DeviceProperties extends IDeviceProperties {
		private DeviceProperties() {
			mPersistentStorage = new IPersistentPreferences("cache", "DeviceProperties");
		}
		
		public Boolean isLoaded() {
			synchronized (mInstanceLock) {
				if (!oClassChecks.get("loaded.device.properties")) {
					if (!mPersistentStorage.getBoolean("isLoaded")) {
						return false;
					}
					
					oClassChecks.put("loaded.device.properties", true);
				}
				
				return true;
			}
		}
		
		public Boolean load(Boolean force) {
			synchronized (mInstanceLock) {
				RootFW root = Root.initiate();
				
				if (root.isConnected() && (force || !isLoaded()) && deviceSetup.isLoaded()) {
					mPersistentStorage.edit().lockEditor();
					
					/* ================================================================
					 * Collect and/or create script properties
					 */
					String dirProperty = getContext().getResources().getString(R.string.config_dir_properties);
					String[] loopContainer = new String[]{"move_apps", "move_sysapps", "move_dalvik", "move_data", "move_libs", "move_media", "move_system", "enable_cache", "enable_swap", "enable_sdext_journal", "enable_debug", "set_swap_level", "set_sdext_fstype", "run_sdext_fschk", "set_storage_threshold", "set_zram_compression", "set_emmc_readahead", "set_emmc_scheduler", "set_immc_readahead", "set_immc_scheduler", "disable_safemode"};
					
					for (int i=0; i < loopContainer.length; i++) {
						FileExtender.File propFile = root.file(dirProperty + "/m2sd." + loopContainer[i]);
						String value = propFile.readOneLine();
						
						if (value == null) {
							if (loopContainer[i].equals("move_apps") || loopContainer[i].equals("disable_safemode")) {
								value = "1";
								
							} else if (loopContainer[i].equals("enable_swap")) {
								value = (deviceSetup.path_device_map_swap() != null && deviceSetup.support_option_swap()) ? "1" : "0";
								
							} else if (loopContainer[i].equals("enable_sdext_journal")) {
								value = deviceSetup.support_binary_tune2fs() ? "2" : "1";
								
							} else if (loopContainer[i].equals("set_sdext_fstype")) {
								value = root.filesystem().hasTypeSupport("ext4") ? "ext4" : "auto";
								
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
						
						mPersistentStorage.edit().putString(loopContainer[i], value);
					}
					/*
					 * ---------------------------------------------------------------- */
					
					oClassChecks.put("loaded.device.properties", true);
					mPersistentStorage.edit().putBoolean("isLoaded", true);
					mPersistentStorage.edit().unlockEditor();
					
					/* ================================================================
					 * Handle old reversed mount if enabled
					 */
					if ("1".equals(root.file(dirProperty + "/m2sd.enable_reversed_mount").readOneLine())) {
						move_apps( !move_apps() );
						move_dalvik( !move_dalvik() );
						move_data( !move_data() );
						
						root.file(dirProperty + "/m2sd.enable_reversed_mount").remove();
					}
					/*
					 * ---------------------------------------------------------------- */
				}
				
				Root.release();
				
				return isLoaded();
			}
		}
	}
	
	public final class IPersistentPreferences {
		private String mStorageName;
		private String mName;
		
		private IPersistentEditor mEditor;
		
		private IPersistentPreferences(String storage, String name) {
			mStorageName = storage;
			mName = name;
		}
		
		public IPersistentEditor edit() {
			if (mEditor == null) {
				mEditor = new IPersistentEditor();
			}
			
			return mEditor;
		}

		public final class IPersistentEditor {
			private Editor mInnerEditor;
			
			private Boolean mLockEditor = false;
			
			private IPersistentEditor() {
				mInnerEditor = mSharedPreferences.get(mStorageName).edit();
			}
			
			public void commit() {
				if (!mLockEditor) {
					apply();
					
					mInnerEditor = null;
					mEditor = null;
				}
			}
			
			@SuppressWarnings("deprecation")
			@TargetApi(Build.VERSION_CODES.GINGERBREAD)
			public void apply() {
				if (Integer.valueOf(android.os.Build.VERSION.SDK) >= 9) {
					mInnerEditor.apply();
					
				} else {
					mInnerEditor.commit();
				}
			}
			
			public void lockEditor() {
				mLockEditor = true;
			}
			
			public void unlockEditor() {
				mLockEditor = false;
				
				commit();
			}
			
			public Boolean isLocked() {
				return mLockEditor;
			}
			
			public IPersistentEditor putLong(String name, Long value) {
				mInnerEditor.putLong(mName + ":" + name, value); return this;
			}
			
			public IPersistentEditor putInteger(String name, Integer value) {
				mInnerEditor.putInt(mName + ":" + name, value); return this;
			}
			
			public IPersistentEditor putBoolean(String name, Boolean value) {
				mInnerEditor.putBoolean(mName + ":" + name, value); return this;
			}
			
			public IPersistentEditor putString(String name, String value) {
				mInnerEditor.putString(mName + ":" + name, value); return this;
			}
			
			public IPersistentEditor putStringArray(String name, String[] value) {
				String newValue = "";
				
				for (int i=0; i < value.length; i++) {
					newValue += (i > 0 ? "," : "") + newValue.replaceAll(",", "*comma*");
				}
				
				return putString(name, newValue);
			}
		}
		
		public Long getLong(String name) {
			return getLong(name, 0L);
		}
		
		public Long getLong(String name, Long defaultValue) {
			if (mEditor != null) {
				mEditor.apply();
			}
			
			return mSharedPreferences.get(mStorageName).getLong(mName + ":" + name, defaultValue);
		}

		public Integer getInteger(String name) {
			return getInteger(name, 0);
		}
		
		public Integer getInteger(String name, Integer defaultValue) {
			if (mEditor != null) {
				mEditor.apply();
			}
			
			return mSharedPreferences.get(mStorageName).getInt(mName + ":" + name, defaultValue);
		}

		public Boolean getBoolean(String name) {
			return getBoolean(name, false);
		}
		
		public Boolean getBoolean(String name, Boolean defaultValue) {
			if (mEditor != null) {
				mEditor.apply();
			}
			
			return mSharedPreferences.get(mStorageName).getBoolean(mName + ":" + name, defaultValue);
		}

		public String getString(String name) {
			return getString(name, null);
		}
		
		public String getString(String name, String defaultValue) {
			if (mEditor != null) {
				mEditor.apply();
			}
			
			return mSharedPreferences.get(mStorageName).getString(mName + ":" + name, defaultValue);
		}
		
		public String[] getStringArray(String name) {
			return getStringArray(name, null);
		}
		
		public String[] getStringArray(String name, String[] defaultValue) {
			if (mEditor != null) {
				mEditor.apply();
			}
			
			if (!mSharedPreferences.get(mStorageName).contains(mName + ":" + name)) {
				return defaultValue;
			}
			
			String[] value = mSharedPreferences.get(mStorageName).getString(mName + ":" + name, "").split(",");
			
			for (int i=0; i < value.length; i++) {
				value[i] = value[i].replaceAll("*comma*", ",");
			}
			
			return value;
		}
	}
}
