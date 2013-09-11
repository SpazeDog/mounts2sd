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

import com.spazedog.lib.rootfw3.RootFW;
import com.spazedog.lib.rootfw3.extenders.MemoryExtender.MemStat;
import com.spazedog.mounts2sd.tools.ExtendedLayout;
import com.spazedog.mounts2sd.tools.ExtendedLayout.OnMeasure;
import com.spazedog.mounts2sd.tools.Preferences;
import com.spazedog.mounts2sd.tools.Root;
import com.spazedog.mounts2sd.tools.Utils;
import com.spazedog.mounts2sd.tools.ViewEventHandler;
import com.spazedog.mounts2sd.tools.ViewEventHandler.ViewClickListener;
import com.spazedog.mounts2sd.tools.interfaces.DialogConfirmResponse;
import com.spazedog.mounts2sd.tools.interfaces.DialogListener;
import com.spazedog.mounts2sd.tools.interfaces.DialogMessageResponse;
import com.spazedog.mounts2sd.tools.interfaces.DialogSelectorResponse;

public class FragmentDialog extends DialogFragment implements OnMeasure, ViewClickListener {
	
	private Bundle mArguments;
	
	private DialogListener mListener;
	
	private String mSelected;
	
	private static Double oDataSize;
	private static Double oMeminfo;
	
	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		super.onCreateDialog(savedInstanceState);
		
		if (savedInstanceState != null) {
			mSelected = savedInstanceState.getString("mSelected");
		}
		
		mArguments = getArguments();
		
		LayoutInflater inflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		Dialog dialog = (Dialog) new Dialog(getActivity(), new Preferences(getActivity()).theme());
		
		dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
		dialog.getWindow().setBackgroundDrawable(new ColorDrawable(0));
		dialog.setCanceledOnTouchOutside(false);
		
		ExtendedLayout layout = null;
		
		if (mArguments.getString("type").equals("message")) {
			layout = (ExtendedLayout) inflater.inflate(R.layout.dialog_message, null);
			
			layout.findViewById(R.id.dialog_close_button).setOnTouchListener(new ViewEventHandler(this));
			
			((TextView) layout.findViewById(R.id.dialog_textbox)).setText(mArguments.getString("message"));
			
		} else if (mArguments.getString("type").equals("confirm")) {
			layout = (ExtendedLayout) inflater.inflate(R.layout.dialog_confirm, null);
			
			layout.findViewById(R.id.dialog_cancel_button).setOnTouchListener(new ViewEventHandler(this));
			layout.findViewById(R.id.dialog_okay_button).setOnTouchListener(new ViewEventHandler(this));
			
			((TextView) layout.findViewById(R.id.dialog_textbox)).setText(mArguments.getString("message"));
			
		} else {
			layout = (ExtendedLayout) inflater.inflate(R.layout.dialog_selector, null);
			
			layout.findViewById(R.id.dialog_cancel_button).setOnTouchListener(new ViewEventHandler(this));
			layout.findViewById(R.id.dialog_okay_button).setOnTouchListener(new ViewEventHandler(this));
			
			inflateSelector( layout.findViewById(R.id.dialog_placeholder) );
		}
		
		layout.setOnMeasure(this);

		dialog.addContentView(layout, new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT));
		
		((TextView) dialog.findViewById(R.id.dialog_title)).setText(mArguments.getString("title"));
		
		return dialog;
	}
	
	@Override
	public void onSaveInstanceState(Bundle savedInstanceState) {
		if (mSelected != null) {
			savedInstanceState.putString("mSelected", mSelected);
		}

		super.onSaveInstanceState(savedInstanceState);
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
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		
		if (getArguments().getBoolean("fragment")) {
			mListener = (DialogListener) getParentFragment();
			
		} else {
			mListener = (DialogListener) activity;
		}
	}

	@Override
	public void onViewClick(View v) {
		if (mArguments.getString("type").equals("message")) {
			dismiss();
			
			((DialogMessageResponse) mListener).onDialogClose(mArguments.getString("tag"), mArguments.getBoolean("quit"));
		
		} else if (mArguments.getString("type").equals("confirm")) {
			dismiss();
			
			((DialogConfirmResponse) mListener).onDialogConfirm(mArguments.getString("tag"), v.getId() == R.id.dialog_okay_button);

		} else {
			if (v.getId() == R.id.dialog_cancel_button || v.getId() == R.id.dialog_okay_button) {
				dismiss();
				
				if (v.getId() == R.id.dialog_okay_button && mSelected != null) {
					((DialogSelectorResponse) mListener).onDialogSelect(mArguments.getString("tag"), mSelected);
				}
				
			} else {
				ViewGroup view = (ViewGroup) v.getParent();
				
				for (int i=0; i < view.getChildCount(); i++) {
					View child = view.getChildAt(i);
					
					if (child == v) {
						child.setSelected(true);
								
						mSelected = (String) child.getTag();
						
					} else {
						child.setSelected(false);
					}
				}
			}
		}
	}
	
	private void inflateSelector(View view) {
		String name = mArguments.getString("selector");
		String defValue = mArguments.getString("defValue");
		String[] enabledValues = mArguments.getStringArray("enabledValues");
		
		Integer iSelectorNames = getResources().getIdentifier("selector_" + name + "_names", "array", getActivity().getPackageName());
		Integer iSelectorValues = getResources().getIdentifier("selector_" + name + "_values", "array", getActivity().getPackageName());
		Integer iSelectorComments = getResources().getIdentifier("selector_" + name + "_comments", "array", getActivity().getPackageName());
		
		LayoutInflater inflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		
		if (iSelectorNames != 0 && iSelectorValues != 0) {
			String[] lSelectorNames = getResources().getStringArray(iSelectorNames);
			String[] lSelectorValues = getResources().getStringArray(iSelectorValues);
			String[] lSelectorComments = iSelectorComments != 0 ? getResources().getStringArray(iSelectorComments) : new String[lSelectorNames.length];
			
			for (int i=0; i < lSelectorNames.length; i++) {
				ViewGroup itemView = (ViewGroup) inflater.inflate(R.layout.inflate_selector_item, (ViewGroup) view, false);
				Boolean enabled = true;
				
				if (enabledValues != null) {
					for (int x=0; x < enabledValues.length; x++) {
						if (enabledValues[x].equals(lSelectorValues[i])) {
							enabled = true; break;
						}
						
						enabled = false;
					}
				}
				
				if (name.equals("threshold")) {
					if (oDataSize == null) {
						oDataSize = Utils.getDiskTotal("/data");
					}

					lSelectorComments[i] = Utils.convertPrifix((oDataSize * (Double.parseDouble(lSelectorValues[i]) / 100)));
					
				} else if (name.equals("zram")) {
					if (oMeminfo == null) {
						RootFW rootfw = Root.open();
						MemStat memstat = rootfw.memory().getUsage();
						oMeminfo = 0D;
						
						if (memstat != null) {
							oMeminfo = memstat.memTotal().doubleValue();
							
						} else {
							oMeminfo = 0D;
						}
						
						Root.close();
					}
					
					lSelectorComments[i] = Utils.convertPrifix((oMeminfo * (Double.parseDouble(lSelectorValues[i]) / 100)));
				}
				
				((TextView) itemView.findViewById(R.id.item_name)).setText(lSelectorNames[i]);
				
				if (lSelectorComments[i] != null && !lSelectorComments[i].equals("")) {
					((TextView) itemView.findViewById(R.id.item_description)).setText(lSelectorComments[i]);
				}
				
				itemView.setSelected( mSelected != null ? lSelectorValues[i].equals(mSelected) : lSelectorValues[i].equals(defValue) );
				itemView.setEnabled(enabled);
				itemView.setTag(lSelectorValues[i]);
				itemView.setOnTouchListener(new ViewEventHandler(this));
				
				if (i > 0) {
					inflater.inflate(R.layout.inflate_selector_divider, (ViewGroup) view);
				}
				
				((ViewGroup) view).addView(itemView);
			}
		}
	}
	
	public static class Builder {
		
		private WeakReference<DialogListener> mListener;
		private String mTag;
		private Bundle mArguments = new Bundle();
		
		public Builder(DialogListener listener, String tag, String title) {
			mListener = new WeakReference<DialogListener>(listener);
			
			mArguments.putString("tag", (mTag = tag));
			mArguments.putString("title", title);
		}
		
		public FragmentDialog showConfirmDialog(String message) {
			mArguments.putString("type", "confirm");
			mArguments.putString("message", message);
			
			return show();
		}
		
		public FragmentDialog showMessageDialog(String message) {
			return showMessageDialog(message, false);
		}
		
		public FragmentDialog showMessageDialog(String message, Boolean quitOnClose) {
			mArguments.putString("type", "message");
			mArguments.putString("message", message);
			mArguments.putBoolean("quit", quitOnClose);
			
			return show();
		}
		
		public FragmentDialog showSelectorDialog(String selector, String defaultValue) {
			return showSelectorDialog(selector, defaultValue, null);
		}
		
		public FragmentDialog showSelectorDialog(String selector, String defaultValue, String[] enabledValues) {
			mArguments.putString("type", "selector");
			mArguments.putString("selector", selector);
			mArguments.putString("defValue", defaultValue);
			mArguments.putStringArray("enabledValues", enabledValues);
			
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
