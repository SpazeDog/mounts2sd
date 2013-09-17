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
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.spazedog.lib.rootfw3.RootFW;
import com.spazedog.lib.rootfw3.extenders.FileExtender;
import com.spazedog.lib.rootfw3.extenders.PackageExtender.PackageDetails;
import com.spazedog.lib.taskmanager.Task;
import com.spazedog.mounts2sd.tools.Root;
import com.spazedog.mounts2sd.tools.ViewEventHandler;
import com.spazedog.mounts2sd.tools.ViewEventHandler.ViewClickListener;
import com.spazedog.mounts2sd.tools.interfaces.IDialogConfirmResponse;
import com.spazedog.mounts2sd.tools.interfaces.ITabController;

public class FragmentTabAppManager extends Fragment implements IDialogConfirmResponse, ViewClickListener {
	
	private static SortedMap<String, PackageDetails> oPackages = new TreeMap<String, PackageDetails>();
	
	private Boolean mAssamblingList = false;
	
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    	super.onCreateView(inflater, container, savedInstanceState);
    	
        return inflater.inflate(R.layout.fragment_tab_appmanager, container, false);
    }
    
	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		
		onHiddenChanged(false);
		
		assembleApplicationList();
	}
	
	@Override
	public void onHiddenChanged(boolean hidden) {
		super.onHiddenChanged(hidden);
		
		if (getActivity() != null && !hidden) {
			((ITabController) getActivity()).onTabUpdate();
		}
	}

	@Override
	public void onViewClick(View v) {
		String message = (Boolean) oPackages.get(v.getTag()).getObject("isSystem") ? 
				getResources().getString(R.string.manager_app_unregister_message) : 
					getResources().getString(R.string.manager_app_register_message);
				
		Bundle extra = new Bundle();
		extra.putString("application", (String) v.getTag());
		
		new FragmentDialog.Builder(this, "app_selector", "Register System Application", extra).showConfirmDialog(message + "\n\n" + getResources().getString(R.string.manager_app_conversion_notice));
	}

	@Override
	public void onDialogConfirm(String tag, Boolean confirm, Bundle extra) {
		if (confirm) {
			moveApplication( extra.getString("application") );
		}
	}
	
	private void moveApplication(final String application) {
		new Task<Context, Void, Boolean>(this, "assembleApplicationList") {
			@Override
			protected void onPreExecute() {
				setProgressMessage( getResources().getString(R.string.manager_app_conversion_loader) + "..." );
			}
			
			@Override
			protected Boolean doInBackground(Context... params) {
				Boolean status = false;
				RootFW root = Root.initiate();
				PackageDetails packageDetails = oPackages.get(application);
				FileExtender.File packageFile = root.file(
					packageDetails.isUpdate() ? packageDetails.updatedPackage().path() : 
							packageDetails.path()
				);
				
				if (packageFile.exists()) {
					root.filesystem("/system").addMount(new String[]{"remount", "rw"});
					
					try {
						Thread.sleep(300);
						
					} catch (InterruptedException e) {}
					
					if ((Boolean) packageDetails.getObject("isSystem") && packageDetails.isUpdate()) {
						status = packageFile.openCanonical().remove() && packageFile.remove();
						
					} else if ((Boolean) packageDetails.getObject("isSystem")) {
						status = packageFile.openCanonical().move("/data/app/" + packageFile.getName()) && packageFile.remove();
						
					} else {
						status = packageFile.move("/data/app-system/" + packageFile.getName()) && packageFile.openNew("/data/app-system/" + packageFile.getName()).createLink("/system/app/" + packageFile.getName());
					}
					
					root.filesystem("/system").addMount(new String[]{"remount", "ro"});
					
					try {
						Thread.sleep(700);
						
					} catch (InterruptedException e) {}
				}
				
				Root.release();
				
				return status;
			}
			
			@Override
			protected void onPostExecute(Boolean result) {
				FragmentTabAppManager fragment = (FragmentTabAppManager) getObject();
				
				if (result) {
					Toast.makeText(getActivity(), getResources().getString(R.string.manager_app_converted_reboot), Toast.LENGTH_LONG).show();
					View view = fragment.getView().findViewWithTag(application);
					
					if (view != null) {
						((ViewGroup) view.getParent()).removeView(view);
					}
					
				} else {
					Toast.makeText(getActivity(), getResources().getString(R.string.manager_app_conversion_failed), Toast.LENGTH_LONG).show();
				}
			}
			
		}.execute(getActivity().getApplicationContext());
	}
	
	private void assembleApplicationList() {
		if (!mAssamblingList && oPackages.size() == 0) {
			new Task<Context, Void, PackageDetails[]>(this, "assembleApplicationList") {
				@Override
				protected void onPreExecute() {
					setProgressMessage( getResources().getString(R.string.manager_app_list_loader) + "..." );
				}
				
				@Override
				protected PackageDetails[] doInBackground(Context... params) {
					Context context = params[0];
					RootFW rootfw = Root.initiate();
					PackageDetails[] packages = rootfw.packages().getPackageList();
					List<PackageDetails> assambled = new ArrayList<PackageDetails>();
					String thisPackage = context.getPackageName();
					
					if (packages != null) {
						for (int i=0; i < packages.length; i++) {
							if (!thisPackage.equals(packages[i].name())) {
								if ((packages[i].path().startsWith("/data/app/") && (!packages[i].isUpdate() || ("" + (rootfw.file(packages[i].updatedPackage().path())).getCanonicalPath()).startsWith("/data/app-system/"))) || 
										(packages[i].path().startsWith("/system/app/") && ("" + (rootfw.file(packages[i].path())).getCanonicalPath()).startsWith("/data/app-system/"))) {
	
									assambled.add(packages[i]);
								}
							}
						}
					}
					
					Root.release();
					
					return assambled.toArray( new PackageDetails[ assambled.size() ] );
				}
				
				@Override
				protected void onPostExecute(PackageDetails[] result) {
					FragmentTabAppManager fragment = (FragmentTabAppManager) getObject();
					PackageManager packageManager = getActivity().getPackageManager();
					
					for (int i=0; i < result.length; i++) {
						try {
							PackageInfo packageInfo = packageManager.getPackageInfo(result[i].name(), PackageManager.GET_META_DATA);
							
							if (packageInfo.applicationInfo.enabled) {
								result[i].putObject("icon", packageInfo.applicationInfo.loadIcon(packageManager));
								result[i].putObject("label", packageInfo.applicationInfo.loadLabel(packageManager).toString());
								result[i].putObject("isSystem", Boolean.valueOf( result[i].isUpdate() || result[i].path().startsWith("/system/") ));
								
								oPackages.put(result[i].name(), result[i]);
							}
							
						} catch (NameNotFoundException e) {}
					}
					
					fragment.buildApplicationList();
				}
				
			}.execute(getActivity().getApplicationContext());
			
		} else if (!mAssamblingList) {
			buildApplicationList();
		}
	}
	
	private void buildApplicationList() {
		LayoutInflater inflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		Boolean divider = false;
		ViewGroup container = (ViewGroup) ((ViewGroup) getView()).getChildAt(0);
		
		for (String key : oPackages.keySet()) {
			ViewGroup itemView = (ViewGroup) inflater.inflate(R.layout.inflate_appmanager_item, container, false);
			
			((ImageView) itemView.findViewById(R.id.item_package_icon)).setImageDrawable((Drawable) oPackages.get(key).getObject("icon"));
			((TextView) itemView.findViewById(R.id.item_package_label)).setText((String) oPackages.get(key).getObject("label"));
			((TextView) itemView.findViewById(R.id.item_package_name)).setText(oPackages.get(key).name());
			
			((ImageView) itemView.findViewById(R.id.item_package_type)).setImageResource(
					(Boolean) oPackages.get(key).getObject("isSystem") ? 
							R.drawable.app_manager_system : R.drawable.app_manager_regular
			);
			
			itemView.setSelected((Boolean) oPackages.get(key).getObject("isSystem"));
			itemView.setTag(oPackages.get(key).name());
			
			if (divider) {
				inflater.inflate(R.layout.inflate_dialog_divider, container);
				
			} else {
				divider = true;
			}
			
			itemView.setOnTouchListener(new ViewEventHandler(this));
			
			container.addView(itemView);
		}
	}
}
