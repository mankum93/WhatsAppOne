package com.whatsappone.whatsappone.services;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.PixelFormat;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.whatsappone.whatsappone.WhatsappOneApplication;
import com.whatsappone.whatsappone.database.ContactsDbHelper;
import com.whatsappone.whatsappone.util.DateTimeUtils;
import com.whatsappone.whatsappone.R;
import com.whatsappone.whatsappone.util.ViewUtils;

import java.util.ArrayList;
import java.util.List;

import model.WhatsAppMessage;

/**
 * Created by DJ on 10/15/2017.
 */

public class ChatHeadsService extends Service{

    public static final String EXTRA_STATUS_BAR_HEIGHT = "ChatHeadsService.STATUS_BAR_HEIGHT";
    public static final String EXTRA_ACTION_BAR_HEIGHT = "ChatHeadsService.ACTION_BAR_HEIGHT";

    public static final String EXTRA_NEW_MESSAGE = "ChatHeadsService.NEW_MESSAGE";

    public static final String ACTION_NEW_MESSAGE = "com.whatsappone.whatsappone.ACTION_NEW_MESSAGE";

    private WindowManager mWindowManager;
    private View mChatHeadLayout;
    private View mChatBubbleCloseButton;

    private SQLiteDatabase db;

    private View mChatWindow;

    /**
     * To receive newly arrived messages.
     */
    private BroadcastReceiver receiver;


    /**
     * Status bar height in px.
     */
    private int mStatusBarHeight;
    private int mActionBarHeight;
    private View mChatWindowLayout;
    private ImageView mChatBubbleImage;
    private RecyclerView recyclerView;
    private ChatMessagesRecyclerViewAdapter mMessagesAdapter;
    private ImageView mChatWindowCloseButtonImage;
    private View mChatBubbleLayout;

    public ChatHeadsService() {
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // Get the Status Bar height
        this.mStatusBarHeight = intent.getIntExtra(EXTRA_STATUS_BAR_HEIGHT, (int)ViewUtils.dpToPx(this, 25));
        this.mActionBarHeight = intent.getIntExtra(EXTRA_ACTION_BAR_HEIGHT, (int)ViewUtils.dpToPx(this, 56));
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onCreate() {
        super.onCreate();

        // Get the Single Db instance
        db = ((WhatsappOneApplication) this.getApplication()).dbInstance;

        mWindowManager = (WindowManager) getSystemService(WINDOW_SERVICE);

        DisplayMetrics displayMetrics = new DisplayMetrics();
        mWindowManager.getDefaultDisplay().getMetrics(displayMetrics);
        int deviceHeight = displayMetrics.heightPixels;
        int deviceWidth = displayMetrics.widthPixels;

        //Inflate the floating arrangement
        mChatHeadLayout = LayoutInflater.from(this).inflate(R.layout.layout_chat_bubble_1, null);

        mChatWindowLayout = mChatHeadLayout.findViewById(R.id.id_chat_window_layout);
        mChatBubbleLayout = mChatHeadLayout.findViewById(R.id.id_chat_bubble_layout);

        // Setup the RecyclerView
        recyclerView = ((RecyclerView) mChatHeadLayout.findViewById(R.id.id_chat_window));
        // Set the Chat window height to 80% of the screen height
        recyclerView.getLayoutParams().height = (int)(deviceHeight * 0.8f);
        recyclerView.setLayoutManager(new LinearLayoutManager(mChatHeadLayout.getContext(), LinearLayoutManager.VERTICAL, false));
        // Query the Db for already existing messages
        List<WhatsAppMessage> existingMessages = ContactsDbHelper.getAllMessageRecordsFromDb(db, ContactsDbHelper.SORT_ORDER_DESC);
        mMessagesAdapter = new ChatMessagesRecyclerViewAdapter(existingMessages);
        recyclerView.setAdapter(mMessagesAdapter);

        // Register a receiver to receive the newly arrived messages
        IntentFilter filter = new IntentFilter();
        filter.addAction(ACTION_NEW_MESSAGE);

        receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                mMessagesAdapter.updateMessage((WhatsAppMessage) intent.getParcelableExtra(EXTRA_NEW_MESSAGE));
            }
        };
        LocalBroadcastManager.getInstance(this).registerReceiver(receiver, filter);

        //Add the view to the window.
        final WindowManager.LayoutParams params = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.TYPE_SYSTEM_ALERT,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                PixelFormat.TRANSLUCENT);

        //Specify the view position to be below the Status Bar and Action Bar by 20dp
        int positionY = mStatusBarHeight + mActionBarHeight + (int)ViewUtils.dpToPx(this, 20);
        params.gravity = Gravity.TOP | Gravity.END;
        params.x = 0;
        params.y = positionY;

        //Add the view to the window
        mWindowManager.addView(mChatHeadLayout, params);

        // Attach a listener to the Close button on the Bubble
        mChatBubbleCloseButton = mChatHeadLayout.findViewById(R.id.close_btn);
        mChatBubbleCloseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Stopping the service would lead to removal of window through
                // onDestroy()
                stopSelf();
            }
        });

        // When the Chat window closes, bring back the Bubble
        mChatWindowCloseButtonImage = ((ImageView) mChatHeadLayout.findViewById(R.id.id_cancel));
        mChatWindowCloseButtonImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mChatWindowLayout.setVisibility(View.GONE);
                mChatBubbleLayout.setVisibility(View.VISIBLE);
            }
        });

        // Next, a listener for the chat head bubble - user would want to see the chat
        // window with the list of messages received/read.
        mChatBubbleImage = ((ImageView) mChatHeadLayout.findViewById(R.id.id_chat_bubble_image));
        mChatBubbleImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Chat window is already inflated, bind it with data and make it visible.
                mChatWindowLayout.setVisibility(View.VISIBLE);
                mChatBubbleLayout.setVisibility(View.GONE);
            }
        });

    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        if (receiver != null) {
            LocalBroadcastManager.getInstance(this).unregisterReceiver(receiver);
            receiver = null;
        }
        if (mChatHeadLayout != null) mWindowManager.removeView(mChatHeadLayout);

        super.onDestroy();
    }

    public static class ChatMessagesRecyclerViewAdapter extends RecyclerView.Adapter<ChatMessageViewHolder>{

        private List<WhatsAppMessage> messages;

        public ChatMessagesRecyclerViewAdapter(@Nullable List<WhatsAppMessage> messages) {

            if(messages == null){
                this.messages = new ArrayList<>();
            }
            else{
                this.messages = messages;
            }
        }

        public void updateMessages(@Nullable List<WhatsAppMessage> messages){
            int oldIndex = this.messages.size() == 0 ? 0 : this.messages.size() - 1;
            if(messages != null){
                this.messages.addAll(0, messages);
                notifyItemRangeInserted(oldIndex, messages.size());
            }
        }

        public void updateMessage(@Nullable WhatsAppMessage message){
            if(message != null){
                messages.add(0, message);
                notifyItemInserted(0);
            }
        }

        @Override
        public ChatMessageViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

            // Inflate the Album view item
            View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_message, parent, false);

            ChatMessageViewHolder holder = new ChatMessageViewHolder(itemView);
            return holder;
        }

        @Override
        public void onBindViewHolder(ChatMessageViewHolder holder, int position) {

            WhatsAppMessage messageToBeBound = messages.get(position);

            holder.mContent.setText(messageToBeBound.getMessageText());
            holder.mSender.setText(messageToBeBound.getSenderName());
            holder.mTime.setText(DateTimeUtils.timeMillisToHH_MMFormat(messageToBeBound.getMessageTimeMillis()));
        }

        @Override
        public int getItemCount() {
            return messages.size();
        }
    }

    public static class ChatMessageViewHolder extends RecyclerView.ViewHolder{

        TextView mContent;
        TextView mSender;
        TextView mTime;

        public ChatMessageViewHolder(View itemView) {
            super(itemView);

            // Get the view hierarchy
            mContent = ((TextView) itemView.findViewById(R.id.id_msg_content));
            mSender = ((TextView) itemView.findViewById(R.id.id_sender));
            mTime = ((TextView) itemView.findViewById(R.id.id_time));
        }
    }
}
