package ca.dal.cs.dalooc.android.gui;

import java.util.HashMap;
import java.util.Map;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.text.InputType;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import ca.dal.cs.dalooc.android.R;
import ca.dal.cs.dalooc.android.gui.components.ConfirmDialog;
import ca.dal.cs.dalooc.android.gui.listener.OnConfirmDialogReturnListener;
import ca.dal.cs.dalooc.model.Course;
import ca.dal.cs.dalooc.model.LearningObject;
import ca.dal.cs.dalooc.model.User;

public class CourseEditActivity extends FragmentActivity implements OnConfirmDialogReturnListener {

	public static final int EDIT_LEARNING_OBJECT_REQUEST_CODE = 100;
	
	private static final int LAYOUT_VIEW = 0;
	private static final int NAME_VIEW = 1;
	private static final int OBJECT_ITEM = 2;
	
	private ImageView ivAddPrerequisite;
	private ImageView ivAddReference;
	private ImageView ivAddLearningObject;
	
	private Map<ImageView, View[]> prerequisitesLayoutMapping;
	private Map<ImageView, View[]> referencesLayoutMapping;
	private Map<View, Object[]> learningObjectLayoutMapping;

	private LinearLayout llPrerequesites;
	private LinearLayout llReferences;
	private LinearLayout llLearningObject;
	
	private EditText etName;
	private EditText etDescription;
	private EditText etInstructor;
	private EditText etCourseDetail;
	
	private ConfirmDialog confirmDialog;
	
	private Course course;
	
	private User user;

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
		this.learningObjectLayoutMapping = new HashMap<View, Object[]>();
		
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
				intent.putExtra(LoginActivity.ARG_USER, CourseEditActivity.this.user);
				intent.putExtra(CourseSectionFragment.ARG_COURSE, CourseEditActivity.this.course);
				intent.putExtra(LearningObjectSectionFragment.ARG_LEARNING_OBJECT_INDEX, -1);
				startActivityForResult(intent, CourseEditActivity.EDIT_LEARNING_OBJECT_REQUEST_CODE);
			}
			
		});
		
		Bundle extras = getIntent().getExtras();
		if (extras != null) {
			this.user = (User)extras.get(LoginActivity.ARG_USER);
			this.course = (Course)extras.get(CourseActivity.ARG_COURSE);
			if (this.course != null) {
				loadData();
				getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
			} else {
				this.course = new Course();
			}
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
			createLearningObjectEntry(learningObject);
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.course_edit, menu);
		
		return true;
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (requestCode == CourseEditActivity.EDIT_LEARNING_OBJECT_REQUEST_CODE) {
			if (resultCode == Activity.RESULT_OK) {
				LearningObject learningObject = (LearningObject)data.getExtras().get(LearningObjectSectionFragment.ARG_LEARNING_OBJECT);
				createLearningObjectEntry(learningObject);
			} else if (resultCode == Activity.RESULT_CANCELED) {
				//TODO see here what to do if edit was canceled
			}
		}
	}
	
	private void showConfirmDialog() {
        FragmentManager fm = getSupportFragmentManager();
        
        Bundle args = new Bundle();
        args.putString(ConfirmDialog.ARG_TITLE, getResources().getString(R.string.dialog_title_course));
        args.putString(ConfirmDialog.ARG_MESSAGE, getResources().getString(R.string.confirm_changes));
        
        confirmDialog = new ConfirmDialog();
        confirmDialog.setArguments(args);
        confirmDialog.setOnConfirmDialogResultListener(this);
        confirmDialog.show(fm, "fragment_edit_name");
    }

	@Override
	public void onConfirmDialogReturn(boolean confirm, int returnCode) {
		Intent resultIntent = new Intent();
		
		if (confirm) {
			//TODO update here the course object
		} else {
			setResult(Activity.RESULT_CANCELED, resultIntent);
		}
		
		this.confirmDialog.dismiss();
		finish();
	}
	
	@Override
	public void onBackPressed() {
		showConfirmDialog();
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
				View viewToRemove = CourseEditActivity.this.prerequisitesLayoutMapping.get(v)[LAYOUT_VIEW];
				((RelativeLayout)viewToRemove).removeAllViews();
				CourseEditActivity.this.llPrerequesites.removeViewInLayout(viewToRemove);
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
				View viewToRemove = (View)CourseEditActivity.this.referencesLayoutMapping.get(v)[LAYOUT_VIEW];
				((RelativeLayout)viewToRemove).removeAllViews();
				CourseEditActivity.this.llReferences.removeViewInLayout(viewToRemove);
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

	protected void createLearningObjectEntry(LearningObject learningObject) {
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
				intent.putExtra(LoginActivity.ARG_USER, CourseEditActivity.this.user);
				intent.putExtra(CourseSectionFragment.ARG_COURSE, CourseEditActivity.this.course);
				int index = CourseEditActivity.this.course.getLearningObjectList().indexOf((LearningObject)CourseEditActivity.this.learningObjectLayoutMapping.get((View)v.getParent())[OBJECT_ITEM]);
				intent.putExtra(LearningObjectSectionFragment.ARG_LEARNING_OBJECT_INDEX, index);
				startActivityForResult(intent, CourseEditActivity.EDIT_LEARNING_OBJECT_REQUEST_CODE);
			}
		});
		
		ImageView imageView = new ImageView(CourseEditActivity.this);
		imageView.setImageDrawable(CourseEditActivity.this.getResources().getDrawable(R.drawable.content_discard));
		imageView.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				View viewToRemove = (View)CourseEditActivity.this.learningObjectLayoutMapping.get((View)v.getParent())[LAYOUT_VIEW];
				((RelativeLayout)viewToRemove).removeAllViews();
				CourseEditActivity.this.llLearningObject.removeViewInLayout(viewToRemove);
				CourseEditActivity.this.learningObjectLayoutMapping.remove(viewToRemove);
			}
		});
		
		//EditText
		RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
		params.addRule(RelativeLayout.ALIGN_PARENT_LEFT, RelativeLayout.TRUE);
//		params.addRule(RelativeLayout.CENTER_VERTICAL, RelativeLayout.TRUE);
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
		
		CourseEditActivity.this.learningObjectLayoutMapping.put((View)imageView.getParent(), viewArray);
		
		LinearLayout.LayoutParams llParams = new LinearLayout.LayoutParams(
				ViewGroup.LayoutParams.MATCH_PARENT, 
				ViewGroup.LayoutParams.WRAP_CONTENT, 
				0);
		llParams.setMargins(0, 10, 0, 0);
		CourseEditActivity.this.llLearningObject.addView(relativeLayout, llParams);
	}
}
