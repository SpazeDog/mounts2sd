package com.spazedog.mounts2sd;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;

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
		
		String content = SettingsHelper.getPropContent("log.status");
		
		if (content == null) {
			content = "v/The log is empty!";
		}
		
		String lines[] = content.split("\n");
		String level;
		
		LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		LinearLayout wrapper = (LinearLayout) findViewById(R.id.log_viewer_layout_36135fbd);
		View item;
		
		for (int i=0; i < lines.length; i++) {
			if (!"".equals(lines[i]) && (item = inflater.inflate(R.layout.activity_log_item, null)) != null) {
				if (lines[i].matches("^w/.*$")) level = "W";
				else if (lines[i].matches("^e/.*$")) level = "E";
				else if (lines[i].matches("^d/.*$")) level = "D";
				else level = "V";
				
				((TextView) item.findViewById(R.id.item_status_36135fbd)).setText( level );
				((TextView) item.findViewById(R.id.item_text_36135fbd)).setText( lines[i].substring(2, lines[i].length()) );
				
				((TextView) item.findViewById(R.id.item_status_36135fbd)).setTextColor( getResources().getColor(
						level == "E" ? R.color.status_error : 
							level == "E" ? R.color.status_warning : 
								level == "D" ? R.color.status_debug : R.color.status_ok
				) );
				
				wrapper.addView( item );
			}
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
            	
            	try {
	            	File sdcard = Environment.getExternalStorageDirectory();
	            	File dir = new File (sdcard.getAbsolutePath() + "/Mounts2SD");
	            	
	            	dir.mkdirs();
	            	
	            	File file = new File(dir, "log.txt");
            	
            		BufferedWriter out = new BufferedWriter(new FileWriter(file.getAbsolutePath(), false));
                    out.write("" + SettingsHelper.getPropContent("log.status"));
                    out.close();
                    
                    status = true;
                    
                } catch (Throwable e) {  }
            	
            	if (status) {
            		Toast.makeText(this, "Log file was copied to sdcard/Mounts2SD/log.txt", Toast.LENGTH_LONG).show();
            		
            	} else {
            		String message = UtilsHelper.sdcardState();
            		
            		if (message != null) {
            			Toast.makeText(this, message, Toast.LENGTH_LONG).show();
            			
            		} else {
            			Toast.makeText(this, "Could not copy the log file to the sdcard", Toast.LENGTH_LONG).show();
            		}
            	}
    			
                return true;

            default:
            return super.onOptionsItemSelected(item);
        }
    }
}
