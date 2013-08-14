package ca.dal.cs.dalooc.android.gui.component;

import android.content.Context;
import android.widget.ImageView;
import ca.dal.cs.dalooc.android.R;
import ca.dal.cs.dalooc.android.gui.listener.OnRecordingBlinkListener;

public class RecordingBlinkImageView extends ImageView {
	
	public static final int IMAGE_RECORDING = 0;
	
	public static final int IMAGE_RECORDING_DARK = 1;
	
	private Thread blinkButton;
	
	private boolean isBlinking = false;
	
	private OnRecordingBlinkListener listener;

	public RecordingBlinkImageView(Context context) {
		super(context);
		setImageDrawable(context.getResources().getDrawable(R.drawable.ic_audio_recording_light_off));
	}
	
	public void startBlinking() {
		RecordingBlinkImageView.this.setImageDrawable(getContext().getResources().getDrawable(R.drawable.ic_audio_recording_light_on));

		this.blinkButton = new Thread(new Runnable() {
			
			@Override
			public void run() {
				RecordingBlinkImageView.this.isBlinking = true;
				boolean light = false;
				while (RecordingBlinkImageView.this.isBlinking) {
					if (!light) {
						RecordingBlinkImageView.this.fireOnBlinkEvent(RecordingBlinkImageView.IMAGE_RECORDING);
						light = true;
						try {
							Thread.sleep(500);
						} catch (InterruptedException e) {}
					} else {
						RecordingBlinkImageView.this.fireOnBlinkEvent(RecordingBlinkImageView.IMAGE_RECORDING_DARK);
						light = false;
						try {
							Thread.sleep(1000);
						} catch (InterruptedException e) {}
					}
				}
			}
		});
		this.blinkButton.start();
	}
	
	public void stopBlinking() {
		this.isBlinking = false;
		fireOnBlinkEvent(RecordingBlinkImageView.IMAGE_RECORDING_DARK);
	}
	
	public void setOnRecordingBlinkListener(OnRecordingBlinkListener listener) {
		this.listener = listener;
	}
	
	private void fireOnBlinkEvent(int arg) {
		if (this.listener != null) {
			this.listener.onBlink(arg);
		}
	}
}
