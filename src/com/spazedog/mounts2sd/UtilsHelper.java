package com.spazedog.mounts2sd;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Environment;
import android.os.StatFs;
import android.util.Log;
import android.view.View;

public class UtilsHelper {
	public static final String TAG = "Mounts2SD";
	private static final String SIZE_PRIFIX[] = {"b","Kb","Mb","Gb"};
	
    public static String getMB(double iNum) {
        String lPrifix = SIZE_PRIFIX[0];
        double iCal = (double) iNum;
        double iDevide = 1024D;

        for (int i=1; i < SIZE_PRIFIX.length; i++) {
                if (iCal < iDevide) {
                        break;
                }

                iCal = iCal/iDevide;
                lPrifix = SIZE_PRIFIX[i];
        }

        return "" + (Math.round(iCal*100.0)/100.0) + lPrifix;
	}
	
	public static double diskUsage(String dir) {
		try {
	        StatFs stat = new StatFs(dir);
	        double result = ((double) stat.getBlockCount() - (double) stat.getAvailableBlocks()) * (double) stat.getBlockSize();
	
	        return result;
	        
		} catch (Exception e) {
			return 0;
		}
	}
	
	public static double diskTotal(String dir) {
		try {
	        StatFs stat = new StatFs(dir);
	        double result = (double) stat.getBlockCount() * (double) stat.getBlockSize();
	
	        return result;
	        
		} catch (Exception e) {
			return 0;
		}
	}
	
	public static final String md5(final String s) { 
		try { // Create MD5 Hash 
			MessageDigest digest = java.security.MessageDigest.getInstance("MD5"); 
			digest.update(s.getBytes()); 
			
			byte messageDigest[] = digest.digest();
	
	        // Create Hex String
	        StringBuffer hexString = new StringBuffer();
	        for (int i = 0; i < messageDigest.length; i++) {
	            String h = Integer.toHexString(0xFF & messageDigest[i]);
	            while (h.length() < 2)
	                h = "0" + h;
	            hexString.append(h);
	        }
	        return hexString.toString();

	    } catch (NoSuchAlgorithmException e) {
	        e.printStackTrace();
	    }
		
	    return "";
	}
	
	public static String sdcardState() {
	    String sdcardStatus = Environment.getExternalStorageState();

	    if (sdcardStatus.equals(Environment.MEDIA_MOUNTED_READ_ONLY)) {
	    	return "The sdcard is mounted in read-only mode";

	    } else if (sdcardStatus.equals(Environment.MEDIA_NOFS)) {
	    	return "The sdcard is not formated in a correct format";

	    } else if (sdcardStatus.equals(Environment.MEDIA_REMOVED)) {
	    	return "The sdcard is not present";

	    } else if (sdcardStatus.equals(Environment.MEDIA_SHARED)) {
	    	return "The sdcard is corrently being shared via the USB";

	    } else if (sdcardStatus.equals(Environment.MEDIA_UNMOUNTABLE)) {
	    	return "The sdcard is not mounted do to issues with the partition or the card it self";

	    } else if (sdcardStatus.equals(Environment.MEDIA_UNMOUNTED)) {
	    	return "The sdcard is not mounted";

	    }

	    return null;
	}
	
	public static class Notifier {
		private static Integer NOTIFY_ID = 0;
		
		public static Integer send(Class<?> pActivity, String pTitle, String pMessage, Integer pIcon, Boolean pFlash, Boolean pVibrate, Boolean pCancel) {
			
			NOTIFY_ID += 1;
			
			PendingIntent lPendingIntent = PendingIntent.getActivity(
					BaseApplication.getContext(), 
					0, 
					new Intent(BaseApplication.getContext(), pActivity), 
					0
			);
			NotificationManager lNotifyManager = (NotificationManager) BaseApplication.getContext().getSystemService(Context.NOTIFICATION_SERVICE);
			Notification lNotifier = new Notification();
			
			lNotifier.icon = pIcon;
			lNotifier.tickerText = pTitle;
			lNotifier.when = System.currentTimeMillis();
			
			if (pFlash) {
				lNotifier.flags |= Notification.FLAG_SHOW_LIGHTS;
				lNotifier.ledARGB = Color.CYAN;
				lNotifier.ledOnMS = 500;
				lNotifier.ledOffMS = 500;
		    }
			
	        if (pVibrate) {
	        	lNotifier.vibrate = new long[] {100, 200, 200, 200, 200, 200};
	        }
	        
	        if(pCancel) {
	        	lNotifier.flags |= Notification.FLAG_AUTO_CANCEL;
	        }
	        
	        lNotifier.setLatestEventInfo(BaseApplication.getContext(), pTitle, pMessage, lPendingIntent);
	        
	        lNotifyManager.notify(NOTIFY_ID, lNotifier);
	        
	        return NOTIFY_ID;
	    }
	}
	
	public static class RootAccount {
		private Process PROCESS;
		private Boolean CONNECTED = false;
		
		private static Boolean SYSTEM_RW = false;
		private static RootAccount INSTANCE;
		private static Integer COUNT = 0;
		
		public final static Integer RETURN_CODE = 101;
		public final static Integer RETURN_LINE = 102;
		public final static Integer RETURN_ALL = 103;
		
		public static RootAccount getInstance(Boolean systemRw) {
			if(INSTANCE == null) {
				INSTANCE = new RootAccount();
			}
			
			if (systemRw && !SYSTEM_RW) {
				INSTANCE.execute("busybox mount -o remount,rw /system\nbusybox mount -o remount,rw /", RETURN_CODE);
				
				SYSTEM_RW = true;
			}
			
			COUNT += 1;
			
			return INSTANCE;
		}
		
		private RootAccount() {

			try {
				ProcessBuilder builder = new ProcessBuilder("su");
				builder.redirectErrorStream(true);
				
				PROCESS = builder.start();

				String data;
				
				if ((data = execute("id", RETURN_LINE)) != null && data.contains("uid=0")) {
					CONNECTED = true;
				}
				
			} catch(Throwable e) {
				Log.d(TAG, "Root access rejected [" + e.getClass().getName() + "] : " + e.getMessage());
			}
		}
		
		public Boolean isConnected() {
			return CONNECTED;
		}
		
		public String execute(String pCommand, Integer pReturn) {
			try {
				DataOutputStream output = new DataOutputStream(PROCESS.getOutputStream());
				BufferedReader buffer = new BufferedReader(new InputStreamReader(PROCESS.getInputStream()));
				
				output.writeBytes(pCommand + (pReturn == RETURN_CODE ? "; busybox echo $?" : "") + "\n");
				
				/* The problem with BufferedReader.readLine, is that it will block as soon as it reaches the end, as it will be waiting
				 * for the next line to be printed. In this case it will never return NULL at any point. So we add a little ID at the end
				 * that we can look for and then manually break the loop when that ID is returned, while getting the last line, which is what we need
				 */
				output.writeBytes("busybox echo ''\n"); 
				output.writeBytes("busybox echo EOL:a00c38d8:EOL\n"); 
				output.flush();

				String line = null;
				String data = "";
			
				try {
					while ((line = buffer.readLine()) != null && !"EOL:a00c38d8:EOL".equals(line)) {
						if (pReturn == RETURN_ALL) {
							data += line + "\n";
							
						} else if (!"".equals(line)) {
							data = line;
						}
					}
					
				} catch(Throwable e) {  }
				
				return data;
				
			} catch (Throwable e) {
				Log.w(TAG, "Error executing operation [" + e.getClass().getName() + "] : " + e.getMessage());
			}
			
			return null;
		}
		
		public Boolean copyFile(String fileName, String filePath, String owner, String mod) {
			InputStream in = BaseApplication.getContext().getResources().openRawResource( BaseApplication.getContext().getResources().getIdentifier(fileName, "raw", "com.spazedog.mounts2sd") );
			FileOutputStream out = null;
			
			try {
				out = BaseApplication.getContext().openFileOutput(fileName, 0);
				
			} catch (FileNotFoundException e) {
				Log.d(TAG, "Could not copy script", e); return false;
			}
			
			byte[] buff = new byte[1024];
			Integer read = 0;
			
			try {
				while ((read = in.read(buff)) > 0) {
					out.write(buff, 0, read);
				}
				
			} catch (IOException e) { return false; }
			
			try {
				in.close();
				out.close();
				
			} catch (IOException e) {}
			
			String command = "";
			
			command += "busybox [ -d $(busybox dirname " + filePath + ") ] || busybox mkdir $(busybox dirname " + filePath + ")\n";
			command += "busybox mv /data/data/com.spazedog.mounts2sd/files/"+ fileName + " " + filePath + "\n";
			command += "busybox chmod " + mod + " " + filePath + "\n";
			command += "busybox chown " + owner + " " + filePath + "\n";
			command += "busybox [ -f " + filePath + " ] && busybox echo 0 || busybox echo 1";
			
			if (!"0".equals( this.execute(command, RETURN_LINE) )) {
				return false;
			}
			
			return true;
		}
		
		public String fileReadAll(String pFile) {
			if ("1".equals(this.execute("busybox [ -f " + pFile + " ] && busybox echo 1 || busybox echo 0", RETURN_LINE))) {
				return (String) this.execute("busybox cat " + pFile, RETURN_ALL);
			}
			
			return null;
		}
		
		public String fileReadLine(String pFile) {
			if ("1".equals(this.execute("busybox [ -f " + pFile + " ] && busybox echo 1 || busybox echo 0", RETURN_LINE))) {
				return (String) this.execute("busybox echo $(busybox sed -n '1p' " + pFile + ")", RETURN_LINE);
			}
			
			return null;
		}
		
		public Boolean filePutLine(String pFile, String pData) {
			if ("0".equals(this.execute("( busybox [ ! -e " + pFile + " ] || busybox [ -f " + pFile + " ] ) && busybox echo \"" + pData + "\" > " + pFile + " && busybox echo 0 || busybox echo 1", RETURN_LINE))) {
				return true;
			}
			
			return false;
		}
		
		public Boolean checkBusybox() {
			if ("1".equals(this.execute("busybox test 2> /dev/null", RETURN_CODE))) {
				return true;
			}
			
			return false;
		}
		
		public Boolean installFromResources(String resFile) {
			if (!this.copyFile("recovery_install_sh", "/recoveryInstall.sh", "0", "a+x")) {
				return false;
				
			} else if (!this.copyFile(resFile, "/update.zip", "0", "0655")) {
				return false;
			}
			
			return "0".equals(this.execute("/recoveryInstall.sh", RootAccount.RETURN_CODE));
		}
		
		public void close() {
			if (COUNT == 1) {
				try {
					if (CONNECTED) {
						if (SYSTEM_RW) {
							INSTANCE.execute("busybox mount -o remount,ro /system\nbusybox mount -o remount,ro /", RETURN_CODE);
							
							SYSTEM_RW = false;
						}
						
						execute("exit", RETURN_CODE);
	
						PROCESS.destroy();
					}
		
					PROCESS = null;
					CONNECTED = false;
					
				} catch (Exception e) {
					Log.w(TAG, "Error closing connections [" + e.getClass().getName() + "] : " + e.getMessage());
				}
				
				INSTANCE = null;
				COUNT -= 1;
			}
		}
	}
	
	public static class ElementContainer {
		private Map<String, String> STRINGS = new HashMap<String, String>();
		private Map<String, Integer> INTEGERS = new HashMap<String, Integer>();
		private Map<String, Boolean> BOOLS = new HashMap<String, Boolean>();
		private Map<String, View> VIEWS = new HashMap<String, View>();
		
		public final void putInt(String pName, int pValue) {
			INTEGERS.put(pName, pValue);
		}
		
		public final Integer getInt(String pName) {
			return INTEGERS.get(pName);
		}
		
		public final void putBool(String pName, Boolean pValue) {
			BOOLS.put(pName, pValue);
		}
		
		public final Boolean getBool(String pName) {
			return BOOLS.get(pName);
		}
		
		public final void putString(String pName, String pValue) {
			STRINGS.put(pName, pValue);
		}
		
		public final String getString(String pName) {
			return STRINGS.get(pName);
		}
		
		public final void putView(String pName, View pValue) {
			VIEWS.put(pName, pValue);
		}
		
		public final View getView(String pName) {
			return VIEWS.get(pName);
		}
	}
	
	public static class SelectorOptions {
		String VALUES[];
		String NAMES[];
		Boolean SUPPORTED[];
		Integer SIZE;
		
		public SelectorOptions(String name) {
			NAMES = BaseApplication.getContext().getResources().getStringArray(
					name.equals("switch") ? R.array.selector_switch_names : 
						name.equals("filesystem") ? R.array.selector_filesystem_names : R.array.selector_readahead_names
			);
			
			VALUES = BaseApplication.getContext().getResources().getStringArray(
					name.equals("switch") ? R.array.selector_switch_values : 
						name.equals("filesystem") ? R.array.selector_filesystem_values : R.array.selector_readahead_values
			);
			
			SUPPORTED = new Boolean[VALUES.length];
			SIZE = VALUES.length;
			
			if (name.equals("filesystem")) {
				try {
					BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream("/proc/filesystems")));
					String line;
					String fsTypes = "auto";
					
					while ((line = br.readLine()) != null) {
						if (!line.contains("nodev ")) {
							fsTypes += " " + line;
						}
					}
					
					br.close();
					
					for (int i=0; i < VALUES.length; i++) {
						SUPPORTED[i] = fsTypes.contains(VALUES[i]);
					}
					
				} catch (Throwable e) { e.printStackTrace(); }
			}
		}
		
		public Integer getSize() {
			return SIZE;
		}
		
		public String getName(Integer index) {
			return NAMES[index];
		}
		
		public String getValue(Integer index) {
			return VALUES[index];
		}
		
		public Boolean isSupported(Integer index) {
			return SUPPORTED[index] != null ? SUPPORTED[index] : true;
		}
	}
}
