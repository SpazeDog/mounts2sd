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

import java.util.HashMap;
import java.util.Map;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.spazedog.lib.taskmanager.Task;
import com.spazedog.mounts2sd.tools.Common;
import com.spazedog.mounts2sd.tools.Preferences;
import com.spazedog.mounts2sd.tools.Utils;
import com.spazedog.mounts2sd.tools.interfaces.ITabController;

public class FragmentTabOverview extends Fragment {
	
	private Map<String, Map<String, View>> mViews = new HashMap<String, Map<String, View>>(); {
		mViews.put("storage", new HashMap<String, View>());
		mViews.put("system", new HashMap<String, View>());
		mViews.put("content", new HashMap<String, View>());
		mViews.put("memory", new HashMap<String, View>());
		mViews.put("filesystem", new HashMap<String, View>());
		mViews.put("immc", new HashMap<String, View>());
		mViews.put("emmc", new HashMap<String, View>());
	}
	
	private Preferences mPreferences;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		mPreferences = Preferences.getInstance((Context) getActivity());
		
		setHasOptionsMenu(true);
	}
	
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    	return inflater.inflate(R.layout.fragment_tab_overview, container, false);
    }
    
	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		
		onHiddenChanged(false);
		
		handleViewSetup();
		handleViewContent();
	}
	
	@Override
	public void onHiddenChanged(boolean hidden) {
		if (getActivity() != null && !hidden) {
			((ITabController) getActivity()).onTabUpdate();
		}
	}
	
	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		inflater.inflate(R.menu.fragment_tab_overview, menu);
		
		super.onCreateOptionsMenu(menu,inflater);
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch(item.getItemId()) {
			case R.id.menu_overview_reload:
				reloadDeviceConfig();
		}
	
		return super.onOptionsItemSelected(item);
	}
	
	private void reloadDeviceConfig() {
		new Task<Context, Void, Boolean>(this, "OverviewTabSetup") {
			@Override
			protected void onPreExecute() {
				setProgressMessage( getResources().getString(R.string.progress_load_config) + "..." );
			}
			
			@Override
			protected Boolean doInBackground(Context... params) {
				Common.wait(350);
				
				return 
					Preferences.getInstance( (Context) params[0] ).deviceConfig.load(true);
			}
			
			@Override
			protected void onPostExecute(Boolean result) {
				if (result) {
					((FragmentTabOverview) getObject()).handleViewContent();
				}
			}
			
		}.execute(getActivity().getApplicationContext());
	}
	
	private void handleViewSetup() {
		View view = getView();
		
		optainView("storage", "data", view.findViewById(R.id.option_storage_item_internal), false);
		optainView("storage", "cache", view.findViewById(R.id.option_storage_item_cache), false);
		
		if (mPreferences.deviceSetup.path_device_map_emmc() == null
				|| (mPreferences.deviceSetup.path_device_readahead_emmc() == null 
				&& mPreferences.deviceSetup.path_device_scheduler_emmc() == null)) {
			
			Utils.removeView(view.findViewById(R.id.option_emmc_item_readahead), true);
			
		} else {
			optainView("emmc", "readahead", view.findViewById(R.id.option_emmc_item_readahead), mPreferences.deviceSetup.path_device_readahead_emmc() == null);
			optainView("emmc", "scheduler", view.findViewById(R.id.option_emmc_item_scheduler), mPreferences.deviceSetup.path_device_scheduler_emmc() == null);
		}
		
    	if (!mPreferences.deviceSetup.support_binary_sqlite3()) {
    		Utils.removeView(view.findViewById(R.id.option_system_item_threshold), true);
    		
    	} else {
    		optainView("system", "threshold", view.findViewById(R.id.option_system_item_threshold), false);
    	}
    	
    	if (mPreferences.deviceSetup.path_device_map_sdext() == null) {
    		Utils.removeView(view.findViewById(R.id.option_storage_item_external), false);
    		Utils.removeView(view.findViewById(R.id.option_filesystem_item_fstype), true);
    		
    	} else {
    		optainView("storage", "sdext", view.findViewById(R.id.option_storage_item_external), false);
    		optainView("filesystem", "driver", view.findViewById(R.id.option_filesystem_item_fstype), false);
    		optainView("filesystem", "journal", view.findViewById(R.id.option_filesystem_item_journal), (!mPreferences.deviceSetup.support_binary_tune2fs() || !"ext4".equals(mPreferences.deviceSetup.type_device_sdext())));
    		optainView("filesystem", "fschk", view.findViewById(R.id.option_filesystem_item_fschk), !mPreferences.deviceSetup.support_binary_e2fsck());
    	}
    	
    	optainView("content", "apps", view.findViewById(R.id.option_content_item_apps), false);
    	optainView("content", "sysapps", view.findViewById(R.id.option_content_item_sysapps), false);
    	optainView("content", "data", view.findViewById(R.id.option_content_item_data), false);
    	optainView("content", "dalvik", view.findViewById(R.id.option_content_item_dalvik), false);
    	optainView("content", "system", view.findViewById(R.id.option_content_item_system), !mPreferences.deviceSetup.support_directory_system());
    	optainView("content", "libs", view.findViewById(R.id.option_content_item_libs), !mPreferences.deviceSetup.support_directory_library());
    	optainView("content", "media", view.findViewById(R.id.option_content_item_media), !mPreferences.deviceSetup.support_directory_media());
		
    	if (mPreferences.deviceSetup.path_device_readahead_immc() == null 
    			&& mPreferences.deviceSetup.path_device_scheduler_immc() == null) {
    		
    		Utils.removeView(view.findViewById(R.id.option_immc_item_readahead), true);
    		
    	} else {
    		optainView("immc", "readahead", view.findViewById(R.id.option_immc_item_readahead), mPreferences.deviceSetup.path_device_readahead_immc() == null);
    		optainView("immc", "scheduler", view.findViewById(R.id.option_immc_item_scheduler), mPreferences.deviceSetup.path_device_scheduler_immc() == null);
    	}
    	
    	if (!mPreferences.deviceSetup.support_option_swap() 
    			&& !mPreferences.deviceSetup.support_option_zram()) {
    		
    		Utils.removeView(view.findViewById(R.id.option_memory_item_swap), true);
    		
    	} else {
    		optainView("memory", "swappiness", view.findViewById(R.id.option_memory_item_swappiness), false);
    		optainView("memory", "swap", view.findViewById(R.id.option_memory_item_swap), !mPreferences.deviceSetup.support_option_swap());
    		optainView("memory", "zram", view.findViewById(R.id.option_memory_item_zram), !mPreferences.deviceSetup.support_option_zram());
    	}
	}
	
	private void handleViewContent() {
		for (String group : mViews.keySet()) {
			Map<String, View> views = mViews.get(group);
			
			for (String name : views.keySet()) {
				if (views.get(name) != null) {
					if (group.equals("storage")) {
						String mount = (String) mPreferences.deviceConfig.find("location_" + group + "_" + name);
						
						((TextView) views.get(name).findViewById(R.id.item_value)).setText(mount != null ? mount : getResources().getString(R.string.device_not_mounted));
						((TextView) views.get(name).findViewById(R.id.item_value_extra)).setText( String.format(getResources().getString(R.string.size_structure), Common.convertPrifix( ((Long) mPreferences.deviceConfig.find("usage_" + group + "_" + name)).doubleValue() ), Common.convertPrifix( ((Long) mPreferences.deviceConfig.find("size_" + group + "_" + name)).doubleValue()) ));
						
					} else if (group.equals("system")) {
						((TextView) views.get(name).findViewById(R.id.item_value)).setText( 
								mPreferences.deviceConfig.size_storage_threshold() > 0L ? Common.convertPrifix( (mPreferences.deviceConfig.size_storage_threshold()).doubleValue() ) : 
									getResources().getString(R.string.status_unknown)
						);
						
					} else if (group.equals("content")) {
						Integer state = (Integer) mPreferences.deviceConfig.find("status_" + group + "_" + name);
						
						((ImageView) views.get(name).findViewById(R.id.item_icon)).setEnabled(state >= 0);
						((ImageView) views.get(name).findViewById(R.id.item_icon)).setSelected(state == 1);
						
						((TextView) views.get(name).findViewById(R.id.item_value_extra)).setText( Common.convertPrifix( ((Long) mPreferences.deviceConfig.find("usage_" + group + "_" + name)).doubleValue() ) );
						
						if (state < 0) {
							((TextView) views.get(name).findViewById(R.id.item_message)).setText( getResources().getString(R.string.option_message_mount_mismatch) );
							views.get(name).findViewById(R.id.item_message).setVisibility(View.VISIBLE);
						}
						
					} else if (group.equals("memory") && name.equals("swappiness")) {
						((TextView) views.get(name).findViewById(R.id.item_value)).setText( Utils.getSelectorValue(getActivity(), "swappiness", "" + mPreferences.deviceConfig.find("level_" + group + "_" + name)) );
						
					} else if (group.equals("memory")) {
						if (mPreferences.deviceConfig.find("size_" + group + "_" + name) != null && (Long) mPreferences.deviceConfig.find("size_" + group + "_" + name) > 0) {
							((TextView) views.get(name).findViewById(R.id.item_value)).setText( String.format(getResources().getString(R.string.size_structure), Common.convertPrifix( ((Long) mPreferences.deviceConfig.find("usage_" + group + "_" + name)).doubleValue() ), Common.convertPrifix( ((Long) mPreferences.deviceConfig.find("size_" + group + "_" + name)).doubleValue() )) );
							
						} else {
							((TextView) views.get(name).findViewById(R.id.item_value)).setText(R.string.status_disabled);
						}
						
					} else if (group.equals("immc") || group.equals("emmc")) {
						((TextView) views.get(name).findViewById(R.id.item_value)).setText( Utils.getSelectorValue(getActivity(), name, (String) mPreferences.deviceConfig.find("value_" + group + "_" + name)) );
						
					} else if (group.equals("filesystem") && name.equals("fschk")) {
						Integer state = (Integer) mPreferences.deviceConfig.find("level_" + group + "_" + name);
						
						((TextView) views.get(name).findViewById(R.id.item_value)).setText(
								state < 0 ? R.string.status_disabled : 
									state == 0 ? R.string.status_okay : 
										state < 5 ? R.string.status_warning : R.string.status_error);
						
						if (state > 0) {
							((TextView) views.get(name).findViewById(R.id.item_message)).setText( state < 5 ? 
									getResources().getString(R.string.option_message_fschk_warning) : 
										getResources().getString(R.string.option_message_fschk_error) );
							
							views.get(name).findViewById(R.id.item_message).setVisibility(View.VISIBLE);
						}
						
					}  else if (group.equals("filesystem") && name.equals("journal")) {
						((TextView) views.get(name).findViewById(R.id.item_value)).setText(
								((Integer) mPreferences.deviceConfig.find("status_" + group + "_" + name)) == 1 ? R.string.status_enabled : R.string.status_disabled);
						
					} else if (group.equals("filesystem")) {
						((TextView) views.get(name).findViewById(R.id.item_value)).setText( Utils.getSelectorValue(getActivity(), "filesystem", (String) mPreferences.deviceConfig.find("type_" + group + "_" + name)) );
					}
				}
			}
		}
	}
	
	private void optainView(String group, String name, View view, Boolean remove) {
		if (remove) {
			Utils.removeView(view, false);
			
		} else {
			mViews.get(group).put(name, view);
		}
	}
}
