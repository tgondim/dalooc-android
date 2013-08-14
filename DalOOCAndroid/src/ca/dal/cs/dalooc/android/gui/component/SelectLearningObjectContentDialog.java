package ca.dal.cs.dalooc.android.gui.component;

import java.util.List;

import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import ca.dal.cs.dalooc.android.R;
import ca.dal.cs.dalooc.android.control.LearningObjectContentAdapter;
import ca.dal.cs.dalooc.android.gui.LearningObjectSectionFragment;
import ca.dal.cs.dalooc.android.gui.TestQuestionEditActivity;
import ca.dal.cs.dalooc.android.gui.listener.OnSelectLearningObjectContentDialogReturnListener;
import ca.dal.cs.dalooc.model.LearningObject;
import ca.dal.cs.dalooc.model.LearningObjectContent;

public class SelectLearningObjectContentDialog extends DialogFragment {
	
	public static final String ARG_TITLE = "title";
	public static final String ARG_RETURN_CODE = "return_code";
	public static final String ARG_CANCEL_BUTTON = "no_cancel_button";

	private OnSelectLearningObjectContentDialogReturnListener listener;
	
	private LearningObjectContentAdapter learningObjectContentAdapter;
	
	private String title;
	
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dialog_select_learning_object_content, container);
        
		this.title = getResources().getString(R.string.select_related_content);
	
		LearningObject learningObject = (LearningObject)getArguments().getSerializable(LearningObjectSectionFragment.ARG_LEARNING_OBJECT);
		
		
        ListView lvLearningObjectContent = (ListView)view.findViewById(R.id.lvLearningObjectContent);
        learningObjectContentAdapter = new LearningObjectContentAdapter(inflater);
        learningObjectContentAdapter.setLearningObjectContentList(learningObject.getLearningObjectContentList());
        lvLearningObjectContent.setAdapter(learningObjectContentAdapter);

        lvLearningObjectContent.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				Object[] relatedContent = new Object[2];
				relatedContent[TestQuestionEditActivity.RELATED_CONTENT_INDEX] = position;
				relatedContent[TestQuestionEditActivity.RELATED_CONTENT_OBJECT] = SelectLearningObjectContentDialog.this.learningObjectContentAdapter.getItem(position);
				fireOnSelectLearningObjectContentDialogResultEvent(relatedContent);
				dismiss();
			}
		});
        
        getDialog().setTitle(this.title);
        getDialog().setCanceledOnTouchOutside(false);
        getDialog().setCancelable(true);
        
        return view;
    }
    
    public void setLearningObjectContentList(List<LearningObjectContent> learningObjectContent) {
    	
    }
    
    private void fireOnSelectLearningObjectContentDialogResultEvent(Object[] relatedContent) {
    	if (this.listener != null) {
    		this.listener.onSelectRelatedContentDialogReturn(relatedContent);
    	}
    }
    
    public void setOnSelectLearningObjectContentDialogResultListener(OnSelectLearningObjectContentDialogReturnListener listener) {
    	this.listener = listener;
    }
}
