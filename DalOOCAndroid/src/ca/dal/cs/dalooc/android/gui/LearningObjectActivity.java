package ca.dal.cs.dalooc.android.gui;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ViewPager;
import android.view.Menu;
import ca.dal.cs.android.dalooc.R;
import ca.dal.cs.dalooc.android.control.LearningObjectSectionsPagerAdapter;
import ca.dal.cs.dalooc.model.LearningObject;

public class LearningObjectActivity extends FragmentActivity {

	private LearningObjectSectionsPagerAdapter mSectionsPagerAdapter;
	
	private LearningObject learningObject; 

	ViewPager mViewPager;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_learning_object);

		this.learningObject = (LearningObject)getIntent().getExtras().getSerializable(LearningObjectSectionFragment.ARG_LEARNING_OBJECT);
		
		setTitle(this.learningObject.getName());
		
		mSectionsPagerAdapter = new LearningObjectSectionsPagerAdapter(getSupportFragmentManager(), this);

		mViewPager = (ViewPager) findViewById(R.id.pager);
		mViewPager.setAdapter(mSectionsPagerAdapter);

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.learning_object, menu);
		return true;
	}

	public LearningObject getLearningObject() {
		return this.learningObject;
	}
}
