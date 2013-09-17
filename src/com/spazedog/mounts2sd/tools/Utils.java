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
import android.content.pm.PackageManager;
import android.os.Environment;
import android.os.StatFs;
import android.view.View;
import android.view.ViewGroup;

import com.spazedog.mounts2sd.R;

public class Utils {
	public static String getSelectorValue(Context context, String aSelector, String aValue) {
		Integer iSelectorNames = context.getResources().getIdentifier("selector_" + aSelector + "_names", "array", context.getPackageName());
		Integer iSelectorValues = context.getResources().getIdentifier("selector_" + aSelector + "_values", "array", context.getPackageName());
		
		if (iSelectorNames != 0 && iSelectorValues != 0) {
			String[] lSelectorNames = context.getResources().getStringArray(iSelectorNames);
			String[] lSelectorValues = context.getResources().getStringArray(iSelectorValues);
			
			for (int i=0; i < lSelectorValues.length; i++) {
				if (lSelectorValues[i].equals(aValue)) {
					return lSelectorNames[i];
				}
			}
		}
		
		return context.getResources().getString(R.string.status_unknown);
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
		public static interface IRelayMessageReceiver {
			public abstract void onMessageReceive(String tag, String message, Message visibilityController);
			public abstract void onMessageRemove(String tag, Boolean retainState);
			public abstract void onMessageVisibilityUpdate();
		}
		
		public static abstract class Message {
			private static WeakReference<IRelayMessageReceiver> mReceiver;
			
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
			
			public static void triggerVisibilityUpdate() {
				if (mReceiver != null && mReceiver.get() != null) {
					mReceiver.get().onMessageVisibilityUpdate();
				}
			}
			
			public static void setReceiver(IRelayMessageReceiver receiver) {
				mReceiver = new WeakReference<IRelayMessageReceiver>(receiver);
			}
			
			public Boolean onVisibilityChange(Context context, Integer tabId, Boolean visible) {
				return true;
			}
		}
	}
}
