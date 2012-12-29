package com.spazedog.mounts2sd;

import java.util.HashMap;
import java.util.Map;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.widget.ImageView;
import android.widget.TextView;

import com.spazedog.mounts2sd.MessageDialog.MessageDialogListener;

public class StatusActivity extends FragmentActivity implements MessageDialogListener {
	
	Map<String, View> STORAGE = new HashMap<String, View>();
	
	private ProgressDialog progressDialog;
	private LoadConfigsAsync loadAsync = null;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		BaseApplication.setContext(this);
		
		setContentView(R.layout.activity_main);
		
		if (!SettingsHelper.isLoaded()) {
			if (loadAsync == null) {
				loadAsync = new LoadConfigsAsync();
				loadAsync.execute("");
			}
			
		} else {
			handleDisplay();
		}
	}
	
	public class LoadConfigsAsync extends AsyncTask<String, Void, String> {
		private Boolean RUNNING = false;
		
		public Boolean isRunning() {
			return RUNNING;
		}
		
		@Override
		protected String doInBackground(String... params) {
			// We do also have an on-boot thread which uses this
			if (SettingsHelper.isRunning()) {
				Integer i = 33;
				
				while (true) {
					try {
						Thread.sleep(300);
						
					} catch (InterruptedException e) {}
					
					if (i < 1 || !SettingsHelper.isRunning()) {
						break;
					}
					
					i -= 1;
				}
			}
			
			if (!SettingsHelper.isLoaded()) {
				SettingsHelper.loadConfigs();
			}
			
			// Some devices needs a little time to proper set all values in loadConfigs after execution
			try {
				Thread.sleep(500);
				
			} catch (InterruptedException e) {}
			
			return null;
		}
		
		@Override
		protected void onPostExecute(String result) {
			StatusActivity.this.progressDialog.dismiss();
			StatusActivity.this.progressDialog = null;
			
			StatusActivity.this.handleDisplay();
			StatusActivity.this.handleStorageView();
		}
		
		@Override
		protected void onPreExecute() {
			StatusActivity.this.progressDialog = ProgressDialog.show(StatusActivity.this, "", "Loading Configurations...");
		}
	}
	
    @Override
    public void onResume() {
        super.onResume();
        
        handleStorageView();
    }
    
	@Override
	public void onStop() {
		for(String key : STORAGE.keySet()) {
			STORAGE.put(key, null);
		}
		
		super.onStop();
	}
	
	@Override
	public void onRestart() {
		View view;
		
		for(String key : STORAGE.keySet()) {
			view = findViewById( getResources().getIdentifier( (key.replaceAll("\\.", "_") + "_layout_7975cd4b"), "id", "com.spazedog.mounts2sd") );
			
			if (view != null) {
				STORAGE.put(key, view);
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

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.activity_m2sd_main, menu);
		return true;
	} 
	
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()) {
            case R.id.menu_settings:
    			Intent intent = new Intent(this, SettingsActivity.class);
    			intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
    			
    			startActivity(intent);
    			
                return true;

            default:
            return super.onOptionsItemSelected(item);
        }
    }
    
    public void onMessageDialogClose() {
    	finish();
    }
    
    public void handleStorageView() {
        String location;
        for(String key : STORAGE.keySet()) {
        	location = SettingsHelper.getPropState(key);
        	
        	if (location != null) {
        		((TextView) STORAGE.get(key).findViewById(R.id.item_usage_7975cd4b)).setText(
        				String.format(getResources().getString(R.string.storage_usage_text), ""+UtilsHelper.getMB(UtilsHelper.diskUsage(location)), ""+UtilsHelper.getMB(UtilsHelper.diskTotal(location)))
        		);
        	}
        }
    }
    
    public void handleDisplay() {
		if (!SettingsHelper.isLoaded()) {
			if (!SettingsHelper.hasSU()) {
				((MessageDialog) new MessageDialog()).addMessage("No SuperUser account", "Your device does not have the abillity to switch to the superuser account. This is needed in order for the startup script to do it's work, and needed by the application it self in order to write configurations to the property folder. Please enable this account (Root your device). You can find posts on how to do this for your specific device on XDA-Developer.com").show(getSupportFragmentManager(), "MessageDialog");
					
			}else if (!SettingsHelper.hasBusybox()) {
				((MessageDialog) new MessageDialog()).addMessage("Busybox is missig", "Your device do not have busybox available. Busybox is an all-in-one binary that contains many standard linux tools, some of which is needed by both the Mounts2SD startup script as well as the application it self. Please install busybox from one of the many XDA-Developer.com threads. DO NOT install it from the android market. Those available are both outdated and does not work properly.").show(getSupportFragmentManager(), "MessageDialog");
				
			} else {
				Intent intent = new Intent(this, SettingsActivity.class);
				intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				
				startActivity(intent);
				
				finish();
			}
			
		} else {
			String propState, propAttention, propMessage, propDependency, dependValue, location;
			String[] props = SettingsHelper.getPropsCollection();
			View view;
			TextView text;
			ImageView bullet;

			for (int i=0; i < props.length; i++) {
				view = findViewById( getResources().getIdentifier((props[i].replaceAll("\\.", "_") + "_layout_7975cd4b"), "id", "com.spazedog.mounts2sd") );
				
				if(view != null) {
					propMessage = SettingsHelper.getPropMessage(props[i]);
		        	if (propMessage != null) {
	        			if ((text = (TextView) view.findViewById(R.id.item_message_7975cd4b)) != null) {
	        				text.setText(propMessage);
	        				text.setVisibility(View.VISIBLE);
	        			}
		        	}
					
		        	if (SettingsHelper.propIsActive(props[i])) {
						if (SettingsHelper.propType(props[i]) == "path") {
							((TextView) view.findViewById(R.id.item_mountpoint_7975cd4b)).setText( (location = SettingsHelper.getPropState(props[i])) != null ? location : getResources().getString(R.string.not_available_text) );
							
							STORAGE.put(props[i], view);
							
						} else if (SettingsHelper.propHasState(props[i]) && SettingsHelper.propHasConfig(props[i])) {
							if ((propDependency = SettingsHelper.propDependedOn(props[i])) == null || ((dependValue = SettingsHelper.getPropState(propDependency)) != null && !"".equals(dependValue) && !"0".equals(dependValue))) {
					        	propState = SettingsHelper.getPropState(props[i]);
					        	propAttention = SettingsHelper.getPropAttention(props[i]);
		
					        	if ((bullet = (ImageView) view.findViewById(R.id.item_bullet_7975cd4b)) != null) {
					        		if ((propState != null && !"0".equals(propState)) || (propAttention != null && !"0".equals(propAttention))) {
					        			bullet.setImageResource(
					        				(propState == null || "0".equals(propState)) && (propAttention != null && !"0".equals(propAttention)) ? R.drawable.status_disabled_error : 
					        						propAttention != null && "1".equals(propAttention) ? R.drawable.status_warning : 
					        							propAttention != null && "2".equals(propAttention) ? R.drawable.status_error : R.drawable.status_enabled
						        		);
					        		}
					        		
					        	} else if ((text = (TextView) view.findViewById(R.id.item_value_7975cd4b)) != null) {
					        		if (propState != null && !"".equals(propState)) {
					        			text.setText(propState);
					        		}
					        	}
							}
						}
		        	}
				}
			}
			
			for (int i=0; i < 2; i++) {
				view = findViewById(getResources().getIdentifier( (i == 1 ? "btn_configure_layout_7975cd4b" : "btn_viewlog_layout_7975cd4b"), "id", "com.spazedog.mounts2sd"));
				view.setOnTouchListener(new OnTouchListener() {
					public boolean onTouch(View v, MotionEvent event) {
						if(event.getActionMasked() == MotionEvent.ACTION_DOWN) {
							v.setBackgroundColor( getResources().getColor(R.color.light_gray) );
							
						} else if (event.getActionMasked() == MotionEvent.ACTION_UP || event.getActionMasked() == MotionEvent.ACTION_OUTSIDE || event.getActionMasked() == MotionEvent.ACTION_CANCEL) {
							v.setBackgroundDrawable(null);
							
							if (event.getActionMasked() == MotionEvent.ACTION_UP) {
								Intent intent = new Intent(BaseApplication.getContext(), v.getId() == R.id.btn_configure_layout_7975cd4b ? ConfigureActivity.class : LogActivity.class);
								intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
								
								BaseApplication.getContext().startActivity(intent);
							}
						}
						
						return false;
					}
				});
			}
		}
		
		loadAsync = null;
    }
}