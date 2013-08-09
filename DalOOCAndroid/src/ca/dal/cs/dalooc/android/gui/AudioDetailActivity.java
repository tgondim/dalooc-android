package ca.dal.cs.dalooc.android.gui;

import java.io.File;
import java.io.IOException;

import android.annotation.SuppressLint;
import android.app.DownloadManager;
import android.app.DownloadManager.Query;
import android.app.DownloadManager.Request;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.NotificationCompat;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager.LayoutParams;
import android.webkit.MimeTypeMap;
import android.widget.Chronometer;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import ca.dal.cs.dalooc.android.R;
import ca.dal.cs.dalooc.android.gui.components.ConfirmDialog;
import ca.dal.cs.dalooc.android.gui.components.ToggleImageButton;
import ca.dal.cs.dalooc.android.gui.components.RecordingBlinkImageView;
import ca.dal.cs.dalooc.android.gui.listener.OnConfirmDialogReturnListener;
import ca.dal.cs.dalooc.android.gui.listener.OnToggleImageButtonListener;
import ca.dal.cs.dalooc.android.gui.listener.OnRecordingBlinkListener;
import ca.dal.cs.dalooc.android.gui.listener.OnUploadFileTaskDoneListener;
import ca.dal.cs.dalooc.android.util.General;
import ca.dal.cs.dalooc.android.util.UploadFileTask;
import ca.dal.cs.dalooc.android.webservices.OnUpdateCourseCallDoneListener;
import ca.dal.cs.dalooc.android.webservices.SaveCourseCallRunnable;
import ca.dal.cs.dalooc.android.webservices.UpdateCourseCallRunnable;
import ca.dal.cs.dalooc.model.Audio;
import ca.dal.cs.dalooc.model.Course;
import ca.dal.cs.dalooc.model.User;

public class AudioDetailActivity extends FragmentActivity implements OnRecordingBlinkListener, OnConfirmDialogReturnListener, OnUploadFileTaskDoneListener, OnUpdateCourseCallDoneListener {
	
	private static final String LOG_TAG = "AudioDetailActivity";
	
	public static final int GET_AUDIO_FILE_ACTIVITY_REQUEST_CODE = 500;
	
	private Audio audio;
	
	private User user;

	private Course course;
	
	private int learningObjectIndex;

	private int audioIndex;
	
	private MediaRecorder mediaRecorder = null;
	
	private MediaPlayer mediaPlayer = null;
	
	private Chronometer audioChronometer;
	
	private LinearLayout llDownloadPreviewStatus;
	
	private RelativeLayout rlAudioDetailForm;
	
	private TextView tvDownloadPreviewStatusMessage;
	
	private ToggleImageButton ibAudioPlay;

	private ToggleImageButton ibAudioRecord;
	
	private ImageButton ibAudioUpload;

	private ImageButton ibAudioDownload;
	
	private RecordingBlinkImageView ivBlinkingImage;
	
	private DownloadManager dm;
	
	private ConfirmDialog confirmDialog;
	
	private String newFileName = "";
	
	private Toast toast;
	
	private long enqueue;
	
	private BroadcastReceiver receiver;
	
	private Intent resultIntent;
	
	private boolean contentUpdated; 
	
	@SuppressLint("HandlerLeak")
	private Handler callBackHandler = new Handler() {
		public void handleMessage(android.os.Message msg) {
			switch (msg.what) {
			case RecordingBlinkImageView.IMAGE_RECORDING:
				AudioDetailActivity.this.ivBlinkingImage.setImageDrawable(AudioDetailActivity.this.getResources().getDrawable(R.drawable.ic_audio_recording_light_on));
				break;
			case RecordingBlinkImageView.IMAGE_RECORDING_DARK:
				AudioDetailActivity.this.ivBlinkingImage.setImageDrawable(AudioDetailActivity.this.getResources().getDrawable(R.drawable.ic_audio_recording_light_off));
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
		setContentView(R.layout.activity_audio_detail);
		
		getWindow().setFlags(LayoutParams.FLAG_NOT_TOUCH_MODAL, LayoutParams.FLAG_NOT_TOUCH_MODAL);
		getWindow().setFlags(LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH, LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH);
	
		this.user = (User)getIntent().getExtras().getSerializable(LoginActivity.ARG_USER);
		this.course = (Course)getIntent().getExtras().getSerializable(CourseSectionFragment.ARG_COURSE);
		this.learningObjectIndex = getIntent().getExtras().getInt(LearningObjectSectionFragment.ARG_LEARNING_OBJECT_INDEX);
		this.audioIndex = getIntent().getExtras().getInt(LearningObjectSectionFragment.ARG_AUDIO_INDEX);
		
		this.audio = this.course.getLearningObjectList().get(this.learningObjectIndex).getAudioList().get(this.audioIndex);

		this.tvDownloadPreviewStatusMessage = (TextView)findViewById(R.id.tvDownloadPreviewStatusMessage);
		this.llDownloadPreviewStatus = (LinearLayout)findViewById(R.id.llDownloadPreviewStatus);
		this.rlAudioDetailForm = (RelativeLayout)findViewById(R.id.rlAudioDetailForm);
		
		TextView txtAudioItemName = (TextView)findViewById(R.id.txtAudioItemName);
		txtAudioItemName.setText(this.audio.getName());

		TextView txtAudioItemDescription = (TextView)findViewById(R.id.txtAudioItemDescription);
		txtAudioItemDescription.setText(this.audio.getDescription());
		
		LinearLayout llAudioDetail = (LinearLayout)findViewById(R.id.llAudioDetail);
		llAudioDetail.setGravity(Gravity.CENTER);
		
		this.ibAudioPlay = new ToggleImageButton(this, 
				getResources().getDrawable(R.drawable.ic_media_play), 
				getResources().getDrawable(R.drawable.ic_media_stop));
		this.ibAudioPlay.setOnRecordAudioImageButtonClick(new OnToggleImageButtonListener() {
			
			@Override
			public void onToggleImageButtonClick(boolean isMediaRecording) {
				if (!TextUtils.isEmpty(AudioDetailActivity.this.audio.getContentFileName())) {
					if (isMediaRecording) {
						startPlaying();
					} else {
						stopPlaying();
					}				
				} else {
					showToast(getResources().getString(R.string.no_file_to_open));
					AudioDetailActivity.this.ibAudioPlay.toggleButton();
				}
			}
		});

		llAudioDetail.addView(ibAudioPlay, new LinearLayout.LayoutParams(
				ViewGroup.LayoutParams.WRAP_CONTENT, 
				ViewGroup.LayoutParams.WRAP_CONTENT, 
				0));
		
		if (this.user.getUserType().equals(User.UserType.PROFESSOR)) {
			this.ibAudioRecord = new ToggleImageButton(this, 
					getResources().getDrawable(R.drawable.ic_audio_record), 
					getResources().getDrawable(R.drawable.ic_media_stop));
			this.ibAudioRecord.setOnRecordAudioImageButtonClick(new OnToggleImageButtonListener() {
				
				@Override
				public void onToggleImageButtonClick(boolean isMediaRecording) {
					if (isMediaRecording) {
						startRecording();
					} else {
						stopRecording();
					}					
				}
			});
			
			llAudioDetail.addView(ibAudioRecord, new LinearLayout.LayoutParams(
					ViewGroup.LayoutParams.WRAP_CONTENT, 
					ViewGroup.LayoutParams.WRAP_CONTENT, 
					0));
			
			ibAudioUpload = new ImageButton(this);
			ibAudioUpload.setImageDrawable(getResources().getDrawable(R.drawable.ic_upload));
			ibAudioUpload.setOnClickListener(new View.OnClickListener() {
				
				@SuppressLint("SimpleDateFormat")
				@Override
				public void onClick(View v) {
					Intent uploadFileIntent = new Intent(Intent.ACTION_GET_CONTENT);
					uploadFileIntent.setType("audio/*");
					
					startActivityForResult(uploadFileIntent, GET_AUDIO_FILE_ACTIVITY_REQUEST_CODE);				
				}
			});
			
			llAudioDetail.addView(ibAudioUpload, new LinearLayout.LayoutParams(
					ViewGroup.LayoutParams.WRAP_CONTENT, 
					ViewGroup.LayoutParams.WRAP_CONTENT, 
					0));
		}
		
		ibAudioDownload = new ImageButton(this);
		ibAudioDownload.setImageDrawable(getResources().getDrawable(R.drawable.ic_download));
		ibAudioDownload.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				if (!TextUtils.isEmpty(AudioDetailActivity.this.audio.getContentFileName())) {
					File destDocFile = new File(Environment.getExternalStorageDirectory() + "/Download/" + AudioDetailActivity.this.audio.getContentFileName());
					AudioDetailActivity.this.dm = (DownloadManager) getSystemService(DOWNLOAD_SERVICE);
					Request request = new Request(Uri.parse(AudioDetailActivity.this.getResources().getString(R.string.host_file_server) 
							+ AudioDetailActivity.this.getResources().getString(R.string.audio_folder)
							+ "/" + AudioDetailActivity.this.audio.getContentFileName()));
					request.setDestinationUri(Uri.fromFile(destDocFile));
					enqueue = dm.enqueue(request);		
					showToast(getResources().getString(R.string.download_file_in_progress));
				} else {
					showToast(getResources().getString(R.string.no_file_to_open));
				}
			}
		});
		
		llAudioDetail.addView(ibAudioDownload, new LinearLayout.LayoutParams(
				ViewGroup.LayoutParams.WRAP_CONTENT, 
				ViewGroup.LayoutParams.WRAP_CONTENT, 
				0));
		
		this.ivBlinkingImage = new RecordingBlinkImageView(this);
		this.ivBlinkingImage.setOnRecordingBlinkListener(this);

		LinearLayout llAudioDetailBelow = (LinearLayout)findViewById(R.id.llAudioDetailBelow);
		llAudioDetailBelow.setGravity(Gravity.CENTER);
		
		LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
		params.gravity = Gravity.CENTER_VERTICAL;
		params.setMargins(20, 0, 0, 0);
		
		llAudioDetailBelow.addView(this.ivBlinkingImage, params);
		
		this.audioChronometer = new Chronometer(this);
		this.audioChronometer.setTextAppearance(this, android.R.style.TextAppearance_Large);

		params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
		params.gravity = Gravity.CENTER_VERTICAL;
		params.setMargins(20, 0, 0, 0);

		llAudioDetailBelow.addView(this.audioChronometer, params);
		
		this.receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                if (DownloadManager.ACTION_DOWNLOAD_COMPLETE.equals(action)) {
                    Query query = new Query();
                    query.setFilterById(AudioDetailActivity.this.enqueue);
                    Cursor c = AudioDetailActivity.this.dm.query(query);
                    if (c.moveToFirst()) {
                        int columnIndex = c.getColumnIndex(DownloadManager.COLUMN_STATUS);
                        if (DownloadManager.STATUS_SUCCESSFUL == c.getInt(columnIndex)) {
                        	Intent audioPreviewIntent = new Intent(Intent.ACTION_VIEW);
                        	String mimeType = "application/*";
            				String extension = MimeTypeMap.getFileExtensionFromUrl(audio.getContentFileName());
            				
            				if (MimeTypeMap.getSingleton().hasExtension(extension)) {
            					mimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension);
            				}
                        	
            				audioPreviewIntent.setDataAndType(Uri.parse(c.getString(c.getColumnIndex(DownloadManager.COLUMN_LOCAL_URI))), mimeType);
							PendingIntent pit = PendingIntent.getActivity(context, 0, audioPreviewIntent, 0);

							Notification notificacao = new NotificationCompat.Builder(
									context)
									.setTicker(AudioDetailActivity.this.getResources().getString(R.string.download_complete))
									.setContentTitle(c.getString(c.getColumnIndex(DownloadManager.COLUMN_TITLE)))
									.setContentIntent(pit)
									.setContentText(AudioDetailActivity.this.getResources().getString(R.string.download_complete))
									.setSmallIcon(R.drawable.ic_download_complete)
									.setAutoCancel(true).build();

							NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
							manager.notify(1, notificacao);
                        	
                        }
                    }
                }
            }
        };

        registerReceiver(receiver, new IntentFilter( DownloadManager.ACTION_DOWNLOAD_COMPLETE));
	}
	
	@Override
    public void onPause() {
        super.onPause();
        if (this.mediaRecorder != null) {
        	this.mediaRecorder.release();
        	this.mediaRecorder = null;
        }

        if (this.mediaPlayer != null) {
        	this.mediaPlayer.release();
        	this.mediaPlayer = null;
        }
        this.audioChronometer.stop();
    }
	
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		  if (event.getAction() == MotionEvent.ACTION_OUTSIDE) {
              if (llDownloadPreviewStatus.getVisibility() != View.VISIBLE) {
            	  onBackPressed();
              }
              return true;
          }
          return false;
	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		if (this.mediaPlayer != null) {
			this.mediaPlayer.release();
		}
		if (this.mediaRecorder != null) {
			this.mediaRecorder.release();
		}
		unregisterReceiver(this.receiver);
	}
	
	@Override
	public void onBackPressed() {
		if (this.contentUpdated) {
			this.course.getLearningObjectList().get(this.learningObjectIndex).getAudioList().set(this.audioIndex, this.audio);
			
			getResultIntent().putExtra(CourseActivity.ARG_COURSE, this.course);
			setResult(LearningObjectActivity.DETAIL_ACTIVITY_CALL, getResultIntent());
		}
		finish();
	}
	
	private Intent getResultIntent() {
		if (this.resultIntent == null) {
			this.resultIntent = new Intent();
		}
		return this.resultIntent;
	}
	
	private void stopPlaying() {
		this.audioChronometer.stop();
		this.audioChronometer.setText("00:00");
		this.mediaPlayer.release();
		this.mediaPlayer = null;
		if (this.ibAudioRecord != null) {
			this.ibAudioRecord.setEnabled(true);
		}
		if (this.ibAudioUpload != null) {
			this.ibAudioUpload.setEnabled(true);
		}
		this.ibAudioDownload.setEnabled(true);
	}

	private void startPlaying() {
		if (this.ibAudioRecord != null) {
			this.ibAudioRecord.setEnabled(false);
		}
		if (this.ibAudioUpload != null) {
			this.ibAudioUpload.setEnabled(false);
		}
		this.ibAudioDownload.setEnabled(false);
		this.mediaPlayer = new MediaPlayer();
		this.mediaPlayer.setOnCompletionListener(new OnCompletionListener() {
			
			@Override
			public void onCompletion(MediaPlayer mp) {
				AudioDetailActivity.this.ibAudioPlay.onClick();
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
		}
	}

	private void stopRecording() {
		this.audioChronometer.stop();
		this.audioChronometer.setText("00:00");
		this.ivBlinkingImage.stopBlinking();
		this.mediaRecorder.stop();
		this.mediaRecorder.release();
		this.mediaRecorder = null;
		this.ibAudioPlay.setEnabled(true);
		this.ibAudioUpload.setEnabled(true);
		this.ibAudioDownload.setEnabled(true);
		
        getConfirmDialog().show(getSupportFragmentManager(), "fragment_edit_name");
	}

	private void startRecording() {
		this.ibAudioPlay.setEnabled(false);
		this.ibAudioUpload.setEnabled(false);
		this.ibAudioDownload.setEnabled(false);
	    this.mediaRecorder = new MediaRecorder();
	    this.mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
	    this.mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
	    this.newFileName = Environment.getExternalStorageDirectory() + "/DalOOC/" + this.audio.getContentFileName();
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
        }
	}
	
	private void showToast(String msg) {
		if (this.toast == null) {
			this.toast = Toast.makeText(this, "", Toast.LENGTH_LONG);
		}
		this.toast.setText(msg);
		this.toast.show();
	}
	
	private void showProgress(boolean show, String msg) {
		this.llDownloadPreviewStatus.setVisibility(show ? View.VISIBLE : View.GONE);
		this.rlAudioDetailForm.setVisibility(show ? View.GONE : View.VISIBLE);
		this.tvDownloadPreviewStatusMessage.setText(msg);
	}
	
	@Override
	public void onBlink(int what) {
		callBackHandler.sendEmptyMessage(what);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
	    if (requestCode == GET_AUDIO_FILE_ACTIVITY_REQUEST_CODE) {
	        if (resultCode == RESULT_OK) {
	        	this.newFileName = data.getDataString().replace("file://", "");

	        	FragmentManager fm = getSupportFragmentManager();
	            getConfirmDialog().show(fm, "fragment_edit_name");
	        } else if (resultCode == RESULT_CANCELED) {
	            // User cancelled the video capture
	        	//DO NOTHING
	        } else {
	            // Video capture failed, advise user
	        	//TODO
	        }
	    }
	}

	private ConfirmDialog getConfirmDialog() {
		if (this.confirmDialog == null) {
			Bundle args = new Bundle();
			args.putString(ConfirmDialog.ARG_MESSAGE, getResources().getString(R.string.audio_overwrite_confirm));
			args.putBoolean(ConfirmDialog.ARG_CANCEL_BUTTON, false);
			
			this.confirmDialog = new ConfirmDialog();
			this.confirmDialog.setArguments(args);
			this.confirmDialog.setOnConfirmDialogResultListener(this);
		}
		return this.confirmDialog;
	}
	
	@Override
	public void onConfirmDialogReturn(boolean confirm, int returnCode) {
		if (confirm) {
			uploadSelectedFile();
     	} else {
     		this.newFileName = "";
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

	@Override
	public void onUploadFileTaskDone(int returnCode) {
		Message msg = new Message();
		msg.what = UploadFileTask.UPLOAD_DONE;
		if (returnCode == UploadFileTask.FILE_UPLOADED_SUCCESSFULY) {
			this.newFileName = General.getIdFileName(this.newFileName, this.audio.getId());
			this.audio.setContentFileName(this.newFileName.substring(this.newFileName.lastIndexOf("/") + 1));
			msg.obj = getResources().getString(R.string.successfull_upload);
			this.contentUpdated = true;
			fireUpdateCourseThread();
		} else {
			msg.obj = getResources().getString(R.string.problems_uploading_file);
		}
		callBackHandler.sendMessage(msg);
		this.newFileName = "";
	}
	
	private void fireUpdateCourseThread() {
		UpdateCourseCallRunnable updateCourseCall = new UpdateCourseCallRunnable(this.course, this);
		updateCourseCall.setOnUpdateCourseCallDoneListener(this);
		new Thread(updateCourseCall).start();
	}

	@Override
	public String getUrlWebService(int serviceCode) {
		if (serviceCode == SaveCourseCallRunnable.SAVE_COURSE_WEB_SERVICE) {
			return getResources().getString(R.string.url_webservice) + "/" + getResources().getString(R.string.course_repository) + "/" + getResources().getString(R.string.save_course_webservice_operation); 
		} else if (serviceCode == UpdateCourseCallRunnable.UPDATE_COURSE_WEB_SERVICE) {
			return getResources().getString(R.string.url_webservice) + "/" + getResources().getString(R.string.course_repository) + "/" + getResources().getString(R.string.update_course_webservice_operation);
		}
		return null;
	}
	
	@Override
	public void returnServiceResponse(int serviceCode) {
//		callBackHandler.sendEmptyMessage(0);		
	}
}
