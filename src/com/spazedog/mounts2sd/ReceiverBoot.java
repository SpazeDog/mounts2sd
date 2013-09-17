package com.spazedog.mounts2sd;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;

import com.spazedog.lib.rootfw3.RootFW;
import com.spazedog.mounts2sd.tools.Preferences;
import com.spazedog.mounts2sd.tools.Root;

public class ReceiverBoot extends BroadcastReceiver {
	
	@Override
	public void onReceive(Context context, Intent intent) {
		if ("android.intent.action.BOOT_COMPLETED".equals(intent.getAction())) {
			new Thread() {
				private Context mContext;
				
				public Thread putContext(Context context) {
					mContext = context;
					
					return this;
				}
				
				@Override
				public void run() {
					String message = null;
					
					RootFW root = Root.initiate();
					
					if (root.isConnected()) {
						Preferences preferences = Preferences.getInstance(mContext);
						
						if (!preferences.deviceSetup.load(true)
								|| !preferences.deviceConfig.load(true)
								|| !preferences.deviceProperties.load(true)) {
						
							message = mContext.getResources().getString(R.string.notify_no_config);
							
						} else if (preferences.deviceSetup.log_level() > 1) {
							message = mContext.getResources().getString(R.string.notify_log_details);
						}
						
					} else {
						message = mContext.getResources().getString(R.string.notify_no_config);
					}
					
					if (message != null) {
						NotificationManager manager = (NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE);
						
						NotificationCompat.Builder notification = new NotificationCompat.Builder(mContext).setContentIntent(PendingIntent.getActivity(
								mContext,
								0,
								new Intent(mContext, ActivityTabController.class),
								0
						))
						.setSmallIcon(R.drawable.ic_launcher)
						.setWhen(System.currentTimeMillis())
						.setAutoCancel(true)
						.setContentTitle( mContext.getApplicationInfo().name )
						.setContentText(message);
						
						manager.notify(1, notification.build());
					}
					
					Root.release();
				}
				
			}.putContext(context.getApplicationContext()).start();
		}
	}
}