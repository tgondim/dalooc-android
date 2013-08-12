package ca.dal.cs.dalooc.android.gui;

import java.io.IOException;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import ca.dal.cs.dalooc.android.R;
import ca.dal.cs.dalooc.android.gui.components.ConfirmDialog;
import ca.dal.cs.dalooc.android.gui.components.RecordingBlinkImageView;
import ca.dal.cs.dalooc.android.gui.components.ToggleImageButton;
import ca.dal.cs.dalooc.android.gui.listener.OnConfirmDialogReturnListener;
import ca.dal.cs.dalooc.android.gui.listener.OnRecordingBlinkListener;
import ca.dal.cs.dalooc.android.gui.listener.OnToggleImageButtonListener;
import ca.dal.cs.dalooc.android.gui.listener.OnUploadFileTaskDoneListener;
import ca.dal.cs.dalooc.android.util.General;
import ca.dal.cs.dalooc.android.webservice.UploadFileTask;
import ca.dal.cs.dalooc.model.Audio;
import ca.dal.cs.dalooc.model.Course;
import ca.dal.cs.dalooc.model.User;

public class AudioEditActivity extends FragmentActivity implements OnRecordingBlinkListener, OnConfirmDialogReturnListener, OnUploadFileTaskDoneListener, OnToggleImageButtonListener {
	
	private static final String LOG_TAG = "AudioEditActivity";
	
	public static final int ACTION_CONFIRM_AUDIO_CHANGES = 200;
	
	public static final int ACTION_CONFIRM_AUDIO_UPLOAD = 300;

	private Audio audio;
	
	private User user;
	
	private Course course;
	
	private int learningObjectIndex;

	private int audioIndex;
	
	private String newFileName = "";
	
	private ConfirmDialog confirmDialog;
	
	private EditText etName;
	
	private EditText etDescription;
	
	private ImageButton ibAudioUpload;
	
	private ToggleImageButton ibAudioPlay;
	
	private ToggleImageButton ibAudioRecord;
	
	private LinearLayout llForm;
	
	private RecordingBlinkImageView ivBlinkingImage;
	
	private LinearLayout llUploadPreviewStatus;
	
	private TextView tvUploadStatusMessage;
	
	private MediaPlayer mediaPlayer = null;
	
	private MediaRecorder mediaRecorder = null;
	
	private Chronometer audioChronometer;
	
	private Toast toast;
	
	@SuppressLint("HandlerLeak")
	private Handler callBackHandler = new Handler() {
		@Override
		public void handleMessage(android.os.Message msg) {
			switch (msg.what) {
			case RecordingBlinkImageView.IMAGE_RECORDING:
				AudioEditActivity.this.ivBlinkingImage.setImageDrawable(AudioEditActivity.this.getResources().getDrawable(R.drawable.ic_audio_recording_light_on));
				break;
			case RecordingBlinkImageView.IMAGE_RECORDING_DARK:
				AudioEditActivity.this.ivBlinkingImage.setImageDrawable(AudioEditActivity.this.getResources().getDrawable(R.drawable.ic_audio_recording_light_off));
				break;
			case UploadFileTask.UPLOAD_DONE:
				showProgress(false, "");
				showToast((String)msg.obj);
				break;
			}
		}
	};
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_audio_edit);
		
		this.llForm = (LinearLayout)findViewById(R.id.llForm);
		this.llUploadPreviewStatus = (LinearLayout)findViewById(R.id.llUploadPreviewStatus);
		this.tvUploadStatusMessage = (TextView)findViewById(R.id.tvUploadStatusMessage);

		this.etName = (EditText)findViewById(R.id.etName);
		this.etDescription = (EditText)findViewById(R.id.etDescription);
		
		LinearLayout llAudioEditButtons = (LinearLayout)findViewById(R.id.llAudioEditButtons);
		
		this.ibAudioPlay = new ToggleImageButton(this, 
				getResources().getDrawable(R.drawable.ic_media_play), 
				getResources().getDrawable(R.drawable.ic_media_stop));
		this.ibAudioPlay.setOnRecordAudioImageButtonClick(new OnToggleImageButtonListener() {
			
			@Override
			public void onToggleImageButtonClick(boolean isStartImage) {
				if (!TextUtils.isEmpty(AudioEditActivity.this.audio.getContentFileName())) {
					if (isStartImage) {
						startPlaying();
					} else {
						stopPlaying();
					}
				} else {
					showToast(getResources().getString(R.string.no_file_to_open));
					AudioEditActivity.this.ibAudioPlay.toggleButton();
				}				
			}
		});
		
		llAudioEditButtons.addView(ibAudioPlay, new LinearLayout.LayoutParams(
				ViewGroup.LayoutParams.WRAP_CONTENT, 
				ViewGroup.LayoutParams.WRAP_CONTENT, 
				0));
		
		this.ibAudioRecord = new ToggleImageButton(this, 
				getResources().getDrawable(R.drawable.ic_audio_record), 
				getResources().getDrawable(R.drawable.ic_media_stop));
		this.ibAudioRecord.setOnRecordAudioImageButtonClick(this);
		
		llAudioEditButtons.addView(this.ibAudioRecord, new LinearLayout.LayoutParams(
				ViewGroup.LayoutParams.WRAP_CONTENT, 
				ViewGroup.LayoutParams.WRAP_CONTENT, 
				0));
		
		this.ibAudioUpload = new ImageButton(this);
		this.ibAudioUpload.setImageDrawable(getResources().getDrawable(R.drawable.ic_upload));
		this.ibAudioUpload.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				Intent uploadFileIntent = new Intent(Intent.ACTION_GET_CONTENT);
				uploadFileIntent.setType("audio/*");
				
				startActivityForResult(uploadFileIntent, AudioDetailActivity.GET_AUDIO_FILE_ACTIVITY_REQUEST_CODE);				
			}
		});
		
		llAudioEditButtons.addView(this.ibAudioUpload, new LinearLayout.LayoutParams(
				ViewGroup.LayoutParams.WRAP_CONTENT, 
				ViewGroup.LayoutParams.WRAP_CONTENT, 
				0));
		
		this.ivBlinkingImage = new RecordingBlinkImageView(this);
		this.ivBlinkingImage.setOnRecordingBlinkListener(this);
		
		llAudioEditButtons.addView(this.ivBlinkingImage, new LinearLayout.LayoutParams(
				ViewGroup.LayoutParams.WRAP_CONTENT, 
				ViewGroup.LayoutParams.WRAP_CONTENT, 
				0));
		
		this.audioChronometer = new Chronometer(this);
		this.audioChronometer.setTextAppearance(this, android.R.style.TextAppearance_Large);

		llAudioEditButtons.addView(this.audioChronometer, new LinearLayout.LayoutParams(
				ViewGroup.LayoutParams.WRAP_CONTENT, 
				ViewGroup.LayoutParams.WRAP_CONTENT, 
				0));
		
		Button btnSave = (Button)findViewById(R.id.btnSave);
		btnSave.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				finishSaving();
			}
		});
		
		Button btnDiscard = (Button)findViewById(R.id.btnDiscard);
		btnDiscard.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				finishWithoutSaving();
			}
		});
		
		Bundle extras = getIntent().getExtras();
		if (extras != null) {
			this.user = (User)extras.get(LoginActivity.ARG_USER);
			this.course = (Course)extras.get(CourseSectionFragment.ARG_COURSE);
			this.learningObjectIndex = extras.getInt(LearningObjectSectionFragment.ARG_LEARNING_OBJECT_INDEX);
			this.audioIndex = extras.getInt(LearningObjectSectionFragment.ARG_AUDIO_INDEX);

			if (this.learningObjectIndex >= 0) {
				if (this.audioIndex >= 0) {
					this.audio = this.course.getLearningObjectList().get(this.learningObjectIndex).getAudioList().get(audioIndex);
				}
			}
			
			if (this.audio != null) {
				loadData();
				getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
			} else {
				this.audio = new Audio();
			}
		}
	}

	private void loadData() {
		this.etName.setText(this.audio.getName());
		this.etDescription.setText(this.audio.getDescription());
	}

	private void fetchData() {
		this.audio.setName(this.etName.getText().toString());
		this.audio.setDescription(this.etDescription.getText().toString());
	}
	
	private void finishSaving() {
		fetchData();
		Intent resultIntent = new Intent();
		resultIntent.putExtra(LearningObjectSectionFragment.ARG_AUDIO, AudioEditActivity.this.audio);
		resultIntent.putExtra(LearningObjectSectionFragment.ARG_AUDIO_INDEX, AudioEditActivity.this.audioIndex);
		setResult(Activity.RESULT_OK, resultIntent);
		finish();
	}

	private void finishWithoutSaving() {
		setResult(Activity.RESULT_CANCELED, new Intent());
		finish();
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.audio_edit, menu);
		
		return true;
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (requestCode == AudioDetailActivity.GET_AUDIO_FILE_ACTIVITY_REQUEST_CODE) {
	        if (resultCode == RESULT_OK) {
	        	this.newFileName = data.getDataString().replace("file://", "");
	            showConfirmDialog(getResources().getString(R.string.audio_overwrite_confirm), ACTION_CONFIRM_AUDIO_UPLOAD);
	        } else if (resultCode == RESULT_CANCELED) {
	        	//DO NOTHING
	        } 
	    }
	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		if (this.mediaRecorder != null) {
			this.mediaRecorder.release();
		}
	}
	
	@Override
	protected void onPause() {
		super.onPause();
		if (this.mediaRecorder != null) {
        	this.mediaRecorder.release();
        	this.mediaRecorder = null;
        }
	}
	
	@Override
	public void onBackPressed() {
//		fetchData();
		showConfirmDialog(getResources().getString(R.string.confirm_changes), ACTION_CONFIRM_AUDIO_CHANGES);
	}
	
	@Override
	public void onConfirmDialogReturn(boolean confirm, int returnCode) {
		switch (returnCode) {
		case ACTION_CONFIRM_AUDIO_CHANGES:
			this.confirmDialog.dismiss();
			if (confirm) {
				finishSaving();
			} else {
				finishWithoutSaving();
			}
			
			break;
			
		case ACTION_CONFIRM_AUDIO_UPLOAD:
			if (confirm) {
				uploadSelectedFile();
	     	} else {
	     		this.newFileName = "";
	     	}
			
			break;
		}
	}
	
	@Override
	public void onUploadFileTaskDone(int returnCode) {
		Message msg = new Message();
		msg.what = UploadFileTask.UPLOAD_DONE;
		if (returnCode == UploadFileTask.FILE_UPLOADED_SUCCESSFULY) {
			msg.obj = getResources().getString(R.string.successfull_upload);
			this.newFileName = General.getIdFileName(this.newFileName, this.audio.getId());
			this.audio.setContentFileName(this.newFileName.substring(this.newFileName.lastIndexOf("/") + 1));
			//TODO save the document modification
		} else {
			msg.obj = getResources().getString(R.string.problems_uploading_file);
		}
		this.newFileName = "";
		callBackHandler.sendMessage(msg);
	}
	
	@Override
	public void onBlink(int what) {
		callBackHandler.sendEmptyMessage(what);
	}
	
	@Override
	public void onToggleImageButtonClick(boolean isMediaRecording) {
		if (isMediaRecording) {
			startRecording();
		} else {
			stopRecording();
		}
	}
	
	private void stopPlaying() {
		this.audioChronometer.stop();
		this.audioChronometer.setText("00:00");
		if (this.mediaPlayer != null) {
			this.mediaPlayer.release();
			this.mediaPlayer = null;
		}
		if (this.ibAudioRecord != null) {
			this.ibAudioRecord.setEnabled(true);
		}
		if (this.ibAudioUpload != null) {
			this.ibAudioUpload.setEnabled(true);
		}
	}

	private void startPlaying() {
		if (this.ibAudioRecord != null) {
			this.ibAudioRecord.setEnabled(false);
		}
		if (this.ibAudioUpload != null) {
			this.ibAudioUpload.setEnabled(false);
		}
		this.mediaPlayer = new MediaPlayer();
		this.mediaPlayer.setOnCompletionListener(new OnCompletionListener() {
			
			@Override
			public void onCompletion(MediaPlayer mp) {
				AudioEditActivity.this.ibAudioPlay.onClick();
			}
		});
		try {
			this.mediaPlayer.setDataSource(getResources().getString(R.string.host_file_server) 
					+ getResources().getString(R.string.audio_folder) 
					+ "/" + this.audio.getContentFileName());
			this.mediaPlayer.prepare();
			this.audioChronometer.setBase(SystemClock.elapsedRealtime());
			this.mediaPlayer.start();
			this.audioChronometer.start();
		} catch (IOException e) {
			Log.e(LOG_TAG, "prepare() failed");
			this.ibAudioPlay.toggleButton();
			stopPlaying();
		}
	}
	
	private void stopRecording() {
		this.audioChronometer.stop();
		this.audioChronometer.setText("00:00");
		this.ivBlinkingImage.stopBlinking();
		if (this.mediaRecorder != null) {
			this.mediaRecorder.stop();
			this.mediaRecorder.release();
			this.mediaRecorder = null;
		}
		this.ibAudioUpload.setEnabled(true);
		this.ibAudioPlay.setEnabled(true);
		
        showConfirmDialog(getResources().getString(R.string.audio_overwrite_confirm), ACTION_CONFIRM_AUDIO_UPLOAD);
	}

	private void startRecording() {
		this.ibAudioPlay.setEnabled(false);
		this.ibAudioUpload.setEnabled(false);
	    this.mediaRecorder = new MediaRecorder();
	    this.mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
	    this.mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
	    
	    if (this.audio.getContentFileName() != null) {
	    	this.newFileName = Environment.getExternalStorageDirectory() + "/DalOOC/" + this.audio.getContentFileName();
	    } else {
	    	this.newFileName = Environment.getExternalStorageDirectory() + "/DalOOC/" + this.audio.getId() + ".3gp";
	    }
	    
	    this.mediaRecorder.setOutputFile(this.newFileName);
	    this.mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);

        try {
        	this.mediaRecorder.prepare();
        	this.audioChronometer.setBase(SystemClock.elapsedRealtime());
        	this.mediaRecorder.start();
        	this.audioChronometer.start();
        	this.ivBlinkingImage.startBlinking();
        } catch (IOException e) {
            Log.e(LOG_TAG, "prepare() failed");
            this.ibAudioRecord.toggleButton();
            stopRecording();
        }
	}
	
	private void uploadSelectedFile() {
		showProgress(true, getResources().getString(R.string.upload_file_in_progress));
		UploadFileTask uploadFileTask = new UploadFileTask();
		uploadFileTask.setOnUploadFileTaskDoneListener(this);
		uploadFileTask.execute(this.newFileName, 
				getResources().getString(R.string.audio_folder),
				this.audio.getId(),
				getResources().getString(R.string.url_upload_file_servlet));
	}
	
	private void showProgress(boolean show, String msg) {
		this.llUploadPreviewStatus.setVisibility(show ? View.VISIBLE : View.GONE);
		this.llForm.setVisibility(show ? View.GONE : View.VISIBLE);
		this.tvUploadStatusMessage.setText(msg);
	}
	
	private void showToast(String msg) {
		if (this.toast == null) {
			this.toast = Toast.makeText(this, "", Toast.LENGTH_LONG);
		}
		this.toast.setText(msg);
		this.toast.show();
	}
	
	private void showConfirmDialog(String message, int returnCode) {
        FragmentManager fm = getSupportFragmentManager();
        
        Bundle args = new Bundle();
        args.putString(ConfirmDialog.ARG_TITLE, getResources().getString(R.string.dialog_title_audio));
        args.putString(ConfirmDialog.ARG_MESSAGE, message);
        args.putInt(ConfirmDialog.ARG_RETURN_CODE, returnCode);
        
        confirmDialog = new ConfirmDialog();
        confirmDialog.setArguments(args);
        confirmDialog.setOnConfirmDialogResultListener(this);
        confirmDialog.show(fm, "fragment_edit_name");
    }
}
