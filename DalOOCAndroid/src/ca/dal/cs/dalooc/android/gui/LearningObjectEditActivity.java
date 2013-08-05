package ca.dal.cs.dalooc.android.gui;


import java.util.Map;

import android.app.Activity;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.view.Menu;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import ca.dal.cs.dalooc.android.R;
import ca.dal.cs.dalooc.android.gui.listener.OnConfirmDialogReturnListener;
import ca.dal.cs.dalooc.model.Course;
import ca.dal.cs.dalooc.model.LearningObject;
import ca.dal.cs.dalooc.model.User;
import ca.dal.cs.dalooc.model.Video;

public class LearningObjectEditActivity extends FragmentActivity implements OnConfirmDialogReturnListener {
	
//	private static final int LAYOUT_VIEW = 0;
	private static final int IMAGE_VIEW = 1;
	private static final int TEXT_VIEW = 2;

	public static final int EDIT_LEARNING_OBJECT_REQUEST_CODE = 100;
	
	private LearningObject learningObject;
	
	private User user;
	
	private Course course;
	
	private int learningObjectIndex;
	
	private ConfirmDialog confirmDialog;
	
	private EditText etName;
	private EditText etDescription;
	
	private Map<ImageView, View[]> videosLayoutMapping;
	
//	private LinearLayout llVideos;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_learning_object_edit);
		
		this.etName = (EditText)findViewById(R.id.etName);
		this.etDescription = (EditText)findViewById(R.id.etDescription);
		
		Bundle extras = getIntent().getExtras();
		if (extras != null) {
			this.user = (User)extras.get(LoginActivity.ARG_USER);
			this.course = (Course)extras.get(CourseSectionFragment.ARG_COURSE);
			this.learningObjectIndex = extras.getInt(LearningObjectSectionFragment.ARG_LEARNING_OBJECT_INDEX);

			if (this.learningObjectIndex >= 0) {
				this.learningObject = this.course.getLearningObjectList().get(this.learningObjectIndex);
			}
			
			if (this.learningObject != null) {
				loadData();
			} else {
				this.learningObject = new LearningObject();
				
			}
		}
	}

	private void loadData() {
		this.etName.setText(this.learningObject.getName());
		this.etDescription.setText(this.learningObject.getDescription());
		
		for (Video video : this.learningObject.getVideoList()) {
			View v = createVideoEntry();
			
			Drawable drawable = getResources().getDrawable(R.drawable.ic_video_thumbnail_default);
			
			((ImageView)this.videosLayoutMapping.get(v)[IMAGE_VIEW]).setImageDrawable(drawable);
			((TextView)this.videosLayoutMapping.get(v)[TEXT_VIEW]).setText(video.getName());
		}
	}

	private void fetchData() {
		this.learningObject.setName(this.etName.getText().toString());
		this.learningObject.setDescription(this.etDescription.getText().toString());
	}

	private View createVideoEntry() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.learning_object_edit, menu);
		
		return true;
	}
	
	private void showEditDialog() {
        FragmentManager fm = getSupportFragmentManager();
        
        Bundle args = new Bundle();
        args.putString(ConfirmDialog.ARG_TITLE, getResources().getString(R.string.dialog_title_learning_object));
        args.putString(ConfirmDialog.ARG_MESSAGE, getResources().getString(R.string.confirm_learning_object_changes));
        
        confirmDialog = new ConfirmDialog();
        confirmDialog.setArguments(args);
        confirmDialog.setOnConfirmDialogResultListener(this);
        confirmDialog.show(fm, "fragment_edit_name");
    }

	@Override
	public void onConfirmDialogReturn(boolean confirm) {
		Intent resultIntent = new Intent();
		
		if (confirm) {
			setResult(Activity.RESULT_OK, resultIntent);
			resultIntent.putExtra(LearningObjectSectionFragment.ARG_LEARNING_OBJECT, this.learningObject);
		} else {
			setResult(Activity.RESULT_CANCELED, resultIntent);
		}
		
		this.confirmDialog.dismiss();
		finish();
	}
	
	@Override
	public void onBackPressed() {
//		super.onBackPressed();
		fetchData();
		showEditDialog();
	}

}
