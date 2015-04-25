
package com.lipengwei.music;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;
import com.lipengwei.music.R;

/**
 * @author pengwei.li@tcl.com
 * @version version-v1.0 This Activity is used to register new account
 */
public class NewAccount extends Activity {

    public static String NAME = "name";// user name
    public static String PASS = "pass";// user password

    private static final String TAG = "MUSIC";
    // EditText view
    private EditText mAccountName;
    private EditText mPassWord;
    private EditText mEmail;
    private EditText mPassWordAgain;

    /**
     * Called when the activity is first created.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_newaccount);
        getActionBar().setDisplayHomeAsUpEnabled(true);
        // get EditText view
        mAccountName = (EditText) findViewById(R.id.newuername);
        mPassWord = (EditText) findViewById(R.id.newpassword);
        mPassWordAgain = (EditText) findViewById(R.id.newpassword_again);
        mEmail = (EditText) findViewById(R.id.emailadress);
    }

    /**
     * Listener button onClick
     * 
     * @param v-button view
     */
    public void onClick(View v) {
        // get string from EditText
        String name = mAccountName.getText().toString();
        String pass = mPassWord.getText().toString();
        String passAgain = mPassWordAgain.getText().toString();
        String emai = mEmail.getText().toString();
        // check if the account exist in database
        String mSelection = "uername=" + "\"" + name + "\"";
        Cursor mCursor = getContentResolver().query(
                MyContentProvider.CONTENT_URI,
                null,
                mSelection,
                null,
                null);
        if ((null == mCursor) || (0 == mCursor.getCount())) {
            // check the information edit completion
            if (name.isEmpty() || pass.isEmpty() || passAgain.isEmpty() ||
                    emai.isEmpty()) {
                Toast.makeText(this, "Please add all",
                        Toast.LENGTH_SHORT).show();
                return;
            }
            // confirm passWord is or not right
            if (!pass.equals(passAgain)) {
                Toast.makeText(this, "Please confirm your passWord",
                        Toast.LENGTH_SHORT).show();
                return;
            }
            // check email is or not legality
            if (!isEmail(emai)) {
                Toast.makeText(this, "Your Email is wrong",
                        Toast.LENGTH_SHORT).show();
                return;
            }
            Toast.makeText(this, "Register Scuess",
                    Toast.LENGTH_SHORT).show();
            // insert new account to database
            insert();
            // start LoginActivty and put name and password
            Intent intent = new Intent(this, LoginActivity.class);
            intent.putExtra(NAME, name);
            intent.putExtra(PASS, pass);
            startActivity(intent);
        } else {
            Toast.makeText(this, "The account has been used",
                    Toast.LENGTH_SHORT).show();
            return;
        }

    }

    /**
     * insert account to database
     */
    private void insert() {
        // get string from EditText
        String userName = mAccountName.getText().toString();
        String passWor = mPassWord.getText().toString();
        String emailAdress = mEmail.getText().toString();
        Log.i(TAG, "" + userName + passWor + emailAdress);
        ContentValues mNewValues = new ContentValues();
        if (userName.isEmpty() || passWor.isEmpty() || emailAdress.isEmpty())
            return;
        mNewValues.put(MyContentProvider.UERNAME, userName);
        mNewValues.put(MyContentProvider.EMAIL, emailAdress);
        mNewValues.put(MyContentProvider.PASSWORD, passWor);
        this.getContentResolver().insert(
                MyContentProvider.CONTENT_URI,
                mNewValues
                );
        Log.i("MUSIC", "can't insert");
    }

    /**
     * check email address legality
     * 
     * @param str-the input email address string
     * @return which the email is legality
     */
    private boolean isEmail(String str) {
        String regex = "[a-zA-Z_]{1,}[0-9]{0,}@(([a-zA-z0-9]-*){1,}\\.){1,3}" +
                "[a-zA-z\\-]{1,}";
        return match(regex, str);
    }

    /**
     * match the regex and email string
     * 
     * @return which str match regex
     */
    private boolean match(String regex, String str) {
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(str);
        return matcher.matches();
    }
}
