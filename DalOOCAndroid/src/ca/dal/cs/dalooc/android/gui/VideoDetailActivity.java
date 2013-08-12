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
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.NotificationCompat;
import android.text.TextUtils;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager.LayoutParams;
import android.webkit.MimeTypeMap;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import ca.dal.cs.dalooc.android.R;
import ca.dal.cs.dalooc.android.gui.components.ConfirmDialog;
import ca.dal.cs.dalooc.android.gui.listener.OnConfirmDialogReturnListener;
import ca.dal.cs.dalooc.android.gui.listener.OnUploadFileTaskDoneListener;
import ca.dal.cs.dalooc.android.util.DownloadImageTask;
import ca.dal.cs.dalooc.android.util.General;
import ca.dal.cs.dalooc.android.webservice.OnWebServiceCallDoneListener;
import ca.dal.cs.dalooc.android.webservice.SaveCourseCallRunnable;
import ca.dal.cs.dalooc.android.webservice.UpdateCourseCallRunnable;
import ca.dal.cs.dalooc.android.webservice.UploadFileTask;
import ca.dal.cs.dalooc.model.Course;
import ca.dal.cs.dalooc.model.User;
import ca.dal.cs.dalooc.model.Video;

public class VideoDetailActivity extends FragmentActivity implements OnUploadFileTaskDoneListener, OnConfirmDialogReturnListener, OnWebServiceCallDoneListener {
	 
	public static final int CAPTURE_VIDEO_ACTIVITY_REQUEST_CODE = 200;
	
	public static final int GET_VIDEO_FILE_ACTIVITY_REQUEST_CODE = 400;
	
	private Video video;
	
	private User user;
	
	private Course course;
	
	private int learningObjectIndex;

	private int videoIndex;
	
	private long enqueue;
	
	private DownloadManager dm;
	
	private LinearLayout llDownloadPreviewStatus;
	
	private RelativeLayout rlVideoDetailForm;
	
	private TextView tvDownloadPreviewStatusMessage;
	
	private ImageButton ibVideoPlay;
	
	private ImageButton ibVideoRecord;
	
	private ImageButton ibVideoUpload;
	
	private ImageButton ibVideoDownload;
	
	private ConfirmDialog confirmDialog;
	
	private String newFileName = "";
	
	private Toast toast;
	
	private BroadcastReceiver receiver;
	
	private Intent resultIntent;
	
	private boolean contentUpdated;
	
	@SuppressLint("HandlerLeak")
	private Handler callBackHandler = new Handler() {
		@Override
		public void handleMessage(android.os.Message msg) {
			switch (msg.what) {
			case UploadFileTask.UPLOAD_DONE:
				showProgress(false, "");
				showToast((String)msg.obj);
				downloadVideoIcon(1500);
				break;
			}
		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_video_detail);
		
		getWindow().setFlags(LayoutParams.FLAG_NOT_TOUCH_MODAL, LayoutParams.FLAG_NOT_TOUCH_MODAL);
		getWindow().setFlags(LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH, LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH);
		
		this.user = (User)getIntent().getExtras().getSerializable(LoginActivity.ARG_USER);
		this.course = (Course)getIntent().getExtras().getSerializable(CourseSectionFragment.ARG_COURSE);
		this.learningObjectIndex = getIntent().getExtras().getInt(LearningObjectSectionFragment.ARG_LEARNING_OBJECT_INDEX);
		this.videoIndex = getIntent().getExtras().getInt(LearningObjectSectionFragment.ARG_VIDEO_INDEX);
		
		this.video = this.course.getLearningObjectList().get(this.learningObjectIndex).getVideoList().get(this.videoIndex);

		this.tvDownloadPreviewStatusMessage = (TextView)findViewById(R.id.tvDownloadPreviewStatusMessage);
		this.llDownloadPreviewStatus = (LinearLayout)findViewById(R.id.llDownloadPreviewStatus);
		this.rlVideoDetailForm = (RelativeLayout)findViewById(R.id.rlVideoDetailForm);
		
		if (!TextUtils.isEmpty(this.video.getContentFileName())) {
			downloadVideoIcon(0);
		}
		
		TextView txtVideoItemName = (TextView)findViewById(R.id.txtVideoItemName);
		txtVideoItemName.setText(this.video.getName());

		TextView txtVideoItemDescription = (TextView)findViewById(R.id.txtVideoItemDescription);
		txtVideoItemDescription.setText(this.video.getDescription());
		
		LinearLayout llVideoDetail = (LinearLayout)findViewById(R.id.llVideoDetail);
		
		this.ibVideoPlay = new ImageButton(this);
		this.ibVideoPlay.setImageDrawable(getResources().getDrawable(R.drawable.ic_media_play));
		this.ibVideoPlay.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				if (!TextUtils.isEmpty(video.getContentFileName())) {
					Intent videoPlayIntent = new Intent(Intent.ACTION_VIEW);
					String mimeType = "application/*";
					String extension = MimeTypeMap.getFileExtensionFromUrl(video.getContentFileName());
					
					if (MimeTypeMap.getSingleton().hasExtension(extension)) {
						mimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension);
						
					}
					
					videoPlayIntent.setDataAndType(Uri.parse(getResources().getString(R.string.host_file_server) 
							+ getResources().getString(R.string.videos_folder) 
							+ "/" + video.getContentFileName()), mimeType);
					videoPlayIntent.putExtra(MediaStore.EXTRA_FINISH_ON_COMPLETION, true);
					try {
						startActivity(videoPlayIntent);
					} 
					catch (ActivityNotFoundException e) {
						Toast.makeText(VideoDetailActivity.this, 
								getResources().getString(R.string.no_application_available), 
								Toast.LENGTH_LONG).show();
					}		
				} else {
					showToast(getResources().getString(R.string.no_file_to_open));
				}
			}
		});
		
		llVideoDetail.addView(ibVideoPlay, new LinearLayout.LayoutParams(
				ViewGroup.LayoutParams.WRAP_CONTENT, 
				ViewGroup.LayoutParams.WRAP_CONTENT, 
				0));
		
		if (this.user.getUserType().equals(User.UserType.PROFESSOR)) {
			this.ibVideoRecord = new ImageButton(this);
			this.ibVideoRecord.setImageDrawable(getResources().getDrawable(R.drawable.ic_video_record));
			this.ibVideoRecord.setOnClickListener(new View.OnClickListener() {
				
				@Override
				public void onClick(View v) {
					Intent cameraIntent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
					
					File videoFolder = new File(Environment.getExternalStorageDirectory(), "DalOOC");
					videoFolder.mkdirs();
					File videoFile = new File(videoFolder, video.getId() + ".mp4");
					VideoDetailActivity.this.newFileName = videoFile.getAbsolutePath();
					VideoDetailActivity.this.video.setContentFileName(video.getId() + ".mp4");
					
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
			
			this.ibVideoUpload = new ImageButton(this);
			this.ibVideoUpload.setImageDrawable(getResources().getDrawable(R.drawable.ic_upload));
			this.ibVideoUpload.setOnClickListener(new View.OnClickListener() {
				
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
		
		this.ibVideoDownload = new ImageButton(this);
		this.ibVideoDownload.setImageDrawable(getResources().getDrawable(R.drawable.ic_download));
		this.ibVideoDownload.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				if (!TextUtils.isEmpty(video.getContentFileName())) {
					File destDocFile = new File(Environment.getExternalStorageDirectory() + "/Download/" + VideoDetailActivity.this.video.getContentFileName());
					VideoDetailActivity.this.dm = (DownloadManager) getSystemService(DOWNLOAD_SERVICE);
					Request request = new Request(Uri.parse(VideoDetailActivity.this.getResources().getString(R.string.host_file_server) 
							+ VideoDetailActivity.this.getResources().getString(R.string.videos_folder)
							+ "/" + VideoDetailActivity.this.video.getContentFileName()));
					request.setDestinationUri(Uri.fromFile(destDocFile));
					enqueue = dm.enqueue(request);	
					showToast(getResources().getString(R.string.download_file_in_progress));
				} else {
					showToast(getResources().getString(R.string.no_file_to_open));
				}
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
            				String extension = MimeTypeMap.getFileExtensionFromUrl(video.getContentFileName());
            				
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

	private void downloadVideoIcon(int waitTime) {
		ImageView videoThumbnail = (ImageView)findViewById(R.id.ivVideoDetailThumbnail);
		if (waitTime > 0) {
			videoThumbnail.setImageDrawable(getResources().getDrawable(R.drawable.ic_video_thumbnail_default));
		}
		new DownloadImageTask(videoThumbnail).execute(getResources().getString(R.string.host_file_server)
				+ getResources().getString(R.string.videos_folder)
				+ "/thumb/" + video.getContentFileName().replace("mp4", "jpg"), String.valueOf(waitTime));
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
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
	    if (requestCode == CAPTURE_VIDEO_ACTIVITY_REQUEST_CODE) {
	    	//TODO Implement the return threatment
	        if (resultCode == RESULT_OK) {
//	        	this.newFileName = data.getDataString().replace("file://", "");
	        	uploadSelectedFile();
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
	            args.putString(ConfirmDialog.ARG_MESSAGE, getResources().getString(R.string.video_overwrite_confirm));
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
		if (this.toast == null) {
			this.toast = Toast.makeText(this, "", Toast.LENGTH_LONG);
		}
		this.toast.setText(msg);
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
	public void onConfirmDialogReturn(boolean confirm, int returnCode) {
		if (confirm) {
			uploadSelectedFile();
		} else {
			this.newFileName = "";
		}
	}
	
	@Override
	public void onBackPressed() {
		if (this.contentUpdated) {
			this.course.getLearningObjectList().get(this.learningObjectIndex).getVideoList().set(this.videoIndex, this.video);
			
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

	private void uploadSelectedFile() {
		showProgress(true, getResources().getString(R.string.upload_file_in_progress));
		UploadFileTask uploadFileTask = new UploadFileTask();
		uploadFileTask.setOnUploadFileTaskDoneListener(this);
		uploadFileTask.execute(this.newFileName, 
				getResources().getString(R.string.videos_folder),
				this.video.getId(),
				getResources().getString(R.string.url_upload_file_servlet));
	}

	@Override
	public void onUploadFileTaskDone(int returnCode) {
		Message msg = new Message();
		msg.what = UploadFileTask.UPLOAD_DONE;
		if (returnCode == UploadFileTask.FILE_UPLOADED_SUCCESSFULY) {
			msg.obj = getResources().getString(R.string.successfull_upload);
			this.newFileName = General.getIdFileName(this.newFileName, this.video.getId());
			this.video.setContentFileName(this.newFileName.substring(this.newFileName.lastIndexOf("/") + 1));
			fireUpdateCourseThread();
			this.contentUpdated = true;
		} else {
			msg.obj = getResources().getString(R.string.problems_uploading_file);
		}
		this.newFileName = "";
		callBackHandler.sendMessage(msg);
	}
	
	private void fireUpdateCourseThread() {
		UpdateCourseCallRunnable updateCourseCall = new UpdateCourseCallRunnable(this.course, this);
		updateCourseCall.setOnWebServiceCallDoneListener(this);
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
	public void returnServiceResponse(int serviceCode, boolean resultOk) {
//		callBackHandler.sendEmptyMessage(0);		
		//TODO implement webservice response treatment
	}
}
