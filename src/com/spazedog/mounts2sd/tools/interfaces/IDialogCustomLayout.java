package com.spazedog.mounts2sd.tools.interfaces;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public interface IDialogCustomLayout {
	public View onDialogCreateView(String tag, LayoutInflater inflater, ViewGroup container, Bundle extra);
	public void onDialogViewCreated(String tag, View view, Bundle extra);
}
