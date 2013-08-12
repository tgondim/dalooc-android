package ca.dal.cs.dalooc.android.task;

import org.ksoap2.SoapEnvelope;
import org.ksoap2.serialization.SoapObject;
import org.ksoap2.serialization.SoapPrimitive;
import org.ksoap2.serialization.SoapSerializationEnvelope;
import org.ksoap2.transport.HttpTransportSE;

import ca.dal.cs.dalooc.android.gui.listener.OnWebServiceCallDoneListener;

import android.os.AsyncTask;
import android.util.Log;

public class RemoveCourseCallTask extends AsyncTask<String, Void, Boolean> {
	
	public static final int REMOVE_COURSE_WEB_SERVICE = 400;
	
	private OnWebServiceCallDoneListener onWebServiceCallDoneListener;
	
	private static final String LOG_TAG = "RemoveCourseCallTask";
	
    protected Boolean doInBackground(String... urls) {
    	String webServiceUrl = urls[0];
        String webServiceNameSpace = urls[1];
        String webServiceOperation = urls[2];
        String courseId = urls[3];

        SoapObject soap = new SoapObject(webServiceNameSpace, webServiceOperation);

		//soap.addProperty("chave", ((Activity)this.callBack).getResources().getString(R.string.chave_dalooc_webservice));
		
		soap.addProperty("courseId", courseId);
		
		SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(SoapEnvelope.VER11);
		
		envelope.setOutputSoapObject(soap);
		Log.d(LOG_TAG, "Calling DalOOCServices/CourseRepository/removeCourse");
		
		HttpTransportSE httpTransport = new HttpTransportSE(webServiceUrl);
		
		try {
			httpTransport.call("", envelope);
			SoapObject results = (SoapObject) envelope.bodyIn;
			int count = results.getPropertyCount();
			if (count != 0) {
				if (((SoapPrimitive)results.getProperty("return")).toString().equals("true")) {
					return true;
				}
			} 
		} catch (Exception e) {
			Log.e(LOG_TAG, e.getStackTrace().toString());
		}
        
        return false;
    }

    @Override
    protected void onPostExecute(Boolean result) {
    	fireOnWebServiceCallDoneEvent(result);
    }
    
    public void setOnWebServiceCallDoneListener(OnWebServiceCallDoneListener onWebServiceCallDoneListener) {
		this.onWebServiceCallDoneListener = onWebServiceCallDoneListener;
	}
    
    private void fireOnWebServiceCallDoneEvent(boolean resultOk) {
		if (this.onWebServiceCallDoneListener != null) {
			this.onWebServiceCallDoneListener.returnServiceResponse(REMOVE_COURSE_WEB_SERVICE, resultOk);
		}
	}
}
