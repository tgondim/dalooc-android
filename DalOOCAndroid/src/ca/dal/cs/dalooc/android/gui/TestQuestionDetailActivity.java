package ca.dal.cs.dalooc.android.gui;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import ca.dal.cs.dalooc.android.R;
import ca.dal.cs.dalooc.model.Option;
import ca.dal.cs.dalooc.model.TestAnswer;
import ca.dal.cs.dalooc.model.TestQuestion;
import ca.dal.cs.dalooc.model.User;

public class TestQuestionDetailActivity extends Activity implements OnClickListener {
	
	public static final String CHOOSE_CORRECT_ARG = "choose_correct";
	public static final String TEST_ANSWER_ARG = "test_answer";
	
	private User user;
	
	private boolean chooseCorrect = false;
	
	private TestQuestion testQuestion;
	
	private TestAnswer testAnswer;
	
	private RadioGroup radioGroup;
	
	private List<MyRadioButton> radioButtonList;
	
	private Button btnSubmit;
	
	private ImageView ivTestQuestionDetailThumbnail;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_test_question_detail);
	
		this.testQuestion = (TestQuestion)getIntent().getExtras().getSerializable(LearningObjectSectionFragment.ARG_TEST_QUESTION);
		this.user = (User)getIntent().getExtras().getSerializable(LoginActivity.ARG_USER);
		
		this.radioButtonList = new ArrayList<MyRadioButton>();
		
		this.btnSubmit = (Button)findViewById(R.id.btnSubmit);
		this.btnSubmit.setOnClickListener(this);
		
		this.ivTestQuestionDetailThumbnail = (ImageView)findViewById(R.id.ivTestQuestionDetailThumbnail);
		
		TextView txtTestQuestion= (TextView)findViewById(R.id.txtTestQuestion);
		txtTestQuestion.setText(this.testQuestion.getQuestion());
		
		LinearLayout llRadioGroup = (LinearLayout)findViewById(R.id.llRadioGroup);
		
		this.radioGroup = new RadioGroup(this);
		MyRadioButton rb;
		Option option;
		
		for (int i = 0; i < testQuestion.getOptionList().size(); i++) {
			
			option = testQuestion.getOptionList().get(i);
			rb = new MyRadioButton(this, i);
			this.radioButtonList.add(rb);
			rb.setText(option.getItem() + getResources().getString(R.string.item_separator) + option.getStatement());
			rb.setOnClickListener(this);
			radioGroup.addView(rb);
		}
		llRadioGroup.addView(radioGroup);
	}
	
	@Override
	public void onBackPressed() {
		setResultParameters();
		super.onBackPressed();
	}

	private void setResultParameters() {
		Intent result = new Intent();
		result.putExtra(CHOOSE_CORRECT_ARG, this.chooseCorrect);
		result.putExtra(TEST_ANSWER_ARG, this.testAnswer);
		setResult(RESULT_OK, result);
	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
	}

	@Override
	public void onClick(View v) {
		if (v instanceof RadioButton) {
			for (RadioButton auxRb : this.radioButtonList) {
				if (auxRb.getId() == v.getId()) {
					auxRb.setSelected(true);
				}
			}
		} else if (v instanceof Button) { 
			Option option;
			for (MyRadioButton radioButton : this.radioButtonList) {
				if (radioButton.isChecked()) {
					option = this.testQuestion.getOptionList().get(radioButton.index);
					this.chooseCorrect = option.isCorrect();
					if (this.chooseCorrect) {
						this.ivTestQuestionDetailThumbnail.setImageDrawable(getResources().getDrawable(R.drawable.ic_correct));
						this.testAnswer = new TestAnswer();
						this.testAnswer.setUserId(this.user.getId());
						this.testAnswer.setTestQuestionId(this.testQuestion.getId());
						this.testAnswer.setOptionId(option.getId());
						//TODO LearningObjectId and CourseId are missing
					} else {
						//TODO wrong answer!
						this.ivTestQuestionDetailThumbnail.setImageDrawable(getResources().getDrawable(R.drawable.ic_incorrect));
						this.testAnswer = null;
					}
					this.btnSubmit.setEnabled(false);
					for (RadioButton rb : this.radioButtonList) {
						rb.setEnabled(false);
					}
				}
			}
		}
	}
	
//	@Override
//	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
//		super.onActivityResult(requestCode, resultCode, data);
//	    if (requestCode == GET_DOCUMENT_FILE_ACTIVITY_REQUEST_CODE) {
//	        if (resultCode == RESULT_OK) {
//    			ConfirmDialogFragment confirm = new ConfirmDialogFragment();
//	        	Bundle bundle = new Bundle();
//	        	bundle.putInt(ConfirmDialogFragment.ARG_CONFIRM_MESSAGE, ConfirmDialogFragment.DOCUMENT_CONFIRM_MESSAGE);
//	        	confirm.setArguments(bundle);
//	        	confirm.show(getFragmentManager(), "Confirm");
//	        	if (confirm.getLastChoice()) {
//	        		this.document.setDocumentUrl(data.getDataString().replace("file://", ""));
//	         	} else {
//	         		//DO NOTHING
//	         	}
//	        } else if (resultCode == RESULT_CANCELED) {
//	            // User cancelled the video capture
//	        } else {
//	            // Video capture failed, advise user
//	        	//TODO
//	        }
//	    }
//	}
//
//	@Override
//	public void onDownloadDocumentDone(File file) {
//		if (file != null) {
//			openDocument(file);		
//		} else {
//			//TODO see what to do case the file is not downloaded
//		}
//	}

//	private void openDocument(File file) {
//		String mimeType = "application/*";
//		String extension = MimeTypeMap.getFileExtensionFromUrl(document.getDocumentUrl());
//		
//		if (MimeTypeMap.getSingleton().hasExtension(extension)) {
//			mimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension);
//		}
//		try {
//			Intent documentPreviewIntent = new Intent(Intent.ACTION_VIEW);
//			documentPreviewIntent.setDataAndType(Uri.fromFile(file), mimeType);
//	        startActivity(documentPreviewIntent);
//		} 
//		catch (ActivityNotFoundException e) {
//		    Toast.makeText(TestQuestionDetailActivity.this, 
//		        getResources().getString(R.string.no_application_available), 
//		        Toast.LENGTH_LONG).show();
//		}
//	}
	private class MyRadioButton extends RadioButton {
		
		protected int index;

		public MyRadioButton(Context context, int index) {
			super(context);
			this.index = index;
		}
		
	}
}
