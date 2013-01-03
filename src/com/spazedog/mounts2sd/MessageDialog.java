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
import android.widget.LinearLayout;
import android.widget.TextView;

public class MessageDialog extends DialogFragment {
	
	private String TITLE;
	private String MESSAGE;
	
	private MessageDialogListener LISTENER;
	
	public interface MessageDialogListener {
		public void onMessageDialogClose();
	}
	
	public MessageDialog addMessage(String title, String message) {
		TITLE = title;
		MESSAGE = message;
		
		return this;
	}
	
	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		super.onCreateDialog(savedInstanceState);
		
		if (savedInstanceState != null) {
			TITLE = savedInstanceState.getString("title");
			MESSAGE = savedInstanceState.getString("message");
		}
		
		LayoutInflater inflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		
		/* Get layout and fit the width */
		Rect display = new Rect();
		getActivity().getWindow().getDecorView().getWindowVisibleDisplayFrame(display);
		View layout = inflater.inflate(R.layout.dialog_message, null);
		layout.setMinimumWidth( display.width() < 1000 ? display.width() : 1000 );
		
		LayoutParams params = new LayoutParams();
		params.width = display.width() < 1000 ? display.width() : 1000;
		
		/* Create a new dialog with no titlebar, borders or background and attach the layout */
		Dialog dialog = (Dialog) new Dialog(getActivity());
		dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
		dialog.addContentView(layout, params);
		dialog.getWindow().setBackgroundDrawable(new ColorDrawable(0));
		
		/* ------------------------------------------------
		 * Lets start populate the dialog */
		
		((TextView) dialog.findViewById(R.id.dialog_message_title_c127d69c)).setText(TITLE);
		((TextView) dialog.findViewById(R.id.dialog_message_text_c127d69c)).setText(MESSAGE);

		((LinearLayout) dialog.findViewById(R.id.dialog_message_btn_layout_c127d69c)).setOnTouchListener(new OnTouchListener() {
			public boolean onTouch(View v, MotionEvent event) {
				if(event.getActionMasked() == MotionEvent.ACTION_DOWN) {
					((LinearLayout) ((LinearLayout) v).findViewById(R.id.dialog_message_btn_wrapper_c127d69c)).setBackgroundColor( getResources().getColor(R.color.light_gray) );
					
				} else if(event.getActionMasked() == MotionEvent.ACTION_UP || event.getActionMasked() == MotionEvent.ACTION_OUTSIDE || event.getActionMasked() == MotionEvent.ACTION_CANCEL) {
					((LinearLayout) ((LinearLayout) v).findViewById(R.id.dialog_message_btn_wrapper_c127d69c)).setBackgroundDrawable(null);
					
					if (event.getActionMasked() == MotionEvent.ACTION_UP) {
						LISTENER.onMessageDialogClose();
						MessageDialog.this.dismiss();
					}
				}
				
				return false;
			}
		});
		
		return dialog;
	}
	
	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		
		LISTENER = (MessageDialogListener) activity;
	}
	
	@Override
	public void onSaveInstanceState(Bundle savedInstanceState) {
		savedInstanceState.putString("title", TITLE);
		savedInstanceState.putString("message", MESSAGE);
		
		super.onSaveInstanceState(savedInstanceState);
	}
}
