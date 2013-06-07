package ca.dal.cs.dalooc.android.util;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.net.URL;
import java.net.URLConnection;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.util.ArrayList;
import java.util.List;

import android.os.AsyncTask;
import android.os.Environment;
import android.util.Log;
import android.webkit.MimeTypeMap;
import ca.dal.cs.dalooc.android.util.listener.OnDownloadDocumentDoneListener;

public class DownloadDocumentTask extends AsyncTask<String, Void, File> {
	
	private File documentFile;
	
	private List<OnDownloadDocumentDoneListener> listeners;
	
    public DownloadDocumentTask() {
        this.listeners = new ArrayList<OnDownloadDocumentDoneListener>();
    }

    protected File doInBackground(String... urls) {
        String urlDocument = urls[0];
        BufferedInputStream bis = null;
        FileOutputStream fos = null;
        
        try {
        	String extention = "." + MimeTypeMap.getFileExtensionFromUrl(urlDocument);
        	File documentDir = new File(Environment.getExternalStorageDirectory() + "/DalOOC/tmp");
        	documentDir.mkdirs();
        	this.documentFile = File.createTempFile("tmp", (extention.equals(".") ? null : extention), documentDir);

        	URL website = new URL(urlDocument);
        	URLConnection connection = website.openConnection();
        	ReadableByteChannel rbc = Channels.newChannel( connection.getInputStream());
        	fos = new FileOutputStream(this.documentFile);
        	long expectedSize = connection.getContentLength();
        	long transferedSize = 0L;
        	while( transferedSize < expectedSize ) {
        	   transferedSize +=
        	      fos.getChannel().transferFrom( rbc, transferedSize, 1 << 24 );
        	}
        	fos.close();
        } catch (Exception e ) {
        	Log.e("DownloadDocumentTask", e.getMessage());
        }
        return this.documentFile;
    }

    protected void onPostExecute(File result) {
    	fireOnDownloadDocumentDone(result);
    	this.documentFile.deleteOnExit();
    }

	private void fireOnDownloadDocumentDone(File result) {
		for (OnDownloadDocumentDoneListener dl : this.listeners) {
    		dl.onDownloadDocumentDone(result);
    	}
	}
    
    public void setOnDownloadDocumentDoneListener(OnDownloadDocumentDoneListener listener) {
    	this.listeners.add(listener);
    }
}
