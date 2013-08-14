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
import android.support.v4.app.DialogFragment;
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
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import ca.dal.cs.dalooc.android.R;
import ca.dal.cs.dalooc.android.gui.component.ConfirmDialog;
import ca.dal.cs.dalooc.android.gui.listener.OnConfirmDialogReturnListener;
import ca.dal.cs.dalooc.android.gui.listener.OnDownloadDocumentDoneListener;
import ca.dal.cs.dalooc.android.gui.listener.OnUploadFileTaskDoneListener;
import ca.dal.cs.dalooc.android.gui.listener.OnWebServiceCallDoneListener;
import ca.dal.cs.dalooc.android.task.DownloadDocumentTask;
import ca.dal.cs.dalooc.android.task.UpdateCourseCallTask;
import ca.dal.cs.dalooc.android.task.UploadFileTask;
import ca.dal.cs.dalooc.android.util.General;
import ca.dal.cs.dalooc.model.Course;
import ca.dal.cs.dalooc.model.Document;
import ca.dal.cs.dalooc.model.User;


public class DocumentDetailActivity extends FragmentActivity implements OnDownloadDocumentDoneListener, OnConfirmDialogReturnListener, OnUploadFileTaskDoneListener, OnWebServiceCallDoneListener {
	 
	public static final int GET_DOCUMENT_FILE_ACTIVITY_REQUEST_CODE = 300;
	
	private DownloadDocumentTask downloadDocumentTask;
	
	private UploadFileTask uploadFileTask;
	
	private UpdateCourseCallTask updateCourseCallTask;
	
	private Document document;
	
	private User user;
	
	private Course course;
	
	private int learningObjectIndex;

	private int documentIndex;
	
	private LinearLayout llDownloadPreviewStatus;
	
	private RelativeLayout rlDocumentDetailForm;
	
	private TextView tvDownloadPreviewStatusMessage;

	private ImageButton ibDocumentPreview;

	private ImageButton ibDocumentUpload;
	
	private ImageButton ibDocumentDownload;
	
	private long enqueue;
	
	private DownloadManager dm;
	
	private BroadcastReceiver receiver;
	
	private String newFileName = "";
	
	private Toast toast;
	
	private ConfirmDialog confirmDialog;
	
	private Intent resultIntent;
	
	private boolean contentUpdated;
	
	private boolean asStudent;
	
	@SuppressLint("HandlerLeak")
	private Handler callBackHandler = new Handler() {
		@Override
		public void handleMessage(android.os.Message msg) {
			switch (msg.what) {
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
		setContentView(R.layout.activity_document_detail);
		
		getWindow().setFlags(LayoutParams.FLAG_NOT_TOUCH_MODAL, LayoutParams.FLAG_NOT_TOUCH_MODAL);
		getWindow().setFlags(LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH, LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH);
		
		this.user = (User)getIntent().getExtras().getSerializable(LoginActivity.ARG_USER);
		this.course = (Course)getIntent().getExtras().getSerializable(CourseSectionFragment.ARG_COURSE);
		this.learningObjectIndex = getIntent().getExtras().getInt(LearningObjectSectionFragment.ARG_LEARNING_OBJECT_INDEX);
		this.documentIndex = getIntent().getExtras().getInt(LearningObjectSectionFragment.ARG_DOCUMENT_INDEX);
		this.asStudent = getIntent().getExtras().getBoolean(LearningObjectSectionFragment.ARG_AS_STUDENT, false);
		
		this.document = this.course.getLearningObjectList().get(this.learningObjectIndex).getDocumentList().get(this.documentIndex);

		TextView txtDocumentItemName = (TextView)findViewById(R.id.txtDocumentItemName);
		txtDocumentItemName.setText(this.document.getName());

		TextView txtDocumentItemDescription = (TextView)findViewById(R.id.txtDocumentItemDescription);
		txtDocumentItemDescription.setText(this.document.getDescription());
		
		this.tvDownloadPreviewStatusMessage = (TextView)findViewById(R.id.tvDownloadPreviewStatusMessage);
		this.llDownloadPreviewStatus = (LinearLayout)findViewById(R.id.llDownloadPreviewStatus);
		this.rlDocumentDetailForm = (RelativeLayout)findViewById(R.id.rlDocumentDetailForm);
		
		LinearLayout llDocumentDetail = (LinearLayout)findViewById(R.id.llDocumentDetail);
		
		ibDocumentPreview = new ImageButton(this);
		ibDocumentPreview.setImageDrawable(getResources().getDrawable(R.drawable.ic_view));
		ibDocumentPreview.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				if (!TextUtils.isEmpty(DocumentDetailActivity.this.document.getContentFileName())) {
					showProgress(true, getResources().getString(R.string.download_preview_in_progress));
					DocumentDetailActivity.this.downloadDocumentTask = new DownloadDocumentTask();
					DocumentDetailActivity.this.downloadDocumentTask.setOnDownloadDocumentDoneListener(DocumentDetailActivity.this);
					DocumentDetailActivity.this.downloadDocumentTask.execute(DocumentDetailActivity.this.getResources().getString(R.string.host_file_server) 
							+ DocumentDetailActivity.this.getResources().getString(R.string.documents_folder)
							+ "/" + DocumentDetailActivity.this.document.getContentFileName());
				} else {
					showToast(getResources().getString(R.string.no_file_to_open));
				}
			}
		});
		
		llDocumentDetail.addView(ibDocumentPreview, new LinearLayout.LayoutParams(
				ViewGroup.LayoutParams.WRAP_CONTENT, 
				ViewGroup.LayoutParams.WRAP_CONTENT, 
				0));
		
		if (this.user.getId().equals(this.course.getOwnerId()) 
				&& this.user.getUserType().equals(User.UserType.PROFESSOR) 
				&& !this.asStudent) {
			ibDocumentUpload = new ImageButton(this);
			ibDocumentUpload.setImageDrawable(getResources().getDrawable(R.drawable.ic_upload));
			ibDocumentUpload.setOnClickListener(new View.OnClickListener() {
				
				@Override
				public void onClick(View v) {
					Intent uploadFileIntent = new Intent(Intent.ACTION_GET_CONTENT);
					switch (document.getType()) {
//					case PDF:
//						uploadFileIntent.setType("file/pdf");
//						break;
//					case DOC:
//						uploadFileIntent.setType("file/doc");
//						break;
//					case PPT:
//						uploadFileIntent.setType("file/pdf");
//						break;
					default:
						uploadFileIntent.setType("file/*");
					}
					
					startActivityForResult(uploadFileIntent, GET_DOCUMENT_FILE_ACTIVITY_REQUEST_CODE);				
				}
			});
			
			llDocumentDetail.addView(ibDocumentUpload, new LinearLayout.LayoutParams(
					ViewGroup.LayoutParams.WRAP_CONTENT, 
					ViewGroup.LayoutParams.WRAP_CONTENT, 
					0));
		}
		ibDocumentDownload = new ImageButton(this);
		ibDocumentDownload.setImageDrawable(getResources().getDrawable(R.drawable.ic_download));
		ibDocumentDownload.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				if (!TextUtils.isEmpty(DocumentDetailActivity.this.document.getContentFileName())) {
					String downloadFileName = DocumentDetailActivity.this.document.getName().replaceAll("[^\\w\\.@-]", "") + 
							DocumentDetailActivity.this.document.getContentFileName().substring(DocumentDetailActivity.this.document.getContentFileName().indexOf("."));
					
					File destDocFile = new File(Environment.getExternalStorageDirectory() + "/Download/" + downloadFileName);
					DocumentDetailActivity.this.dm = (DownloadManager) getSystemService(DOWNLOAD_SERVICE);
					Request request = new Request(Uri.parse(DocumentDetailActivity.this.getResources().getString(R.string.host_file_server)
							+ DocumentDetailActivity.this.getResources().getString(R.string.documents_folder)
							+ "/" + DocumentDetailActivity.this.document.getContentFileName()));
					request.setDestinationUri(Uri.fromFile(destDocFile));
					enqueue = dm.enqueue(request);		
					showToast(getResources().getString(R.string.download_file_in_progress));
				} else {
					showToast(getResources().getString(R.string.no_file_to_download));
				}
			}
		});

		llDocumentDetail.addView(ibDocumentDownload, new LinearLayout.LayoutParams(
				ViewGroup.LayoutParams.WRAP_CONTENT, 
				ViewGroup.LayoutParams.WRAP_CONTENT, 
				0));
		
		this.receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                if (DownloadManager.ACTION_DOWNLOAD_COMPLETE.equals(action)) {
                    Query query = new Query();
                    query.setFilterById(DocumentDetailActivity.this.enqueue);
                    Cursor c = DocumentDetailActivity.this.dm.query(query);
                    if (c.moveToFirst()) {
                        int columnIndex = c.getColumnIndex(DownloadManager.COLUMN_STATUS);
                        if (DownloadManager.STATUS_SUCCESSFUL == c.getInt(columnIndex)) {
                        	Intent documentPreviewIntent = new Intent(Intent.ACTION_VIEW);
                        	String mimeType = "application/*";
            				String extension = MimeTypeMap.getFileExtensionFromUrl(document.getContentFileName());
            				
            				if (MimeTypeMap.getSingleton().hasExtension(extension)) {
            					mimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension);
            				}
                        	
            				documentPreviewIntent.setDataAndType(Uri.parse(c.getString(c.getColumnIndex(DownloadManager.COLUMN_LOCAL_URI))), mimeType);
							PendingIntent pit = PendingIntent.getActivity(context, 0, documentPreviewIntent, 0);

							Notification notificacao = new NotificationCompat.Builder(
									context)
									.setTicker(DocumentDetailActivity.this.getResources().getString(R.string.download_complete))
									.setContentTitle(c.getString(c.getColumnIndex(DownloadManager.COLUMN_TITLE)))
									.setContentIntent(pit)
									.setContentText(DocumentDetailActivity.this.getResources().getString(R.string.download_complete))
									.setSmallIcon(R.drawable.ic_download_complete)
									.setAutoCancel(true).build();

							NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
							manager.notify(1, notificacao);
                        	
                        }
                    }
                }
//                unregisterReceiver(DocumentDetailActivity.this.receiver);
            }
        };

        registerReceiver(receiver, new IntentFilter( DownloadManager.ACTION_DOWNLOAD_COMPLETE));
	}
	
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		  if (event.getAction() == MotionEvent.ACTION_OUTSIDE) {
			  if ((!this.asStudent) && (llDownloadPreviewStatus.getVisibility() != View.VISIBLE)) {
            	  onBackPressed();
              }
              return true;
          }
          return false;
	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		unregisterReceiver(this.receiver);
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
	    if (requestCode == GET_DOCUMENT_FILE_ACTIVITY_REQUEST_CODE) {
	        if (resultCode == RESULT_OK) {
	        	this.newFileName = data.getDataString().replace("file://", "");
	            
	            showConfirmDialog(getResources().getString(R.string.document_overwrite_confirm), 0);
	        } else if (resultCode == RESULT_CANCELED) {
	            // User cancelled the document upload
	        } 
	    }
	}

	private void showConfirmDialog(String message, int returnCode) {
        FragmentManager fm = getSupportFragmentManager();
        
        Bundle args = new Bundle();
        args.putString(ConfirmDialog.ARG_TITLE, getResources().getString(R.string.dialog_title_audio));
        args.putString(ConfirmDialog.ARG_MESSAGE, message);
        args.putInt(ConfirmDialog.ARG_RETURN_CODE, returnCode);
        
        this.confirmDialog = new ConfirmDialog();
        this.confirmDialog.setArguments(args);
        this.confirmDialog.setStyle(DialogFragment.STYLE_NORMAL, android.R.style.Theme_Holo_Light_Dialog);
        this.confirmDialog.setOnConfirmDialogResultListener(this);
        this.confirmDialog.show(fm, "fragment_edit_name");
    }
	
	@Override
	public void onDownloadDocumentDone(File file) {
		showProgress(false, "");
		DocumentDetailActivity.this.downloadDocumentTask = null;
		if (file != null) {
			openDocument(file);		
		} else {
			showToast(getResources().getString(R.string.problems_downloading_file));
		}
	}
	
	private void showProgress(boolean show, String msg) {
		this.llDownloadPreviewStatus.setVisibility(show ? View.VISIBLE : View.GONE);
		this.rlDocumentDetailForm.setVisibility(show ? View.GONE : View.VISIBLE);
		this.tvDownloadPreviewStatusMessage.setText(msg);
	}
	
	private void showToast(String msg) {
		if (this.toast == null) {
			this.toast = Toast.makeText(this, "", Toast.LENGTH_LONG);
		}
		this.toast.setText(msg);
		this.toast.show();
	}

	private void openDocument(File file) {
		String mimeType = "application/*";
		String extension = MimeTypeMap.getFileExtensionFromUrl(document.getContentFileName());
		
		if (MimeTypeMap.getSingleton().hasExtension(extension)) {
			mimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension);
		}
		try {
			Intent documentPreviewIntent = new Intent(Intent.ACTION_VIEW);
			documentPreviewIntent.setDataAndType(Uri.fromFile(file), mimeType);
	        startActivity(documentPreviewIntent);
		} 
		catch (ActivityNotFoundException e) {
		    Toast.makeText(DocumentDetailActivity.this, 
		        getResources().getString(R.string.no_application_available), 
		        Toast.LENGTH_LONG).show();
		}
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
			this.course.getLearningObjectList().get(this.learningObjectIndex).getDocumentList().set(this.documentIndex, this.document);
			
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
		this.uploadFileTask = new UploadFileTask(this.tvDownloadPreviewStatusMessage);
		this.uploadFileTask.setOnUploadFileTaskDoneListener(this);
		this.uploadFileTask.execute(this.newFileName, 
				getResources().getString(R.string.documents_folder), 
				this.document.getId(),
				getResources().getString(R.string.url_upload_file_servlet));
	}

	@Override
	public void onUploadFileTaskDone(int returnCode) {
		Message msg = new Message();
		msg.what = UploadFileTask.UPLOAD_DONE;
		if (returnCode == UploadFileTask.FILE_UPLOADED_SUCCESSFULY) {
			msg.obj = getResources().getString(R.string.successfull_upload);
			this.newFileName = General.getIdFileName(this.newFileName, this.document.getId());
			this.document.setContentFileName(this.newFileName.substring(this.newFileName.lastIndexOf("/") + 1));
			this.contentUpdated = true;
			fireUpdateCourseTask();
		} else {
			msg.obj = getResources().getString(R.string.problems_uploading_file);
		}
		this.newFileName = "";
		this.uploadFileTask = null;
		callBackHandler.sendMessage(msg);
	}
	
	private void fireUpdateCourseTask() {
		this.updateCourseCallTask = new UpdateCourseCallTask(this.course);
		this.updateCourseCallTask.setOnWebServiceCallDoneListener(this);
		this.updateCourseCallTask.execute(getUrlWebService(UpdateCourseCallTask.UPDATE_COURSE_WEB_SERVICE),
				getResources().getString(R.string.namespace_webservice),
				getResources().getString(R.string.update_course_webservice_operation));
	}

	private String getUrlWebService(int serviceCode) {
		if (serviceCode == UpdateCourseCallTask.UPDATE_COURSE_WEB_SERVICE) {
			return getResources().getString(R.string.url_webservice) + "/" + getResources().getString(R.string.course_repository) + "/" + getResources().getString(R.string.update_course_webservice_operation);
		}
		return null;
	}
	
	@Override
	public void returnServiceResponse(int serviceCode, boolean resultOk) {
		switch (serviceCode) {
		case UpdateCourseCallTask.UPDATE_COURSE_WEB_SERVICE:
			if (!resultOk) {
				showToast(getResources().getString(R.string.error_unable_to_update_course));
			}
			
			this.updateCourseCallTask = null;
		
			break;
			
		default:
				
				break;
	    }
	}
}
