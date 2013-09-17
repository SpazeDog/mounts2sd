package com.spazedog.mounts2sd.tools.containers;

import android.content.Context;

import com.spazedog.mounts2sd.R;
import com.spazedog.mounts2sd.tools.Common;

public abstract class IApplicationSession {
	private static Boolean mUnlocked;
	
	protected abstract Context getContext();
	
	public Boolean is_unlocked() {
		if (mUnlocked == null) {
			mUnlocked = getContext().getResources().getBoolean(R.bool.config_unlocked) 
					|| Common.checkSignatures(getContext(), "com.spazedog.mounts2sd.unlock");
		}
		
		return mUnlocked;
	}
}
