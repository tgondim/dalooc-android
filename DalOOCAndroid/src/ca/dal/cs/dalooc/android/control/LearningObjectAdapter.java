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
import ca.dal.cs.dalooc.model.LearningObject;

public class LearningObjectAdapter extends BaseAdapter {

	private LayoutInflater inflater;
	
	private List<LearningObject> learningObjectList;
	
	private int expandedPosition;
	
	public LearningObjectAdapter(LayoutInflater inflater) {
		this.inflater = inflater;
		this.learningObjectList = new ArrayList<LearningObject>();
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

//		txtLearningObjectName.setOnClickListener(new View.OnClickListener() {
//			
//			@Override
//			public void onClick(View v) {
//				RelativeLayout llLearningObjectSummary = (RelativeLayout)((View)v.getParent()).findViewById(R.id.rlLearningObjectSummary);
//				if (llLearningObjectSummary.getVisibility() == View.VISIBLE) {
//					llLearningObjectSummary.setVisibility(View.GONE);
//				} else { 
//					llLearningObjectSummary.setVisibility(View.VISIBLE);
//				}
//			}
//		});

		TextView txtLearningObjectTotalVideos = (TextView)view.findViewById(R.id.txtLearningObjectTotalVideos);
		txtLearningObjectTotalVideos.setText(String.valueOf(learningObject.getVideoList().size()));
		
		TextView txtLearningObjectTotalAudio = (TextView)view.findViewById(R.id.txtLearningObjectTotalAudio);
		txtLearningObjectTotalAudio.setText(String.valueOf(learningObject.getAudioList().size()));
		
		TextView txtLearningObjectTotalDocuments = (TextView)view.findViewById(R.id.txtLearningObjectTotalDocuments);
		txtLearningObjectTotalDocuments.setText(String.valueOf(learningObject.getDocumentList().size()));
		
		TextView txtLearningObjectTotalTestQuestions = (TextView)view.findViewById(R.id.txtLearningObjectTotalTestQuestions);
		txtLearningObjectTotalTestQuestions.setText(String.valueOf(learningObject.getTestQuestionList().size()));
		
		return view;
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
