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

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.View;
import android.widget.LinearLayout;

import com.spazedog.mounts2sd.R;

public class ExtendedLayout extends LinearLayout {
	
	public static interface OnMeasure {
		public void spec(View view, Integer height, Integer width);
	}
	
	private OnMeasure mOnMeasure;
	
	private Integer mMaxWidth;
	private Integer mMaxHeight;
	private Integer mWidth;
	private Integer mHeight;
	
	public ExtendedLayout(Context context) {
		super(context);
		
		mMaxWidth = Integer.MAX_VALUE;
		mMaxHeight = Integer.MAX_VALUE;
		mWidth = 0;
		mHeight = 0;
	}
	
	public ExtendedLayout(Context context, AttributeSet attrs) {
		super(context, attrs);
		
		TypedArray a = context.getTheme().obtainStyledAttributes(attrs, R.styleable.ExtendedView, 0, 0);
		
		try {
			mMaxWidth = a.getDimensionPixelSize(R.styleable.ExtendedView_maxWidth, Integer.MAX_VALUE);
			mMaxHeight = a.getDimensionPixelSize(R.styleable.ExtendedView_maxHeight, Integer.MAX_VALUE);
			
			mWidth = a.getDimensionPixelSize(R.styleable.ExtendedView_width, 0);
			mHeight = a.getDimensionPixelSize(R.styleable.ExtendedView_height, 0);
	       
		} finally {
			a.recycle();
		}
	}
	
	public void setOnMeasure(OnMeasure aOnMeasure) {
		mOnMeasure = aOnMeasure;
	}
	
	public void removeOnMeasure() {
		mOnMeasure = null;
	}
	
	public void setMaxWidth(Integer argWidth) {
		mMaxWidth = argWidth;
		
		invalidate();
		requestLayout();
		refreshDrawableState();
	}

	public void setMaxHeight(Integer argHeight) {
		mMaxHeight = argHeight;
		
		invalidate();
		requestLayout();
		refreshDrawableState();
	}
	
	public void setWidth(Integer argWidth) {
		mWidth = argWidth;
		
		invalidate();
		requestLayout();
		refreshDrawableState();
	}

	public void setHeight(Integer argHeight) {
		mHeight = argHeight;
		
		invalidate();
		requestLayout();
		refreshDrawableState();
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int measuredWidth = MeasureSpec.getSize(widthMeasureSpec), 
        		measuredHeight = MeasureSpec.getSize(heightMeasureSpec), 
        			measureMode;
        
        if (mWidth > 0 || (mMaxWidth > 0 && mMaxWidth < measuredWidth)) {
            measureMode = MeasureSpec.getMode(widthMeasureSpec);
            widthMeasureSpec = MeasureSpec.makeMeasureSpec(mWidth > 0 ? mWidth : mMaxWidth, measureMode);
        }
        
        if (mHeight > 0 || (mMaxHeight > 0 && mMaxHeight < measuredHeight)) {
            measureMode = MeasureSpec.getMode(heightMeasureSpec);
            heightMeasureSpec = MeasureSpec.makeMeasureSpec(mHeight > 0 ? mHeight : mMaxHeight, measureMode);
        }
        
        if (mOnMeasure != null) {
        	mOnMeasure.spec((View) this, measuredHeight, measuredWidth);
        }
        
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
	}
}
