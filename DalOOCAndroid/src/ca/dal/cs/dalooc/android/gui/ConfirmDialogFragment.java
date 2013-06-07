package ca.dal.cs.dalooc.android.gui;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import ca.dal.cs.android.dalooc.R;

public class ConfirmDialogFragment extends DialogFragment {
	
	public static final String ARG_CONFIRM_MESSAGE = "confirm_message";
	
	public static final int VIDEO_CONFIRM_MESSAGE = 0;
	
	public static final int AUDIO_CONFIRM_MESSAGE = 1;
	
	public static final int DOCUMENT_CONFIRM_MESSAGE = 2;
	
	private int messageType;
	
	private boolean lastChoice;
	
	@Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        
        this.messageType = getArguments().getInt(ARG_CONFIRM_MESSAGE, -1);
        String message;
        switch (messageType) {
		case VIDEO_CONFIRM_MESSAGE:
			message = getResources().getString(R.string.video_overwrite_confirm);
			break;
		
		case AUDIO_CONFIRM_MESSAGE:
			message = getResources().getString(R.string.audio_overwrite_confirm);
			break;

		case DOCUMENT_CONFIRM_MESSAGE:
			message = getResources().getString(R.string.document_overwrite_confirm);
			break;

		default:
			message = getResources().getString(R.string.default_overwrite_confirm);
			break;
        }
        
        builder.setMessage(message)
               .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                   public void onClick(DialogInterface dialog, int id) {
                	   ConfirmDialogFragment.this.lastChoice = true;
                   }
               })
               .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                   public void onClick(DialogInterface dialog, int id) {
                	   ConfirmDialogFragment.this.lastChoice = false;
                   }
               });
        
        return builder.create();
    }
	
	public boolean getLastChoice() {
		return this.lastChoice;
	}

}
