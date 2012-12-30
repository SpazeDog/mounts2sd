package com.spazedog.mounts2sd;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;

import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.util.Base64;

import com.spazedog.mounts2sd.UtilsHelper.ElementContainer;
import com.spazedog.mounts2sd.UtilsHelper.RootAccount;

public class SettingsHelper {
	protected static Config VALUES[] = {
		new Config("script.status", "enabled", "load", null, null, null, 0, true, true, null, "0"),
		new Config("content.apps.status", "move_apps", "load", null, "storage.rmount.status", "script.status", 2, true, true, null, "1"),
		new Config("content.dalvik.status", "move_dalvik", "load", null, "storage.rmount.status", "script.status", 2, true, true, null, "0"),
		new Config("content.data.status", "move_data", "load", null, "storage.rmount.status", "script.status", 2, true, true, null, "0"),
		new Config("storage.cache.status", "enable_cache", "load", "switch", null, "script.status", 0, true, true, null, "1"),
		new Config("storage.rmount.status", "enable_reversed_mount", "load", null, null, "script.status", 2, true, true, null, "0"),
		new Config("memory.swap.status", "enable_swap", "load", null, null, "script.status", 3, true, true, null, "1"),
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
		new Config("path.cache.storage", "destination.cache", "path", null, null, null, 0, false, true, "/cache", null),
		new Config("log.status", "log", "log", null, null, null, 0, false, true, null, null)
	};
	
	protected static Map<String, Integer> KEYS = new HashMap<String, Integer>();
	
	protected static Boolean COMMIT = false;
	protected static Boolean LOADED = false;
	protected static Boolean RUNNING = false;

    protected static Integer getKey(String pName) {
        if (KEYS.size() == 0) {
            for (int i=0; i < VALUES.length; i++) {
                KEYS.put(VALUES[i].getString("name"), i);
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
	
	public static String propFileName(String pName) {
		return VALUES[ getKey(pName) ].getString("prop");
	}
	
	public static String propType(String pName) {
		return VALUES[ getKey(pName) ].getString("type");
	}
	
	public static Boolean propHasState(String pName) {
		return VALUES[ getKey(pName) ].getBool("hasState");
	}
	
	public static Boolean propHasConfig(String pName) {
		return VALUES[ getKey(pName) ].getBool("hasConfig");
	}
	
	public static Boolean propIsActive(String pName) {
		return VALUES[ getKey(pName) ].getBool("active");
	}
	
	public static String propSelector(String pName) {
		return VALUES[ getKey(pName) ].getString("selector");
	}
	
	public static String propReverseOn(String pName) {
		return VALUES[ getKey(pName) ].getString("reverseOn");
	}
	
	public static String propDependedOn(String pName) {
		return VALUES[ getKey(pName) ].getString("dependedOn");
	}
	
	public static String propDefaultConfig(String pName) {
		return VALUES[ getKey(pName) ].getString("defConfig");
	}

	public static String propDefaultState(String pName) {
		return VALUES[ getKey(pName) ].getString("defState");
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
	
	public static String getPropContent(String pName) {
		SharedPreferences settings = BaseApplication.getContext().getSharedPreferences("prop_configuration", 0x00000000);
		
		String content = settings.getString("prop.content." + pName, null);
		
		if (content != null) {
			try {
				return new String(Base64.decode(content, Base64.URL_SAFE), "UTF-8");
				
			} catch (UnsupportedEncodingException e) {
				return null;
			}
		}
		
		return null;
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
			RootAccount root = RootAccount.getInstance(false);
			
			if (root.isConnected()) {
				SharedPreferences settings = BaseApplication.getContext().getSharedPreferences("prop_configuration", 0x00000000);
				
				for (int i=0; i < VALUES.length; i++) {
					if (VALUES[i].getBool("hasConfig")) {
						if (VALUES[i].getBool("update")) {
							root.filePutLine("/data/property/m2sd." + VALUES[i].getString("prop"), settings.getString("prop.config." + VALUES[i].getString("name"), ""));
							
							VALUES[i].putBool("update", false);
						}
					}
				}
				
				COMMIT = false;
			}
			
			root.close();
			
		} else {
			return true;
		}
		
		return false;
	}
	
	public static Boolean loadConfigs() {
		RUNNING = true;
		
		String appid = BaseApplication.getContext().getResources().getString(R.string.config_app_id);
		String scriptpath = BaseApplication.getContext().getResources().getString(R.string.config_script_path);
		
		RootAccount root = null;
		SharedPreferences settings = BaseApplication.getContext().getSharedPreferences("prop_configuration", 0x00000000);
		Editor editor = null;
		Boolean status = false;
		
		if ((!LOADED && !(LOADED = new File("/props/app.config.loaded").isFile())) || !appid.equals( settings.getString("app.id", null) )) {
			root = RootAccount.getInstance(true);
			editor = settings.edit();
			
			editor.clear();
			
			if (!root.isConnected()) {
				editor.putBoolean("check.superuser", false);
				
			} else if(!root.checkBusybox()) {
				editor.putBoolean("check.busybox", false);
				
			} else {
				String bbCheck = root.execute("( busybox [ 1 -eq 0 ] || busybox [ 0 -eq 1 ] ) && busybox echo no || busybox echo '$(sed -n '1p' /dev/null)remove this part okay:bla=no-4' | busybox grep -e '.*bla=no-[1-9]*' | busybox sed -e 's/^remove //' | busybox awk '{print $3}' | busybox cut -d ':' -f1 | md5sum | busybox awk '{print $1}'", RootAccount.RETURN_LINE);
				
				if (bbCheck != null && bbCheck.contains("46a313b06d28557b0ed1e03fb3d92a40")) {
					editor.putBoolean("check.busybox.compatibility", true);
					
					if ("0".equals(root.execute("/system/bin/busybox.sh check", RootAccount.RETURN_CODE))) {
						editor.putBoolean("check.busybox.configured", true);
					}
					
					if (checkScript()) {
						editor.putBoolean("check.script", true);
						
						if (!new File("/props/app.finalized.script").isFile()) {
							// Execute whatever needs to be done after boot
							root.execute(scriptpath + " finalize", RootAccount.RETURN_CODE);
							root.execute("busybox [ ! -d /props ] && busybox mkdir /props\nbusybox echo 1 > /props/app.finalized.script", RootAccount.RETURN_CODE);
						}
						
						Integer sdLevel = 0;
						for (int i=0; i < 10; i++) {
							if ("SD".equals( root.execute("busybox [ -e /sys/block/mmcblk" + i + "/device/type ] && busybox cat /sys/block/mmcblk" + i + "/device/type || busybox echo none", RootAccount.RETURN_LINE) )) {
								for (int x=1; x < 4; x++) {
									if ("1".equals( root.execute("busybox [ -e /dev/block/mmcblk" + i + "p" + x + " ] && busybox echo 1 || busybox echo 0", RootAccount.RETURN_LINE) )) {
										sdLevel = x;
										
									} else {
										break;
									}
								}
								
								break;
							}
						}
						
						editor.putInt("check.sdcard.partitions", sdLevel);

						String propConfig, propState, propMessage, propAttention, lastAttentionLevel=null;
                        Integer propMessageId;
						
						for (int i=0; i < VALUES.length; i++) {
							if (VALUES[i].getString("prop") != null) {
								propMessage = root.fileReadLine("/props/message." + VALUES[i].getString("prop"));
	                            
	                            if (propMessage != null) {
	                                propMessageId = BaseApplication.getContext().getResources().getIdentifier( ("script_msg_" + UtilsHelper.md5(propMessage)), "string", "com.spazedog.mounts2sd");
	
	                                editor.putString("prop.message." + VALUES[i].getString("name"),
	                                    propMessageId != null && propMessageId != 0 ? (propMessage = BaseApplication.getContext().getResources().getString(propMessageId)) : propMessage
	                                );
	                            }
	
	                            propConfig = VALUES[i].getBool("hasConfig") ? root.fileReadLine("/props/config." + VALUES[i].getString("prop")) : null;
	                            propState = VALUES[i].getBool("hasState") ? root.fileReadLine("/props/status." + VALUES[i].getString("prop")) : null;
	                            propAttention = VALUES[i].getBool("hasState") ? root.fileReadLine("/props/attention." + VALUES[i].getString("prop")) : null;
	
	                            if (VALUES[i].getBool("hasConfig") && VALUES[i].getBool("hasState")) {
	 					        	propAttention = propAttention != null && !propAttention.equals("0") ? propAttention : 
						        		(propState == null && propConfig == null) || (propConfig != null && (propConfig.equals(propState) || (propState == null && propConfig.equals("0")) || (propConfig.equals("2") && VALUES[i].getString("type") == "load"))) ? "0" : "1";
	                            }
	
	                            if (propConfig != null) 
	                                editor.putString("prop.config." + VALUES[i].getString("name"), propConfig);
	
	                            if (propState != null) 
	                                editor.putString("prop.state." + VALUES[i].getString("name"), propState);
	
	                            if (propAttention != null) 
	                                editor.putString("prop.attention." + VALUES[i].getString("name"), propAttention);
	                            
								if ((lastAttentionLevel == null && propAttention != null && propAttention.matches("^[0-9]$")) || ((lastAttentionLevel != null && lastAttentionLevel.matches("^[0-9]$") && propAttention != null && propAttention.matches("^[0-9]$")) && Integer.parseInt(propAttention) >= Integer.parseInt(lastAttentionLevel))) {
									editor.putString("prop.attention.script.status", (lastAttentionLevel = propAttention));
	
	                                if (VALUES[i].getString("type") == "log" && propMessage != null) {
	                                	editor.putString("prop.message.script.status", propMessage);
	                                }
								}
								
								if (VALUES[i].getString("type") == "log") {
									String propContent = root.fileReadAll("/props/content." + VALUES[i].getString("prop"));
									
									if (propContent != null && !"".equals(propContent)) {
										editor.putString("prop.content." + VALUES[i].getString("name"), Base64.encodeToString(propContent.getBytes(), Base64.URL_SAFE));
									}
								}
								
								lastAttentionLevel = propAttention;
							}
                        }

						root.execute("busybox [ ! -d /props ] && busybox mkdir /props\nbusybox echo 1 > /props/app.config.loaded", RootAccount.RETURN_CODE);

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
		
		if (root != null) {
			root.close();
		}
		
		// Re-create this prop info when the app has been closed and re-opened
		for (int i=0; i < VALUES.length; i++) {
			VALUES[i].putBool("active", settings.getInt("check.sdcard.partitions", 0) >= VALUES[i].getInt("SDDependency") ? true : false);
		}
		
		RUNNING = false;
		
		return status;
	}
	
	public static Boolean checkScript() {
		RootAccount root = RootAccount.getInstance(false);
		String scriptId = root.execute("busybox md5sum " + BaseApplication.getContext().getResources().getString(R.string.config_script_path), RootAccount.RETURN_LINE);
		root.close();
		
		if (scriptId != null) {
			return scriptId.contains( BaseApplication.getContext().getResources().getString(R.string.config_script_id) );
		}
		
		return false;
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