package ca.dal.cs.dalooc.android.gui;

import java.io.File;

import android.annotation.SuppressLint;
import android.app.DownloadManager;
import android.app.DownloadManager.Query;
import android.app.DownloadManager.Request;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.provider.MediaStore;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.NotificationCompat;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.MimeTypeMap;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import ca.dal.cs.android.dalooc.R;
import ca.dal.cs.dalooc.android.gui.listener.OnConfirmDialogReturnListener;
import ca.dal.cs.dalooc.android.gui.listener.OnUploadFileTaskDoneListener;
import ca.dal.cs.dalooc.android.util.DownloadImageTask;
import ca.dal.cs.dalooc.android.util.General;
import ca.dal.cs.dalooc.android.util.UploadFileTask;
import ca.dal.cs.dalooc.model.User;
import ca.dal.cs.dalooc.model.Video;

@SuppressLint("ShowToast")
public class VideoDetailActivity extends FragmentActivity implements OnUploadFileTaskDoneListener, OnConfirmDialogReturnListener {
	 
	private static final int CAPTURE_VIDEO_ACTIVITY_REQUEST_CODE = 200;
	
	private static final int GET_VIDEO_FILE_ACTIVITY_REQUEST_CODE = 400;
	
	private Video video;
	
	private User user;
	
	private long enqueue;
	
	private DownloadManager dm;
	
	private LinearLayout llDownloadPreviewStatus;
	
	private RelativeLayout rlVideoDetailForm;
	
	private TextView tvDownloadPreviewStatusMessage;
	
	private ImageButton ibVideoPlay;
	
	private ImageButton ibVideoRecord;
	
	private ImageButton ibVideoUpload;
	
	private ImageButton ibVideoDownload;
	
	private ImageView ivVideoDetailThumbnail;

	private ConfirmDialog confirmDialog;
	
	private String newFileName = "";
	
	private Toast toast;
	
	private BroadcastReceiver receiver;
	
	@SuppressLint("HandlerLeak")
	private Handler callBackHandler = new Handler() {
		public void handleMessage(android.os.Message msg) {
			switch (msg.what) {
			case UploadFileTask.UPLOAD_DONE:
				showProgress(false, "");
				break;
			}
		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_video_detail);
	
		this.video = (Video)getIntent().getExtras().getSerializable(LearningObjectSectionFragment.ARG_VIDEO);
		byte[] videoThumbnail = (byte[])getIntent().getExtras().getSerializable(LearningObjectSectionFragment.ARG_VIDEO_THUMBNAIL);
		this.user = (User)getIntent().getExtras().getSerializable(LoginActivity.ARG_USER);
		
		this.toast = Toast.makeText(this, "", Toast.LENGTH_LONG);

		this.tvDownloadPreviewStatusMessage = (TextView)findViewById(R.id.tvDownloadPreviewStatusMessage);
		this.llDownloadPreviewStatus = (LinearLayout)findViewById(R.id.llDownloadPreviewStatus);
		this.rlVideoDetailForm = (RelativeLayout)findViewById(R.id.rlVideoDetailForm);
		
		this.ivVideoDetailThumbnail = (ImageView)findViewById(R.id.ivVideoDetailThumbnail);
		
		Bitmap b = null;
		if (videoThumbnail != null) {
			b = BitmapFactory.decodeByteArray(videoThumbnail, 0, videoThumbnail.length);
//			Bitmap bitmap = ThumbnailUtils.createVideoThumbnail(getResources().getString(R.string.host_file_server) + video.getVideoUrl(), MediaStore.Video.Thumbnails.MICRO_KIND);
		}
		
		if (b != null) {
			ivVideoDetailThumbnail.setImageBitmap(b);
		}
		
		TextView txtVideoItemName = (TextView)findViewById(R.id.txtVideoItemName);
		txtVideoItemName.setText(this.video.getName());

		TextView txtVideoItemDescription = (TextView)findViewById(R.id.txtVideoItemDescription);
		txtVideoItemDescription.setText(this.video.getDescription());
		
		LinearLayout llVideoDetail = (LinearLayout)findViewById(R.id.llVideoDetail);
		
		ibVideoPlay = new ImageButton(this);
		ibVideoPlay.setImageDrawable(getResources().getDrawable(R.drawable.ic_media_play));
		ibVideoPlay.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				Intent videoPlayIntent = new Intent(Intent.ACTION_VIEW);
				String mimeType = "application/*";
				String extension = MimeTypeMap.getFileExtensionFromUrl(video.getVideoUrl());
				
				if (MimeTypeMap.getSingleton().hasExtension(extension)) {
					mimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension);
					
				}
				
				videoPlayIntent.setDataAndType(Uri.parse(getResources().getString(R.string.host_file_server) 
						+ getResources().getString(R.string.videos_folder) 
						+ "/" + video.getVideoUrl()), mimeType);
				videoPlayIntent.putExtra(MediaStore.EXTRA_FINISH_ON_COMPLETION, true);
				try {
                    startActivity(videoPlayIntent);
                } 
                catch (ActivityNotFoundException e) {
                    Toast.makeText(VideoDetailActivity.this, 
                        getResources().getString(R.string.no_application_available), 
                        Toast.LENGTH_LONG).show();
                }		
			}
		});
		
		llVideoDetail.addView(ibVideoPlay, new LinearLayout.LayoutParams(
				ViewGroup.LayoutParams.WRAP_CONTENT, 
				ViewGroup.LayoutParams.WRAP_CONTENT, 
				0));
		
		if (this.user.getUserType().equals(User.UserType.PROFESSOR)) {
			ibVideoRecord = new ImageButton(this);
			ibVideoRecord.setImageDrawable(getResources().getDrawable(R.drawable.ic_video_record));
			ibVideoRecord.setOnClickListener(new View.OnClickListener() {
				
				@SuppressLint("SimpleDateFormat")
				@Override
				public void onClick(View v) {
					Intent cameraIntent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
					
					File videoFolder = new File(Environment.getExternalStorageDirectory(), "DalOOC");
					videoFolder.mkdirs();
					File videoFile = new File(videoFolder, video.getId() + ".mp4");
					
					VideoDetailActivity.this.video.setVideoUrl(video.getId() + ".mp4");
					
					Uri uriSavedVideoFile = Uri.fromFile(videoFile);
					
					cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, uriSavedVideoFile);
					cameraIntent.putExtra(MediaStore.EXTRA_VIDEO_QUALITY, 1);
	
					startActivityForResult(cameraIntent, CAPTURE_VIDEO_ACTIVITY_REQUEST_CODE);				
				}
			});
			
			llVideoDetail.addView(ibVideoRecord, new LinearLayout.LayoutParams(
					ViewGroup.LayoutParams.WRAP_CONTENT, 
					ViewGroup.LayoutParams.WRAP_CONTENT, 
					0));
			
			ibVideoUpload = new ImageButton(this);
			ibVideoUpload.setImageDrawable(getResources().getDrawable(R.drawable.ic_upload));
			ibVideoUpload.setOnClickListener(new View.OnClickListener() {
				
				@SuppressLint("SimpleDateFormat")
				@Override
				public void onClick(View v) {
					Intent uploadFileIntent = new Intent(Intent.ACTION_GET_CONTENT);
					uploadFileIntent.setType("video/*");
					
					startActivityForResult(uploadFileIntent, GET_VIDEO_FILE_ACTIVITY_REQUEST_CODE);				
				}
			});
			
			llVideoDetail.addView(ibVideoUpload, new LinearLayout.LayoutParams(
					ViewGroup.LayoutParams.WRAP_CONTENT, 
					ViewGroup.LayoutParams.WRAP_CONTENT, 
					0));
		}
		
		ibVideoDownload = new ImageButton(this);
		ibVideoDownload.setImageDrawable(getResources().getDrawable(R.drawable.ic_download));
		ibVideoDownload.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				File destDocFile = new File(Environment.getExternalStorageDirectory() + "/Download/" + VideoDetailActivity.this.video.getVideoUrl());
				VideoDetailActivity.this.dm = (DownloadManager) getSystemService(DOWNLOAD_SERVICE);
				Request request = new Request(Uri.parse(VideoDetailActivity.this.getResources().getString(R.string.host_file_server) 
						+ VideoDetailActivity.this.getResources().getString(R.string.videos_folder)
						+ "/" + VideoDetailActivity.this.video.getVideoUrl()));
				request.setDestinationUri(Uri.fromFile(destDocFile));
				enqueue = dm.enqueue(request);			
			}
		});

		llVideoDetail.addView(ibVideoDownload, new LinearLayout.LayoutParams(
				ViewGroup.LayoutParams.WRAP_CONTENT, 
				ViewGroup.LayoutParams.WRAP_CONTENT, 
				0));
		
		this.receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                if (DownloadManager.ACTION_DOWNLOAD_COMPLETE.equals(action)) {
                    Query query = new Query();
                    query.setFilterById(VideoDetailActivity.this.enqueue);
                    Cursor c = VideoDetailActivity.this.dm.query(query);
                    if (c.moveToFirst()) {
                        int columnIndex = c.getColumnIndex(DownloadManager.COLUMN_STATUS);
                        if (DownloadManager.STATUS_SUCCESSFUL == c.getInt(columnIndex)) {
                        	Intent videoPreviewIntent = new Intent(Intent.ACTION_VIEW);
                        	String mimeType = "application/video";
            				String extension = MimeTypeMap.getFileExtensionFromUrl(video.getVideoUrl());
            				
            				if (MimeTypeMap.getSingleton().hasExtension(extension)) {
            					mimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension);
            				}
                        	
            				videoPreviewIntent.setDataAndType(Uri.parse(c.getString(c.getColumnIndex(DownloadManager.COLUMN_LOCAL_URI))), mimeType);
							PendingIntent pit = PendingIntent.getActivity(context, 0, videoPreviewIntent, 0);

							Notification notificacao = new NotificationCompat.Builder(
									context)
									.setTicker(VideoDetailActivity.this.getResources().getString(R.string.download_complete))
									.setContentTitle(c.getString(c.getColumnIndex(DownloadManager.COLUMN_TITLE)))
									.setContentIntent(pit)
									.setContentText(VideoDetailActivity.this.getResources().getString(R.string.download_complete))
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
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
	    if (requestCode == CAPTURE_VIDEO_ACTIVITY_REQUEST_CODE) {
	    	//TODO Implement the return threatment
	        if (resultCode == RESULT_OK) {
	        	new DownloadImageTask(this.ivVideoDetailThumbnail)
				.execute(getResources().getString(R.string.host_file_server)
						+ getResources().getString(R.string.videos_folder)
						+ "/thumb/" + video.getVideoUrl().replace(video.getVideoUrl().substring(video.getVideoUrl().lastIndexOf(".")), "jpg"));
	        } else if (resultCode == RESULT_CANCELED) {
	            // User cancelled the video capture
	        } else {
	            // Video capture failed, advise user
	        }
	    } else if (requestCode == GET_VIDEO_FILE_ACTIVITY_REQUEST_CODE) {
	        if (resultCode == RESULT_OK) {
	        	this.newFileName = data.getDataString().replace("file://", "");

	        	FragmentManager fm = getSupportFragmentManager();
	            
	            Bundle args = new Bundle();
	            args.putString(ConfirmDialog.ARG_MESSAGE, getResources().getString(R.string.document_overwrite_confirm));
	            args.putBoolean(ConfirmDialog.ARG_CANCEL_BUTTON, false);
	            
	            this.confirmDialog = new ConfirmDialog();
	            this.confirmDialog.setArguments(args);
	            this.confirmDialog.setOnConfirmDialogResultListener(this);
	            this.confirmDialog.show(fm, "fragment_edit_name");
	        } else if (resultCode == RESULT_CANCELED) {
	            // User cancelled the video capture
	        } else {
	            // Video capture failed, advise user
	        	//TODO
	        }
	    }
	}
	
	private void showToast(String msg) {
		this.toast.setText(msg);
		this.toast.cancel();
		this.toast.show();
	}
	
	private void showProgress(boolean show, String msg) {
		this.llDownloadPreviewStatus.setVisibility(show ? View.VISIBLE : View.GONE);
		this.rlVideoDetailForm.setVisibility(show ? View.GONE : View.VISIBLE);
		this.tvDownloadPreviewStatusMessage.setText(msg);
	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		unregisterReceiver(this.receiver);
	}
	
	@Override
	public void onConfirmDialogReturn(boolean confirm) {
		if (confirm) {
			showProgress(true, getResources().getString(R.string.upload_file_in_progress));
    		UploadFileTask uploadFileTask = new UploadFileTask();
    		uploadFileTask.setOnUploadFileTaskDoneListener(this);
    		uploadFileTask.execute(this.newFileName, getResources().getString(R.string.videos_folder), this.video.getId());
     	} else {
     		this.newFileName = "";
     	}
	}

	@Override
	public void onUploadFileTaskDone(int returnCode) {
		callBackHandler.sendEmptyMessage(UploadFileTask.UPLOAD_DONE);
		if (returnCode == UploadFileTask.FILE_UPLOADED_SUCCESSFULY) {
			this.newFileName = General.getIdFileName(this.newFileName, this.video.getId());
			this.video.setVideoUrl(this.newFileName.substring(this.newFileName.lastIndexOf("/")));
			showToast(getResources().getString(R.string.successfull_upload));
			//TODO save the document modification
		} else {
			showToast(getResources().getString(R.string.problems_uploading_file));
		}
		this.newFileName = "";
	}
}
