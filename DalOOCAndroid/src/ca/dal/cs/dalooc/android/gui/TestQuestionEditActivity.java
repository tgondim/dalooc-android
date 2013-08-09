package ca.dal.cs.dalooc.android.gui;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.text.InputType;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;
import ca.dal.cs.dalooc.android.R;
import ca.dal.cs.dalooc.android.gui.components.ConfirmDialog;
import ca.dal.cs.dalooc.android.gui.components.MyRadioButton;
import ca.dal.cs.dalooc.android.gui.listener.OnConfirmDialogReturnListener;
import ca.dal.cs.dalooc.android.util.ItemLetterDispenser;
import ca.dal.cs.dalooc.model.Course;
import ca.dal.cs.dalooc.model.LearningObjectContent;
import ca.dal.cs.dalooc.model.Option;
import ca.dal.cs.dalooc.model.TestQuestion;
import ca.dal.cs.dalooc.model.User;

public class TestQuestionEditActivity extends FragmentActivity implements OnConfirmDialogReturnListener {
	
	public static final int ACTION_CONFIRM_TEST_QUESTION_CHANGES = 200;
	
	public static final int ACTION_CONFIRM_TEST_QUESTION_UPLOAD = 300;
	
	private static final int RELATED_CONTENT_INDEX = 0;
	
	private static final int RELATED_CONTENT_OBJECT = 1;
	
	private static final int LAYOUT_VIEW = 0;
	
	private static final int NAME_VIEW = 1;
	
	private static final int RADIO_BUTTON = 2;

	private static final int OBJECT_ITEM = 3;

	private TestQuestion testQuestion;

	private User user;
	
	private Course course;
	
	private int learningObjectIndex;

	private int testQuestionIndex;
	
	private ConfirmDialog confirmDialog;
	
	private EditText etQuestion;
	
	private TextView tvRelatedContentName;
	
	private ImageView ivAddOption;

	private Button ibSelectRelatedContent;
	
	private LinearLayout llOptions;
	
	private RadioGroup radioGroup;
	
	private List<MyRadioButton> radioButtonList;
	
	private Map<ImageView, Object[]> optionsLayoutMapping;
	
	private Object[] relatedContent;
	
	private ItemLetterDispenser letterDispenser;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_test_question_edit);
		
		this.letterDispenser = new ItemLetterDispenser();
		
		this.etQuestion = (EditText)findViewById(R.id.etQuestion);
		this.tvRelatedContentName = (TextView)findViewById(R.id.tvRelatedContentName);
		
		this.llOptions = (LinearLayout)findViewById(R.id.llOptions);
		
		this.optionsLayoutMapping = new HashMap<ImageView, Object[]>();
		
		this.radioButtonList = new ArrayList<MyRadioButton>();
		
		LinearLayout llTestQuestionEditButtons = (LinearLayout)findViewById(R.id.llTestQuestionEditButtons);
		
		LinearLayout llTestQuestionRadioButtons = (LinearLayout)findViewById(R.id.llTestQuestionRadioButtons);
		
		this.radioGroup = new RadioGroup(this);
		this.radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
			
			@Override
			public void onCheckedChanged(RadioGroup group, int checkedId) {
				 int index = group.indexOfChild(group.findViewById(checkedId));
				 
				 for (int i = 0; i < TestQuestionEditActivity.this.testQuestion.getOptionList().size(); i++) {
					 if (index == i) {
						 TestQuestionEditActivity.this.testQuestion.getOptionList().get(i).setCorrect(true);
					 } else {
						 TestQuestionEditActivity.this.testQuestion.getOptionList().get(i).setCorrect(false);
						 
					 }
				}
			}
		});
		
		this.ivAddOption = (ImageView)findViewById(R.id.ivAddOption);
		this.ivAddOption.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				createOptionEntry(null, -1);
			}
		});
		
		ibSelectRelatedContent = new Button(this);
		ibSelectRelatedContent.setText(getResources().getString(R.string.select_related_content));
//		ibDocumentUpload.setOnClickListener(new View.OnClickListener() {
//			
//			@Override
//			public void onClick(View v) {
//				Intent uploadFileIntent = new Intent(Intent.ACTION_GET_CONTENT);
//				switch (testQuestion.getType()) {
//				case PDF: 
//					uploadFileIntent.setType("file/pdf");
//					break;
//				case DOC: 
//					uploadFileIntent.setType("file/doc");
//					break;
//				case PPT: 
//					uploadFileIntent.setType("file/pdf");
//					break;
//				default:
//					uploadFileIntent.setType("file/*");
//				}
//				
//				startActivityForResult(uploadFileIntent, DocumentDetailActivity.GET_DOCUMENT_FILE_ACTIVITY_REQUEST_CODE);				
//			}
//		});
		
		LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
				ViewGroup.LayoutParams.WRAP_CONTENT, 
				ViewGroup.LayoutParams.WRAP_CONTENT, 
				0);
		
		llTestQuestionRadioButtons.addView(this.radioGroup, params);

		params = new LinearLayout.LayoutParams(
				ViewGroup.LayoutParams.WRAP_CONTENT, 
				ViewGroup.LayoutParams.WRAP_CONTENT, 
				0);
		params.gravity = Gravity.CENTER;

		llTestQuestionEditButtons.addView(ibSelectRelatedContent, params);
		
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
			this.testQuestionIndex = extras.getInt(LearningObjectSectionFragment.ARG_TEST_QUESTION_INDEX);

			if (this.learningObjectIndex >= 0) {
//				this.learningObject = this.course.getLearningObjectList().get(this.learningObjectIndex);
				if (this.testQuestionIndex >= 0) {
					this.testQuestion = this.course.getLearningObjectList().get(this.learningObjectIndex).getTestQuestionList().get(testQuestionIndex);
				}
			}
			
			if (this.testQuestion != null) {
				loadData();
				getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
			} else {
				this.testQuestion = new TestQuestion();
			}
		}
	}

	private void loadData() {
		this.etQuestion.setText(this.testQuestion.getQuestion());
		
		List<Option> optionsList = this.course.getLearningObjectList().get(this.learningObjectIndex).getTestQuestionList().get(testQuestionIndex).getOptionList();
		for (int i = 0; i < optionsList.size(); i++) {
			createOptionEntry(optionsList.get(i), i);
		}
		
		if (!TextUtils.isEmpty(this.testQuestion.getRelatedContendId())) {
			this.relatedContent = getRelatedContent(this.testQuestion.getRelatedContendId());
			this.tvRelatedContentName.setText(((LearningObjectContent)relatedContent[1]).getName());
			this.tvRelatedContentName.setVisibility(View.VISIBLE);
		} else {
			this.tvRelatedContentName.setVisibility(View.GONE);
		}
	}

	private void fetchData() {
		this.testQuestion.setQuestion(this.etQuestion.getText().toString());
		if (this.relatedContent != null) {
			this.testQuestion.setRelatedContendId(((LearningObjectContent)this.relatedContent[RELATED_CONTENT_OBJECT]).getId());
		}
		
		Iterator<ImageView> iterator = this.optionsLayoutMapping.keySet().iterator();
		this.testQuestion.getOptionList().clear();
		
		while (iterator.hasNext()) {
			Object[] object = this.optionsLayoutMapping.get(iterator.next());
			Option option = ((Option)object[OBJECT_ITEM]);
			option.setStatement(((EditText)object[NAME_VIEW]).getText().toString());
			option.setStatement(((EditText)object[NAME_VIEW]).getText().toString());
			
			MyRadioButton radioButton = (MyRadioButton)object[RADIO_BUTTON];
			
			if (radioButton != null) {
				if (radioButton.isChecked()) {
					option.setCorrect(true);
				}
			}

			option.setItem(String.valueOf(ItemLetterDispenser
					.getLetterInPosition(radioButton.getIndex())));
			this.testQuestion.getOptionList().add(option);
		}
		if (this.testQuestion.getOptionList().size() > 0) {
			Collections.sort(this.testQuestion.getOptionList(),
					new Comparator<Option>() {
						@Override
						public int compare(final Option object1, final Option object2) {
							return object1.getItem().toLowerCase().compareTo(object2.getItem().toLowerCase());
						}
					});
		}
	}
	
	private void finishSaving() {
		fetchData();
		Intent resultIntent = new Intent();
		resultIntent.putExtra(LearningObjectSectionFragment.ARG_TEST_QUESTION, TestQuestionEditActivity.this.testQuestion);
		resultIntent.putExtra(LearningObjectSectionFragment.ARG_TEST_QUESTION_INDEX, TestQuestionEditActivity.this.testQuestionIndex);
		setResult(Activity.RESULT_OK, resultIntent);
		finish();
	}

	private void finishWithoutSaving() {
		Intent resultIntent = new Intent();
		setResult(Activity.RESULT_CANCELED, resultIntent);
		finish();
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.test_question_edit, menu);
		
		return true;
	}
	
	@Override
	public void onBackPressed() {
//		super.onBackPressed();
//		fetchData();
		showConfirmDialog(getResources().getString(R.string.confirm_changes), ACTION_CONFIRM_TEST_QUESTION_CHANGES);
	}
	
	@Override
	public void onConfirmDialogReturn(boolean confirm, int returnCode) {
		switch (returnCode) {
		case ACTION_CONFIRM_TEST_QUESTION_CHANGES:
			this.confirmDialog.dismiss();
			if (confirm) {
				finishSaving();
			} else {
				finishWithoutSaving();
			}
			
			break;
		}
	}
	
	protected void createOptionEntry(Option option, int position) {
		
		EditText editText = new EditText(TestQuestionEditActivity.this);
		editText.setInputType(InputType.TYPE_TEXT_FLAG_MULTI_LINE);
		
		if (option == null) {
			option = new Option();
			option.setItem(String.valueOf(this.letterDispenser.getNextItem()));
			position = this.letterDispenser.getLastItemNumberTaken();
			editText.requestFocus();
		} else {
			this.letterDispenser.getNextItem();
		}
		
		RelativeLayout relativeLayout = new RelativeLayout(TestQuestionEditActivity.this);
		
		
		editText.setText(option.getStatement());
		
		ImageView imageView = new ImageView(TestQuestionEditActivity.this);
		imageView.setImageDrawable(TestQuestionEditActivity.this.getResources().getDrawable(R.drawable.content_discard));
		imageView.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				View viewToRemove = (View)TestQuestionEditActivity.this.optionsLayoutMapping.get(v)[LAYOUT_VIEW];
				RadioButton radioButtonToRemove = (RadioButton)TestQuestionEditActivity.this.optionsLayoutMapping.get(v)[RADIO_BUTTON];
				TestQuestionEditActivity.this.radioGroup.removeView(radioButtonToRemove);
				((RelativeLayout)viewToRemove).removeAllViews();
				TestQuestionEditActivity.this.llOptions.removeViewInLayout(viewToRemove);
				TestQuestionEditActivity.this.optionsLayoutMapping.remove(v);
			}
		});
		
		//EditText
		RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
		params.addRule(RelativeLayout.ALIGN_PARENT_LEFT, RelativeLayout.TRUE);
		params.addRule(RelativeLayout.CENTER_VERTICAL, RelativeLayout.TRUE);
		params.addRule(RelativeLayout.ALIGN_LEFT, imageView.getId());
		params.setMargins(0, 0, 50, 0);
		
		relativeLayout.addView(editText, params);
		
		MyRadioButton radioButton = new MyRadioButton(this, position);
		
		radioButton.setChecked(option.isCorrect());
		
		this.radioButtonList.add(radioButton);
		radioButton.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				for (MyRadioButton auxRb : TestQuestionEditActivity.this.radioButtonList) {
					if (auxRb.getId() == v.getId()) {
						auxRb.setChecked(true);
						
					} else {
						auxRb.setChecked(false);
					}
				}				
			}
		});

		radioGroup.addView(radioButton);
		
		//ImageView
		params = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
		params.addRule(RelativeLayout.ALIGN_PARENT_RIGHT, RelativeLayout.TRUE);
		params.addRule(RelativeLayout.ALIGN_PARENT_TOP, RelativeLayout.TRUE);
		
		relativeLayout.addView(imageView, params);
		
		Object[] viewArray = new Object[4];
		viewArray[LAYOUT_VIEW] = relativeLayout;
		viewArray[NAME_VIEW] = editText;
		viewArray[RADIO_BUTTON] = radioButton;
		viewArray[OBJECT_ITEM] = option;
		
		TestQuestionEditActivity.this.optionsLayoutMapping.put(imageView, viewArray);
		
		LinearLayout.LayoutParams llParams = new LinearLayout.LayoutParams(
				ViewGroup.LayoutParams.MATCH_PARENT, 
				ViewGroup.LayoutParams.WRAP_CONTENT, 
				0);
//		llParams.setMargins(0, 10, 0, 0);
		TestQuestionEditActivity.this.llOptions.addView(relativeLayout, llParams);
		
	}
	
	private Object[] getRelatedContent(String relatedContentId) {
		Object[] relatedContent = getLearningObjectContent(this.course.getLearningObjectList().get(this.learningObjectIndex).getVideoList(), relatedContentId);
		if (relatedContent != null) {
			return relatedContent;
		}
		
		relatedContent = getLearningObjectContent(this.course.getLearningObjectList().get(this.learningObjectIndex).getAudioList(), relatedContentId);
		if (relatedContent != null) {
			return relatedContent;
		}
		
		relatedContent = getLearningObjectContent(this.course.getLearningObjectList().get(this.learningObjectIndex).getDocumentList(), relatedContentId);
		if (relatedContent != null) {
			return relatedContent;
		}
		
		return null;
	}
	
	private Object[] getLearningObjectContent(@SuppressWarnings("rawtypes") List learningObjectContentList, String learningObjectContentId) {
		Object[] relatedContent = new Object[2];
		LearningObjectContent learningObjectContent;
		
		for (int i =0; i < learningObjectContentList.size(); i++) {
			learningObjectContent = (LearningObjectContent)learningObjectContentList.get(i);
			if (learningObjectContent.getId().equals(learningObjectContentId)) {
				relatedContent[RELATED_CONTENT_INDEX] = i;
				relatedContent[RELATED_CONTENT_OBJECT] = learningObjectContent;
				
				return relatedContent;
			}
		}
		
		return null;
	}
	
//	private void showToast(String msg) {
//		if (this.toast == null) {
//			this.toast = Toast.makeText(this, "", Toast.LENGTH_LONG);
//		}
//		this.toast.setText(msg);
//		this.toast.show();
//	}
	
	private void showConfirmDialog(String message, int returnCode) {
        FragmentManager fm = getSupportFragmentManager();
        
        Bundle args = new Bundle();
        args.putString(ConfirmDialog.ARG_TITLE, getResources().getString(R.string.dialog_title_test_question));
        args.putString(ConfirmDialog.ARG_MESSAGE, message);
        args.putInt(ConfirmDialog.ARG_RETURN_CODE, returnCode);
        
        confirmDialog = new ConfirmDialog();
        confirmDialog.setArguments(args);
        confirmDialog.setOnConfirmDialogResultListener(this);
        confirmDialog.show(fm, "fragment_edit_name");
    }
}
