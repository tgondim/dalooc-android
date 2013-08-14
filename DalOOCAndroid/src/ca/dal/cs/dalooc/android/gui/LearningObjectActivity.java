package ca.dal.cs.dalooc.android.gui;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

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
		if (this.user.getId().equals(LearningObjectActivity.course.getOwnerId()) 
				&& this.user.getUserType().equals(User.UserType.PROFESSOR)) {
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
				LearningObjectActivity.contentUpdated = true;
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
				
				List<Video> videoList = LearningObjectActivity.getCourse().getLearningObjectList().get(this.getLearningObjectIndex()).getVideoList();
				videoList.add(videoReturned);
				
				Comparator<Video> videoComparator = new Comparator<Video>() {
					@Override
					public int compare(final Video object1, final Video object2) {
						return ((Integer)object1.getOrder()).compareTo(object2.getOrder());
					}
				};

				if (videoList.size() > 0) {
					Collections.sort(videoList, videoComparator);
				}
				
				LearningObjectActivity.contentUpdated = true;
				fireUpdateCourseTask(); 
				loadData();
			}  else if (resultCode == Activity.RESULT_CANCELED) {
				//do nothing
			}
		} else if (requestCode == LearningObjectEditActivity.NEW_AUDIO_REQUEST_CODE) { 
			if (resultCode == Activity.RESULT_OK) {
				Audio audioReturned = (Audio)data.getExtras().get(LearningObjectSectionFragment.ARG_AUDIO);
				
				List<Audio> audioList = LearningObjectActivity.getCourse().getLearningObjectList().get(this.getLearningObjectIndex()).getAudioList();
				audioList.add(audioReturned);
				
				Comparator<Audio> audioComparator = new Comparator<Audio>() {
					@Override
					public int compare(final Audio object1, final Audio object2) {
						return ((Integer)object1.getOrder()).compareTo(object2.getOrder());
					}
				};

				if (audioList.size() > 0) {
					Collections.sort(audioList, audioComparator);
				}
				
				LearningObjectActivity.contentUpdated = true;
				fireUpdateCourseTask(); 
				loadData();
			}  else if (resultCode == Activity.RESULT_CANCELED) {
				//do nothing
			}
		} else if (requestCode == LearningObjectEditActivity.NEW_DOCUMENT_REQUEST_CODE) { 
			if (resultCode == Activity.RESULT_OK) {
				Document documentReturned = (Document)data.getExtras().get(LearningObjectSectionFragment.ARG_DOCUMENT);
				
				List<Document> documentList = LearningObjectActivity.getCourse().getLearningObjectList().get(this.getLearningObjectIndex()).getDocumentList();
				documentList.add(documentReturned);
				
				Comparator<Document> documentComparator = new Comparator<Document>() {
					@Override
					public int compare(final Document object1, final Document object2) {
						return ((Integer)object1.getOrder()).compareTo(object2.getOrder());
					}
				};

				if (documentList.size() > 0) {
					Collections.sort(documentList, documentComparator);
				}
				
				LearningObjectActivity.contentUpdated = true;
				fireUpdateCourseTask(); 
				loadData();
			}  else if (resultCode == Activity.RESULT_CANCELED) {
				//do nothing
			}
		} else if (requestCode == LearningObjectEditActivity.NEW_TEST_QUESTION_REQUEST_CODE) {
			if (resultCode == Activity.RESULT_OK) {
				TestQuestion testQuestionReturned = (TestQuestion)data.getExtras().get(LearningObjectSectionFragment.ARG_TEST_QUESTION);
				
				List<TestQuestion> testQuestionList = LearningObjectActivity.getCourse().getLearningObjectList().get(this.getLearningObjectIndex()).getTestQuestionList();
				testQuestionList.add(testQuestionReturned);
				
				Comparator<TestQuestion> testQuestionComparator = new Comparator<TestQuestion>() {
					@Override
					public int compare(final TestQuestion object1, final TestQuestion object2) {
						return ((Integer)object1.getOrder()).compareTo(object2.getOrder());
					}
				};

				if (testQuestionList.size() > 0) {
					Collections.sort(testQuestionList, testQuestionComparator);
				}
				
				LearningObjectActivity.contentUpdated = true;
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
			CourseActivity.contentUpdated = true;
			getResultIntent().putExtra(TestQuestionDetailActivity.ARG_WAS_ANSWERED, true);
			setResult(CourseActivity.LEARNING_OBJECT_ACTIVITY_CALL, getResultIntent());
			LearningObjectActivity.contentUpdated = false;
		}
		finish();
	}
	
	private String getUrlWebService(int serviceCode) {
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
