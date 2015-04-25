
package com.lipengwei.music;

import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import com.lipengwei.music.R;

/**
 * @author pengwei.li@tcl.com
 * @version version-v1.0 the fragment is used to show the login user information
 *          and sign out
 */
public class MyFragment extends Fragment {

    /**
     * Called when a fragment is first attached to its activity
     */
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        Log.i("test", "onAttach");
    }

    /**
     * Called to have the fragment instantiate its user interface view
     * 
     * @return he View for the fragment's UI
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        // get the view
        View v = inflater.inflate(R.layout.myfragment, container, false);
        TextView showUser = (TextView) v.findViewById(R.id.textView_username);
        TextView showEmail = (TextView) v.findViewById(R.id.textView_email);
        Button button = (Button) v.findViewById(R.id.button_quit);
        // set the information to show
        showUser.setText(MainActivity.showUser);
        showEmail.setText(MainActivity.showEmail);
        // Listener click button
        button.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                // Login out
                ((MainActivity) getActivity()).clickQuit();
            }
        });
        return v;

    }
}
