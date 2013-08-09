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
import android.widget.TextView;
import ca.dal.cs.dalooc.android.R;
import ca.dal.cs.dalooc.android.util.DownloadImageTask;
import ca.dal.cs.dalooc.model.Video;


public class VideoAdapter extends BaseAdapter {

	private LayoutInflater inflater;
	
	private List<Video> videoList;
	
	public VideoAdapter(LayoutInflater inflater) {
		this.inflater = inflater;
		this.videoList = new ArrayList<Video>();
	}
	
	@Override
	public int getCount() {
		return this.videoList.size();
	}

	@Override
	public Object getItem(int position) {
		return this.videoList.get(position);
	}

	@Override
	public long getItemId(int position) {
		BigInteger big = new BigInteger(this.videoList.get(position).getId(), 16);
		return Long.valueOf(big.longValue());
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		Video video = this.videoList.get(position);
		
		View view = this.inflater.inflate(R.layout.video_list_item, null);
		
		TextView txtVideoName = (TextView)view.findViewById(R.id.txtVideoName);
 		txtVideoName.setText(video.getName());
		
 		if (!TextUtils.isEmpty(video.getContentFileName())) {
			new DownloadImageTask((ImageView)view.findViewById(R.id.ivVideoThumbnail))
				.execute(this.inflater.getContext().getResources().getString(R.string.host_file_server)
						+ this.inflater.getContext().getResources().getString(R.string.videos_folder)
						+ "/thumb/" + video.getContentFileName().replace("mp4", "jpg"));
 		}
		
		return view;
	}

	public List<Video> getVideoList() {
		return this.videoList;
	}
	
	public void setVideoList(List<Video> videoList) {
		this.videoList = videoList;
	}
	
	public void insert(Video video) {
		this.videoList.add(video);
	}
}
