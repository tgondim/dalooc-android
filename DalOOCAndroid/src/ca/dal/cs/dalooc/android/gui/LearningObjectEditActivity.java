package ca.dal.cs.dalooc.android.gui;


import java.util.HashMap;
import java.util.Map;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import ca.dal.cs.dalooc.android.R;
import ca.dal.cs.dalooc.android.gui.components.ConfirmDialog;
import ca.dal.cs.dalooc.android.gui.listener.OnConfirmDialogReturnListener;
import ca.dal.cs.dalooc.android.webservice.OnWebServiceCallDoneListener;
import ca.dal.cs.dalooc.android.webservice.UpdateCourseCallRunnable;
import ca.dal.cs.dalooc.model.Audio;
import ca.dal.cs.dalooc.model.Course;
import ca.dal.cs.dalooc.model.Document;
import ca.dal.cs.dalooc.model.LearningObject;
import ca.dal.cs.dalooc.model.TestQuestion;
import ca.dal.cs.dalooc.model.User;
import ca.dal.cs.dalooc.model.Video;

public class LearningObjectEditActivity extends FragmentActivity implements OnConfirmDialogReturnListener, OnWebServiceCallDoneListener {

	private static final int LAYOUT_VIEW = 0;
	private static final int NAME_VIEW = 1;
	private static final int OBJECT_ITEM = 2;

	public static final int EDIT_VIDEO_REQUEST_CODE = 200;
	public static final int EDIT_AUDIO_REQUEST_CODE = 300;
	public static final int EDIT_DOCUMENT_REQUEST_CODE = 400;
	public static final int EDIT_TEST_QUESTION_REQUEST_CODE = 500;
	public static final int NEW_VIDEO_REQUEST_CODE = 600;
	public static final int NEW_AUDIO_REQUEST_CODE = 700;
	public static final int NEW_DOCUMENT_REQUEST_CODE = 800;
	public static final int NEW_TEST_QUESTION_REQUEST_CODE = 900;
	
	private ImageView ivAddVideo;
	
	private ImageView ivAddAudio;
	
	private ImageView ivAddDocument;
	
	private ImageView ivAddTestQuestion;
	
	private LearningObject learningObject;
	
	private User user;
	
	private Course course;
	
	private int learningObjectIndex;
	
	private Map<View, Object[]> videosLayoutMapping;
	
	private Map<View, Object[]> audioLayoutMapping;
	
	private Map<View, Object[]> documentsLayoutMapping;
	
	private Map<View, Object[]> testQuestionsLayoutMapping;
	
	private ConfirmDialog confirmDialog;
	
	private EditText etName;
	
	private EditText etDescription;
	
	private EditText etOrder;
	
	private LinearLayout llVideos;

	private LinearLayout llAudio;
	
	private LinearLayout llDocuments;
	
	private LinearLayout llTestQuestions;
	
	private View lastClickedView;
	
	private Intent resultIntent;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_learning_object_edit);
		
		this.etName = (EditText)findViewById(R.id.etName);
		this.etDescription = (EditText)findViewById(R.id.etDescription);
		this.etOrder = (EditText)findViewById(R.id.etOrder);
		
		this.videosLayoutMapping = new HashMap<View, Object[]>();
		this.audioLayoutMapping = new HashMap<View, Object[]>();
		this.documentsLayoutMapping = new HashMap<View, Object[]>();
		this.testQuestionsLayoutMapping = new HashMap<View, Object[]>();
		
		this.llVideos = (LinearLayout)findViewById(R.id.llVideos);
		this.llAudio = (LinearLayout)findViewById(R.id.llAudio);
		this.llDocuments = (LinearLayout)findViewById(R.id.llDocuments);
		this.llTestQuestions = (LinearLayout)findViewById(R.id.llTestQuestions);
		
		this.ivAddVideo = (ImageView)findViewById(R.id.ivAddVideo);
		this.ivAddVideo.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(LearningObjectEditActivity.this, VideoEditActivity.class);
				intent.putExtra(LoginActivity.ARG_USER, LearningObjectEditActivity.this.user);
				intent.putExtra(CourseSectionFragment.ARG_COURSE, LearningObjectEditActivity.this.course);
				intent.putExtra(LearningObjectSectionFragment.ARG_LEARNING_OBJECT_INDEX, LearningObjectEditActivity.this.learningObjectIndex);
				intent.putExtra(LearningObjectSectionFragment.ARG_VIDEO_INDEX, -1);
				startActivityForResult(intent, LearningObjectEditActivity.NEW_VIDEO_REQUEST_CODE);
			}
			
		});
		
		this.ivAddAudio = (ImageView)findViewById(R.id.ivAddAudio);
		this.ivAddAudio.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(LearningObjectEditActivity.this, AudioEditActivity.class);
				intent.putExtra(LoginActivity.ARG_USER, LearningObjectEditActivity.this.user);
				intent.putExtra(CourseSectionFragment.ARG_COURSE, LearningObjectEditActivity.this.course);
				intent.putExtra(LearningObjectSectionFragment.ARG_LEARNING_OBJECT_INDEX, LearningObjectEditActivity.this.learningObjectIndex);
				intent.putExtra(LearningObjectSectionFragment.ARG_AUDIO_INDEX, -1);
				startActivityForResult(intent, LearningObjectEditActivity.NEW_AUDIO_REQUEST_CODE);
			}
			
		});
		
		this.ivAddDocument = (ImageView)findViewById(R.id.ivAddDocument);
		this.ivAddDocument.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(LearningObjectEditActivity.this, DocumentEditActivity.class);
				intent.putExtra(LoginActivity.ARG_USER, LearningObjectEditActivity.this.user);
				intent.putExtra(CourseSectionFragment.ARG_COURSE, LearningObjectEditActivity.this.course);
				intent.putExtra(LearningObjectSectionFragment.ARG_LEARNING_OBJECT_INDEX, LearningObjectEditActivity.this.learningObjectIndex);
				intent.putExtra(LearningObjectSectionFragment.ARG_DOCUMENT_INDEX, -1);
				startActivityForResult(intent, LearningObjectEditActivity.NEW_DOCUMENT_REQUEST_CODE);
			}
			
		});
		
		this.ivAddTestQuestion = (ImageView)findViewById(R.id.ivAddTestQuestion);
		this.ivAddTestQuestion.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(LearningObjectEditActivity.this, TestQuestionEditActivity.class);
				intent.putExtra(LoginActivity.ARG_USER, LearningObjectEditActivity.this.user);
				intent.putExtra(CourseSectionFragment.ARG_COURSE, LearningObjectEditActivity.this.course);
				intent.putExtra(LearningObjectSectionFragment.ARG_LEARNING_OBJECT_INDEX, LearningObjectEditActivity.this.learningObjectIndex);
				intent.putExtra(LearningObjectSectionFragment.ARG_TEST_QUESTION_INDEX, -1);
				startActivityForResult(intent, LearningObjectEditActivity.NEW_TEST_QUESTION_REQUEST_CODE);
			}
			
		});
		
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

			if (this.learningObjectIndex >= 0) {
				this.learningObject = this.course.getLearningObjectList().get(this.learningObjectIndex);
			}
			
			if (this.learningObject != null) {
				loadData();
				getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
			} else {
				this.learningObject = new LearningObject();
				
			}
		}
	}

	private void loadData() {
		this.etName.setText(this.learningObject.getName());
		this.etDescription.setText(this.learningObject.getDescription());
		this.etOrder.setText(String.valueOf(this.learningObject.getOrder()));
		
		for (Video video : this.learningObject.getVideoList()) {
			createVideoEntry(video);
		}

		for (Audio audio : this.learningObject.getAudioList()) {
			createAudioEntry(audio);
		}
		
		for (Document document : this.learningObject.getDocumentList()) {
			createDocumentEntry(document);
		}
		
		for (TestQuestion testQuestion : this.learningObject.getTestQuestionList()) {
			createTestQuestionEntry(testQuestion);
		}
	}

	private void fetchData() {
		this.learningObject.setName(this.etName.getText().toString());
		this.learningObject.setDescription(this.etDescription.getText().toString());
		this.learningObject.setOrder(Integer.valueOf(this.etOrder.getText().toString()));
		//videos, audio, documents and test questions fetch is made when returning from respective edit activities
	}
	
	private void finishSaving() {
		fetchData();
		getResultIntent().putExtra(LearningObjectSectionFragment.ARG_LEARNING_OBJECT, LearningObjectEditActivity.this.learningObject);
		getResultIntent().putExtra(LearningObjectSectionFragment.ARG_LEARNING_OBJECT_INDEX, LearningObjectEditActivity.this.learningObjectIndex);
		setResult(Activity.RESULT_OK, resultIntent);
		finish();
	}

	private void finishWithoutSaving() {
		setResult(Activity.RESULT_CANCELED, getResultIntent());
		finish();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.learning_object_edit, menu);
		
		return true;
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (requestCode == LearningObjectEditActivity.EDIT_VIDEO_REQUEST_CODE) {
			if (resultCode == Activity.RESULT_OK) {
				Video returnedVideo = (Video)data.getExtras().get(LearningObjectSectionFragment.ARG_VIDEO);
				int videoIndex = (Integer)data.getExtras().get(LearningObjectSectionFragment.ARG_VIDEO_INDEX);
				
				this.learningObject.getVideoList().set(videoIndex, returnedVideo);
				
				Object[] viewArray = LearningObjectEditActivity.this.videosLayoutMapping.get(LearningObjectEditActivity.this.lastClickedView);
				((TextView)viewArray[NAME_VIEW]).setText(returnedVideo.getName());
				viewArray[OBJECT_ITEM] = returnedVideo;
				
				LearningObjectEditActivity.this.lastClickedView = null;
				
				getResultIntent().putExtra(LearningObjectSectionFragment.ARG_VIDEO, returnedVideo);
				getResultIntent().putExtra(LearningObjectSectionFragment.ARG_VIDEO_INDEX, videoIndex);
				
				fireUpdateCourseThread();
			} else if (resultCode == Activity.RESULT_CANCELED) {
				//do nothing
			}
		}else if (requestCode == LearningObjectEditActivity.EDIT_AUDIO_REQUEST_CODE) {
			if (resultCode == Activity.RESULT_OK) {
				Audio returnedAudio = (Audio)data.getExtras().get(LearningObjectSectionFragment.ARG_AUDIO);
				int audioIndex = (Integer)data.getExtras().get(LearningObjectSectionFragment.ARG_AUDIO_INDEX);
				
				this.learningObject.getAudioList().set(audioIndex, returnedAudio);
				Object[] viewArray = LearningObjectEditActivity.this.audioLayoutMapping.get(LearningObjectEditActivity.this.lastClickedView);
				((TextView)viewArray[NAME_VIEW]).setText(returnedAudio.getName());
				viewArray[OBJECT_ITEM] = returnedAudio;
				
				LearningObjectEditActivity.this.lastClickedView = null;
				
				getResultIntent().putExtra(LearningObjectSectionFragment.ARG_AUDIO, returnedAudio);
				getResultIntent().putExtra(LearningObjectSectionFragment.ARG_AUDIO_INDEX, audioIndex);
				
				fireUpdateCourseThread();
			} else if (resultCode == Activity.RESULT_CANCELED) {
				//do nothing
			}
		} else if (requestCode == LearningObjectEditActivity.EDIT_DOCUMENT_REQUEST_CODE) {
			if (resultCode == Activity.RESULT_OK) {
				Document returnedDocument = (Document)data.getExtras().get(LearningObjectSectionFragment.ARG_DOCUMENT);
				int documentIndex = (Integer)data.getExtras().get(LearningObjectSectionFragment.ARG_DOCUMENT_INDEX);
				
				this.learningObject.getDocumentList().set(documentIndex, returnedDocument);
				Object[] viewArray = LearningObjectEditActivity.this.documentsLayoutMapping.get(LearningObjectEditActivity.this.lastClickedView);
				((TextView)viewArray[NAME_VIEW]).setText(returnedDocument.getName());
				viewArray[OBJECT_ITEM] = returnedDocument;
				
				LearningObjectEditActivity.this.lastClickedView = null;
				
				getResultIntent().putExtra(LearningObjectSectionFragment.ARG_DOCUMENT, returnedDocument);
				getResultIntent().putExtra(LearningObjectSectionFragment.ARG_DOCUMENT_INDEX, documentIndex);
				
				fireUpdateCourseThread();
			} else if (resultCode == Activity.RESULT_CANCELED) {
				//do nothing
			}
		} else if (requestCode == LearningObjectEditActivity.EDIT_TEST_QUESTION_REQUEST_CODE) {
			if (resultCode == Activity.RESULT_OK) {
				TestQuestion returnedTestQuestion = (TestQuestion)data.getExtras().get(LearningObjectSectionFragment.ARG_TEST_QUESTION);
				int testQuestionIndex = (Integer)data.getExtras().get(LearningObjectSectionFragment.ARG_TEST_QUESTION_INDEX);
				
				this.learningObject.getTestQuestionList().set(testQuestionIndex, returnedTestQuestion);
				Object[] viewArray = LearningObjectEditActivity.this.testQuestionsLayoutMapping.get(LearningObjectEditActivity.this.lastClickedView);
				((TextView)viewArray[NAME_VIEW]).setText(returnedTestQuestion.getQuestion());
				viewArray[OBJECT_ITEM] = returnedTestQuestion;
				
				LearningObjectEditActivity.this.lastClickedView = null;
				
				getResultIntent().putExtra(LearningObjectSectionFragment.ARG_TEST_QUESTION, returnedTestQuestion);
				getResultIntent().putExtra(LearningObjectSectionFragment.ARG_TEST_QUESTION_INDEX, testQuestionIndex);
				
				fireUpdateCourseThread();
			} else if (resultCode == Activity.RESULT_CANCELED) {
				//do nothing
			}
		} else if (requestCode == LearningObjectEditActivity.NEW_VIDEO_REQUEST_CODE) {
			if (resultCode == Activity.RESULT_OK) {
				Video video = (Video)data.getExtras().get(LearningObjectSectionFragment.ARG_VIDEO);
				createVideoEntry(video);
				this.learningObject.getVideoList().add(video);
				
				getResultIntent().putExtra(LearningObjectSectionFragment.ARG_VIDEO, video);
				getResultIntent().putExtra(LearningObjectSectionFragment.ARG_VIDEO_INDEX, -1);
				
				fireUpdateCourseThread();
			} else if (resultCode == Activity.RESULT_CANCELED) {
				//do nothing
			}
		}else if (requestCode == LearningObjectEditActivity.NEW_AUDIO_REQUEST_CODE) {
			if (resultCode == Activity.RESULT_OK) {
				Audio audio = (Audio)data.getExtras().get(LearningObjectSectionFragment.ARG_AUDIO);
				createAudioEntry(audio);
				this.learningObject.getAudioList().add(audio);
				
				getResultIntent().putExtra(LearningObjectSectionFragment.ARG_AUDIO, audio);
				getResultIntent().putExtra(LearningObjectSectionFragment.ARG_AUDIO_INDEX, -1);
				
				fireUpdateCourseThread();
			} else if (resultCode == Activity.RESULT_CANCELED) {
				//do nothing
			}
		} else if (requestCode == LearningObjectEditActivity.NEW_DOCUMENT_REQUEST_CODE) {
			if (resultCode == Activity.RESULT_OK) {
				Document document = (Document)data.getExtras().get(LearningObjectSectionFragment.ARG_DOCUMENT);
				createDocumentEntry(document);
				this.learningObject.getDocumentList().add(document);

				getResultIntent().putExtra(LearningObjectSectionFragment.ARG_DOCUMENT, document);
				getResultIntent().putExtra(LearningObjectSectionFragment.ARG_DOCUMENT_INDEX, -1);
				
				fireUpdateCourseThread();
			} else if (resultCode == Activity.RESULT_CANCELED) {
				//do nothing
			}
		} else if (requestCode == LearningObjectEditActivity.NEW_TEST_QUESTION_REQUEST_CODE) {
			if (resultCode == Activity.RESULT_OK) {
				TestQuestion testQuestion = (TestQuestion)data.getExtras().get(LearningObjectSectionFragment.ARG_TEST_QUESTION);
				createTestQuestionEntry(testQuestion);
				this.learningObject.getTestQuestionList().add(testQuestion);
				
				getResultIntent().putExtra(LearningObjectSectionFragment.ARG_TEST_QUESTION, testQuestion);
				getResultIntent().putExtra(LearningObjectSectionFragment.ARG_TEST_QUESTION_INDEX, -1);
				
				fireUpdateCourseThread();
			} else if (resultCode == Activity.RESULT_CANCELED) {
				//do nothing
			}
		}
	}
	
	@Override
	public String getUrlWebService(int serviceCode) {
		if (serviceCode == UpdateCourseCallRunnable.UPDATE_COURSE_WEB_SERVICE) {
			return getResources().getString(R.string.url_webservice) + "/" + getResources().getString(R.string.course_repository) + "/" + getResources().getString(R.string.update_course_webservice_operation);
		}
		return null;
	}
	
	@Override
	public void returnServiceResponse(int serviceCode, boolean resultOk) {
//		callBackHandler.sendEmptyMessage(0);	
		//TODO implement webservice response treatment
	}
	
	private void fireUpdateCourseThread() {
		UpdateCourseCallRunnable updateCourseCall = new UpdateCourseCallRunnable(this.course, this);
		updateCourseCall.setOnWebServiceCallDoneListener(this);
		new Thread(updateCourseCall).start();
	}
	
	private Intent getResultIntent() {
		if (this.resultIntent == null) {
			this.resultIntent = new Intent();
		}
		return this.resultIntent;
	}
	
	private void showConfirmDialog() {
        FragmentManager fm = getSupportFragmentManager();
        
        Bundle args = new Bundle();
        args.putString(ConfirmDialog.ARG_TITLE, getResources().getString(R.string.dialog_title_learning_object));
        args.putString(ConfirmDialog.ARG_MESSAGE, getResources().getString(R.string.confirm_changes));
        
        confirmDialog = new ConfirmDialog();
        confirmDialog.setArguments(args);
        confirmDialog.setOnConfirmDialogResultListener(this);
        confirmDialog.show(fm, "fragment_edit_name");
    }

	@Override
	public void onConfirmDialogReturn(boolean confirm, int returnCode) {
		this.confirmDialog.dismiss();
		
		if (confirm) {
			finishSaving();
		} else {
			finishWithoutSaving();
		}
	}
	
	@Override
	public void onBackPressed() {
//		fetchData();
		showConfirmDialog();
	}

	private void createVideoEntry(Video video) {
		RelativeLayout relativeLayout = new RelativeLayout(LearningObjectEditActivity.this);
		
		TextView textView = new TextView(LearningObjectEditActivity.this);
		textView.setTextAppearance(this, android.R.style.TextAppearance_Medium);
		
		if (video != null) {
			textView.setText(video.getName());
		}
		
		textView.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				LearningObjectEditActivity.this.lastClickedView = (View)v.getParent();
				Intent intent = new Intent(LearningObjectEditActivity.this, VideoEditActivity.class);
				intent.putExtra(LoginActivity.ARG_USER, LearningObjectEditActivity.this.user);
				intent.putExtra(CourseSectionFragment.ARG_COURSE, LearningObjectEditActivity.this.course);
				intent.putExtra(LearningObjectSectionFragment.ARG_LEARNING_OBJECT_INDEX, LearningObjectEditActivity.this.learningObjectIndex);
				int videoIndex = LearningObjectEditActivity.this.learningObject.getVideoList()
						.indexOf(LearningObjectEditActivity.this.videosLayoutMapping.get((View)v.getParent())[OBJECT_ITEM]);
				intent.putExtra(LearningObjectSectionFragment.ARG_VIDEO_INDEX, videoIndex);
				startActivityForResult(intent, LearningObjectEditActivity.EDIT_VIDEO_REQUEST_CODE);
			}
		});
		
		ImageView imageView = new ImageView(LearningObjectEditActivity.this);
		imageView.setImageDrawable(LearningObjectEditActivity.this.getResources().getDrawable(R.drawable.content_discard));
		imageView.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				View viewToRemove = (View)LearningObjectEditActivity.this.videosLayoutMapping.get((View)v.getParent())[LAYOUT_VIEW];
				Video videoToRemove = (Video)LearningObjectEditActivity.this.videosLayoutMapping.get((View)v.getParent())[OBJECT_ITEM];
				((RelativeLayout)viewToRemove).removeAllViews();
				LearningObjectEditActivity.this.llVideos.removeViewInLayout(viewToRemove);
				LearningObjectEditActivity.this.videosLayoutMapping.remove(viewToRemove);
				LearningObjectEditActivity.this.learningObject.getVideoList().remove(videoToRemove);
			}
		});
		
		//TextView
		RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
		params.addRule(RelativeLayout.ALIGN_PARENT_LEFT, RelativeLayout.TRUE);
//		params.addRule(RelativeLayout.CENTER_VERTICAL, RelativeLayout.TRUE);
		params.addRule(RelativeLayout.ALIGN_RIGHT, imageView.getId());
		params.setMargins(0, 0, 50, 0);
		
		relativeLayout.addView(textView, params);
		
		//ImageView
		params = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
		params.addRule(RelativeLayout.ALIGN_PARENT_RIGHT, RelativeLayout.TRUE);
		params.addRule(RelativeLayout.ALIGN_PARENT_TOP, RelativeLayout.TRUE);
		
		relativeLayout.addView(imageView, params);
		
		Object[] viewArray = new Object[3];
		viewArray[LAYOUT_VIEW] = relativeLayout;
		viewArray[NAME_VIEW] = textView;
		viewArray[OBJECT_ITEM] = video;
		
		LearningObjectEditActivity.this.videosLayoutMapping.put((View)imageView.getParent(), viewArray);
		
		LinearLayout.LayoutParams llParams = new LinearLayout.LayoutParams(
				ViewGroup.LayoutParams.MATCH_PARENT, 
				ViewGroup.LayoutParams.WRAP_CONTENT, 
				0);
		llParams.setMargins(0, 10, 0, 0);
		LearningObjectEditActivity.this.llVideos.addView(relativeLayout, llParams);
	}
	
	private void createAudioEntry(Audio audio) {
		RelativeLayout relativeLayout = new RelativeLayout(LearningObjectEditActivity.this);
		
		TextView textView = new TextView(LearningObjectEditActivity.this);
		textView.setTextAppearance(this, android.R.style.TextAppearance_Medium);

		if (audio != null) {
			textView.setText(audio.getName());
		}
		
		textView.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				LearningObjectEditActivity.this.lastClickedView = (View)v.getParent();
				Intent intent = new Intent(LearningObjectEditActivity.this, AudioEditActivity.class);
				intent.putExtra(LoginActivity.ARG_USER, LearningObjectEditActivity.this.user);
				intent.putExtra(CourseSectionFragment.ARG_COURSE, LearningObjectEditActivity.this.course);
				intent.putExtra(LearningObjectSectionFragment.ARG_LEARNING_OBJECT_INDEX, LearningObjectEditActivity.this.learningObjectIndex);
				int audioIndex = LearningObjectEditActivity.this.learningObject.getAudioList()
						.indexOf(LearningObjectEditActivity.this.audioLayoutMapping.get((View)v.getParent())[OBJECT_ITEM]);
				intent.putExtra(LearningObjectSectionFragment.ARG_AUDIO_INDEX, audioIndex);
				startActivityForResult(intent, LearningObjectEditActivity.EDIT_AUDIO_REQUEST_CODE);
			}
		});
		
		ImageView imageView = new ImageView(LearningObjectEditActivity.this);
		imageView.setImageDrawable(LearningObjectEditActivity.this.getResources().getDrawable(R.drawable.content_discard));
		imageView.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				View viewToRemove = (View)LearningObjectEditActivity.this.audioLayoutMapping.get((View)v.getParent())[LAYOUT_VIEW];
				Audio audioToRemove = (Audio)LearningObjectEditActivity.this.audioLayoutMapping.get((View)v.getParent())[OBJECT_ITEM];
				((RelativeLayout)viewToRemove).removeAllViews();
				LearningObjectEditActivity.this.llAudio.removeViewInLayout(viewToRemove);
				LearningObjectEditActivity.this.audioLayoutMapping.remove(viewToRemove);
				LearningObjectEditActivity.this.learningObject.getAudioList().remove(audioToRemove);
			}
		});
		
		//TextView
		RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
		params.addRule(RelativeLayout.ALIGN_PARENT_LEFT, RelativeLayout.TRUE);
//		params.addRule(RelativeLayout.CENTER_VERTICAL, RelativeLayout.TRUE);
		params.addRule(RelativeLayout.ALIGN_RIGHT, imageView.getId());
		params.setMargins(0, 0, 50, 0);
		
		relativeLayout.addView(textView, params);
		
		//ImageView
		params = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
		params.addRule(RelativeLayout.ALIGN_PARENT_RIGHT, RelativeLayout.TRUE);
		params.addRule(RelativeLayout.ALIGN_PARENT_TOP, RelativeLayout.TRUE);
		
		relativeLayout.addView(imageView, params);
		
		Object[] viewArray = new Object[3];
		viewArray[LAYOUT_VIEW] = relativeLayout;
		viewArray[NAME_VIEW] = textView;
		viewArray[OBJECT_ITEM] = audio;
		
		LearningObjectEditActivity.this.audioLayoutMapping.put((View)imageView.getParent(), viewArray);
		
		LinearLayout.LayoutParams llParams = new LinearLayout.LayoutParams(
				ViewGroup.LayoutParams.MATCH_PARENT, 
				ViewGroup.LayoutParams.WRAP_CONTENT, 
				0);
		llParams.setMargins(0, 10, 0, 0);
		LearningObjectEditActivity.this.llAudio.addView(relativeLayout, llParams);
	}

	private void createDocumentEntry(Document document) {
		RelativeLayout relativeLayout = new RelativeLayout(LearningObjectEditActivity.this);
		
		TextView textView = new TextView(LearningObjectEditActivity.this);
		textView.setTextAppearance(this, android.R.style.TextAppearance_Medium);

		if (document != null) {
			textView.setText(document.getName());
		}
		
		textView.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				LearningObjectEditActivity.this.lastClickedView = (View)v.getParent();
				Intent intent = new Intent(LearningObjectEditActivity.this, DocumentEditActivity.class);
				intent.putExtra(LoginActivity.ARG_USER, LearningObjectEditActivity.this.user);
				intent.putExtra(CourseSectionFragment.ARG_COURSE, LearningObjectEditActivity.this.course);
				intent.putExtra(LearningObjectSectionFragment.ARG_LEARNING_OBJECT_INDEX, LearningObjectEditActivity.this.learningObjectIndex);
				int documentIndex = LearningObjectEditActivity.this.learningObject.getDocumentList()
						.indexOf(LearningObjectEditActivity.this.documentsLayoutMapping.get((View)v.getParent())[OBJECT_ITEM]);
				intent.putExtra(LearningObjectSectionFragment.ARG_DOCUMENT_INDEX, documentIndex);
				startActivityForResult(intent, LearningObjectEditActivity.EDIT_DOCUMENT_REQUEST_CODE);
			}
		});
		
		ImageView imageView = new ImageView(LearningObjectEditActivity.this);
		imageView.setImageDrawable(LearningObjectEditActivity.this.getResources().getDrawable(R.drawable.content_discard));
		imageView.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				View viewToRemove = (View)LearningObjectEditActivity.this.documentsLayoutMapping.get((View)v.getParent())[LAYOUT_VIEW];
				Document documentToRemove = (Document)LearningObjectEditActivity.this.documentsLayoutMapping.get((View)v.getParent())[OBJECT_ITEM];
				((RelativeLayout)viewToRemove).removeAllViews();
				LearningObjectEditActivity.this.llDocuments.removeViewInLayout(viewToRemove);
				LearningObjectEditActivity.this.documentsLayoutMapping.remove(viewToRemove);
				LearningObjectEditActivity.this.learningObject.getDocumentList().remove(documentToRemove);
			}
		});
		
		//TextView
		RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
		params.addRule(RelativeLayout.ALIGN_PARENT_LEFT, RelativeLayout.TRUE);
//		params.addRule(RelativeLayout.CENTER_VERTICAL, RelativeLayout.TRUE);
		params.addRule(RelativeLayout.ALIGN_RIGHT, imageView.getId());
		params.setMargins(0, 0, 50, 0);
		
		relativeLayout.addView(textView, params);
		
		//ImageView
		params = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
		params.addRule(RelativeLayout.ALIGN_PARENT_RIGHT, RelativeLayout.TRUE);
		params.addRule(RelativeLayout.ALIGN_PARENT_TOP, RelativeLayout.TRUE);
		
		relativeLayout.addView(imageView, params);
		
		Object[] viewArray = new Object[3];
		viewArray[LAYOUT_VIEW] = relativeLayout;
		viewArray[NAME_VIEW] = textView;
		viewArray[OBJECT_ITEM] = document;
		
		LearningObjectEditActivity.this.documentsLayoutMapping.put((View)imageView.getParent(), viewArray);
		
		LinearLayout.LayoutParams llParams = new LinearLayout.LayoutParams(
				ViewGroup.LayoutParams.MATCH_PARENT, 
				ViewGroup.LayoutParams.WRAP_CONTENT, 
				0);
		llParams.setMargins(0, 10, 0, 0);
		LearningObjectEditActivity.this.llDocuments.addView(relativeLayout, llParams);
	}
	
	private void createTestQuestionEntry(TestQuestion testQuestion) {
		RelativeLayout relativeLayout = new RelativeLayout(LearningObjectEditActivity.this);
		
		TextView textView = new TextView(LearningObjectEditActivity.this);
		textView.setTextAppearance(this, android.R.style.TextAppearance_Medium);

		if (testQuestion != null) {
			textView.setText(testQuestion.getQuestion());
		}
		
		textView.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				LearningObjectEditActivity.this.lastClickedView = (View)v.getParent();
				Intent intent = new Intent(LearningObjectEditActivity.this, TestQuestionEditActivity.class);
				intent.putExtra(LoginActivity.ARG_USER, LearningObjectEditActivity.this.user);
				intent.putExtra(CourseSectionFragment.ARG_COURSE, LearningObjectEditActivity.this.course);
				intent.putExtra(LearningObjectSectionFragment.ARG_LEARNING_OBJECT_INDEX, LearningObjectEditActivity.this.learningObjectIndex);
				int testQuestionIndex = LearningObjectEditActivity.this.learningObject.getTestQuestionList()
						.indexOf(LearningObjectEditActivity.this.testQuestionsLayoutMapping.get((View)v.getParent())[OBJECT_ITEM]);
				intent.putExtra(LearningObjectSectionFragment.ARG_DOCUMENT_INDEX, testQuestionIndex);
				startActivityForResult(intent, LearningObjectEditActivity.EDIT_TEST_QUESTION_REQUEST_CODE);
			}
		});
		
		ImageView imageView = new ImageView(LearningObjectEditActivity.this);
		imageView.setImageDrawable(LearningObjectEditActivity.this.getResources().getDrawable(R.drawable.content_discard));
		imageView.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				View viewToRemove = (View)LearningObjectEditActivity.this.testQuestionsLayoutMapping.get((View)v.getParent())[LAYOUT_VIEW];
				TestQuestion testQuestionToRemove = (TestQuestion)LearningObjectEditActivity.this.testQuestionsLayoutMapping.get((View)v.getParent())[OBJECT_ITEM];
				((RelativeLayout)viewToRemove).removeAllViews();
				LearningObjectEditActivity.this.llTestQuestions.removeViewInLayout(viewToRemove);
				LearningObjectEditActivity.this.testQuestionsLayoutMapping.remove(viewToRemove);
				LearningObjectEditActivity.this.learningObject.getTestQuestionList().remove(testQuestionToRemove);
			}
		});
		
		//TextView
		RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
		params.addRule(RelativeLayout.ALIGN_PARENT_LEFT, RelativeLayout.TRUE);
//		params.addRule(RelativeLayout.CENTER_VERTICAL, RelativeLayout.TRUE);
		params.addRule(RelativeLayout.ALIGN_RIGHT, imageView.getId());
		params.setMargins(0, 0, 50, 0);
		
		relativeLayout.addView(textView, params);
		
		//ImageView
		params = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
		params.addRule(RelativeLayout.ALIGN_PARENT_RIGHT, RelativeLayout.TRUE);
		params.addRule(RelativeLayout.ALIGN_PARENT_TOP, RelativeLayout.TRUE);
		
		relativeLayout.addView(imageView, params);
		
		Object[] viewArray = new Object[3];
		viewArray[LAYOUT_VIEW] = relativeLayout;
		viewArray[NAME_VIEW] = textView;
		viewArray[OBJECT_ITEM] = testQuestion;
		
		LearningObjectEditActivity.this.testQuestionsLayoutMapping.put((View)imageView.getParent(), viewArray);
		
		LinearLayout.LayoutParams llParams = new LinearLayout.LayoutParams(
				ViewGroup.LayoutParams.MATCH_PARENT, 
				ViewGroup.LayoutParams.WRAP_CONTENT, 
				0);
		llParams.setMargins(0, 10, 0, 0);
		LearningObjectEditActivity.this.llTestQuestions.addView(relativeLayout, llParams);
	}
}
