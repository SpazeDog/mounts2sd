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
import com.spazedog.lib.rootfw3.extenders.MemoryExtender.MemStat;
import com.spazedog.mounts2sd.tools.Common;
import com.spazedog.mounts2sd.tools.Preferences;
import com.spazedog.mounts2sd.tools.Root;
import com.spazedog.mounts2sd.tools.Utils;
import com.spazedog.mounts2sd.tools.ViewEventHandler;
import com.spazedog.mounts2sd.tools.ViewEventHandler.ViewClickListener;
import com.spazedog.mounts2sd.tools.interfaces.IDialogConfirmResponse;
import com.spazedog.mounts2sd.tools.interfaces.IDialogCustomLayout;
import com.spazedog.mounts2sd.tools.interfaces.ITabController;

public class FragmentTabConfigure extends Fragment implements IDialogConfirmResponse, IDialogCustomLayout, ViewClickListener {
	
	private ViewGroup mOptionStorageCache;
	private ViewGroup mOptionContentApps;
	private ViewGroup mOptionContentSysApps;
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
	
	private static Map<Integer, String[]> oEnabledSelectorValues = new HashMap<Integer, String[]>();
	private static Double oMemoryUsage;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		mPreferences = Preferences.getInstance((Context) getActivity());
	}
	
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_tab_configure, container, false);
    }
    
	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		
		onHiddenChanged(false);
		
		mOptionContentApps = (ViewGroup) view.findViewById(R.id.option_content_item_apps);
		mOptionContentApps.setOnTouchListener(new ViewEventHandler(this));
		mOptionContentApps.setSelected(mPreferences.deviceProperties.move_apps());
		
		mOptionContentSysApps = (ViewGroup) view.findViewById(R.id.option_content_item_sysapps);
		mOptionContentSysApps.setOnTouchListener(new ViewEventHandler(this));
		mOptionContentSysApps.setSelected(mPreferences.deviceProperties.move_sysapps());
		
		mOptionContentLibs = (ViewGroup) view.findViewById(R.id.option_content_item_libs);
		mOptionContentLibs.setOnTouchListener(new ViewEventHandler(this));
		mOptionContentLibs.setSelected(mPreferences.deviceProperties.move_libs());
		
		mOptionContentData = (ViewGroup) view.findViewById(R.id.option_content_item_data);
		mOptionContentData.setOnTouchListener(new ViewEventHandler(this));
		mOptionContentData.setSelected(mPreferences.deviceProperties.move_data());
		
		mOptionContentDalvik = (ViewGroup) view.findViewById(R.id.option_content_item_dalvik);
		mOptionContentDalvik.setOnTouchListener(new ViewEventHandler(this));
		mOptionContentDalvik.setSelected(mPreferences.deviceProperties.move_dalvik());
		
		mOptionContentSystem = (ViewGroup) view.findViewById(R.id.option_content_item_system);
		mOptionContentSystem.setOnTouchListener(new ViewEventHandler(this));
		mOptionContentSystem.setSelected(mPreferences.deviceProperties.move_system());
		
		mOptionContentMedia = (ViewGroup) view.findViewById(R.id.option_content_item_media);
		mOptionContentMedia.setOnTouchListener(new ViewEventHandler(this));
		mOptionContentMedia.setSelected(mPreferences.deviceProperties.move_media());
		
		mOptionMemorySwap = (ViewGroup) view.findViewById(R.id.option_memory_item_swap);
		mOptionMemorySwap.setOnTouchListener(new ViewEventHandler(this));
		mOptionMemorySwap.setSelected(mPreferences.deviceProperties.enable_swap());
		
		mOptionFilesystemFschk = (ViewGroup) view.findViewById(R.id.option_filesystem_item_fschk);
		mOptionFilesystemFschk.setOnTouchListener(new ViewEventHandler(this));
		mOptionFilesystemFschk.setSelected(mPreferences.deviceProperties.run_sdext_fschk());

		mOptionMiscSafemode = (ViewGroup) view.findViewById(R.id.option_misc_item_safemode);
		mOptionMiscSafemode.setOnTouchListener(new ViewEventHandler(this));
		mOptionMiscSafemode.setSelected(mPreferences.deviceProperties.disable_safemode());
		
		mOptionMiscDebug = (ViewGroup) view.findViewById(R.id.option_misc_item_debug);
		mOptionMiscDebug.setOnTouchListener(new ViewEventHandler(this));
		mOptionMiscDebug.setSelected(mPreferences.deviceProperties.enable_debug());

		mOptionMemoryZram = (ViewGroup) view.findViewById(R.id.option_memory_item_zram);
		mOptionMemoryZram.setOnTouchListener(new ViewEventHandler(this));
		mOptionMemoryZram.setTag( new String[]{"zram", "" + mPreferences.deviceProperties.set_zram_compression()} );
		((TextView) mOptionMemoryZram.findViewById(R.id.item_value)).setText( Utils.getSelectorValue(getActivity(), "zram", "" + mPreferences.deviceProperties.set_zram_compression()) );
		
		mOptionMemorySwappiness = (ViewGroup) view.findViewById(R.id.option_memory_item_swappiness);
		mOptionMemorySwappiness.setOnTouchListener(new ViewEventHandler(this));
		mOptionMemorySwappiness.setTag( new String[]{"swappiness", "" + mPreferences.deviceProperties.set_swap_level()} );
		((TextView) mOptionMemorySwappiness.findViewById(R.id.item_value)).setText( Utils.getSelectorValue(getActivity(), "swappiness", "" + mPreferences.deviceProperties.set_swap_level()) );
		
		mOptionFilesystemFstype = (ViewGroup) view.findViewById(R.id.option_filesystem_item_fstype);
		mOptionFilesystemFstype.setOnTouchListener(new ViewEventHandler(this));
		mOptionFilesystemFstype.setTag( new String[]{"filesystem", "" + mPreferences.deviceProperties.set_sdext_fstype()} );
		((TextView) mOptionFilesystemFstype.findViewById(R.id.item_value)).setText( Utils.getSelectorValue(getActivity(), "filesystem", "" + mPreferences.deviceProperties.set_sdext_fstype()) );
		
		mOptionFilesystemJournal = (ViewGroup) view.findViewById(R.id.option_filesystem_item_journal);
		mOptionFilesystemJournal.setOnTouchListener(new ViewEventHandler(this));
		mOptionFilesystemJournal.setTag( new String[]{"journal", "" + mPreferences.deviceProperties.enable_sdext_journal()} );
		((TextView) mOptionFilesystemJournal.findViewById(R.id.item_value)).setText( Utils.getSelectorValue(getActivity(), "journal", "" + mPreferences.deviceProperties.enable_sdext_journal()) );
		
		mOptionStorageCache = (ViewGroup) view.findViewById(R.id.option_storage_item_cache);
		mOptionStorageCache.setOnTouchListener(new ViewEventHandler(this));
		mOptionStorageCache.setTag( new String[]{"cache", "" + mPreferences.deviceProperties.enable_cache()} );
		((TextView) mOptionStorageCache.findViewById(R.id.item_value)).setText( Utils.getSelectorValue(getActivity(), "cache", "" + mPreferences.deviceProperties.enable_cache()) );

		mOptionImmcScheduler = (ViewGroup) view.findViewById(R.id.option_immc_item_scheduler);
		mOptionImmcScheduler.setOnTouchListener(new ViewEventHandler(this));
		mOptionImmcScheduler.setTag( new String[]{"scheduler", "" + mPreferences.deviceProperties.set_immc_scheduler()} );
		((TextView) mOptionImmcScheduler.findViewById(R.id.item_value)).setText( Utils.getSelectorValue(getActivity(), "scheduler", "" + mPreferences.deviceProperties.set_immc_scheduler()) );
		
		mOptionImmcReadahead = (ViewGroup) view.findViewById(R.id.option_immc_item_readahead);
		mOptionImmcReadahead.setOnTouchListener(new ViewEventHandler(this));
		mOptionImmcReadahead.setTag( new String[]{"readahead", "" + mPreferences.deviceProperties.set_immc_readahead()} );
		((TextView) mOptionImmcReadahead.findViewById(R.id.item_value)).setText( Utils.getSelectorValue(getActivity(), "readahead", "" + mPreferences.deviceProperties.set_immc_readahead()) );

		mOptionEmmcScheduler = (ViewGroup) view.findViewById(R.id.option_emmc_item_scheduler);
		mOptionEmmcScheduler.setOnTouchListener(new ViewEventHandler(this));
		mOptionEmmcScheduler.setTag( new String[]{"scheduler", "" + mPreferences.deviceProperties.set_emmc_scheduler()} );
		((TextView) mOptionEmmcScheduler.findViewById(R.id.item_value)).setText( Utils.getSelectorValue(getActivity(), "scheduler", "" + mPreferences.deviceProperties.set_emmc_scheduler()) );
		
		mOptionEmmcReadahead = (ViewGroup) view.findViewById(R.id.option_emmc_item_readahead);
		mOptionEmmcReadahead.setOnTouchListener(new ViewEventHandler(this));
		mOptionEmmcReadahead.setTag( new String[]{"readahead", "" + mPreferences.deviceProperties.set_emmc_readahead()} );
		((TextView) mOptionEmmcReadahead.findViewById(R.id.item_value)).setText( Utils.getSelectorValue(getActivity(), "readahead", "" + mPreferences.deviceProperties.set_emmc_readahead()) );
		
		mOptionSystemThreshold = (ViewGroup) view.findViewById(R.id.option_system_item_threshold);
		mOptionSystemThreshold.setOnTouchListener(new ViewEventHandler(this));
		mOptionSystemThreshold.setTag( new String[]{"threshold", "" + mPreferences.deviceProperties.set_storage_threshold()} );
		((TextView) mOptionSystemThreshold.findViewById(R.id.item_value)).setText( Utils.getSelectorValue(getActivity(), "threshold", "" + mPreferences.deviceProperties.set_storage_threshold()) );

		handleEnabledState();
	}
	
	@Override
	public void onHiddenChanged(boolean hidden) {
		if (getActivity() != null && !hidden) {
			((ITabController) getActivity()).onTabUpdate();
		}
	}

	@Override
	public View onDialogCreateView(String tag, LayoutInflater inflater, ViewGroup container, final Bundle extra) {
		ViewGroup placeholder = (ViewGroup) inflater.inflate(R.layout.inflate_dialog_placeholder, container, false);
		
		String selectorType = extra.getString("type");
		String selectorValue = extra.getString("value");
		String[] selectorEnabledValues = getEnabledSelectorValues(extra.getInt("viewId"));
		
		Integer selectorNamesId = getResources().getIdentifier("selector_" + selectorType + "_names", "array", getActivity().getPackageName());
		Integer selectorValuesId = getResources().getIdentifier("selector_" + selectorType + "_values", "array", getActivity().getPackageName());
		Integer selectorCommentsId = getResources().getIdentifier("selector_" + selectorType + "_comments", "array", getActivity().getPackageName());
		
		if (selectorNamesId != 0 && selectorValuesId != 0) {
			String[] selectorNames = getResources().getStringArray(selectorNamesId);
			String[] selectorValues = getResources().getStringArray(selectorValuesId);
			String[] selectorComments = selectorCommentsId != 0 ? getResources().getStringArray(selectorCommentsId) : new String[selectorNames.length];
			
			for (int i=0; i < selectorNames.length; i++) {
				ViewGroup itemView = (ViewGroup) inflater.inflate(R.layout.inflate_selector_item, (ViewGroup) placeholder, false);
				Boolean enabled = true;
				
				if (selectorEnabledValues != null) {
					for (int x=0; x < selectorEnabledValues.length; x++) {
						if (selectorEnabledValues[x].equals(selectorValues[i])) {
							enabled = true; break;
						}
						
						enabled = false;
					}
				}
				
				if (selectorType.equals("threshold")) {
					selectorComments[i] = Common.convertPrifix((mPreferences.deviceConfig.size_storage_data() * (Double.parseDouble(selectorValues[i]) / 100)));
					
				} else if (selectorType.equals("zram")) {
					if (oMemoryUsage == null) {
						RootFW rootfw = Root.initiate();
						MemStat memstat = rootfw.memory().getUsage();
						oMemoryUsage = memstat != null ? memstat.memTotal().doubleValue() : 0D;
						Root.release();
					}
					
					selectorComments[i] = Common.convertPrifix((oMemoryUsage * (Double.parseDouble(selectorValues[i]) / 100)));
				}
				
				((TextView) itemView.findViewById(R.id.item_name)).setText(selectorNames[i]);
				
				if (selectorComments[i] != null && !selectorComments[i].equals("")) {
					((TextView) itemView.findViewById(R.id.item_description)).setText(selectorComments[i]);
				}
				
				itemView.setSelected(selectorValues[i].equals(selectorValue));
				itemView.setEnabled(enabled);
				itemView.setTag(selectorValues[i]);
				itemView.setOnTouchListener(new ViewEventHandler(new ViewClickListener(){
					@Override
					public void onViewClick(View v) {
						extra.putString("value", (String) v.getTag());
						
						ViewGroup view = (ViewGroup) v.getParent();
						
						for (int i=0; i < view.getChildCount(); i++) {
							View child = view.getChildAt(i);
							
							if (child == v) {
								child.setSelected(true);
								
							} else {
								child.setSelected(false);
							}
						}
					}
				}));
				
				if (i > 0) {
					inflater.inflate(R.layout.inflate_dialog_divider, placeholder);
				}
				
				placeholder.addView(itemView);
			}
		}
		
		return placeholder;
	}

	@Override
	public void onDialogViewCreated(String tag, View view, Bundle extra) {
		
	}

	@Override
	public void onDialogConfirm(String tag, Boolean confirm, Bundle extra) {
		if (confirm) {
			ViewGroup view = (ViewGroup) getView().findViewById(extra.getInt("viewId"));
			
			switch (extra.getInt("viewId")) {
				case R.id.option_memory_item_zram: mPreferences.deviceProperties.set_zram_compression(Integer.parseInt(extra.getString("value"))); break;
				case R.id.option_memory_item_swappiness: mPreferences.deviceProperties.set_swap_level(Integer.parseInt(extra.getString("value"))); break;
				case R.id.option_filesystem_item_fstype: mPreferences.deviceProperties.set_sdext_fstype(extra.getString("value")); break;
				case R.id.option_filesystem_item_journal: mPreferences.deviceProperties.enable_sdext_journal(Integer.parseInt(extra.getString("value"))); break;
				case R.id.option_storage_item_cache: mPreferences.deviceProperties.enable_cache(Integer.parseInt(extra.getString("value"))); break;
				case R.id.option_immc_item_scheduler: mPreferences.deviceProperties.set_immc_scheduler(extra.getString("value")); break;
				case R.id.option_emmc_item_scheduler: mPreferences.deviceProperties.set_emmc_scheduler(extra.getString("value")); break;
				case R.id.option_immc_item_readahead: mPreferences.deviceProperties.set_immc_readahead(Integer.parseInt(extra.getString("value"))); break;
				case R.id.option_emmc_item_readahead: mPreferences.deviceProperties.set_emmc_readahead(Integer.parseInt(extra.getString("value")));
				case R.id.option_system_item_threshold: mPreferences.deviceProperties.set_storage_threshold(Integer.parseInt(extra.getString("value")));
			}
			
			((String[]) view.getTag())[1] = extra.getString("value");
			((TextView) view.findViewById(R.id.item_value)).setText( Utils.getSelectorValue(getActivity(), ((String[]) view.getTag())[0], extra.getString("value")) );
			
			handleEnabledState();
		}
	}

	@Override
	public void onViewClick(View v) {
		if (v == mOptionContentApps ||
				v == mOptionContentSysApps ||
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
				case R.id.option_content_item_apps: mPreferences.deviceProperties.move_apps( !v.isSelected() ); break;
				case R.id.option_content_item_sysapps: mPreferences.deviceProperties.move_sysapps( !v.isSelected() ); break;
				case R.id.option_content_item_data: mPreferences.deviceProperties.move_data( !v.isSelected() ); break;
				case R.id.option_content_item_dalvik: mPreferences.deviceProperties.move_dalvik( !v.isSelected() ); break;
				case R.id.option_content_item_libs: mPreferences.deviceProperties.move_libs( !v.isSelected() ); break;
				case R.id.option_content_item_media: mPreferences.deviceProperties.move_media( !v.isSelected() ); break;
				case R.id.option_content_item_system: mPreferences.deviceProperties.move_system( !v.isSelected() ); break;
				case R.id.option_memory_item_swap: mPreferences.deviceProperties.enable_swap( !v.isSelected() ); break;
				case R.id.option_filesystem_item_fschk: mPreferences.deviceProperties.run_sdext_fschk( !v.isSelected() ); break;
				case R.id.option_misc_item_safemode: mPreferences.deviceProperties.disable_safemode( !v.isSelected() ); break;
				case R.id.option_misc_item_debug: mPreferences.deviceProperties.enable_debug( !v.isSelected() );
			}
			
			v.setSelected( !v.isSelected() );
			
			handleEnabledState();
			
		} else {
			Bundle extras = new Bundle();
			
			extras.putString("type", (String) ((String[]) v.getTag())[0]);
			extras.putString("value", (String) ((String[]) v.getTag())[1]);
			extras.putInt("viewId", v.getId());
			
			new FragmentDialog.Builder(this, "selector", (String) ((TextView) v.findViewById(R.id.item_name)).getText(), extras).showCustomConfirmDialog();
		}
	}
	
	private void handleEnabledState() {
		Boolean workingScript = mPreferences.deviceSetup.environment_startup_script();
		Boolean workingSdext = workingScript && mPreferences.deviceSetup.path_device_map_sdext() != null;
		Boolean safeMode = "service".equals(mPreferences.deviceSetup.init_implementation()) && !mPreferences.deviceProperties.disable_safemode();
		
		mOptionContentApps.setEnabled(workingSdext);
		mOptionContentData.setEnabled(workingSdext && !safeMode);
		mOptionContentDalvik.setEnabled(workingSdext && !safeMode);
		mOptionContentLibs.setEnabled(workingSdext && !safeMode && mPreferences.deviceSetup.support_directory_library());
		mOptionContentMedia.setEnabled(workingSdext && !safeMode && mPreferences.deviceSetup.support_directory_media());
		mOptionContentSystem.setEnabled(workingSdext && !safeMode && mPreferences.deviceSetup.support_directory_system());
		mOptionMemorySwap.setEnabled(workingSdext && mPreferences.deviceSetup.support_option_swap());
		mOptionMemoryZram.setEnabled(workingScript && mPreferences.deviceSetup.support_option_zram());
		mOptionMemorySwappiness.setEnabled(workingScript && (mPreferences.deviceSetup.support_option_zram() || mPreferences.deviceSetup.support_option_swap()));
		mOptionFilesystemFschk.setEnabled(workingSdext && mPreferences.deviceSetup.support_binary_e2fsck());
		mOptionFilesystemFstype.setEnabled(workingSdext);
		mOptionFilesystemJournal.setEnabled(workingSdext && mPreferences.deviceSetup.support_binary_tune2fs() && "ext4".equals(mPreferences.deviceSetup.type_device_sdext()));
		mOptionMiscSafemode.setEnabled(workingScript && mPreferences.deviceSetup.init_implementation().equals("service"));
		mOptionMiscDebug.setEnabled(workingScript && mPreferences.deviceSetup.environment_startup_script());
		mOptionStorageCache.setEnabled(workingScript);
		mOptionImmcScheduler.setEnabled(workingScript && mPreferences.deviceSetup.path_device_scheduler_immc() != null);
		mOptionImmcReadahead.setEnabled(workingScript && mPreferences.deviceSetup.path_device_readahead_immc() != null);
		mOptionEmmcScheduler.setEnabled(workingSdext && mPreferences.deviceSetup.path_device_scheduler_emmc() != null);
		mOptionEmmcReadahead.setEnabled(workingSdext && mPreferences.deviceSetup.path_device_readahead_emmc() != null);
		mOptionSystemThreshold.setEnabled(workingScript && mPreferences.deviceSetup.support_binary_sqlite3());
	}
	
	private String[] getEnabledSelectorValues(Integer id) {
		if (!oEnabledSelectorValues.containsKey(id)) {
			RootFW rootfw;
			
			switch (id) {
				case R.id.option_immc_item_readahead: 
					oEnabledSelectorValues.put(id, new String[]{"4", "8", "16", "32", "64", "128"}); break;
					
				case R.id.option_immc_item_scheduler: 
				case R.id.option_emmc_item_scheduler: 
					rootfw = Root.initiate();
					String file = id == R.id.option_immc_item_scheduler ? mPreferences.deviceSetup.path_device_scheduler_immc() : mPreferences.deviceSetup.path_device_scheduler_emmc();
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
					
					Root.release();
					
					oEnabledSelectorValues.put(id, parts); break;
					
				case R.id.option_filesystem_item_fstype:
					rootfw = Root.initiate();
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
					
					Root.release();
					
					oEnabledSelectorValues.put(id, filesystems.size() > 0 ? filesystems.toArray(new String[filesystems.size()]) : null); break;
					
				default: 
					oEnabledSelectorValues.put(id, null);
			}
		}
		
		return oEnabledSelectorValues.get(id);
	}
}
