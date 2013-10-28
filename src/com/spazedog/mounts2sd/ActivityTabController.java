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

import com.spazedog.lib.taskmanager.Task;
import com.spazedog.mounts2sd.tools.Common;
import com.spazedog.mounts2sd.tools.ExtendedActivity;
import com.spazedog.mounts2sd.tools.Preferences;
import com.spazedog.mounts2sd.tools.Root;
import com.spazedog.mounts2sd.tools.Utils;
import com.spazedog.mounts2sd.tools.Utils.Relay.IRelayMessageReceiver;
import com.spazedog.mounts2sd.tools.Utils.Relay.Message;
import com.spazedog.mounts2sd.tools.interfaces.IDialogMessageResponse;
import com.spazedog.mounts2sd.tools.interfaces.ITabController;

public class ActivityTabController extends ExtendedActivity implements OnClickListener, IDialogMessageResponse, ITabController, IRelayMessageReceiver {
	
	private final static Integer RESULT_FAILED_GENERAL = 0;
	private final static Integer RESULT_FAILED_ROOT = -1;
	private final static Integer RESULT_SUCCESS = 1;
	
	private final Map<Integer, Fragment> mTabFragments = new HashMap<Integer, Fragment>(); {
		mTabFragments.put(R.id.tab_fragment_overview, null);
		mTabFragments.put(R.id.tab_fragment_configure, null);
		mTabFragments.put(R.id.tab_fragment_log, null);
		mTabFragments.put(R.id.tab_fragment_appmanager, null);
	};
	
	private Integer mCurrentTabFragment = R.id.tab_fragment_overview;
	
	private Boolean mBackPressed = false;
	
	private Boolean mLoaderIsRunning = false;
	
	private Preferences mPreferences;
	
	private Map<String, Message> mMessageBoxes = new HashMap<String, Message>();
	
	@Override
	public void onSaveInstanceState(Bundle savedInstanceState) {
		savedInstanceState.putInt("CurrentTabFragment", mCurrentTabFragment);
		savedInstanceState.putBoolean("LoaderIsRunning", mLoaderIsRunning);
		
		super.onSaveInstanceState(savedInstanceState);
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		if (savedInstanceState != null) {
			mCurrentTabFragment = savedInstanceState.getInt("CurrentTabFragment");
			mLoaderIsRunning = savedInstanceState.getBoolean("LoaderIsRunning");
		}
		
		mPreferences = Preferences.getInstance((Context) this);
		
		setTheme(mPreferences.theme());
		
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.activity_tab_controller);
		
		Utils.Relay.Message.setReceiver(this);
		
		for (int key : mTabFragments.keySet()) {
			Fragment fragment = getSupportFragmentManager().findFragmentByTag("" + key);
			
			findViewById(key).setOnClickListener(this);
			
			if (fragment == null) {
				switch (key) {
					case R.id.tab_fragment_appmanager: fragment = new FragmentTabAppManager(); break;
					case R.id.tab_fragment_log: fragment = new FragmentTabLog(); break;
					case R.id.tab_fragment_configure: fragment = new FragmentTabConfigure(); break;
					case R.id.tab_fragment_overview: fragment = new FragmentTabOverview();
				}
			}
			
			mTabFragments.put(key, fragment);
		}
		
		if (!mPreferences.applicationSession.is_unlocked()) {
			mTabFragments.remove(R.id.tab_fragment_appmanager);
			Utils.removeView(findViewById(R.id.tab_fragment_appmanager), false);
		}
		
		Root.lock();
		
		loadDeviceConfigurations();
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		
		onMessageVisibilityUpdate();
	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		
		if (mBackPressed) {
			if (Root.isConnected()) {
				Root.unlock();
				Root.initiate().destroy();
			}
			
			System.exit(0);
		}
	}
	
	@Override
	public void onBackPressed() {
		mBackPressed = true;
		
		finish();
	}

	@Override
	public void onMessageReceive(String tag, String message, Message visibilityController) {
		if (!mMessageBoxes.containsKey(tag) && !mPreferences.persistence.get("Message:" + tag)) {
			ViewGroup viewGroup = (ViewGroup) findViewById(R.id.placeholder);
			
			if (viewGroup != null) {
				TextView view = (TextView) getLayoutInflater().inflate(R.layout.inflate_info_box, viewGroup, false);
				view.setText(message);
				view.setTag(tag);
				
				viewGroup.addView(view);
				
				mMessageBoxes.put(tag, visibilityController);
				
				if (visibilityController != null) {
					view.setVisibility(View.GONE);
					
					onMessageVisibilityUpdate();
				}
			}
		}
	}

	@Override
	public void onMessageRemove(String tag, Boolean retainState) {
		if (mMessageBoxes.containsKey(tag)) {
			ViewGroup viewGroup = (ViewGroup) findViewById(R.id.placeholder);
			
			if (viewGroup != null) {
				View view = ((ViewGroup) findViewById(R.id.placeholder)).findViewWithTag(tag);
				
				if (view != null) {
					viewGroup.removeView(view);
				}
			}
			
			if (retainState) {
				mPreferences.persistence.set("Message:" + tag, true);
			}
			
			mMessageBoxes.remove(tag);
		}
	}
	
	@Override
	public void onMessageVisibilityUpdate() {
		Integer count = 0;
		ViewGroup container = (ViewGroup) findViewById(R.id.placeholder);
		
		for (String tag : mMessageBoxes.keySet()) {
			if (mMessageBoxes.get(tag) != null) {
				TextView view = (TextView) container.findViewWithTag(tag);
				
				if (view != null) {
					Boolean status = mMessageBoxes.get(tag).onVisibilityChange((Context) this, mCurrentTabFragment, view.isShown());
					
					if (status) {
						view.setVisibility(View.VISIBLE); count += 1;
						
					} else {
						view.setVisibility(View.GONE);
					}
				}
			}
		}
		
		container.setVisibility( (count > 0 ? View.VISIBLE : View.GONE) );
	}

	@Override
	public void onDialogClose(String tag, Boolean exit, Bundle extra) {
		if (exit) {
			onBackPressed();
		}
	}
	
	@Override
	public void onTabUpdate() {
		tabUpdateButtons();
	}

	@Override
	public void onClick(View view) {
		tabSwitchFragment( view.getId() );
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
	
	private void loadDeviceConfigurations() {
		if (!mLoaderIsRunning) {
			mLoaderIsRunning = true;
			
			try {
				new Task<Context, String, Integer>(this, "loadDeviceConfigurations") {
					@Override
					protected Integer doInBackground(Context... params) {
						Context context = (Context) params[0];
						
						if (Root.initiate().isConnected()) {
							Preferences preferences = Preferences.getInstance(context);
							
							if (!preferences.deviceSetup.isLoaded()) {
								publishProgress( context.getResources().getString(R.string.progress_load_setup) + "..." );
								
								Common.wait(350);
								
								if (!preferences.deviceSetup.load(false)) {
									return RESULT_FAILED_GENERAL;
								}
							}
							
							if (!preferences.deviceConfig.isLoaded()) {
								publishProgress( context.getResources().getString(R.string.progress_load_config) + "..." );
								
								Common.wait(350);
								
								if (!preferences.deviceConfig.load(false)) {
									return RESULT_FAILED_GENERAL;
								}
							}
							
							if (!preferences.deviceProperties.isLoaded()) {
								publishProgress( context.getResources().getString(R.string.progress_load_properties) + "..." );
								
								Common.wait(350);
								
								if (!preferences.deviceProperties.load(false)) {
									return RESULT_FAILED_GENERAL;
								}
							}
							
							return RESULT_SUCCESS;
						}
						
						return RESULT_FAILED_ROOT;
					}
					
					@Override
					protected void onProgressUpdate(String... progress) {
						setProgressMessage(progress[0]);
					}
					
					@Override
					protected void onPostExecute(Integer result) {
						ActivityTabController activity = (ActivityTabController) getObject();
						
						activity.mLoaderIsRunning = false;
						activity.handleDeviceConfigurations(result);
					}
					
				}.execute( getApplicationContext() );
				
			} catch (IllegalStateException e) {}
		}
	}
	
	private void handleDeviceConfigurations(Integer result) {
		if (result != RESULT_SUCCESS) {
			if (isForeground() && getSupportFragmentManager().findFragmentByTag("TabControllerDialog") == null) {
				if (result == RESULT_FAILED_ROOT) {
					new FragmentDialog.Builder(this, "TabControllerDialog", getResources().getString(R.string.message_error_su_headline), null).showMessageDialog(getResources().getString(R.string.message_error_su_text), true);
					
				} else if (!mPreferences.deviceSetup.environment_busybox()) {
					new FragmentDialog.Builder(this, "TabControllerDialog", getResources().getString(R.string.message_error_busybox_headline), null).showMessageDialog(getResources().getString(R.string.message_error_busybox_text), true);
					
				} else {
					new FragmentDialog.Builder(this, "TabControllerDialog", getResources().getString(R.string.message_error_unknown_headline), null).showMessageDialog(getResources().getString(R.string.message_error_unknown_text), true);
				}
			}
			
		} else {
			if (mPreferences.deviceSetup.log_level() > 1) {
				Utils.Relay.Message.add("log-details", getResources().getString(R.string.infobox_errors), new Message() {
					@Override
					public Boolean onVisibilityChange(Context context, Integer tabId, Boolean visible) {
						if (tabId == R.id.tab_fragment_log) {
							Utils.Relay.Message.remove("log-details", true); return false;
						}
						
						return tabId != R.id.tab_fragment_log && tabId != R.id.tab_fragment_appmanager;
					}
				});
			}
			
			if (mPreferences.deviceSetup.safemode()) {
				Utils.Relay.Message.add("save-mode", getResources().getString(R.string.infobox_safemode), new Message() {
					@Override
					public Boolean onVisibilityChange(Context context, Integer tabId, Boolean visible) {
						return tabId != R.id.tab_fragment_log && tabId != R.id.tab_fragment_appmanager;
					}
				});
			}
			
			if (mPreferences.deviceSetup.path_device_map_sdext() == null) {
				Utils.Relay.Message.add("no-sdext", getResources().getString(R.string.infobox_no_sdext), new Message() {
					@Override
					public Boolean onVisibilityChange(Context context, Integer tabId, Boolean visible) {
						return tabId == R.id.tab_fragment_configure;
					}
				});
			}
			
			if (!mPreferences.deviceSetup.environment_startup_script()) {
				Utils.Relay.Message.add("no-script", getResources().getString(R.string.infobox_no_script), new Message() {
					@Override
					public Boolean onVisibilityChange(Context context, Integer tabId, Boolean visible) {
						if (mPreferences.deviceSetup.environment_startup_script()) {
							Utils.Relay.Message.remove("no-script", false); return false;
						}
						
						return tabId != R.id.tab_fragment_log && tabId != R.id.tab_fragment_appmanager;
					}
				});
				
			} else if (!getResources().getString(R.string.config_script_id).equals( "" + mPreferences.deviceSetup.id_startup_script() )) {
				Utils.Relay.Message.add("update-script", getResources().getString(R.string.infobox_update_script), new Message() {
					@Override
					public Boolean onVisibilityChange(Context context, Integer tabId, Boolean visible) {
						if (mPreferences.deviceSetup.environment_startup_script() && context.getResources().getString(R.string.config_script_id).equals( "" + mPreferences.deviceSetup.id_startup_script() )) {
							Utils.Relay.Message.remove("update-script", false); return false;
						}
						
						return tabId != R.id.tab_fragment_log && tabId != R.id.tab_fragment_appmanager;
					}
				});
			}
			
			tabSwitchFragment(mCurrentTabFragment);
		}
	}
	
	private void tabSwitchFragment(Integer tabId) {
		FragmentManager fragmentManager = getSupportFragmentManager();
		Boolean isAdded = fragmentManager.findFragmentByTag("" + tabId) != null;
		
		if (!isAdded || mTabFragments.get(tabId).isDetached()) {
			FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
			
			for (int id : mTabFragments.keySet()) {
				if (id != tabId && fragmentManager.findFragmentByTag("" + id) != null && !mTabFragments.get(id).isDetached()) {
					fragmentTransaction.detach( mTabFragments.get(id) );
				}
			}
			
			if (isAdded && mTabFragments.get(tabId).isDetached()) {
				fragmentTransaction.attach(mTabFragments.get(tabId));
				
			} else if (!isAdded) {
				fragmentTransaction.add(R.id.fragment_frame, mTabFragments.get(tabId), "" + tabId);
			}
			
			fragmentTransaction.commit();
			
		} else {
			tabUpdateButtons();
		}
	}
	
	private void tabUpdateButtons() {
		for (int id : mTabFragments.keySet()) {
			if (getSupportFragmentManager().findFragmentByTag("" + id) != null && !mTabFragments.get(id).isDetached()) {
				mCurrentTabFragment = id;
				
				findViewById(id).setSelected(true);
				
			} else {
				findViewById(id).setSelected(false);
			}
		}
		
		Utils.Relay.Message.triggerVisibilityUpdate();
	}
}
