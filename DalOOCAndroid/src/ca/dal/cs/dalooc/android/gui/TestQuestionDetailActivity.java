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
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import ca.dal.cs.dalooc.android.R;
import ca.dal.cs.dalooc.model.Audio;
import ca.dal.cs.dalooc.model.Course;
import ca.dal.cs.dalooc.model.Document;
import ca.dal.cs.dalooc.model.LearningObjectContent;
import ca.dal.cs.dalooc.model.Option;
import ca.dal.cs.dalooc.model.TestAnswer;
import ca.dal.cs.dalooc.model.TestQuestion;
import ca.dal.cs.dalooc.model.User;
import ca.dal.cs.dalooc.model.Video;

public class TestQuestionDetailActivity extends Activity implements OnClickListener {
	
	public static final String CHOOSE_CORRECT_ARG = "choose_correct";
	public static final String TEST_ANSWER_ARG = "test_answer";
	
	private User user;
	
	private Course course;
	
	private TestQuestion testQuestion;
	
	private TestAnswer testAnswer;
	
	private int learningObjectIndex;

	private int testQuestionIndex;
	
	private boolean chooseCorrect = false;
	
	private RadioGroup radioGroup;
	
	private List<MyRadioButton> radioButtonList;
	
	private Button btnSubmit;
	
	private ImageView ivTestQuestionDetailThumbnail;

	private ImageView ivTestQuestionRelatedContentThumbnail;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_test_question_detail);
	
		this.user = (User)getIntent().getExtras().getSerializable(LoginActivity.ARG_USER);
		this.course = (Course)getIntent().getExtras().getSerializable(CourseSectionFragment.ARG_COURSE);
		this.learningObjectIndex = getIntent().getExtras().getInt(LearningObjectSectionFragment.ARG_LEARNING_OBJECT_INDEX);
		this.testQuestionIndex = getIntent().getExtras().getInt(LearningObjectSectionFragment.ARG_TEST_QUESTION_INDEX);

		this.testQuestion = this.course.getLearningObjectList().get(learningObjectIndex).getTestQuestionList().get(testQuestionIndex);
		
		this.radioButtonList = new ArrayList<MyRadioButton>();
		
		this.btnSubmit = (Button)findViewById(R.id.btnSubmit);
		this.btnSubmit.setOnClickListener(this);
		
		this.ivTestQuestionDetailThumbnail = (ImageView)findViewById(R.id.ivTestQuestionAnswerThumbnail);
		
		this.ivTestQuestionRelatedContentThumbnail = (ImageView)findViewById(R.id.ivTestQuestionRelatedContentThumbnail);
		this.ivTestQuestionRelatedContentThumbnail.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				Object[] learningObjectContent = getRelatedContent(TestQuestionDetailActivity.this.testQuestion.getRelatedContendId());
				if (learningObjectContent != null) {
					Intent relatedContentDetailIntent = null;
					
					if (learningObjectContent[1] instanceof Video) {
						relatedContentDetailIntent = new Intent("VIDEO_DETAIL_ACTIVITY");
						
					} else if (learningObjectContent[1] instanceof Audio) {
						relatedContentDetailIntent = new Intent("AUDIO_DETAIL_ACTIVITY");
						
					} else if (learningObjectContent[1] instanceof Document) {
						relatedContentDetailIntent = new Intent("DOCUMENT_DETAIL_ACTIVITY");
					}

					if (relatedContentDetailIntent != null) {
						relatedContentDetailIntent.putExtra(LoginActivity.ARG_USER, TestQuestionDetailActivity.this.user);
						relatedContentDetailIntent.putExtra(CourseSectionFragment.ARG_COURSE, TestQuestionDetailActivity.this.course);
						relatedContentDetailIntent.putExtra(LearningObjectSectionFragment.ARG_LEARNING_OBJECT_INDEX, TestQuestionDetailActivity.this.learningObjectIndex);
						relatedContentDetailIntent.putExtra(LearningObjectSectionFragment.ARG_DOCUMENT_INDEX, (Integer)learningObjectContent[0]);
						
						startActivity(relatedContentDetailIntent);
					} 
				} else {
					//TODO what to do when the relatedContent is not found
				}
			}
		});
		
		TextView txtTestQuestion= (TextView)findViewById(R.id.txtTestQuestion);
		txtTestQuestion.setText(this.testQuestion.getQuestion());
		
		LinearLayout llRadioGroup = (LinearLayout)findViewById(R.id.llTestQuestionRadioGroup);
		
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
						this.ivTestQuestionRelatedContentThumbnail.setVisibility(View.VISIBLE);
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
		
		for (int i =0; i< learningObjectContentList.size(); i++) {
			learningObjectContent = (LearningObjectContent)learningObjectContentList.get(i);
			if (learningObjectContent.getId().equals(learningObjectContentId)) {
				relatedContent[0] = i;
				relatedContent[1] = learningObjectContent;
				
				return relatedContent;
			}
		}
		
		return null;
	}
	
	private class MyRadioButton extends RadioButton {
		
		protected int index;

		public MyRadioButton(Context context, int index) {
			super(context);
			this.index = index;
		}
		
	}
}
