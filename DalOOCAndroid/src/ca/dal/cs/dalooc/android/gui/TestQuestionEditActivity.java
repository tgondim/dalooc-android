package ca.dal.cs.dalooc.android.gui;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import android.app.Activity;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
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
import ca.dal.cs.dalooc.android.gui.component.ConfirmDialog;
import ca.dal.cs.dalooc.android.gui.component.MyRadioButton;
import ca.dal.cs.dalooc.android.gui.component.SelectLearningObjectContentDialog;
import ca.dal.cs.dalooc.android.gui.listener.OnConfirmDialogReturnListener;
import ca.dal.cs.dalooc.android.gui.listener.OnSelectLearningObjectContentDialogReturnListener;
import ca.dal.cs.dalooc.android.task.DownloadImageTask;
import ca.dal.cs.dalooc.android.util.ItemLetterDispenser;
import ca.dal.cs.dalooc.model.Audio;
import ca.dal.cs.dalooc.model.Course;
import ca.dal.cs.dalooc.model.Document;
import ca.dal.cs.dalooc.model.LearningObjectContent;
import ca.dal.cs.dalooc.model.Option;
import ca.dal.cs.dalooc.model.TestQuestion;
import ca.dal.cs.dalooc.model.User;
import ca.dal.cs.dalooc.model.Video;

public class TestQuestionEditActivity extends FragmentActivity implements OnConfirmDialogReturnListener, OnSelectLearningObjectContentDialogReturnListener {
	
	public static final int ACTION_CONFIRM_TEST_QUESTION_CHANGES = 200;
	
	public static final int ACTION_CONFIRM_TEST_QUESTION_UPLOAD = 300;
	
	public static final int RELATED_CONTENT_INDEX = 0;
	
	public static final int RELATED_CONTENT_OBJECT = 1;
	
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

	private SelectLearningObjectContentDialog selectRelatedContentDialog;
	
	private EditText etQuestion;
	
	private EditText etTestQuestionOrder;
	
	private TextView tvRelatedContentName;
	
	private TextView txtOption;
	
	private ImageView ivLearningObjectContentIcon;
	
	private ImageView ivAddOption;

	private Button btnSelectRelatedContent;
	
	private Button btnClearRelatedContent;
	
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
		
		this.txtOption = (TextView)findViewById(R.id.txtOption);
		this.etQuestion = (EditText)findViewById(R.id.etQuestion);
		this.etTestQuestionOrder = (EditText)findViewById(R.id.etTestQuestionOrder);
		this.tvRelatedContentName = (TextView)findViewById(R.id.tvRelatedContentName);
		this.ivLearningObjectContentIcon = (ImageView)findViewById(R.id.ivLearningObjectContentIcon);
		
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
		
		LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
				ViewGroup.LayoutParams.WRAP_CONTENT, 
				ViewGroup.LayoutParams.WRAP_CONTENT, 
				0);
		
		llTestQuestionRadioButtons.addView(this.radioGroup, params);

		this.ivAddOption = (ImageView)findViewById(R.id.ivAddOption);
		this.ivAddOption.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				createOptionEntry(null, -1);
			}
		});
		
		btnSelectRelatedContent = new Button(this);
		btnSelectRelatedContent.setText(getResources().getString(R.string.select_related_content));
		btnSelectRelatedContent.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				showSelectRelatedContentDialog();
			}
		});

		params = new LinearLayout.LayoutParams(
				ViewGroup.LayoutParams.WRAP_CONTENT, 
				ViewGroup.LayoutParams.WRAP_CONTENT, 
				0);
		params.gravity = Gravity.CENTER;

		llTestQuestionEditButtons.addView(btnSelectRelatedContent, params);

		btnClearRelatedContent = new Button(this);
		btnClearRelatedContent.setText(getResources().getString(R.string.clear_related_content));
		btnClearRelatedContent.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				TestQuestionEditActivity.this.relatedContent = null;
				setLearningObjectRelatedContentVisibility(View.GONE);
			}

		});
		
		params = new LinearLayout.LayoutParams(
				ViewGroup.LayoutParams.WRAP_CONTENT, 
				ViewGroup.LayoutParams.WRAP_CONTENT, 
				0);
		params.gravity = Gravity.CENTER;
		
		llTestQuestionEditButtons.addView(btnClearRelatedContent, params);
		
		Button btnSave = (Button)findViewById(R.id.btnSave);
		btnSave.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				resetFieldErrors();
				
				boolean cancel = false;
				View focusView = null;
				
				// Check for a valid order.
				if (TextUtils.isEmpty(TestQuestionEditActivity.this.etTestQuestionOrder.getText().toString().replaceAll("\\s+$", ""))) {
					TestQuestionEditActivity.this.etTestQuestionOrder.setError(getString(R.string.error_field_required));
					focusView = TestQuestionEditActivity.this.etTestQuestionOrder;
					cancel = true;
				} 

				// Check for a valid name.
				if (TextUtils.isEmpty(TestQuestionEditActivity.this.etQuestion.getText().toString().replaceAll("\\s+$", ""))) {
					TestQuestionEditActivity.this.etQuestion.setError(getString(R.string.error_field_required));
					focusView = TestQuestionEditActivity.this.etQuestion;
					cancel = true;
				} 
				
				// Check for a correct answer.
				if (TestQuestionEditActivity.this.radioGroup.getCheckedRadioButtonId() == -1) {
					if ((TestQuestionEditActivity.this.radioButtonList != null) && (TestQuestionEditActivity.this.radioButtonList.size() > 0)) {
						if (TestQuestionEditActivity.this.radioButtonList.get(0) != null) {
							TestQuestionEditActivity.this.radioButtonList.get(0).setError(getString(R.string.error_correct_answer_required));
							focusView = TestQuestionEditActivity.this.radioButtonList.get(0);
							cancel = true;
						} 
					}
				} 
				
				if (cancel) {
					focusView.requestFocus();
				} else {
					finishSaving();
				}
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
	
	private void resetFieldErrors() {
		this.etTestQuestionOrder.setError(null);
		this.etQuestion.setError(null);
		
		if ((this.radioButtonList != null) && (this.radioButtonList.size() > 0)) {
			if (this.radioButtonList.get(0) != null) {
				this.radioButtonList.get(0).setError(null);
			} 
		}
	}

	private void setLearningObjectRelatedContentVisibility(int visibility) {
		TestQuestionEditActivity.this.ivLearningObjectContentIcon.setVisibility(visibility);
		TestQuestionEditActivity.this.tvRelatedContentName.setVisibility(visibility);
	}
	
	private void loadData() {
		this.etQuestion.setText(this.testQuestion.getQuestion());
		this.etTestQuestionOrder.setText(String.valueOf(this.testQuestion.getOrder()));
		
		List<Option> optionsList = this.course.getLearningObjectList().get(this.learningObjectIndex).getTestQuestionList().get(testQuestionIndex).getOptionList();
		for (int i = 0; i < optionsList.size(); i++) {
			createOptionEntry(optionsList.get(i), i);
		}
		
		if (!TextUtils.isEmpty(this.testQuestion.getRelatedContendId())) {
			this.relatedContent = getRelatedContent(this.testQuestion.getRelatedContendId());
			setLearningObjectContentDrawable(this.relatedContent);
			this.tvRelatedContentName.setText(((LearningObjectContent)relatedContent[RELATED_CONTENT_OBJECT]).getName());
			this.tvRelatedContentName.setVisibility(View.VISIBLE);
		} else {
			this.tvRelatedContentName.setVisibility(View.GONE);
		}
	}

	private void fetchData() {
		this.testQuestion.setQuestion(this.etQuestion.getText().toString());
		this.testQuestion.setOrder(Integer.valueOf(this.etTestQuestionOrder.getText().toString()));
		if (this.relatedContent != null) {
			this.testQuestion.setRelatedContendId(((LearningObjectContent)this.relatedContent[RELATED_CONTENT_OBJECT]).getId());
		} else {
			this.testQuestion.setRelatedContendId("");
		}
		
		Iterator<ImageView> iterator = this.optionsLayoutMapping.keySet().iterator();
		this.testQuestion.getOptionList().clear();
		
		while (iterator.hasNext()) {
			Object[] object = this.optionsLayoutMapping.get(iterator.next());
			Option option = ((Option)object[OBJECT_ITEM]);
			
			String statement = ((EditText)object[NAME_VIEW]).getText().toString();
			option.setStatement(statement);
			
			if (TextUtils.isEmpty(statement)) {
				continue;
			}
			
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
							return object1.getItem().toLowerCase(Locale.getDefault()).compareTo(object2.getItem().toLowerCase());
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
		TestQuestionEditActivity.this.llOptions.addView(relativeLayout, llParams);
		
	}
	
//	private Object[] getRelatedContent(String relatedContentId) {
//		
//		Object[] relatedContent = new Object[2];
//		LearningObjectContent learningObjectContent;
//		List<LearningObjectContent> learningObjectContentList = this.course.getLearningObjectList().get(this.learningObjectIndex).getLearningObjectContentList();
//		
//		for (int i = 0; i < learningObjectContentList.size(); i++) {
//			learningObjectContent = (LearningObjectContent)learningObjectContentList.get(i);
//			if (learningObjectContent.getId().equals(relatedContentId)) {
//				relatedContent[RELATED_CONTENT_INDEX] = i;
//				relatedContent[RELATED_CONTENT_OBJECT] = learningObjectContent;
//				
//				return relatedContent;
//			}
//		}
//		
//		return null;
//	}

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
				relatedContent[TestQuestionEditActivity.RELATED_CONTENT_INDEX] = i;
				relatedContent[TestQuestionEditActivity.RELATED_CONTENT_OBJECT] = learningObjectContent;
				
				return relatedContent;
			}
		}
		
		return null;
	}
	
	private void downloadVideoIcon(Video video, ImageView ivVideoThumbnail, int waitTime) {
		if (waitTime > 0) {
			ivVideoThumbnail.setImageDrawable(getResources().getDrawable(R.drawable.ic_video_thumbnail_default));
		}
		new DownloadImageTask(ivVideoThumbnail).execute(getResources().getString(R.string.host_file_server)
				+ getResources().getString(R.string.videos_folder)
				+ "/thumb/" + video.getContentFileName().replace("mp4", "jpg"), String.valueOf(waitTime));
	}
	
	private void showConfirmDialog(String message, int returnCode) {
        FragmentManager fm = getSupportFragmentManager();
        
        Bundle args = new Bundle();
        args.putString(ConfirmDialog.ARG_TITLE, getResources().getString(R.string.dialog_title_test_question));
        args.putString(ConfirmDialog.ARG_MESSAGE, message);
        args.putInt(ConfirmDialog.ARG_RETURN_CODE, returnCode);
        
        this.confirmDialog = new ConfirmDialog();
        this.confirmDialog.setArguments(args);
        this.confirmDialog.setStyle(DialogFragment.STYLE_NORMAL, android.R.style.Theme_Holo_Dialog);
        this.confirmDialog.setOnConfirmDialogResultListener(this);
        this.confirmDialog.show(fm, "fragment_edit_name");
    }
	
	private void showSelectRelatedContentDialog() {
		Bundle args = new Bundle();	
        args.putSerializable(LearningObjectSectionFragment.ARG_LEARNING_OBJECT, this.course.getLearningObjectList().get(this.learningObjectIndex));
        
        this.selectRelatedContentDialog = new SelectLearningObjectContentDialog();
        this.selectRelatedContentDialog.setArguments(args);
        this.selectRelatedContentDialog.setStyle(DialogFragment.STYLE_NORMAL, android.R.style.Theme_Holo_Light_Dialog);
        this.selectRelatedContentDialog.setOnSelectLearningObjectContentDialogResultListener(this);
        this.selectRelatedContentDialog.show(getSupportFragmentManager(), "fragment_edit_name");
    }

	@Override
	public void onSelectRelatedContentDialogReturn(Object[] relatedContent) {
		this.tvRelatedContentName.setText(((LearningObjectContent)relatedContent[RELATED_CONTENT_OBJECT]).getName());
		this.testQuestion.setRelatedContendId(((LearningObjectContent)relatedContent[RELATED_CONTENT_OBJECT]).getId());
		this.relatedContent = relatedContent;
		setLearningObjectContentDrawable(relatedContent);
		setLearningObjectRelatedContentVisibility(View.VISIBLE);
	}

	private void setLearningObjectContentDrawable(Object[] relatedContent) {
		Drawable drawable = null;
		if (relatedContent[RELATED_CONTENT_OBJECT] instanceof Video) {
			drawable = getResources().getDrawable(R.drawable.ic_video_thumbnail_default);
			downloadVideoIcon((Video)relatedContent[RELATED_CONTENT_OBJECT], this.ivLearningObjectContentIcon, 0);
		} else if (relatedContent[RELATED_CONTENT_OBJECT] instanceof Audio) {
			drawable = getResources().getDrawable(R.drawable.ic_audio_light);
		} else if (relatedContent[RELATED_CONTENT_OBJECT] instanceof Document) {
			drawable = getResources().getDrawable(R.drawable.ic_document_light);
		}
		
		if (drawable != null) {
			this.ivLearningObjectContentIcon.setImageDrawable(drawable);
		}
	}
}
