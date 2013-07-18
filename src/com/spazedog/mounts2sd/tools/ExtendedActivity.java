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

import java.util.ArrayList;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;

public class ExtendedActivity extends FragmentActivity {
	
	private final static ArrayList<String> mActivities = new ArrayList<String>();
	
	private final static Object oLock = new Object();
	
	private Boolean mReCreate = false;
	
	@Override
	public void onSaveInstanceState(Bundle savedInstanceState) {
		savedInstanceState.putBoolean("mReCreate", true);

		super.onSaveInstanceState(savedInstanceState);
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		if (savedInstanceState != null) {
			mReCreate = savedInstanceState.getBoolean("mReCreate");
		}
		
		synchronized (oLock) {
			mActivities.add( (mReCreate ? mActivities.size() : 0), this.getClass().getName());
		}
	}
	
	@Override
	protected void onDestroy() {
		synchronized (oLock) {
			mActivities.remove(this.getClass().getName());
		}
		
		super.onDestroy();
	}
	
	public final Boolean isForeground() {
		synchronized (oLock) {
			return mActivities.size() == 0 || mActivities.get(0).equals(this.getClass().getName());
		}
	}
	
	public final Boolean isRecreated() {
		return mReCreate;
	}
}
