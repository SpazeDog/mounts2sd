package com.spazedog.mounts2sd;

import java.util.HashMap;
import java.util.Map;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.widget.ImageView;
import android.widget.TextView;

import com.spazedog.mounts2sd.MessageDialog.MessageDialogListener;
import com.spazedog.mounts2sd.SelectorDialog.SelectorChoice;
import com.spazedog.mounts2sd.SelectorDialog.SelectorListener;

public class ConfigureActivity extends FragmentActivity implements SelectorListener,MessageDialogListener {
	
	Map<String, View> ELEMENTS = new HashMap<String, View>();
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		BaseApplication.setContext(this);
		
		setContentView(R.layout.activity_configure);
		
		String[] props = SettingsHelper.getPropsCollection();
		View view;
		for (int i=0; i < props.length; i++) {
			if (SettingsHelper.propHasConfig(props[i])) {
				view = findViewById( getResources().getIdentifier( (props[i].replaceAll("\\.", "_") + "_layout_2a010ab8"), "id", "com.spazedog.mounts2sd") );
				
				if (view != null) {
					view.setOnTouchListener(new OnTouchListener() {
						public boolean onTouch(View v, MotionEvent event) {
							if(event.getActionMasked() == MotionEvent.ACTION_DOWN) {
								ConfigureActivity.this.onElementEvent(v, MotionEvent.ACTION_DOWN);
								
							} else if(event.getActionMasked() == MotionEvent.ACTION_UP || event.getActionMasked() == MotionEvent.ACTION_OUTSIDE || event.getActionMasked() == MotionEvent.ACTION_CANCEL) {
								ConfigureActivity.this.onElementEvent(v, MotionEvent.ACTION_CANCEL);
								
								if (event.getActionMasked() == MotionEvent.ACTION_UP) {
									ConfigureActivity.this.onElementEvent(v, MotionEvent.ACTION_UP);
								}
							}

							return false;
						}
					});
					
					ELEMENTS.put(props[i], view);
				}
			}
		}
		
		String value;
		ImageView image;
		TextView text;
		for(String key : ELEMENTS.keySet()) {
			value = SettingsHelper.getPropConfig(key);
			
			if (!SettingsHelper.propIsActive(key)) {
				dependerState(key, false, true);
				
			} else if (value == null || "0".equals(value) || "".equals(value)) {
				dependerState(key, false, false);
				
			} else if(ELEMENTS.get(key).isEnabled()) {
				image = (ImageView) ELEMENTS.get(key).findViewById(R.id.item_checkbox_2a010ab8);
				if (image != null) {
					image.setImageResource("2".equals(value) ? R.drawable.btn_checkbox_auto : R.drawable.btn_checkbox_on);
					
				} else {
					text = (TextView) ELEMENTS.get(key).findViewById(R.id.item_value_2a010ab8);
					
					if (text != null) {
						text.setText(value);
					}
				}
			}
		}
	}
	
	@Override
	public void onStop() {
		for(String key : ELEMENTS.keySet()) {
			ELEMENTS.put(key, null);
		}
		
		SettingsHelper.commitPropConfigs();
		
		super.onStop();
	}
	
	@Override
	public void onRestart() {
		View view;
		
		for(String key : ELEMENTS.keySet()) {
			view = findViewById( getResources().getIdentifier( (key.replaceAll("\\.", "_") + "_layout_2a010ab8"), "id", "com.spazedog.mounts2sd") );
			
			if (view != null) {
				ELEMENTS.put(key, view);
			}
		}
		
		super.onRestart();
	}
	
	public void dependerState(String prop, Boolean enable, Boolean includeRoot) {
		String value;
		String depender;
		ImageView image;
		TextView text;
		
		if (enable == false || (value = SettingsHelper.getPropConfig(prop)) != null && !value.equals("0")) {
			for(String key : ELEMENTS.keySet()) {
				if (enable != ELEMENTS.get(key).isEnabled() && (!enable || SettingsHelper.propIsActive(key)) && ((includeRoot && key == prop) || ((depender = SettingsHelper.propDependedOn(key)) != null && depender == prop))) {
					ELEMENTS.get(key).setEnabled(enable);
					
					((TextView) ELEMENTS.get(key).findViewById(R.id.item_header_2a010ab8)).setTextColor( getResources().getColor(enable ? R.color.dark : R.color.light_gray) );
					((TextView) ELEMENTS.get(key).findViewById(R.id.item_description_2a010ab8)).setTextColor( getResources().getColor(enable ? R.color.dark_gray : R.color.light_gray) );
					
					image = (ImageView) ELEMENTS.get(key).findViewById(R.id.item_checkbox_2a010ab8);
					if (image != null) {
						image.setImageResource(
								enable == false ? R.drawable.btn_checkbox_disabled : 
									(value = SettingsHelper.getPropConfig(key)) == null || value.equals("0") ? R.drawable.btn_checkbox_off : value.equals("2") ? R.drawable.btn_checkbox_auto : R.drawable.btn_checkbox_on
						);
						
					} else {
						text = (TextView) ELEMENTS.get(key).findViewById(R.id.item_value_2a010ab8);
						
						if (text != null) {
							text.setTextColor( getResources().getColor(enable ? R.color.dark : R.color.light_gray) );
						}
					}
					
					if (key != prop) {
						dependerState(key, enable, false);
					}
				}
			}
		}
	}
	
	public void reverseProps(String prop) {
		String value;
		String depender;
		ImageView image;
		
		for(String key : ELEMENTS.keySet()) {
			if ((depender = SettingsHelper.propReverseOn(key)) != null && depender == prop) {
				image = (ImageView) ELEMENTS.get(key).findViewById(R.id.item_checkbox_2a010ab8);
				if (image != null) {
					value = SettingsHelper.getPropConfig(key);
							
					image.setImageResource( !"1".equals(value) ? R.drawable.btn_checkbox_on : R.drawable.btn_checkbox_off );
					SettingsHelper.setPropConfig(key, !"1".equals(value) ? "1" : "0");
					
					reverseProps(key);
				}
			}
		}
	}
	
	public void onElementEvent(View v, Integer event) {
		if (event == MotionEvent.ACTION_UP) {
			for (Map.Entry<String, View> item : ELEMENTS.entrySet()) {
				if (item.getValue() == v) {
					if (SettingsHelper.propSelector(item.getKey()) != null) {
						((SelectorDialog) new SelectorDialog()).addSelector(SettingsHelper.propSelector(item.getKey()), ((String) ((TextView) v.findViewById(R.id.item_header_2a010ab8)).getText()), SettingsHelper.getPropConfig(item.getKey()), item.getKey()).show(getSupportFragmentManager(), "SelectorDialog");
						
					} else {
						onCheckboxChange(item.getKey());
					}
					
					break;
				}
			}
		
		} else if (event == MotionEvent.ACTION_DOWN) {
			v.setBackgroundColor( getResources().getColor(R.color.light_gray) );
			
		} else {
			v.setBackgroundDrawable(null);
		}
	}
	
	public void onCheckboxChange(String id) {
		String value = SettingsHelper.getPropConfig(id);
		
		((ImageView) ELEMENTS.get(id).findViewById(R.id.item_checkbox_2a010ab8)).setImageResource(!"1".equals(value) ? R.drawable.btn_checkbox_on : R.drawable.btn_checkbox_off);
		SettingsHelper.setPropConfig(id, !"1".equals(value) ? "1" : "0");
		
		dependerState(id, !"1".equals(value) ? true : false, false);
		reverseProps(id);
		
		if (id == "misc.safemode.status" && !"1".equals(value)) {
			((MessageDialog) new MessageDialog()).addMessage("Safe-mode Disabled", "The safe-mode option was made to avoid issues. Disabling it will be at your own risk and any issues caused by it will not be supported. Instead of disabling safe-mode you should implement a proper init.d method into your ramdisk so that safe-mode is never needed and therefor never used by the script!").show(getSupportFragmentManager(), "MessageDialog");
		}
	}

	public void onSelectorChange(SelectorChoice choice) {
		if (choice.getValue() != null && choice.getAction() == true) {
			SettingsHelper.setPropConfig(choice.getId(), choice.getValue());
			
			if (choice.getType() == "switch") {
				((ImageView) ELEMENTS.get(choice.getId()).findViewById(R.id.item_checkbox_2a010ab8)).setImageResource(
						"1".equals(choice.getValue()) ? R.drawable.btn_checkbox_on : 
							"2".equals(choice.getValue()) ? R.drawable.btn_checkbox_auto : R.drawable.btn_checkbox_off
				);
				
			} else {
				((TextView) ELEMENTS.get(choice.getId()).findViewById(R.id.item_value_2a010ab8)).setText(choice.getValue());
			}
			
			dependerState(choice.getId(), "0".equals(choice.getValue()) || "".equals(choice.getValue()) ? false : true, false);
		}
	}
	
	public void onMessageDialogClose() {}
}