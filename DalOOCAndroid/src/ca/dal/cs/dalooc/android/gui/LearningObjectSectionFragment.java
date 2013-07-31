package ca.dal.cs.dalooc.android.gui;

import java.io.ByteArrayOutputStream;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
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
import ca.dal.cs.android.dalooc.R;
import ca.dal.cs.dalooc.android.control.AudioAdapter;
import ca.dal.cs.dalooc.android.control.DocumentAdapter;
import ca.dal.cs.dalooc.android.control.TestQuestionAdapter;
import ca.dal.cs.dalooc.android.control.VideoAdapter;
import ca.dal.cs.dalooc.model.Audio;
import ca.dal.cs.dalooc.model.Document;
import ca.dal.cs.dalooc.model.LearningObject;
import ca.dal.cs.dalooc.model.TestAnswer;
import ca.dal.cs.dalooc.model.TestQuestion;
import ca.dal.cs.dalooc.model.User;
import ca.dal.cs.dalooc.model.Video;

public class LearningObjectSectionFragment extends Fragment implements OnItemClickListener {

	private static final int TEST_QUESTION_ANSWER_ACTIVITY_REQUEST_CODE = 600;
	
	public static final String ARG_SECTION_NUMBER = "section_number";
	
	public static final String ARG_LEARNING_OBJECT =  "learning_object";

	public static final String ARG_VIDEO =  "video";
	
	public static final String ARG_AUDIO =  "audio";
	
	public static final String ARG_DOCUMENT =  "document";

	public static final String ARG_TEST_QUESTION =  "test_question";

	public static final String ARG_VIDEO_THUMBNAIL =  "document";
	
	private int sectionNumber;
	
	private int position;
	
	private int lastSelectedItemPosition = -1;
	
	private User user;
	
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
		
		LearningObject learningObject = (LearningObject)getArguments().getSerializable(ARG_LEARNING_OBJECT);

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
		
		this.position = position;
		
		switch (this.sectionNumber) {
		case 1:
			this.lastSelectedItemPosition = position;
			
			Video video = (Video)this.videoAdapter.getItem(position);
			
			Intent videoDetailIntent = new Intent("VIDEO_DETAIL_ACTIVITY");
			videoDetailIntent.putExtra(ARG_VIDEO, video);
			videoDetailIntent.putExtra(LoginActivity.ARG_USER, this.user);
			
			Bitmap b = ((BitmapDrawable)((ImageView)view.findViewById(R.id.ivVideoThumbnail)).getDrawable()).getBitmap();
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			b.compress(Bitmap.CompressFormat.JPEG, 100, baos);
			byte[] videoThumbnail = baos.toByteArray();
			
			videoDetailIntent.putExtra(ARG_VIDEO_THUMBNAIL, videoThumbnail);
			
			startActivity(videoDetailIntent);
		break;
		
		case 2: 
			Audio audio = (Audio)this.audioAdapter.getItem(position);

			Intent audioDetailIntent = new Intent("AUDIO_DETAIL_ACTIVITY");
			audioDetailIntent.putExtra(ARG_AUDIO, audio);
			audioDetailIntent.putExtra(LoginActivity.ARG_USER, this.user);
			
			startActivity(audioDetailIntent);
			break;
		
		case 3: 
			Document document = (Document)this.documentAdapter.getItem(position);
			
			Intent documentDetailIntent = new Intent("DOCUMENT_DETAIL_ACTIVITY");
			documentDetailIntent.putExtra(ARG_DOCUMENT, document);
			documentDetailIntent.putExtra(LoginActivity.ARG_USER, this.user);
			
			startActivity(documentDetailIntent);
			break;

		case 4: 
			TestQuestion testQuestion = (TestQuestion)this.testQuestionAdapter.getItem(position);
			
			Intent testQuestionDetailIntent = new Intent("TEST_QUESTION_DETAIL_ACTIVITY");
			testQuestionDetailIntent.putExtra(ARG_TEST_QUESTION, testQuestion);
			testQuestionDetailIntent.putExtra(LoginActivity.ARG_USER, this.user);
			
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
			
			Bitmap bitmap = ThumbnailUtils.createVideoThumbnail(Environment.getExternalStorageDirectory() + video.getVideoUrl(), MediaStore.Video.Thumbnails.MICRO_KIND);
			
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
