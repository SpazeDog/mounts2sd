package com.spazedog.mounts2sd;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class StartupReceiver extends BroadcastReceiver {
	
	@Override
	public void onReceive(Context context, Intent intent) {
		if ("android.intent.action.BOOT_COMPLETED".equals(intent.getAction())) {
			BaseApplication.setContext(context);
			
			// This service will only do the configurations and then kill itself
			context.startService( new Intent(context, StartupService.class) );
		}
	}
}
