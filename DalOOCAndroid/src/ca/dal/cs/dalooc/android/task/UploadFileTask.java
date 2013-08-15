package ca.dal.cs.dalooc.android.task;

import java.io.BufferedInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import android.os.AsyncTask;
import android.util.Log;
import android.widget.TextView;
import ca.dal.cs.dalooc.android.gui.listener.OnUploadFileTaskDoneListener;
import ca.dal.cs.dalooc.android.util.General;


public class UploadFileTask extends AsyncTask<String, Integer, Boolean> {
	
	private final static String LOG_TAG = "UploadFileTask";
	
	public final static int FILE_UPLOADED_SUCCESSFULY = 1;
	
	public final static int FILE_NOT_UPLOADED = 2;
	
	public static final int UPLOAD_DONE = 3;
	
	private List<OnUploadFileTaskDoneListener> onUploadFileTaskDoneListeners = new ArrayList<OnUploadFileTaskDoneListener>();
	
	private TextView tvUploadStatusMessage;

	public UploadFileTask(TextView tvUploadStatusMessage) {
		this.tvUploadStatusMessage = tvUploadStatusMessage;
	}
	
	@Override
	protected Boolean doInBackground(String... params) {
		HttpURLConnection connection = null;
		DataOutputStream outputStream = null;
		
		int serverResponseCode = -1;
		String serverResponseMessage = "";

		String pathToOurFile = params[0];
		String fileFolder = params[1];
		String fileId = params[2];
		String urlServer = params[3];
		String lineEnd = "\r\n";
		String twoHyphens = "--";
		String boundary =  "*****";

		int bytesRead, bytesAvailable, bufferSize;
		byte[] buffer;
		int maxBufferSize = 1*1024;
//		int maxBufferSize = 1*1024*1024;

		try
		{
			FileInputStream fileInputStream = new FileInputStream(new File(pathToOurFile) );
	
			URL url = new URL(urlServer);
			connection = (HttpURLConnection) url.openConnection();
	
			// Allow Inputs & Outputs
			connection.setDoInput(true);
			connection.setDoOutput(true);
			connection.setUseCaches(false);
			connection.setChunkedStreamingMode(1024);
	
			// Enable POST method
			connection.setRequestMethod("POST");
	
			connection.setRequestProperty("Connection", "Keep-Alive");
			connection.setRequestProperty("Content-Type", "multipart/form-data;boundary="+boundary);
	
			outputStream = new DataOutputStream( connection.getOutputStream() );
			outputStream.writeBytes(twoHyphens + boundary + lineEnd);
			outputStream.writeBytes("Content-Disposition: form-data; name=\"fileFolder\"" + lineEnd + lineEnd + fileFolder + lineEnd);
			outputStream.writeBytes(twoHyphens + boundary + lineEnd);
			outputStream.writeBytes("Content-Disposition: form-data; name=\"uploadedFile\";filename=\"" + General.getIdFileName(pathToOurFile, fileId) +"\"" + lineEnd);
			outputStream.writeBytes(lineEnd);
	
			bytesAvailable = fileInputStream.available();
			bufferSize = Math.min(bytesAvailable, maxBufferSize);
			buffer = new byte[bufferSize];
	
			// Read file
//			bytesRead = fileInputStream.read(buffer, 0, bufferSize);
//	
//			while (bytesRead > 0)
//			{
//				outputStream.write(buffer, 0, bufferSize);
//				bytesAvailable = fileInputStream.available();
//				bufferSize = Math.min(bytesAvailable, maxBufferSize);
//				bytesRead = fileInputStream.read(buffer, 0, bufferSize);
////				publishProgress((bytesRead/(double)bytesAvailable)*100);
//			}
			
			// Read file
	        bytesRead = fileInputStream.read(buffer, 0, bufferSize);
	        int totalBytes = 0;
	        Log.e("Image length", bytesAvailable + "");
	        try {
	            while (bytesRead > 0) {
	            	totalBytes += bytesRead;
	                try {
	                    outputStream.write(buffer, 0, bufferSize);
	                } catch (OutOfMemoryError e) {
	                    e.printStackTrace();
//	                    response = "outofmemoryerror";
	                    fileInputStream.close();
	    	            outputStream.flush();
	    	            outputStream.close();
	    	            outputStream = null;
	                    return false;
	                }
	                bytesAvailable = fileInputStream.available();
	                bufferSize = Math.min(bytesAvailable, maxBufferSize);
	                bytesRead = fileInputStream.read(buffer, 0, bufferSize);
	                publishProgress((totalBytes/1024)/1024);
	            }
	        } catch (Exception e) {
	            e.printStackTrace();
//	            response = "error";
	            fileInputStream.close();
	            outputStream.flush();
	            outputStream.close();
	            outputStream = null;
	            return false;
	        }
	
//	        publishProgress(100);
			outputStream.writeBytes(lineEnd);
			outputStream.writeBytes(twoHyphens + boundary + twoHyphens + lineEnd);

			// Responses from the server (code and message)
			serverResponseCode = connection.getResponseCode();
			serverResponseMessage = connection.getResponseMessage();
			
			Log.i("Server Response Code ", "" + serverResponseCode);
	        Log.i("Server Response Message", serverResponseMessage);

//	        if (serverResponseCode == 200) {
//	            response = "true";
//	        }

	        DateFormat df = new SimpleDateFormat("yyyy_MM_dd_HH:mm:ss");
	        String CDate = null;
	        Date serverTime = new Date(connection.getDate());
	        try {
	            CDate = df.format(serverTime);
	        } catch (Exception e) {
	            e.printStackTrace();
	            Log.e("Date Exception", e.getMessage() + " Parse Exception");
	        }
	        Log.i("Server Response Time", CDate + "");

			fileInputStream.close();
			outputStream.flush();
			outputStream.close();
		} catch (Exception ex) {
			//Exception handling
			Log.e(LOG_TAG, ex.toString());
			fireOnUploadFileTaskDoneEvent(FILE_NOT_UPLOADED);
			return false;
		}
		if (serverResponseCode != 200) {
			Log.e(LOG_TAG, serverResponseMessage);
			fireOnUploadFileTaskDoneEvent(FILE_NOT_UPLOADED);
			return false;
		}
		return true;
		
	}
	
	@Override
	protected void onProgressUpdate(Integer... values) {
		this.tvUploadStatusMessage.setText(values[0] + "Mb Uploaded");
	}
	
	protected void onPostExecute(Boolean result) {
		if (result) {
			fireOnUploadFileTaskDoneEvent(FILE_UPLOADED_SUCCESSFULY);
		} else {
			fireOnUploadFileTaskDoneEvent(FILE_NOT_UPLOADED);
		}
	}
	
	public void setOnUploadFileTaskDoneListener(OnUploadFileTaskDoneListener onUploadFileTaskDoneListener) {
		this.onUploadFileTaskDoneListeners.add(onUploadFileTaskDoneListener);	
	}
	
	private void fireOnUploadFileTaskDoneEvent(int returnCode) {
		for (OnUploadFileTaskDoneListener onFileTaskDoneListener : this.onUploadFileTaskDoneListeners) {
			onFileTaskDoneListener.onUploadFileTaskDone(returnCode);
		}
	}

}
