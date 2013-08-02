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
import ca.dal.cs.dalooc.model.Document;


public class DocumentAdapter extends BaseAdapter {

	private LayoutInflater inflater;
	
	private List<Document> documentList;
	
	public DocumentAdapter(LayoutInflater inflater) {
		this.inflater = inflater;
		this.documentList = new ArrayList<Document>();
	}
	
	@Override
	public int getCount() {
		return this.documentList.size();
	}

	@Override
	public Object getItem(int position) {
		return this.documentList.get(position);
	}

	@Override
	public long getItemId(int position) {
		BigInteger big = new BigInteger(this.documentList.get(position).getId(), 16);
		return Long.valueOf(big.longValue());
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		Document document = this.documentList.get(position);
		
		View view = this.inflater.inflate(R.layout.document_list_item, null);
		
		TextView txtDocumentName = (TextView)view.findViewById(R.id.txtDocumentItemName);
		txtDocumentName.setText(document.getName());
		
//		ImageView ivDocumentThumbnail = (ImageView)view.findViewById(R.id.ivDocumentThumbnail);
		
//		if (video.getThumbnail() != null) {
//			ivDocumentThumbnail.setImageDrawable(this.inflater.getContext().getResources().getDrawable(R.drawable.ic_document));
//		}
		
		return view;
	}

	public List<Document> getDocumentList() {
		return this.documentList;
	}
	
	public void setDocumentList(List<Document> documentList) {
		this.documentList = documentList;
	}
	
	public void insert(Document document) {
		this.documentList.add(document);
	}
}
