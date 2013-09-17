package com.spazedog.mounts2sd.tools.interfaces;

import android.os.Bundle;

public interface IDialogConfirmResponse extends IDialogListener {
	public void onDialogConfirm(String tag, Boolean confirm, Bundle extra);
}
