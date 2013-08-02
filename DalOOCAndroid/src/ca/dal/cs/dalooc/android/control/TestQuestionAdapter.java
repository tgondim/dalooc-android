package ca.dal.cs.dalooc.android.control;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
import ca.dal.cs.dalooc.android.R;
import ca.dal.cs.dalooc.model.TestQuestion;

public class TestQuestionAdapter extends BaseAdapter {
	private LayoutInflater inflater;
	
	private List<TestQuestion> testQuestionList;
	
	public TestQuestionAdapter(LayoutInflater inflater) {
		this.inflater = inflater;
		this.testQuestionList = new ArrayList<TestQuestion>();
	}
	
	@Override
	public int getCount() {
		return this.testQuestionList.size();
	}

	@Override
	public Object getItem(int position) {
		return this.testQuestionList.get(position);
	}

	@Override
	public long getItemId(int position) {
		BigInteger big = new BigInteger(this.testQuestionList.get(position).getId(), 16);
		return Long.valueOf(big.longValue());
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		TestQuestion testQuestion = this.testQuestionList.get(position);
		
		View view = this.inflater.inflate(R.layout.test_question_list_item, null);
		
		TextView txtQuestion = (TextView)view.findViewById(R.id.txtQuestion);
		txtQuestion.setText(testQuestion.getQuestion());
		
		return view;
	}

	public List<TestQuestion> getTestQuestionList() {
		return this.testQuestionList;
	}
	
	public void setTestQuestionList(List<TestQuestion> testQuestionList) {
		this.testQuestionList = testQuestionList;
	}
	
	public void insert(TestQuestion testQuestion) {
		this.testQuestionList.add(testQuestion);
	}
}
