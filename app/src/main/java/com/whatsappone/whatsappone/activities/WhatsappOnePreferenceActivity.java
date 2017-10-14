package com.whatsappone.whatsappone.activities;

import android.os.Bundle;
import android.preference.PreferenceFragment;
import android.support.v7.app.AppCompatActivity;

import com.whatsappone.whatsappone.R;

/**
 * Created by DJ on 10/14/2017.
 */

public class WhatsappOnePreferenceActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Hook up the
        getFragmentManager().beginTransaction()
                .replace(android.R.id.content, new WhatsappOnePreferenceFragment())
                .commit();
    }


    public static class WhatsappOnePreferenceFragment extends PreferenceFragment {
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);

            // Load the preferences from an XML resource
            addPreferencesFromResource(R.xml.preferences);
        }
    }
}
