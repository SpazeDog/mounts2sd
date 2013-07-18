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
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.TextView;

import com.spazedog.lib.taskmanager.Daemon;
import com.spazedog.lib.taskmanager.Task;
import com.spazedog.mounts2sd.tools.ExtendedActivity;
import com.spazedog.mounts2sd.tools.Preferences;
import com.spazedog.mounts2sd.tools.Shell;
import com.spazedog.mounts2sd.tools.Utils;
import com.spazedog.mounts2sd.tools.containers.DeviceSetup;
import com.spazedog.mounts2sd.tools.containers.MessageItem;
import com.spazedog.mounts2sd.tools.interfaces.DialogListener;
import com.spazedog.mounts2sd.tools.interfaces.DialogMessageResponse;
import com.spazedog.mounts2sd.tools.interfaces.TabController;

public class ActivityTabController extends ExtendedActivity implements OnClickListener, DialogListener, DialogMessageResponse, TabController {

	private ProgressDialog mProgressDialog;

	private Boolean mAsyncProcessing = false;
	private Boolean mAsyncFinished = false;
	private Boolean mAsyncResult = false;

	private Integer mCurFragment = null;

	private Map<Integer, Fragment> mFragments = new HashMap<Integer, Fragment>(); {
		mFragments.put(R.id.tab_fragment_log, null);
		mFragments.put(R.id.tab_fragment_configure, null);
		mFragments.put( (mCurFragment = R.id.tab_fragment_overview), null);
	}

	private Map<String, MessageItem> mInfoBoxes = new HashMap<String, MessageItem>();
	
	private Preferences mPreferences;
	
	private Boolean mDaemonCreated = false;
	
	private Boolean mBackPressed = false;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		mPreferences = new Preferences((Context) this);
		
		setTheme( mPreferences.theme() );
		
		super.onCreate(savedInstanceState);
		
		if (savedInstanceState != null) {
			mCurFragment = savedInstanceState.getInt("mCurFragment");
			mAsyncProcessing = savedInstanceState.getBoolean("mAsyncProcessing");
			mAsyncFinished = savedInstanceState.getBoolean("mAsyncFinished");
			mAsyncResult = savedInstanceState.getBoolean("mAsyncResult");
			mDaemonCreated = savedInstanceState.getBoolean("mDaemonCreated");
		}
		
		setContentView(R.layout.activity_tab_controller);
		
		for (int key : mFragments.keySet()) {
			Fragment fragment = getSupportFragmentManager().findFragmentByTag(""+key);
			
			if (fragment == null) {
				switch (key) {
					case R.id.tab_fragment_log: fragment = new FragmentTabLog(); break;
					case R.id.tab_fragment_configure: fragment = new FragmentTabConfigure(); break;
					case R.id.tab_fragment_overview: fragment = new FragmentTabOverview();
				}
			}
			
			mFragments.put(key, fragment);
		}
		
		for (int id : mFragments.keySet()) {
			findViewById(id).setOnClickListener(this);
		}
		
		if (!mDaemonCreated) {
			try {
				mDaemonCreated = true;
				
				new Daemon<Void, Void>(this, "InfoBox") {
					@Override
					protected void doInBackground(Void... params) {
						if (Utils.Relay.Message.pending()) {
							sendToReceiver(null);
						}
					}
					
					@Override
					protected void receiver(Void result) {
						if (getActivityObject() != null) {
							((ActivityTabController) getActivityObject()).handleMessageRelay();
						}
					}
					
				}.setTimeout(1000).start();
				
			} catch (IllegalStateException e) {}
		}
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		
		if (mPreferences == null) {
			mPreferences = new Preferences((Context) this);
		}
		
		startLoader();
	}
	
	@Override
	protected void onPause() {
		super.onPause();
		
		mPreferences = null;
	}
	
	@Override
	public void onSaveInstanceState(Bundle savedInstanceState) {
		savedInstanceState.putInt("mCurFragment", mCurFragment);
		savedInstanceState.putBoolean("mAsyncProcessing", mAsyncProcessing);
		savedInstanceState.putBoolean("mAsyncFinished", mAsyncFinished);
		savedInstanceState.putBoolean("mAsyncResult", mAsyncResult);
		savedInstanceState.putBoolean("mDaemonCreated", mDaemonCreated);

		super.onSaveInstanceState(savedInstanceState);
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.activity_tab_controller, menu);
		
		return true;
	}
	
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()) {
            case R.id.menu_controller_app_settings:
    			Intent intent = new Intent(this, ActivityAppSettings.class);
    			intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
    			intent.setFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
    			
    			startActivity(intent);
    			
                return true;

            default:
            return super.onOptionsItemSelected(item);
        }
    }

	private void startLoader() {
		if (!mAsyncProcessing && !mAsyncFinished) {
			mAsyncProcessing = true;
			
			new Task<Context, Integer, Boolean>(this, "TabController") {
				private String mProgressMessage;
				
				@Override
				protected void onUIReady() {
					if (mProgressMessage != null) {
						ActivityTabController activity = (ActivityTabController) getObject();
						
						if (activity.mProgressDialog == null) {
							activity.mProgressDialog = ProgressDialog.show((FragmentActivity) getActivityObject(), "", mProgressMessage + "...");
							
						} else {
							activity.mProgressDialog.setMessage(mProgressMessage + "...");
						}
					}
				}
				
				@Override
				protected Boolean doInBackground(Context... params) {
					if (Shell.connection.connected(true)) {
						Preferences preferences = new Preferences( (Context) params[0] );
						
						if (!preferences.checkDeviceSetup() || !preferences.checkDeviceConfig() || !preferences.checkDeviceProperties()) {
							publishProgress(1);

							if (preferences.loadDeviceSetup(false)) {
								publishProgress(2);
								
								if (preferences.loadDeviceConfig(false)) {
									publishProgress(3);
									
									if (preferences.loadDeviceProperties(false)) {
										return true;
									}
								}
							}
							
						} else {
							return true;
						}
					}
					
					return false;
				}
				
				@Override
				protected void onProgressUpdate(Integer... progress) {
					switch((Integer) progress[0]) {
						case 1: mProgressMessage = getResources().getString(R.string.progress_load_setup); break;
						case 2: mProgressMessage = getResources().getString(R.string.progress_load_config); break;
						case 3: mProgressMessage = getResources().getString(R.string.progress_load_properties);
					}
					
					onUIReady();
				}
				
				@Override
				protected void onPostExecute(Boolean result) {
					ActivityTabController activity = (ActivityTabController) getObject();
					
					if (activity.mProgressDialog != null) {
						activity.mProgressDialog.dismiss();
						activity.mProgressDialog = null;
					}
					
					activity.mAsyncProcessing = false;
					activity.mAsyncFinished = true;
					activity.mAsyncResult = result;
					
					activity.stopLoader();
				}
				
			}.execute(getApplicationContext());
			
		} else if (mAsyncFinished) {
			stopLoader();
		}
	}
	
	private void stopLoader() {
		DeviceSetup deviceSetup = mPreferences.deviceSetup();
		
		if (!mAsyncResult) {
			if (isForeground() && getSupportFragmentManager().findFragmentByTag("TabControllerDialog") == null) {
				if (!Shell.connection.connected()) {
					new FragmentDialog.Builder(this, "TabControllerDialog", getResources().getString(R.string.message_error_su_headline)).showMessageDialog(getResources().getString(R.string.message_error_su_text), true);
					
				} else if (!deviceSetup.environment_busybox()) {
					new FragmentDialog.Builder(this, "TabControllerDialog", getResources().getString(R.string.message_error_busybox_headline)).showMessageDialog(getResources().getString(R.string.message_error_busybox_text), true);
					
				} else {
					new FragmentDialog.Builder(this, "TabControllerDialog", getResources().getString(R.string.message_error_unknown_headline)).showMessageDialog(getResources().getString(R.string.message_error_unknown_text), true);
				}
			}
			
		} else {
			switchTabFragment(mCurFragment);
			
			if (deviceSetup.safemode()) {
				Utils.Relay.Message.add(new MessageItem("save-mode", getResources().getString(R.string.infobox_safemode)) {
					@Override
					public Boolean onVisibilityChange(Context context, Integer tabId, Boolean visible) {
						return tabId != R.id.tab_fragment_log;
					}
				});
			}
			
			if (deviceSetup.path_device_map_sdext() == null) {
				Utils.Relay.Message.add(new MessageItem("no-sdext", getResources().getString(R.string.infobox_no_sdext)) {
					@Override
					public Boolean onVisibilityChange(Context context, Integer tabId, Boolean visible) {
						return tabId == R.id.tab_fragment_configure;
					}
				});
			}
			
			if (!deviceSetup.environment_startup_script()) {
				Utils.Relay.Message.add(new MessageItem("no-script", getResources().getString(R.string.infobox_no_script)) {
					@Override
					public Boolean onVisibilityChange(Context context, Integer tabId, Boolean visible) {
						if ((new Preferences(context)).deviceSetup().environment_startup_script()) {
							Utils.Relay.Message.remove("no-script"); return false;
						}
						
						return tabId == R.id.tab_fragment_configure;
					}
				});
				
			} else if (!getResources().getString(R.string.config_script_id).equals( "" + mPreferences.deviceSetup().id_startup_script() )) {
				Utils.Relay.Message.add(new MessageItem("update-script", getResources().getString(R.string.infobox_update_script)) {
					@Override
					public Boolean onVisibilityChange(Context context, Integer tabId, Boolean visible) {
						DeviceSetup setup = new Preferences(context).deviceSetup();
						
						if (setup.environment_startup_script() && context.getResources().getString(R.string.config_script_id).equals( "" + setup.id_startup_script() )) {
							Utils.Relay.Message.remove("update-script"); return false;
						}
						
						return tabId != R.id.tab_fragment_log;
					}
				});
			}
		}
	}

	private void switchTabFragment(Integer aTabId) {
		FragmentManager fragmentManager = getSupportFragmentManager();
		Boolean isAdded = fragmentManager.findFragmentByTag(""+aTabId) != null;
		
		if (!isAdded || mFragments.get(aTabId).isDetached()) {
			FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
			
			for (int id : mFragments.keySet()) {
				if (id != aTabId && fragmentManager.findFragmentByTag(""+id) != null && !mFragments.get(id).isDetached()) {
					fragmentTransaction.detach( mFragments.get(id) );
				}
			} 
			
			if (isAdded && mFragments.get(aTabId).isDetached()) {
				fragmentTransaction.attach(mFragments.get(aTabId));
				
			} else if (!isAdded) {
				fragmentTransaction.add(R.id.fragment_frame, mFragments.get(aTabId), ""+aTabId);
			}
			
			fragmentTransaction.commit();
			
		} else {
			handleMessageVisibility();
		}
	}
	
	private void updateTabState() {
		for (int id : mFragments.keySet()) {
			if (getSupportFragmentManager().findFragmentByTag(""+id) != null && !mFragments.get(id).isDetached()) {
				mCurFragment = id;
				
				findViewById(id).setSelected(true);
				
			} else {
				findViewById(id).setSelected(false);
			}
		}
		
		handleMessageVisibility();
	}

	@Override
	public void onClick(View v) {
		switchTabFragment( v.getId() );
	}
	
	@Override
	public void onDialogClose(String tag, Boolean exit) {
		if (exit) {
			onBackPressed();
		}
	}
	
	private void handleMessageRelay() {
		if (mPreferences != null) {
			MessageItem item;
			String actionTag;
			
			ViewGroup container = (ViewGroup) findViewById(R.id.placeholder);
			
			while ((item = Utils.Relay.Message.nextMessage()) != null) {
				if (!mInfoBoxes.containsKey(item.tag())) {
					TextView view = (TextView) getLayoutInflater().inflate(R.layout.inflate_info_box, container, false);
					view.setText(item.message());
					view.setTag(item.tag());
					
					container.addView(view);
					
					mInfoBoxes.put(item.tag(), item);
				}
			}
			
			while ((actionTag = Utils.Relay.Message.nextAction()) != null) {
				if (mInfoBoxes.containsKey(actionTag)) {
					TextView view = (TextView) container.findViewWithTag(actionTag);
					
					if (view != null) {
						container.removeView(view);
						
						mInfoBoxes.remove(actionTag);
					}
				}
			}
			
			handleMessageVisibility();
		}
	}
	
	private void handleMessageVisibility() {
		Integer count = 0;
		ViewGroup container = (ViewGroup) findViewById(R.id.placeholder);
		
		for (String tag : mInfoBoxes.keySet()) {
			TextView view = (TextView) container.findViewWithTag(tag);
			
			if (view != null) {
				Boolean status = mInfoBoxes.get(tag).onVisibilityChange((Context) this, mCurFragment, view.isShown());
				
				if (status) {
					view.setVisibility(View.VISIBLE); count += 1;
					
				} else {
					view.setVisibility(View.GONE);
				}
			}
		}
		
		container.setVisibility( (count > 0 ? View.VISIBLE : View.GONE) );
	}
	
	@Override
	public void frameUpdated() {
		updateTabState();
	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		
		if (mBackPressed) {
			System.exit(0);
		}
	}
	
	@Override
	public void onBackPressed() {
		mBackPressed = true;
		
		finish();
	}
}
