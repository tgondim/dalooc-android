package ca.dal.cs.dalooc.android.gui;

import java.util.ArrayList;
import java.util.List;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;
import ca.dal.cs.dalooc.android.R;
import ca.dal.cs.dalooc.android.gui.components.MyRadioButton;
import ca.dal.cs.dalooc.android.gui.listener.OnWebServiceCallDoneListener;
import ca.dal.cs.dalooc.android.task.HasAnsweredCorrectCallTask;
import ca.dal.cs.dalooc.android.task.SaveTestAnswerCallTask;
import ca.dal.cs.dalooc.model.Audio;
import ca.dal.cs.dalooc.model.Course;
import ca.dal.cs.dalooc.model.Document;
import ca.dal.cs.dalooc.model.LearningObjectContent;
import ca.dal.cs.dalooc.model.Option;
import ca.dal.cs.dalooc.model.TestAnswer;
import ca.dal.cs.dalooc.model.TestQuestion;
import ca.dal.cs.dalooc.model.User;
import ca.dal.cs.dalooc.model.Video;

public class TestQuestionDetailActivity extends FragmentActivity implements OnWebServiceCallDoneListener {
	
	public static final String CHOOSE_CORRECT_ARG = "choose_correct";
	public static final String TEST_ANSWER_ARG = "test_answer";
	
	private HasAnsweredCorrectCallTask hasAnsweredCorrectCallTask;
	
	private SaveTestAnswerCallTask saveTestAnswerCallTask;
	
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
	
	private Toast toast;
	
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
		this.btnSubmit.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				if (v instanceof Button) { 
					Option option;

					TestQuestionDetailActivity.this.testAnswer = new TestAnswer();
					TestQuestionDetailActivity.this.testAnswer.setUserId(TestQuestionDetailActivity.this.user.getId());
					TestQuestionDetailActivity.this.testAnswer.setCourseId(TestQuestionDetailActivity.this.course.getId());
					TestQuestionDetailActivity.this.testAnswer.setLearningObjectId(TestQuestionDetailActivity.this.course.getLearningObjectList().get(TestQuestionDetailActivity.this.learningObjectIndex).getId());
					TestQuestionDetailActivity.this.testAnswer.setTestQuestionId(TestQuestionDetailActivity.this.testQuestion.getId());
					
					for (MyRadioButton radioButton : TestQuestionDetailActivity.this.radioButtonList) {
						if (radioButton.isChecked()) {
							option = TestQuestionDetailActivity.this.testQuestion.getOptionList().get(radioButton.getIndex());

							TestQuestionDetailActivity.this.testAnswer.setOptionId(option.getId());
							
							TestQuestionDetailActivity.this.chooseCorrect = option.isCorrect();
							if (TestQuestionDetailActivity.this.chooseCorrect) {
								TestQuestionDetailActivity.this.ivTestQuestionDetailThumbnail.setImageDrawable(getResources().getDrawable(R.drawable.ic_correct));
								TestQuestionDetailActivity.this.ivTestQuestionRelatedContentThumbnail.setVisibility(View.VISIBLE);
								
								TestQuestionDetailActivity.this.testAnswer.setCorrect(true);
								//TODO LearningObjectId and CourseId are missing
							} else {
								TestQuestionDetailActivity.this.ivTestQuestionDetailThumbnail.setImageDrawable(getResources().getDrawable(R.drawable.ic_incorrect));
								
								TestQuestionDetailActivity.this.testAnswer.setCorrect(false);
							}
							TestQuestionDetailActivity.this.btnSubmit.setEnabled(false);

							TestQuestionDetailActivity.this.saveTestAnswerCallTask = new SaveTestAnswerCallTask(TestQuestionDetailActivity.this.testAnswer);
							TestQuestionDetailActivity.this.saveTestAnswerCallTask.setOnWebServiceCallDoneListener(TestQuestionDetailActivity.this);
							TestQuestionDetailActivity.this.saveTestAnswerCallTask.execute(getUrlWebService(SaveTestAnswerCallTask.SAVE_TEST_ANSWER_WEB_SERVICE),
									getResources().getString(R.string.namespace_webservice),
									getResources().getString(R.string.save_test_answer_webservice_operation));
						}
					}
				}				
			}
		});
		
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
			rb.setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View v) {
					for (RadioButton auxRb : TestQuestionDetailActivity.this.radioButtonList) {
						if (auxRb.getId() == v.getId()) {
							auxRb.setChecked(true);
						} else {
							auxRb.setChecked(false);
						}
					}
				}
			});
			radioGroup.addView(rb);
		}
		llRadioGroup.addView(radioGroup);
		
		checkIfHasAnsweredCorrect();
	}
	
	private void checkIfHasAnsweredCorrect() {
		this.hasAnsweredCorrectCallTask = new HasAnsweredCorrectCallTask();
		this.hasAnsweredCorrectCallTask.setOnWebServiceCallDoneListener(TestQuestionDetailActivity.this);
		this.hasAnsweredCorrectCallTask.execute(getUrlWebService(HasAnsweredCorrectCallTask.HAS_ANSWERED_CORRECT_WEB_SERVICE),
				getResources().getString(R.string.namespace_webservice),
				getResources().getString(R.string.has_answered_correct_webservice_operation),
				this.user.getId(), 
				this.course.getId(), 
				this.course.getLearningObjectList().get(this.learningObjectIndex).getId(), 
				this.testQuestion.getId(),
				this.testQuestion.getCorrectOption().getId());
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
	public String getUrlWebService(int serviceCode) {
		if (serviceCode == SaveTestAnswerCallTask.SAVE_TEST_ANSWER_WEB_SERVICE) {
			return getResources().getString(R.string.url_webservice) + "/" + getResources().getString(R.string.test_answer_repository) + "/" + getResources().getString(R.string.save_test_answer_webservice_operation);
		} else if (serviceCode == HasAnsweredCorrectCallTask.HAS_ANSWERED_CORRECT_WEB_SERVICE) {
			return getResources().getString(R.string.url_webservice) + "/" + getResources().getString(R.string.test_answer_repository) + "/" + getResources().getString(R.string.has_answered_correct_webservice_operation);
		}
		return null;
	}
	
	@Override
	public void returnServiceResponse(int serviceCode, boolean resultOk) {
		if (serviceCode == HasAnsweredCorrectCallTask.HAS_ANSWERED_CORRECT_WEB_SERVICE) {
			if (resultOk) {
				this.ivTestQuestionDetailThumbnail.setImageDrawable(getResources().getDrawable(R.drawable.ic_correct));
				this.ivTestQuestionRelatedContentThumbnail.setVisibility(View.VISIBLE);
				this.btnSubmit.setEnabled(false);
				int index = this.testQuestion.getOptionList().indexOf(this.testQuestion.getCorrectOption());
				this.radioButtonList.get(index).setChecked(true);
			} else {
				//do nothing
			}
			this.hasAnsweredCorrectCallTask = null;
		} else if (serviceCode == SaveTestAnswerCallTask.SAVE_TEST_ANSWER_WEB_SERVICE) {
			if (!resultOk) {
				showToast(getResources().getString(R.string.error_unable_to_save_test_answer));
			}
			TestQuestionDetailActivity.this.saveTestAnswerCallTask = null;
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
		
		for (int i =0; i < learningObjectContentList.size(); i++) {
			learningObjectContent = (LearningObjectContent)learningObjectContentList.get(i);
			if (learningObjectContent.getId().equals(learningObjectContentId)) {
				relatedContent[0] = i;
				relatedContent[1] = learningObjectContent;
				
				return relatedContent;
			}
		}
		
		return null;
	}
	
	private void showToast(String msg) {
		this.toast.setText(msg);
		this.toast.cancel();
		this.toast.show();
	}
}
