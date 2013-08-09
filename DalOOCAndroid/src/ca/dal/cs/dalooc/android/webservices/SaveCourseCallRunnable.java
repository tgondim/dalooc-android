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

public class SaveCourseCallRunnable implements Runnable {

	private static final String LOG_TAG = "SaveCourseCallRunnable";
	
	public static final int SAVE_COURSE_WEB_SERVICE = 100;
	
	private Course course;
	private OnSaveCourseCallDoneListener onSaveCourseCallDoneListener;
	private Context context;
	
	private boolean isLoading;
//	private boolean saveCourseWebServiceResponseOk;
	
	public SaveCourseCallRunnable(Course course, Context context) {
		super();
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
		Log.d(SaveCourseCallRunnable.LOG_TAG, "Calling DalOOCWebServices.saveCourse");
		
		HttpTransportSE httpTransport = new HttpTransportSE(this.onSaveCourseCallDoneListener.getUrlWebService(SaveCourseCallRunnable.SAVE_COURSE_WEB_SERVICE));
		
		try {
//			this.saveCourseWebServiceResponseOk = false;
			httpTransport.call("", envelope);
			SoapObject results = (SoapObject) envelope.bodyIn;
			int count = results.getPropertyCount();
			if (count != 0) {
				//TODO deal with de answer
			} else {
//				MainActivity.this.nextPageCourseList.clear();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		this.isLoading = false;
		fireOnSaveCourseCallDoneEvent();
	}
	
	private void fireOnSaveCourseCallDoneEvent() {
		if (this.onSaveCourseCallDoneListener != null) {
			this.onSaveCourseCallDoneListener.returnServiceResponse(SaveCourseCallRunnable.SAVE_COURSE_WEB_SERVICE);
		}
	}
	
	public void setOnSaveCourseCallDoneListener(OnSaveCourseCallDoneListener onSaveCourseCallDoneListener) {
		this.onSaveCourseCallDoneListener = onSaveCourseCallDoneListener;
	}
	
	public boolean isLoading() {
		return this.isLoading;
	}
}
