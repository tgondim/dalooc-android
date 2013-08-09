package ca.dal.cs.dalooc.android.control;

import java.util.Locale;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.app.FragmentTransaction;
import android.view.ViewGroup;
import ca.dal.cs.dalooc.android.R;
import ca.dal.cs.dalooc.android.gui.CourseActivity;
import ca.dal.cs.dalooc.android.gui.CourseSectionFragment;
import ca.dal.cs.dalooc.android.gui.LoginActivity;

public class CourseSectionsPagerAdapter extends FragmentStatePagerAdapter {

	private Context context;
	
	public CourseSectionsPagerAdapter(FragmentManager fm, Context context) {
		super(fm);
		this.context = context;
	}

	@Override
	public Fragment getItem(int position) {
		Fragment fragment = new CourseSectionFragment();
		
		Bundle args = new Bundle();		
		args.putInt(CourseSectionFragment.ARG_SECTION_NUMBER, position + 1);
		args.putSerializable(LoginActivity.ARG_USER, ((CourseActivity)this.context).getUser());
		args.putSerializable(CourseSectionFragment.ARG_COURSE, ((CourseActivity)this.context).getCourse());
		fragment.setArguments(args);
		
		return fragment;
	}

	@Override
	public int getCount() {
		// Show 2 total pages.
		return 2;
	}

	@Override
	public CharSequence getPageTitle(int position) {
		Locale l = Locale.getDefault();
		switch (position) {
		case 0:
			return this.context.getResources().getString(R.string.syllabus).toUpperCase(l);
		case 1:
			return this.context.getResources().getString(R.string.learning_objects).toUpperCase(l);
		}
		return null;
	}
}