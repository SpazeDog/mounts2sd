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

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import android.content.Context;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import com.spazedog.lib.rootfw3.RootFW;
import com.spazedog.lib.rootfw3.extenders.FileExtender.FileData;
import com.spazedog.lib.rootfw3.extenders.FileExtender.FileStat;
import com.spazedog.lib.rootfw3.extenders.FilesystemExtender.MountStat;
import com.spazedog.lib.taskmanager.Task;
import com.spazedog.mounts2sd.tools.Preferences;
import com.spazedog.mounts2sd.tools.Root;
import com.spazedog.mounts2sd.tools.containers.IDeviceConfig;
import com.spazedog.mounts2sd.tools.containers.IDeviceSetup;
import com.spazedog.mounts2sd.tools.interfaces.ITabController;

public class FragmentTabLog extends Fragment {
	
	private Preferences mPreferences;
	
	private static String[] oLogEntry;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		mPreferences = Preferences.getInstance((Context) getActivity());
		
		setHasOptionsMenu(true);
	}
	
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {	
    	ViewGroup view = (ViewGroup) inflater.inflate(R.layout.fragment_tab_log, container, false);
    	TableLayout table = (TableLayout) view.findViewById(R.id.log_table);
    	
    	if (oLogEntry == null) {
    		RootFW rootfw = Root.initiate();
    		FileData data = rootfw.file(getResources().getString(R.string.config_dir_tmp) + "/log.txt").read();
    		
    		if (data == null) {
    			data = rootfw.file("/data/m2sd.fallback.log").read();
    			
    			if (data != null) {
    				oLogEntry = data.getArray();
    			}
    			
    		} else {
    			oLogEntry = data.getArray();
    		}
    		
    		if (oLogEntry == null || oLogEntry.length == 0) {
    			oLogEntry = new String[]{"I/" + getResources().getString(R.string.log_empty)};
    		}
    		
    		Root.release();
    	}
    	
    	Boolean bool = false;
    	
    	Integer color1 = getResources().getColor(resolveAttr(R.attr.colorRef_logItemBackgroundFirst));
    	Integer color2 = getResources().getColor(resolveAttr(R.attr.colorRef_logItemBackgroundSecond));
    	
    	for (int i=0; i < oLogEntry.length; i++) {    		
    		TableRow row = (TableRow) inflater.inflate(R.layout.inflate_log_item, table, false);
    		String[] parts = oLogEntry[i].split("/", 2);
    		
    		((TextView) row.getChildAt(0)).setText( parts.length > 1 ? parts[0] : "?" );
    		((TextView) row.getChildAt(1)).setText( parts.length > 1 ? parts[1] : parts[0] );
    		
    		if ((bool = !bool)) {
    			row.setBackgroundColor( color1 );
    			
    		} else {
    			row.setBackgroundColor( color2 );
    		}
    		
    		table.addView(row);
    	}
    	
        return (View) view;
    }
    
	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		onHiddenChanged(false);
	}
	
	@Override
	public void onHiddenChanged(boolean hidden) {
		if (getActivity() != null && !hidden) {
			((ITabController) getActivity()).onTabUpdate();
		}
	}
	
	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		inflater.inflate(R.menu.fragment_tab_log, menu);
		
		super.onCreateOptionsMenu(menu,inflater);
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch(item.getItemId()) {
			case R.id.menu_log_save:
				saveLogEntry(); break;
				
			case R.id.menu_build_debug:
				generateDebugFile();
		}
		
		return super.onOptionsItemSelected(item);
	}
    
	public Integer resolveAttr(Integer attr) {
		TypedValue typedvalueattr = new TypedValue();
		getActivity().getTheme().resolveAttribute(attr, typedvalueattr, true);
		
		return typedvalueattr.resourceId;
	}
	
	public void saveLogEntry() {
		File sdcardPath = new File (Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getParent() + "/Mounts2SD");
		File logFile = new File(sdcardPath, "log.txt");
		
		sdcardPath.mkdirs();
		
		try {
			BufferedWriter output = new BufferedWriter(new FileWriter(logFile.getAbsolutePath(), false));
			
			for (int i=0; i < oLogEntry.length; i++) {
				output.write(oLogEntry[i] + "\r\n");
			}
			
			output.close();
			
			Toast.makeText(getActivity(), String.format(getResources().getString(R.string.toast_log_copied), logFile.getAbsolutePath()), Toast.LENGTH_LONG).show();
			
		} catch (Throwable e) {
			Toast.makeText(getActivity(), getResources().getString(R.string.toast_log_unsuccessful), Toast.LENGTH_LONG).show();
		}
	}
	
	public void generateDebugFile() {
		new Task<Context, Void, List<String>>(this, "generateDebugFile") {
			@Override
			protected void onPreExecute() {
				setProgressMessage( getResources().getString(R.string.progress_load_debug_file) + "..." );
			}
			
			@Override
			protected List<String> doInBackground(Context... params) {
				RootFW root = Root.initiate();
				List<String> lines = new ArrayList<String>();
				Pattern patternPidMatch = Pattern.compile("^[0-9]+$");
				
				Map<String, MountStat[]> mountStats = new HashMap<String, MountStat[]>();
				Map<String, FileStat[]> fileStats = new HashMap<String, FileStat[]>();
				Map<String, Method[]> deviceConfigs = new HashMap<String, Method[]>();
				
				mountStats.put("FStab", root.filesystem().getFstabList());
				mountStats.put("Mount", root.filesystem().getMountList());
				
				fileStats.put("/proc", root.file("/proc").getDetailedList());
				fileStats.put("/dev/block", root.file("/dev/block").getDetailedList());
				fileStats.put("/data", root.file("/data").getDetailedList());
				fileStats.put("/sd-ext", root.file("/sd-ext").getDetailedList());
				fileStats.put("/system/etc/init.d", root.file("/system/etc/init.d").getDetailedList());
				fileStats.put("/system/etc", root.file("/system/etc").getDetailedList());
				
				deviceConfigs.put("Device Preferences", mPreferences.deviceSetup.listAllOptions());
				deviceConfigs.put("Device Configuration", mPreferences.deviceConfig.listAllOptions());
				deviceConfigs.put("Script Properties", mPreferences.deviceProperties.listAllOptions());
				
				lines.add("Mounts2SD Debug File\r\n");
				
				for (String key : mountStats.keySet()) {
					if (mountStats.get(key) != null) {
						lines.add("\r\n==================================\r\n" + key + " Listing\r\n----------------------------------\r\n");
						
						for (int x=0; x < mountStats.get(key).length; x++) {
							lines.add("[" + mountStats.get(key)[x].device() + "] [" + mountStats.get(key)[x].location() + "] [" + mountStats.get(key)[x].fstype() + "] [" + (mountStats.get(key)[x].options() != null ? TextUtils.join(",", Arrays.asList(mountStats.get(key)[x].options())) : "") + "]\r\n");
						}
					}
				}
				
				for (String key : fileStats.keySet()) {
					if (fileStats.get(key) != null) {
						lines.add("\r\n==================================\r\n" + key + " Listing\r\n----------------------------------\r\n");
						
						for (int x=0; x < fileStats.get(key).length; x++) {
							if (!key.equals("/proc") || !patternPidMatch.matcher(fileStats.get(key)[x].name()).matches()) {
								lines.add("[" + fileStats.get(key)[x].name() + "] [" + fileStats.get(key)[x].link() + "] [" + fileStats.get(key)[x].user() + ":" + fileStats.get(key)[x].group() + "] [" + fileStats.get(key)[x].access() + "]\r\n");
							}
						}
					}
				}
				
				for (String key : deviceConfigs.keySet()) {
					if (deviceConfigs.get(key) != null) {
						lines.add("\r\n==================================\r\n" + key + "\r\n----------------------------------\r\n");
						
						for (int x=0; x < deviceConfigs.get(key).length; x++) {
							try {
								Object value = deviceConfigs.get(key)[x].invoke(
										deviceConfigs.get(key)[x].getDeclaringClass().equals(IDeviceSetup.class) ? mPreferences.deviceSetup : 
											deviceConfigs.get(key)[x].getDeclaringClass().equals(IDeviceConfig.class) ? mPreferences.deviceConfig : mPreferences.deviceProperties
								);
								
								if (deviceConfigs.get(key)[x].getReturnType().equals(Boolean.TYPE)) {
									lines.add(deviceConfigs.get(key)[x].getName() + " = " + ((Boolean) value ? "true" : "false") + "\r\n");
									
								} else {
									lines.add(deviceConfigs.get(key)[x].getName() + " = " + value + "\r\n");
								}
								
							} catch (Throwable e) {}
						}
					}
				}
				
				lines.add("\r\n==================================\r\nScript Log\r\n----------------------------------\r\n");
				
				return lines;
			}
			
			@Override
			protected void onPostExecute(List<String> result) {
				File sdcardPath = new File (Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getParent() + "/Mounts2SD");
				File debugFile = new File(sdcardPath, "debug.txt");
				
				sdcardPath.mkdirs();
				
				try {
					BufferedWriter output = new BufferedWriter(new FileWriter(debugFile.getAbsolutePath(), false));
					
					for (int i=0; i < result.size(); i++) {
						output.write(result.get(i));
					}
					
					for (int i=0; i < oLogEntry.length; i++) {
						output.write(oLogEntry[i] + "\r\n");
					}
					
					output.close();
					
					Toast.makeText(getActivity(), String.format(getResources().getString(R.string.toast_log_copied), debugFile.getAbsolutePath()), Toast.LENGTH_LONG).show();
					
				} catch (Throwable e) {
					Toast.makeText(getActivity(), getResources().getString(R.string.toast_log_unsuccessful), Toast.LENGTH_LONG).show();
				}
			}
			
		}.execute(getActivity().getApplicationContext());
	}
}
