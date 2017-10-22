package com.whatsappone.whatsappone.activities;

import android.content.Intent;
import android.os.Build;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.whatsappone.whatsappone.R;
import com.whatsappone.whatsappone.util.Util;
import com.whatsappone.whatsappone.util.ViewUtils;
import com.whatsappone.whatsappone.services.ChatHeadsService;

import static com.whatsappone.whatsappone.services.ChatHeadsService.EXTRA_ACTION_BAR_HEIGHT;
import static com.whatsappone.whatsappone.services.ChatHeadsService.EXTRA_FROM;
import static com.whatsappone.whatsappone.services.ChatHeadsService.EXTRA_STATUS_BAR_HEIGHT;

public class MainActivity extends AppCompatActivity {

    public static final String FROM = "MainActivity";

    private boolean mIsPermissionPromptShowing = false;
    private ViewGroup root;
    private Button mChatHeadButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Parent Container for the message
        root = ((ViewGroup) findViewById(R.id.rootView));

        // Setup the Chat head button
        mChatHeadButton = ((Button) findViewById(R.id.btn_chat_head_launcher));
        setupChatHeadButton();

        // Check if we have the permission to Access Notifications from other Apps?
        if(!Util.haveNotificationAccessPermission(this.getApplicationContext())
                || (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Util.haveDrawOverlayPermission(this.getApplicationContext()))){
            // Display a message in this regard.
            showPermissionPrompt();
        }
        else{
            // Enable it
            mChatHeadButton.setEnabled(true);
        }

        // TODO: We can show a welcome message irrespective of any status.
    }

    private void showPermissionPrompt(){
        View permissionPrompt = LayoutInflater.from(this).inflate(R.layout.layout_msg_notifications_permission, root, false);
        // Add to root
        root.addView(permissionPrompt, 1);
        mIsPermissionPromptShowing = true;
    }

    private void setupChatHeadButton(){

        // Listener to launch the service
        mChatHeadButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Launch the Chat Head Service
                Intent intent = new Intent(getApplicationContext(), ChatHeadsService.class);
                intent.putExtra(EXTRA_FROM, FROM);
                intent.putExtra(EXTRA_STATUS_BAR_HEIGHT, ViewUtils.getStatusBarHeight(MainActivity.this));
                intent.putExtra(EXTRA_ACTION_BAR_HEIGHT, ViewUtils.getActionBarHeight(MainActivity.this, getTheme()));
                startService(intent);

                //finish();
            }
        });
    }

    @Override
    protected void onRestart() {
        super.onRestart();

        // Every time we need to check for permissions. User may have revoked them meanwhile.

        // Check if we have the permission to Access Notifications from other Apps?
        if(!Util.haveNotificationAccessPermission(this.getApplicationContext())
                || (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Util.haveDrawOverlayPermission(this.getApplicationContext()))){

            // Disable the Chat head Button
            mChatHeadButton.setEnabled(false);

            if(!mIsPermissionPromptShowing){
                // Previously, the prompt had not been showing.

                // Display a message in this regard.
                showPermissionPrompt();
            }
        }
        else{
            // Enable the Chat head Button
            mChatHeadButton.setEnabled(true);

            if(mIsPermissionPromptShowing){

                // Previously, we showed the Notification prompt to the user and he has
                // permitted us with our Application in background. Remove the prompt.
                root.removeViewAt(1);

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
        startActivity(new Intent(this, WhatsAppOnePreferenceActivity.class));
    }
}
