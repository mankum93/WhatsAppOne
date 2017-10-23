package com.whatsappone.whatsappone.services;

import android.app.Service;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.PixelFormat;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

import com.whatsappone.whatsappone.WhatsAppOneApplication;
import com.whatsappone.whatsappone.adapters.ItemTouchHelperAdapter;
import com.whatsappone.whatsappone.database.ContactsDbHelper;
import com.whatsappone.whatsappone.util.DateTimeUtils;
import com.whatsappone.whatsappone.R;
import com.whatsappone.whatsappone.util.ViewUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import model.WhatsAppMessage;

/**
 * Created by DJ on 10/15/2017.
 */

public class ChatHeadsService extends Service{

    public static final String EXTRA_STATUS_BAR_HEIGHT = "ChatHeadsService.STATUS_BAR_HEIGHT";
    public static final String EXTRA_ACTION_BAR_HEIGHT = "ChatHeadsService.ACTION_BAR_HEIGHT";

    public static final String EXTRA_NEW_MESSAGE = "ChatHeadsService.NEW_MESSAGE";
    public static final String EXTRA_FROM = "ChatHeadsService.FROM";
    public static final String EXTRA_UPDATE_NAME = "ChatHeadsService.UPDATE_NAME";
    public static final String EXTRA_INSERT_MESSAGE = "ChatHeadsService.INSERT_MESSAGE";

    public static final String ACTION_NEW_MESSAGE = "com.whatsappone.whatsappone.ACTION_NEW_MESSAGE";

    public static final int STATUS_DB_WRITE_PENDING = 0x00;
    public static final int STATUS_DB_UPDATE_PENDING = 0x01;
    public static final int STATUS_DB_NO_ACTION = 0x02;

    private WindowManager mWindowManager;

    /**
     * Singleton instance of Db
     */
    private SQLiteDatabase db;

    /**
     * A worked Handler to be used to post Db related takss to background thread
     */
    private Handler mHandler;

    /**
     * Status bar height in px.
     */
    private int mStatusBarHeight;
    /**
     * Action bar height in px.
     */
    private int mActionBarHeight;

    /**
     * View hierarchy
     */
    private View mChatWindowLayout;
    private ImageView mChatBubbleImage;
    private ImageView mChatWindowCloseButtonImage;
    private View mChatBubbleLayout;
    private View mChatHeadLayout;
    private View mChatBubbleCloseButton;

    private RecyclerView mRecyclerView;
    private ChatMessagesRecyclerViewAdapter mMessagesAdapter;
    private LinearLayoutManager mLayoutManager;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        if(intent != null){

            String intentSource = intent.getStringExtra(EXTRA_FROM);

            if(intentSource.equals(WhatsAppNotificationListenerService.FROM)){

                // Message is bound to be there in any case
                final WhatsAppMessage message = (WhatsAppMessage) intent.getParcelableExtra(EXTRA_NEW_MESSAGE);

                // Do we have to update the Name?
                if(intent.getBooleanExtra(EXTRA_UPDATE_NAME, false)){
                    ContactsDbHelper.updateMessagesTableWithNewNameConditionally(db, message.getPhoneNo(), message.getSenderName());
                }
                // Do we need to insert/display the Message?
                if(intent.getBooleanExtra(EXTRA_INSERT_MESSAGE, false)){

                    message.setMessageReadStatus(false);

                    if(mRecyclerView.getVisibility() != View.VISIBLE){
                        // If not visible, write to Db just like this
                        // Later on, it shall be updated with new visibility
                        mHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                // Write it to Db
                                ContactsDbHelper.insertMessageToDb(db, message);
                            }
                        });
                        // For now, just update the Adapter with new messages(not UI)
                        mMessagesAdapter.updateMessageWithoutUI(message);
                    }
                    else{
                        // It will be new for a while until the user has actually
                        // seen it and then shall be transitioned to as read
                        // Update the UI with the new Message
                        mMessagesAdapter.updateMessage(message);
                        // Force to scroll to the top position; notifyItemInserted()
                        // mysteriously stopped working. This is a workaround for that
                        mLayoutManager.scrollToPosition(0);
                    }

                }
            }
        }
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onCreate() {
        super.onCreate();

        // Get the Status Bar and Action Bar height
        mStatusBarHeight = ViewUtils.getStatusBarHeight(this);
        mActionBarHeight = ViewUtils.getActionBarHeight(this, getTheme());

        // Get the Single Db instance
        db = WhatsAppOneApplication.dbInstance;

        // Prepare a worker thread to perform Db transactions
        HandlerThread worker = new HandlerThread("worker");
        worker.start();
        mHandler = new Handler(worker.getLooper());

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
        mRecyclerView = ((RecyclerView) mChatHeadLayout.findViewById(R.id.id_chat_window));
        mLayoutManager = new LinearLayoutManager(mChatHeadLayout.getContext(), LinearLayoutManager.VERTICAL, false);
        mRecyclerView.setLayoutManager(mLayoutManager);
        // Query the Db for already existing mMessages
        List<WhatsAppMessage> existingMessages = ContactsDbHelper.getAllMessageRecordsFromDb(db, ContactsDbHelper.SORT_ORDER_DESC);
        mMessagesAdapter = new ChatMessagesRecyclerViewAdapter(existingMessages, mHandler, new Handler(getMainLooper()));
        mRecyclerView.setAdapter(mMessagesAdapter);

        // ItemTouchHelper to enable swipe-to-dismiss behavior
        ItemTouchHelper.Callback callback =
                new SimpleItemTouchHelperCallback(mMessagesAdapter);
        ItemTouchHelper touchHelper = new ItemTouchHelper(callback);
        touchHelper.attachToRecyclerView(mRecyclerView);

        //Add the view to the window.
        final WindowManager.LayoutParams params = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.TYPE_PHONE,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                PixelFormat.TRANSLUCENT);

        // Set the Chat window height to 80% of the screen height
        mChatWindowLayout.getLayoutParams().height = (int)(deviceHeight * 0.8f);
        mChatWindowLayout.getLayoutParams().width = (int)(deviceWidth * 0.9f);

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
        // window with the list of mMessages received/read.
        mChatBubbleImage = ((ImageView) mChatHeadLayout.findViewById(R.id.id_chat_bubble_image));
        mChatBubbleImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Chat window is already inflated, bind it with data and make it visible.
                mChatWindowLayout.setVisibility(View.VISIBLE);
                mMessagesAdapter.notifyDataSetChanged();
                mChatBubbleLayout.setVisibility(View.GONE);
            }
        });

    }

    private void positionChatHeadLayout(){
        //Specify the view position to be below the Status Bar
        int positionY = mActionBarHeight;
        mChatBubbleLayout.setY(positionY);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {

        if (mChatHeadLayout != null) mWindowManager.removeView(mChatHeadLayout);

        super.onDestroy();
    }




    // RECYCLERVIEW RELATED CODE--------------------------------------------------------------------

    public static class ChatMessagesRecyclerViewAdapter extends RecyclerView.Adapter<ChatMessageViewHolder>
            implements ItemTouchHelperAdapter{

        private List<WhatsAppMessage> mMessages;

        private Handler mHandler;
        private Handler mMainHandler;

        private List<Integer> mMessageDbWriteStatuses = new ArrayList<>();

        public ChatMessagesRecyclerViewAdapter(@Nullable List<WhatsAppMessage> messages,
                                               @NonNull Handler workerHandler,
                                               @NonNull Handler mainHandler) {

            if(messages == null){
                this.mMessages = new ArrayList<>();
            }
            else{
                this.mMessages = messages;
                if(!mMessages.isEmpty()){
                    // Update the initial Db write statuses
                    mMessageDbWriteStatuses.addAll(Collections.nCopies(mMessages.size(), STATUS_DB_NO_ACTION));
                }
            }
            mHandler = workerHandler;
            mMainHandler = mainHandler;
        }

        public void updateMessages(@Nullable List<WhatsAppMessage> messages){
            int oldIndex = this.mMessages.size() == 0 ? 0 : this.mMessages.size() - 1;
            if(messages != null){
                this.mMessages.addAll(0, messages);
                notifyItemRangeInserted(oldIndex, messages.size());
            }
        }

        public void updateMessage(@Nullable WhatsAppMessage message){
            if(message != null){
                mMessages.add(0, message);
                // Db write status
                mMessageDbWriteStatuses.add(0, STATUS_DB_WRITE_PENDING);
                notifyItemInserted(0);
            }
        }

        public void updateMessageWithoutUI(@Nullable WhatsAppMessage message){
            if(message != null){
                mMessages.add(0, message);
                // Db write status
                mMessageDbWriteStatuses.add(0, STATUS_DB_UPDATE_PENDING);
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
        public void onBindViewHolder(final ChatMessageViewHolder holder, final int position) {

            final WhatsAppMessage messageToBeBound = mMessages.get(position);

            // Is the message a new one?
            if(!messageToBeBound.getMessageReadStatus()){
                // Set the background color to reflect that
                holder.itemView.setBackgroundResource(R.drawable.rounded_shape_6);

                // Post this message to the Db with a proper Read status
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        messageToBeBound.setMessageReadStatus(true);
                        // Update or insert to Db
                        if(mMessageDbWriteStatuses.get(position) == STATUS_DB_UPDATE_PENDING){
                            ContactsDbHelper.updateMessageToDb(WhatsAppOneApplication.dbInstance, messageToBeBound);
                        }
                        else if(mMessageDbWriteStatuses.get(position) == STATUS_DB_WRITE_PENDING){
                            ContactsDbHelper.insertMessageToDb(WhatsAppOneApplication.dbInstance, messageToBeBound);
                        }

                        mMainHandler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                if(messageToBeBound.getMessageReadStatus()){
                                    holder.itemView.setBackgroundResource(R.drawable.rounded_shape_5);
                                }
                            }
                        }, 3000);
                    }
                });
            }

            holder.mContent.setText(messageToBeBound.getMessageText());
            holder.mSender.setText(messageToBeBound.getSenderName());
            holder.mTime.setText(DateTimeUtils.timeMillisToHH_MMFormat(messageToBeBound.getMessageTimeMillis()));
        }

        @Override
        public int getItemCount() {
            return mMessages.size();
        }


        @Override
        public void onItemMove(int fromPosition, int toPosition) {
            // Currently, we don't support this movement
        }

        @Override
        public void onItemDismiss(int position) {
            // Remove the message item from the list, Db and update UI
            ContactsDbHelper.removeMessageFromDb(WhatsAppOneApplication.dbInstance, mMessages.get(position));
            mMessages.remove(position);
            notifyItemRemoved(position);
        }
    }

    /*public static abstract class DbTask implements Runnable{

        protected WhatsAppMessage message;

        public DbTask(WhatsAppMessage message) {
        }
    }*/

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

    // SWIPE BEHAVIOR FOR RECYCLERVIEW--------------------------------------------------------------

    // Courtesy of Mr. Paul: https://medium.com/@ipaulpro/drag-and-swipe-with-recyclerview-b9456d2b1aaf
    public class SimpleItemTouchHelperCallback extends ItemTouchHelper.Callback {

        private final ItemTouchHelperAdapter mAdapter;

        public SimpleItemTouchHelperCallback(@NonNull ItemTouchHelperAdapter adapter) {
            mAdapter = adapter;
        }

        @Override
        public boolean isLongPressDragEnabled() {
            return false;
        }

        @Override
        public boolean isItemViewSwipeEnabled() {
            return true;
        }

        @Override
        public int getMovementFlags(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder) {
            int dragFlags = ItemTouchHelper.UP | ItemTouchHelper.DOWN;
            int swipeFlags = ItemTouchHelper.START | ItemTouchHelper.END;
            return makeMovementFlags(dragFlags, swipeFlags);
        }

        @Override
        public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder,
                              RecyclerView.ViewHolder target) {
            // We don't want the Drag behavior
            return false;
        }

        @Override
        public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
            mAdapter.onItemDismiss(viewHolder.getAdapterPosition());
        }

    }
}
