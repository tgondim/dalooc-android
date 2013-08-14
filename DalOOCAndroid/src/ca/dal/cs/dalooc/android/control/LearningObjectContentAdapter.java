package ca.dal.cs.dalooc.android.control;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import ca.dal.cs.dalooc.android.R;
import ca.dal.cs.dalooc.android.task.DownloadImageTask;
import ca.dal.cs.dalooc.model.Audio;
import ca.dal.cs.dalooc.model.Document;
import ca.dal.cs.dalooc.model.LearningObjectContent;
import ca.dal.cs.dalooc.model.Video;

public class LearningObjectContentAdapter extends BaseAdapter {

	private LayoutInflater inflater;
	
	private LearningObjectContent learningObjectContent;
	
	private List<LearningObjectContent> learningObjectContentList;
	
	public LearningObjectContentAdapter(LayoutInflater inflater) {
		this.inflater = inflater;
		this.learningObjectContentList = new ArrayList<LearningObjectContent>();
	}
	
	@Override
	public int getCount() {
		return this.learningObjectContentList.size();
	}

	@Override
	public Object getItem(int position) {
		return this.learningObjectContentList.get(position);
	}

	@Override
	public long getItemId(int position) {
		BigInteger big = new BigInteger(this.learningObjectContentList.get(position).getId(), 16);
		return Long.valueOf(big.longValue());
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		learningObjectContent = this.learningObjectContentList.get(position);
		
		View view = this.inflater.inflate(R.layout.learning_object_content_list_item, null);
		
		TextView txtRelatedContentItemName = (TextView)view.findViewById(R.id.txtLearningObjectContentItemName);
		txtRelatedContentItemName.setText(learningObjectContent.getName());
		
		ImageView ivLearningObjectContentIcon = (ImageView)view.findViewById(R.id.ivLearningObjectContentIcon);

		if (learningObjectContent instanceof Video) {
			ivLearningObjectContentIcon.setImageDrawable(this.inflater.getContext().getResources().getDrawable(R.drawable.ic_video_thumbnail_default));
			if (!TextUtils.isEmpty(learningObjectContent.getContentFileName())) {
				downloadVideoIcon((Video)learningObjectContent, ivLearningObjectContentIcon, 0);
			}
		} else if (learningObjectContent instanceof Audio) {
			ivLearningObjectContentIcon.setImageDrawable(this.inflater.getContext().getResources().getDrawable(R.drawable.ic_audio_light));
		} else if (learningObjectContent instanceof Document) {
			ivLearningObjectContentIcon.setImageDrawable(this.inflater.getContext().getResources().getDrawable(R.drawable.ic_document_light));
		}

		return view;
	}

	public List<LearningObjectContent> getLearningObjectList() {
		return this.learningObjectContentList;
	}
	
	public void setLearningObjectContentList(List<LearningObjectContent> learningObjectContentList) {
		this.learningObjectContentList = learningObjectContentList;
	}
	
	public void insert(LearningObjectContent learningObjectContent) {
		this.learningObjectContentList.add(learningObjectContent);
	}
	
	private void downloadVideoIcon(Video video, ImageView ivVideoThumbnail, int waitTime) {
		if (waitTime > 0) {
			ivVideoThumbnail.setImageDrawable(this.inflater.getContext().getResources().getDrawable(R.drawable.ic_video_thumbnail_default));
		}
		
		RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(64, 64);
		layoutParams.setMargins(15, 5, 0, 5);
		layoutParams.addRule(RelativeLayout.ALIGN_PARENT_LEFT, RelativeLayout.TRUE);
		layoutParams.addRule(RelativeLayout.ALIGN_PARENT_TOP, RelativeLayout.TRUE);
		ivVideoThumbnail.setLayoutParams(layoutParams);
		
		new DownloadImageTask(ivVideoThumbnail).execute(this.inflater.getContext().getResources().getString(R.string.host_file_server)
				+ this.inflater.getContext().getResources().getString(R.string.videos_folder)
				+ "/thumb/" + video.getContentFileName().replace("mp4", "jpg"), String.valueOf(waitTime));
	}

}
