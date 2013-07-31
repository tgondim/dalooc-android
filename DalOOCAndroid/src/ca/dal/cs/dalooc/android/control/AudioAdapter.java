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
import ca.dal.cs.dalooc.model.Audio;

public class AudioAdapter extends BaseAdapter {

	private LayoutInflater inflater;
	
	private Audio audio;
	
	private List<Audio> audioList;
	
	public AudioAdapter(LayoutInflater inflater) {
		this.inflater = inflater;
		this.audioList = new ArrayList<Audio>();
	}
	
	@Override
	public int getCount() {
		return this.audioList.size();
	}

	@Override
	public Object getItem(int position) {
		return this.audioList.get(position);
	}

	@Override
	public long getItemId(int position) {
		BigInteger big = new BigInteger(this.audioList.get(position).getId(), 16);
		return Long.valueOf(big.longValue());
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		audio = this.audioList.get(position);
		
		View view = this.inflater.inflate(R.layout.audio_list_item, null);
		
		TextView txtAudioItemName = (TextView)view.findViewById(R.id.txtAudioItemName);
		txtAudioItemName.setText(audio.getName());

		return view;
	}

	public List<Audio> getAudioList() {
		return this.audioList;
	}
	
	public void setAudioList(List<Audio> videoList) {
		this.audioList = videoList;
	}
	
	public void insert(Audio video) {
		this.audioList.add(video);
	}

}
