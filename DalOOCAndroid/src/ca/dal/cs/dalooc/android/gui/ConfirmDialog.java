package ca.dal.cs.dalooc.android.gui;

import java.util.ArrayList;
import java.util.List;

import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import ca.dal.cs.android.dalooc.R;
import ca.dal.cs.dalooc.android.gui.listener.OnConfirmDialogReturnListener;

public class ConfirmDialog extends DialogFragment {
	
	public static final String ARG_TITLE = "title";
	public static final String ARG_MESSAGE = "message";
	public static final String ARG_CANCEL_BUTTON = "no_cancel_button";

	private List<OnConfirmDialogReturnListener> listeners;
	
	private String title;
	private String message;
	private boolean cancelButton;
	
	public ConfirmDialog() {
		super();
		this.listeners = new ArrayList<OnConfirmDialogReturnListener>();
	}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dialog_learning_object, container);
 
        this.title = getArguments().getString(ARG_TITLE);
		this.message = getArguments().getString(ARG_MESSAGE);
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
    	for (OnConfirmDialogReturnListener listener : this.listeners) {
    		listener.onConfirmDialogReturn(confirm);
    	}
    }
    
    public void setOnConfirmDialogResultListener(OnConfirmDialogReturnListener listener) {
    	this.listeners.add(listener);
    }
    
}
