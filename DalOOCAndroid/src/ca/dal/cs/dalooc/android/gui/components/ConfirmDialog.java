package ca.dal.cs.dalooc.android.gui.components;

import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import ca.dal.cs.dalooc.android.R;
import ca.dal.cs.dalooc.android.gui.listener.OnConfirmDialogReturnListener;

public class ConfirmDialog extends DialogFragment {
	
	public static final String ARG_TITLE = "title";
	public static final String ARG_MESSAGE = "message";
	public static final String ARG_RETURN_CODE = "return_code";
	public static final String ARG_CANCEL_BUTTON = "no_cancel_button";

	private OnConfirmDialogReturnListener listener;
	
	private String title;
	
	private String message;
	
	private boolean cancelButton;
	
	private int returnCode;
	
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dialog_learning_object, container);
 
        this.title = getArguments().getString(ARG_TITLE);
		this.message = getArguments().getString(ARG_MESSAGE);
		this.returnCode = getArguments().getInt(ARG_RETURN_CODE);
		this.cancelButton = getArguments().getBoolean(ARG_CANCEL_BUTTON, true);
		
		if (this.title == null) {
			this.title = getResources().getString(R.string.confirm);
		}
		
		if (this.message == null) {
			this.message = getResources().getString(R.string.confirm_message);
		}
        
        TextView textView = (TextView)view.findViewById(R.id.tvDialog);
        textView.setText(this.message);
        
        getDialog().setTitle(this.title);
        getDialog().setCanceledOnTouchOutside(false);
        getDialog().setCancelable(true);
        
        Button bYes = (Button)view.findViewById(R.id.bYes);
        bYes.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				fireOnConfirmDialogResultEvent(true);
				ConfirmDialog.this.dismiss();
			}
		});
        
        Button bNo = (Button)view.findViewById(R.id.bNo);
        bNo.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				fireOnConfirmDialogResultEvent(false);				
				ConfirmDialog.this.dismiss();
			}
		});
        
        Button bCancel = (Button)view.findViewById(R.id.bCancel);

        if (this.cancelButton) {
	        bCancel.setOnClickListener(new OnClickListener() {
	        	
	        	@Override
	        	public void onClick(View v) {
	        		ConfirmDialog.this.dismiss();
	        	}
	        });
        } else {
        	bCancel.setVisibility(View.GONE);
        }

        return view;
    }
    
    private void fireOnConfirmDialogResultEvent(boolean confirm) {
    	if (this.listener != null) {
    		this.listener.onConfirmDialogReturn(confirm, this.returnCode);
    	}
    }
    
    public void setOnConfirmDialogResultListener(OnConfirmDialogReturnListener listener) {
    	this.listener = listener;
    }
}
