package com.spazedog.mounts2sd.tools;

import com.spazedog.mounts2sd.R;

import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Environment;
import android.os.StatFs;

public class Common {
	private static final String[] mPrifixes = {"b","Kb","Mb","Gb"};
	
    public static String convertPrifix(double iNum) {
        String lPrifix = mPrifixes[0];
        double iCal = (double) iNum;
        double iDevide = 1024D;

        for (int i=1; i < mPrifixes.length; i++) {
                if (iCal < iDevide) {
                        break;
                }

                iCal = iCal/iDevide;
                lPrifix = mPrifixes[i];
        }

        return "" + (Math.round(iCal*100.0)/100.0) + lPrifix;
	}
    
	public static double getDiskUsage(String dir) {
		try {
	        StatFs stat = new StatFs(dir);
	        double result = ((double) stat.getBlockCount() - (double) stat.getAvailableBlocks()) * (double) stat.getBlockSize();
	
	        return result;
	        
		} catch (Exception e) {
			return 0;
		}
	}
	
	public static double getDiskTotal(String dir) {
		try {
	        StatFs stat = new StatFs(dir);
	        double result = (double) stat.getBlockCount() * (double) stat.getBlockSize();

	        return result;
	        
		} catch (Exception e) {
			return 0;
		}
	}
	
	public static String sdcardStateMessage(Context context) {
	    String sdcardStatus = Environment.getExternalStorageState();

	    if (sdcardStatus.equals(Environment.MEDIA_MOUNTED_READ_ONLY)) {
	    	return context.getResources().getString(R.string.sdcard_state_ro);

	    } else if (sdcardStatus.equals(Environment.MEDIA_NOFS)) {
	    	return context.getResources().getString(R.string.sdcard_state_format);

	    } else if (sdcardStatus.equals(Environment.MEDIA_REMOVED)) {
	    	return context.getResources().getString(R.string.sdcard_state_missing);

	    } else if (sdcardStatus.equals(Environment.MEDIA_SHARED)) {
	    	return context.getResources().getString(R.string.sdcard_state_ums);

	    } else if (sdcardStatus.equals(Environment.MEDIA_UNMOUNTABLE)) {
	    	return context.getResources().getString(R.string.sdcard_state_mount_failure);

	    } else if (sdcardStatus.equals(Environment.MEDIA_UNMOUNTED)) {
	    	return context.getResources().getString(R.string.sdcard_state_mount);
	    }

	    return null;
	}
	
	public static boolean checkSignatures(Context context, String packageName) {
	    PackageManager manager = context.getPackageManager();
	    
	    return manager.checkSignatures(context.getPackageName(), packageName)
	    		== PackageManager.SIGNATURE_MATCH;
	}
	
	public static void wait(int time) {
		try {
			Thread.sleep(time);
			
		} catch (Throwable e) {}
	}
}
