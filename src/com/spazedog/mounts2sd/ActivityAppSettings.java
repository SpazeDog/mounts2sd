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
import com.spazedog.lib.taskmanager.Task;
import com.spazedog.mounts2sd.tools.ExtendedActivity;
import com.spazedog.mounts2sd.tools.ExtendedLayout;
import com.spazedog.mounts2sd.tools.ExtendedLayout.OnMeasure;
import com.spazedog.mounts2sd.tools.Preferences;
import com.spazedog.mounts2sd.tools.Root;
import com.spazedog.mounts2sd.tools.Utils;
import com.spazedog.mounts2sd.tools.ViewEventHandler;
import com.spazedog.mounts2sd.tools.ViewEventHandler.ViewClickListener;
import com.spazedog.mounts2sd.tools.interfaces.DialogConfirmResponse;
import com.spazedog.mounts2sd.tools.interfaces.DialogListener;

public class ActivityAppSettings extends ExtendedActivity implements OnMeasure, ViewClickListener, DialogListener, DialogConfirmResponse {
	
	private static String REQUEST_LOADER_SCRIPT = "script";
	private static String REQUEST_LOADER_SQLITE = "sqlite";
	private static String REQUEST_LOADER_BUSYBOX = "busybox";
	
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
			mCheckboxBusybox.setSelected(mPreferences.deviceSetup() != null && mPreferences.deviceSetup().environment_busybox_internal());
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
			requestLoader(REQUEST_LOADER_BUSYBOX);
			
		} else if (v == mTextScriptButton) {
			requestLoader(REQUEST_LOADER_SCRIPT);
			
		} else if (v == mViewSqlite) {
			requestLoader(REQUEST_LOADER_SQLITE);
		}
	}

	public void requestLoader(String loader) {
		if (!mPreferences.deviceSetup().environment_secure_flag_off()) {
			if (!loader.equals(REQUEST_LOADER_BUSYBOX)) {
				new FragmentDialog.Builder(this, loader, "S-On Protection").showConfirmDialog("Your device is S-On protected which means that the appliction cannot write to the system partition from within Android.\n\nDo you want to launch the recovery fallback handler?");
				
				return;
			}
		}
		
		onDialogConfirm(loader, true);
	}
	
	@Override
	public void onDialogConfirm(final String loader, final Boolean confirm) {
		if (confirm) {
			new Task<Context, String, Boolean>(this, "settings_loader") {
				private String mProgressMessage;
				private String mErrorMessage;
				
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
				protected void onProgressUpdate(String... progressMessage) {
					mProgressMessage = progressMessage[0];
					onUIReady();
				}
				
				@Override
				protected Boolean doInBackground(Context... params) {
					Preferences preferences = new Preferences((Context) params[0]);
					RootFW rootfw = Root.open();
					Boolean status = false;
					Boolean remount = !loader.equals(REQUEST_LOADER_BUSYBOX);
					Boolean remove = false;
					String[] resources = null, files = null;
					
					if (loader.equals(REQUEST_LOADER_BUSYBOX) && preferences.deviceSetup().environment_busybox_internal()) {
						remove = true;
						
					} else if (loader.equals(REQUEST_LOADER_SCRIPT) && preferences.deviceSetup().environment_startup_script() && getResources().getString(R.string.config_script_id).equals( "" + mPreferences.deviceSetup().id_startup_script() )) {
						remove = true;
					}
									
					if (!loader.equals(REQUEST_LOADER_BUSYBOX) && !preferences.deviceSetup().environment_secure_flag_off()) {
						String dataLocation = preferences.deviceConfig().location_storage_data();
						
						if (!loader.equals(REQUEST_LOADER_SCRIPT) || !remove) {
							resources = loader.equals(REQUEST_LOADER_SQLITE) ? 
									new String[] {"sqlite3", "libsqlite.so", "libsqlite_jni.so"} : 
									new String[] {"mounts2sd.sh", "10mounts2sd-runner", "a2sd_cleanup"};
							
							files = loader.equals(REQUEST_LOADER_SQLITE) ? 
									new String[] {dataLocation + "/local/sqlite3", dataLocation + "/local/libsqlite.so", dataLocation + "/local/libsqlite_jni.so"} : 
									new String[] {dataLocation + "/local/mounts2sd.sh", dataLocation + "/local/10mounts2sd-runner", dataLocation + "/local/a2sd_cleanup"};
						}
								
						_WRAPPER_:
						while (true) {
							if (resources != null) {
								for (int i=0; i < resources.length; i++) {
									publishProgress( String.format(getResources().getString(R.string.resource_copying_to_disk), resources[i], files[i]) );
									
									Utils.wait(750);
									
									if (!rootfw.file(files[i]).extractFromResource((Context) params[0], resources[i])) {
										mErrorMessage = String.format(getResources().getString(R.string.resource_copy_to_disk_failed), resources[i], files[i]); break _WRAPPER_;
									}
								}
							}
							
							publishProgress( String.format(getResources().getString(R.string.resource_copying_to_disk), "fallback.zip", dataLocation + "/local/fallback.zip") );
							
							Utils.wait(750);
							
							if (!rootfw.file(dataLocation + "/local/fallback.zip").extractFromResource((Context) params[0], "fallback.zip")) {
								mErrorMessage = String.format(getResources().getString(R.string.resource_copy_to_disk_failed), "fallback.zip", dataLocation + "/local/fallback.zip"); break _WRAPPER_;
							}
							
							/* If data is not mounted at /data, we need an empty temp file to trick runInRecovery()
							 * into running our file. It will not run a file that does not exist, and since we make sure to extract the resources into the
							 * real data partition, this location will be correct from within the recovery
							 */
							rootfw.file("/data/local").createDirectory();
							rootfw.file("/data/local/fallback.zip").createFile();
							rootfw.file("/data/local/fallback.zip").runInRecovery(loader, (remove ? "remove" : "install"));
							
							/*
							 * This will only be displayed if the device did not reboot
							 */
							mErrorMessage = getResources().getString(R.string.resource_fallback_failed);
							
							break;
						}
						
					} else {
						resources = loader.equals(REQUEST_LOADER_SQLITE) ? 
								new String[] {"sqlite3", "libsqlite.so", "libsqlite_jni.so"} : 
									loader.equals(REQUEST_LOADER_BUSYBOX) ? 
											new String[] {"busybox"} : 
											new String[] {"mounts2sd.sh", "10mounts2sd-runner"};
											
						files = loader.equals(REQUEST_LOADER_SQLITE) ? 
								new String[] {"/system/xbin/sqlite3", "/system/lib/libsqlite.so", "/system/lib/libsqlite_jni.so"} : 
									loader.equals(REQUEST_LOADER_BUSYBOX) ? 
											new String[] {"/data/local/busybox"} : 
											new String[] {"/system/etc/mounts2sd.sh", "/system/etc/init.d/10mounts2sd-runner"};
							
						if (remount) {
							rootfw.filesystem("/system").addMount(new String[]{"remount", "rw"});
						
							publishProgress( String.format(getResources().getString(R.string.resource_running_script), "a2sd_cleanup") );
							
							Utils.wait(750);
							
							if (loader.equals(REQUEST_LOADER_SCRIPT) && !preferences.deviceSetup().environment_startup_script()) {
								rootfw.file().runFromResource((Context) params[0], "a2sd_cleanup");
							}
						}

						_WRAPPER_:
						while (true) {
							for (int i=0; i < resources.length; i++) {
								if (remove) {
									publishProgress( String.format(getResources().getString(R.string.resource_deleting_from_disk), resources[i], files[i]) );
									
									Utils.wait(750);
									
									if (!rootfw.file(files[i]).remove()) {
										mErrorMessage = String.format(getResources().getString(R.string.resource_delete_from_disk_failed), resources[i], files[i]); break _WRAPPER_;
									}
									
								} else {
									String perm = loader.equals(REQUEST_LOADER_SQLITE) ? (i > 0 ? "0644" : "0755") : "0775";
									String group = loader.equals(REQUEST_LOADER_SQLITE) && i == 0 ? "2000" : "0";
									
									publishProgress( String.format(getResources().getString(R.string.resource_copying_to_disk), resources[i], files[i]) );
									
									Utils.wait(750);
									
									if (!rootfw.file(files[i]).extractFromResource((Context) params[0], resources[i], perm, "0", group)) {
										mErrorMessage = String.format(getResources().getString(R.string.resource_copy_to_disk_failed), resources[i], files[i]); break _WRAPPER_;
									}
								}
							}
							
							if (loader.equals(REQUEST_LOADER_SCRIPT)) {
								Bundle bundle = new Bundle();
								
								String scriptId = remove ? null : rootfw.file("/system/etc/mounts2sd.sh").readOneMatch("@id");
								String scriptVersion = remove ? null : rootfw.file("/system/etc/mounts2sd.sh").readOneMatch("@version");
								
								try {
									bundle.putBoolean("environment_startup_script", !remove);
									bundle.putString("version_startup_script", scriptVersion == null ? null : (scriptVersion = scriptVersion.trim()).substring( scriptVersion.lastIndexOf(" ") + 1 ) );
									bundle.putInt("id_startup_script", scriptId == null ? -1 : Integer.parseInt( (scriptId = scriptId.trim()).substring( scriptId.lastIndexOf(" ") + 1 ) ));
								
								} catch (Throwable e) {}
									
								preferences.cached("DeviceSetup").putAll(bundle);
								
							} else if (loader.equals(REQUEST_LOADER_BUSYBOX)) {
								preferences.cached("DeviceSetup").putBoolean("environment_busybox_internal", !remove);
							}
							
							status = true; break;
						}
						
						if (remount) {
							rootfw.filesystem("/system").addMount(new String[]{"remount", "ro"});
						}
					}
					
					Root.close();
					
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
						Toast.makeText(activity, mErrorMessage, Toast.LENGTH_LONG).show();
						
					} else {
						activity.fillContent();
					}
				}
				
			}.execute(getApplicationContext());
		}
	}
}
