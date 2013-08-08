package ca.dal.cs.dalooc.android.util;

import java.io.BufferedInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import android.os.AsyncTask;
import android.util.Log;
import ca.dal.cs.dalooc.android.gui.listener.OnUploadFileTaskDoneListener;


public class UploadFileTask extends AsyncTask<String, Void, Boolean> {
	
	public final static int FILE_UPLOADED_SUCCESSFULY = 1;
	public final static int FILE_NOT_UPLOADED = 2;
	public static final int UPLOAD_DONE = 3;
	
	private List<OnUploadFileTaskDoneListener> onUploadFileTaskDoneListeners = new ArrayList<OnUploadFileTaskDoneListener>();

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
		int maxBufferSize = 1*1024*1024;

		try
		{
			BufferedInputStream fileInputStream = new BufferedInputStream(new FileInputStream(new File(pathToOurFile) ), 8192);
	
			URL url = new URL(urlServer);
			connection = (HttpURLConnection) url.openConnection();
	
			// Allow Inputs & Outputs
			connection.setDoInput(true);
			connection.setDoOutput(true);
			connection.setUseCaches(false);
	
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
			bytesRead = fileInputStream.read(buffer, 0, bufferSize);
	
			while (bytesRead > 0)
			{
				outputStream.write(buffer, 0, bufferSize);
				bytesAvailable = fileInputStream.available();
				bufferSize = Math.min(bytesAvailable, maxBufferSize);
				bytesRead = fileInputStream.read(buffer, 0, bufferSize);
			}
	
			outputStream.writeBytes(lineEnd);
			outputStream.writeBytes(twoHyphens + boundary + twoHyphens + lineEnd);

			// Responses from the server (code and message)
			serverResponseCode = connection.getResponseCode();
			serverResponseMessage = connection.getResponseMessage();
	
			fileInputStream.close();
			outputStream.flush();
			outputStream.close();
		} catch (Exception ex) {
			//Exception handling
			Log.e("UploadFileTask.java", ex.getMessage());
			fireOnUploadFileTaskDoneEvent(FILE_NOT_UPLOADED);
			return false;
		}
		if (serverResponseCode != 200) {
			Log.e("UploadFileTask.java", serverResponseMessage);
			fireOnUploadFileTaskDoneEvent(FILE_NOT_UPLOADED);
			return false;
		}
		fireOnUploadFileTaskDoneEvent(FILE_UPLOADED_SUCCESSFULY);
		return true;
		
	}
	
	@Override
	protected void onPostExecute(Boolean result) {
		// TODO Auto-generated method stub
		super.onPostExecute(result);
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
