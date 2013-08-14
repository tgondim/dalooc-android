package ca.dal.cs.dalooc.android.task;

import org.ksoap2.SoapEnvelope;
import org.ksoap2.serialization.SoapObject;
import org.ksoap2.serialization.SoapPrimitive;
import org.ksoap2.serialization.SoapSerializationEnvelope;
import org.ksoap2.transport.HttpTransportSE;

import android.os.AsyncTask;
import android.text.TextUtils;
import android.util.Log;
import android.widget.ImageView;
import ca.dal.cs.dalooc.android.gui.listener.OnGetStatusCallDoneListener;

public class GetAnswerStatusCallTask extends AsyncTask<String, Void, String> {
	
	public static final int GET_ANSWER_STATUS_WEB_SERVICE = 300;
	
	private OnGetStatusCallDoneListener onGetAnswerStatusCallDoneListener;
	
	private int position;
	
	private ImageView ivIcon;
	
	private static final String LOG_TAG = "GetAnswerStatusCallTask";
	
	
	public GetAnswerStatusCallTask(ImageView ivIcon, int position) {
		this.ivIcon = ivIcon;
		this.position = position;
	}
	
    protected String doInBackground(String... urls) {
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
		Log.d(LOG_TAG, "Calling DalOOCServices/TestAnswerRepository/getAnswerStatus");
		
		HttpTransportSE httpTransport = new HttpTransportSE(webServiceUrl);
		
		try {
			httpTransport.call("", envelope);
			SoapObject results = (SoapObject) envelope.bodyIn;
			int count = results.getPropertyCount();
			if (count != 0) {
				String returnString = ((SoapPrimitive)results.getProperty("return")).toString();
				if (!TextUtils.isEmpty(returnString)) {
					return returnString;
				}
			} 
		} catch (Exception e) {
			Log.e(LOG_TAG, e.toString());
		}
        
        return "none";
    }

    @Override
    protected void onPostExecute(String result) {
    	fireOnGetAnswerStatusCallDoneEvent(result);
    }
    
    public void setOnGetAnswerStatusCallDoneListener(OnGetStatusCallDoneListener onGetAnswerStatusCallDoneListener) {
		this.onGetAnswerStatusCallDoneListener = onGetAnswerStatusCallDoneListener;
	}
    
    private void fireOnGetAnswerStatusCallDoneEvent(String resultString) {
		if (this.onGetAnswerStatusCallDoneListener != null) {
			this.onGetAnswerStatusCallDoneListener.onGetStatusCallResponse(resultString, this.ivIcon, null, this.position);
		}
	}
}
