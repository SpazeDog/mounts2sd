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

import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.spazedog.lib.rootfw3.RootFW;
import com.spazedog.lib.rootfw3.extenders.FileExtender;
import com.spazedog.lib.taskmanager.Task;
import com.spazedog.mounts2sd.tools.ExtendedActivity;
import com.spazedog.mounts2sd.tools.ExtendedLayout;
import com.spazedog.mounts2sd.tools.ExtendedLayout.OnMeasure;
import com.spazedog.mounts2sd.tools.Preferences;
import com.spazedog.mounts2sd.tools.Root;
import com.spazedog.mounts2sd.tools.Utils;
import com.spazedog.mounts2sd.tools.ViewEventHandler;
import com.spazedog.mounts2sd.tools.ViewEventHandler.ViewClickListener;

public class ActivityAppSettings extends ExtendedActivity implements OnMeasure, ViewClickListener {
	
	private LinearLayout mCheckboxTheme;
	private LinearLayout mCheckboxLayout;
	private LinearLayout mCheckboxDialog;
	private LinearLayout mCheckboxBusybox;
	private LinearLayout mViewSqlite;
	
	private TextView mTextScriptInstalled;
	private TextView mTextScriptBundled;
	private TextView mTextScriptButton;
	
	private Preferences mPreferences;
	
	private ProgressDialog mProgressDialog;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		mPreferences = new Preferences((Context) this);
		
		setTheme( mPreferences.theme() );
		
		super.onCreate(savedInstanceState);
		
		LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		ExtendedLayout layoutOuter = (ExtendedLayout) inflater.inflate(R.layout.activity_app_settings, null);
		
		if (mPreferences.settings().use_tablet_settings() || getResources().getString(R.string.config_screen_type).equals("xlarge")) {
			requestWindowFeature(Window.FEATURE_NO_TITLE);
			
			getWindow().setBackgroundDrawable(new ColorDrawable(0));
			
			layoutOuter.setOnMeasure(this);
			
			setContentView(layoutOuter, new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT));
			
		} else {
			ViewGroup layout = (ViewGroup) ((ViewGroup) layoutOuter.getChildAt(0)).getChildAt(0);
			
			((ViewGroup) layoutOuter.getChildAt(0)).removeView(layout);
			
			setContentView(layout, new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT));
		}
		
		mCheckboxTheme = (LinearLayout) findViewById(R.id.settings_style_item_theme);
		mCheckboxTheme.setOnTouchListener(new ViewEventHandler(this));
		
		mCheckboxLayout = (LinearLayout) findViewById(R.id.settings_style_item_layout);
		mCheckboxLayout.setOnTouchListener(new ViewEventHandler(this));
		
		mCheckboxDialog = (LinearLayout) findViewById(R.id.settings_style_item_dialog);
		if (getResources().getString(R.string.config_screen_type).equals("normal")) {
			mCheckboxDialog.setOnTouchListener(new ViewEventHandler(this));
			
		} else {
			Utils.removeView(mCheckboxDialog, false);
		}
		
		mCheckboxBusybox = (LinearLayout) findViewById(R.id.settings_general_item_busybox);
		if (mPreferences.deviceSetup().environment_multiple_binaries() && mPreferences.isUserOwner()) {
			mCheckboxBusybox.setOnTouchListener(new ViewEventHandler(this));
			
		} else {
			mCheckboxBusybox.setEnabled(false);
		}
		
		mTextScriptButton = (TextView) findViewById(R.id.settings_script_item_button);
		if (mPreferences.isUserOwner()) {
			mTextScriptButton.setOnTouchListener(new ViewEventHandler(this));
			
		} else {
			mTextScriptButton.setEnabled(false);
		}
		
		mViewSqlite = (LinearLayout) findViewById(R.id.settings_general_item_sqlite);
		if (mPreferences.isUserOwner()) {
			mViewSqlite.setOnTouchListener(new ViewEventHandler(this));
			
		} else {
			mViewSqlite.setEnabled(false);
		}
		
		mTextScriptInstalled = (TextView) findViewById(R.id.settings_script_item_installed);
		mTextScriptBundled = (TextView) findViewById(R.id.settings_script_item_bundled);
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		
		if (mPreferences == null) {
			mPreferences = new Preferences((Context) this);
		}
		
		fillContent();
	}
	
	@Override
	protected void onPause() {
		super.onPause();
		
		mPreferences = null;
	}
	
	public void fillContent() {
		mCheckboxTheme.setSelected(mPreferences.settings().use_dark_theme());
		mCheckboxLayout.setSelected(mPreferences.settings().use_global_settings_style());
		mTextScriptBundled.setText(R.string.config_script_version);
		
		if (!getResources().getString(R.string.config_screen_type).equals("xlarge")) {
			mCheckboxDialog.setSelected(mPreferences.settings().use_tablet_settings());
		}
		
		if (mPreferences.deviceSetup().environment_multiple_binaries() && mPreferences.isUserOwner()) {
			mCheckboxBusybox.setSelected( new File( getResources().getString(R.string.config_path_busybox) ).isFile() );
		}
		
		if (mPreferences.deviceSetup().environment_startup_script()) {
			mTextScriptInstalled.setText( mPreferences.deviceSetup() == null || mPreferences.deviceSetup().version_startup_script() == null ? getResources().getString(R.string.status_unknown) : mPreferences.deviceSetup().version_startup_script() );
			
			if (!getResources().getString(R.string.config_script_id).equals( "" + mPreferences.deviceSetup().id_startup_script() )) {
				mTextScriptButton.setText(R.string.settings_btn_script_update);
				
			} else {
				mTextScriptButton.setText(R.string.settings_btn_script_remove);
			}

		} else {
			mTextScriptInstalled.setText("");
			mTextScriptButton.setText(R.string.settings_btn_script_install);
		}
	}

	@Override
	public void spec(View view, Integer height, Integer width) {
		
		((ExtendedLayout) view).removeOnMeasure();
		
		Integer lHeight=null, lWidth=null;
		
		if (getResources().getString(R.string.config_screen_type).equals("xlarge")) {
			lHeight = (int) (height * 0.7);
			lWidth = (int) ((width > height ? height : width) * 0.7);
			
		} else {
			lHeight = (int) (height * 0.9);
			lWidth = (int) (width * 0.9);
		}
		
		((LinearLayout) ((ViewGroup) view).getChildAt(0)).setLayoutParams(new LinearLayout.LayoutParams(lWidth, lHeight));
	}

	@Override
	public void onViewClick(View v) {
		if (v == mCheckboxTheme || 
				v == mCheckboxLayout || 
				v == mCheckboxDialog) {
			
			switch (v.getId()) {
				case R.id.settings_style_item_theme: mPreferences.settings().use_dark_theme( !v.isSelected() ); break;
				case R.id.settings_style_item_layout: mPreferences.settings().use_global_settings_style( !v.isSelected() ); break;
				case R.id.settings_style_item_dialog: mPreferences.settings().use_tablet_settings( !v.isSelected() );
			}
			
			v.setSelected( !v.isSelected() );
			
		} else if (v == mCheckboxBusybox) {
			mPreferences.settings().use_builtin_busybox( !v.isSelected() );
			handleBusybox();
			
		} else if (v == mTextScriptButton) {
			handleScript();
			
		} else if (v == mViewSqlite) {
			handleSqlite();
		}
	}
	
	private void handleBusybox() {
		new Task<Context, Integer, Boolean>(this, "busybox_installer") {
			private String mProgressMessage;
			private String mErrorMessage;
			private String mFilePath;
			
			@Override
			protected void onUIReady() {
				if (mProgressMessage != null) {
					ActivityAppSettings activity = (ActivityAppSettings) getObject();
					
					if (activity.mProgressDialog == null) {
						activity.mProgressDialog = ProgressDialog.show((FragmentActivity) getActivityObject(), "", mProgressMessage + "...");
						
					} else {
						activity.mProgressDialog.setMessage(mProgressMessage + "...");
					}
				}
			}
			
			@Override
			protected Boolean doInBackground(Context... params) {
				mFilePath = ((Context) params[0]).getResources().getString(R.string.config_path_busybox);
				Boolean remove = new File(mFilePath).isFile();
				Boolean status = true;
				RootFW rootfw = Root.open();
				FileExtender.File busyboxFile = rootfw.file(mFilePath);
				
				if (remove) {
					publishProgress(2);
					
					if(!busyboxFile.remove()) {
						mErrorMessage = String.format(((Context) params[0]).getResources().getString(R.string.resource_delete_from_disk_failed), "busybox", mFilePath);
						status = false;
					}
					
				} else {
					publishProgress(1);
					
					if (!busyboxFile.remove() || !rootfw.file(mFilePath).extractFromResource((Context) params[0], "busybox", "0777", "0", "0")) {
						mErrorMessage = String.format(((Context) params[0]).getResources().getString(R.string.resource_copy_to_disk_failed), "busybox", mFilePath);
						status = false;
					}
				}
				
				Root.close();
				
				try {
					Thread.sleep(1000);
					
				} catch (InterruptedException e) {}
				
				return status;
			}
			
			@Override
			protected void onProgressUpdate(Integer... progress) {
				switch((Integer) progress[0]) {
					case 1: mProgressMessage = String.format(getResources().getString(R.string.resource_copying_to_disk), "busybox", mFilePath); break;
					case 2: mProgressMessage = String.format(getResources().getString(R.string.resource_deleting_from_disk), "busybox", mFilePath);
				}
				
				onUIReady();
			}
			
			@Override
			protected void onPostExecute(Boolean result) {
				ActivityAppSettings activity = (ActivityAppSettings) getObject();
				
				if (activity.mProgressDialog != null) {
					activity.mProgressDialog.dismiss();
					activity.mProgressDialog = null;
				}
				
				if (mErrorMessage != null) {
					Toast.makeText(activity, mErrorMessage, Toast.LENGTH_LONG).show();
				}
				
				activity.fillContent();
			}
			
		}.execute(getApplicationContext());
	}
	
	private void handleScript() {
		new Task<Context, Integer, Boolean>(this, "script_installer") {
			private String mProgressMessage;
			private String mFilePath;
			private Boolean mRemove;
			
			@Override
			protected void onUIReady() {
				if (mProgressMessage != null) {
					ActivityAppSettings activity = (ActivityAppSettings) getObject();
					
					if (activity.mProgressDialog == null) {
						activity.mProgressDialog = ProgressDialog.show((FragmentActivity) getActivityObject(), "", mProgressMessage + "...");
						
					} else {
						activity.mProgressDialog.setMessage(mProgressMessage + "...");
					}
				}
			}
			
			@Override
			protected Boolean doInBackground(Context... params) {
				RootFW rootfw = Root.open();
				Preferences pref = new Preferences((Context) params[0]);
				Boolean status = false;
				Boolean remove = mRemove = !pref.deviceSetup().environment_startup_script() ? false : 
					(!getResources().getString(R.string.config_script_id).equals( "" + mPreferences.deviceSetup().id_startup_script() ) ? false : true);
				
				FileExtender.File scriptFile = rootfw.file(mFilePath = ((Context) params[0]).getResources().getString(R.string.config_path_script));
				FileExtender.File runnerFile = rootfw.file(((Context) params[0]).getResources().getString(R.string.config_path_runner));
				
				rootfw.filesystem("/system").addMount(new String[]{"remount", "rw"});
				
				try {
					Thread.sleep(300);
					
				} catch (InterruptedException e) {}
				
				for (int tries=0; tries < 2; tries++) {
					if (remove) {
						publishProgress(2);
						
						if(scriptFile.remove() && runnerFile.remove()) {
							status = true; break;
						}
						
					} else {
						publishProgress(1);
						
						if (!scriptFile.exists()) {
							FileExtender.File cleanupFile = rootfw.file("/data/local/a2sd_cleanup");
							
							if (cleanupFile.remove() && cleanupFile.extractFromResource((Context) params[0], "a2sd_cleanup", "0775", "0", "0")) {
								rootfw.shell( cleanupFile.getAbsolutePath() );
								cleanupFile.remove();
								
							} else {
								continue;
							}
						}
						
						if(scriptFile.remove() && scriptFile.extractFromResource((Context) params[0], "mounts2sd.sh", "0775", "0", "0")) {
							if (runnerFile.remove() && runnerFile.extractFromResource((Context) params[0], "10mounts2sd-runner", "0775", "0", "0")) {
								status = true; break;
								
							} else {
								scriptFile.remove();
							}
						}
					}
				}
				
				rootfw.filesystem("/system").addMount(new String[]{"remount", "ro"});
				
				try {
					Thread.sleep(700);
					
				} catch (InterruptedException e) {}
				
				if (status && remove) {
					Bundle bundle = new Bundle();
					
					bundle.putInt("id_startup_script", -1);
					bundle.putString("version_startup_script", null);
					bundle.putBoolean("version_startup_script", false);
					
					pref.cached("DeviceSetup").putAll(bundle);
					
				} else if (status) {
					Bundle bundle = new Bundle();
					
					String scriptId = scriptFile.readOneMatch("@id");
					String scriptVersion = scriptFile.readOneMatch("@version");
					
					try {
						bundle.putString("version_startup_script", scriptVersion == null ? null : (scriptVersion = scriptVersion.trim()).substring( scriptVersion.lastIndexOf(" ") + 1 ) );
						bundle.putInt("id_startup_script", scriptId == null ? -1 : Integer.valueOf( (scriptId = scriptId.trim()).substring( scriptId.lastIndexOf(" ") + 1 ) ));
						
					} catch (Throwable e) {}
					
					bundle.putBoolean("version_startup_script", true);
					
					pref.cached("DeviceSetup").putAll(bundle);
				}
				
				Root.close();
				
				return status;
			}
			
			@Override
			protected void onProgressUpdate(Integer... progress) {
				switch((Integer) progress[0]) {
					case 1: mProgressMessage = String.format(getResources().getString(R.string.resource_copying_to_disk), "mounts2sd.sh", mFilePath); break;
					case 2: mProgressMessage = String.format(getResources().getString(R.string.resource_deleting_from_disk), "mounts2sd.sh", mFilePath);
				}
				
				onUIReady();
			}
			
			@Override
			protected void onPostExecute(Boolean result) {
				ActivityAppSettings activity = (ActivityAppSettings) getObject();
				
				if (activity.mProgressDialog != null) {
					activity.mProgressDialog.dismiss();
					activity.mProgressDialog = null;
				}
				
				if (!result) {
					String message = String.format(getResources().getString( mRemove ? R.string.resource_delete_from_disk_failed : R.string.resource_copy_to_disk_failed ), "mounts2sd.sh", mFilePath);
					Toast.makeText(activity, message, Toast.LENGTH_LONG).show();
				}
				
				activity.fillContent();
			}
			
		}.execute(getApplicationContext());
	}
	
	private void handleSqlite() {
		new Task<Context, Integer, Boolean>(this, "sqlite_installer") {
			@Override
			protected void onUIReady() {
				ActivityAppSettings activity = (ActivityAppSettings) getObject();
				
				if (activity.mProgressDialog == null) {
					activity.mProgressDialog = ProgressDialog.show((FragmentActivity) getActivityObject(), "", activity.getResources().getString(R.string.progress_install_sqlite) + "...");
				}
			}
			
			@Override
			protected Boolean doInBackground(Context... params) {
				RootFW rootfw = Root.open();
				Boolean status = false;
				
				String[] resources = new String[] {"sqlite3", "libsqlite.so", "libsqlite_jni.so"};
				String[] files = new String[] {"/system/xbin/sqlite3", "/system/lib/libsqlite.so", "/system/lib/libsqlite_jni.so"};
				
				rootfw.filesystem("/system").addMount(new String[]{"remount", "rw"});
				
				try {
					Thread.sleep(300);
					
				} catch (InterruptedException e) {}
				
				for (int i=0; i < resources.length; i++) {
					FileExtender.File file = rootfw.file(files[i]);
					
					if (!(status = ( file.remove() && file.extractFromResource((Context) params[0], resources[i], "0777", "0", "0") ))) {
						break;
					}
				}
				
				rootfw.filesystem("/system").addMount(new String[]{"remount", "ro"});
				
				Root.close();
				
				try {
					Thread.sleep(700);
					
				} catch (InterruptedException e) {}
				
				if (status) {
					new Preferences((Context) params[0]).cached("DeviceSetup").putBoolean("support_binary_sqlite3", true);
				}
				
				return status;
			}
			
			@Override
			protected void onPostExecute(Boolean result) {
				ActivityAppSettings activity = (ActivityAppSettings) getObject();
				
				if (activity.mProgressDialog != null) {
					activity.mProgressDialog.dismiss();
					activity.mProgressDialog = null;
				}
				
				if (!result) {
					Toast.makeText(activity, activity.getResources().getString(R.string.progress_install_sqlite_failed), Toast.LENGTH_LONG).show();
				}
			}
			
		}.execute(getApplicationContext());
	}
}
