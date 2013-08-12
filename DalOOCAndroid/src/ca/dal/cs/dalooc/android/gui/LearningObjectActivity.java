package ca.dal.cs.dalooc.android.gui;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ViewPager;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;
import ca.dal.cs.dalooc.android.R;
import ca.dal.cs.dalooc.android.control.LearningObjectSectionsPagerAdapter;
import ca.dal.cs.dalooc.android.gui.listener.OnWebServiceCallDoneListener;
import ca.dal.cs.dalooc.android.task.UpdateCourseCallTask;
import ca.dal.cs.dalooc.model.Audio;
import ca.dal.cs.dalooc.model.Course;
import ca.dal.cs.dalooc.model.Document;
import ca.dal.cs.dalooc.model.LearningObject;
import ca.dal.cs.dalooc.model.TestQuestion;
import ca.dal.cs.dalooc.model.User;
import ca.dal.cs.dalooc.model.Video;

public class LearningObjectActivity extends FragmentActivity implements OnWebServiceCallDoneListener {

	public static final int DETAIL_ACTIVITY_CALL = 200;

	public static boolean contentUpdated;
	
	private UpdateCourseCallTask updateCourseCallTask;
	
	private LearningObjectSectionsPagerAdapter mSectionsPagerAdapter;
	
	private User user;
	
	private LearningObject learningObject; 
	
	private static Course course;
	
	private int learningObjectIndex;

	private ViewPager mViewPager;
	
	private Toast toast;
	
	private Intent resultIntent;
	
//	private Toast toast;
	
//	@SuppressLint("HandlerLeak")
//	private Handler callBackHandler = new Handler() {
//		public void handleMessage(android.os.Message msg) {
//			//TODO implement what to do when persisting is done
//		}
//	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_learning_object);

		this.user = (User)getIntent().getExtras().getSerializable(LoginActivity.ARG_USER);
		LearningObjectActivity.setCourse((Course)getIntent().getExtras().getSerializable(CourseSectionFragment.ARG_COURSE));
		this.learningObjectIndex = getIntent().getIntExtra(LearningObjectSectionFragment.ARG_LEARNING_OBJECT_INDEX, -1);

		LearningObjectActivity.contentUpdated = false;
		
		loadData();
	}

	private void loadData() {
		this.learningObject = LearningObjectActivity.getCourse().getLearningObjectList().get(this.learningObjectIndex);

		setTitle(this.learningObject.getName());
		
		mSectionsPagerAdapter = new LearningObjectSectionsPagerAdapter(getSupportFragmentManager(), this);

		mViewPager = (ViewPager) findViewById(R.id.pager);
		mViewPager.setAdapter(mSectionsPagerAdapter);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		if (this.user.getUserType().equals(User.UserType.PROFESSOR)) {
			getMenuInflater().inflate(R.menu.learning_object, menu);
		}
		return true;
	}
	
	@Override
	public boolean onMenuItemSelected(int featureId, MenuItem item) {
		Intent intent;
		switch (item.getOrder()) {
		case 100:
			break;
			
		case 200:
			intent = new Intent(this, LearningObjectEditActivity.class);
			intent.putExtra(LoginActivity.ARG_USER, this.user);
			intent.putExtra(CourseActivity.ARG_COURSE, LearningObjectActivity.getCourse());
			intent.putExtra(LearningObjectSectionFragment.ARG_LEARNING_OBJECT_INDEX, this.learningObjectIndex);
			
			startActivityForResult(intent, CourseEditActivity.EDIT_LEARNING_OBJECT_REQUEST_CODE);
			break;

		case 300:
			intent = new Intent(this, VideoEditActivity.class);
			intent.putExtra(LoginActivity.ARG_USER, this.user);
			intent.putExtra(CourseActivity.ARG_COURSE, LearningObjectActivity.getCourse());
			intent.putExtra(LearningObjectSectionFragment.ARG_LEARNING_OBJECT_INDEX, this.learningObjectIndex);
			intent.putExtra(LearningObjectSectionFragment.ARG_VIDEO_INDEX, -1);
			
			startActivityForResult(intent, LearningObjectEditActivity.NEW_VIDEO_REQUEST_CODE);
			break;
			
		case 400:
			intent = new Intent(this, AudioEditActivity.class);
			intent.putExtra(LoginActivity.ARG_USER, this.user);
			intent.putExtra(CourseActivity.ARG_COURSE, LearningObjectActivity.getCourse());
			intent.putExtra(LearningObjectSectionFragment.ARG_LEARNING_OBJECT_INDEX, this.learningObjectIndex);
			intent.putExtra(LearningObjectSectionFragment.ARG_AUDIO_INDEX, -1);
			
			startActivityForResult(intent, LearningObjectEditActivity.NEW_AUDIO_REQUEST_CODE);
			break;
			
		case 500:
			intent = new Intent(this, DocumentEditActivity.class);
			intent.putExtra(LoginActivity.ARG_USER, this.user);
			intent.putExtra(CourseActivity.ARG_COURSE, LearningObjectActivity.getCourse());
			intent.putExtra(LearningObjectSectionFragment.ARG_LEARNING_OBJECT_INDEX, this.learningObjectIndex);
			intent.putExtra(LearningObjectSectionFragment.ARG_DOCUMENT_INDEX, -1);
			
			startActivityForResult(intent, LearningObjectEditActivity.NEW_DOCUMENT_REQUEST_CODE);
			break;
			
		case 600:
			intent = new Intent(this, TestQuestionEditActivity.class);
			intent.putExtra(LoginActivity.ARG_USER, this.user);
			intent.putExtra(CourseActivity.ARG_COURSE, LearningObjectActivity.getCourse());
			intent.putExtra(LearningObjectSectionFragment.ARG_LEARNING_OBJECT_INDEX, this.learningObjectIndex);
			intent.putExtra(LearningObjectSectionFragment.ARG_TEST_QUESTION_INDEX, -1);
			
			startActivityForResult(intent, LearningObjectEditActivity.NEW_TEST_QUESTION_REQUEST_CODE);
			break;
		}
		return super.onMenuItemSelected(featureId, item);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (requestCode == CourseEditActivity.EDIT_LEARNING_OBJECT_REQUEST_CODE) {
			if (resultCode == Activity.RESULT_OK) {
				LearningObjectActivity.getCourse().getLearningObjectList().set(this.learningObjectIndex, (LearningObject)data.getExtras().get(LearningObjectSectionFragment.ARG_LEARNING_OBJECT));
				fireUpdateCourseTask();
				loadData();
			} else if (resultCode == Activity.RESULT_CANCELED) {
				if (data.getExtras() != null) {
					if (CourseEditActivity.checkAndUpdateLearningObjectChilds(data, LearningObjectActivity.getCourse().getLearningObjectList().get(this.learningObjectIndex))) {
						loadData();
					}
				}
			}
		} else if (requestCode == LearningObjectEditActivity.NEW_VIDEO_REQUEST_CODE) {
			if (resultCode == Activity.RESULT_OK) {
				Video videoReturned = (Video)data.getExtras().get(LearningObjectSectionFragment.ARG_VIDEO);
				LearningObjectActivity.getCourse().getLearningObjectList().get(this.getLearningObjectIndex()).getVideoList().add(videoReturned);
				fireUpdateCourseTask(); 
				loadData();
			}  else if (resultCode == Activity.RESULT_CANCELED) {
				//do nothing
			}
		} else if (requestCode == LearningObjectEditActivity.NEW_AUDIO_REQUEST_CODE) { 
			if (resultCode == Activity.RESULT_OK) {
				Audio audioReturned = (Audio)data.getExtras().get(LearningObjectSectionFragment.ARG_AUDIO);
				LearningObjectActivity.getCourse().getLearningObjectList().get(this.getLearningObjectIndex()).getAudioList().add(audioReturned);
				fireUpdateCourseTask(); 
				loadData();
			}  else if (resultCode == Activity.RESULT_CANCELED) {
				//do nothing
			}
		} else if (requestCode == LearningObjectEditActivity.NEW_DOCUMENT_REQUEST_CODE) { 
			if (resultCode == Activity.RESULT_OK) {
				Document documentReturned = (Document)data.getExtras().get(LearningObjectSectionFragment.ARG_DOCUMENT);
				LearningObjectActivity.getCourse().getLearningObjectList().get(this.getLearningObjectIndex()).getDocumentList().add(documentReturned);
				fireUpdateCourseTask(); 
				loadData();
			}  else if (resultCode == Activity.RESULT_CANCELED) {
				//do nothing
			}
		} else if (requestCode == LearningObjectEditActivity.NEW_TEST_QUESTION_REQUEST_CODE) {
			if (resultCode == Activity.RESULT_OK) {
				TestQuestion testQuestionReturned = (TestQuestion)data.getExtras().get(LearningObjectSectionFragment.ARG_TEST_QUESTION);
				LearningObjectActivity.getCourse().getLearningObjectList().get(this.getLearningObjectIndex()).getTestQuestionList().add(testQuestionReturned);
				fireUpdateCourseTask(); 
				loadData();
			}  else if (resultCode == Activity.RESULT_CANCELED) {
				//do nothing
			}
//		} else if (requestCode == LearningObjectActivity.DETAIL_ACTIVITY_CALL) {
//			if (data != null) {
//				Bundle extras = data.getExtras();
//				if (extras != null) {
//					Course returnCourse = (Course)extras.get(CourseSectionFragment.ARG_COURSE);
//					if (returnCourse != null) {
//						LearningObjectActivity.setCourse(returnCourse);
//						LearningObjectActivity.contentUpdated = true;
//					}
//				}
//			}
		} 
	}

	@Override
	public void onBackPressed() {
		if (LearningObjectActivity.contentUpdated) {
			getResultIntent().putExtra(CourseActivity.ARG_COURSE, LearningObjectActivity.getCourse());
			setResult(CourseActivity.LEARNING_OBJECT_ACTIVITY_CALL, getResultIntent());
			LearningObjectActivity.contentUpdated = false;
		}
		finish();
	}
	
	@Override
	public String getUrlWebService(int serviceCode) {
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
	
	private void fireUpdateCourseTask() {
		this.updateCourseCallTask = new UpdateCourseCallTask(LearningObjectActivity.getCourse());
		this.updateCourseCallTask.setOnWebServiceCallDoneListener(this);
		this.updateCourseCallTask.execute(getUrlWebService(UpdateCourseCallTask.UPDATE_COURSE_WEB_SERVICE),
				getResources().getString(R.string.namespace_webservice),
				getResources().getString(R.string.update_course_webservice_operation));
	}
	
	public int getLearningObjectIndex() {
		return this.learningObjectIndex;
	}
	
	private Intent getResultIntent() {
		if (this.resultIntent == null) {
			this.resultIntent = new Intent();
		}
		return this.resultIntent;
	}
	
	private void showToast(String msg) {
		if (this.toast == null) {
			this.toast = Toast.makeText(this, "", Toast.LENGTH_LONG);
		}
		this.toast.setText(msg);
		this.toast.show();
	}
	
	public User getUser() {
		return this.user;
	}
	
	public static Course getCourse() {
		return LearningObjectActivity.course;
	}
	
	public static void setCourse(Course course) {
		LearningObjectActivity.course = course;
	}
}
