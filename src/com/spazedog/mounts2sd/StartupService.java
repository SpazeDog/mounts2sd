package com.spazedog.mounts2sd;

import com.spazedog.mounts2sd.UtilsHelper.Notifier;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

public class StartupService extends Service {

	@Override
	public IBinder onBind(Intent arg0) {
		return null;
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		super.onStartCommand(intent, flags, startId);
		
		Integer message = null;
		
		if (!SettingsHelper.isRunning()) {
			if(!SettingsHelper.loadConfigs()) {
				if (!SettingsHelper.hasSU()) {
					message = R.string.notify_superuser_message;
					
				} else if (!SettingsHelper.hasBusybox()) {
					message = R.string.notify_busybox_message;
	
				} else if (!SettingsHelper.hasCompatibleBusybox()) {
					message = R.string.notify_busybox_compatibility_message;
	
				} else if (!SettingsHelper.hasScript()) {
					message = R.string.notify_script_message;
				}
				
			} else { 
				String lStatus = SettingsHelper.getPropAttention("script.status");
				
				if(lStatus != null && Integer.parseInt(lStatus) > 0) {
					message = R.string.notify_alert_message;
				}
			}
			
			// Some devices needs a little time to proper set all values in loadConfigs after execution
			try {
				Thread.sleep(500);
				
			} catch (InterruptedException e) {}
			
			if (message != null) {
				Notifier.send(StatusActivity.class, getResources().getString(R.string.notify_title), getResources().getString(message), R.drawable.ic_launcher, false, true, true);
			}
		}

		stopSelf();
		
		return START_NOT_STICKY;
	}
}
