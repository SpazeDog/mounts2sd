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

import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.Fragment;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import com.spazedog.lib.rootfw3.RootFW;
import com.spazedog.lib.rootfw3.extenders.FileExtender.FileData;
import com.spazedog.mounts2sd.tools.Root;
import com.spazedog.mounts2sd.tools.Utils;
import com.spazedog.mounts2sd.tools.interfaces.TabController;

public class FragmentTabLog extends Fragment {
	
	private String[] mLog;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setHasOptionsMenu(true);
	}
	
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {		
    	ViewGroup view = (ViewGroup) inflater.inflate(R.layout.fragment_tab_log, container, false);
    	TableLayout table = (TableLayout) view.findViewById(R.id.log_table);
    	
    	if (savedInstanceState == null || (mLog = savedInstanceState.getStringArray("mLog")) == null) {
    		RootFW rootfw = Root.open();
    		FileData data = rootfw.file("/tmp/log.txt").read();
    		
    		if (data == null) {
    			data = rootfw.file("/data/m2sd.fallback.log").read();
    			
    			if (data != null) {
    				mLog = data.getArray();
    			}
    			
    		} else {
    			mLog = data.getArray();
    		}
    		
    		if (mLog == null || mLog.length == 0) {
    			mLog = new String[]{"I/" + getResources().getString(R.string.log_empty)};
    		}
    		
    		Root.close();
    	}

    	Boolean bool = false;
    	
    	Integer color1 = getResources().getColor(resolveAttr(R.attr.colorRef_logItemBackgroundFirst));
    	Integer color2 = getResources().getColor(resolveAttr(R.attr.colorRef_logItemBackgroundSecond));
    	
    	for (int i=0; i < mLog.length; i++) {    		
    		TableRow row = (TableRow) inflater.inflate(R.layout.inflate_log_item, table, false);
    		String[] parts = mLog[i].split("/", 2);
    		
    		((TextView) row.getChildAt(0)).setText( parts.length > 1 ? parts[0] : "?" );
    		((TextView) row.getChildAt(1)).setText( parts.length > 1 ? parts[1] : parts[0] );
    		
    		if ((bool = !bool)) {
    			row.setBackgroundColor( color1 );
    			
    		} else {
    			row.setBackgroundColor( color2 );
    		}
    		
    		table.addView(row);
    	}
    	
        return (View) view;
    }
    
	@Override
	public void onSaveInstanceState(Bundle savedInstanceState) {
		savedInstanceState.putStringArray("mLog", mLog);

		super.onSaveInstanceState(savedInstanceState);
	}
    
	@Override
	public void onHiddenChanged(boolean hidden) {
		if (getActivity() != null) {
			((TabController) getActivity()).frameUpdated();
		}
	}
	
	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		onHiddenChanged(false);
	}
	
	public Integer resolveAttr(Integer attr) {
		TypedValue typedvalueattr = new TypedValue();
		getActivity().getTheme().resolveAttribute(attr, typedvalueattr, true);
		
		return typedvalueattr.resourceId;
	}
	
	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		inflater.inflate(R.menu.fragment_tab_log, menu);
		super.onCreateOptionsMenu(menu,inflater);
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch(item.getItemId()) {
			case R.id.menu_log_save:
				Boolean status = true;
				String message;
				
				try {
					File sdcardPath = new File (Environment.getExternalStorageDirectory().getAbsolutePath() + "/Mounts2SD");
					File logFile = new File(sdcardPath, "log.txt");
					
					sdcardPath.mkdirs();
	
					BufferedWriter output = new BufferedWriter(new FileWriter(logFile.getAbsolutePath(), false));
					for (int i=0; i < mLog.length; i++) {
						output.write(mLog[i]);
						output.newLine();
					}
					output.close();
					
				} catch (Throwable e) { status = false; }
				
				if (status) {
					message = getResources().getString(R.string.toast_log_copied);
					
				} else if ((message = Utils.sdcardStateMessage(getActivity())) == null) {
					message = getResources().getString(R.string.toast_log_unsuccessful);
				}
				
				Toast.makeText(getActivity(), message, Toast.LENGTH_LONG).show();
		}
		
		return super.onOptionsItemSelected(item);
	}
}
