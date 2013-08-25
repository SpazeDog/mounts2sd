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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.spazedog.lib.rootfw3.RootFW;
import com.spazedog.lib.rootfw3.extenders.FileExtender.FileData;
import com.spazedog.mounts2sd.tools.Preferences;
import com.spazedog.mounts2sd.tools.Root;
import com.spazedog.mounts2sd.tools.ViewEventHandler;
import com.spazedog.mounts2sd.tools.ViewEventHandler.ViewClickListener;
import com.spazedog.mounts2sd.tools.containers.DeviceProperties;
import com.spazedog.mounts2sd.tools.containers.DeviceSetup;
import com.spazedog.mounts2sd.tools.interfaces.DialogListener;
import com.spazedog.mounts2sd.tools.interfaces.DialogSelectorResponse;
import com.spazedog.mounts2sd.tools.interfaces.TabController;

public class FragmentTabConfigure extends Fragment implements ViewClickListener, DialogListener, DialogSelectorResponse {
	
	private ViewGroup mOptionStorageCache;
	private ViewGroup mOptionContentApps;
	private ViewGroup mOptionContentLibs;
	private ViewGroup mOptionContentData;
	private ViewGroup mOptionContentDalvik;
	private ViewGroup mOptionContentSystem;
	private ViewGroup mOptionContentMedia;
	private ViewGroup mOptionMemorySwap;
	private ViewGroup mOptionMemoryZram;
	private ViewGroup mOptionMemorySwappiness;
	private ViewGroup mOptionFilesystemFschk;
	private ViewGroup mOptionFilesystemFstype;
	private ViewGroup mOptionFilesystemJournal;
	private ViewGroup mOptionMiscSafemode;
	private ViewGroup mOptionMiscDebug;
	private ViewGroup mOptionImmcScheduler;
	private ViewGroup mOptionImmcReadahead;
	private ViewGroup mOptionEmmcScheduler;
	private ViewGroup mOptionEmmcReadahead;
	private ViewGroup mOptionSystemThreshold;
	
	private Preferences mPreferences;
	
	private static Map<Integer, String[]> mEnabledValues = new HashMap<Integer, String[]>();
	
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    	mPreferences = new Preferences((Context) getActivity());
    	
        return inflater.inflate(R.layout.fragment_tab_configure, container, false);
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
		
		DeviceProperties deviceProperties = mPreferences.deviceProperties();
		
		mOptionContentApps = (ViewGroup) view.findViewById(R.id.option_content_item_apps);
		mOptionContentApps.setOnTouchListener(new ViewEventHandler(this));
		mOptionContentApps.setSelected(deviceProperties.move_apps());
		
		mOptionContentLibs = (ViewGroup) view.findViewById(R.id.option_content_item_libs);
		mOptionContentLibs.setOnTouchListener(new ViewEventHandler(this));
		mOptionContentLibs.setSelected(deviceProperties.move_libs());
		
		mOptionContentData = (ViewGroup) view.findViewById(R.id.option_content_item_data);
		mOptionContentData.setOnTouchListener(new ViewEventHandler(this));
		mOptionContentData.setSelected(deviceProperties.move_data());
		
		mOptionContentDalvik = (ViewGroup) view.findViewById(R.id.option_content_item_dalvik);
		mOptionContentDalvik.setOnTouchListener(new ViewEventHandler(this));
		mOptionContentDalvik.setSelected(deviceProperties.move_dalvik());
		
		mOptionContentSystem = (ViewGroup) view.findViewById(R.id.option_content_item_system);
		mOptionContentSystem.setOnTouchListener(new ViewEventHandler(this));
		mOptionContentSystem.setSelected(deviceProperties.move_system());
		
		mOptionContentMedia = (ViewGroup) view.findViewById(R.id.option_content_item_media);
		mOptionContentMedia.setOnTouchListener(new ViewEventHandler(this));
		mOptionContentMedia.setSelected(deviceProperties.move_media());
		
		mOptionMemorySwap = (ViewGroup) view.findViewById(R.id.option_memory_item_swap);
		mOptionMemorySwap.setOnTouchListener(new ViewEventHandler(this));
		mOptionMemorySwap.setSelected(deviceProperties.enable_swap());
		
		mOptionFilesystemFschk = (ViewGroup) view.findViewById(R.id.option_filesystem_item_fschk);
		mOptionFilesystemFschk.setOnTouchListener(new ViewEventHandler(this));
		mOptionFilesystemFschk.setSelected(deviceProperties.run_sdext_fschk());

		mOptionMiscSafemode = (ViewGroup) view.findViewById(R.id.option_misc_item_safemode);
		mOptionMiscSafemode.setOnTouchListener(new ViewEventHandler(this));
		mOptionMiscSafemode.setSelected(deviceProperties.disable_safemode());
		
		mOptionMiscDebug = (ViewGroup) view.findViewById(R.id.option_misc_item_debug);
		mOptionMiscDebug.setOnTouchListener(new ViewEventHandler(this));
		mOptionMiscDebug.setSelected(deviceProperties.enable_debug());

		mOptionMemoryZram = (ViewGroup) view.findViewById(R.id.option_memory_item_zram);
		mOptionMemoryZram.setOnTouchListener(new ViewEventHandler(this));
		mOptionMemoryZram.setTag( new String[]{"zram", "" + deviceProperties.set_zram_compression()} );
		((TextView) mOptionMemoryZram.findViewById(R.id.item_value)).setText( mPreferences.getSelectorValue("zram", "" + deviceProperties.set_zram_compression()) );
		
		mOptionMemorySwappiness = (ViewGroup) view.findViewById(R.id.option_memory_item_swappiness);
		mOptionMemorySwappiness.setOnTouchListener(new ViewEventHandler(this));
		mOptionMemorySwappiness.setTag( new String[]{"swappiness", "" + deviceProperties.set_swap_level()} );
		((TextView) mOptionMemorySwappiness.findViewById(R.id.item_value)).setText( mPreferences.getSelectorValue("swappiness", "" + deviceProperties.set_swap_level()) );
		
		mOptionFilesystemFstype = (ViewGroup) view.findViewById(R.id.option_filesystem_item_fstype);
		mOptionFilesystemFstype.setOnTouchListener(new ViewEventHandler(this));
		mOptionFilesystemFstype.setTag( new String[]{"filesystem", "" + deviceProperties.set_sdext_fstype()} );
		((TextView) mOptionFilesystemFstype.findViewById(R.id.item_value)).setText( mPreferences.getSelectorValue("filesystem", "" + deviceProperties.set_sdext_fstype()) );
		
		mOptionFilesystemJournal = (ViewGroup) view.findViewById(R.id.option_filesystem_item_journal);
		mOptionFilesystemJournal.setOnTouchListener(new ViewEventHandler(this));
		mOptionFilesystemJournal.setTag( new String[]{"journal", "" + deviceProperties.enable_sdext_journal()} );
		((TextView) mOptionFilesystemJournal.findViewById(R.id.item_value)).setText( mPreferences.getSelectorValue("journal", "" + deviceProperties.enable_sdext_journal()) );
		
		mOptionStorageCache = (ViewGroup) view.findViewById(R.id.option_storage_item_cache);
		mOptionStorageCache.setOnTouchListener(new ViewEventHandler(this));
		mOptionStorageCache.setTag( new String[]{"cache", "" + deviceProperties.enable_cache()} );
		((TextView) mOptionStorageCache.findViewById(R.id.item_value)).setText( mPreferences.getSelectorValue("cache", "" + deviceProperties.enable_cache()) );

		mOptionImmcScheduler = (ViewGroup) view.findViewById(R.id.option_immc_item_scheduler);
		mOptionImmcScheduler.setOnTouchListener(new ViewEventHandler(this));
		mOptionImmcScheduler.setTag( new String[]{"scheduler", "" + deviceProperties.set_immc_scheduler()} );
		((TextView) mOptionImmcScheduler.findViewById(R.id.item_value)).setText( mPreferences.getSelectorValue("scheduler", "" + deviceProperties.set_immc_scheduler()) );
		
		mOptionImmcReadahead = (ViewGroup) view.findViewById(R.id.option_immc_item_readahead);
		mOptionImmcReadahead.setOnTouchListener(new ViewEventHandler(this));
		mOptionImmcReadahead.setTag( new String[]{"readahead", "" + deviceProperties.set_immc_readahead()} );
		((TextView) mOptionImmcReadahead.findViewById(R.id.item_value)).setText( mPreferences.getSelectorValue("readahead", "" + deviceProperties.set_immc_readahead()) );
		
		mOptionEmmcScheduler = (ViewGroup) view.findViewById(R.id.option_emmc_item_scheduler);
		mOptionEmmcScheduler.setOnTouchListener(new ViewEventHandler(this));
		mOptionEmmcScheduler.setTag( new String[]{"scheduler", "" + deviceProperties.set_emmc_scheduler()} );
		((TextView) mOptionEmmcScheduler.findViewById(R.id.item_value)).setText( mPreferences.getSelectorValue("scheduler", "" + deviceProperties.set_emmc_scheduler()) );
		
		mOptionEmmcReadahead = (ViewGroup) view.findViewById(R.id.option_emmc_item_readahead);
		mOptionEmmcReadahead.setOnTouchListener(new ViewEventHandler(this));
		mOptionEmmcReadahead.setTag( new String[]{"readahead", "" + deviceProperties.set_emmc_readahead()} );
		((TextView) mOptionEmmcReadahead.findViewById(R.id.item_value)).setText( mPreferences.getSelectorValue("readahead", "" + deviceProperties.set_emmc_readahead()) );
		
		mOptionSystemThreshold = (ViewGroup) view.findViewById(R.id.option_system_item_threshold);
		mOptionSystemThreshold.setOnTouchListener(new ViewEventHandler(this));
		mOptionSystemThreshold.setTag( new String[]{"threshold", "" + deviceProperties.set_storage_threshold()} );
		((TextView) mOptionSystemThreshold.findViewById(R.id.item_value)).setText( mPreferences.getSelectorValue("threshold", "" + deviceProperties.set_storage_threshold()) );
	}
	
	@Override
	public void onPause() {
		super.onPause();
		
		mPreferences.saveDeviceProperties();
		mPreferences = null;
	}
	
	@Override
	public void onResume() {
		super.onResume();

		if (mPreferences == null) {
			mPreferences = new Preferences((Context) getActivity());
		}
		
		handleEnabledState();
	}
	
	private void handleEnabledState() {
		DeviceSetup deviceSetup = mPreferences.deviceSetup();
		DeviceProperties deviceProperties = mPreferences.deviceProperties();
		
		Boolean workingScript = deviceSetup.environment_startup_script();
		Boolean workingSdext = workingScript && deviceSetup.path_device_map_sdext() != null;
		Boolean safeMode = deviceSetup.init_implementation().equals("service") && !deviceProperties.disable_safemode();
		
		mOptionContentApps.setEnabled(workingSdext);
		mOptionContentData.setEnabled(workingSdext && !safeMode);
		mOptionContentDalvik.setEnabled(workingSdext && !safeMode);
		mOptionContentLibs.setEnabled(workingSdext && !safeMode && deviceSetup.support_directory_library());
		mOptionContentMedia.setEnabled(workingSdext && !safeMode && deviceSetup.support_directory_media());
		mOptionContentSystem.setEnabled(workingSdext && !safeMode && deviceSetup.support_directory_system());
		mOptionMemorySwap.setEnabled(workingSdext && deviceSetup.support_option_swap());
		mOptionMemoryZram.setEnabled(workingScript && deviceSetup.support_option_zram());
		mOptionMemorySwappiness.setEnabled(workingScript && (deviceSetup.support_option_zram() || deviceSetup.support_option_swap()));
		mOptionFilesystemFschk.setEnabled(workingSdext && deviceSetup.support_binary_e2fsck());
		mOptionFilesystemFstype.setEnabled(workingSdext);
		mOptionFilesystemJournal.setEnabled(workingSdext && deviceSetup.support_binary_tune2fs() && "ext4".equals(deviceSetup.type_device_sdext()));
		mOptionMiscSafemode.setEnabled(workingScript && deviceSetup.init_implementation().equals("service"));
		mOptionMiscDebug.setEnabled(workingScript && deviceSetup.environment_startup_script());
		mOptionStorageCache.setEnabled(workingScript);
		mOptionImmcScheduler.setEnabled(workingScript && deviceSetup.path_device_scheduler_immc() != null);
		mOptionImmcReadahead.setEnabled(workingScript && deviceSetup.path_device_readahead_immc() != null);
		mOptionEmmcScheduler.setEnabled(workingSdext && deviceSetup.path_device_scheduler_emmc() != null);
		mOptionEmmcReadahead.setEnabled(workingSdext && deviceSetup.path_device_readahead_emmc() != null);
		mOptionSystemThreshold.setEnabled(workingScript && deviceSetup.support_binary_sqlite3());
	}

	@Override
	public void onViewClick(View v) {
		if (v == mOptionContentApps ||
				v == mOptionContentData ||
				v == mOptionContentDalvik || 
				v == mOptionContentLibs || 
				v == mOptionContentMedia || 
				v == mOptionContentSystem || 
				v == mOptionMemorySwap || 
				v == mOptionFilesystemFschk || 
				v == mOptionMiscSafemode || 
				v == mOptionMiscDebug) {
			
			switch (v.getId()) {
				case R.id.option_content_item_apps: mPreferences.deviceProperties().move_apps( !v.isSelected() ); break;
				case R.id.option_content_item_data: mPreferences.deviceProperties().move_data( !v.isSelected() ); break;
				case R.id.option_content_item_dalvik: mPreferences.deviceProperties().move_dalvik( !v.isSelected() ); break;
				case R.id.option_content_item_libs: mPreferences.deviceProperties().move_libs( !v.isSelected() ); break;
				case R.id.option_content_item_media: mPreferences.deviceProperties().move_media( !v.isSelected() ); break;
				case R.id.option_content_item_system: mPreferences.deviceProperties().move_system( !v.isSelected() ); break;
				case R.id.option_memory_item_swap: mPreferences.deviceProperties().enable_swap( !v.isSelected() ); break;
				case R.id.option_filesystem_item_fschk: mPreferences.deviceProperties().run_sdext_fschk( !v.isSelected() ); break;
				case R.id.option_misc_item_safemode: mPreferences.deviceProperties().disable_safemode( !v.isSelected() ); break;
				case R.id.option_misc_item_debug: mPreferences.deviceProperties().enable_debug( !v.isSelected() );
			}
			
			v.setSelected( !v.isSelected() );
			
			handleEnabledState();
			
		} else {
			new FragmentDialog.Builder(this, "" + v.getId(), (String) ((TextView) v.findViewById(R.id.item_name)).getText()).showSelectorDialog((String) ((String[]) v.getTag())[0], (String) ((String[]) v.getTag())[1], getEnabledValues(v.getId()));
		}
	}
	
	@Override
	public void onDialogSelect(String tag, String value) {
		Integer id = Integer.parseInt(tag);
		ViewGroup view = (ViewGroup) getView().findViewById(id);
		
		switch (id) {
			case R.id.option_memory_item_zram: mPreferences.deviceProperties().set_zram_compression(Integer.parseInt(value)); break;
			case R.id.option_memory_item_swappiness: mPreferences.deviceProperties().set_swap_level(Integer.parseInt(value)); break;
			case R.id.option_filesystem_item_fstype: mPreferences.deviceProperties().set_sdext_fstype(value); break;
			case R.id.option_filesystem_item_journal: mPreferences.deviceProperties().enable_sdext_journal(Integer.parseInt(value)); break;
			case R.id.option_storage_item_cache: mPreferences.deviceProperties().enable_cache(Integer.parseInt(value)); break;
			case R.id.option_immc_item_scheduler: mPreferences.deviceProperties().set_immc_scheduler(value); break;
			case R.id.option_emmc_item_scheduler: mPreferences.deviceProperties().set_emmc_scheduler(value); break;
			case R.id.option_immc_item_readahead: mPreferences.deviceProperties().set_immc_readahead(Integer.parseInt(value)); break;
			case R.id.option_emmc_item_readahead: mPreferences.deviceProperties().set_emmc_readahead(Integer.parseInt(value));
			case R.id.option_system_item_threshold: mPreferences.deviceProperties().set_storage_threshold(Integer.parseInt(value));
		}
		
		((String[]) view.getTag())[1] = value;
		((TextView) view.findViewById(R.id.item_value)).setText( mPreferences.getSelectorValue(((String[]) view.getTag())[0], value) );
		
		handleEnabledState();
	}
	
	private String[] getEnabledValues(Integer id) {
		if (!mEnabledValues.containsKey(id)) {
			switch (id) {
				case R.id.option_immc_item_readahead: 
					mEnabledValues.put(id, new String[]{"4", "8", "16", "32", "64", "128"}); break;
					
				case R.id.option_immc_item_scheduler: 
				case R.id.option_emmc_item_scheduler: 
					RootFW rootfw = Root.open();
					DeviceSetup deviceSetup = mPreferences.deviceSetup();
					String file = id == R.id.option_immc_item_scheduler ? deviceSetup.path_device_scheduler_immc() : deviceSetup.path_device_scheduler_emmc();
					String content = rootfw.file(file).readOneLine();
					String[] parts = null;
					
					if (content != null) {
						parts = content.split(" ");
						
						for (int i=0; i < parts.length; i++) {
							if (parts[i].contains("[")) {
								parts[i] = parts[i].substring(1, parts[i].length()-1);
							}
						}
					}
					
					Root.close();
					
					mEnabledValues.put(id, parts); break;
					
				case R.id.option_filesystem_item_fstype:
					rootfw = Root.open();
					FileData data = rootfw.file("/proc/filesystems").read();
					ArrayList<String> filesystems = new ArrayList<String>();
					
					if (data != null) {
						String[] lines = data.getArray();

						filesystems.add("auto");
						
						for (int i=0; i < lines.length; i++) {
							if (!lines[i].contains("nodev ")) {
								filesystems.add(lines[i].trim());
							}
						}
					}
					
					Root.close();
					
					mEnabledValues.put(id, filesystems.size() > 0 ? filesystems.toArray(new String[filesystems.size()]) : null); break;
					
				default: 
					mEnabledValues.put(id, null);
			}
		}
		
		return mEnabledValues.get(id);
	}
}
