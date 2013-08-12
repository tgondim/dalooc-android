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
import ca.dal.cs.dalooc.model.Course;


public class CourseAdapter extends BaseAdapter {

	private LayoutInflater inflater;
	
	private List<Course> courseList;
	
	public CourseAdapter(LayoutInflater inflater) {
		this.inflater = inflater;
		this.courseList = new ArrayList<Course>();
	}
	
	@Override
	public int getCount() {
		return this.courseList.size();
	}

	@Override
	public Object getItem(int position) {
		return this.courseList.get(position);
	}

	@Override
	public long getItemId(int position) {
		BigInteger big = new BigInteger(this.courseList.get(position).getId(), 16);
		return Long.valueOf(big.longValue());
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		Course course = this.courseList.get(position);
		
		View view = this.inflater.inflate(R.layout.course_list_item, null);
		
		TextView txtCourseName = (TextView)view.findViewById(R.id.txtCourseName);
		txtCourseName.setText(course.getName());
		
		return view;
	}

	public List<Course> getCourseList() {
		return this.courseList;
	}
	
	public void setCourseList(List<Course> courseList) {
		this.courseList = courseList;
	}
	
	public void insert(Course course) {
		this.courseList.add(course);
	}
	
	public void removeAllItems() {
		this.courseList.clear();
	}
}
