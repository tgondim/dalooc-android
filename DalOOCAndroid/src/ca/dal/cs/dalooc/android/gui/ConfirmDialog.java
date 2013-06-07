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
import ca.dal.cs.android.dalooc.R;
import ca.dal.cs.dalooc.android.gui.listener.OnConfirmDialogReturnListener;

public class ConfirmDialog extends DialogFragment {

	private List<OnConfirmDialogReturnListener> listeners;
	
    public ConfirmDialog() {
    	super();
    	this.listeners = new ArrayList<OnConfirmDialogReturnListener>();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dialog_learning_object, container);
        getDialog().setTitle(getResources().getString(R.string.dialog_title_learning_object));
        getDialog().setCanceledOnTouchOutside(false);
        getDialog().setCancelable(true);
        
        Button bYes = (Button)view.findViewById(R.id.bYes);
        bYes.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				fireOnConfirmDialogResultEvent(true);
			}
		});
        
        Button bNo = (Button)view.findViewById(R.id.bNo);
        bNo.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				fireOnConfirmDialogResultEvent(false);				
			}
		});
        
        Button bCancel = (Button)view.findViewById(R.id.bCancel);
        bCancel.setOnClickListener(new OnClickListener() {
        	
        	@Override
        	public void onClick(View v) {
        		ConfirmDialog.this.dismiss();
        	}
        });

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
