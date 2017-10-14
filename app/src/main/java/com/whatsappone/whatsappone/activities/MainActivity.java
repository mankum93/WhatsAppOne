package com.whatsappone.whatsappone.activities;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup;

import com.whatsappone.whatsappone.R;
import com.whatsappone.whatsappone.Util;
import com.whatsappone.whatsappone.activities.WhatsappOnePreferenceActivity;

public class MainActivity extends AppCompatActivity {

    private boolean mIsPermissionPromptShowing = false;
    private ViewGroup root;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Parent Container for the message
        root = ((ViewGroup) findViewById(R.id.rootView));

        // Check if we have the permission to Access Notifications from other Apps?
        if(!Util.haveNotificationAccessPermission(this.getApplicationContext())){

            // Display a message in this regard.

            LayoutInflater.from(this).inflate(R.layout.layout_msg_notifications_permission, root, true);

            mIsPermissionPromptShowing = true;
        }

        // TODO: We can show a welcome message irrespective of any status.
    }

    @Override
    protected void onRestart() {
        super.onRestart();

        // Every time we have to check for permissions. User may have revoked them meanwhile.

        // Check if we have the permission to Access Notifications from other Apps?
        if(!Util.haveNotificationAccessPermission(this.getApplicationContext())){
            // Display a message in this regard.

            // Parent Container for the message
            LayoutInflater.from(this).inflate(R.layout.layout_msg_notifications_permission, root, true);

            mIsPermissionPromptShowing = true;
        }
        else{
            if(mIsPermissionPromptShowing){
                // Previously, we showed the Notification prompt to the user and he has
                // permitted us with our Application in background. Remove the prompt.
                root.removeViewAt(0);

                // Reset the prompt flag
                mIsPermissionPromptShowing = false;
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        // Handle item selection
        switch (item.getItemId()) {
            case R.id.id_menu_settings:
                showAppPreferences();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void showAppPreferences(){
        startActivity(new Intent(this, WhatsappOnePreferenceActivity.class));
    }
}
