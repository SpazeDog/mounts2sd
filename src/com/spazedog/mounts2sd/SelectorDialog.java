package com.spazedog.mounts2sd;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.graphics.Rect;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.Window;
import android.view.WindowManager.LayoutParams;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

public class SelectorDialog extends DialogFragment {

	private String PROP_TITLE;
	private String PROP_SELECTOR_TYPE;
	private String PROP_SELECTOR_VALUE;
	private String PROP_SELECTOR_ID;
	
	private Boolean USER_SELECTOR_ACTION;
	
	private SelectorListener LISTENER;
	
	private String ITEM_NAMES[];
	private String ITEM_VALUES[];
	private LinearLayout ITEMS[];
	
	public interface SelectorListener {
		public void onSelectorChange(SelectorChoice choice);
	}
	
	class SelectorChoice {
		private String TYPE;
		private String VALUE;
		private String ID;
		private Boolean ACTION;
		
		public SelectorChoice(String type, String value, String id, Boolean action) {
			TYPE = type;
			VALUE = value;
			ID = id;
			ACTION = action;
		}
		
		public String getType() { return TYPE; }
		public String getValue() { return VALUE; }
		public String getId() { return ID; }
		public Boolean getAction() { return ACTION; }
	}
	
	public SelectorDialog addSelector(String type, String title, String value, String id) {
		PROP_TITLE = title;
		PROP_SELECTOR_TYPE = type;
		PROP_SELECTOR_VALUE = value;
		PROP_SELECTOR_ID = id;
		
		return this;
	}
	
	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		super.onCreateDialog(savedInstanceState);
		
		if (savedInstanceState != null) {
			PROP_TITLE = savedInstanceState.getString("prop/title");
			PROP_SELECTOR_TYPE = savedInstanceState.getString("prop/type");
			PROP_SELECTOR_ID = savedInstanceState.getString("prop/id");
			PROP_SELECTOR_VALUE = savedInstanceState.getString("prop/value");
		}
		
		LayoutInflater inflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		
		/* Get layout and fit the width */
		Rect display = new Rect();
		getActivity().getWindow().getDecorView().getWindowVisibleDisplayFrame(display);
		View layout = inflater.inflate(R.layout.dialog_selector, null);
		layout.setMinimumWidth( display.width() < 1000 ? display.width() : 1000 );
		
		/* Create a new dialog with no titlebar, borders or background and attach the layout */
		Dialog dialog = (Dialog) new Dialog(getActivity());
		dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
		dialog.addContentView(layout, new LayoutParams());
		dialog.getWindow().setBackgroundDrawable(new ColorDrawable(0));
		
		/* ------------------------------------------------
		 * Lets start populate the dialog */
		
		ITEM_NAMES = getResources().getStringArray(
				PROP_SELECTOR_TYPE.equals("switch") ? R.array.selector_switch_names : 
					PROP_SELECTOR_TYPE.equals("filesystem") ? R.array.selector_filesystem_names : R.array.selector_readahead_names
		);
		
		ITEM_VALUES = getResources().getStringArray(
				PROP_SELECTOR_TYPE.equals("switch") ? R.array.selector_switch_values : 
					PROP_SELECTOR_TYPE.equals("filesystem") ? R.array.selector_filesystem_values : R.array.selector_readahead_values
		);
		
		ITEMS = new LinearLayout[ITEM_NAMES.length];
		
		LinearLayout wrapper = (LinearLayout) dialog.findViewById(R.id.dialog_selector_wrapper_e7f45199);
		
		((TextView) dialog.findViewById(R.id.dialog_selector_title_8d4e10e5)).setText(PROP_TITLE);
		
		for(int i=0; i < ITEM_NAMES.length; i++) {
			ITEMS[i] = (LinearLayout) inflater.inflate(R.layout.dialog_selector_item, null);
			
			ITEMS[i].setOnTouchListener(new OnTouchListener() {
				public boolean onTouch(View v, MotionEvent event) {
					if(event.getActionMasked() == MotionEvent.ACTION_DOWN) {
						SelectorDialog.this.onItemEvent(v, MotionEvent.ACTION_DOWN);
						
					} else if(event.getActionMasked() == MotionEvent.ACTION_UP || event.getActionMasked() == MotionEvent.ACTION_OUTSIDE || event.getActionMasked() == MotionEvent.ACTION_CANCEL) {
						SelectorDialog.this.onItemEvent(v, MotionEvent.ACTION_CANCEL);
						
						if (event.getActionMasked() == MotionEvent.ACTION_UP) {
							SelectorDialog.this.onItemEvent(v, MotionEvent.ACTION_UP);
						}
					}
					
					return false;
				}
			});
			
			((TextView) ITEMS[i].findViewById(R.id.item_text)).setText(ITEM_NAMES[i]);
			((ImageView) ITEMS[i].findViewById(R.id.item_image)).setImageResource( ((String) ITEM_VALUES[i]).equals(PROP_SELECTOR_VALUE) ? R.drawable.btn_radio_on : R.drawable.btn_radio_off );
			
			wrapper.addView( ITEMS[i] );
		}
		
		/* ------------------------------------------------
		 * Lets work on the buttons */
		
		Integer btn[] = {R.id.dialog_selector_btn_cancel_1aa48a56, R.id.dialog_selector_btn_okay_e91793b2};
		
		for (int i=0; i < btn.length; i++) {
			dialog.findViewById(btn[i]).setOnTouchListener(new OnTouchListener() {
				public boolean onTouch(View v, MotionEvent event) {
					if(event.getActionMasked() == MotionEvent.ACTION_DOWN) {
						SelectorDialog.this.onBtnEvent(v, MotionEvent.ACTION_DOWN);
						
					} else if(event.getActionMasked() == MotionEvent.ACTION_UP || event.getActionMasked() == MotionEvent.ACTION_OUTSIDE || event.getActionMasked() == MotionEvent.ACTION_CANCEL) {
						SelectorDialog.this.onBtnEvent(v, MotionEvent.ACTION_CANCEL);
						
						if (event.getActionMasked() == MotionEvent.ACTION_UP) {
							USER_SELECTOR_ACTION = v.getId() == R.id.dialog_selector_btn_cancel_1aa48a56 ? false : true;
							
							SelectorDialog.this.onBtnEvent(v, MotionEvent.ACTION_UP);
						}
					}
					
					return false;
				}
			});
		}
		
		return dialog;
	}
	
	public void onItemEvent(View v, Integer event) {
		if (event == MotionEvent.ACTION_DOWN) {
			((LinearLayout) ((LinearLayout) v).findViewById(R.id.item_layout)).setBackgroundColor( getResources().getColor(R.color.light_gray) );
			
		} else if (event == MotionEvent.ACTION_CANCEL) {
			((LinearLayout) ((LinearLayout) v).findViewById(R.id.item_layout)).setBackgroundDrawable(null);
			
		} else {
			for(int i=0; i < ITEMS.length; i++) {
				((ImageView) ITEMS[i].findViewById(R.id.item_image)).setImageResource( ITEMS[i] == v ? R.drawable.btn_radio_on : R.drawable.btn_radio_off );
				
				if (ITEMS[i] == v) {
					PROP_SELECTOR_VALUE = ITEM_VALUES[i];
				}
			}
		}
	}
	
	public void onBtnEvent(View v, Integer event) {
		if (event == MotionEvent.ACTION_DOWN) {
			((LinearLayout) ((LinearLayout) v).findViewById(R.id.dialog_selector_btn_layout)).setBackgroundColor( getResources().getColor(R.color.light_gray) );
			
		} else if (event == MotionEvent.ACTION_CANCEL) {
			((LinearLayout) ((LinearLayout) v).findViewById(R.id.dialog_selector_btn_layout)).setBackgroundDrawable(null);
			
		} else {
			LISTENER.onSelectorChange(new SelectorChoice(PROP_SELECTOR_TYPE, PROP_SELECTOR_VALUE, PROP_SELECTOR_ID, USER_SELECTOR_ACTION));

			dismiss();
		}
	}
	
	@Override
	public void onSaveInstanceState(Bundle savedInstanceState) {
		savedInstanceState.putString("prop/title", PROP_TITLE);
		savedInstanceState.putString("prop/type", PROP_SELECTOR_TYPE);
		savedInstanceState.putString("prop/id", PROP_SELECTOR_ID);
		savedInstanceState.putString("prop/value", PROP_SELECTOR_VALUE);
		
		super.onSaveInstanceState(savedInstanceState);
	}
	
	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		
		LISTENER = (SelectorListener) activity;
	}
}
