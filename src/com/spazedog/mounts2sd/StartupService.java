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
