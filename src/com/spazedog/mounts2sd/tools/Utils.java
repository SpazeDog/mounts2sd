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

package com.spazedog.mounts2sd.tools;

import java.lang.ref.WeakReference;

import android.content.Context;
import android.os.Environment;
import android.os.StatFs;
import android.view.View;
import android.view.ViewGroup;

import com.spazedog.mounts2sd.R;

public class Utils {
	
	private static final String[] mPrifixes = {"b","Kb","Mb","Gb"};
	
    public static String convertPrifix(double iNum) {
        String lPrifix = mPrifixes[0];
        double iCal = (double) iNum;
        double iDevide = 1024D;

        for (int i=1; i < mPrifixes.length; i++) {
                if (iCal < iDevide) {
                        break;
                }

                iCal = iCal/iDevide;
                lPrifix = mPrifixes[i];
        }

        return "" + (Math.round(iCal*100.0)/100.0) + lPrifix;
	}
	
	public static double getDiskUsage(String dir) {
		try {
	        StatFs stat = new StatFs(dir);
	        double result = ((double) stat.getBlockCount() - (double) stat.getAvailableBlocks()) * (double) stat.getBlockSize();
	
	        return result;
	        
		} catch (Exception e) {
			return 0;
		}
	}
	
	public static double getDiskTotal(String dir) {
		try {
	        StatFs stat = new StatFs(dir);
	        double result = (double) stat.getBlockCount() * (double) stat.getBlockSize();
	
	        return result;
	        
		} catch (Exception e) {
			return 0;
		}
	}
	
	public static void removeView(View aView, Boolean aGroup) {
		ViewGroup group = (ViewGroup) ((ViewGroup) aView).getParent();
		
		if (!aGroup) {
			for (int i=0, x=0; i < group.getChildCount(); i++) {
				if (group.getChildAt(i) == aView) {
					group.removeView(group.getChildAt(i));
					
					if ((group.getChildAt( (x=i) ) != null && group.getChildAt(x).getId() == R.id.item_divider) || group.getChildAt( (x=i-1) ).getId() == R.id.item_divider) {
						group.removeView(group.getChildAt(x));
					}
				}
			}
			
		} else {
			((ViewGroup) group.getParent()).removeView(group);
		}
	}
	
	public static class Relay {
		public static interface MessageReceiver {
			public abstract void onMessageReceive(String tag, String message, Message visibilityController);
			public abstract void onMessageRemove(String tag, Boolean retainState);
		}
		
		public static abstract class Message {
			private static WeakReference<MessageReceiver> mReceiver;
			
			public static void add(String tag, String message, Message visibilityController) {
				if (mReceiver != null && mReceiver.get() != null) {
					mReceiver.get().onMessageReceive(tag, message, visibilityController);
				}
			}
			
			public static void remove(String tag, Boolean retainState) {
				if (mReceiver != null && mReceiver.get() != null) {
					mReceiver.get().onMessageRemove(tag, retainState);
				}
			}
			
			public static void setReceiver(MessageReceiver receiver) {
				mReceiver = new WeakReference<MessageReceiver>(receiver);
			}
			
			public Boolean onVisibilityChange(Context context, Integer tabId, Boolean visible) {
				return true;
			}
		}
	}
	
	public static String sdcardStateMessage(Context context) {
	    String sdcardStatus = Environment.getExternalStorageState();

	    if (sdcardStatus.equals(Environment.MEDIA_MOUNTED_READ_ONLY)) {
	    	return context.getResources().getString(R.string.sdcard_state_ro);

	    } else if (sdcardStatus.equals(Environment.MEDIA_NOFS)) {
	    	return context.getResources().getString(R.string.sdcard_state_format);

	    } else if (sdcardStatus.equals(Environment.MEDIA_REMOVED)) {
	    	return context.getResources().getString(R.string.sdcard_state_missing);

	    } else if (sdcardStatus.equals(Environment.MEDIA_SHARED)) {
	    	return context.getResources().getString(R.string.sdcard_state_ums);

	    } else if (sdcardStatus.equals(Environment.MEDIA_UNMOUNTABLE)) {
	    	return context.getResources().getString(R.string.sdcard_state_mount_failure);

	    } else if (sdcardStatus.equals(Environment.MEDIA_UNMOUNTED)) {
	    	return context.getResources().getString(R.string.sdcard_state_mount);
	    }

	    return null;
	}
}
