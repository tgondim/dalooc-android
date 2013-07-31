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
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.NotificationCompat;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.MimeTypeMap;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import ca.dal.cs.android.dalooc.R;
import ca.dal.cs.dalooc.android.gui.listener.OnConfirmDialogReturnListener;
import ca.dal.cs.dalooc.android.gui.listener.OnUploadFileTaskDoneListener;
import ca.dal.cs.dalooc.android.util.DownloadDocumentTask;
import ca.dal.cs.dalooc.android.util.General;
import ca.dal.cs.dalooc.android.util.UploadFileTask;
import ca.dal.cs.dalooc.android.util.listener.OnDownloadDocumentDoneListener;
import ca.dal.cs.dalooc.model.Document;
import ca.dal.cs.dalooc.model.User;


public class DocumentDetailActivity extends FragmentActivity implements OnDownloadDocumentDoneListener, OnConfirmDialogReturnListener, OnUploadFileTaskDoneListener {
	 
	private static final int GET_DOCUMENT_FILE_ACTIVITY_REQUEST_CODE = 300;
	
	private Document document;
	
	private User user;
	
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

	@SuppressLint("ShowToast")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_document_detail);
		
		this.toast = Toast.makeText(this, "", Toast.LENGTH_LONG);
	
		this.document = (Document)getIntent().getExtras().getSerializable(LearningObjectSectionFragment.ARG_DOCUMENT);
		this.user = (User)getIntent().getExtras().getSerializable(LoginActivity.ARG_USER);

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
				showProgress(true, getResources().getString(R.string.download_preview_in_progress));
				DownloadDocumentTask downloadDocTask = new DownloadDocumentTask();
				downloadDocTask.setOnDownloadDocumentDoneListener(DocumentDetailActivity.this);
				downloadDocTask.execute(DocumentDetailActivity.this.getResources().getString(R.string.host_file_server) 
						+ DocumentDetailActivity.this.getResources().getString(R.string.documents_folder)
						+ "/" + DocumentDetailActivity.this.document.getDocumentUrl());;
			}
		});
		
		llDocumentDetail.addView(ibDocumentPreview, new LinearLayout.LayoutParams(
				ViewGroup.LayoutParams.WRAP_CONTENT, 
				ViewGroup.LayoutParams.WRAP_CONTENT, 
				0));
		
		if (this.user.getUserType().equals(User.UserType.PROFESSOR)) {
			ibDocumentUpload = new ImageButton(this);
			ibDocumentUpload.setImageDrawable(getResources().getDrawable(R.drawable.ic_upload));
			ibDocumentUpload.setOnClickListener(new View.OnClickListener() {
				
				@Override
				public void onClick(View v) {
					Intent uploadFileIntent = new Intent(Intent.ACTION_GET_CONTENT);
					switch (document.getType()) {
					case PDF: 
						uploadFileIntent.setType("file/pdf");
						break;
					case DOC: 
						uploadFileIntent.setType("file/doc");
						break;
					case PPT: 
						uploadFileIntent.setType("file/pdf");
						break;
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
				File destDocFile = new File(Environment.getExternalStorageDirectory() + "/Download/" + DocumentDetailActivity.this.document.getDocumentUrl());
				DocumentDetailActivity.this.dm = (DownloadManager) getSystemService(DOWNLOAD_SERVICE);
				Request request = new Request(Uri.parse(DocumentDetailActivity.this.getResources().getString(R.string.host_file_server)
						+ DocumentDetailActivity.this.getResources().getString(R.string.documents_folder)
						+ "/" + DocumentDetailActivity.this.document.getDocumentUrl()));
				request.setDestinationUri(Uri.fromFile(destDocFile));
				enqueue = dm.enqueue(request);			
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
            				String extension = MimeTypeMap.getFileExtensionFromUrl(document.getDocumentUrl());
            				
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

	@Override
	public void onDownloadDocumentDone(File file) {
		showProgress(false, "");
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
		this.toast.setText(msg);
		this.toast.cancel();
		this.toast.show();
	}

	private void openDocument(File file) {
		String mimeType = "application/*";
		String extension = MimeTypeMap.getFileExtensionFromUrl(document.getDocumentUrl());
		
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
	public void onConfirmDialogReturn(boolean confirm) {
		if (confirm) {
			showProgress(true, getResources().getString(R.string.upload_file_in_progress));
    		UploadFileTask uploadFileTask = new UploadFileTask();
    		uploadFileTask.setOnUploadFileTaskDoneListener(this);
    		uploadFileTask.execute(this.newFileName, getResources().getString(R.string.documents_folder), this.document.getId());
     	} else {
     		this.newFileName = "";
     	}
	}

	@Override
	public void onUploadFileTaskDone(int returnCode) {
		callBackHandler.sendEmptyMessage(UploadFileTask.UPLOAD_DONE);
		if (returnCode == UploadFileTask.FILE_UPLOADED_SUCCESSFULY) {
			this.newFileName = General.getIdFileName(this.newFileName, this.document.getId());
			this.document.setDocumentUrl(this.newFileName.substring(this.newFileName.lastIndexOf("/")));
			showToast(getResources().getString(R.string.successfull_upload));
			//TODO save the document modification
		} else {
			showToast(getResources().getString(R.string.problems_uploading_file));
		}
		this.newFileName = "";
	}
}
