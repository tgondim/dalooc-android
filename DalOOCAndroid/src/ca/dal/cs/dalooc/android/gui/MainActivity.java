package ca.dal.cs.dalooc.android.gui;

import java.util.ArrayList;

import org.ksoap2.SoapEnvelope;
import org.ksoap2.serialization.SoapObject;
import org.ksoap2.serialization.SoapSerializationEnvelope;
import org.ksoap2.transport.HttpTransportSE;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Handler;
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
import ca.dal.cs.android.dalooc.R;
import ca.dal.cs.dalooc.android.control.CourseAdapter;
import ca.dal.cs.dalooc.android.webservices.GetAllCoursesCallBack;
import ca.dal.cs.dalooc.android.webservices.SaveCourseCall;
import ca.dal.cs.dalooc.android.webservices.SaveCourseCallBack;
import ca.dal.cs.dalooc.model.Course;
import ca.dal.cs.dalooc.model.User;
import ca.dal.cs.dalooc.webservice.util.Parser;

import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.util.JSON;

public class MainActivity extends Activity implements OnItemClickListener, GetAllCoursesCallBack, SaveCourseCallBack {

	private ListView listViewCourse;
	
	private boolean getAllCoursesWebServiceResponseOk;
//	private boolean erroConsulta;
	private boolean inicioConsulta;
//	private boolean loading;
	
	private User user;
	
	private CourseAdapter courseAdapter;
	
	private ArrayList<Course> nextPageCourseList;
	
	private Toast toast;
	
//	private String urlWebService;
	
//	private int currentIndex;
	
	private Handler callBackHandler = new Handler() {
		public void handleMessage(android.os.Message msg) {
			if (MainActivity.this.getAllCoursesWebServiceResponseOk) {				
				loadCourseList(MainActivity.this.getAllCoursesWebServiceResponseOk);
				MainActivity.this.getAllCoursesWebServiceResponseOk = false;
//				MainActivitys.this.impossivelCarregar.setVisibility(View.GONE);
				
//				LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
//				if (MainActivity.this.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
//					lp.weight = 1.8f;						
//				} else {
//					lp.weight = 6f;			
//				}
//				MainActivity.this.rlTopBar.setLayoutParams(lp);
				
//				MainActivity.this.erroConsulta = false;
//				MainActivity.this.lvListaJogos.removeFooterView(MainActivity.this.impossivelCarregar);
			}
			
			if ((MainActivity.this.courseAdapter == null) || (MainActivity.this.courseAdapter.getCount() == 0)){
//				MainActivity.this.impossivelCarregar.setVisibility(View.VISIBLE);
				
				LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
				if (MainActivity.this.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
					lp.weight = 1.3f;						
				} else {
					lp.weight = 4.3f;			
				}
//				MainActivity.this.rlTopBar.setLayoutParams(lp);
				
//				MainActivity.this.erroConsulta = true;
				MainActivity.this.inicioConsulta = true;
//				MainActivity.this.lvCourseList.addFooterView(MainActivity.this.impossivelCarregar);
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
		
		//for testing purposes
		//TODO here is where I save a course into DB through webservice
//		new Thread(new SaveCourseCall(getTempCourseList().get(0), this)).start();
		
		this.toast = Toast.makeText(this, "", Toast.LENGTH_LONG);
		
		this.user = (User)getIntent().getExtras().getSerializable(LoginActivity.ARG_USER);
		
		getCourses();
	}

	private void getCourses() {
		
		if (this.inicioConsulta) {
//			this.currentIndex = 0;
//			mostrarDialogEsperaConsultaWebService();
		}
		
		new Thread(new GetAllCoursesCall()).start();
	}
	
	@Override
	public String getUrlWebService(int serviceCode) {
		if (serviceCode == GetAllCoursesCall.GET_ALL_COURSES_WEB_SERVICE) {
			return getResources().getString(R.string.url_webservice) + "/" + getResources().getString(R.string.course_repository) + "/" + getResources().getString(R.string.get_all_courses_webservice_operation); 
//			return "http://192.168.0.3:8080/DalOOCWebServices/services/CourseRepository/getAllCourses";
//			return "http://10.0.2.2:8080/DalOOCWebServices/services/CourseRepository/getAllCourses";
//			return "http://tgondim.dyndns.info:8080/DalOOCWebServices/services/CourseRepository/getAllCourses";
		} else if (serviceCode == SaveCourseCall.SAVE_COURSE_WEB_SERVICE) {
			return getResources().getString(R.string.url_webservice) + "/" + getResources().getString(R.string.course_repository) + "/" + getResources().getString(R.string.save_course_webservice_operation); 
//			return "http://192.168.0.3:8080/DalOOCWebServices/services/CourseRepository/saveCourse";
//			return "http://10.0.2.2:8080/DalOOCWebServices/services/CourseRepository/saveCourse";
//			return "http://tgondim.dyndns.info:8080/DalOOCWebServices/services/CourseRepository/saveCourse";
		}
		return null;
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onMenuItemSelected(int featureId, MenuItem item) {
		switch (item.getOrder()) {
		case 100:
			break;
			
		case 200:
			Intent intent = new Intent(this, CourseEditActivity.class);
			intent.putExtra(LoginActivity.ARG_USER, this.user);
			
			startActivity(intent);
			break;
		}
		return super.onMenuItemSelected(featureId, item);
	}
	
	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		Intent courseIntent;
		Course course = (Course) this.listViewCourse.getAdapter().getItem(position);
		
		if (course != null) {
			courseIntent = new Intent("COURSE_ACTIVITY");
			courseIntent.putExtra(CourseActivity.ARG_COURSE, course);
			courseIntent.putExtra(LoginActivity.ARG_USER, this.user);
			
			startActivity(courseIntent);
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
//		this.txtLoadingCourseList.setVisibility(View.GONE);
	}
	
	@Override
	public void returnServiceResponse(int serviceCode) {
//		if (openDialog != null) {
//			openDialog.dismiss();
//		}
		callBackHandler.sendEmptyMessage(0);
	}
	
	private void showToast(String msg) {
		MainActivity.this.toast.setText(msg);
		MainActivity.this.toast.cancel();
		MainActivity.this.toast.show();
	};
	
	public class GetAllCoursesCall implements Runnable {
		
		public static final int GET_ALL_COURSES_WEB_SERVICE = 200;
		
		@Override
		public void run() {
//			MainActivity.this.loading = true;
			SoapObject soap = new SoapObject(getResources().getString(
					R.string.namespace_webservice), getResources().getString(
							R.string.get_all_courses_webservice_operation));
			
//			int pos = MainActivity.this.currentIndex;
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
			
//			MainActivity.this.loading = false;
			MainActivity.this.returnServiceResponse(GetAllCoursesCall.GET_ALL_COURSES_WEB_SERVICE);
		}

	}
}
