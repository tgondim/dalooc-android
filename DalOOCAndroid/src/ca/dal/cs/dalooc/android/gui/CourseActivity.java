package ca.dal.cs.dalooc.android.gui;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.ViewPager;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;
import ca.dal.cs.dalooc.android.R;
import ca.dal.cs.dalooc.android.control.CourseSectionsPagerAdapter;
import ca.dal.cs.dalooc.android.gui.components.ConfirmDialog;
import ca.dal.cs.dalooc.android.gui.listener.OnConfirmDialogReturnListener;
import ca.dal.cs.dalooc.android.gui.listener.OnWebServiceCallDoneListener;
import ca.dal.cs.dalooc.android.task.RemoveCourseCallTask;
import ca.dal.cs.dalooc.android.task.UpdateCourseCallTask;
import ca.dal.cs.dalooc.model.Course;
import ca.dal.cs.dalooc.model.LearningObject;
import ca.dal.cs.dalooc.model.User;

public class CourseActivity extends FragmentActivity implements OnWebServiceCallDoneListener, OnConfirmDialogReturnListener {

	public static final String ARG_COURSE =  "course";
	
	public static final int EDIT_COURSE_REQUEST_CODE = 100;

	public static final int REMOVE_COURSE_REQUEST_CODE = 200;

	public static final int NEW_COURSE_REQUEST_CODE = 300;
	
	public static final int LEARNING_OBJECT_ACTIVITY_CALL = 400;
	
	public static final int ACTION_CONFIRM_COURSE_REMOVE = 500;
	
	private RemoveCourseCallTask removeCourseCallTask;
	
	private UpdateCourseCallTask updateCourseCallTask;
	
	private CourseSectionsPagerAdapter mSectionsPagerAdapter;
	
	private ViewPager mViewPager;
	
	private Toast toast;
	
	private ConfirmDialog confirmDialog;
	
	private User user;
	
	private static Course course;
	
	public static boolean contentUpdated;
	
	private Intent resultIntent;
	
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
			showConfirmDialog(getResources().getString(R.string.course_remove_confirm), ACTION_CONFIRM_COURSE_REMOVE);
			break;

		case 400:
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
					fireUpdateCourseTask();
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
					fireUpdateCourseTask();
					loadData();
				}
			} else if (resultCode == Activity.RESULT_CANCELED) {
				//do nothing
			}
		} 
	}

	private void fireUpdateCourseTask() {
		this.updateCourseCallTask = new UpdateCourseCallTask(CourseActivity.getCourse());
		this.updateCourseCallTask.setOnWebServiceCallDoneListener(this);
		this.updateCourseCallTask.execute(getUrlWebService(UpdateCourseCallTask.UPDATE_COURSE_WEB_SERVICE),
				getResources().getString(R.string.namespace_webservice),
				getResources().getString(R.string.update_course_webservice_operation));
	}
	
	@Override
	public String getUrlWebService(int serviceCode) {
		if (serviceCode == RemoveCourseCallTask.REMOVE_COURSE_WEB_SERVICE) {
			return getResources().getString(R.string.url_webservice) + "/" + getResources().getString(R.string.course_repository) + "/" + getResources().getString(R.string.remove_course_webservice_operation); 
		} else if (serviceCode == UpdateCourseCallTask.UPDATE_COURSE_WEB_SERVICE) {
			return getResources().getString(R.string.url_webservice) + "/" + getResources().getString(R.string.course_repository) + "/" + getResources().getString(R.string.update_course_webservice_operation); 
		}
		return null;
	}
	
	@Override
	public void returnServiceResponse(int serviceCode, boolean resultOk) {
		switch (serviceCode) {
		case RemoveCourseCallTask.REMOVE_COURSE_WEB_SERVICE:
			if (resultOk) {
				Intent intent = new Intent();
				intent.putExtra(MainActivity.ARG_REFRESH_COURSE_LIST, true);
				setResult(MainActivity.COURSE_ACTIVITY_CALL, intent);
				finish();
			} else {
				showToast(getResources().getString(R.string.error_unable_to_remove_course));
			}
			
			this.removeCourseCallTask = null;
			
			break;
			
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
	
	@Override
	public void onConfirmDialogReturn(boolean confirm, int returnCode) {
		this.confirmDialog.dismiss();
		switch (returnCode) {
		case ACTION_CONFIRM_COURSE_REMOVE:
			if (confirm) {
				
				this.removeCourseCallTask = new RemoveCourseCallTask();
				this.removeCourseCallTask.setOnWebServiceCallDoneListener(CourseActivity.this);
				this.removeCourseCallTask.execute(getUrlWebService(RemoveCourseCallTask.REMOVE_COURSE_WEB_SERVICE),
						getResources().getString(R.string.namespace_webservice),
						getResources().getString(R.string.remove_course_webservice_operation),
						CourseActivity.course.getId()); 
			} else {
				//do nothing
			}
			
			break;

		default:
			break;
		}
	}
	
	private Intent getResultIntent() {
		if (this.resultIntent == null) {
			this.resultIntent = new Intent();
		}
		return this.resultIntent;
	}
	
	private void showConfirmDialog(String message, int returnCode) {
        FragmentManager fm = getSupportFragmentManager();
        
        Bundle args = new Bundle();
        args.putString(ConfirmDialog.ARG_TITLE, getResources().getString(R.string.warning));
        args.putString(ConfirmDialog.ARG_MESSAGE, message);
        args.putBoolean(ConfirmDialog.ARG_CANCEL_BUTTON, false);
        args.putInt(ConfirmDialog.ARG_RETURN_CODE, returnCode);
        
        confirmDialog = new ConfirmDialog();
        confirmDialog.setArguments(args);
        confirmDialog.setOnConfirmDialogResultListener(this);
        confirmDialog.show(fm, "fragment_edit_name");
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
	
	private void showToast(String msg) {
		if (this.toast == null) {
			this.toast = Toast.makeText(this, "", Toast.LENGTH_LONG);
		}
		this.toast.setText(msg);
		this.toast.show();
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
