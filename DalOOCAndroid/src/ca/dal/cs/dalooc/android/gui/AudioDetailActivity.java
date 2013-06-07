package ca.dal.cs.dalooc.android.gui;

import java.io.File;
import java.io.IOException;

import android.annotation.SuppressLint;
import android.app.Activity;
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
import android.os.SystemClock;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.MimeTypeMap;
import android.widget.Chronometer;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import ca.dal.cs.android.dalooc.R;
import ca.dal.cs.dalooc.model.Audio;

public class AudioDetailActivity extends Activity implements RecordingBlinkImageViewCallBack {
	
	private static final String LOG_TAG = "AudioDetailActivity";
	
	private static final int GET_AUDIO_FILE_ACTIVITY_REQUEST_CODE = 500;
	
	public static final int IMAGE_RECORDING = 0;
	
	public static final int IMAGE_RECORDING_DARK = 1;
	
	private Audio audio;
	
	private MediaRecorder mediaRecorder = null;
	
	private MediaPlayer mediaPlayer = null;
	
	private Chronometer audioChronometer;
	
	private PlayAudioImageButton ibAudioPlay;

	private RecordAudioImageButton ibAudioRecord;
	
	private ImageButton ibAudioUpload;

	private ImageButton ibAudioDownload;
	
	private RecordingBlinkImageView ivBlinkingImage;
	
	private DownloadManager dm;
	
	private long enqueue;
	
	private BroadcastReceiver receiver;
	
	private Handler callBackHandler = new Handler() {
		public void handleMessage(android.os.Message msg) {
			switch (msg.what) {
			case AudioDetailActivity.IMAGE_RECORDING:
				AudioDetailActivity.this.ivBlinkingImage.setImageDrawable(AudioDetailActivity.this.getResources().getDrawable(R.drawable.ic_audio_recording));
				break;
			case AudioDetailActivity.IMAGE_RECORDING_DARK:
				AudioDetailActivity.this.ivBlinkingImage.setImageDrawable(AudioDetailActivity.this.getResources().getDrawable(R.drawable.ic_audio_recording_dark));
				break;
			}
		}
	};
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_audio_detail);
	
		this.audio = (Audio)getIntent().getExtras().getSerializable(LearningObjectSectionFragment.ARG_AUDIO);
		
		TextView txtAudioItemName = (TextView)findViewById(R.id.txtAudioItemName);
		txtAudioItemName.setText(this.audio.getName());

		TextView txtAudioItemDescription = (TextView)findViewById(R.id.txtAudioItemDescription);
		txtAudioItemDescription.setText(this.audio.getDescription());
		
		this.ibAudioPlay = new PlayAudioImageButton(this);
		this.ibAudioRecord = new RecordAudioImageButton(this);
		
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
		
		ibAudioDownload = new ImageButton(this);
		ibAudioDownload.setImageDrawable(getResources().getDrawable(R.drawable.ic_download));
		ibAudioDownload.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				File destDocFile = new File(Environment.getExternalStorageDirectory() + "/Download" + AudioDetailActivity.this.audio.getAudioUrl().replace("/DalOOC",""));
				AudioDetailActivity.this.dm = (DownloadManager) getSystemService(DOWNLOAD_SERVICE);
				Request request = new Request(Uri.parse(AudioDetailActivity.this.getResources().getString(R.string.host_file_server) 
						+ AudioDetailActivity.this.audio.getAudioUrl()));
				request.setDestinationUri(Uri.fromFile(destDocFile));
				enqueue = dm.enqueue(request);			
			}
		});
		
		LinearLayout llAudioDetail = (LinearLayout)findViewById(R.id.llAudioDetail);
		llAudioDetail.setGravity(Gravity.CENTER);
		
		llAudioDetail.addView(ibAudioPlay, new LinearLayout.LayoutParams(
				ViewGroup.LayoutParams.WRAP_CONTENT, 
				ViewGroup.LayoutParams.WRAP_CONTENT, 
				0));
		
		llAudioDetail.addView(ibAudioRecord, new LinearLayout.LayoutParams(
				ViewGroup.LayoutParams.WRAP_CONTENT, 
				ViewGroup.LayoutParams.WRAP_CONTENT, 
				0));
		
		llAudioDetail.addView(ibAudioUpload, new LinearLayout.LayoutParams(
				ViewGroup.LayoutParams.WRAP_CONTENT, 
				ViewGroup.LayoutParams.WRAP_CONTENT, 
				0));

		llAudioDetail.addView(ibAudioDownload, new LinearLayout.LayoutParams(
				ViewGroup.LayoutParams.WRAP_CONTENT, 
				ViewGroup.LayoutParams.WRAP_CONTENT, 
				0));
		
		this.ivBlinkingImage = new RecordingBlinkImageView(this);

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
            				String extension = MimeTypeMap.getFileExtensionFromUrl(audio.getAudioUrl());
            				
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
	
	private void onRecord(boolean start) {
        if (start) {
            startRecording();
        } else {
            stopRecording();
        }
    }
	
	private void onPlay(boolean start) {
        if (start) {
            startPlaying();
        } else {
            stopPlaying();
        }
    }

	private void stopPlaying() {
		this.audioChronometer.stop();
//		this.ivBlinkingImage.stopBlinking();
		this.mediaPlayer.release();
		this.mediaPlayer = null;
		this.ibAudioRecord.setEnabled(true);
		this.ibAudioUpload.setEnabled(true);
		this.ibAudioDownload.setEnabled(true);
	}

	private void startPlaying() {
		this.ibAudioRecord.setEnabled(false);
		this.ibAudioUpload.setEnabled(false);
		this.ibAudioDownload.setEnabled(false);
		this.mediaPlayer = new MediaPlayer();
		this.mediaPlayer.setOnCompletionListener(new OnCompletionListener() {
			
			@Override
			public void onCompletion(MediaPlayer mp) {
				AudioDetailActivity.this.ibAudioPlay.onClick();
			}
		});
		try {
			this.mediaPlayer.setDataSource(getResources().getString(R.string.host_file_server) + this.audio.getAudioUrl());
			this.mediaPlayer.prepare();
			this.audioChronometer.setBase(SystemClock.elapsedRealtime());
			this.mediaPlayer.start();
			this.audioChronometer.start();
//			this.ivBlinkingImage.startBlinking();
		} catch (IOException e) {
			Log.e(LOG_TAG, "prepare() failed");
		}
	}

	private void stopRecording() {
		this.audioChronometer.stop();
		this.ivBlinkingImage.stopBlinking();
		this.mediaRecorder.stop();
		this.mediaRecorder.release();
		this.mediaRecorder = null;
		this.ibAudioPlay.setEnabled(true);
		this.ibAudioUpload.setEnabled(true);
		this.ibAudioDownload.setEnabled(true);
	}

	private void startRecording() {
		this.ibAudioPlay.setEnabled(false);
		this.ibAudioUpload.setEnabled(false);
		this.ibAudioDownload.setEnabled(false);
	    this.mediaRecorder = new MediaRecorder();
	    this.mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
	    this.mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
	    this.mediaRecorder.setOutputFile(Environment.getExternalStorageDirectory() + this.audio.getAudioUrl());
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

	class PlayAudioImageButton extends ImageButton {
		private boolean mediaStartPlaying = true;

		public PlayAudioImageButton(Context ctx) {
			super(ctx);
			setImageDrawable(AudioDetailActivity.this.getResources().getDrawable(R.drawable.ic_media_play));
			
			setOnClickListener(new View.OnClickListener() {
				public void onClick(View v) {
					PlayAudioImageButton.this.onClick();
				}
			});
		}
		
		public void onClick() {
			onPlay(PlayAudioImageButton.this.mediaStartPlaying);
			if (PlayAudioImageButton.this.mediaStartPlaying) {
				setImageDrawable(AudioDetailActivity.this.getResources().getDrawable(R.drawable.ic_media_stop));
			} else {
				setImageDrawable(AudioDetailActivity.this.getResources().getDrawable(R.drawable.ic_media_play));
			}
			PlayAudioImageButton.this.mediaStartPlaying = !PlayAudioImageButton.this.mediaStartPlaying;
		}
	}
	
	class RecordAudioImageButton extends ImageButton {
		
		private boolean mediaStartRecording = true;

		public RecordAudioImageButton(Context ctx) {
			super(ctx);
			setImageDrawable(AudioDetailActivity.this.getResources().getDrawable(R.drawable.ic_audio_record));
			
			setOnClickListener(new View.OnClickListener() {
				public void onClick(View v) {
					RecordAudioImageButton.this.onClick();
				}
			});
		}
		
		public void onClick() {
			onRecord(RecordAudioImageButton.this.mediaStartRecording);
			if (RecordAudioImageButton.this.mediaStartRecording) {
				setImageDrawable(AudioDetailActivity.this.getResources().getDrawable(R.drawable.ic_media_stop));
			} else {
				setImageDrawable(AudioDetailActivity.this.getResources().getDrawable(R.drawable.ic_audio_record));
			}
			RecordAudioImageButton.this.mediaStartRecording = !RecordAudioImageButton.this.mediaStartRecording;
		}
	}
	
	class RecordingBlinkImageView extends ImageView {
		
		private Thread blinkButton;
		
		private boolean isBlinking = false;

		public RecordingBlinkImageView(Context context) {
			super(context);
//			setBackgroundColor(Color.DKGRAY);
			setImageDrawable(AudioDetailActivity.this.getResources().getDrawable(R.drawable.ic_audio_recording_dark));
		}
		
		public void startBlinking() {
			RecordingBlinkImageView.this.setImageDrawable(AudioDetailActivity.this.getResources().getDrawable(R.drawable.ic_audio_recording));

			this.blinkButton = new Thread(new Runnable() {
				
				@Override
				public void run() {
					RecordingBlinkImageView.this.isBlinking = true;
					boolean light = false;
					while (RecordingBlinkImageView.this.isBlinking) {
						if (!light) {
							AudioDetailActivity.this.blinkImage(AudioDetailActivity.IMAGE_RECORDING);
							light = true;
							try {
								Thread.sleep(500);
							} catch (InterruptedException e) {}
						} else {
							AudioDetailActivity.this.blinkImage(AudioDetailActivity.IMAGE_RECORDING_DARK);
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
			AudioDetailActivity.this.blinkImage(AudioDetailActivity.IMAGE_RECORDING_DARK);
		}
	}
	

	@Override
	public void blinkImage(int what) {
		callBackHandler.sendEmptyMessage(what);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
	    if (requestCode == GET_AUDIO_FILE_ACTIVITY_REQUEST_CODE) {
	        if (resultCode == RESULT_OK) {
	        	ConfirmDialogFragment confirm = new ConfirmDialogFragment();
	        	Bundle bundle = new Bundle();
	        	bundle.putInt(ConfirmDialogFragment.ARG_CONFIRM_MESSAGE, ConfirmDialogFragment.AUDIO_CONFIRM_MESSAGE);
	        	confirm.setArguments(bundle);
	        	confirm.show(getFragmentManager(), "Confirm");
	        	if (confirm.getLastChoice()) {
	        		this.audio.setAudioUrl(data.getDataString().replace("file://", ""));
	         	} else {
	         		//DO NOTHING
	         	}
	        } else if (resultCode == RESULT_CANCELED) {
	            // User cancelled the video capture
	        	//DO NOTHING
	        } else {
	            // Video capture failed, advise user
	        	//TODO
	        }
	    }
	}
}
