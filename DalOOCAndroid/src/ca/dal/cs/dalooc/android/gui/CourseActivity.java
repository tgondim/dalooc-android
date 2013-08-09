package ca.dal.cs.dalooc.android.gui;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ViewPager;
import android.view.Menu;
import android.view.MenuItem;
import ca.dal.cs.dalooc.android.R;
import ca.dal.cs.dalooc.android.control.CourseSectionsPagerAdapter;
import ca.dal.cs.dalooc.android.webservices.OnUpdateCourseCallDoneListener;
import ca.dal.cs.dalooc.android.webservices.SaveCourseCallRunnable;
import ca.dal.cs.dalooc.android.webservices.UpdateCourseCallRunnable;
import ca.dal.cs.dalooc.model.Course;
import ca.dal.cs.dalooc.model.LearningObject;
import ca.dal.cs.dalooc.model.User;

public class CourseActivity extends FragmentActivity implements OnUpdateCourseCallDoneListener {

	public static final String ARG_COURSE =  "course";
	
	public static final int EDIT_COURSE_REQUEST_CODE = 100;

	public static final int NEW_COURSE_REQUEST_CODE = 200;
	
	public static final int LEARNING_OBJECT_ACTIVITY_CALL = 300;
	
	private CourseSectionsPagerAdapter mSectionsPagerAdapter;
	
	ViewPager mViewPager;
	
	private User user;
	
	private static Course course;
	
	public static boolean contentUpdated;
	
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
		setContentView(R.layout.activity_course);
		
		CourseActivity.setCourse((Course)getIntent().getExtras().getSerializable(ARG_COURSE));
		this.user = (User)getIntent().getExtras().getSerializable(LoginActivity.ARG_USER);
		
		loadData();
	}

	private void loadData() {
		setTitle(CourseActivity.getCourse().getName());
		
		mSectionsPagerAdapter = new CourseSectionsPagerAdapter(getSupportFragmentManager(), this);

		mViewPager = (ViewPager) findViewById(R.id.pager);
		mViewPager.setAdapter(mSectionsPagerAdapter);
		
		mSectionsPagerAdapter.notifyDataSetChanged();
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		if (this.user.getUserType().equals(User.UserType.PROFESSOR)) {
			getMenuInflater().inflate(R.menu.course, menu);
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
			intent = new Intent(this, CourseEditActivity.class);
			intent.putExtra(LoginActivity.ARG_USER, this.user);
			intent.putExtra(CourseActivity.ARG_COURSE, CourseActivity.getCourse());
			
			startActivityForResult(intent, EDIT_COURSE_REQUEST_CODE);
			break;

		case 300:
			intent = new Intent(this, LearningObjectEditActivity.class);
			intent.putExtra(LoginActivity.ARG_USER, this.user);
			intent.putExtra(CourseActivity.ARG_COURSE, CourseActivity.getCourse());
			intent.putExtra(LearningObjectSectionFragment.ARG_LEARNING_OBJECT_INDEX, -1);
			
			startActivityForResult(intent, CourseEditActivity.NEW_LEARNING_OBJECT_REQUEST_CODE);
			break;
		}
		return super.onMenuItemSelected(featureId, item);
	}
	
	@Override
	public void onBackPressed() {
		if (CourseActivity.contentUpdated) {
			getResultIntent().putExtra(CourseActivity.ARG_COURSE, LearningObjectActivity.getCourse());
			setResult(CourseActivity.LEARNING_OBJECT_ACTIVITY_CALL, getResultIntent());
			LearningObjectActivity.contentUpdated = false;
		}
		finish();
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (requestCode == EDIT_COURSE_REQUEST_CODE) {
			if (resultCode == Activity.RESULT_OK) {
				Bundle extras = data.getExtras();
				if (extras != null) {
					CourseActivity.setCourse((Course)extras.get(ARG_COURSE));
					fireUpdateCourseThread();
					loadData();
				}
			} else if (resultCode == Activity.RESULT_CANCELED) {
				if (data.getExtras() != null) {
					if (checkAndUpdateCourseChilds(data)) {
						loadData();
					}
				}
			}
		} else if (requestCode == CourseEditActivity.NEW_LEARNING_OBJECT_REQUEST_CODE) {
			if (resultCode == Activity.RESULT_OK) {
				Bundle extras = data.getExtras();
				if (extras != null) {
					LearningObject learningObject = (LearningObject)extras.get(LearningObjectSectionFragment.ARG_LEARNING_OBJECT);
					CourseActivity.getCourse().getLearningObjectList().add(learningObject);
					fireUpdateCourseThread();
					loadData();
				}
			} else if (resultCode == Activity.RESULT_CANCELED) {
				//do nothing
			}
		} 
	}

	private void fireUpdateCourseThread() {
		UpdateCourseCallRunnable updateCourseCall = new UpdateCourseCallRunnable(CourseActivity.getCourse(), this);
		updateCourseCall.setOnUpdateCourseCallDoneListener(this);
		new Thread(updateCourseCall).start();
	}
	
	@Override
	public String getUrlWebService(int serviceCode) {
		if (serviceCode == SaveCourseCallRunnable.SAVE_COURSE_WEB_SERVICE) {
			return getResources().getString(R.string.url_webservice) + "/" + getResources().getString(R.string.course_repository) + "/" + getResources().getString(R.string.save_course_webservice_operation); 
		} else if (serviceCode == UpdateCourseCallRunnable.UPDATE_COURSE_WEB_SERVICE) {
			return getResources().getString(R.string.url_webservice) + "/" + getResources().getString(R.string.course_repository) + "/" + getResources().getString(R.string.update_course_webservice_operation);
		}
		return null;
	}
	
	@Override
	public void returnServiceResponse(int serviceCode) {
//		callBackHandler.sendEmptyMessage(0);		
	}
	
	private Intent getResultIntent() {
		if (this.resultIntent == null) {
			this.resultIntent = new Intent();
		}
		return this.resultIntent;
	}
	
	public static Course getCourse() {
		return CourseActivity.course;
	}
	
	public static void setCourse(Course course) {
		CourseActivity.course = course;
	}
	
	public User getUser() {
		return this.user;
	}
	
	private boolean checkAndUpdateCourseChilds(Intent data) {
		int index;
		boolean anyUpdateMade = false;
		LearningObject learningObjectResult = (LearningObject)data.getExtras().get(LearningObjectSectionFragment.ARG_LEARNING_OBJECT);
		
		if (learningObjectResult != null) {
			index = (Integer)data.getExtras().get(LearningObjectSectionFragment.ARG_LEARNING_OBJECT_INDEX);
			if (index >= 0) {
				CourseActivity.getCourse().getLearningObjectList().set(index, learningObjectResult);
			} else {
				CourseActivity.getCourse().getLearningObjectList().add(learningObjectResult);
			}
			anyUpdateMade = true;
		}
		
		return anyUpdateMade;
	}	
}
