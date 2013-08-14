package ca.dal.cs.dalooc.android.gui.component;

import android.annotation.SuppressLint;
import android.content.Context;
import android.widget.RadioButton;

@SuppressLint("ViewConstructor")
public class MyRadioButton extends RadioButton {
	
	protected int index;

	public MyRadioButton(Context context, int index) {
		super(context);
		this.index = index;
	}
	
	public int getIndex() {
		return this.index;
	}
	
}