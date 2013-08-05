package ca.dal.cs.dalooc.android.gui;

import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import ca.dal.cs.dalooc.android.R;
import ca.dal.cs.dalooc.android.control.LearningObjectAdapter;
import ca.dal.cs.dalooc.model.Course;
import ca.dal.cs.dalooc.model.LearningObject;
import ca.dal.cs.dalooc.model.User;

public class CourseSectionFragment extends Fragment { 

	public static final String ARG_SECTION_NUMBER = "section_number";
	
	public static final String ARG_COURSE =  "course";

	private int sectionNumber;
	
	private User user;

	private Course course;
	
	private LearningObjectAdapter learningObjectAdapter;
	
	private ListView listViewItem;

	public CourseSectionFragment() {
		super();
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		super.onCreateView(inflater, container, savedInstanceState);
		
		Resources resources = inflater.getContext().getResources();
		
		this.sectionNumber = getArguments().getInt(ARG_SECTION_NUMBER);
		this.user = (User)getArguments().getSerializable(LoginActivity.ARG_USER);
		this.course = (Course)getArguments().getSerializable(ARG_COURSE);
		
		View rootView = inflater.inflate(R.layout.fragment_course, container, false);
		
		LinearLayout llFragmentCourse = (LinearLayout)rootView.findViewById(R.id.llFragmentCourse);
		
		switch (this.sectionNumber) {
		case 1: 
			RelativeLayout rlSyllabus = new RelativeLayout(inflater.getContext());
			
			TextView txtInstructor = new TextView(inflater.getContext());
			txtInstructor.setId(1);
			txtInstructor.setTextAppearance(inflater.getContext(), android.R.style.TextAppearance_Medium);
			txtInstructor.setTypeface(null, Typeface.BOLD);
			txtInstructor.setText(resources.getString(R.string.instructor));
			
			RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
			params.addRule(RelativeLayout.ALIGN_PARENT_LEFT, RelativeLayout.TRUE);
			params.addRule(RelativeLayout.ALIGN_PARENT_TOP, RelativeLayout.TRUE);
			
			rlSyllabus.addView(txtInstructor, params);

			TextView txtInstructorName = new TextView(inflater.getContext());
			txtInstructorName.setId(2);
			txtInstructorName.setText(course.getSyllabus().getInstructor());
			
			params = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
			params.addRule(RelativeLayout.BELOW, txtInstructor.getId());
			params.setMargins(0, 5, 0, 0);
			
			rlSyllabus.addView(txtInstructorName, params);
			
			View line1 = new View(inflater.getContext());
			line1.setId(3);
			line1.setBackgroundColor(Color.rgb(51, 51, 51));
			
			params = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, 1);
			params.addRule(RelativeLayout.BELOW, txtInstructorName.getId());
			params.setMargins(0, 20, 0, 0);
			
			rlSyllabus.addView(line1, params);
			
			TextView txtCourseDetail = new TextView(inflater.getContext());
			txtCourseDetail.setId(4);
			txtCourseDetail.setTextAppearance(inflater.getContext(), android.R.style.TextAppearance_Medium);
			txtCourseDetail.setTypeface(null, Typeface.BOLD);
			txtCourseDetail.setText(resources.getString(R.string.course_detail));
			
			params = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
			params.addRule(RelativeLayout.BELOW, line1.getId());
			params.setMargins(0, 20, 0, 0);

			rlSyllabus.addView(txtCourseDetail, params);

			TextView txtCourseDetailText = new TextView(inflater.getContext());
			txtCourseDetailText.setId(5);
			txtCourseDetailText.setText(course.getSyllabus().getCourseDetail());
			
			params = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
			params.addRule(RelativeLayout.BELOW, txtCourseDetail.getId());
			params.setMargins(0, 5, 0, 0);
			
			rlSyllabus.addView(txtCourseDetailText, params);

			View line2 = new View(inflater.getContext());
			line2.setId(6);
			line2.setBackgroundColor(Color.rgb(51, 51, 51));
			
			params = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, 1);
			params.addRule(RelativeLayout.BELOW, txtCourseDetailText.getId());
			params.setMargins(0, 20, 0, 0);
			
			rlSyllabus.addView(line2, params);
			
			TextView txtReferences = new TextView(inflater.getContext());
			txtReferences.setId(7);
			txtReferences.setTextAppearance(inflater.getContext(), android.R.style.TextAppearance_Medium);
			txtReferences.setTypeface(null, Typeface.BOLD);
			txtReferences.setText(resources.getString(R.string.references));
			
			params = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
			params.addRule(RelativeLayout.BELOW, line2.getId());
			params.setMargins(0, 20, 0, 0);
			
			rlSyllabus.addView(txtReferences, params);

			TextView txtReferencesText = new TextView(inflater.getContext());
			txtReferencesText.setId(8);
			
			if (course.getSyllabus().getReferences().size() == 0) {
				txtReferencesText.setText(resources.getString(R.string.no_references));
			} else {
				StringBuffer sb = new StringBuffer();
				for (int i = 0; i < course.getSyllabus().getReferences().size(); i++) {
					sb.append(" - " + course.getSyllabus().getReferences().get(i) + ((i + 1) != course.getSyllabus().getReferences().size() ? ";\n" : "."));
				}
				txtReferencesText.setText(sb.toString());
			}
			
			params = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
			params.addRule(RelativeLayout.BELOW, txtReferences.getId());
			params.setMargins(0, 5, 0, 0);
			
			rlSyllabus.addView(txtReferencesText, params);
			
			View line3 = new View(inflater.getContext());
			line3.setId(9);
			line3.setBackgroundColor(Color.rgb(51, 51, 51));
			
			params = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, 1);
			params.addRule(RelativeLayout.BELOW, txtReferencesText.getId());
			params.setMargins(0, 20, 0, 0);
			
			rlSyllabus.addView(line3, params);
			
			TextView txtPrerequisites = new TextView(inflater.getContext());
			txtPrerequisites.setId(10);
			txtPrerequisites.setTextAppearance(inflater.getContext(), android.R.style.TextAppearance_Medium);
			txtPrerequisites.setTypeface(null, Typeface.BOLD);
			txtPrerequisites.setText(resources.getString(R.string.prerequisites));
			
			params = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
			params.addRule(RelativeLayout.BELOW, line3.getId());
			params.setMargins(0, 20, 0, 0);
			
			rlSyllabus.addView(txtPrerequisites, params);

			TextView txtPrerequisitesText = new TextView(inflater.getContext());
			txtPrerequisitesText.setId(11);
			
			if (course.getSyllabus().getPrerequisites().size() == 0) {
				txtPrerequisitesText.setText(resources.getString(R.string.no_prerequisites));
			} else {
				StringBuffer sb = new StringBuffer();
				for (int i = 0; i < course.getSyllabus().getPrerequisites().size(); i++) {
					sb.append(" -  " + course.getSyllabus().getPrerequisites().get(i) + ((i + 1) != course.getSyllabus().getPrerequisites().size() ? ";\n" : "."));
				}
				txtPrerequisitesText.setText(sb.toString());
			}
			
			params = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
			params.addRule(RelativeLayout.BELOW, txtPrerequisites.getId());
			params.setMargins(0, 5, 0, 0);
			
			rlSyllabus.addView(txtPrerequisitesText, params);
			
			ScrollView scrollView = new ScrollView(inflater.getContext());
			scrollView.addView(rlSyllabus);
			llFragmentCourse.addView(scrollView, new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
			
			break;
			
		case 2: 
			this.learningObjectAdapter = new LearningObjectAdapter(inflater);

			this.listViewItem = new ListView(inflater.getContext());
			
			llFragmentCourse.addView(this.listViewItem, new LinearLayout.LayoutParams(
					ViewGroup.LayoutParams.MATCH_PARENT, 
					ViewGroup.LayoutParams.MATCH_PARENT, 
					0));
			
			this.listViewItem.setDividerHeight(0);
			this.listViewItem.setAdapter(learningObjectAdapter);
			this.listViewItem.setOnItemClickListener(new OnItemClickListener() {
				@Override
				public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
					Intent learningObjectIntent;
					LearningObject learningObject = (LearningObject)CourseSectionFragment.this.listViewItem.getAdapter().getItem(position);
					
					if (learningObject != null) {
						learningObjectIntent = new Intent("LEARNING_OBJECT_ACTIVITY");
						learningObjectIntent.putExtra(LearningObjectSectionFragment.ARG_LEARNING_OBJECT_INDEX, position);
						learningObjectIntent.putExtra(LoginActivity.ARG_USER, CourseSectionFragment.this.user);
						learningObjectIntent.putExtra(CourseSectionFragment.ARG_COURSE, CourseSectionFragment.this.course);

						startActivity(learningObjectIntent);
					}
				}
			});

			learningObjectAdapter.setLearningObjectList(course.getLearningObjectList());
			
			learningObjectAdapter.notifyDataSetChanged();
			break;
			
		}
		return rootView;
	}
}
