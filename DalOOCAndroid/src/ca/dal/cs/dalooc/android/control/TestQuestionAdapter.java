package ca.dal.cs.dalooc.android.control;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import ca.dal.cs.dalooc.android.R;
import ca.dal.cs.dalooc.android.gui.listener.OnGetStatusCallDoneListener;
import ca.dal.cs.dalooc.android.task.GetAnswerStatusCallTask;
import ca.dal.cs.dalooc.model.TestQuestion;

public class TestQuestionAdapter extends BaseAdapter {
	
	private LayoutInflater inflater;
	
	private List<TestQuestion> testQuestionList;
	
	private String userId;

	private String courseId;
	
	private String learningObjectId;
	
	public TestQuestionAdapter(LayoutInflater inflater, String userId, String courseId, String learningObjectId) {
		this.inflater = inflater;
		this.testQuestionList = new ArrayList<TestQuestion>();
		this.userId = userId;
		this.courseId = courseId;
		this.learningObjectId = learningObjectId;
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
		View view = this.inflater.inflate(R.layout.test_question_list_item, null);

		TestQuestion testQuestion = this.testQuestionList.get(position);
		
		TextView txtQuestion = (TextView)view.findViewById(R.id.txtQuestion);
		txtQuestion.setText(testQuestion.getQuestion());
		
		checkAnswerStatus((ImageView)view.findViewById(R.id.ivTestQuestionThumbnail), position);
		
		return view;
	}

	private void checkAnswerStatus(ImageView ivIcon, int position) {
		TestQuestion testQuestion = ((TestQuestion)getItem(position));
		GetAnswerStatusCallTask getAnswerStatusCallTask = new GetAnswerStatusCallTask(ivIcon, position);
		getAnswerStatusCallTask.setOnGetAnswerStatusCallDoneListener(new OnGetStatusCallDoneListener() {
			
			@Override
			public void onGetStatusCallResponse(String resultString, ImageView ivIcon, TextView txtLearningObjectName,  int position) {
				TestQuestion testQuestion = (TestQuestion)getItem(position);
				
				if (resultString.equals("correct")) {
					ivIcon.setImageDrawable(TestQuestionAdapter.this.inflater.getContext().getResources().getDrawable(R.drawable.ic_correct));
					testQuestion.setStatus(TestQuestion.Status.CORRECT);
				} else if (resultString.equals("incorrect")) {
					ivIcon.setImageDrawable(TestQuestionAdapter.this.inflater.getContext().getResources().getDrawable(R.drawable.ic_incorrect));
					testQuestion.setStatus(TestQuestion.Status.INCORRECT);
				} 
				ivIcon.setVisibility(View.VISIBLE);
			}
		});
		
		getAnswerStatusCallTask.execute(getUrlWebService(GetAnswerStatusCallTask.GET_ANSWER_STATUS_WEB_SERVICE),
				this.inflater.getContext().getResources().getString(R.string.namespace_webservice),
				this.inflater.getContext().getResources().getString(R.string.get_answer_status_webservice_operation),
				this.userId, 
				this.courseId, 
				this.learningObjectId, 
				testQuestion.getId(),
				testQuestion.getCorrectOption().getId());
	}
	
	private String getUrlWebService(int serviceCode) {
		if (serviceCode == GetAnswerStatusCallTask.GET_ANSWER_STATUS_WEB_SERVICE) {
			return this.inflater.getContext().getResources().getString(R.string.url_webservice) 
					+ "/" + this.inflater.getContext().getResources().getString(R.string.test_answer_repository) 
					+ "/" + this.inflater.getContext().getResources().getString(R.string.get_answer_status_webservice_operation);
		}
		return null;
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
