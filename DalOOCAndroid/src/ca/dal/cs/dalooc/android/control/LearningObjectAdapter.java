package ca.dal.cs.dalooc.android.control;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
import ca.dal.cs.android.dalooc.R;
import ca.dal.cs.dalooc.model.LearningObject;

public class LearningObjectAdapter extends BaseAdapter {

	private LayoutInflater inflater;
	
	private List<LearningObject> learningObjectList;
	
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
