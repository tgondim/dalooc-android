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
import android.widget.TextView;
import ca.dal.cs.dalooc.android.gui.listener.OnGetStatusCallDoneListener;

public class GetLearningObjectStatusCallTask extends AsyncTask<String, Void, String> {
	
	public static final int GET_LEARNING_OBJECT_STATUS_WEB_SERVICE = 970;
	
	private OnGetStatusCallDoneListener onGetStatusCallDoneListener;
	
	private int position;
	
	private ImageView ivIcon;
	
	private TextView txtLearningObjectName;
	
	private static final String LOG_TAG = "GetLearningObjectStatusCallTask";
	
	
	public GetLearningObjectStatusCallTask(ImageView ivIcon, TextView txtLearningObjectName, int position) {
		this.ivIcon = ivIcon;
		this.position = position;
		this.txtLearningObjectName = txtLearningObjectName;
	}
	
    protected String doInBackground(String... urls) {
    	String webServiceUrl = urls[0];
        String webServiceNameSpace = urls[1];
        String webServiceOperation = urls[2];
        String userId = urls[3];
        String courseId = urls[4];
        String learningObjectIndex = urls[5];

        SoapObject soap = new SoapObject(webServiceNameSpace, webServiceOperation);

		//soap.addProperty("chave", ((Activity)this.callBack).getResources().getString(R.string.chave_dalooc_webservice));
		
		soap.addProperty("userId", userId);
		soap.addProperty("courseId", courseId);
		soap.addProperty("learningObjectIndex", learningObjectIndex);
		
		SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(SoapEnvelope.VER11);
		
		envelope.setOutputSoapObject(soap);
		Log.d(LOG_TAG, "Calling DalOOCServices/CourseRepository/getLearningObjectStatus");
		
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
        
        return "0";
    }

    @Override
    protected void onPostExecute(String result) {
    	fireOnGetStatusCallDoneEvent(result);
    }
    
    public void setOnGetStatusCallDoneListener(OnGetStatusCallDoneListener onGetStatusCallDoneListener) {
		this.onGetStatusCallDoneListener = onGetStatusCallDoneListener;
	}
    
    private void fireOnGetStatusCallDoneEvent(String resultString) {
		if (this.onGetStatusCallDoneListener != null) {
			this.onGetStatusCallDoneListener.onGetStatusCallResponse(resultString, this.ivIcon, this.txtLearningObjectName, this.position);
		}
	}
}
