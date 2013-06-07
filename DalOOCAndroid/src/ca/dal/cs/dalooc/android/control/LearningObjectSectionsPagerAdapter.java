package ca.dal.cs.dalooc.android.control;

import java.util.Locale;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import ca.dal.cs.android.dalooc.R;
import ca.dal.cs.dalooc.android.gui.LearningObjectActivity;
import ca.dal.cs.dalooc.android.gui.LearningObjectSectionFragment;

public class LearningObjectSectionsPagerAdapter extends FragmentPagerAdapter {

	private Context context;
	
	public LearningObjectSectionsPagerAdapter(FragmentManager fm, Context context) {
		super(fm);
		this.context = context;
	}

	@Override
	public Fragment getItem(int position) {
		Fragment fragment = new LearningObjectSectionFragment();
		
		Bundle args = new Bundle();		
		args.putInt(LearningObjectSectionFragment.ARG_SECTION_NUMBER, position + 1);
		args.putSerializable(LearningObjectSectionFragment.ARG_LEARNING_OBJECT, ((LearningObjectActivity)this.context).getLearningObject());
		fragment.setArguments(args);
		
		return fragment;
	}

	@Override
	public int getCount() {
		// Show 3 total pages.
		return 3;
	}

	@Override
	public CharSequence getPageTitle(int position) {
		Locale l = Locale.getDefault();
		switch (position) {
		case 0:
			return this.context.getResources().getString(R.string.videos).toUpperCase(l);
		case 1:
			return this.context.getResources().getString(R.string.audios).toUpperCase(l);
		case 2:
			return this.context.getResources().getString(R.string.documents).toUpperCase(l);
		}
		return null;
	}
}