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

package com.spazedog.mounts2sd.tools.containers;

import android.content.Context;

public abstract class MessageItem {
	
	private String mTag;
	private String mMessage;
	
	public MessageItem(String aTag, String aMessage) {
		mTag = aTag;
		mMessage = aMessage;
	}
	
	public final String tag() {
		return mTag;
	}
	
	public final String message() {
		return mMessage;
	}
	
	public Boolean onVisibilityChange(Context context, Integer tabId, Boolean visible) {
		return true;
	}
}
