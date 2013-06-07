package ca.dal.cs.dalooc.android.gui;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ViewPager;
import android.view.Menu;
import android.view.MenuItem;
import ca.dal.cs.android.dalooc.R;
import ca.dal.cs.dalooc.android.control.CourseSectionsPagerAdapter;
import ca.dal.cs.dalooc.model.Course;

public class CourseActivity extends FragmentActivity {

	public static final String ARG_COURSE =  "course";
	
	private CourseSectionsPagerAdapter mSectionsPagerAdapter;
	
	ViewPager mViewPager;
	
	private Course course;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_course);
		
		this.course = (Course)getIntent().getExtras().getSerializable(ARG_COURSE);
		
		setTitle(this.course.getName());
		
		mSectionsPagerAdapter = new CourseSectionsPagerAdapter(getSupportFragmentManager(), this);

		mViewPager = (ViewPager) findViewById(R.id.pager);
		mViewPager.setAdapter(mSectionsPagerAdapter);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.course, menu);
		return true;
	}

	@Override
	public boolean onMenuItemSelected(int featureId, MenuItem item) {
		switch (item.getOrder()) {
		case 100:
			break;
			
		case 200:
			Intent intent = new Intent(this, CourseEditActivity.class);
			intent.putExtra(CourseActivity.ARG_COURSE, this.course);
			startActivity(intent);
			break;

		case 300:
			break;
		}
		return super.onMenuItemSelected(featureId, item);
	}
	public Course getCourse() {
		return course;
	}
}
