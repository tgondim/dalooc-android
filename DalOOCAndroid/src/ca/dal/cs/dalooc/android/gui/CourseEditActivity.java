package ca.dal.cs.dalooc.android.gui;

import java.util.HashMap;
import java.util.Map;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import ca.dal.cs.android.dalooc.R;
import ca.dal.cs.dalooc.model.Course;
import ca.dal.cs.dalooc.model.LearningObject;

public class CourseEditActivity extends Activity {

	private static final int LAYOUT_VIEW = 0;
	private static final int NAME_VIEW = 1;
	private static final int OBJECT_ITEM = 2;
	
	private ImageView ivAddPrerequisite;
	private ImageView ivAddReference;
	private ImageView ivAddLearningObject;
	
	private Map<ImageView, View[]> prerequisitesLayoutMapping;
	private Map<ImageView, View[]> referencesLayoutMapping;
	private Map<ImageView, Object[]> learningObjectLayoutMapping;

	private LinearLayout llPrerequesites;
	private LinearLayout llReferences;
	private LinearLayout llLearningObject;
	
	private EditText etName;
	private EditText etDescription;
	private EditText etInstructor;
	private EditText etCourseDetail;
	
	private Course course;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_course_edit);
		
		this.etName = (EditText)findViewById(R.id.etName);
		this.etDescription = (EditText)findViewById(R.id.etDescription);
		this.etInstructor = (EditText)findViewById(R.id.etInstructor);
		this.etCourseDetail = (EditText)findViewById(R.id.etCourseDetail);
		
		this.prerequisitesLayoutMapping = new HashMap<ImageView, View[]>();
		this.referencesLayoutMapping = new HashMap<ImageView, View[]>();
		this.learningObjectLayoutMapping = new HashMap<ImageView, Object[]>();
		
		this.llPrerequesites = (LinearLayout)findViewById(R.id.llPrerequisites);
		this.llReferences = (LinearLayout)findViewById(R.id.llReferences);
		this.llLearningObject = (LinearLayout)findViewById(R.id.llLearningObject);
		
		this.ivAddPrerequisite = (ImageView)findViewById(R.id.ivAddPrerequisite);
		this.ivAddPrerequisite.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				CourseEditActivity.this.createPrerequisiteEntry();
			}
			
		});
		
		this.ivAddReference = (ImageView)findViewById(R.id.ivAddReference);
		this.ivAddReference.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				createReferenceEntry();
			}

		});
		
		this.ivAddLearningObject = (ImageView)findViewById(R.id.ivAddLearningObject);
		this.ivAddLearningObject.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(CourseEditActivity.this, LearningObjectEditActivity.class);
				startActivityForResult(intent, LearningObjectEditActivity.EDIT_LEARNING_OBJECT_REQUEST_CODE);
			}
			
		});
		
		Bundle extras = getIntent().getExtras();
		if (extras != null) {
			this.course = (Course)extras.get(CourseActivity.ARG_COURSE);
			loadData();
		} else {
			this.course = new Course();
		}
	}

	private void loadData() {
		this.etName.setText(this.course.getName());
		this.etDescription.setText(this.course.getDescription());
		this.etInstructor.setText(this.course.getSyllabus().getInstructor());
		this.etCourseDetail.setText(this.course.getSyllabus().getCourseDetail());
		
		for (String prerequisite : this.course.getSyllabus().getPrerequisites()) {
			View v = createPrerequisiteEntry();
			((EditText)this.prerequisitesLayoutMapping.get(v)[NAME_VIEW]).setText(prerequisite);
		}
		
		for (String reference : this.course.getSyllabus().getReferences()) {
			View v = createReferenceEntry();
			((EditText)this.referencesLayoutMapping.get(v)[NAME_VIEW]).setText(reference);
		}
		
		for (LearningObject learningObject : this.course.getLearningObjectList()) {
			View v = createLearningObjectEntry(learningObject);
//			((TextView)this.learningObjectLayoutMapping.get(v)[NAME_VIEW]).setText(learningObject.getName());
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.course_edit, menu);
		
		return true;
	}

	protected View createPrerequisiteEntry() {
		RelativeLayout relativeLayout = new RelativeLayout(CourseEditActivity.this);
		
		EditText editText = new EditText(CourseEditActivity.this);
		editText.setInputType(InputType.TYPE_TEXT_VARIATION_SHORT_MESSAGE);
		editText.setEms(16);
		
		ImageView imageView = new ImageView(CourseEditActivity.this);
		imageView.setImageDrawable(CourseEditActivity.this.getResources().getDrawable(R.drawable.content_discard));
		imageView.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				((RelativeLayout)CourseEditActivity.this.prerequisitesLayoutMapping.get(v)[LAYOUT_VIEW]).removeAllViews();
				CourseEditActivity.this.llPrerequesites.removeViewInLayout(CourseEditActivity.this.prerequisitesLayoutMapping.get(v)[LAYOUT_VIEW]);
				CourseEditActivity.this.prerequisitesLayoutMapping.remove(v);
			}
		});
		
		//EditText
		RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
		params.addRule(RelativeLayout.ALIGN_PARENT_LEFT, RelativeLayout.TRUE);
		params.addRule(RelativeLayout.CENTER_VERTICAL, RelativeLayout.TRUE);
		params.addRule(RelativeLayout.ALIGN_RIGHT, imageView.getId());
		
		relativeLayout.addView(editText, params);

		//ImageView
		params = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
		params.addRule(RelativeLayout.ALIGN_PARENT_RIGHT, RelativeLayout.TRUE);
		params.addRule(RelativeLayout.ALIGN_PARENT_TOP, RelativeLayout.TRUE);
		
		relativeLayout.addView(imageView, params);

		View[] viewArray = new View[2];
		viewArray[LAYOUT_VIEW] = relativeLayout;
		viewArray[NAME_VIEW] = editText;
		
		CourseEditActivity.this.prerequisitesLayoutMapping.put(imageView, viewArray);
		LinearLayout.LayoutParams llParams = new LinearLayout.LayoutParams(
				ViewGroup.LayoutParams.MATCH_PARENT, 
				ViewGroup.LayoutParams.WRAP_CONTENT, 
				0);
		llParams.setMargins(0, 10, 0, 0);
		CourseEditActivity.this.llPrerequesites.addView(relativeLayout, llParams);
		
		return imageView;
	}
	
	protected View createReferenceEntry() {
		RelativeLayout relativeLayout = new RelativeLayout(CourseEditActivity.this);
		
		EditText editText = new EditText(CourseEditActivity.this);
		editText.setInputType(InputType.TYPE_TEXT_VARIATION_SHORT_MESSAGE);
		editText.setEms(16);
		
		ImageView imageView = new ImageView(CourseEditActivity.this);
		imageView.setImageDrawable(CourseEditActivity.this.getResources().getDrawable(R.drawable.content_discard));
		imageView.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				((RelativeLayout)CourseEditActivity.this.referencesLayoutMapping.get(v)[LAYOUT_VIEW]).removeAllViews();
				CourseEditActivity.this.llReferences.removeViewInLayout(CourseEditActivity.this.referencesLayoutMapping.get(v)[LAYOUT_VIEW]);
				CourseEditActivity.this.referencesLayoutMapping.remove(v);
			}
		});
		
		//EditText
		RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
		params.addRule(RelativeLayout.ALIGN_PARENT_LEFT, RelativeLayout.TRUE);
		params.addRule(RelativeLayout.CENTER_VERTICAL, RelativeLayout.TRUE);
		params.addRule(RelativeLayout.ALIGN_RIGHT, imageView.getId());
		
		relativeLayout.addView(editText, params);
		
		//ImageView
		params = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
		params.addRule(RelativeLayout.ALIGN_PARENT_RIGHT, RelativeLayout.TRUE);
		params.addRule(RelativeLayout.ALIGN_PARENT_TOP, RelativeLayout.TRUE);
		
		relativeLayout.addView(imageView, params);
		
		View[] viewArray = new View[2];
		viewArray[LAYOUT_VIEW] = relativeLayout;
		viewArray[NAME_VIEW] = editText;
		
		CourseEditActivity.this.referencesLayoutMapping.put(imageView, viewArray);
		
		LinearLayout.LayoutParams llParams = new LinearLayout.LayoutParams(
				ViewGroup.LayoutParams.MATCH_PARENT, 
				ViewGroup.LayoutParams.WRAP_CONTENT, 
				0);
		llParams.setMargins(0, 10, 0, 0);
		CourseEditActivity.this.llReferences.addView(relativeLayout, llParams);
		
		return imageView;
	}

	protected View createLearningObjectEntry(LearningObject learningObject) {
		RelativeLayout relativeLayout = new RelativeLayout(CourseEditActivity.this);
		
		TextView textView = new TextView(CourseEditActivity.this);
		textView.setTextAppearance(this, android.R.style.TextAppearance_Medium);
		
		if (learningObject != null) {
			textView.setText(learningObject.getName());
		}
		
		textView.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(CourseEditActivity.this, LearningObjectEditActivity.class);
//				int pos = CourseEditActivity.this.learningObjectLayoutMapping.
				intent.putExtra(LearningObjectSectionFragment.ARG_LEARNING_OBJECT, (LearningObject)CourseEditActivity.this.learningObjectLayoutMapping.get(v)[OBJECT_ITEM]);
				startActivityForResult(intent, LearningObjectEditActivity.EDIT_LEARNING_OBJECT_REQUEST_CODE);
			}
		});
		
		ImageView imageView = new ImageView(CourseEditActivity.this);
		imageView.setImageDrawable(CourseEditActivity.this.getResources().getDrawable(R.drawable.content_discard));
		imageView.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				((RelativeLayout)CourseEditActivity.this.learningObjectLayoutMapping.get(v)[LAYOUT_VIEW]).removeAllViews();
				CourseEditActivity.this.llLearningObject.removeViewInLayout((View)CourseEditActivity.this.learningObjectLayoutMapping.get(v)[LAYOUT_VIEW]);
				CourseEditActivity.this.learningObjectLayoutMapping.remove(v);
			}
		});
		
		//EditText
		RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
		params.addRule(RelativeLayout.ALIGN_PARENT_LEFT, RelativeLayout.TRUE);
		params.addRule(RelativeLayout.CENTER_VERTICAL, RelativeLayout.TRUE);
		params.addRule(RelativeLayout.ALIGN_RIGHT, imageView.getId());
		
		relativeLayout.addView(textView, params);
		
		//ImageView
		params = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
		params.addRule(RelativeLayout.ALIGN_PARENT_RIGHT, RelativeLayout.TRUE);
		params.addRule(RelativeLayout.ALIGN_PARENT_TOP, RelativeLayout.TRUE);
		
		relativeLayout.addView(imageView, params);
		
		Object[] viewArray = new Object[3];
		viewArray[LAYOUT_VIEW] = relativeLayout;
		viewArray[NAME_VIEW] = textView;
		viewArray[OBJECT_ITEM] = learningObject;
		
		CourseEditActivity.this.learningObjectLayoutMapping.put(imageView, viewArray);
		
		LinearLayout.LayoutParams llParams = new LinearLayout.LayoutParams(
				ViewGroup.LayoutParams.MATCH_PARENT, 
				ViewGroup.LayoutParams.WRAP_CONTENT, 
				0);
		llParams.setMargins(0, 10, 0, 0);
		CourseEditActivity.this.llLearningObject.addView(relativeLayout, llParams);
		
		return imageView;
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (requestCode == LearningObjectEditActivity.EDIT_LEARNING_OBJECT_REQUEST_CODE) {
			if (resultCode == Activity.RESULT_OK) {
				LearningObject lo = (LearningObject)data.getExtras().get(LearningObjectSectionFragment.ARG_LEARNING_OBJECT);
				createLearningObjectEntry(lo);
			} else if (resultCode == Activity.RESULT_CANCELED) {
				//TODO see here what to do if edit was canceled
			}
		}
	}
}
