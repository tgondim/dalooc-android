package ca.dal.cs.dalooc.android.gui;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.media.ThumbnailUtils;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ImageView;
import android.widget.ListView;
import ca.dal.cs.dalooc.android.R;
import ca.dal.cs.dalooc.android.control.AudioAdapter;
import ca.dal.cs.dalooc.android.control.DocumentAdapter;
import ca.dal.cs.dalooc.android.control.TestQuestionAdapter;
import ca.dal.cs.dalooc.android.control.VideoAdapter;
import ca.dal.cs.dalooc.model.Course;
import ca.dal.cs.dalooc.model.LearningObject;
import ca.dal.cs.dalooc.model.TestAnswer;
import ca.dal.cs.dalooc.model.User;
import ca.dal.cs.dalooc.model.Video;

public class LearningObjectSectionFragment extends Fragment implements OnItemClickListener {

	private static final int TEST_QUESTION_ANSWER_ACTIVITY_REQUEST_CODE = 600;
	
	public static final String ARG_SECTION_NUMBER = "section_number";
	
	public static final String ARG_LEARNING_OBJECT =  "learning_object";
	
	public static final String ARG_LEARNING_OBJECT_INDEX =  "learning_object_index";

	public static final String ARG_VIDEO_INDEX =  "video_index";

	public static final String ARG_AUDIO_INDEX =  "audio_index";
	
	public static final String ARG_DOCUMENT_INDEX =  "document_index";

	public static final String ARG_TEST_QUESTION_INDEX =  "test_question_index";

	public static final String ARG_VIDEO_THUMBNAIL =  "document";
	
	private int sectionNumber;
	
	private int lastSelectedItemPosition = -1;
	
	private int learningObjectIndex;
	
	private User user;
	
	private Course course;
	
	private VideoAdapter videoAdapter;

	private AudioAdapter audioAdapter;

	private DocumentAdapter documentAdapter;

	private TestQuestionAdapter testQuestionAdapter;
	
	private ListView listViewItem;
	
	public LearningObjectSectionFragment() {
		super();
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		super.onCreateView(inflater, container, savedInstanceState);
		
		this.sectionNumber = getArguments().getInt(ARG_SECTION_NUMBER);
		
		this.user = (User)getArguments().getSerializable(LoginActivity.ARG_USER);
		this.course = (Course)getArguments().getSerializable(CourseSectionFragment.ARG_COURSE);
		this.learningObjectIndex = getArguments().getInt(ARG_LEARNING_OBJECT_INDEX);
		
		LearningObject learningObject = this.course.getLearningObjectList().get(this.learningObjectIndex);

		View rootView = null;
		rootView = inflater.inflate(R.layout.fragment_learning_object, container, false);
		this.listViewItem = (ListView) rootView.findViewById(R.id.listViewLearningObjectItem);

		switch (this.sectionNumber) {
		case 1: 
			this.videoAdapter = new VideoAdapter(inflater);
			
			this.listViewItem.setAdapter(videoAdapter);

			this.videoAdapter.setVideoList(learningObject.getVideoList());
			
			this.videoAdapter.notifyDataSetChanged();
			break;
			
		case 2: 
			this.audioAdapter = new AudioAdapter(inflater);
			
			this.listViewItem.setAdapter(audioAdapter);
			this.listViewItem.setOnItemClickListener(this);
			
			this.audioAdapter.setAudioList(learningObject.getAudioList());
			
			this.audioAdapter.notifyDataSetChanged();
			break;
			
		case 3: 
			this.documentAdapter = new DocumentAdapter(inflater);
			
			this.listViewItem.setAdapter(documentAdapter);
			this.listViewItem.setOnItemClickListener(this);
			
			this.documentAdapter.setDocumentList(learningObject.getDocumentList());
			
			this.documentAdapter.notifyDataSetChanged();
			break;
			
		case 4: 
			this.testQuestionAdapter = new TestQuestionAdapter(inflater);
			
			this.listViewItem.setAdapter(testQuestionAdapter);
			this.listViewItem.setOnItemClickListener(this);
			
			this.testQuestionAdapter.setTestQuestionList(learningObject.getTestQuestionList());
			
			this.testQuestionAdapter.notifyDataSetChanged();
			break;
			
		}
		
		this.listViewItem.setDividerHeight(0);
		this.listViewItem.setOnItemClickListener(this);
		
		return rootView;
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		
		switch (this.sectionNumber) {
		case 1:
			this.lastSelectedItemPosition = position;
			
			Intent videoDetailIntent = new Intent("VIDEO_DETAIL_ACTIVITY");
			videoDetailIntent.putExtra(LoginActivity.ARG_USER, this.user);
			videoDetailIntent.putExtra(CourseSectionFragment.ARG_COURSE, this.course);
			videoDetailIntent.putExtra(ARG_LEARNING_OBJECT_INDEX, this.learningObjectIndex);
			videoDetailIntent.putExtra(ARG_VIDEO_INDEX, position);
			
			startActivity(videoDetailIntent);
			break;
		
		case 2: 
			Intent audioDetailIntent = new Intent("AUDIO_DETAIL_ACTIVITY");
			audioDetailIntent.putExtra(LoginActivity.ARG_USER, this.user);
			audioDetailIntent.putExtra(CourseSectionFragment.ARG_COURSE, this.course);
			audioDetailIntent.putExtra(ARG_LEARNING_OBJECT_INDEX, this.learningObjectIndex);
			audioDetailIntent.putExtra(ARG_AUDIO_INDEX, position);
			
			startActivity(audioDetailIntent);
			break;
		
		case 3: 
			Intent documentDetailIntent = new Intent("DOCUMENT_DETAIL_ACTIVITY");
			documentDetailIntent.putExtra(LoginActivity.ARG_USER, this.user);
			documentDetailIntent.putExtra(CourseSectionFragment.ARG_COURSE, this.course);
			documentDetailIntent.putExtra(ARG_LEARNING_OBJECT_INDEX, this.learningObjectIndex);
			documentDetailIntent.putExtra(ARG_DOCUMENT_INDEX, position);
			
			startActivity(documentDetailIntent);
			break;

		case 4: 
			Intent testQuestionDetailIntent = new Intent("TEST_QUESTION_DETAIL_ACTIVITY");
			testQuestionDetailIntent.putExtra(ARG_LEARNING_OBJECT_INDEX, this.learningObjectIndex);
			testQuestionDetailIntent.putExtra(ARG_TEST_QUESTION_INDEX, position);
			testQuestionDetailIntent.putExtra(LoginActivity.ARG_USER, this.user);
			testQuestionDetailIntent.putExtra(CourseSectionFragment.ARG_COURSE, this.course);
			
			startActivityForResult(testQuestionDetailIntent, TEST_QUESTION_ANSWER_ACTIVITY_REQUEST_CODE);
			break;
		}
	}
	
	@Override
	public void onResume() {
		super.onResume();
		if ((this.videoAdapter != null) && (this.lastSelectedItemPosition != -1)){
			View itemVideo = this.videoAdapter.getView(this.lastSelectedItemPosition, null, this.listViewItem);
			ImageView ivVideoThumbnail = (ImageView)itemVideo.findViewById(R.id.ivVideoThumbnail);
			
			Video video = ((Video)this.videoAdapter.getItem(this.lastSelectedItemPosition));
			
			Bitmap bitmap = ThumbnailUtils.createVideoThumbnail(Environment.getExternalStorageDirectory() + video.getContentFileName(), MediaStore.Video.Thumbnails.MICRO_KIND);
			
			if (bitmap != null) {
				ivVideoThumbnail.setImageBitmap(bitmap);
			}
		}
	}
	
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
//		super.onActivityResult(requestCode, resultCode, data);
		if (requestCode == TEST_QUESTION_ANSWER_ACTIVITY_REQUEST_CODE) {
			if (resultCode == Activity.RESULT_OK) {
				boolean chooseCorrect = data.getBooleanExtra(TestQuestionDetailActivity.CHOOSE_CORRECT_ARG, false);
				TestAnswer answer = (TestAnswer)data.getSerializableExtra(TestQuestionDetailActivity.TEST_ANSWER_ARG);
				
//				((TestQuestion)this.listViewItem.getItemAtPosition(this.position)).set(R.id.ivTestQuestionThumbnail);
//				if (chooseCorrect) {
//					iv.setImageDrawable(getResources().getDrawable(R.drawable.correct));
//				} else {
//					iv.setImageDrawable(getResources().getDrawable(R.drawable.incorrect));
//				}
			} else {
				//till now, do nothing
			}
		}
	}
	
}
