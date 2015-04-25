
package com.lipengwei.music;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

import android.annotation.SuppressLint;
import android.app.Service;
import android.content.Intent;
import android.database.Cursor;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.net.Uri;
import android.os.IBinder;
import android.provider.MediaStore;
import android.util.Log;

public class MySevice extends Service {

    private final static String TAG = "MUSIC";
    private static MediaPlayer mMediaPlayer;
    // ArrayList for data get from music database
    private final ArrayList<String> mNameList = new ArrayList<String>();
    private final ArrayList<Integer> mSongId = new ArrayList<Integer>();
    private final ArrayList<Integer> mAlbumId = new ArrayList<Integer>();
    private final ArrayList<String> mData = new ArrayList<String>();
    private ArrayList<HashMap<String, Object>> mArrlist = new
            ArrayList<HashMap<String, Object>>();
    private int mNum;// the list number
    private String mPlayMode = "is_Order";// play mode
    private static int mCurr = -1;// current playing music position
    private final MusicBinder mBinder = new MusicBinder();

    /**
     * @param intent-that was used to bind to this service
     * @return an IBinder through which clients can call on to the service
     */
    @Override
    public IBinder onBind(Intent intent) {

        if (mMediaPlayer == null) {
            mMediaPlayer = new MediaPlayer();
        }
        Log.i(TAG, "Sevice has binder");
        queryData();// query data from database
        return mBinder;
    }
    
    @Override
    public void onDestroy() {
        super.onDestroy();
        if(mMediaPlayer != null)
            mMediaPlayer.release();
    }

    /**
     * format time as "mm:ss"
     * 
     * @param time-the time get from musicplayer
     * @return format time
     */
    @SuppressLint("SimpleDateFormat")
    private String formatFileTime(int time) {
        SimpleDateFormat hm = new SimpleDateFormat("mm:ss");
        return hm.format(time);
    }

    /**
     * define binder
     */
    class MusicBinder extends IMusic.Stub {

        /**
         * @return music play mode
         */
        public String getMode() {
            return mPlayMode;
        }

        /**
         * @param mode-set music mode by Activity
         */
        public void setMode(String mode) {
            mPlayMode = mode;
        }

        /**
         * play current music
         * 
         * @param crrent-play music position
         */
        public void play(int current) {
            mCurr = current;
            playMusic();
            Log.i(TAG, "curr=" + mCurr);
            Log.i(TAG, "Data=" + mData.get(mCurr));
        }

        /**
         * @return the duration of mediaplayer
         */
        public int getDuration() {
            return mMediaPlayer.getDuration();
        }

        /**
         * @return service current position of playing music
         */
        public int getCurrentItem() {
            return mCurr;
        }

        /**
         * set mediaplayer to progress
         * 
         * @param progress-the seekBar changed progress
         */
        public void seekBarTo(int progress) {
            mMediaPlayer.seekTo(progress);
        }

        /**
         * do when click nextButton
         */
        public void next() {
            if (++mCurr >= mNum) {
                mCurr = 0;
                playMusic();
            } else {
                playMusic();
            }
        }

        /**
         * do when click lastButton
         */
        public void last() {
            if (--mCurr < 0) {
                mCurr = mNum - 1;
                playMusic();
            } else {
                playMusic();
            }
        }

        /**
         * do when click play or pause button
         */
        public void pause() {
            if (mMediaPlayer.isPlaying()) {
                mMediaPlayer.pause();
            } else {
                mMediaPlayer.start();
            }
        }

        /**
         * @return _id of current song
         */
        public int getSongId() {
            return mSongId.get(mCurr);
        }

        /**
         * @return album_id of current song
         */
        public int getAlbumId() {
            return mAlbumId.get(mCurr);
        }

        /**
         * @return mediaplayer status
         */
        public boolean isPlaying() {
            return mMediaPlayer.isPlaying();
        }

        /**
         * @return mediaplayer current position
         */
        public int getTime() {
            return mMediaPlayer.getCurrentPosition();
        }

        /**
         * do when click deleteItem
         * 
         * @param postion-the music which will be deleted
         */
        public void deleteItem(int postion) {
            if (mCurr == postion) {
                return;
            }
            // delete the music file
            File file = new File(mData.get(postion));
            Uri uri = Uri.fromFile(file);
            file.delete();
            // tell mediascaner the file changed and change database
            Intent scanFileIntent = new Intent(
                    Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, uri);
            sendBroadcast(scanFileIntent);
            // remove view
            mData.remove(postion);
            mNameList.remove(postion);
        }

        /**
         * @param postion-music position
         * @return music title
         */
        public String getFileList(int postion) {
            if ((postion < 0) || (postion > mNum)) {
                return null;
            } else {
                return mNameList.get(postion);
            }
        }

        /**
         * play music
         */
        private void playMusic() {
            if (mMediaPlayer == null) {
                return;
            }
            mMediaPlayer.reset();
            try {
                mMediaPlayer.setDataSource(mData.get(mCurr));
                mMediaPlayer.prepare();
            } catch (IllegalArgumentException e) {
                e.printStackTrace();
            } catch (SecurityException e) {
                e.printStackTrace();
            } catch (IllegalStateException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            mMediaPlayer.start();
            // Listener mediaplayer completion
            mMediaPlayer.setOnCompletionListener(new OnCompletionListener() {
                public void onCompletion(MediaPlayer aeg0) {
                    if (!mMediaPlayer.isPlaying()) {
                        playFormat();
                        doSend();// send broadcast to activity to updata ui
                    }
                }
            });
        }

        /**
         * play music as play mode
         */
        private void playFormat() {
            if ("is_Sigle".equals(mPlayMode)) {
                playMusic();
            }
            if ("is_Order".equals(mPlayMode)) {
                next();
            }
            if ("is_Random".equals(mPlayMode)) {
                Random r = new Random();
                int idx = r.nextInt(mNum);
                mCurr = idx;
                playMusic();
            }
        }

        /**
         * send broadcast
         */
        private void doSend() {
            Intent sendIntent = new Intent("com.lipengwei.music.myservice");
            sendBroadcast(sendIntent);
        }
    }

    /**
     * query data from database
     */
    private void queryData() {
        mArrlist.clear();
        mData.clear();
        mNameList.clear();
        String[] mProjection = {
                MediaStore.Audio.Media.DATA,
                MediaStore.Audio.Media.TITLE,
                MediaStore.Audio.Media.ARTIST,
                MediaStore.Audio.Media.DURATION,
                MediaStore.Audio.Media._ID,
                MediaStore.Audio.Media.ALBUM_ID
        };
        String mSelectionClause =
                "mime_type in ('audio/mpeg','audio/mp4') and is_music=1";
        Cursor mCursor = getContentResolver().query(
                MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                mProjection,
                mSelectionClause,
                null,
                null);
        if ((null == mCursor) || (0 == mCursor.getCount())) {
            return;
        }
        mCursor.moveToFirst();
        mNum = mCursor.getCount();
        for (int j = 1; j <= mNum; j++) {
            HashMap<String, Object> map = new HashMap<String, Object>(6);
            String data = mCursor.getString(0);
            String title = mCursor.getString(1);
            String artist = mCursor.getString(2);
            int duration = mCursor.getInt(3);
            int songId = mCursor.getInt(4);
            int albumId = mCursor.getInt(5);

            map.put("TITLE", title);
            map.put("ARTIST", artist);
            map.put("DURATION", formatFileTime(duration));

            mCursor.moveToNext();
            mSongId.add(songId);
            mAlbumId.add(albumId);
            mData.add(data);
            mNameList.add(title);
            mArrlist.add(map);
        }
        Log.i("MUSIC", "sevice mArrlist" + mArrlist.isEmpty());
        PreferData.writeArrlist(mArrlist);
    }
}
