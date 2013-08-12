package ca.dal.cs.dalooc.android.webservice;

import org.ksoap2.SoapEnvelope;
import org.ksoap2.serialization.SoapObject;
import org.ksoap2.serialization.SoapSerializationEnvelope;
import org.ksoap2.transport.HttpTransportSE;

import android.content.Context;
import android.util.Log;
import ca.dal.cs.dalooc.android.R;
import ca.dal.cs.dalooc.model.TestAnswer;
import ca.dal.cs.dalooc.webservice.util.Parser;

public class SaveTestAnswerCallRunnable implements Runnable {

	private static final String LOG_TAG = "SaveTestAnswerCallRunnable";
	
	public static final int SAVE_TEST_ANSWER_WEB_SERVICE = 200;
	
	private TestAnswer testAswer;
	private OnWebServiceCallDoneListener onWebServiceCallDoneListener;
	private Context context;
	
	private boolean isLoading;
	
	public SaveTestAnswerCallRunnable(TestAnswer testAnswer, Context context) {
		super();
		this.testAswer = testAnswer;
		this.context = context;
	}
	
	@Override
	public void run() {
		
		this.isLoading = true;
		SoapObject soap = new SoapObject(this.context.getResources().getString(R.string.namespace_webservice), 
			 							this.context.getResources().getString(R.string.save_test_answer_webservice_operation));
		
//		soap.addProperty("chave", ((Activity)this.callBack).getResources().getString(R.string.chave_dalooc_webservice));
		
		String testAnswerString = Parser.getTestAnswerDBObject(this.testAswer).toString();
		soap.addProperty("testAnswerString", testAnswerString);
		
		SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(SoapEnvelope.VER11);
		
		envelope.setOutputSoapObject(soap);
		Log.d(SaveTestAnswerCallRunnable.LOG_TAG, "Calling DalOOCServices.saveTestAnswer");
		
		HttpTransportSE httpTransport = new HttpTransportSE(this.onWebServiceCallDoneListener.getUrlWebService(SaveTestAnswerCallRunnable.SAVE_TEST_ANSWER_WEB_SERVICE));
		
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
		fireOnWebServiceCallDoneEvent(true);
	}
	
	private void fireOnWebServiceCallDoneEvent(boolean resultOk) {
		if (this.onWebServiceCallDoneListener != null) {
			this.onWebServiceCallDoneListener.returnServiceResponse(SaveTestAnswerCallRunnable.SAVE_TEST_ANSWER_WEB_SERVICE, resultOk);
		}
	}
	
	public void setOnWebServiceCallDoneListener(OnWebServiceCallDoneListener onWebServiceCallDoneListener) {
		this.onWebServiceCallDoneListener = onWebServiceCallDoneListener;
	}
	
	public boolean isLoading() {
		return this.isLoading;
	}
}
