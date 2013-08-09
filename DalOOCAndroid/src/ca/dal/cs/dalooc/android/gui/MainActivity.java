package ca.dal.cs.dalooc.android.gui;

import java.util.ArrayList;

import org.ksoap2.SoapEnvelope;
import org.ksoap2.serialization.SoapObject;
import org.ksoap2.serialization.SoapSerializationEnvelope;
import org.ksoap2.transport.HttpTransportSE;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Toast;
import ca.dal.cs.dalooc.android.R;
import ca.dal.cs.dalooc.android.control.CourseAdapter;
import ca.dal.cs.dalooc.android.webservices.GetAllCoursesCallBack;
import ca.dal.cs.dalooc.android.webservices.SaveCourseCallRunnable;
import ca.dal.cs.dalooc.android.webservices.OnSaveCourseCallDoneListener;
import ca.dal.cs.dalooc.model.Course;
import ca.dal.cs.dalooc.model.User;
import ca.dal.cs.dalooc.webservice.util.Parser;

import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.util.JSON;

public class MainActivity extends Activity implements OnItemClickListener, GetAllCoursesCallBack, OnSaveCourseCallDoneListener {

	public static final int COURSE_ACTIVITY_CALL = 300;
	
	private ListView listViewCourse;
	
	private boolean getAllCoursesWebServiceResponseOk;
	private boolean inicioConsulta;
	
	private User user;
	
	private CourseAdapter courseAdapter;
	
	private ArrayList<Course> nextPageCourseList;
	
	private Toast toast;
	
	private int lastPositionClicked;
	
	@SuppressLint("HandlerLeak")
	private Handler callBackHandler = new Handler() {
		public void handleMessage(android.os.Message msg) {
			if (MainActivity.this.getAllCoursesWebServiceResponseOk) {				
				loadCourseList(MainActivity.this.getAllCoursesWebServiceResponseOk);
				MainActivity.this.getAllCoursesWebServiceResponseOk = false;
			}
			
			if ((MainActivity.this.courseAdapter == null) || (MainActivity.this.courseAdapter.getCount() == 0)){
				
				LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
				if (MainActivity.this.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
					lp.weight = 1.3f;						
				} else {
					lp.weight = 4.3f;			
				}
				
				MainActivity.this.inicioConsulta = true;
			}
		}
	};
	
	@SuppressLint("ShowToast")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
	
		courseAdapter = new CourseAdapter(LayoutInflater.from(this));
		
		this.listViewCourse = (ListView)findViewById(R.id.listViewCourse);
		this.listViewCourse.setDividerHeight(0);
		this.listViewCourse.setAdapter(courseAdapter);
		this.listViewCourse.setOnItemClickListener(this);
		
		this.toast = Toast.makeText(this, "", Toast.LENGTH_LONG);
		
		this.user = (User)getIntent().getExtras().getSerializable(LoginActivity.ARG_USER);
		
		getCourses();
	}

	private void getCourses() {
		new Thread(new GetAllCoursesCall()).start();
	}
	
	@Override
	protected void onRestart() {
		super.onRestart();
//		this.listViewCourse.removeAllViews();
//		getCourses();
	}
	
	@Override
	public String getUrlWebService(int serviceCode) {
		if (serviceCode == GetAllCoursesCall.GET_ALL_COURSES_WEB_SERVICE) {
			return getResources().getString(R.string.url_webservice) + "/" + getResources().getString(R.string.course_repository) + "/" + getResources().getString(R.string.get_all_courses_webservice_operation); 
		} else if (serviceCode == SaveCourseCallRunnable.SAVE_COURSE_WEB_SERVICE) {
			return getResources().getString(R.string.url_webservice) + "/" + getResources().getString(R.string.course_repository) + "/" + getResources().getString(R.string.save_course_webservice_operation); 
		}
		return null;
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		if (this.user.getUserType().equals(User.UserType.PROFESSOR)) {
			getMenuInflater().inflate(R.menu.main_user_type_professor, menu);
		} else if (this.user.getUserType().equals(User.UserType.STUDENT)) {
			getMenuInflater().inflate(R.menu.main_user_type_student, menu);
		}
		return true;
	}

	@Override
	public boolean onMenuItemSelected(int featureId, MenuItem item) {
		Intent intent;
		switch (item.getOrder()) {
		case 100:
			break;
			
		case 200:
			intent = new Intent(this, CourseEditActivity.class);
			intent.putExtra(LoginActivity.ARG_USER, this.user);
			
			startActivityForResult(intent, CourseActivity.NEW_COURSE_REQUEST_CODE);
			break;

		case 888:
			intent = new Intent(this, SettingsActivity.class);
			intent.putExtra(LoginActivity.ARG_USER, this.user);
			startActivity(intent);
			break;
			
		case 999:
			SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(this);
			if (!pref.getBoolean("pref_sign_in_automatically", false)) { 
				Intent loginIntent = new Intent(this, LoginActivity.class);
				startActivity(loginIntent);
			}
			finish();
			break;
		
		}
		return super.onMenuItemSelected(featureId, item);
	}
	
	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		Intent courseIntent;
		Course course = (Course) this.listViewCourse.getAdapter().getItem(position);
		this.lastPositionClicked = position;
		
		if (course != null) {
			courseIntent = new Intent("COURSE_ACTIVITY");
			courseIntent.putExtra(CourseActivity.ARG_COURSE, course);
			courseIntent.putExtra(LoginActivity.ARG_USER, this.user);
			
			startActivityForResult(courseIntent, COURSE_ACTIVITY_CALL);
		}		
	}
	
	@SuppressWarnings("unchecked")
	private void loadCourseList(boolean wsResponse) {
		if (wsResponse) {
			if (this.inicioConsulta || this.courseAdapter == null) {
				this.courseAdapter = new CourseAdapter(this.getLayoutInflater());
				this.listViewCourse.setAdapter(this.courseAdapter);
				this.listViewCourse.setOnItemClickListener(this);
				registerForContextMenu(this.listViewCourse);
			}
			ArrayList<Course> auxCourseList = (ArrayList<Course>) this.nextPageCourseList.clone();
			for (Course course : auxCourseList) {
				try {
					this.courseAdapter.insert((Course)course.clone());
				} catch (CloneNotSupportedException e) {
					e.printStackTrace();
				}
			}
			this.courseAdapter.notifyDataSetChanged();
		} else {
			showToast(getResources().getString(R.string.connection_error));
		}
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (requestCode == CourseActivity.NEW_COURSE_REQUEST_CODE) {
			if (resultCode == Activity.RESULT_OK) {
				Course course = (Course)data.getExtras().get(CourseActivity.ARG_COURSE);
				this.courseAdapter.getCourseList().add(course);
				this.courseAdapter.notifyDataSetChanged();
				//TODO implement here a web service call to update the course
			} else if (resultCode == Activity.RESULT_CANCELED) {
				//do nothing
			}
		} else if (requestCode == MainActivity.COURSE_ACTIVITY_CALL) {
			if (data != null) {
				Bundle extras = data.getExtras();
				if (extras != null) {
					Course returnCourse = (Course)extras.get(CourseSectionFragment.ARG_COURSE);
					if (returnCourse != null) {
						this.courseAdapter.getCourseList().set(this.lastPositionClicked, returnCourse);
					}
				}
			}
		}
	}
	
	@Override
	public void returnServiceResponse(int serviceCode) {
		callBackHandler.sendEmptyMessage(0);
	}
	
	private void showToast(String msg) {
		this.toast.setText(msg);
		this.toast.cancel();
		this.toast.show();
	}
	
	public class GetAllCoursesCall implements Runnable {
		
		public static final int GET_ALL_COURSES_WEB_SERVICE = 200;
		
		@Override
		public void run() {
			SoapObject soap = new SoapObject(getResources().getString(
					R.string.namespace_webservice), getResources().getString(
							R.string.get_all_courses_webservice_operation));
			
//			soap.addProperty("chave", getResources().getString(R.string.chave_dalooc_webservice));
			
			SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(SoapEnvelope.VER11);
			
			envelope.setOutputSoapObject(soap);
			Log.d("DalOOC", "Calling DalOOCWebServices.getAllCourses");
			
			HttpTransportSE httpTransport = new HttpTransportSE(MainActivity.this.getUrlWebService(GetAllCoursesCall.GET_ALL_COURSES_WEB_SERVICE));
			
			try {
				MainActivity.this.getAllCoursesWebServiceResponseOk = false;
				httpTransport.call("", envelope);
				SoapObject results = (SoapObject) envelope.bodyIn;
				int count = results.getPropertyCount();
				if (count != 0) {
					MainActivity.this.nextPageCourseList = new ArrayList<Course>();
					for (int i = 0; i < count; i++) {
						BasicDBList courseDBList = new BasicDBList();
						courseDBList = (BasicDBList)JSON.parse(results.getProperty(i).toString());
						
						MainActivity.this.nextPageCourseList = new ArrayList<Course>();
						for (int j = 0; j < courseDBList.size(); j++) {
							MainActivity.this.nextPageCourseList.add(Parser.getCourseObject((BasicDBObject)courseDBList.get(j)));
						}
					}
				} else {
					MainActivity.this.nextPageCourseList.clear();
				}
				MainActivity.this.getAllCoursesWebServiceResponseOk = true;
			} catch (Exception e) {
				e.printStackTrace();
			}
			MainActivity.this.returnServiceResponse(GetAllCoursesCall.GET_ALL_COURSES_WEB_SERVICE);
		}

	}
}
