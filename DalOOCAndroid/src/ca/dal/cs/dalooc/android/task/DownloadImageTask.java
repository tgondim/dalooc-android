package ca.dal.cs.dalooc.android.task;

import java.io.InputStream;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.ImageView;

public class DownloadImageTask extends AsyncTask<String, Void, Bitmap> {
	
	ImageView bmImage;

    public DownloadImageTask(ImageView bmImage) {
        this.bmImage = bmImage;
    }

    protected Bitmap doInBackground(String... urls) {
        String urldisplay = urls[0];
        
        int waitTime = 0;
        
        if (urls.length > 1) { 
        	try {
        		waitTime = Integer.valueOf(urls[1]);
        		if (waitTime > 0) {
        			Thread.sleep(waitTime);
        		}
        	} catch (Exception ignored) {}
        }
        
        Bitmap mIcon11 = null;
        try {
            InputStream in = new java.net.URL(urldisplay).openStream();
            mIcon11 = BitmapFactory.decodeStream(in);
        } catch (Exception e) {
            Log.e("Error", e.toString());
        }
        return mIcon11;
    }

    protected void onPostExecute(Bitmap result) {
        //pDlg.dismiss();
    	if (result != null) {
    		bmImage.setImageBitmap(result);
    	}
    }
}
