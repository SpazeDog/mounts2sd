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

package com.spazedog.mounts2sd;

import java.lang.ref.WeakReference;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.spazedog.mounts2sd.tools.ExtendedLayout;
import com.spazedog.mounts2sd.tools.ExtendedLayout.OnMeasure;
import com.spazedog.mounts2sd.tools.Preferences;
import com.spazedog.mounts2sd.tools.ViewEventHandler;
import com.spazedog.mounts2sd.tools.ViewEventHandler.ViewClickListener;
import com.spazedog.mounts2sd.tools.interfaces.IDialogConfirmResponse;
import com.spazedog.mounts2sd.tools.interfaces.IDialogCustomLayout;
import com.spazedog.mounts2sd.tools.interfaces.IDialogListener;
import com.spazedog.mounts2sd.tools.interfaces.IDialogMessageResponse;

public class FragmentDialog extends DialogFragment implements OnMeasure, ViewClickListener {
	
	private Bundle mArguments;
	private Bundle mExtra;
	
	private IDialogListener mListener;
	
	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		
		if (getArguments().getBoolean("fragment")) {
			mListener = (IDialogListener) getParentFragment();
			
		} else {
			mListener = (IDialogListener) activity;
		}
	}
	
	@Override
	public void onSaveInstanceState(Bundle savedInstanceState) {
		savedInstanceState.putBundle("extra", mExtra);

		super.onSaveInstanceState(savedInstanceState);
	}
	
	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		super.onCreateDialog(savedInstanceState);
		
		mArguments = getArguments();
		
		if (savedInstanceState != null) {
			mExtra = savedInstanceState.getBundle("extra");
			
		} else {
			mExtra = mArguments.getBundle("extra");
			
			if (mExtra == null) {
				mExtra = new Bundle();
			}
		}
		
		LayoutInflater inflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		Dialog dialog = (Dialog) new Dialog(getActivity(), Preferences.getInstance(getActivity()).theme());
		
		dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
		dialog.getWindow().setBackgroundDrawable(new ColorDrawable(0));
		dialog.setCanceledOnTouchOutside(false);
		
		ExtendedLayout layout = null;
		
		if (mArguments.getString("type").equals("message")) {
			layout = (ExtendedLayout) inflater.inflate(R.layout.dialog_message, null);
			layout.findViewById(R.id.dialog_close_button).setOnTouchListener(new ViewEventHandler(this));
			
		} else {
			layout = (ExtendedLayout) inflater.inflate(R.layout.dialog_confirm, null);
			layout.findViewById(R.id.dialog_cancel_button).setOnTouchListener(new ViewEventHandler(this));
			layout.findViewById(R.id.dialog_okay_button).setOnTouchListener(new ViewEventHandler(this));
		}
		
		((TextView) layout.findViewById(R.id.dialog_title)).setText(mArguments.getString("title"));
		
		if (mArguments.getBoolean("custom")) {
			ViewGroup scroller = (ViewGroup) layout.findViewById(R.id.dialog_textbox).getParent();
			
			scroller.removeView(layout.findViewById(R.id.dialog_textbox));
			scroller.addView(
				((IDialogCustomLayout) mListener).onDialogCreateView(mArguments.getString("tag"), inflater, scroller, mExtra)
			);
			
		} else {
			((TextView) layout.findViewById(R.id.dialog_textbox)).setText(mArguments.getString("message"));
		}
		
		layout.setOnMeasure(this);

		dialog.addContentView(layout, new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT));
		
		return dialog;
	}
	
	@Override
	public void spec(View view, Integer height, Integer width) {
		
		((ExtendedLayout) view).removeOnMeasure();
		
		float density = getResources().getDisplayMetrics().density;
		Integer newWidthMargin = Math.round((float) 28 * density); // 14 * 2
		Integer newHeightMargin = Math.round((float) 28 * density); // 25 * 2
		Integer newWidth = Math.round((float) 600 * density);
		Integer newHeight = height - newHeightMargin;
		
		if (width < newWidth) {
			newWidth = width - newWidthMargin;
			
		} else {
			newWidth -= newWidthMargin;
		}
		
		if (getResources().getString(R.string.config_screen_type).equals("xlarge")) {
			Integer testHeight = width * 2;
			
			if (height > width && testHeight < height) {
				newHeight = testHeight;
				
				if ((height - testHeight) < newHeightMargin) {
					newHeight -= (newHeightMargin - (height - testHeight));
				}
			}
		}
		
		ExtendedLayout layout = (ExtendedLayout) ((ViewGroup) view).getChildAt(0);
		layout.setLayoutParams(new LinearLayout.LayoutParams(newWidth, LinearLayout.LayoutParams.WRAP_CONTENT));
		layout.setMaxHeight(newHeight);
	}
	
	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		if (mArguments.getBoolean("custom")) {
			((IDialogCustomLayout) mListener).onDialogViewCreated(mArguments.getString("tag"), view, mExtra);
		}
	}

	@Override
	public void onViewClick(View v) {
		dismiss();
		
		if (mArguments.getString("type").equals("message")) {
			((IDialogMessageResponse) mListener).onDialogClose(mArguments.getString("tag"), mArguments.getBoolean("quit"), mExtra);
			
		} else {
			((IDialogConfirmResponse) mListener).onDialogConfirm(mArguments.getString("tag"), v.getId() == R.id.dialog_okay_button, mExtra);
		}
	}
	
	public static class Builder {
		private WeakReference<IDialogListener> mListener;
		private String mTag;
		private Bundle mArguments = new Bundle();
		
		public Builder(IDialogListener listener, String tag, String title, Bundle extra) {
			mListener = new WeakReference<IDialogListener>(listener);
			
			mArguments.putString("tag", (mTag = tag));
			mArguments.putString("title", title);
			mArguments.putBundle("extra", extra);
		}
		
		public FragmentDialog showCustomConfirmDialog() {
			mArguments.putString("type", "confirm");
			mArguments.putBoolean("custom", true);
			
			return show();
		}
		
		public FragmentDialog showConfirmDialog(String message) {
			mArguments.putString("type", "confirm");
			mArguments.putString("message", message);
			
			return show();
		}
		
		public FragmentDialog showCustomMessageDialog(Boolean quitOnClose) {
			mArguments.putString("type", "message");
			mArguments.putBoolean("quit", quitOnClose);
			mArguments.putBoolean("custom", true);
			
			return show();
		}
		
		public FragmentDialog showMessageDialog(String message, Boolean quitOnClose) {
			mArguments.putString("type", "message");
			mArguments.putString("message", message);
			mArguments.putBoolean("quit", quitOnClose);
			
			return show();
		}
		
		private FragmentDialog show() {
			FragmentManager manager = mListener.get() instanceof Fragment ? 
					((Fragment) mListener.get()).getChildFragmentManager() : 
						((FragmentActivity) mListener.get()).getSupportFragmentManager();
			
			if (manager.findFragmentByTag(mTag) == null) {
				DialogFragment dialog = new FragmentDialog();
				FragmentTransaction transaction = manager.beginTransaction();
				
				if (mListener.get() instanceof Fragment) {
					mArguments.putBoolean("fragment", true);
				}
				
				dialog.setArguments(mArguments);
				
				transaction.add(dialog, mTag);
				transaction.commitAllowingStateLoss();
				
				return (FragmentDialog) dialog;
			}
			
			return null;
		}
	}
}
