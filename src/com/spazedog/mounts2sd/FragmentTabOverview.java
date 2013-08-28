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

import android.app.ProgressDialog;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.spazedog.lib.taskmanager.Task;
import com.spazedog.mounts2sd.tools.Preferences;
import com.spazedog.mounts2sd.tools.Utils;
import com.spazedog.mounts2sd.tools.containers.DeviceConfig;
import com.spazedog.mounts2sd.tools.containers.DeviceSetup;
import com.spazedog.mounts2sd.tools.interfaces.TabController;

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
	
	private ProgressDialog mProgressDialog;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setHasOptionsMenu(true);
	}
	
	@Override
	public void onPause() {
		super.onPause();
		
		mPreferences = null;
	}
	
	@Override
	public void onResume() {
		super.onResume();

		if (mPreferences == null) {
			mPreferences = new Preferences((Context) getActivity());
		}
		
		fillContent();
	}
	
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    	mPreferences = new Preferences((Context) getActivity());
    	
        return inflater.inflate(R.layout.fragment_tab_overview, container, false);
    }
	
	@Override
	public void onHiddenChanged(boolean hidden) {
		if (getActivity() != null) {
			((TabController) getActivity()).frameUpdated();
		}
	}
	
	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		onHiddenChanged(false);
		
		buildView();
	}
	
	private void buildView() {
		View view = getView();
		
		DeviceSetup deviceSetup = mPreferences.deviceSetup();
		
    	mViews.get("storage").put("data", view.findViewById(R.id.option_storage_item_internal));
    	mViews.get("storage").put("cache", view.findViewById(R.id.option_storage_item_cache));
    	
    	if (deviceSetup.path_device_map_emmc() == null) {
    		Utils.removeView(view.findViewById(R.id.option_emmc_item_readahead), true);
    		
    	} else {
        	if (deviceSetup.path_device_readahead_emmc() == null && deviceSetup.path_device_scheduler_emmc() == null) {
        		Utils.removeView(view.findViewById(R.id.option_emmc_item_readahead), true);
        		
        	} else {
        		if (deviceSetup.path_device_readahead_emmc() == null) {
        			Utils.removeView(view.findViewById(R.id.option_emmc_item_readahead), false);
        			
        		} else {
        			mViews.get("emmc").put("readahead", view.findViewById(R.id.option_emmc_item_readahead));
        		}
        		
        		if (deviceSetup.path_device_scheduler_emmc() == null) {
        			Utils.removeView(view.findViewById(R.id.option_emmc_item_scheduler), false);
        			
        		} else {
        			mViews.get("emmc").put("scheduler", view.findViewById(R.id.option_emmc_item_scheduler));
        		}
        	}
    	}
    	
    	if (!deviceSetup.support_binary_sqlite3()) {
    		Utils.removeView(view.findViewById(R.id.option_system_item_threshold), true);
    		
    	} else {
    		mViews.get("system").put("threshold", view.findViewById(R.id.option_system_item_threshold));
    	}
		
    	if (deviceSetup.path_device_map_emmc() == null || deviceSetup.path_device_map_sdext() == null) {
    		Utils.removeView(view.findViewById(R.id.option_storage_item_external), false);
    		Utils.removeView(view.findViewById(R.id.option_filesystem_item_fstype), true);
    		
    	} else {
    		mViews.get("storage").put("sdext", view.findViewById(R.id.option_storage_item_external));
    		mViews.get("filesystem").put("driver", view.findViewById(R.id.option_filesystem_item_fstype));
        	
    		if (!deviceSetup.support_binary_tune2fs() || !"ext4".equals(deviceSetup.type_device_sdext())) {
    			Utils.removeView(view.findViewById(R.id.option_filesystem_item_journal), false);
    			
    		} else {
    			mViews.get("filesystem").put("journal", view.findViewById(R.id.option_filesystem_item_journal));
    		}
        	
        	if (!deviceSetup.support_binary_e2fsck()) {
        		Utils.removeView(view.findViewById(R.id.option_filesystem_item_fschk), false);
        		
        	} else {
        		mViews.get("filesystem").put("fschk", view.findViewById(R.id.option_filesystem_item_fschk));
        	}
    	}
    	
    	mViews.get("content").put("apps", view.findViewById(R.id.option_content_item_apps));
    	mViews.get("content").put("data", view.findViewById(R.id.option_content_item_data));
    	mViews.get("content").put("dalvik", view.findViewById(R.id.option_content_item_dalvik));
    	
    	if (!deviceSetup.support_directory_system()) {
    		Utils.removeView(view.findViewById(R.id.option_content_item_system), false);
    		
    	} else {
    		mViews.get("content").put("system", view.findViewById(R.id.option_content_item_system));
    	}
    	
    	if (!deviceSetup.support_directory_library()) {
    		Utils.removeView(view.findViewById(R.id.option_content_item_libs), false);
    		
    	} else {
    		mViews.get("content").put("libs", view.findViewById(R.id.option_content_item_libs));
    	}
    	
    	if (!deviceSetup.support_directory_media()) {
    		Utils.removeView(view.findViewById(R.id.option_content_item_media), false);
    		
    	} else {
    		mViews.get("content").put("media", view.findViewById(R.id.option_content_item_media));
    	}
    	
    	if (deviceSetup.path_device_readahead_immc() == null && deviceSetup.path_device_scheduler_immc() == null) {
    		Utils.removeView(view.findViewById(R.id.option_immc_item_readahead), true);
    		
    	} else {
    		if (deviceSetup.path_device_readahead_immc() == null) {
    			Utils.removeView(view.findViewById(R.id.option_immc_item_readahead), false);
    			
    		} else {
    			mViews.get("immc").put("readahead", view.findViewById(R.id.option_immc_item_readahead));
    		}
    		
    		if (deviceSetup.path_device_scheduler_immc() == null) {
    			Utils.removeView(view.findViewById(R.id.option_immc_item_scheduler), false);
    			
    		} else {
    			mViews.get("immc").put("scheduler", view.findViewById(R.id.option_immc_item_scheduler));
    		}
    	}
    	
    	if (!deviceSetup.support_option_swap() && !deviceSetup.support_option_zram()) {
    		Utils.removeView(view.findViewById(R.id.option_memory_item_swap), true);
    		
    	} else {
    		mViews.get("memory").put("swappiness", view.findViewById(R.id.option_memory_item_swappiness));
    		
    		if (!deviceSetup.support_option_swap()) {
    			Utils.removeView(view.findViewById(R.id.option_memory_item_swap), false);
    			
    		} else {
    			mViews.get("memory").put("swap", view.findViewById(R.id.option_memory_item_swap));
    		}
    		
    		if (!deviceSetup.support_option_zram()) {
    			Utils.removeView(view.findViewById(R.id.option_memory_item_zram), false);
    			
    		} else {
    			mViews.get("memory").put("zram", view.findViewById(R.id.option_memory_item_zram));
    		}
    	}
	}
	
	private void fillContent() {
		DeviceConfig deviceConfig = mPreferences.deviceConfig();
		
		for (String group : mViews.keySet()) {
			Map<String, View> views = mViews.get(group);
			
			for (String name : views.keySet()) {
				if (views.get(name) != null) {
					if (group.equals("storage")) {
						String mount = (String) deviceConfig.find("location_" + group + "_" + name);
						
						((TextView) views.get(name).findViewById(R.id.item_value)).setText(mount != null ? mount : getResources().getString(R.string.device_not_mounted));
						((TextView) views.get(name).findViewById(R.id.item_value_extra)).setText( String.format(getResources().getString(R.string.size_structure), Utils.convertPrifix( ((Long) deviceConfig.find("usage_" + group + "_" + name)).doubleValue() ), Utils.convertPrifix( ((Long) deviceConfig.find("size_" + group + "_" + name)).doubleValue()) ));
						
					} else if (group.equals("system")) {
						((TextView) views.get(name).findViewById(R.id.item_value)).setText( 
								deviceConfig.size_storage_threshold() > 0L ? Utils.convertPrifix( (deviceConfig.size_storage_threshold()).doubleValue() ) : 
									getResources().getString(R.string.status_unknown)
						);
						
					} else if (group.equals("content")) {
						Integer state = (Integer) deviceConfig.find("status_" + group + "_" + name);
						
						((ImageView) views.get(name).findViewById(R.id.item_icon)).setEnabled(state >= 0);
						((ImageView) views.get(name).findViewById(R.id.item_icon)).setSelected(state == 1);
						
						((TextView) views.get(name).findViewById(R.id.item_value_extra)).setText( Utils.convertPrifix( ((Long) deviceConfig.find("usage_" + group + "_" + name)).doubleValue() ) );
						
						if (state < 0) {
							((TextView) views.get(name).findViewById(R.id.item_message)).setText( getResources().getString(R.string.option_message_mount_mismatch) );
							views.get(name).findViewById(R.id.item_message).setVisibility(View.VISIBLE);
						}
						
					} else if (group.equals("memory") && name.equals("swappiness")) {
						((TextView) views.get(name).findViewById(R.id.item_value)).setText( mPreferences.getSelectorValue("swappiness", ""+deviceConfig.find("level_" + group + "_" + name)) );
						
					} else if (group.equals("memory")) {
						if (deviceConfig.find("size_" + group + "_" + name) != null && (Long) deviceConfig.find("size_" + group + "_" + name) > 0) {
							((TextView) views.get(name).findViewById(R.id.item_value)).setText( String.format(getResources().getString(R.string.size_structure), Utils.convertPrifix( ((Long) deviceConfig.find("usage_" + group + "_" + name)).doubleValue() ), Utils.convertPrifix( ((Long) deviceConfig.find("size_" + group + "_" + name)).doubleValue() )) );
							
						} else {
							((TextView) views.get(name).findViewById(R.id.item_value)).setText(R.string.status_disabled);
						}
						
					} else if (group.equals("immc") || group.equals("emmc")) {
						((TextView) views.get(name).findViewById(R.id.item_value)).setText( mPreferences.getSelectorValue(name, (String) deviceConfig.find("value_" + group + "_" + name)) );
						
					} else if (group.equals("filesystem") && name.equals("fschk")) {
						Integer state = (Integer) deviceConfig.find("level_" + group + "_" + name);
						
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
								((Integer) deviceConfig.find("status_" + group + "_" + name)) == 1 ? R.string.status_enabled : R.string.status_disabled);
						
					} else if (group.equals("filesystem")) {
						((TextView) views.get(name).findViewById(R.id.item_value)).setText( mPreferences.getSelectorValue("filesystem", (String) deviceConfig.find("type_" + group + "_" + name)) );
					}
				}
			}
		}
	}
	
	private void loader() {
		new Task<Context, Void, Boolean>(this, "OverviewTab") {
			@Override
			protected void onUIReady() {
				FragmentTabOverview fragment = (FragmentTabOverview) getObject();
				
				if (fragment.mProgressDialog == null) {
					fragment.mProgressDialog = ProgressDialog.show((FragmentActivity) getActivityObject(), "", getResources().getString(R.string.progress_load_config) + "...");
				}
			}
			
			@Override
			protected Boolean doInBackground(Context... params) {
				return new Preferences( (Context) params[0] ).loadDeviceConfig(true);
			}
			
			@Override
			protected void onPostExecute(Boolean result) {
				FragmentTabOverview fragment = (FragmentTabOverview) getObject();
				
				if (fragment.mProgressDialog != null) {
					fragment.mProgressDialog.dismiss();
					fragment.mProgressDialog = null;
				}
				
				if (result) {
					fragment.fillContent();
				}
			}
			
		}.execute(getActivity().getApplicationContext());
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
				loader();
		}
		
		return super.onOptionsItemSelected(item);
	}
}
