
package com.lipengwei.music;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;
import com.lipengwei.music.R;

/**
 * @author author-pengwei.li@tcl.com
 * @version version-v1.0 This class is Login Activity which edit the user name
 *          and password
 */
public class LoginActivity extends Activity {

    private EditText mUserName;// EditText use to edit user name
    private EditText mPassWor;// EditText use to edit user password
    private String mUserNa;// user name get from intent of NewAccount
    private String mPassW;// user password get from intent of NewAccount

    /**
     * Called when the activity is first created.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        getActionBar().setDisplayHomeAsUpEnabled(true);
        // get Intent from NewAccount.class to get user name and password
        Intent intent = this.getIntent();
        mUserNa = intent.getStringExtra(NewAccount.NAME);
        mPassW = intent.getStringExtra(NewAccount.PASS);
        // get EditText view
        mUserName = (EditText) findViewById(R.id.uesername);
        mPassWor = (EditText) findViewById(R.id.password);
        // set text if start by NewAccount
        mUserName.setText(mUserNa);
        mPassWor.setText(mPassW);
    }

    /**
     * Listener button click
     * 
     * @param view-the button view
     */
    public void onClick(View view) {
        int id = view.getId();
        switch (id) {
            case R.id.signin_button:
                // Sign in and start MainActivity
                signIn();
                Log.i("MUSIC", "Signin click");
                break;
            case R.id.newaccount_button:
                // start NewAccount Activity
                startRegister();
                break;
        }
    }

    /**
     * Listener checkbox isChecked
     * 
     * @param view-the CheckBox
     */
    public void onCheckboxClicked(View view) {
        boolean checked = ((CheckBox) view).isChecked();
        if (checked) {
            // set the password visible
            mPassWor.setInputType(InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
        } else {
            // set the password not visible
            mPassWor.setInputType(InputType.TYPE_CLASS_TEXT |
                    InputType.TYPE_TEXT_VARIATION_PASSWORD);
        }
    }

    /**
     * sign in and start MainActivity
     */
    private void signIn() {
        // get the text from EditText
        String userNam = mUserName.getText().toString();
        String passW = mPassWor.getText().toString();
        // check user name and password is or not right to database
        String[] mProjection = {
                MyContentProvider._ID,
                MyContentProvider.UERNAME,
                MyContentProvider.EMAIL,
                MyContentProvider.PASSWORD
        };
        String mSelection = "uername=" + "\"" + userNam + "\"" + " and " + "password=" +
                "\"" + passW + "\"";
        Log.i("MUSIC", "mSelection=" + mSelection);
        Cursor mCursor = getContentResolver().query(
                MyContentProvider.CONTENT_URI,
                mProjection,
                mSelection,
                null,
                null);
        if ((null == mCursor) || (0 == mCursor.getCount())) {
            Toast.makeText(this, "Your account or password is wrong",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        mCursor.moveToFirst();
        String currentEmail = mCursor.getString(2);
        Toast.makeText(this, "Sign In Scuess",
                Toast.LENGTH_SHORT).show();
        // start MainActivity
        Intent startMain = new Intent(this, MainActivity.class);
        startMain.putExtra("curentueser", userNam);
        startMain.putExtra("curentemail", currentEmail);
        startActivity(startMain);
        // set Login status
        PreferData.setisLogin(true);
    }

    /**
     * start NewAccount Activity
     */
    private void startRegister() {
        Intent startRegister = new Intent(this, NewAccount.class);
        startActivity(startRegister);
    }
}
