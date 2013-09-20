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

import android.graphics.Rect;
import android.os.Handler;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;

public class ViewEventHandler implements OnTouchListener {
	
	private abstract class OnTouchRunnable implements Runnable {
		protected View mView;
		protected Integer mDelay = 150;
		
		public void begin(View aView) {
			mView = aView;
			mHandler.postDelayed(this, mDelay);
		}
		
		public void cancel() {
			mView = null;
			mHandler.removeCallbacks(this);
		}
	}
	
	private OnTouchRunnable mStatePressedRunnable = new OnTouchRunnable() {
		@Override
		public void begin(View aView) {
			mDelay = 250;
					
			super.begin(aView);
			
			mView.setPressed(false);
		}
		
		@Override
		public void run() {
			if (mView != null) {
				mView.setPressed(true);
				mView = null;
			}
			
			super.cancel();
		}
	};
	
	private OnTouchRunnable mStateClickedRunnable = new OnTouchRunnable() {
		@Override
		public void begin(View aView) {
			super.begin(aView);
			
			mView.setPressed(true);
		}
		
		@Override
		public void run() {
			mListener.onViewClick(mView);
			
			mView.setPressed(false);
			
			super.cancel();
		}
	};
	
	private Handler mHandler = new Handler();
	
	private ViewClickListener mListener;
	
	private Boolean mEventDown = false;
	
	private Rect mRect;
	
	public static interface ViewClickListener {
		public void onViewClick(View v);
	}
	
	public ViewEventHandler(ViewClickListener aListener) {
		mListener = aListener;
	}

	@Override
	public boolean onTouch(View v, MotionEvent event) {
		if(event.getActionMasked() == MotionEvent.ACTION_DOWN) {
			mStatePressedRunnable.begin(v);
			
			mEventDown = true;
			mRect = new Rect(v.getLeft(), v.getTop(), v.getRight(), v.getBottom());
			
			return true;
			
			/* ACTION_CANCEL, ACTION_OUTSIDE and such is not used with newer Android versions, 
			 * here we use the mRect check instead. However, you need the other in order to fix conflicts with older versions. 
			 */
		} else if(mEventDown && ( event.getActionMasked() == MotionEvent.ACTION_UP || event.getActionMasked() == MotionEvent.ACTION_CANCEL || event.getActionMasked() == MotionEvent.ACTION_SCROLL || event.getActionMasked() == MotionEvent.ACTION_OUTSIDE || !mRect.contains(v.getLeft() + (int) event.getX(), v.getTop() + (int) event.getY()) )) {
			mStatePressedRunnable.cancel();
			
			if(mEventDown && event.getActionMasked() == MotionEvent.ACTION_UP) {
				mEventDown = false;
				mStateClickedRunnable.begin(v); 
				
				return true;
			}
			
			v.setPressed( (mEventDown = false) );
		}
		
		return false;
	}
}
