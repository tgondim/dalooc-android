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

public class UpdateCourseCallRunnable implements Runnable {

	private static final String LOG_TAG = "UpdateCourseCallRunnable";
	
	public static final int UPDATE_COURSE_WEB_SERVICE = 500;
	
	private Course course;
	private OnUpdateCourseCallDoneListener onUpdateCourseCallDoneListener;
	private Context context;
	
	private boolean isLoading;
//	private boolean saveCourseWebServiceResponseOk;
	
	public UpdateCourseCallRunnable(Course course, Context context) {
		super();
		this.course = course;
		this.context = context;
	}
	
	@Override
	public void run() {
		
		this.isLoading = true;
		SoapObject soap = new SoapObject(this.context.getResources().getString(R.string.namespace_webservice), 
			 							this.context.getResources().getString(R.string.update_course_webservice_operation));
		
//		soap.addProperty("chave", ((Activity)this.callBack).getResources().getString(R.string.chave_dalooc_webservice));
		
		String courseString = Parser.getCourseDBObject(this.course).toString();
		soap.addProperty("courseId", this.course.getId());
		soap.addProperty("courseString", courseString);
		
		SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(SoapEnvelope.VER11);
		
		envelope.setOutputSoapObject(soap);
		Log.d(UpdateCourseCallRunnable.LOG_TAG, "Calling DalOOCWebServices.updateCourse");
		
		HttpTransportSE httpTransport = new HttpTransportSE(this.onUpdateCourseCallDoneListener.getUrlWebService(UpdateCourseCallRunnable.UPDATE_COURSE_WEB_SERVICE));
		
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
		fireOnUpdateCourseCallDoneEvent();
	}
	
	private void fireOnUpdateCourseCallDoneEvent() {
		if (this.onUpdateCourseCallDoneListener != null) {
			this.onUpdateCourseCallDoneListener.returnServiceResponse(UpdateCourseCallRunnable.UPDATE_COURSE_WEB_SERVICE);
		}
	}
	
	public void setOnUpdateCourseCallDoneListener(OnUpdateCourseCallDoneListener onUpdateCourseCallDoneListener) {
		this.onUpdateCourseCallDoneListener = onUpdateCourseCallDoneListener;
	}
	
	public boolean isLoading() {
		return this.isLoading;
	}
}
