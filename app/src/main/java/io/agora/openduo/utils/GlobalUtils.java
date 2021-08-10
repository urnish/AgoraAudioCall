package io.agora.openduo.utils;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;

/**
 * Created by theonetech25 on 8/31/2015.
 */
public class GlobalUtils {

    Context mContext;

    public GlobalUtils(Context mCon) {
        this.mContext = mCon;
    }

    public void hideKeyboard(Activity noticeBoard) {
        View view = noticeBoard.getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) noticeBoard.getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    public synchronized boolean isNetworkAvailable() {
        boolean flag = false;

        if (checkNetworkAvailable()) {
            flag = true;

        } else {
            flag = false;
            //  Toast.makeText(mContext,"No network available!",Toast.LENGTH_SHORT).show();
            Log.d("", "No network available!");
        }
        return flag;
    }

    private boolean checkNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) mContext.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null;
    }

    public void setPrefBoolean(String Tag, Boolean isBool) {

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(mContext);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putBoolean(Tag, isBool);
        editor.apply();
    }

    public boolean getPrefBoolean(String Tag, Boolean isBool) {

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(mContext);
        return prefs.getBoolean(Tag, isBool);
    }

    public  void setPrefString(String Tag, String value) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(mContext);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(Tag, value);
        editor.apply();
    }
    public String getPrefString(String Tag) {
        String value = "";
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(mContext);
        return prefs.getString(Tag, value);
    }

    public void setPrefInteger(String Tag, Integer value) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(mContext);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putInt(Tag, value);
        editor.apply();
    }

    public void setPrefBitmap(String Tag, Bitmap value) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(mContext);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(Tag, encodeTobase64(value));
        editor.apply();

    }

    public void setPrefBitmapURI(String Tag, Uri value) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(mContext);
        SharedPreferences.Editor editor = prefs.edit();
        //editor.putString(Tag, encodeTobase64(value));
        editor.putString(Tag, String.valueOf(value));

        editor.apply();

    }

    public String getPrefBitmapURI(String Tag) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(mContext);
        String mImageUri = preferences.getString(Tag, null);
        return preferences.getString(Tag, mImageUri);
    }



    public static String encodeTobase64(Bitmap image) {
        Bitmap bitmap_image = image;
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap_image.compress(Bitmap.CompressFormat.PNG, 100, baos);
        byte[] b = baos.toByteArray();
        String imageEncoded = Base64.encodeToString(b, Base64.DEFAULT);

        return imageEncoded;
    }


    public Integer getPrefInteger(String Tag) {
        int value = 0;
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(mContext);
        return prefs.getInt(Tag, value);
    }


    public void showToast(String strToastMessage){
        Toast.makeText(mContext,strToastMessage,Toast.LENGTH_SHORT).show();
    }

    public static String convertStreamToString(InputStream is) {

        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        StringBuilder sb = new StringBuilder();

        String line = null;
        try {
            while ((line = reader.readLine()) != null) {
                sb.append(line + "\n");
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                is.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return sb.toString();
    }
    public static Bitmap getSampleBitmapFromFile(String bitmapFilePath, int reqWidth, int reqHeight) throws FileNotFoundException {
        // calculating image size
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;

        try {
            BitmapFactory.decodeStream(new FileInputStream(new File(bitmapFilePath)), null, options);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        try {
            int scale = calculateInSampleSize1(options, reqWidth, reqHeight);

            Log.e("scale",scale+"_");

            BitmapFactory.Options o2 = new BitmapFactory.Options();
            o2.inSampleSize = scale;

            return BitmapFactory.decodeStream(new FileInputStream(new File(bitmapFilePath)), null, o2);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        return null;
    }

    public static int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
        // Raw height and width of image
        final int height = options.outHeight;
        final int width = options.outWidth;

        int inSampleSize = 1;
        Log.e("Image_Actual",height+"_"+width+"_");
        Log.e("Image_Actual_Compress",reqWidth+"_"+reqHeight+"_");


        if (height > reqHeight || width > reqWidth) {

            // Calculate ratios of height and width to requested height and
            // width
            final int heightRatio = Math.round((float) height / (float) reqHeight);
            final int widthRatio = Math.round((float) width / (float) reqWidth);

            Log.e("Image_Actual_New",widthRatio+"_"+heightRatio+"_");

            // Choose the smallest ratio as inSampleSize value, this will
            // guarantee
            // a final image with both dimensions larger than or equal to the
            // requested height and width.
            inSampleSize = heightRatio < widthRatio ? heightRatio : widthRatio;
            Log.e("Image_inSampleSize",inSampleSize+"_");
        }

        return inSampleSize;
    }

    public static int calculateInSampleSize1(
            BitmapFactory.Options options, int reqWidth, int reqHeight) {
        // Raw height and width of image
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;


        if (height > reqHeight || width > reqWidth) {

            final int halfHeight = height / 2;
            final int halfWidth = width / 2;

            // Calculate the largest inSampleSize value that is a power of 2 and keeps both
            // height and width larger than the requested height and width.
            while ((halfHeight / inSampleSize) >= reqHeight
                    && (halfWidth / inSampleSize) >= reqWidth) {
                inSampleSize *= 2;
            }
        }

        return inSampleSize;
    }


    public  void showToastMessage(String strToastMessage){
        Toast.makeText(mContext,strToastMessage, Toast.LENGTH_SHORT).show();
    }

    public void saveImageToLocal(String strSourceLocation, String strTargetLocation) {
        try {
            File sourceLocation = new File(strSourceLocation);
            File targetLocation = new File(strTargetLocation);

            InputStream in = new FileInputStream(sourceLocation);
            OutputStream out = new FileOutputStream(targetLocation);

            // Copy the bits from instream to outstream
            byte[] buf = new byte[1024];
            int len;
            while ((len = in.read(buf)) > 0) {
                out.write(buf, 0, len);
            }
            in.close();
            out.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }


}
