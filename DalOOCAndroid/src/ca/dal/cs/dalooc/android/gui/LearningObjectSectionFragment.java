package ca.dal.cs.dalooc.android.gui;

import java.io.ByteArrayOutputStream;

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
import ca.dal.cs.dalooc.android.control.VideoAdapter;
import ca.dal.cs.dalooc.model.Audio;
import ca.dal.cs.dalooc.model.Document;
import ca.dal.cs.dalooc.model.LearningObject;
import ca.dal.cs.dalooc.model.Video;

public class LearningObjectSectionFragment extends Fragment implements OnItemClickListener {

	public static final String ARG_SECTION_NUMBER = "section_number";
	
	public static final String ARG_LEARNING_OBJECT =  "learning_object";

	public static final String ARG_VIDEO =  "video";
	
	public static final String ARG_AUDIO =  "audio";
	
	public static final String ARG_DOCUMENT =  "document";
	
	public static final String ARG_VIDEO_THUMBNAIL =  "document";
	
	private int sectionNumber;
	
	private int lastSelectedItemPosition = -1;
	
	private VideoAdapter videoAdapter;

	private AudioAdapter audioAdapter;

	private DocumentAdapter documentAdapter;
	
	private ListView listViewItem;
	
	public LearningObjectSectionFragment() {
		super();
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		super.onCreateView(inflater, container, savedInstanceState);
		
		this.sectionNumber = getArguments().getInt(ARG_SECTION_NUMBER);
		
		View rootView = inflater.inflate(R.layout.fragment_learning_object, container, false);
		
		LearningObject learningObject = (LearningObject)getArguments().getSerializable(ARG_LEARNING_OBJECT);
		
		this.listViewItem = (ListView) rootView.findViewById(R.id.listViewLearningObjectItem);
		this.listViewItem.setDividerHeight(0);
		this.listViewItem.setOnItemClickListener(this);

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
			
		}
		return rootView;
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		switch (this.sectionNumber) {
		case 1:
			this.lastSelectedItemPosition = position;
			
			Video video = (Video)this.videoAdapter.getItem(position);
			
			Intent videoDetailIntent = new Intent("VIDEO_DETAIL_ACTIVITY");
			videoDetailIntent.putExtra(ARG_VIDEO, video);
			
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
			
			startActivity(audioDetailIntent);
			break;
		
		case 3: 
			Document document = (Document)this.documentAdapter.getItem(position);
			
			Intent documentDetailIntent = new Intent("DOCUMENT_DETAIL_ACTIVITY");
			documentDetailIntent.putExtra(ARG_DOCUMENT, document);
			
			startActivity(documentDetailIntent);
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
	
}
