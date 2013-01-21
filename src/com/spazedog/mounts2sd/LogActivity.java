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

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.Locale;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

public class LogActivity extends Activity {
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.activity_log);
		
		String content = SettingsHelper.getLog();
		
		LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		LinearLayout wrapper = (LinearLayout) findViewById(R.id.log_viewer_layout_36135fbd);
		View item;
		
		if (content != null) {
			String lines[] = content.split("\n");
			String level;
			String[] parts;
			
			for (int i=0; i < lines.length; i++) {
				if (!"".equals(lines[i]) && (item = inflater.inflate(R.layout.activity_log_item, null)) != null && (parts = UtilsHelper.splitScriptMessage(lines[i], true)).length == 3) {
					level = parts[0].toUpperCase(Locale.US);
					
					((TextView) item.findViewById(R.id.item_status_36135fbd)).setText( level );
					((TextView) item.findViewById(R.id.item_text_36135fbd)).setText( parts[2] );
					
					((TextView) item.findViewById(R.id.item_status_36135fbd)).setTextColor( getResources().getColor(
							level.equals("E") ? R.color.status_error : 
								level.equals("W") ? R.color.status_warning : 
									level.equals("D") ? R.color.status_debug : R.color.status_ok
					) );
					
					wrapper.addView( item );
				}
			}
			
		} else if ((item = inflater.inflate(R.layout.activity_log_item, null)) != null) {
			((TextView) item.findViewById(R.id.item_text_36135fbd)).setText(getResources().getString(R.string.log_is_empty));
		}
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.activity_log, menu);
		return true;
	}
	
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()) {
            case R.id.log_menu_save:
            	Boolean status = false;
            	String content = SettingsHelper.getLog();
            	
            	try {
            		if (content != null) {
            			String lines[] = content.split("\n");
            			String[] parts;
            			String log="";
            			
            			for (int i=0; i < lines.length; i++) {
            				parts = UtilsHelper.splitScriptMessage(lines[i], false);
            				log += parts[0] + "/" + parts[1] + " - " + parts[2] + "\n";
            			}
            			
		            	File sdcard = Environment.getExternalStorageDirectory();
		            	File dir = new File (sdcard.getAbsolutePath() + "/Mounts2SD");
		            	
		            	dir.mkdirs();
		            	
		            	File file = new File(dir, "log.txt");
	            	
	            		BufferedWriter out = new BufferedWriter(new FileWriter(file.getAbsolutePath(), false));
	                    out.write(log);
	                    out.close();
            		}
                    
                    status = true;
                    
                } catch (Throwable e) { e.printStackTrace(); }
            	
            	if (status) {
            		if (content == null) {
            			Toast.makeText(this, getResources().getString(R.string.toast_log_empty_file), Toast.LENGTH_LONG).show();
            			
            		} else {
            			Toast.makeText(this, getResources().getString(R.string.toast_log_copied), Toast.LENGTH_LONG).show();
            		}
            		
            	} else {
            		String message = UtilsHelper.sdcardState();
            		
            		if (message != null) {
            			Toast.makeText(this, message, Toast.LENGTH_LONG).show();
            			
            		} else {
            			Toast.makeText(this, getResources().getString(R.string.toast_log_unsuccessful), Toast.LENGTH_LONG).show();
            		}
            	}
    			
                return true;

            default:
            return super.onOptionsItemSelected(item);
        }
    }
}
