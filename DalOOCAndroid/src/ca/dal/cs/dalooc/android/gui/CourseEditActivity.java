package ca.dal.cs.dalooc.android.gui;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.text.InputType;
import android.text.TextUtils;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
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
import ca.dal.cs.dalooc.model.Syllabus;
import ca.dal.cs.dalooc.model.User;

public class CourseEditActivity extends FragmentActivity implements OnConfirmDialogReturnListener {

	public static final int EDIT_LEARNING_OBJECT_REQUEST_CODE = 100;

	public static final int NEW_LEARNING_OBJECT_REQUEST_CODE = 200;
	
	private static final int LAYOUT_VIEW = 0;
	private static final int NAME_VIEW = 1;
	private static final int OBJECT_ITEM = 2;
	
	private ImageView ivAddPrerequisite;
	private ImageView ivAddReference;
	private ImageView ivAddLearningObject;
	
	private Map<ImageView, Object[]> prerequisitesLayoutMapping;
	private Map<ImageView, Object[]> referencesLayoutMapping;
	private Map<View, Object[]> learningObjectsLayoutMapping;

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
	
	private View lastClickedView;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_course_edit);
		
		this.etName = (EditText)findViewById(R.id.etName);
		this.etDescription = (EditText)findViewById(R.id.etDescription);
		this.etInstructor = (EditText)findViewById(R.id.etInstructor);
		this.etCourseDetail = (EditText)findViewById(R.id.etCourseDetail);
		
		this.prerequisitesLayoutMapping = new HashMap<ImageView, Object[]>();
		this.referencesLayoutMapping = new HashMap<ImageView, Object[]>();
		this.learningObjectsLayoutMapping = new HashMap<View, Object[]>();
		
		this.llPrerequesites = (LinearLayout)findViewById(R.id.llPrerequisites);
		this.llReferences = (LinearLayout)findViewById(R.id.llReferences);
		this.llLearningObject = (LinearLayout)findViewById(R.id.llLearningObject);
		
		this.ivAddPrerequisite = (ImageView)findViewById(R.id.ivAddPrerequisite);
		this.ivAddPrerequisite.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				createPrerequisiteEntry("");
			}
			
		});
		
		this.ivAddReference = (ImageView)findViewById(R.id.ivAddReference);
		this.ivAddReference.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				createReferenceEntry("");
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
				startActivityForResult(intent, CourseEditActivity.NEW_LEARNING_OBJECT_REQUEST_CODE);
			}
			
		});
		
		Button btnSave = (Button)findViewById(R.id.btnSave);
		btnSave.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				finishSaving();
			}
		});
		
		Button btnDiscard = (Button)findViewById(R.id.btnDiscard);
		btnDiscard.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				finishWithoutSaving();
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

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.course_edit, menu);
		
		return true;
	}
	
	private void loadData() {
		this.etName.setText(this.course.getName());
		this.etDescription.setText(this.course.getDescription());
		this.etInstructor.setText(this.course.getSyllabus().getInstructor());
		this.etCourseDetail.setText(this.course.getSyllabus().getCourseDetail());
		
		for (String prerequisite : this.course.getSyllabus().getPrerequisites()) {
			createPrerequisiteEntry(prerequisite);
//			((EditText)this.prerequisitesLayoutMapping.get(v)[NAME_VIEW]).setText(prerequisite);
		}
		
		for (String reference : this.course.getSyllabus().getReferences()) {
			createReferenceEntry(reference);
//			((EditText)this.referencesLayoutMapping.get(v)[NAME_VIEW]).setText(reference);
		}
		
		for (LearningObject learningObject : this.course.getLearningObjectList()) {
			createLearningObjectEntry(learningObject);
		}
	}
	
	private void fetchData() {
		this.course.setName(this.etName.getText().toString());
		this.course.setDescription(this.etDescription.getText().toString());
		this.course.setName(this.etName.getText().toString());
		
		if (this.course.getSyllabus() == null) {
			this.course.setSyllabus(new Syllabus());
		}
		
		this.course.getSyllabus().setInstructor(this.etInstructor.getText().toString());
		this.course.getSyllabus().setCourseDetail(this.etCourseDetail.getText().toString());
		
		Iterator<ImageView> iterator = this.prerequisitesLayoutMapping.keySet().iterator();
		this.course.getSyllabus().getPrerequisites().clear();
		
		while (iterator.hasNext()) {
			Object[] object = this.prerequisitesLayoutMapping.get(iterator.next());
			this.course.getSyllabus().getPrerequisites().add(((EditText)object[NAME_VIEW]).getText().toString());
		}

		iterator = this.referencesLayoutMapping.keySet().iterator();
		this.course.getSyllabus().getReferences().clear();
		
		while (iterator.hasNext()) {
			Object[] object = this.referencesLayoutMapping.get(iterator.next());
			this.course.getSyllabus().getReferences().add(((EditText)object[NAME_VIEW]).getText().toString());
		}
		//the learningObjects fetch is made when returning from edit activity
	}
	
	private void finishSaving() {
		fetchData();
		Intent resultIntent = new Intent();
		resultIntent.putExtra(CourseActivity.ARG_COURSE, CourseEditActivity.this.course);
//		resultIntent.putExtra(LearningObjectSectionFragment.ARG_LEARNING_OBJECT_INDEX, CourseEditActivity.this.learningObjectIndex);
		setResult(Activity.RESULT_OK, resultIntent);
		finish();
	}

	private void finishWithoutSaving() {
		setResult(Activity.RESULT_CANCELED, new Intent());
		finish();
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (requestCode == CourseEditActivity.EDIT_LEARNING_OBJECT_REQUEST_CODE) {
			if (resultCode == Activity.RESULT_OK) {
				LearningObject returnedLearningObject = (LearningObject)data.getExtras().get(LearningObjectSectionFragment.ARG_LEARNING_OBJECT);
				int learningObjectIndex = (Integer)data.getExtras().get(LearningObjectSectionFragment.ARG_LEARNING_OBJECT_INDEX);
				
				this.course.getLearningObjectList().set(learningObjectIndex, returnedLearningObject);
				
				Object[] viewArray = CourseEditActivity.this.learningObjectsLayoutMapping.get(CourseEditActivity.this.lastClickedView);
				((TextView)viewArray[NAME_VIEW]).setText(returnedLearningObject.getName());
				viewArray[OBJECT_ITEM] = returnedLearningObject;
				
				CourseEditActivity.this.lastClickedView = null;
			} else if (resultCode == Activity.RESULT_CANCELED) {
				//do nothing
			}
		} else if (requestCode == CourseEditActivity.NEW_LEARNING_OBJECT_REQUEST_CODE) {
			if (resultCode == Activity.RESULT_OK) {
				LearningObject learningObject = (LearningObject)data.getExtras().get(LearningObjectSectionFragment.ARG_LEARNING_OBJECT);
				createLearningObjectEntry(learningObject);
				this.course.getLearningObjectList().add(learningObject);
			} else if (resultCode == Activity.RESULT_CANCELED) {
				//do nothing
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
//		Intent resultIntent = new Intent();
		this.confirmDialog.dismiss();
		
		if (confirm) {
			finishSaving();
		} else {
			finishWithoutSaving();
		}
//		finish();
	}
	
	@Override
	public void onBackPressed() {
		showConfirmDialog();
	}	

	protected View createPrerequisiteEntry(String prerequisite) {
		RelativeLayout relativeLayout = new RelativeLayout(CourseEditActivity.this);
		
		EditText editText = new EditText(CourseEditActivity.this);
		editText.setInputType(InputType.TYPE_TEXT_VARIATION_SHORT_MESSAGE);
		editText.setHint(R.string.prerequisite_edit_text_hint);
		editText.setEms(16);
		
		if (!TextUtils.isEmpty(prerequisite)) {
			editText.setText(prerequisite);
		}
		
		ImageView imageView = new ImageView(CourseEditActivity.this);
		imageView.setImageDrawable(CourseEditActivity.this.getResources().getDrawable(R.drawable.content_discard));
		imageView.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				View viewToRemove = (View)CourseEditActivity.this.prerequisitesLayoutMapping.get(v)[LAYOUT_VIEW];
				String prerequisiteToRemove = (String)CourseEditActivity.this.prerequisitesLayoutMapping.get(v)[OBJECT_ITEM];
				((RelativeLayout)viewToRemove).removeAllViews();
				CourseEditActivity.this.llPrerequesites.removeViewInLayout(viewToRemove);
				CourseEditActivity.this.prerequisitesLayoutMapping.remove(v);
				CourseEditActivity.this.course.getSyllabus().getPrerequisites().remove(prerequisiteToRemove);
			}
		});
		
		//EditText
		RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
		params.addRule(RelativeLayout.ALIGN_PARENT_LEFT, RelativeLayout.TRUE);
		params.addRule(RelativeLayout.CENTER_VERTICAL, RelativeLayout.TRUE);
		params.addRule(RelativeLayout.ALIGN_RIGHT, imageView.getId());
		params.setMargins(0, 0, 50, 0);
		
		relativeLayout.addView(editText, params);

		//ImageView
		params = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
		params.addRule(RelativeLayout.ALIGN_PARENT_RIGHT, RelativeLayout.TRUE);
		params.addRule(RelativeLayout.ALIGN_PARENT_TOP, RelativeLayout.TRUE);
		
		relativeLayout.addView(imageView, params);

		Object[] viewArray = new Object[3];
		viewArray[LAYOUT_VIEW] = relativeLayout;
		viewArray[NAME_VIEW] = editText;
		viewArray[OBJECT_ITEM] = prerequisite;
		
		CourseEditActivity.this.prerequisitesLayoutMapping.put(imageView, viewArray);
		LinearLayout.LayoutParams llParams = new LinearLayout.LayoutParams(
				ViewGroup.LayoutParams.MATCH_PARENT, 
				ViewGroup.LayoutParams.WRAP_CONTENT, 
				0);
		llParams.setMargins(0, 10, 0, 0);
		CourseEditActivity.this.llPrerequesites.addView(relativeLayout, llParams);
		
		return imageView;
	}
	
	protected View createReferenceEntry(String reference) {
		RelativeLayout relativeLayout = new RelativeLayout(CourseEditActivity.this);
		
		EditText editText = new EditText(CourseEditActivity.this);
		editText.setInputType(InputType.TYPE_TEXT_VARIATION_SHORT_MESSAGE);
		editText.setHint(R.string.reference_edit_text_hint);
		editText.setEms(16);
		
		if (!TextUtils.isEmpty(reference)) {
			editText.setText(reference);
		}
		
		ImageView imageView = new ImageView(CourseEditActivity.this);
		imageView.setImageDrawable(CourseEditActivity.this.getResources().getDrawable(R.drawable.content_discard));
		imageView.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				View viewToRemove = (View)CourseEditActivity.this.referencesLayoutMapping.get(v)[LAYOUT_VIEW];
				String referenceToRemove = (String)CourseEditActivity.this.referencesLayoutMapping.get(v)[OBJECT_ITEM];
				((RelativeLayout)viewToRemove).removeAllViews();
				CourseEditActivity.this.llReferences.removeViewInLayout(viewToRemove);
				CourseEditActivity.this.referencesLayoutMapping.remove(v);
				CourseEditActivity.this.course.getSyllabus().getReferences().remove(referenceToRemove);
			}
		});
		
		//EditText
		RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
		params.addRule(RelativeLayout.ALIGN_PARENT_LEFT, RelativeLayout.TRUE);
		params.addRule(RelativeLayout.CENTER_VERTICAL, RelativeLayout.TRUE);
		params.addRule(RelativeLayout.ALIGN_RIGHT, imageView.getId());
		params.setMargins(0, 0, 50, 0);
		
		relativeLayout.addView(editText, params);
		
		//ImageView
		params = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
		params.addRule(RelativeLayout.ALIGN_PARENT_RIGHT, RelativeLayout.TRUE);
		params.addRule(RelativeLayout.ALIGN_PARENT_TOP, RelativeLayout.TRUE);
		
		relativeLayout.addView(imageView, params);
		
		Object[] viewArray = new Object[3];
		viewArray[LAYOUT_VIEW] = relativeLayout;
		viewArray[NAME_VIEW] = editText;
		viewArray[OBJECT_ITEM] = reference;
		
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
				CourseEditActivity.this.lastClickedView = (View)v.getParent();
				Intent intent = new Intent(CourseEditActivity.this, LearningObjectEditActivity.class);
				intent.putExtra(LoginActivity.ARG_USER, CourseEditActivity.this.user);
				intent.putExtra(CourseSectionFragment.ARG_COURSE, CourseEditActivity.this.course);
				int index = CourseEditActivity.this.course.getLearningObjectList().indexOf((LearningObject)CourseEditActivity.this.learningObjectsLayoutMapping.get((View)v.getParent())[OBJECT_ITEM]);
				intent.putExtra(LearningObjectSectionFragment.ARG_LEARNING_OBJECT_INDEX, index);
				startActivityForResult(intent, CourseEditActivity.EDIT_LEARNING_OBJECT_REQUEST_CODE);
			}
		});
		
		ImageView imageView = new ImageView(CourseEditActivity.this);
		imageView.setImageDrawable(CourseEditActivity.this.getResources().getDrawable(R.drawable.content_discard));
		imageView.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				View viewToRemove = (View)CourseEditActivity.this.learningObjectsLayoutMapping.get((View)v.getParent())[LAYOUT_VIEW];
				LearningObject learningObjectToRemove = (LearningObject)CourseEditActivity.this.learningObjectsLayoutMapping.get((View)v.getParent())[OBJECT_ITEM];
				((RelativeLayout)viewToRemove).removeAllViews();
				CourseEditActivity.this.llLearningObject.removeViewInLayout(viewToRemove);
				CourseEditActivity.this.learningObjectsLayoutMapping.remove(viewToRemove);
				CourseEditActivity.this.course.getLearningObjectList().remove(learningObjectToRemove);
			}
		});
		
		//TextView
		RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
		params.addRule(RelativeLayout.ALIGN_PARENT_LEFT, RelativeLayout.TRUE);
//		params.addRule(RelativeLayout.CENTER_VERTICAL, RelativeLayout.TRUE);
		params.addRule(RelativeLayout.ALIGN_RIGHT, imageView.getId());
		params.setMargins(0, 0, 50, 0);
		
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
		
		CourseEditActivity.this.learningObjectsLayoutMapping.put((View)imageView.getParent(), viewArray);
		
		LinearLayout.LayoutParams llParams = new LinearLayout.LayoutParams(
				ViewGroup.LayoutParams.MATCH_PARENT, 
				ViewGroup.LayoutParams.WRAP_CONTENT, 
				0);
		llParams.setMargins(0, 10, 0, 0);
		CourseEditActivity.this.llLearningObject.addView(relativeLayout, llParams);
	}
}
