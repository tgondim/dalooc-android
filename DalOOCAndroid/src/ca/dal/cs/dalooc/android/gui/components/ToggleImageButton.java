package ca.dal.cs.dalooc.android.gui.components;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.widget.ImageButton;
import ca.dal.cs.dalooc.android.gui.listener.OnToggleImageButtonListener;

@SuppressLint("ViewConstructor")
public class ToggleImageButton extends ImageButton {
	
	private boolean isStartImage = true;
	
	private Drawable startImage;
	
	private Drawable stopImage;
	
	private OnToggleImageButtonListener listener;
	
	public ToggleImageButton(Context ctx, Drawable startImage, Drawable stopImage) {
		super(ctx);
		
		this.startImage = startImage;
		this.stopImage = stopImage;
		
		setImageDrawable(this.startImage);
		
		setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				ToggleImageButton.this.onClick();
			}
		});
	}
	
	public void onClick() {
		fireOnRecordAudioImageButtonClick();
		toggleButton();
	}
	
	public void setOnRecordAudioImageButtonClick(OnToggleImageButtonListener onRecordAudioImageButtonListener) {
		this.listener = onRecordAudioImageButtonListener;
	}
	
	private void fireOnRecordAudioImageButtonClick() {
		if (this.listener != null) {
			this.listener.onToggleImageButtonClick(this.isStartImage);
		}
	}
	
	public void toggleButton() {
		if (ToggleImageButton.this.isStartImage) {
			setImageDrawable(this.stopImage);
		} else {
			setImageDrawable(this.startImage);
		}
		ToggleImageButton.this.isStartImage = !ToggleImageButton.this.isStartImage;
	}
}
