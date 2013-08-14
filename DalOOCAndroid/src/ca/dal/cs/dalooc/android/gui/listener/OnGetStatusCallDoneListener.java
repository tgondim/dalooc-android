package ca.dal.cs.dalooc.android.gui.listener;

import android.widget.ImageView;
import android.widget.TextView;

public interface OnGetStatusCallDoneListener {

	public void onGetStatusCallResponse(String resultString, ImageView ivIcon, TextView txtLearningObjectName, int position);
	
}
