package ca.dal.cs.dalooc.android.gui;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ViewPager;
import android.view.Menu;
import android.view.MenuItem;
import ca.dal.cs.dalooc.android.R;
import ca.dal.cs.dalooc.android.control.LearningObjectSectionsPagerAdapter;
import ca.dal.cs.dalooc.model.Course;
import ca.dal.cs.dalooc.model.LearningObject;
import ca.dal.cs.dalooc.model.User;

public class LearningObjectActivity extends FragmentActivity {

	private LearningObjectSectionsPagerAdapter mSectionsPagerAdapter;
	
	private User user;
	
	private LearningObject learningObject; 
	
	private Course course;
	
	private int learningObjectIndex;

	ViewPager mViewPager;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_learning_object);

		this.user = (User)getIntent().getExtras().getSerializable(LoginActivity.ARG_USER);
		this.course = (Course)getIntent().getExtras().getSerializable(CourseSectionFragment.ARG_COURSE);
		this.learningObjectIndex = getIntent().getIntExtra(LearningObjectSectionFragment.ARG_LEARNING_OBJECT_INDEX, -1);
		
		this.learningObject = this.course.getLearningObjectList().get(this.learningObjectIndex);

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
			intent.putExtra(CourseActivity.ARG_COURSE, this.course);
			intent.putExtra(LearningObjectSectionFragment.ARG_LEARNING_OBJECT_INDEX, this.learningObjectIndex);
			
			startActivity(intent);
			break;

		case 300:
			intent = new Intent(this, VideoEditActivity.class);
			intent.putExtra(LoginActivity.ARG_USER, this.user);
			intent.putExtra(CourseActivity.ARG_COURSE, this.course);
			intent.putExtra(LearningObjectSectionFragment.ARG_LEARNING_OBJECT_INDEX, this.learningObjectIndex);
			intent.putExtra(LearningObjectSectionFragment.ARG_VIDEO_INDEX, -1);
			
			startActivity(intent);
			break;
			
		case 400:
			intent = new Intent(this, AudioEditActivity.class);
			intent.putExtra(LoginActivity.ARG_USER, this.user);
			intent.putExtra(CourseActivity.ARG_COURSE, this.course);
			intent.putExtra(LearningObjectSectionFragment.ARG_LEARNING_OBJECT_INDEX, this.learningObjectIndex);
			intent.putExtra(LearningObjectSectionFragment.ARG_AUDIO_INDEX, -1);
			
			startActivity(intent);
			break;
			
		case 500:
			intent = new Intent(this, DocumentEditActivity.class);
			intent.putExtra(LoginActivity.ARG_USER, this.user);
			intent.putExtra(CourseActivity.ARG_COURSE, this.course);
			intent.putExtra(LearningObjectSectionFragment.ARG_LEARNING_OBJECT_INDEX, this.learningObjectIndex);
			intent.putExtra(LearningObjectSectionFragment.ARG_DOCUMENT_INDEX, -1);
			
			startActivity(intent);
			break;
			
		case 600:
			intent = new Intent(this, TestQuestionEditActivity.class);
			intent.putExtra(LoginActivity.ARG_USER, this.user);
			intent.putExtra(CourseActivity.ARG_COURSE, this.course);
			intent.putExtra(LearningObjectSectionFragment.ARG_LEARNING_OBJECT_INDEX, this.learningObjectIndex);
			intent.putExtra(LearningObjectSectionFragment.ARG_TEST_QUESTION_INDEX, -1);
			
			startActivity(intent);
			break;
		}
		return super.onMenuItemSelected(featureId, item);
	}

	public int getLearningObjectIndex() {
		return this.learningObjectIndex;
	}

	public User getUser() {
		return this.user;
	}
	
	public Course getCourse() {
		return this.course;
	}
}
