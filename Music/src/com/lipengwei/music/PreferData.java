
package com.lipengwei.music;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.util.Log;
import com.lipengwei.music.R;

/**
 * @author pengwei.li@tcl.com
 * @version version-v1.0 This class is used to share data between Activity and
 *          service
 */
public class PreferData {

    private static int mCurrentItem = -1;// current playing music
    private static boolean mIsLogin = false;// Login status
    // uri of auio albumart
    private static final Uri mArtworkUri =
            Uri.parse("content://media/external/audio/albumart");
    private static final BitmapFactory.Options mBitmapOptions =
            new BitmapFactory.Options();
    private static boolean mIsDefult = false;// music album status
    private static int mBackgroundFlag = 1;// set default background
    private static ArrayList<HashMap<String, Object>> mArrlist = new
            ArrayList<HashMap<String, Object>>();

    /**
     * write ArrayList to share
     * 
     * @param Arrlist-the data to write
     */
    public static void writeArrlist(ArrayList<HashMap<String, Object>>
            Arrlist) {
        mArrlist = Arrlist;
        Log.i("MUSIC", "can write ");
    }

    /**
     * get ArrayList
     * 
     * @return ArrayList
     */
    public static ArrayList<HashMap<String, Object>> getArrlist() {
        Log.i("MUSIC", "mArrlist is empty " + mArrlist.isEmpty());
        return mArrlist;
    }

    /**
     * write current music position to share
     * 
     * @param curr-current music position
     */
    public static void writeCurr(int curr) {
        mCurrentItem = curr;
    }

    /**
     * get current music position
     * 
     * @return current music position
     */
    public static int readCurr() {
        return mCurrentItem;
    }

    /**
     * set default background
     * 
     * @param flag-which background to set
     */
    public static void setBackgroundFlag(int flag) {
        mBackgroundFlag = flag;
    }

    /**
     * @return background status
     */
    public static boolean isDeful() {
        return mIsDefult;
    }

    /**
     * @param islogin-set Login status
     */
    public static void setisLogin(boolean islogin) {
        mIsLogin = islogin;
    }

    /**
     * @return Login status
     */
    public static boolean getisLogin() {
        return mIsLogin;
    }

    /**
     * get music album art
     * 
     * @param context-the activity call the method
     * @param song_id-the music id
     * @param album_id-the music album_id
     * @return bitmap of album art
     */
    public static Bitmap getArtwork(Context context, long song_id,
            long album_id) {

        ContentResolver res = context.getContentResolver();
        Uri uri = ContentUris.withAppendedId(mArtworkUri, album_id);
        if (uri != null) {
            InputStream in = null;
            try {
                in = res.openInputStream(uri);
                mIsDefult = false;
                return BitmapFactory.decodeStream(in, null, mBitmapOptions);
            } catch (FileNotFoundException ex) {
                Bitmap bm = null;
                bm = getDefaultArtwork(context);// return default art
                mIsDefult = true;
                return bm;
            } finally {
                try {
                    if (in != null) {
                        in.close();
                    }
                } catch (IOException ex) {
                }
            }
        }

        return null;
    }

    /**
     * get default art
     * 
     * @param context
     * @return default art
     */
    static Bitmap getDefaultArtwork(Context context) {
        Bitmap mbitmap;
        BitmapFactory.Options opts = new BitmapFactory.Options();
        opts.inPreferredConfig = Bitmap.Config.ARGB_8888;
        // set different background from user
        switch (mBackgroundFlag) {
            case 1:
                mbitmap = BitmapFactory.decodeStream(
                        context.getResources().openRawResource(
                                R.drawable.default_album_1), null, opts);
                break;
            case 2:
                mbitmap = BitmapFactory.decodeStream(
                        context.getResources().openRawResource(
                                R.drawable.default_album_2), null, opts);
                break;
            case 3:
                mbitmap = BitmapFactory.decodeStream(
                        context.getResources().openRawResource(
                                R.drawable.default_album_3), null, opts);
                break;
            default:
                mbitmap = BitmapFactory.decodeStream(
                        context.getResources().openRawResource(
                                R.drawable.default_album_1), null, opts);
                break;
        }
        return mbitmap;
    }
}
