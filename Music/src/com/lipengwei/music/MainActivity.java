
package com.lipengwei.music;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.provider.MediaStore;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.database.Cursor;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Toast;

import com.lipengwei.music.R;

/**
 * @author author-pengwei.li@tcl.com
 * @version version-v1.0 This class is the mainActivity when the application
 *          start
 */
public class MainActivity extends Activity {

    public final static String POSTION = "CUR_POSTION";
    // listview current click item position
    public static String showUser;// fragment show information
    public static String showEmail;

    private final static String TAG = "MUSIC";
    private static ArrayList<HashMap<String, Object>> mArrlist = new
            ArrayList<HashMap<String, Object>>();
    private MBroadcastReceiver mBroadcastReceiver = null;
    private IntentFilter mIntentFilter = null;
    private int mCurrentListItem;
    private static int mDeletePosition;
    private ListView mListView;
    private Button mCurrMusicButton;
    private String mCurrMusicName;

    private boolean mIsLogin = false;// sign state
    private boolean mIsFragmentShow = false;// fragment state

    private Fragment mNewFragment;
    private IMusic mIMusic;// Service client when bind the service

    /**
     * Called when the activity is first created.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mListView = (ListView) findViewById(R.id.listView);
        mCurrMusicButton = (Button) findViewById(R.id.crrentMusic);
        mNewFragment = new MyFragment();
        // bind MyService
        Intent intent = new Intent(this, MySevice.class);
        this.startService(intent);
        bindService(intent, conn, BIND_AUTO_CREATE);
        // get fragment information from LoginActivity
        Intent signin = this.getIntent();
        showUser = signin.getStringExtra("curentueser");
        showEmail = signin.getStringExtra("curentemail");
        // register BroadcastReceiver
        mBroadcastReceiver = new MBroadcastReceiver();
        mIntentFilter = new IntentFilter("com.lipengwei.music.myservice");
        mIntentFilter.addAction("com.lipengwei.music.querydatafinish");
        this.registerReceiver(mBroadcastReceiver, mIntentFilter);
        // register ListView item clicklistener
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                    int position, long itemId) {
                mCurrentListItem = position;
                startSecActivity();
                Log.i(TAG, "currentItem=" + mCurrentListItem);
            }
        });
        mListView.setOnItemLongClickListener(
                new AdapterView.OnItemLongClickListener() {
                    @Override
                    public boolean onItemLongClick(AdapterView<?> parent, View view,
                            int position, long itemId) {
                        mDeletePosition = position;
                        showDialog();
                        return true;
                    }
                });
    }

    /**
     * start current playing music when click the button
     * 
     * @param view-the button view
     */
    public void onClick(View view) {

        if ((mCurrMusicButton.getText()).equals("")) {
        } else {
            startSecActivity();
        }

    }

    /**
     * remove fragment and Logout when click signout
     */
    public void clickQuit() {
        FragmentTransaction transactionQuit = getFragmentManager().
                beginTransaction();
        transactionQuit.remove(mNewFragment);
        transactionQuit.commit();
        // set Logout status
        PreferData.setisLogin(false);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle presses on the action bar items
        switch (item.getItemId()) {
            case R.id.login:
                // start LoginActivity
                startLogin();
                return true;
            case R.id.action_quit:
                // exit application
                exit();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        // get current music status to updata ui
        mCurrentListItem = PreferData.readCurr();
        Log.i(TAG, "mIMusic is " + mIMusic);
        if (mIMusic != null) {
            try {
                // updataUi
                mCurrentListItem = mIMusic.getCurrentItem();
                mCurrMusicName = mIMusic.getFileList(mCurrentListItem);
                mCurrMusicButton.setText(mCurrMusicName);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
        Log.i(TAG, "fistActivity currentItem = " + mCurrentListItem);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // unbindService
        unbindService(conn);
        this.unregisterReceiver(mBroadcastReceiver);
        Log.i(TAG, "onDestroy");
    }

    /**
     * start SecActivity when click music item to play
     */
    private void startSecActivity() {
        Intent startSec = new Intent(this, SecActivity.class);
        // send current click item position
        startSec.putExtra(POSTION, mCurrentListItem);
        startActivity(startSec);
    }

    /**
     * start LoginActivity when click signin item
     */
    private void startLogin() {
        // get Login status
        mIsLogin = PreferData.getisLogin();
        if (!mIsLogin) {
            Intent startLogin = new Intent(this, LoginActivity.class);
            startActivity(startLogin);
        } else {
            if (!mIsFragmentShow) {
                // add fragment and show
                View v = (View) findViewById(R.id.fragment_container);
                v.bringToFront();
                FragmentTransaction transaction = getFragmentManager().
                        beginTransaction();
                transaction.add(R.id.fragment_container, mNewFragment);
                transaction.addToBackStack(null);
                transaction.commit();
                mIsFragmentShow = true;
            } else {
                // remove fragment
                FragmentTransaction transactionRemove = getFragmentManager().
                        beginTransaction();
                transactionRemove.remove(mNewFragment);
                transactionRemove.commit();
                mIsFragmentShow = false;
            }
        }
    }

    /**
     * query data music data from mediastore
     */
    private void queryData() {
        mArrlist.clear();
        // selection media projection
        String[] mProjection = {
                MediaStore.Audio.Media.DATA,
                MediaStore.Audio.Media.TITLE,
                MediaStore.Audio.Media.ARTIST,
                MediaStore.Audio.Media.DURATION,
                MediaStore.Audio.Media._ID
        };
        // selection clause
        String mSelectionClause =
                "mime_type in ('audio/mpeg','audio/mp4') and is_music=1";
        // query data from database
        Cursor mCursor = getContentResolver().query(
                MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                mProjection,
                mSelectionClause,
                null,
                null);
        if ((null == mCursor) || (0 == mCursor.getCount())) {
            return;
        }
        // add music data to ArrayList
        mCursor.moveToFirst();
        int num = mCursor.getCount();
        for (int j = 1; j <= num; j++) {
            HashMap<String, Object> map = new HashMap<String, Object>(6);
            String title = mCursor.getString(1);
            String artist = mCursor.getString(2);
            int duration = mCursor.getInt(3);

            map.put("TITLE", title);
            map.put("ARTIST", artist);
            map.put("DURATION", formatFileTime(duration));

            mCursor.moveToNext();
            mArrlist.add(map);
        }
        // set ArrayList to Adapter of ListView
        SimpleAdapter adapter = new SimpleAdapter(getApplicationContext(),
                mArrlist, R.layout.item_style, new String[] {
                        "TITLE", "ARTIST",
                        "DURATION"
                }, new int[] {
                        R.id.music_name,
                        R.id.music_artist, R.id.music_duration
                });
        mListView.setAdapter(adapter);
    }

    /**
     * format the time as "mm:ss"
     * 
     * @param time-get from music system time
     * @return format the time as "mm:ss"
     */
    @SuppressLint("SimpleDateFormat")
    private String formatFileTime(int time) {

        SimpleDateFormat hm = new SimpleDateFormat("mm:ss");
        return hm.format(time);
    }

    /**
     * updata UI when succeed to delete a music
     */
    private void updataUI() {
        // remove from ListView
        mArrlist.remove(mDeletePosition);
        SimpleAdapter adapter = new SimpleAdapter(getApplicationContext(),
                mArrlist, R.layout.item_style, new String[] {
                        "TITLE", "ARTIST",
                        "DURATION"
                }, new int[] {
                        R.id.music_name,
                        R.id.music_artist, R.id.music_duration
                });
        mListView.setAdapter(adapter);
    }

    /**
     * show DialogFragment when long click music item
     */
    private void showDialog() {
        DialogFragment newFragment = new ItemDeleteDialog();
        newFragment.show(getFragmentManager(), "dialog");
        Log.i(TAG, "dialog");
    }

    /**
     * The action when click position of DialogFragment
     */
    private void doPositiveClick() {
        Log.i(TAG, "doPositiveClick");
        // refuse delete if the music is playing
        if (mDeletePosition == mCurrentListItem) {
            Toast.makeText(this, "The Music is Playing", Toast.LENGTH_SHORT).
                    show();
            return;
        }
        try {
            mIMusic.deleteItem(mDeletePosition);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        updataUI();
        Toast.makeText(this, "Delete Scuess", Toast.LENGTH_SHORT).show();
    }

    /**
     * The action when click Negative of DialogFragment
     */
    private void doNegativeClick() {
        Log.i(TAG, "doNegativeClick");
    }

    /**
     * exit application when click menuitem quit
     */
    private void exit() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this)
                .setIcon(R.drawable.ic_dialog_alert).setTitle("Exit");
        builder.setMessage("Do you want to exit").
                setPositiveButton(R.string.exit_ok, new DialogInterface.
                        OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                android.os.Process.killProcess(android.os.Process.myPid());
                            }
                        }).setNegativeButton(R.string.exit_cancle, new DialogInterface.
                        OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.cancel();
                            }
                        });
        AlertDialog alert = builder.create();
        alert.show();
    }

    /**
     * This class defined DialogFragment when long click item
     */
    @SuppressLint("ValidFragment")
    private class ItemDeleteDialog extends DialogFragment {

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {

            return new AlertDialog.Builder(getActivity())
                    .setIcon(R.drawable.ic_dialog_alert)
                    .setTitle(R.string.delete)
                    .setMessage("This music will be deleted")
                    .setPositiveButton(R.string.itemdelete_ok,
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog,
                                        int whichButton) {
                                    doPositiveClick();
                                }
                            }
                    )
                    .setNegativeButton(R.string.itemdelete_cancel,
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog,
                                        int whichButton) {
                                    doNegativeClick();
                                }
                            }
                    )
                    .create();
        }
    }

    /**
     * This class is defining BroadcastReceiver
     */
    private class MBroadcastReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.i(TAG, "getBroadcast");
            // the action get from service when music completion
            if ((intent.getAction()).equals("com.lipengwei.music.myservice")) {
                try {
                    // updata ui
                    mCurrentListItem = mIMusic.getCurrentItem();
                    mCurrMusicName = mIMusic.getFileList(mCurrentListItem);
                    mCurrMusicButton.setText(mCurrMusicName);
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }
            if ((intent.getAction()).
                    equals("com.lipengwei.music.querydatafinish")) {
                queryData();
            }
        }
    }

    /**
     * define a serviceconnection object and override the method
     */
    private ServiceConnection conn = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName componentName,
                IBinder iBinder) {
            mIMusic = IMusic.Stub.asInterface(iBinder);
            Log.i(TAG, "ServiceConnected");
            queryData();
            try {
                // updata ui when connect service
                mCurrentListItem = mIMusic.getCurrentItem();
                mCurrMusicName = mIMusic.getFileList(mCurrentListItem);
                mCurrMusicButton.setText(mCurrMusicName);
                // set current item
                PreferData.writeCurr(mCurrentListItem);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            Log.i(TAG, "ServiceDisConnected");
            mIMusic = null;
        }
    };
}
