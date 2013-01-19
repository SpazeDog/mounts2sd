package com.spazedog.mounts2sd;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Environment;
import android.os.StatFs;
import android.util.Log;
import android.view.View;

public class UtilsHelper {
	public static final String TAG = "Mounts2SD";
	private static final String SIZE_PRIFIX[] = {"b","Kb","Mb","Gb"};
	
	public static String[] splitScriptMessage(String msg, Boolean translate) {
		String[] tmp = msg.split("883b21e9/");
		String[] out = new String[3];
		String[] parts = new String[0];
		Integer translatedId;
		
		if (tmp.length > 3) {
			parts = tmp[3].split("ad4ebc50/");
		}
		
		for (int i=0; i < out.length; i++) {
			out[i] = tmp.length >= (i+1) ? tmp[i] : null;
			
			if (i == 2) {
				if (translate && out[i] != null) {
					translatedId = BaseApplication.getContext().getResources().getIdentifier("script_msg_" + md5(out[i].trim()), "string", BaseApplication.getContext().getPackageName());
					
					if (translatedId > 0) {
						out[i] = BaseApplication.getContext().getResources().getString(translatedId);
					}
				}
				
				for (int x=0; x < parts.length; x++) {
					out[i] = out[i].replace("%arg" + (x+1), parts[x]);
				}
			}
		}
		
		return out;
	}
	
    public static String getMB(double iNum) {
        String lPrifix = SIZE_PRIFIX[0];
        double iCal = (double) iNum;
        double iDevide = 1024D;

        for (int i=1; i < SIZE_PRIFIX.length; i++) {
                if (iCal < iDevide) {
                        break;
                }

                iCal = iCal/iDevide;
                lPrifix = SIZE_PRIFIX[i];
        }

        return "" + (Math.round(iCal*100.0)/100.0) + lPrifix;
	}
	
	public static double diskUsage(String dir) {
		try {
	        StatFs stat = new StatFs(dir);
	        double result = ((double) stat.getBlockCount() - (double) stat.getAvailableBlocks()) * (double) stat.getBlockSize();
	
	        return result;
	        
		} catch (Exception e) {
			return 0;
		}
	}
	
	public static double diskTotal(String dir) {
		try {
	        StatFs stat = new StatFs(dir);
	        double result = (double) stat.getBlockCount() * (double) stat.getBlockSize();
	
	        return result;
	        
		} catch (Exception e) {
			return 0;
		}
	}
	
	public static final String md5(final String s) { 
		try { // Create MD5 Hash 
			
			MessageDigest digest = MessageDigest.getInstance("MD5");
			digest.update(s.getBytes(), 0, s.length());
			
			char[] hex_digits = "0123456789abcdef".toCharArray();
			byte[] data = digest.digest();
		    char[] chars = new char[data.length * 2];
		    
		    for (int i = 0; i < data.length; i++) {
		        chars[i * 2] = hex_digits[(data[i] >> 4) & 0xf];
		        chars[i * 2 + 1] = hex_digits[data[i] & 0xf];
		    }
			
			return new String(chars);

	    } catch (NoSuchAlgorithmException e) {
	        e.printStackTrace();
	    }
		
	    return "";
	}
	
	public static String sdcardState() {
	    String sdcardStatus = Environment.getExternalStorageState();

	    if (sdcardStatus.equals(Environment.MEDIA_MOUNTED_READ_ONLY)) {
	    	return BaseApplication.getContext().getResources().getString(R.string.sdcard_state_ro);

	    } else if (sdcardStatus.equals(Environment.MEDIA_NOFS)) {
	    	return BaseApplication.getContext().getResources().getString(R.string.sdcard_state_format);

	    } else if (sdcardStatus.equals(Environment.MEDIA_REMOVED)) {
	    	return BaseApplication.getContext().getResources().getString(R.string.sdcard_state_missing);

	    } else if (sdcardStatus.equals(Environment.MEDIA_SHARED)) {
	    	return BaseApplication.getContext().getResources().getString(R.string.sdcard_state_ums);

	    } else if (sdcardStatus.equals(Environment.MEDIA_UNMOUNTABLE)) {
	    	return BaseApplication.getContext().getResources().getString(R.string.sdcard_state_mount_failure);

	    } else if (sdcardStatus.equals(Environment.MEDIA_UNMOUNTED)) {
	    	return BaseApplication.getContext().getResources().getString(R.string.sdcard_state_mount);

	    }

	    return null;
	}
	
	public static class Notifier {
		private static Integer NOTIFY_ID = 0;
		
		public static Integer send(Class<?> pActivity, String pTitle, String pMessage, Integer pIcon, Boolean pFlash, Boolean pVibrate, Boolean pCancel) {
			
			NOTIFY_ID += 1;
			
			PendingIntent lPendingIntent = PendingIntent.getActivity(
					BaseApplication.getContext(), 
					0, 
					new Intent(BaseApplication.getContext(), pActivity), 
					0
			);
			NotificationManager lNotifyManager = (NotificationManager) BaseApplication.getContext().getSystemService(Context.NOTIFICATION_SERVICE);
			Notification lNotifier = new Notification();
			
			lNotifier.icon = pIcon;
			lNotifier.tickerText = pTitle;
			lNotifier.when = System.currentTimeMillis();
			
			if (pFlash) {
				lNotifier.flags |= Notification.FLAG_SHOW_LIGHTS;
				lNotifier.ledARGB = Color.CYAN;
				lNotifier.ledOnMS = 500;
				lNotifier.ledOffMS = 500;
		    }
			
	        if (pVibrate) {
	        	lNotifier.vibrate = new long[] {100, 200, 200, 200, 200, 200};
	        }
	        
	        if(pCancel) {
	        	lNotifier.flags |= Notification.FLAG_AUTO_CANCEL;
	        }
	        
	        lNotifier.setLatestEventInfo(BaseApplication.getContext(), pTitle, pMessage, lPendingIntent);
	        
	        lNotifyManager.notify(NOTIFY_ID, lNotifier);
	        
	        return NOTIFY_ID;
	    }
	}
	
	public static class ElementContainer {
		private Map<String, String> STRINGS = new HashMap<String, String>();
		private Map<String, Integer> INTEGERS = new HashMap<String, Integer>();
		private Map<String, Boolean> BOOLS = new HashMap<String, Boolean>();
		private Map<String, View> VIEWS = new HashMap<String, View>();
		
		public final void putInt(String pName, int pValue) {
			INTEGERS.put(pName, pValue);
		}
		
		public final Integer getInt(String pName) {
			return INTEGERS.get(pName);
		}
		
		public final void putBool(String pName, Boolean pValue) {
			BOOLS.put(pName, pValue);
		}
		
		public final Boolean getBool(String pName) {
			return BOOLS.get(pName);
		}
		
		public final void putString(String pName, String pValue) {
			STRINGS.put(pName, pValue);
		}
		
		public final String getString(String pName) {
			return STRINGS.get(pName);
		}
		
		public final void putView(String pName, View pValue) {
			VIEWS.put(pName, pValue);
		}
		
		public final View getView(String pName) {
			return VIEWS.get(pName);
		}
	}
	
	public static class SelectorOptions {
		String VALUES[];
		String NAMES[];
		String COMMENTS[];
		Boolean SUPPORTED[];
		Integer SIZE;
		
		public SelectorOptions(String name) {
			NAMES = BaseApplication.getContext().getResources().getStringArray( BaseApplication.getContext().getResources().getIdentifier("selector_" + name + "_names", "array", BaseApplication.getContext().getPackageName()) );
			VALUES = BaseApplication.getContext().getResources().getStringArray( BaseApplication.getContext().getResources().getIdentifier("selector_" + name + "_values", "array", BaseApplication.getContext().getPackageName()) );
			
			Integer commentsId = BaseApplication.getContext().getResources().getIdentifier("selector_" + name + "_comments", "array", BaseApplication.getContext().getPackageName());
			
			if (commentsId != 0) {
				COMMENTS = BaseApplication.getContext().getResources().getStringArray(commentsId);
				
			} else {
				COMMENTS = new String[VALUES.length];
			}
			
			SUPPORTED = new Boolean[VALUES.length];
			SIZE = VALUES.length;
			
			if (name.equals("filesystem")) {
				try {
					BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream("/proc/filesystems")));
					String line;
					String fsTypes = "auto";
					
					while ((line = br.readLine()) != null) {
						if (!line.contains("nodev ")) {
							fsTypes += " " + line;
						}
					}
					
					br.close();
					
					for (int i=0; i < VALUES.length; i++) {
						SUPPORTED[i] = fsTypes.contains(VALUES[i]);
					}
					
				} catch (Throwable e) { e.printStackTrace(); }
			}
		}
		
		public Integer getSize() {
			return SIZE;
		}
		
		public String getName(Integer index) {
			return NAMES[index];
		}
		
		public String getValue(Integer index) {
			return VALUES[index];
		}
		
		public String getComment(Integer index) {
			return COMMENTS[index];
		}
		
		public Boolean isSupported(Integer index) {
			return SUPPORTED[index] != null ? SUPPORTED[index] : true;
		}
	}
}
