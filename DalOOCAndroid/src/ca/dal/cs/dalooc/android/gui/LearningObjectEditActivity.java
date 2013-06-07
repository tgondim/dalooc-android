package ca.dal.cs.dalooc.android.gui;


import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.view.Menu;
import ca.dal.cs.android.dalooc.R;
import ca.dal.cs.dalooc.android.gui.listener.OnConfirmDialogReturnListener;
import ca.dal.cs.dalooc.model.LearningObject;

public class LearningObjectEditActivity extends FragmentActivity implements OnConfirmDialogReturnListener {

	public static final int EDIT_LEARNING_OBJECT_REQUEST_CODE = 100;
	
	private LearningObject learningObject;
	
	private ConfirmDialog confirmDialog;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_learning_object_edit);
		
		Bundle extras = getIntent().getExtras();
		if (extras != null) {
			this.learningObject = (LearningObject)extras.get(LearningObjectSectionFragment.ARG_LEARNING_OBJECT);
			loadData();
		} else {
			this.learningObject = new LearningObject();
		}
	}

	private void loadData() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.learning_object_edit, menu);
		
		return true;
	}
	
	private void showEditDialog() {
        FragmentManager fm = getSupportFragmentManager();
        confirmDialog = new ConfirmDialog();
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
		showEditDialog();
	}
}
