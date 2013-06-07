package ca.dal.cs.dalooc.android.util;

import android.graphics.Bitmap;
import android.media.ThumbnailUtils;
import android.os.AsyncTask;
import android.os.Environment;
import android.provider.MediaStore;
import android.widget.ImageView;

public class DownloadVideoIconTask extends AsyncTask<String, Void, Bitmap> {
	
	ImageView bmImage;

    public DownloadVideoIconTask(ImageView bmImage) {
        this.bmImage = bmImage;
    }

    protected Bitmap doInBackground(String... urls) {
        String urldisplay = urls[0];
//        String urldisplay = this.url;
        Bitmap mIcon11 = ThumbnailUtils.createVideoThumbnail(Environment.getExternalStorageDirectory() + urldisplay, MediaStore.Video.Thumbnails.MICRO_KIND);
        return mIcon11;
    }

    protected void onPostExecute(Bitmap result) {
        //pDlg.dismiss();
    	if (result != null) {
    		bmImage.setImageBitmap(result);
    	}
    }
}