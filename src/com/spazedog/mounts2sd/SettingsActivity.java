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

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.widget.ImageView;
import android.widget.TextView;

import com.spazedog.rootfw.RootFW;

public class SettingsActivity extends Activity {
	
	Map<String, View> BUTTONS = new HashMap<String, View>();
	
	private ProgressDialog progressDialog;
	private LoadConfigsAsync loadAsync;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		BaseApplication.setContext(this);
		
		setContentView(R.layout.activity_settings);
		
		BUTTONS.put("busybox_configuration_layout_a845e349", findViewById(R.id.busybox_configuration_layout_a845e349));
		BUTTONS.put("script_installation_layout_a845e349", findViewById(R.id.script_installation_layout_a845e349));
		
		TextView text;
		
		for(String key : BUTTONS.keySet()) {
			if (key == "busybox_configuration_layout_a845e349") {
				if (!SettingsHelper.hasCompatibleBusybox()) {
					text = (TextView) BUTTONS.get(key).findViewById(R.id.item_description_a845e349);
					text.setText("Busybox is not proper configured and will not work with the startup script. Click here to try configure it properly. Note that your phone might reboot into recovery if installation fails while the device is running!");
					text.setTextColor(getResources().getColor(R.color.red));
				
				}else if (!SettingsHelper.hasConfiguredBusybox() || !SettingsHelper.hasCompatibleBusybox()) {
					((TextView) BUTTONS.get(key).findViewById(R.id.item_description_a845e349)).setText("Busybox is not proper configured. Please click here to configure busybox. Note that your phone might reboot into recovery if installation fails while the device is running!");
					
				} else {
					((TextView) BUTTONS.get(key).findViewById(R.id.item_description_a845e349)).setText("Busybox is fully configured");
					((ImageView) BUTTONS.get(key).findViewById(R.id.item_loader_a845e349)).setImageResource(R.drawable.btn_okay);
					
					BUTTONS.get(key).setEnabled(false);
				}
				
			} else {
				if (!SettingsHelper.hasScript()) {
					text = (TextView) BUTTONS.get(key).findViewById(R.id.item_description_a845e349);
					text.setText("The init.d startup script is either missing or outdated. Click here to install/update the script. Note that your phone might reboot into recovery if installation fails while the device is running!");
					text.setTextColor(getResources().getColor(R.color.red));
					
				} else {
					((TextView) BUTTONS.get(key).findViewById(R.id.item_description_a845e349)).setText("The init.d startup script is installed and up-to-date");;
					((ImageView) BUTTONS.get(key).findViewById(R.id.item_loader_a845e349)).setImageResource(R.drawable.btn_okay);
					
					BUTTONS.get(key).setEnabled(false);
				}
			}
			
			BUTTONS.get(key).setOnTouchListener(new OnTouchListener() {
				public boolean onTouch(View v, MotionEvent event) {
					if(event.getActionMasked() == MotionEvent.ACTION_DOWN) {
						v.setBackgroundColor( getResources().getColor(R.color.light_gray) );
						
					} else if (event.getActionMasked() == MotionEvent.ACTION_UP || event.getActionMasked() == MotionEvent.ACTION_OUTSIDE || event.getActionMasked() == MotionEvent.ACTION_CANCEL) {
						v.setBackgroundDrawable(null);
						
						if (event.getActionMasked() == MotionEvent.ACTION_UP) {
							String id = "";
							Map<String, Boolean> states = new HashMap<String, Boolean>();
							for(String key : BUTTONS.keySet()) {
								states.put(key, BUTTONS.get(key).isEnabled());
								
								BUTTONS.get(key).setEnabled(false);
								
								if (v == BUTTONS.get(key)) {
									id = key;
								}
							}
							
							loadAsync = new LoadConfigsAsync();
							loadAsync.addId(id, states);
							loadAsync.execute("");
						} 
					}
					
					return false;
				}
			});
		}
	}
	
	@Override
	public void onStop() {
		for(String key : BUTTONS.keySet()) {
			BUTTONS.put(key, null);
		}
		
		super.onStop();
	}
	
	@Override
	public void onRestart() {
		View view;
		
		for(String key : BUTTONS.keySet()) {
			view = findViewById( getResources().getIdentifier(key, "id", "com.spazedog.mounts2sd") );
			
			if (view != null) {
				BUTTONS.put(key, view);
			}
		}
		
		super.onRestart();
	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		
		if (loadAsync != null) {
			loadAsync.cancel(true);
		}
	}
	
	public class LoadConfigsAsync extends AsyncTask<String, Void, String> {
		private String ID;
		private Map<String, Boolean> STATES;
		private Bundle BUNDLE;
		
		public LoadConfigsAsync addId(String id, Map<String, Boolean> states) {
			ID = id;
			STATES = states;
			
			return this;
		}
		
		@Override
		protected String doInBackground(String... params) {
			String description = null;
			Integer icon = null, color = null;
			Boolean enabled = null, status = false;
			
			if (ID == "script_installation_layout_a845e349") {
				RootFW rootfw = RootFW.getInstance(getPackageName());
				
				if (rootfw.isConnected()) {
					rootfw.filesystem.remount("/system", "rw");
					
					status = rootfw.filesystem.copyFileResource(getBaseContext(), getResources().getIdentifier("delete_a2sd", "raw", getPackageName()), "/system/bin/delete_a2sd.sh", "0770", "0", "0");
					
					if (status) {
						rootfw.runShell("/system/bin/delete_a2sd.sh");
						
						status = rootfw.filesystem.copyFileResource(getBaseContext(), getResources().getIdentifier("mounts2sd", "raw", getPackageName()), "/system/etc/init.d/10mounts2sd", "0770", "0", "0");
						
						if (status) {
							status = SettingsHelper.checkScript();
							
							if (!status) {
								rootfw.filesystem.rmFile("/system/etc/init.d/10mounts2sd");
								
								status = rootfw.utils.recoveryInstall(getBaseContext(), getResources().getIdentifier( rootfw.filesystem.exist("/proc/mtd") ? "mounts2sd_mtd_zip" : "mounts2sd_ext4_zip" , "raw", getPackageName()));
							}
						}
					}
					
					rootfw.filesystem.remount("/system", "rw");
				}
				
				rootfw.close();
				
				description = status ? 
						"The init.d startup script has been copied successfully to the system partition" : 
						"The init.d startup script could not be copied to the system partition";
				
			} else {
				RootFW rootfw = RootFW.getInstance(getPackageName());
				
				if (status = rootfw.isConnected()) {
					rootfw.filesystem.remount("/system", "rw");
					
					status = rootfw.filesystem.copyFileResource(getBaseContext(), getResources().getIdentifier("busybox_sh", "raw", getPackageName()), "/system/bin/busybox.sh", "0770", "0", "0");
					
					if (status) {
						rootfw.runShell("/system/bin/busybox.sh configure"); 
						status = rootfw.runShell("/system/bin/busybox.sh check").getResultCode() == 0 ? true : false;
						
						if (!status) { 
							status = rootfw.utils.recoveryInstall(getBaseContext(), getResources().getIdentifier( rootfw.filesystem.exist("/proc/mtd") ? "busybox_mtd_zip" : "busybox_ext4_zip" , "raw", getPackageName()));
							
						} else {
							SharedPreferences settings = SettingsActivity.this.getSharedPreferences("prop_configuration", 0x00000000);
							Editor editor = settings.edit();
							editor.putBoolean("check.busybox.configured", true);
							editor.commit();
						}
					} 
					
					rootfw.filesystem.remount("/system", "ro");
				}
				
				rootfw.close();
				
				description = status ? 
						"Busybox was successfully configured" : 
						SettingsHelper.hasCompatibleBusybox() ? "Busybox could not be configured. " : "The current busybox version is outdated or to limited. Please update to a newer one";
			}
			
			icon = status ? R.drawable.btn_okay : R.drawable.btn_refresh;
			color = status ? R.color.dark_gray : R.color.red;
			enabled = status ? false : true;
			
			BUNDLE = new Bundle();
			
			BUNDLE.putString("id", ID);
			BUNDLE.putString("description", description);
			BUNDLE.putInt("icon", icon);
			BUNDLE.putInt("color", color);
			BUNDLE.putBoolean("enabled", enabled);
			BUNDLE.putBoolean("status", status);
			
			// Some devices needs a little time to proper set all values in loadConfigs after execution
			try {
				Thread.sleep(2000);
				
			} catch (InterruptedException e) {}
			
			return null;
		}
		
		@Override
		protected void onPostExecute(String result) {
			SettingsActivity.this.progressDialog.dismiss();
			SettingsActivity.this.progressDialog = null;
			
			TextView text = (TextView) BUTTONS.get( BUNDLE.getString("id") ).findViewById(R.id.item_description_a845e349);
			text.setTextColor(getResources().getColor(BUNDLE.getInt("color")));
			text.setText(BUNDLE.getString("description"));
			
			ImageView image = (ImageView) BUTTONS.get( BUNDLE.getString("id") ).findViewById(R.id.item_loader_a845e349);
			image.setImageResource(BUNDLE.getInt("icon"));
			
			for(String key : BUTTONS.keySet()) {
				BUTTONS.get(key).setEnabled( key != BUNDLE.getString("id") ? STATES.get(key) : BUNDLE.getBoolean("enabled") );
			}
			
			if (BUNDLE.getBoolean("status") && !SettingsHelper.isLoaded()) {
				Intent intent = new Intent(SettingsActivity.this, StatusActivity.class);
				intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				
				SettingsActivity.this.startActivity(intent);
				SettingsActivity.this.finish();
			}
		}
		
		@Override
		protected void onPreExecute() {
			SettingsActivity.this.progressDialog = ProgressDialog.show(SettingsActivity.this, "", 
					ID == "script_installation_layout_a845e349" ? "Installing init.d script" : 
						SettingsHelper.hasCompatibleBusybox() ? "Configuring Busybox" : "Installing Busybox");
		}
	}
}
