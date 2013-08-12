package ca.dal.cs.dalooc.android.task;

import org.ksoap2.SoapEnvelope;
import org.ksoap2.serialization.SoapObject;
import org.ksoap2.serialization.SoapPrimitive;
import org.ksoap2.serialization.SoapSerializationEnvelope;
import org.ksoap2.transport.HttpTransportSE;

import android.os.AsyncTask;
import android.util.Log;
import ca.dal.cs.dalooc.android.gui.listener.OnWebServiceCallDoneListener;
import ca.dal.cs.dalooc.model.TestAnswer;
import ca.dal.cs.dalooc.webservice.util.Parser;

public class SaveTestAnswerCallTask extends AsyncTask<String, Void, Boolean> {
	
	private static final String LOG_TAG = "SaveTestAnswerCallTask";
	
	public static final int SAVE_TEST_ANSWER_WEB_SERVICE = 596;
	
	private OnWebServiceCallDoneListener onWebServiceCallDoneListener;

	private TestAnswer testAswer;
	
	public SaveTestAnswerCallTask(TestAnswer testAswer) {
		this.testAswer = testAswer;
	}
	
    protected Boolean doInBackground(String... urls) {
    	String webServiceUrl = urls[0];
        String webServiceNameSpace = urls[1];
        String webServiceOperation = urls[2];

        SoapObject soap = new SoapObject(webServiceNameSpace, webServiceOperation);

		//soap.addProperty("chave", ((Activity)this.callBack).getResources().getString(R.string.chave_dalooc_webservice));
		
        String testAnswerString = Parser.getTestAnswerDBObject(this.testAswer).toString();
		soap.addProperty("testAnswerString", testAnswerString);
		
		SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(SoapEnvelope.VER11);
		
		envelope.setOutputSoapObject(soap);
		Log.d(LOG_TAG, "Calling DalOOCServices/TestAnswerRepository/saveTestAnswer");
		
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
			this.onWebServiceCallDoneListener.returnServiceResponse(SAVE_TEST_ANSWER_WEB_SERVICE, resultOk);
		}
	}
}
