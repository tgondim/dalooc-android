package ca.dal.cs.dalooc.android.webservices;

import org.ksoap2.SoapEnvelope;
import org.ksoap2.serialization.SoapObject;
import org.ksoap2.serialization.SoapSerializationEnvelope;
import org.ksoap2.transport.HttpTransportSE;

import android.content.Context;
import android.util.Log;
import ca.dal.cs.dalooc.android.R;
import ca.dal.cs.dalooc.model.Course;
import ca.dal.cs.dalooc.webservice.util.Parser;

public class SaveCourseCall implements Runnable {

	public static final int SAVE_COURSE_WEB_SERVICE = 100;
	
	private Course course;
	private SaveCourseCallBack callBack;
	private Context context;
	
	private boolean isLoading;
//	private boolean saveCourseWebServiceResponseOk;
	
	public SaveCourseCall(Course course, SaveCourseCallBack callBack, Context context) {
		super();
		this.callBack = callBack;
		this.course = course;
		this.context = context;
	}
	
	@Override
	public void run() {
		
		this.isLoading = true;
		SoapObject soap = new SoapObject(this.context.getResources().getString(R.string.namespace_webservice), 
			 							this.context.getResources().getString(R.string.save_course_webservice_operation));
		
//		soap.addProperty("chave", ((Activity)this.callBack).getResources().getString(R.string.chave_dalooc_webservice));
		
		String courseString = Parser.getCourseDBObject(this.course).toString();
		soap.addProperty("courseString", courseString);
		
		SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(SoapEnvelope.VER11);
		
		envelope.setOutputSoapObject(soap);
		Log.d("DalOOC", "Calling DalOOCWebServices.saveCourse");
		
		HttpTransportSE httpTransport = new HttpTransportSE(this.callBack.getUrlWebService(SaveCourseCall.SAVE_COURSE_WEB_SERVICE));
		
		try {
//			this.saveCourseWebServiceResponseOk = false;
			httpTransport.call("", envelope);
			SoapObject results = (SoapObject) envelope.bodyIn;
			int count = results.getPropertyCount();
			if (count != 0) {

			} else {
//				MainActivity.this.nextPageCourseList.clear();
			}
//			MainActivity.this.getAllCoursesWebServiceResponseOk = true;
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		this.isLoading = false;
		this.callBack.returnServiceResponse(SaveCourseCall.SAVE_COURSE_WEB_SERVICE);
	}
	
	public boolean getisLoading() {
		return this.isLoading;
	}
}
