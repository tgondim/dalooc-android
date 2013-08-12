package ca.dal.cs.dalooc.android.gui;

import java.io.File;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
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
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;
import ca.dal.cs.dalooc.android.R;
import ca.dal.cs.dalooc.android.gui.components.ConfirmDialog;
import ca.dal.cs.dalooc.android.gui.listener.OnConfirmDialogReturnListener;
import ca.dal.cs.dalooc.android.gui.listener.OnUploadFileTaskDoneListener;
import ca.dal.cs.dalooc.android.util.DownloadDocumentTask;
import ca.dal.cs.dalooc.android.util.General;
import ca.dal.cs.dalooc.android.util.listener.OnDownloadDocumentDoneListener;
import ca.dal.cs.dalooc.android.webservice.UploadFileTask;
import ca.dal.cs.dalooc.model.Course;
import ca.dal.cs.dalooc.model.Document;
import ca.dal.cs.dalooc.model.LearningObject;
import ca.dal.cs.dalooc.model.User;

public class DocumentEditActivity extends FragmentActivity implements OnConfirmDialogReturnListener, OnUploadFileTaskDoneListener, OnDownloadDocumentDoneListener {
	
	public static final int ACTION_CONFIRM_DOCUMENT_CHANGES = 200;
	
	public static final int ACTION_CONFIRM_DOCUMENT_UPLOAD = 300;

	private Document document;
	
	private User user;
	
	private Course course;
	
	private int learningObjectIndex;

	private int documentIndex;
	
	private String newFileName = "";
	
	private ConfirmDialog confirmDialog;
	
	private EditText etName;
	
	private EditText etDescription;
	
	private ImageButton ibDocumentPreview;

	private ImageButton ibDocumentUpload;
	
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
				break;
			}
		}
	};
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_document_edit);
		
		this.llForm = (LinearLayout)findViewById(R.id.llForm);
		this.llUploadPreviewStatus = (LinearLayout)findViewById(R.id.llUploadPreviewStatus);
		this.tvUploadStatusMessage = (TextView)findViewById(R.id.tvUploadStatusMessage);

		this.etName = (EditText)findViewById(R.id.etName);
		this.etDescription = (EditText)findViewById(R.id.etDescription);
		
		LinearLayout llDocumentEditButtons = (LinearLayout)findViewById(R.id.llDocumentEditButtons);
		
		this.ibDocumentPreview = new ImageButton(this);
		this.ibDocumentPreview.setImageDrawable(getResources().getDrawable(R.drawable.ic_view));
		this.ibDocumentPreview.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				if (!TextUtils.isEmpty(DocumentEditActivity.this.document.getContentFileName())) {
					showProgress(true, getResources().getString(R.string.download_preview_in_progress));
					DownloadDocumentTask downloadDocTask = new DownloadDocumentTask();
					downloadDocTask.setOnDownloadDocumentDoneListener(DocumentEditActivity.this);
					downloadDocTask.execute(DocumentEditActivity.this.getResources().getString(R.string.host_file_server) 
							+ DocumentEditActivity.this.getResources().getString(R.string.documents_folder)
							+ "/" + DocumentEditActivity.this.document.getContentFileName());
				} else {
					showToast(getResources().getString(R.string.no_file_to_open));
				}
			}
		});
		
		llDocumentEditButtons.addView(ibDocumentPreview, new LinearLayout.LayoutParams(
				ViewGroup.LayoutParams.WRAP_CONTENT, 
				ViewGroup.LayoutParams.WRAP_CONTENT, 
				0));
		
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
				
				startActivityForResult(uploadFileIntent, DocumentDetailActivity.GET_DOCUMENT_FILE_ACTIVITY_REQUEST_CODE);				
			}
		});
		
		llDocumentEditButtons.addView(ibDocumentUpload, new LinearLayout.LayoutParams(
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
			this.documentIndex = extras.getInt(LearningObjectSectionFragment.ARG_DOCUMENT_INDEX);

			if (this.learningObjectIndex >= 0) {
				if (this.documentIndex >= 0) {
					this.document = this.course.getLearningObjectList().get(this.learningObjectIndex).getDocumentList().get(documentIndex);
				}
			}
			
			if (this.document != null) {
				loadData();
				getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
			} else {
				this.document = new Document();
			}
		}
	}

	private void loadData() {
		this.etName.setText(this.document.getName());
		this.etDescription.setText(this.document.getDescription());
	}

	private void fetchData() {
		this.document.setName(this.etName.getText().toString());
		this.document.setDescription(this.etDescription.getText().toString());

		if (!TextUtils.isEmpty(this.document.getContentFileName())) {
			this.document.setType(Document.getDocumentType(this.document.getContentFileName().substring(this.document.getContentFileName().lastIndexOf(".") + 1)));			
		} else {
			this.document.setType(Document.DocumentType.UNKNOWN);
		}
	}

	private void finishSaving() {
		fetchData();
		Intent resultIntent = new Intent();
		resultIntent.putExtra(LearningObjectSectionFragment.ARG_DOCUMENT, DocumentEditActivity.this.document);
		resultIntent.putExtra(LearningObjectSectionFragment.ARG_DOCUMENT_INDEX, DocumentEditActivity.this.documentIndex);
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
		getMenuInflater().inflate(R.menu.document_edit, menu);
		
		return true;
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (requestCode == DocumentDetailActivity.GET_DOCUMENT_FILE_ACTIVITY_REQUEST_CODE) {
	        if (resultCode == RESULT_OK) {
	        	this.newFileName = data.getDataString().replace("file://", "");
	            
	            showConfirmDialog(getResources().getString(R.string.document_overwrite_confirm), ACTION_CONFIRM_DOCUMENT_UPLOAD);
	        } else if (resultCode == RESULT_CANCELED) {
	            // User cancelled the document file selection
	        } 
	    }
	}
	
	@Override
	public void onBackPressed() {
//		super.onBackPressed();
//		fetchData();
		showConfirmDialog(getResources().getString(R.string.confirm_changes), ACTION_CONFIRM_DOCUMENT_CHANGES);
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
	
	@Override
	public void onConfirmDialogReturn(boolean confirm, int returnCode) {
//		Intent resultIntent = new Intent();
		
		switch (returnCode) {
		case ACTION_CONFIRM_DOCUMENT_CHANGES:
			this.confirmDialog.dismiss();
			if (confirm) {
				finishSaving();
			} else {
				finishWithoutSaving();
			}
//			finish();

			break;
			
		case ACTION_CONFIRM_DOCUMENT_UPLOAD:
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
			this.newFileName = General.getIdFileName(this.newFileName, this.document.getId());
			this.document.setContentFileName(this.newFileName.substring(this.newFileName.lastIndexOf("/") + 1));
			//TODO save the document modification
		} else {
			msg.obj = getResources().getString(R.string.problems_uploading_file);
		}
		this.newFileName = "";
		callBackHandler.sendMessage(msg);
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
		    Toast.makeText(DocumentEditActivity.this, 
		        getResources().getString(R.string.no_application_available), 
		        Toast.LENGTH_LONG).show();
		}
	}
	
	private void uploadSelectedFile() {
		showProgress(true, getResources().getString(R.string.upload_file_in_progress));
		UploadFileTask uploadFileTask = new UploadFileTask();
		uploadFileTask.setOnUploadFileTaskDoneListener(this);
		uploadFileTask.execute(this.newFileName, 
				getResources().getString(R.string.documents_folder),
				this.document.getId(),
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
        args.putString(ConfirmDialog.ARG_TITLE, getResources().getString(R.string.dialog_title_document));
        args.putString(ConfirmDialog.ARG_MESSAGE, message);
        args.putInt(ConfirmDialog.ARG_RETURN_CODE, returnCode);
        
        confirmDialog = new ConfirmDialog();
        confirmDialog.setArguments(args);
        confirmDialog.setOnConfirmDialogResultListener(this);
        confirmDialog.show(fm, "fragment_edit_name");
    }
}
