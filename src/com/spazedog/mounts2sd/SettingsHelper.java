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

package com.spazedog.mounts2sd;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;

import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.util.Base64;

import com.spazedog.mounts2sd.UtilsHelper.ElementContainer;
import com.spazedog.rootfw.RootFW;
import com.spazedog.rootfw.containers.FileData;
import com.spazedog.rootfw.containers.ShellResult;

public class SettingsHelper {
	protected static Config VALUES[] = {
		new Config("script.status", "script", "load", null, null, null, 0, true, true, null, "1"),
		new Config("content.apps.status", "move_apps", "load", null, "storage.rmount.status", "script.status", 2, true, true, null, "1"),
		new Config("content.dalvik.status", "move_dalvik", "load", null, "storage.rmount.status", "script.status", 2, true, true, null, "0"),
		new Config("content.data.status", "move_data", "load", null, "storage.rmount.status", "script.status", 2, true, true, null, "0"),
		new Config("storage.cache.status", "enable_cache", "load", "switch", null, "script.status", 0, true, true, null, "2"),
		new Config("storage.rmount.status", "enable_reversed_mount", "load", null, null, "script.status", 2, true, true, null, "0"),
		new Config("memory.swap.status", "enable_swap", "load", null, null, "script.status", 3, true, true, null, "1"),
		new Config("memory.swap.level", "set_swap_level", "configure", "swappiness", null, "memory.swap.status", 3, true, true, null, "0"),
		new Config("partition.fschk.status", "run_sdext_fschk", "execute", null, null, "script.status", 2, true, true, null, "1"),
		new Config("partition.fstype.value", "set_sdext_fstype", "configure", "filesystem", null, "script.status", 2, true, true, null, "ext4"),
		new Config("device.readahead.value", "set_sdcard_readahead", "configure", "readahead", null, "script.status", 1, true, true, null, "512"),
		new Config("partition.journal.status", "enable_sdext_journal", "enable", "switch", null, "script.status", 2, true, true, null, "0"),
		new Config("misc.safemode.status", "disable_safemode", "setting", null, null, "script.status", 1, true, false, null, "0"),
		new Config("misc.cache.tmpfs.status", "no_tmpfs_cache", "setting", null, null, "storage.cache.status", 0, true, false, null, "0"),
		new Config("misc.cache.folders.status", "disable_cache_folders", "setting", null, null, "script.status", 0, true, false, null, "0"),
		new Config("misc.debug.status", "enable_debug", "setting", null, null, "script.status", 0, true, false, null, "0"),
		new Config("path.internal.storage", "destination.internal", "path", null, null, null, 0, false, true, "/data", null),
		new Config("path.external.storage", "destination.external", "path", null, null, null, 2, false, true, null, null),
		new Config("path.cache.storage", "destination.cache", "path", null, null, null, 0, false, true, "/cache", null)
	};
	
	protected static Map<String, Integer> KEYS = new HashMap<String, Integer>();
	
	protected static Boolean COMMIT = false;
	protected static Boolean LOADED = false;
	protected static Boolean RUNNING = false;

    protected static Integer getKey(String pName) {
        if (KEYS.size() == 0) {
            for (int i=0; i < VALUES.length; i++) {
                KEYS.put(VALUES[i].getString("name"), i);
                KEYS.put(VALUES[i].getString("prop"), i);
            }
        }

        return KEYS.get(pName);
    }
    
	public static String[] getPropsCollection() {
		String elements[] = new String[VALUES.length];
		
		for (int i=0; i < VALUES.length; i++) {
			elements[i] = VALUES[i].getString("name");
		}
		
		return elements;
	}
	
	public static String propName(String pPropName) {
		if (getKey(pPropName) != null) {
			return VALUES[ getKey(pPropName) ].getString("name");
		}
		
		return null;
	}
	
	public static String propFileName(String pName) {
		if (getKey(pName) != null) {
			return VALUES[ getKey(pName) ].getString("prop");
		}
		
		return null;
	}
	
	public static String propType(String pName) {
		if (getKey(pName) != null) {
			return VALUES[ getKey(pName) ].getString("type");
		}
		
		return null;
	}
	
	public static Boolean propHasState(String pName) {
		if (getKey(pName) != null) {
			return VALUES[ getKey(pName) ].getBool("hasState");
		}
		
		return false;
	}
	
	public static Boolean propHasConfig(String pName) {
		if (getKey(pName) != null) {
			return VALUES[ getKey(pName) ].getBool("hasConfig");
		}
		
		return false;
	}
	
	public static Boolean propIsActive(String pName) {
		if (getKey(pName) != null) {
			return VALUES[ getKey(pName) ].getBool("active");
		}
		
		return false;
	}
	
	public static String propSelector(String pName) {
		if (getKey(pName) != null) {
			return VALUES[ getKey(pName) ].getString("selector");
		}
		
		return null;
	}
	
	public static String propReverseOn(String pName) {
		if (getKey(pName) != null) {
			return VALUES[ getKey(pName) ].getString("reverseOn");
		}
		
		return null;
	}
	
	public static String propDependedOn(String pName) {
		if (getKey(pName) != null) {
			return VALUES[ getKey(pName) ].getString("dependedOn");
		}
		
		return null;
	}
	
	public static String propDefaultConfig(String pName) {
		if (getKey(pName) != null) {
			return VALUES[ getKey(pName) ].getString("defConfig");
		}
		
		return null;
	}

	public static String propDefaultState(String pName) {
		if (getKey(pName) != null) {
			return VALUES[ getKey(pName) ].getString("defState");
		}
		
		return null;
	}
	
	public static void setPropConfig(String pName, String pValue) {
		SharedPreferences settings = BaseApplication.getContext().getSharedPreferences("prop_configuration", 0x00000000);
		Editor editor = settings.edit();
		editor.putString("prop.config." + pName, pValue);
		editor.commit();
		
		VALUES[ getKey(pName) ].putBool("update", true);
		
		if (VALUES[ getKey(pName) ].getBool("hasConfig")) {
			COMMIT = true;
		}
	}
	
	public static String getPropMessage(String pName) {
		SharedPreferences settings = BaseApplication.getContext().getSharedPreferences("prop_configuration", 0x00000000);
		
		return settings.getString("prop.message." + pName, null);
	}
	
	public static String getPropConfig(String pName) {
		SharedPreferences settings = BaseApplication.getContext().getSharedPreferences("prop_configuration", 0x00000000);
		
		return settings.getString("prop.config." + pName, VALUES[ getKey(pName) ].getString("defConfig"));
	}
	
	public static String getPropState(String pName) {
		SharedPreferences settings = BaseApplication.getContext().getSharedPreferences("prop_configuration", 0x00000000);
		
		return settings.getString("prop.state." + pName, VALUES[ getKey(pName) ].getString("defState"));
	}
	
	public static String getPropAttention(String pName) {
		SharedPreferences settings = BaseApplication.getContext().getSharedPreferences("prop_configuration", 0x00000000);
		
		return settings.getString("prop.attention." + pName, null);
	}
	
	public static Boolean commitPropConfigs() {
		if (COMMIT) {
			RootFW rootfw = RootFW.getInstance(BaseApplication.getContext().getPackageName());
			
			if (rootfw.isConnected()) {
				SharedPreferences settings = BaseApplication.getContext().getSharedPreferences("prop_configuration", 0x00000000);
				
				for (int i=0; i < VALUES.length; i++) {
					if (VALUES[i].getBool("hasConfig")) {
						if (VALUES[i].getBool("update")) {
							rootfw.filesystem.putFileLine("/data/property/m2sd." + VALUES[i].getString("prop"), settings.getString("prop.config." + VALUES[i].getString("name"), ""));
							
							VALUES[i].putBool("update", false);
						}
					}
				}
				
				COMMIT = false;
			}
			
			rootfw.close();
			
		} else {
			return true;
		}
		
		return false;
	}
	
	public static String getLog() {
		SharedPreferences settings = BaseApplication.getContext().getSharedPreferences("prop_configuration", 0x00000000);
		
		String content = settings.getString("log", null);
		
		if (content != null) {
			try {
				return new String(Base64.decode(content, Base64.URL_SAFE), "UTF-8");
				
			} catch (UnsupportedEncodingException e) {
				return null;
			}
		}
		
		return null;
	}
	
	public static Boolean loadConfigs() {
		RUNNING = true;
		
		String appid = BaseApplication.getContext().getResources().getString(R.string.config_app_id);
		String scriptpath = BaseApplication.getContext().getResources().getString(R.string.config_script_path);
		
		RootFW rootfw = null;
		SharedPreferences settings = BaseApplication.getContext().getSharedPreferences("prop_configuration", 0x00000000);
		Editor editor = null;
		Boolean status = false;
		ShellResult result;
		String tmp;
		
		if ((!LOADED && !(LOADED = new File("/props/app.config.loaded").isFile())) || !appid.equals( settings.getString("app.id", null) )) {
			rootfw = RootFW.getInstance( BaseApplication.getContext().getPackageName() );
			editor = settings.edit();
			
			editor.clear();
			
			if (!rootfw.isConnected()) {
				editor.putBoolean("check.superuser", false);
				
			} else if(!rootfw.busybox.exist()) {
				editor.putBoolean("check.busybox", false);
				
			} else {
				result = rootfw.runShell("( busybox [ 1 -eq 0 ] || busybox [ 0 -eq 1 ] ) && busybox echo no || busybox echo '$(sed -n '1p' /dev/null)remove this part okay:bla=no-4' | busybox grep -e '.*bla=no-[1-9]*' | busybox sed -e 's/^remove //' | busybox awk '{print $3}' | busybox cut -d ':' -f1 | md5sum | busybox awk '{print $1}'");

				if (result.getResultCode() == 0 && result.getResult().getAssembled().contains("46a313b06d28557b0ed1e03fb3d92a40")) {
					editor.putBoolean("check.busybox.compatibility", true);
					
					result = rootfw.runShell("/system/bin/busybox.sh check");
					
					if (result.getResultCode() == 0) {
						editor.putBoolean("check.busybox.configured", true);
					}
					
					if (checkScript()) {
						editor.putBoolean("check.script", true);
						
						rootfw.filesystem.remount("/", "rw");
						
						if (!rootfw.filesystem.isFile("/props/app.finalized.script")) {
							// Execute whatever needs to be done after boot
							rootfw.runShell(scriptpath + " finalize");
							rootfw.filesystem.putFileLine("/props/app.finalized.script", "1");
						}
						
						Integer sdLevel = 0;
						for (int i=0; i < 10; i++) {
							if ((tmp = rootfw.filesystem.readFileLine("/sys/block/mmcblk" + i + "/device/type")) != null && "SD".equals(tmp)) {
								for (int x=1; x < 4; x++) {
									if (rootfw.filesystem.exist("/dev/block/mmcblk" + i + "p" + x)) {
										sdLevel = x;
										
									} else {
										break;
									}
								}
								
								break;
							}
						}
						
						editor.putInt("check.sdcard.partitions", sdLevel);

						Integer level, lastLevel=0;
						String[] logParts, log;
						FileData filedata = rootfw.filesystem.readFile("/props/log");
						HashMap<String, Integer> levels = new HashMap<String, Integer>();
						HashMap<String, String> messages = new HashMap<String, String>();
						
						if (filedata != null && filedata.getLength() > 0) {
							editor.putString("log", Base64.encodeToString(filedata.getAssembled().getBytes(), Base64.URL_SAFE));
							
							log = filedata.getData();
							for (int x=0; x < log.length; x++) {
								logParts = UtilsHelper.splitScriptMessage(log[x], false);
								
								if (!"v".equals(logParts[0]) && !"d".equals(logParts[0])) {
									level = "e".equals(logParts[0]) ? 2 : "w".equals(logParts[0]) ? 1 : 0;
									
									if (level >= lastLevel) {
										if (propName(logParts[1]) != null) {
											messages.put(propName(logParts[1]), log[x]);
											levels.put(propName(logParts[1]), level);
										}
										
										lastLevel = level; 
									}
								}
							}
						}
						
						String propConfig, propState;
						Integer propAttention, lastAttention=0;
						
						for (int i=0; i < VALUES.length; i++) {
							if (VALUES[i].getString("prop") != null) {
								propConfig = VALUES[i].getBool("hasConfig") ? rootfw.filesystem.readFileLine("/props/config." + VALUES[i].getString("prop")) : null;
								propState = VALUES[i].getBool("hasState") ? rootfw.filesystem.readFileLine("/props/status." + VALUES[i].getString("prop")) : null;
								
								if (VALUES[i].getBool("hasConfig") && VALUES[i].getBool("hasState")) {
									propAttention = levels.get(VALUES[i].getString("name")) != null && levels.get(VALUES[i].getString("name")) != 0 ? levels.get(VALUES[i].getString("name")) : 
									(propState == null && propConfig == null) || (propConfig != null && (propConfig.equals(propState) || (propState == null && propConfig.equals("0")) || (propConfig.equals("2") && (VALUES[i].getString("type") == "load" || VALUES[i].getString("type") == "enable")))) ? 0 : 1;
									
									editor.putString("prop.attention." + VALUES[i].getString("name"), ""+propAttention);
									editor.putString("prop.message." + VALUES[i].getString("name"), messages.get(VALUES[i].getString("name")));
									
									if (lastAttention < propAttention) {
										lastAttention = propAttention;
										
										if (VALUES[i].getString("name") != "script.status") {
											editor.putString("prop.attention.script.status", ""+lastAttention);
											editor.putString("prop.message.script.status", "");
										}
									}
								}
								
	                            if (VALUES[i].getBool("hasConfig")) 
	                                editor.putString("prop.config." + VALUES[i].getString("name"), propConfig);
	
	                            if (VALUES[i].getBool("hasState")) 
	                                editor.putString("prop.state." + VALUES[i].getString("name"), propState);
							}
                        }

						rootfw.filesystem.putFileLine("/props/app.config.loaded", "1");
						rootfw.filesystem.remount("/", "ro");

						editor.putString("app.id", appid);
						
						status = LOADED = true;
					} 
					
				} else {
					editor.putBoolean("check.script", checkScript());
				}
			}
			
		} else {
			status = true;
		}

		if (editor != null) {
			editor.commit();
		}
		
		if (rootfw != null) {
			rootfw.close();
		}
		
		// Re-create this prop info when the app has been closed and re-opened
		for (int i=0; i < VALUES.length; i++) {
			VALUES[i].putBool("active", settings.getInt("check.sdcard.partitions", 0) >= VALUES[i].getInt("SDDependency") ? true : false);
		}
		
		RUNNING = false;
		
		return status;
	}
	
	public static Boolean checkScript() {
		RootFW rootfw = RootFW.getInstance( BaseApplication.getContext().getPackageName() );

		Boolean match = rootfw.utils.matchMd5(BaseApplication.getContext().getResources().getString(R.string.config_script_path), BaseApplication.getContext().getResources().getString(R.string.config_script_id));
		
		rootfw.close();
		
		return match;
	}
	
	public static Boolean hasBusybox() {
		SharedPreferences settings = BaseApplication.getContext().getSharedPreferences("prop_configuration", 0x00000000);
		
		return settings.getBoolean("check.busybox", true);
	}
	
	public static Boolean hasCompatibleBusybox() {
		SharedPreferences settings = BaseApplication.getContext().getSharedPreferences("prop_configuration", 0x00000000);
		
		return settings.getBoolean("check.busybox.compatibility", false);
	}
	
	public static Boolean hasConfiguredBusybox() {
		SharedPreferences settings = BaseApplication.getContext().getSharedPreferences("prop_configuration", 0x00000000);
		
		return settings.getBoolean("check.busybox.configured", false);
	}
	
	public static Boolean hasScript() {
		SharedPreferences settings = BaseApplication.getContext().getSharedPreferences("prop_configuration", 0x00000000);
		
		return settings.getBoolean("check.script", false);
	}
	
	public static Boolean hasSU() {
		SharedPreferences settings = BaseApplication.getContext().getSharedPreferences("prop_configuration", 0x00000000);
		
		return settings.getBoolean("check.superuser", true);
	}
	
	public static Boolean isLoaded() {
		SharedPreferences settings = BaseApplication.getContext().getSharedPreferences("prop_configuration", 0x00000000);
		
		return LOADED && BaseApplication.getContext().getResources().getString(R.string.config_app_id).equals( settings.getString("app.id", null) ) ? true : false;
	}
	
	public static Boolean isRunning() {
		return RUNNING;
	}
	
	protected static class Config extends ElementContainer {
		public Config(String name, String prop, String type, String selector, String reverseOn, String dependedOn, Integer SDDependency, Boolean hasConfig, Boolean hasState, String defState, String defConfig) {
			this.putString("name", name);
			this.putString("prop", prop);
			this.putString("type", type);
			this.putBool("hasConfig", hasConfig);
			this.putBool("hasState", hasState);
			this.putInt("SDDependency", SDDependency);
			this.putString("defState", defState);
			this.putString("defConfig", defConfig);
			this.putString("selector", selector);
			this.putString("reverseOn", reverseOn);
			this.putString("dependedOn", dependedOn);
			this.putBool("update", false);
            this.putBool("active", false);
		}
	}
}
