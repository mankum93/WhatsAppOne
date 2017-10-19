package com.whatsappone.whatsappone.activities;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceGroup;
import android.preference.PreferenceScreen;
import android.provider.Settings;
import android.support.v7.app.AppCompatActivity;

import com.whatsappone.whatsappone.R;

/**
 * Created by DJ on 10/14/2017.
 */

public class WhatsAppOnePreferenceActivity extends AppCompatActivity {

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

            // Add on a Preference for Draw overlay permission if the API >= 23
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
                Preference overlay = new Preference(this.getActivity());
                overlay.setTitle(R.string.pref_draw_overlay_setting_title);
                overlay.setSummary(R.string.pref_draw_overlay_setting_summary);
                overlay.setIntent(new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION));

                PreferenceGroup preferenceGroup = (PreferenceGroup) findPreference("permissions");
                preferenceGroup.addPreference(overlay);
            }
        }
    }
}
