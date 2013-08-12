package ca.dal.cs.dalooc.android.webservice;

import org.ksoap2.SoapEnvelope;
import org.ksoap2.serialization.SoapObject;
import org.ksoap2.serialization.SoapPrimitive;
import org.ksoap2.serialization.SoapSerializationEnvelope;
import org.ksoap2.transport.HttpTransportSE;

import android.os.AsyncTask;
import android.util.Log;

public class HasAnsweredCorrectCallTask extends AsyncTask<String, Void, Boolean> {
	
	public static final int HAS_ANSWERED_CORRECT_WEB_SERVICE = 300;
	
	private OnWebServiceCallDoneListener onWebServiceCallDoneListener;
	
	private static final String LOG_TAG = "HasAnsweredCorrectCallTask";
	
    protected Boolean doInBackground(String... urls) {
    	String webServiceUrl = urls[0];
        String webServiceNameSpace = urls[1];
        String webServiceOperation = urls[2];
        String userId = urls[3];
        String courseId = urls[4];
        String learningObjectId = urls[5];
        String testQuestionId = urls[6];
        String optionId = urls[7];

        SoapObject soap = new SoapObject(webServiceNameSpace, webServiceOperation);

		//soap.addProperty("chave", ((Activity)this.callBack).getResources().getString(R.string.chave_dalooc_webservice));
		
		soap.addProperty("userId", userId);
		soap.addProperty("courseId", courseId);
		soap.addProperty("learningObjectId", learningObjectId);
		soap.addProperty("testQuestionId", testQuestionId);
		soap.addProperty("optionId", optionId);
		
		SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(SoapEnvelope.VER11);
		
		envelope.setOutputSoapObject(soap);
		Log.d(LOG_TAG, "Calling DalOOCServices/TestAnswerRepository/hasAnsweredCorrect");
		
		HttpTransportSE httpTransport = new HttpTransportSE(webServiceUrl);
		
		try {
			// this.saveCourseWebServiceResponseOk = false;
			httpTransport.call("", envelope);
			SoapObject results = (SoapObject) envelope.bodyIn;
			int count = results.getPropertyCount();
			if (count != 0) {
				if (((SoapPrimitive)results.getProperty("return")).toString().equals("true")) {
					return true;
				}
			} 
		} catch (Exception e) {
			e.printStackTrace();
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
			this.onWebServiceCallDoneListener.returnServiceResponse(HAS_ANSWERED_CORRECT_WEB_SERVICE, resultOk);
		}
	}
}
