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
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.TextView;

import com.spazedog.lib.taskmanager.Task;
import com.spazedog.mounts2sd.tools.ExtendedActivity;
import com.spazedog.mounts2sd.tools.Preferences;
import com.spazedog.mounts2sd.tools.Root;
import com.spazedog.mounts2sd.tools.Utils;
import com.spazedog.mounts2sd.tools.Utils.Relay.Message;
import com.spazedog.mounts2sd.tools.Utils.Relay.MessageReceiver;
import com.spazedog.mounts2sd.tools.containers.DeviceSetup;
import com.spazedog.mounts2sd.tools.interfaces.DialogListener;
import com.spazedog.mounts2sd.tools.interfaces.DialogMessageResponse;
import com.spazedog.mounts2sd.tools.interfaces.TabController;

public class ActivityTabController extends ExtendedActivity implements OnClickListener, DialogListener, DialogMessageResponse, TabController, MessageReceiver {

	private ProgressDialog mProgressDialog;

	private Boolean mAsyncProcessing = false;
	private Boolean mAsyncFinished = false;
	private Boolean mAsyncResult = false;
	
	private Boolean mLoaded = false;

	private Integer mCurFragment = null;

	private Map<Integer, Fragment> mFragments = new HashMap<Integer, Fragment>(); {
		mFragments.put(R.id.tab_fragment_appmanager, null);
		mFragments.put(R.id.tab_fragment_log, null);
		mFragments.put(R.id.tab_fragment_configure, null);
		mFragments.put( (mCurFragment = R.id.tab_fragment_overview), null);
	}

	private Map<String, Message> mInfoBoxes = new HashMap<String, Message>();
	
	private Preferences mPreferences;
	
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
			mLoaded = savedInstanceState.getBoolean("mLoaded");
		}
		
		setContentView(R.layout.activity_tab_controller);
		
		for (int key : mFragments.keySet()) {
			Fragment fragment = getSupportFragmentManager().findFragmentByTag(""+key);
			
			if (fragment == null) {
				switch (key) {
					case R.id.tab_fragment_appmanager: fragment = new FragmentTabAppManager(); break;
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
		
		Utils.Relay.Message.setReceiver(this);
		
		mPreferences.session().is_unlocked( getResources().getBoolean(R.bool.config_unlocked) || Utils.checkLicenseKey(this) );
		
		if (!mPreferences.session().is_unlocked()) {
			View view = findViewById(R.id.tab_fragment_appmanager);
			((ViewGroup) view.getParent()).removeView(view);
			
			mFragments.remove(R.id.tab_fragment_appmanager);
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
		savedInstanceState.putBoolean("mLoaded", mLoaded);

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
				
				private Boolean mForceProgress = false;
				
				@Override
				protected void onUIReady() {
					ActivityTabController activity = (ActivityTabController) getObject();
					
					if ((!activity.mLoaded || mForceProgress) && mProgressMessage != null) {
						if (activity.mProgressDialog == null) {
							activity.mProgressDialog = ProgressDialog.show((FragmentActivity) getActivityObject(), "", mProgressMessage + "...");
							
						} else {
							activity.mProgressDialog.setMessage(mProgressMessage + "...");
						}
					}
				}
				
				@Override
				protected Boolean doInBackground(Context... params) {
					if (Root.isConnected()) {
						Root.lock("TabController");
						
						Preferences preferences = new Preferences( (Context) params[0] );
						
						publishProgress(1);
						if (!preferences.checkDeviceSetup()) {
							mForceProgress = true;
							
							publishProgress(1);
							if (!preferences.loadDeviceSetup(false)) {
								return false;
							}
						}
						
						publishProgress(2);
						if (!preferences.checkDeviceConfig()) {
							mForceProgress = true;
							
							publishProgress(2);
							if (!preferences.loadDeviceConfig(false)) {
								return false;
							}
						}
						
						publishProgress(3);
						if (!preferences.checkDeviceProperties()) {
							mForceProgress = true;
							
							publishProgress(3);
							if (!preferences.loadDeviceProperties(false)) {
								return false;
							}
						}
						
						return true;
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
					
					activity.mLoaded = true;
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
				if (!Root.isConnected()) {
					new FragmentDialog.Builder(this, "TabControllerDialog", getResources().getString(R.string.message_error_su_headline)).showMessageDialog(getResources().getString(R.string.message_error_su_text), true);
					
				} else if (!deviceSetup.environment_busybox()) {
					new FragmentDialog.Builder(this, "TabControllerDialog", getResources().getString(R.string.message_error_busybox_headline)).showMessageDialog(getResources().getString(R.string.message_error_busybox_text), true);
					
				} else {
					new FragmentDialog.Builder(this, "TabControllerDialog", getResources().getString(R.string.message_error_unknown_headline)).showMessageDialog(getResources().getString(R.string.message_error_unknown_text), true);
				}
			}
			
		} else {
			switchTabFragment(mCurFragment);
			
			if (deviceSetup.log_level() > 1) {
				Utils.Relay.Message.add("log-details", getResources().getString(R.string.infobox_errors), new Message() {
					@Override
					public Boolean onVisibilityChange(Context context, Integer tabId, Boolean visible) {
						if (tabId == R.id.tab_fragment_log) {
							Utils.Relay.Message.remove("log-details", true); return false;
						}
						
						return tabId != R.id.tab_fragment_log;
					}
				});
			}
			
			if (deviceSetup.safemode()) {
				Utils.Relay.Message.add("save-mode", getResources().getString(R.string.infobox_safemode), new Message() {
					@Override
					public Boolean onVisibilityChange(Context context, Integer tabId, Boolean visible) {
						return tabId != R.id.tab_fragment_log;
					}
				});
			}
			
			if (deviceSetup.path_device_map_sdext() == null) {
				Utils.Relay.Message.add("no-sdext", getResources().getString(R.string.infobox_no_sdext), new Message() {
					@Override
					public Boolean onVisibilityChange(Context context, Integer tabId, Boolean visible) {
						return tabId == R.id.tab_fragment_configure;
					}
				});
			}
			
			if (!deviceSetup.environment_startup_script()) {
				Utils.Relay.Message.add("no-script", getResources().getString(R.string.infobox_no_script), new Message() {
					@Override
					public Boolean onVisibilityChange(Context context, Integer tabId, Boolean visible) {
						if ((new Preferences(context)).deviceSetup().environment_startup_script()) {
							Utils.Relay.Message.remove("no-script", false); return false;
						}
						
						return tabId == R.id.tab_fragment_configure;
					}
				});
				
			} else if (!getResources().getString(R.string.config_script_id).equals( "" + mPreferences.deviceSetup().id_startup_script() )) {
				Utils.Relay.Message.add("update-script", getResources().getString(R.string.infobox_update_script), new Message() {
					@Override
					public Boolean onVisibilityChange(Context context, Integer tabId, Boolean visible) {
						DeviceSetup setup = new Preferences(context).deviceSetup();
						
						if (setup.environment_startup_script() && context.getResources().getString(R.string.config_script_id).equals( "" + setup.id_startup_script() )) {
							Utils.Relay.Message.remove("update-script", false); return false;
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
	
	private void handleMessageVisibility() {
		Integer count = 0;
		ViewGroup container = (ViewGroup) findViewById(R.id.placeholder);
		
		for (String tag : mInfoBoxes.keySet()) {
			if (mInfoBoxes.get(tag) != null) {
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
			Root.unlock("TabController");
			Root.close();
			
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
		if (!mInfoBoxes.containsKey(tag) && !mPreferences.cached("infobox").getBoolean("retain_" + tag)) {
			ViewGroup viewGroup = (ViewGroup) findViewById(R.id.placeholder);
			
			if (viewGroup != null) {
				TextView view = (TextView) getLayoutInflater().inflate(R.layout.inflate_info_box, viewGroup, false);
				view.setText(message);
				view.setTag(tag);
				
				viewGroup.addView(view);
				
				mInfoBoxes.put(tag, visibilityController);
				
				if (visibilityController != null) {
					view.setVisibility(View.GONE);
					
					handleMessageVisibility();
				}
			}
		}
	}

	@Override
	public void onMessageRemove(String tag, Boolean retainState) {
		if (mInfoBoxes.containsKey(tag)) {
			ViewGroup viewGroup = (ViewGroup) findViewById(R.id.placeholder);
			
			if (viewGroup != null) {
				View view = ((ViewGroup) findViewById(R.id.placeholder)).findViewWithTag(tag);
				
				if (view != null) {
					viewGroup.removeView(view);
				}
			}
			
			if (retainState) {
				mPreferences.cached("infobox").putBoolean("retain_" + tag, true);
			}
			
			mInfoBoxes.remove(tag);
		}
	}
}
