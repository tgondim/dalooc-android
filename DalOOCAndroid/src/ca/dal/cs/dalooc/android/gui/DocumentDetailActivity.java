package ca.dal.cs.dalooc.android.gui;

import java.io.File;

import android.annotation.SuppressLint;
import android.app.Activity;
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
import android.support.v4.app.NotificationCompat;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.MimeTypeMap;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import ca.dal.cs.android.dalooc.R;
import ca.dal.cs.dalooc.android.util.DownloadDocumentTask;
import ca.dal.cs.dalooc.android.util.listener.OnDownloadDocumentDoneListener;
import ca.dal.cs.dalooc.model.Document;

public class DocumentDetailActivity extends Activity implements OnDownloadDocumentDoneListener {
	 
	private static final int GET_DOCUMENT_FILE_ACTIVITY_REQUEST_CODE = 300;
	
	private Document document;
	
	private ImageButton ibDocumentPreview;

	private ImageButton ibDocumentUpload;
	
	private ImageButton ibDocumentDownload;
	
	private long enqueue;
	
	private DownloadManager dm;
	
	private BroadcastReceiver receiver;

	@SuppressLint("ShowToast")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_document_detail);
	
		this.document = (Document)getIntent().getExtras().getSerializable(LearningObjectSectionFragment.ARG_DOCUMENT);
		
//		this.toast = Toast.makeText(this, "", Toast.LENGTH_LONG);
		
		TextView txtDocumentItemName = (TextView)findViewById(R.id.txtDocumentItemName);
		txtDocumentItemName.setText(this.document.getName());

		TextView txtDocumentItemDescription = (TextView)findViewById(R.id.txtDocumentItemDescription);
		txtDocumentItemDescription.setText(this.document.getDescription());
		
		ibDocumentPreview = new ImageButton(this);
		ibDocumentPreview.setImageDrawable(getResources().getDrawable(R.drawable.ic_view));
		ibDocumentPreview.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				DownloadDocumentTask downloadDocTask = new DownloadDocumentTask();
				downloadDocTask.setOnDownloadDocumentDoneListener(DocumentDetailActivity.this);
				downloadDocTask.execute(DocumentDetailActivity.this.getResources().getString(R.string.host_file_server) + DocumentDetailActivity.this.document.getDocumentUrl());;
			}
		});
		
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
		
		ibDocumentDownload = new ImageButton(this);
		ibDocumentDownload.setImageDrawable(getResources().getDrawable(R.drawable.ic_download));
		ibDocumentDownload.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				File destDocFile = new File(Environment.getExternalStorageDirectory() + "/Download" + DocumentDetailActivity.this.document.getDocumentUrl().replace("/DalOOC",""));
				DocumentDetailActivity.this.dm = (DownloadManager) getSystemService(DOWNLOAD_SERVICE);
				Request request = new Request(Uri.parse(DocumentDetailActivity.this.getResources().getString(R.string.host_file_server) 
						+ DocumentDetailActivity.this.document.getDocumentUrl()));
				request.setDestinationUri(Uri.fromFile(destDocFile));
				enqueue = dm.enqueue(request);			
			}
		});

		LinearLayout llDocumentDetail = (LinearLayout)findViewById(R.id.llDocumentDetail);
		
		llDocumentDetail.addView(ibDocumentPreview, new LinearLayout.LayoutParams(
				ViewGroup.LayoutParams.WRAP_CONTENT, 
				ViewGroup.LayoutParams.WRAP_CONTENT, 
				0));

		llDocumentDetail.addView(ibDocumentUpload, new LinearLayout.LayoutParams(
				ViewGroup.LayoutParams.WRAP_CONTENT, 
				ViewGroup.LayoutParams.WRAP_CONTENT, 
				0));

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
    			ConfirmDialogFragment confirm = new ConfirmDialogFragment();
	        	Bundle bundle = new Bundle();
	        	bundle.putInt(ConfirmDialogFragment.ARG_CONFIRM_MESSAGE, ConfirmDialogFragment.DOCUMENT_CONFIRM_MESSAGE);
	        	confirm.setArguments(bundle);
	        	confirm.show(getFragmentManager(), "Confirm");
	        	if (confirm.getLastChoice()) {
	        		this.document.setDocumentUrl(data.getDataString().replace("file://", ""));
	         	} else {
	         		//DO NOTHING
	         	}
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
		if (file != null) {
			openDocument(file);		
		} else {
			//TODO see what to do case the file is not downloaded
		}
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
}
