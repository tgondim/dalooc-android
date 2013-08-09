package ca.dal.cs.dalooc.android.gui;

import java.io.File;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.text.TextUtils;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import ca.dal.cs.dalooc.android.R;
import ca.dal.cs.dalooc.android.gui.components.ConfirmDialog;
import ca.dal.cs.dalooc.android.gui.listener.OnConfirmDialogReturnListener;
import ca.dal.cs.dalooc.android.gui.listener.OnUploadFileTaskDoneListener;
import ca.dal.cs.dalooc.android.util.DownloadImageTask;
import ca.dal.cs.dalooc.android.util.General;
import ca.dal.cs.dalooc.android.util.UploadFileTask;
import ca.dal.cs.dalooc.model.Course;
import ca.dal.cs.dalooc.model.User;
import ca.dal.cs.dalooc.model.Video;

public class VideoEditActivity extends FragmentActivity implements OnConfirmDialogReturnListener, OnUploadFileTaskDoneListener {
	
	public static final int ACTION_CONFIRM_VIDEO_CHANGES = 200;
	
	public static final int ACTION_CONFIRM_VIDEO_UPLOAD = 300;

	private Video video;
	
	private User user;
	
	private Course course;
	
	private int learningObjectIndex;

	private int videoIndex;
	
	private String newFileName = "";
	
	private ConfirmDialog confirmDialog;
	
	private EditText etName;
	
	private EditText etDescription;
	
	private ImageButton ibVideoRecord;
	
	private ImageButton ibVideoPlay;

	private ImageButton ibVideoUpload;
	
	private LinearLayout llForm;
	
	private LinearLayout llUploadPreviewStatus;
	
	private TextView tvUploadStatusMessage;
	
	private Toast toast;
	
	@SuppressLint("HandlerLeak")
	private Handler callBackHandler = new Handler() {
		@Override
		public void handleMessage(android.os.Message msg) {
			switch (msg.what) {
			case UploadFileTask.UPLOAD_DONE:
				showProgress(false, "");
				showToast((String)msg.obj);
				if (!TextUtils.isEmpty(VideoEditActivity.this.video.getContentFileName())) {
					downloadVideoIcon(1500);
				}
				break;
			}
		}
	};
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_video_edit);
		
		this.llForm = (LinearLayout)findViewById(R.id.llForm);
		this.llUploadPreviewStatus = (LinearLayout)findViewById(R.id.llUploadPreviewStatus);
		this.tvUploadStatusMessage = (TextView)findViewById(R.id.tvUploadStatusMessage);

		this.etName = (EditText)findViewById(R.id.etName);
		this.etDescription = (EditText)findViewById(R.id.etDescription);
		
		LinearLayout llVideoEditButtons = (LinearLayout)findViewById(R.id.llVideoEditButtons);
		
		this.ibVideoPlay = new ImageButton(this);
		this.ibVideoPlay.setImageDrawable(getResources().getDrawable(R.drawable.ic_media_play));
		this.ibVideoPlay.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				if (!TextUtils.isEmpty(VideoEditActivity.this.video.getContentFileName())) {
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
	                    Toast.makeText(VideoEditActivity.this, 
	                        getResources().getString(R.string.no_application_available), 
	                        Toast.LENGTH_LONG).show();
	                }		
				} else {
					showToast(getResources().getString(R.string.no_file_to_open));
				}
			}
		});
		
		llVideoEditButtons.addView(ibVideoPlay, new LinearLayout.LayoutParams(
				ViewGroup.LayoutParams.WRAP_CONTENT, 
				ViewGroup.LayoutParams.WRAP_CONTENT, 
				0));
		
		this.ibVideoRecord = new ImageButton(this);
		this.ibVideoRecord.setImageDrawable(getResources().getDrawable(R.drawable.ic_video_record));
		this.ibVideoRecord.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				Intent cameraIntent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
				
				File videoFolder = new File(Environment.getExternalStorageDirectory(), "DalOOC");
				videoFolder.mkdirs();
				File videoFile = new File(videoFolder, video.getId() + ".mp4");
				VideoEditActivity.this.newFileName = videoFile.getAbsolutePath();
				VideoEditActivity.this.video.setContentFileName(video.getId() + ".mp4");
				
				Uri uriSavedVideoFile = Uri.fromFile(videoFile);
				
				cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, uriSavedVideoFile);
				cameraIntent.putExtra(MediaStore.EXTRA_VIDEO_QUALITY, 1);

				startActivityForResult(cameraIntent, VideoDetailActivity.CAPTURE_VIDEO_ACTIVITY_REQUEST_CODE);				
			}
		});
		
		llVideoEditButtons.addView(ibVideoRecord, new LinearLayout.LayoutParams(
				ViewGroup.LayoutParams.WRAP_CONTENT, 
				ViewGroup.LayoutParams.WRAP_CONTENT, 
				0));
		
		ibVideoUpload = new ImageButton(this);
		ibVideoUpload.setImageDrawable(getResources().getDrawable(R.drawable.ic_upload));
		ibVideoUpload.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				Intent uploadFileIntent = new Intent(Intent.ACTION_GET_CONTENT);
				uploadFileIntent.setType("video/*");
				
				startActivityForResult(uploadFileIntent, VideoDetailActivity.GET_VIDEO_FILE_ACTIVITY_REQUEST_CODE);				
			}
		});
		
		llVideoEditButtons.addView(ibVideoUpload, new LinearLayout.LayoutParams(
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
			this.videoIndex = extras.getInt(LearningObjectSectionFragment.ARG_VIDEO_INDEX);

			if (this.learningObjectIndex >= 0) {
				if (this.videoIndex >= 0) {
					this.video = this.course.getLearningObjectList().get(this.learningObjectIndex).getVideoList().get(videoIndex);
				}
			}
			
			if (this.video != null) {
				loadData();
				getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
			} else {
				this.video = new Video();
			}
		}
	}

	private void loadData() {
		this.etName.setText(this.video.getName());
		this.etDescription.setText(this.video.getDescription());
		
		if (!TextUtils.isEmpty(this.video.getContentFileName())) {
			downloadVideoIcon(0);
		}
	}

	private void fetchData() {
		this.video.setName(this.etName.getText().toString());
		this.video.setDescription(this.etDescription.getText().toString());
	}
	
	private void finishSaving() {
		fetchData();
		Intent resultIntent = new Intent();
		resultIntent.putExtra(LearningObjectSectionFragment.ARG_VIDEO, VideoEditActivity.this.video);
		resultIntent.putExtra(LearningObjectSectionFragment.ARG_VIDEO_INDEX, VideoEditActivity.this.videoIndex);
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
		getMenuInflater().inflate(R.menu.video_edit, menu);
		
		return true;
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
	    if (requestCode == VideoDetailActivity.CAPTURE_VIDEO_ACTIVITY_REQUEST_CODE) {
	        if (resultCode == RESULT_OK) {
//	        	this.newFileName = data.getDataString().replace("file://", "");
	        	uploadSelectedFile();
	        } else if (resultCode == RESULT_CANCELED) {
	            // User cancelled the video capture
	        } else {
	            //TODO Video capture failed, warn user
	        }
	    } else if (requestCode == VideoDetailActivity.GET_VIDEO_FILE_ACTIVITY_REQUEST_CODE) {
	        if (resultCode == RESULT_OK) {
	        	this.newFileName = data.getDataString().replace("file://", "");
	        	showConfirmDialog(getResources().getString(R.string.video_overwrite_confirm), ACTION_CONFIRM_VIDEO_UPLOAD);
	        } else if (resultCode == RESULT_CANCELED) {
	            // User cancelled the video capture
	        } 
	    }
	}
	
	@Override
	public void onBackPressed() {
//		super.onBackPressed();
//		fetchData();
		showConfirmDialog(getResources().getString(R.string.confirm_changes), ACTION_CONFIRM_VIDEO_CHANGES);
	}
	
	@Override
	public void onConfirmDialogReturn(boolean confirm, int returnCode) {
		Intent resultIntent = new Intent();
		
		switch (returnCode) {
		case ACTION_CONFIRM_VIDEO_CHANGES:
			this.confirmDialog.dismiss();
			if (confirm) {
				finishSaving();
			} else {
				finishWithoutSaving();
			}
//			finish();

			break;
			
		case ACTION_CONFIRM_VIDEO_UPLOAD:
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
			this.newFileName = General.getIdFileName(this.newFileName, this.video.getId());
			this.video.setContentFileName(this.newFileName.substring(this.newFileName.lastIndexOf("/") + 1));
			//TODO save the document modification
		} else {
			msg.obj = getResources().getString(R.string.problems_uploading_file);
		}
		this.newFileName = "";
		callBackHandler.sendMessage(msg);
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
	
	private void uploadSelectedFile() {
		showProgress(true, getResources().getString(R.string.upload_file_in_progress));
		UploadFileTask uploadFileTask = new UploadFileTask();
		uploadFileTask.setOnUploadFileTaskDoneListener(this);
		uploadFileTask.execute(this.newFileName, 
				getResources().getString(R.string.videos_folder),
				this.video.getId(),
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
        args.putString(ConfirmDialog.ARG_TITLE, getResources().getString(R.string.dialog_title_video));
        args.putString(ConfirmDialog.ARG_MESSAGE, message);
        args.putInt(ConfirmDialog.ARG_RETURN_CODE, returnCode);
        
        confirmDialog = new ConfirmDialog();
        confirmDialog.setArguments(args);
        confirmDialog.setOnConfirmDialogResultListener(this);
        confirmDialog.show(fm, "fragment_edit_name");
    }
}
