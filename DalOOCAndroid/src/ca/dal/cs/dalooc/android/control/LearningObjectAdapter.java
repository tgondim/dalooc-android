package ca.dal.cs.dalooc.android.control;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import ca.dal.cs.dalooc.android.R;
import ca.dal.cs.dalooc.android.gui.listener.OnGetStatusCallDoneListener;
import ca.dal.cs.dalooc.android.task.GetLearningObjectStatusCallTask;
import ca.dal.cs.dalooc.model.LearningObject;

public class LearningObjectAdapter extends BaseAdapter {

	private LayoutInflater inflater;
	
	private List<LearningObject> learningObjectList;
	
	private String userId;
	
	private String courseId;
	
	public LearningObjectAdapter(LayoutInflater inflater, String userId, String courseId) {
		this.inflater = inflater;
		this.learningObjectList = new ArrayList<LearningObject>();
		this.userId = userId;
		this.courseId = courseId;
	}
	
	@Override
	public int getCount() {
		return this.learningObjectList.size();
	}

	@Override
	public Object getItem(int position) {
		return this.learningObjectList.get(position);
	}

	@Override
	public long getItemId(int position) {
		BigInteger big = new BigInteger(this.learningObjectList.get(position).getId(), 16);
		return Long.valueOf(big.longValue());
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		LearningObject learningObject = this.learningObjectList.get(position);
		
		View view =  this.inflater.inflate(R.layout.learning_object_list_item, null);
		
		TextView txtLearningObjectName = (TextView)view.findViewById(R.id.txtLearningObjectName);
		txtLearningObjectName.setText(learningObject.getName());
		
		ImageView ivLearningObjectStatusIcon = (ImageView)view.findViewById(R.id.ivLearningObjectStatusIcon);

		ImageView ivViewLearningObject = (ImageView)view.findViewById(R.id.ivViewLearningObject);
		ivViewLearningObject.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {

				RelativeLayout llLearningObjectSummary = (RelativeLayout)((View)v.getParent()).findViewById(R.id.rlLearningObjectSummary);
				if (llLearningObjectSummary.getVisibility() == View.VISIBLE) {
					llLearningObjectSummary.setVisibility(View.GONE);
					((ImageView)v).setImageDrawable(inflater.getContext().getResources().getDrawable(R.drawable.ic_menu_more));
				} else { 
					llLearningObjectSummary.setVisibility(View.VISIBLE);
					((ImageView)v).setImageDrawable(inflater.getContext().getResources().getDrawable(R.drawable.ic_menu_less));
				}
			}
		});

		TextView txtLearningObjectTotalVideos = (TextView)view.findViewById(R.id.txtLearningObjectTotalVideos);
		txtLearningObjectTotalVideos.setText(String.valueOf(learningObject.getVideoList().size()));
		
		TextView txtLearningObjectTotalAudio = (TextView)view.findViewById(R.id.txtLearningObjectTotalAudio);
		txtLearningObjectTotalAudio.setText(String.valueOf(learningObject.getAudioList().size()));
		
		TextView txtLearningObjectTotalDocuments = (TextView)view.findViewById(R.id.txtLearningObjectTotalDocuments);
		txtLearningObjectTotalDocuments.setText(String.valueOf(learningObject.getDocumentList().size()));
		
		TextView txtLearningObjectTotalTestQuestions = (TextView)view.findViewById(R.id.txtLearningObjectTotalTestQuestions);
		txtLearningObjectTotalTestQuestions.setText(String.valueOf(learningObject.getTestQuestionList().size()));
		
		checkLearningObjectStatus(ivLearningObjectStatusIcon, txtLearningObjectName, position);
		
		return view;
	}
	
	private void checkLearningObjectStatus(ImageView ivIcon, TextView txtLearningObjectName, int position) {
		GetLearningObjectStatusCallTask getLearningObjectStatusCallTask = new GetLearningObjectStatusCallTask(ivIcon, txtLearningObjectName, position);
		getLearningObjectStatusCallTask.setOnGetStatusCallDoneListener(new OnGetStatusCallDoneListener() {
			
			@Override
			public void onGetStatusCallResponse(String resultString, ImageView ivIcon, TextView txtLearningObjectName, int position) {
				LearningObject learningObject = (LearningObject)getItem(position);
				double correctAnswersPercentage = Double.valueOf(resultString);
				
				if (correctAnswersPercentage == 1.0d) {
					ivIcon.setImageDrawable(LearningObjectAdapter.this.inflater.getContext().getResources().getDrawable(R.drawable.ic_done_green));
				} else if ((correctAnswersPercentage > 0d) && (correctAnswersPercentage < 1.0d)) {
					ivIcon.setImageDrawable(LearningObjectAdapter.this.inflater.getContext().getResources().getDrawable(R.drawable.ic_done_orange));
				} 
				if (correctAnswersPercentage >= learningObject.getCorrectAnswersPercentage()) { 
					learningObject.setStatus(LearningObject.Status.DONE);
				} else {
					learningObject.setStatus(LearningObject.Status.TODO);
				}
				
				if (position > 0) {
					LearningObject previousLearningObject = (LearningObject)getItem(position - 1);
					if (previousLearningObject.getStatus() == LearningObject.Status.TODO) {
						txtLearningObjectName.setTextColor(LearningObjectAdapter.this.inflater.getContext().getResources().getColor(android.R.color.darker_gray));
					}
				}
				ivIcon.setVisibility(View.VISIBLE);
			}
		});
		
		getLearningObjectStatusCallTask.execute(getUrlWebService(GetLearningObjectStatusCallTask.GET_LEARNING_OBJECT_STATUS_WEB_SERVICE),
				this.inflater.getContext().getResources().getString(R.string.namespace_webservice),
				this.inflater.getContext().getResources().getString(R.string.get_learning_object_status_webservice_operation),
				this.userId, 
				this.courseId, 
				String.valueOf(position));
	}
	
	private String getUrlWebService(int serviceCode) {
		if (serviceCode == GetLearningObjectStatusCallTask.GET_LEARNING_OBJECT_STATUS_WEB_SERVICE) {
			return this.inflater.getContext().getResources().getString(R.string.url_webservice) 
					+ "/" + this.inflater.getContext().getResources().getString(R.string.course_repository) 
					+ "/" + this.inflater.getContext().getResources().getString(R.string.get_learning_object_status_webservice_operation);
		}
		return null;
	}
	
	public List<LearningObject> getLearningObjectList() {
		return this.learningObjectList;
	}
	
	public void setLearningObjectList(List<LearningObject> learningObjectList) {
		this.learningObjectList = learningObjectList;
	}
	
	public void insert(LearningObject learningObject) {
		this.learningObjectList.add(learningObject);
	}

}
