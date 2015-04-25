
package com.lipengwei.music;

import java.text.SimpleDateFormat;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;

import com.lipengwei.music.R;

public class SecActivity extends Activity {

    private final static String TAG = "MUSIC";
    private IMusic mIMusic;// Client of service
    private GestureDetector mGestureDetector;
    // View
    private TextView mMusicName;
    private TextView mCurrentTime;
    private TextView mDurationView;
    private ImageButton mPlayButton;
    private ImageButton mPlayMode;
    private SeekBar mSeekBar;
    private View v;

    private int mDuration;// the music duration
    private int mMode = 1;// the music play mode
    private int mBackgroundFlag = 1;// which background to set
    private int mCurrentListItem;// current playing position
    private boolean mIsPlaying;// media player status
    private String mServiceMode;// the play mode get from service
    private MBroadcastReceiver mBroadcastReceiver = null;
    private IntentFilter mIntentFilter = null;
    private Bitmap mArtBitmap = null;
    private int mSongId;// the music _id
    private int mAlbumId;// the music album_id
    private int mPosition;// seekBar postion to Music time

    /**
     * Called when the activity is first created
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sec);

        // bind service
        Intent intent = new Intent(this, MySevice.class);
        bindService(intent, conn, BIND_AUTO_CREATE);

        // get intent from MainActivity
        Intent MainIntent = this.getIntent();
        mCurrentListItem = MainIntent.getIntExtra(MainActivity.POSTION, -1);

        // register BroadcastReceiver
        mBroadcastReceiver = new MBroadcastReceiver();
        mIntentFilter = new IntentFilter("com.lipengwei.music.myservice");
        this.registerReceiver(mBroadcastReceiver, mIntentFilter);

        // get view
        mMusicName = (TextView) findViewById(R.id.musicname);
        mCurrentTime = (TextView) findViewById(R.id.currenttime);
        mDurationView = (TextView) findViewById(R.id.duration);
        mPlayButton = (ImageButton) findViewById(R.id.playbutton);
        mPlayMode = (ImageButton) findViewById(R.id.playformat);
        mSeekBar = (SeekBar) findViewById(R.id.seekBar1);
        v = (View) findViewById(R.id.play_activity);

        // Listener seekBar change
        mSeekBar.setOnSeekBarChangeListener(seekBarListener);
        // init GestureDetector
        initGestureDetector(this.getApplicationContext());
        // listener gesture touch
        v.setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (PreferData.isDeful()) {
                    mGestureDetector.onTouchEvent(event);
                }
                return true;
            }
        });
    }

    /**
     * Listener button click
     */
    public void onClick(View view) {
        int id = view.getId();
        switch (id) {
            case R.id.playbutton:
                try {
                    mIMusic.pause();
                    updataUI();
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
                break;
            case R.id.lastbutton:
                try {
                    mIMusic.last();
                    mCurrentListItem = mIMusic.getCurrentItem();
                    PreferData.writeCurr(mCurrentListItem);
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
                updataUI();
                break;
            case R.id.nextbutton:
                try {
                    mIMusic.next();
                    mCurrentListItem = mIMusic.getCurrentItem();
                    PreferData.writeCurr(mCurrentListItem);
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
                updataUI();
                break;
            case R.id.playformat:
                playMo();
                break;
            default:
                break;
        }
    }

    /**
     * set play mode when click playmode button
     */
    private void playMo() {
        switch (mMode) {
            case 1:
                mPlayMode.setImageResource(R.drawable.ic_mp_repeat_once_btn);
                mMode++;
                try {
                    mIMusic.setMode("is_Sigle");
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
                break;
            case 2:
                mPlayMode.setImageResource(R.drawable.ic_mp_shuffle_on_btn);
                mMode = 0;
                try {
                    mIMusic.setMode("is_Random");
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
                break;
            case 0:
                mPlayMode.setImageResource(R.drawable.ic_mp_repeat_all_btn);
                mMode++;
                try {
                    mIMusic.setMode("is_Order");
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
                break;
        }
    }

    /**
     * updata ui
     */
    private void updataUI() {
        try {
            // get music status and information from service
            mIsPlaying = mIMusic.isPlaying();
            mDuration = mIMusic.getDuration();
            mServiceMode = mIMusic.getMode();
            // set text to View
            mMusicName.setText(mIMusic.getFileList(mCurrentListItem));
            mDurationView.setText(getFileTime(mDuration));
            mCurrentTime.setText(getFileTime(mIMusic.getTime()));
            // set progress to seekbar
            mSeekBar.setProgress(mIMusic.getTime() * 1000 / mDuration);
            // call thread to updata UI
            handler.removeCallbacks(thread);
            handler.postDelayed(thread, 1000);
            // set current playmode button icon append to service play mode
            if (mIsPlaying) {
                mPlayButton.setImageResource(R.drawable.suspend_sel);
            } else {
                mPlayButton.setImageResource(R.drawable.play_sel);
            }
            if ("is_Order".equals(mServiceMode)) {
                mPlayMode.setImageResource(R.drawable.ic_mp_repeat_all_btn);
                mMode = 1;
            }
            if ("is_Sigle".equals(mServiceMode)) {
                mPlayMode.setImageResource(R.drawable.ic_mp_repeat_once_btn);
                mMode = 2;
            }
            if ("is_Random".equals(mServiceMode)) {
                mPlayMode.setImageResource(R.drawable.ic_mp_shuffle_on_btn);
                mMode = 0;
            }
            // get background set by user
            getBitmap();
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    /**
     * get background
     */
    private void getBitmap() {
        try {
            // get music -id and album_id
            mSongId = mIMusic.getSongId();
            mAlbumId = mIMusic.getAlbumId();
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        // get ArtBitmap
        mArtBitmap = PreferData.getArtwork(SecActivity.this, mSongId, mAlbumId);
        // set background to View
        v.setBackground(new BitmapDrawable(v.getResources(), mArtBitmap));
    }

    /**
     * the object of GestureDetector and override onFling
     * 
     * @param context-who call it
     */
    private void initGestureDetector(Context context) {
        mGestureDetector = new GestureDetector(context,
                new GestureDetector.SimpleOnGestureListener() {
                    @Override
                    public boolean onFling(MotionEvent e1, MotionEvent e2,
                            float velocityX, float velocityY) {

                        if (e1.getX() - e2.getX() > 50) {
                            Log.i(TAG, "to left");
                            switch (mBackgroundFlag) {
                                case 1:
                                    PreferData.setBackgroundFlag(2);
                                    mBackgroundFlag = 2;
                                    updataUI();
                                    break;
                                case 2:
                                    PreferData.setBackgroundFlag(3);
                                    mBackgroundFlag = 3;
                                    updataUI();
                                    break;
                                case 3:
                                    PreferData.setBackgroundFlag(1);
                                    mBackgroundFlag = 1;
                                    updataUI();
                                    break;
                                default:
                                    break;
                            }
                            return true;
                        } else if (e2.getX() - e1.getX() > 50) {
                            Log.i(TAG, "to right");
                            switch (mBackgroundFlag) {
                                case 1:
                                    PreferData.setBackgroundFlag(3);
                                    mBackgroundFlag = 3;
                                    updataUI();
                                    break;
                                case 2:
                                    PreferData.setBackgroundFlag(1);
                                    mBackgroundFlag = 1;
                                    updataUI();
                                    break;
                                case 3:
                                    PreferData.setBackgroundFlag(2);
                                    mBackgroundFlag = 2;
                                    updataUI();
                                    break;
                                default:
                                    break;
                            }
                            return true;
                        }
                        return super.onFling(e1, e2, velocityX, velocityY);
                    }
                });
    }

    /**
     * Format time as "mm:ss"
     * 
     * @param time-system time get from music
     */
    @SuppressLint("SimpleDateFormat")
    private String getFileTime(int time) {

        SimpleDateFormat hm = new SimpleDateFormat("mm:ss");
        return hm.format(time);
    }

    /**
     * play music when start
     */
    private void playMusic() {
        try {
            mIMusic.play(mCurrentListItem);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        // share current music position
        PreferData.writeCurr(mCurrentListItem);
        updataUI();
        Log.i(TAG, "play currentItem is " + mCurrentListItem);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbindService(conn);
    }

    // handler the new thread to updata UI
    Handler handler = new Handler();
    Thread thread = new Thread(new Runnable() {
        @Override
        public void run() {
            if (mIsPlaying) {
                handler.removeCallbacks(thread);
                updataUI();
                handler.postDelayed(thread, 1000);
            }
        }
    });

    // connect service
    private ServiceConnection conn = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName componentName,
                IBinder iBinder) {
            mIMusic = IMusic.Stub.asInterface(iBinder);
            try {
                if (mCurrentListItem == mIMusic.getCurrentItem()) {
                    updataUI();
                } else {
                    playMusic();
                }
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
        }
    };

    // define seek bar listener
    private OnSeekBarChangeListener seekBarListener = new
            OnSeekBarChangeListener() {

                @Override
                public void onProgressChanged(SeekBar seekBar, int progress,
                        boolean fromUser) {
                    if (fromUser) {
                        mPosition = mDuration * progress / 1000;
                        mCurrentTime.setText(getFileTime(mPosition));
                    }
                }

                @Override
                public void onStopTrackingTouch(SeekBar seekBar) {
                    try {
                        mIMusic.seekBarTo(mPosition);
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onStartTrackingTouch(SeekBar seekBar) {
                }
            };

    /**
     * this class define BroadcastReceiver
     */
    private class MBroadcastReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.i(TAG, "getBroadcast");
            try {
                mCurrentListItem = mIMusic.getCurrentItem();
                mMusicName.setText(mIMusic.getFileList(mCurrentListItem));
                PreferData.writeCurr(mCurrentListItem);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    }
}
