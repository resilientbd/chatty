package com.faisal.chatty;

import android.Manifest;
import android.content.Intent;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Bundle;

import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.faisal.chatty.util.PermissionUtil;
import com.faisal.chatty.util.SinchHelper;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.sinch.android.rtc.PushPair;
import com.sinch.android.rtc.Sinch;
import com.sinch.android.rtc.SinchClient;
import com.sinch.android.rtc.SinchError;
import com.sinch.android.rtc.calling.Call;
import com.sinch.android.rtc.calling.CallClient;
import com.sinch.android.rtc.calling.CallClientListener;
import com.sinch.android.rtc.calling.CallListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

import static com.faisal.chatty.util.Constrants.SnitchFlags.APP_KEY;
import static com.faisal.chatty.util.Constrants.SnitchFlags.APP_SECRET;
import static com.faisal.chatty.util.Constrants.SnitchFlags.ENVIRONMENT;

public class ChatActivity extends AppCompatActivity  {

    private String mChatUser;
    TextView mUserName;
    TextView mUserLastSeen;
    CircleImageView mUserImage;
    private FirebaseAuth mAuth;

    String mCurrentUserId;

    DatabaseReference mDatabaseReference;
    private DatabaseReference mRootReference;

    private ImageButton mChatSendButton,mChatAddButton;
    private EditText mMessageView;

    private RecyclerView mMessagesList;
    private SwipeRefreshLayout mSwipeRefreshLayout;

    private final List<Messages> messagesList = new ArrayList<>();
    private LinearLayoutManager mLinearLayoutManager;
    private MessageAdapter mMessageAdapter;

    public static final int TOTAL_ITEM_TO_LOAD = 10;
    private int mCurrentPage = 1;

    //Solution for descending list on refresh
    private int itemPos = 0;
    private String mLastKey="";
    private String mPrevKey="";

    private static final int GALLERY_PICK=1;
    StorageReference mImageStorage;
    private ImageView callBtn;
    private SinchClient sinchClient;

    // private SinchHelper sinchHelper;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        mChatAddButton = (ImageButton)findViewById(R.id.chatAddButton);
        mChatSendButton = (ImageButton)findViewById(R.id.chatSendButton);
        mMessageView = (EditText)findViewById(R.id.chatMessageView);

        //-----GETING FROM INTENT----
        mChatUser = getIntent().getStringExtra("user_id");
        String userName = getIntent().getStringExtra("user_name");

        //---SETTING ONLINE------
        mDatabaseReference = FirebaseDatabase.getInstance().getReference().child("users");

        //----ADDING ACTION BAR-----
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        actionBar.setDisplayHomeAsUpEnabled(true);


        //---INFLATING APP BAR LAYOUT INTO ACTION BAR----
        LayoutInflater inflater = (LayoutInflater)this.getSystemService(LAYOUT_INFLATER_SERVICE);
        View actionBarView = inflater.inflate(R.layout.app_bar_layout,null);
        actionBar.setCustomView(actionBarView);
        callBtn=actionBarView.findViewById(R.id.btnVideoCall);
        disableVideoCall();
        //---ADDING DATA ON ACTION BAR----
        mUserName=(TextView) actionBarView.findViewById(R.id.textView3);
        mUserLastSeen = (TextView) actionBarView.findViewById(R.id.textView5);
        mUserImage = (CircleImageView) actionBarView.findViewById(R.id.circleImageView);
        mUserName.setText(userName);

        mRootReference = FirebaseDatabase.getInstance().getReference();
        mImageStorage = FirebaseStorage.getInstance().getReference();

        mAuth = FirebaseAuth.getInstance();
        mCurrentUserId = mAuth.getCurrentUser().getUid();

        mMessageAdapter = new MessageAdapter(messagesList);

        mMessagesList = (RecyclerView)findViewById(R.id.recycleViewMessageList);
        mSwipeRefreshLayout = (SwipeRefreshLayout)findViewById(R.id.message_swipe_layout);
        mLinearLayoutManager = new LinearLayoutManager(ChatActivity.this);

       // mMessagesList.setHasFixedSize(true);
        mMessagesList.setLayoutManager(mLinearLayoutManager);
        mMessagesList.setAdapter(mMessageAdapter);

        loadMessages();

        //----ADDING LAST SEEN-----
        mRootReference.child("users").child(mChatUser).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String onlineValue=dataSnapshot.child("online").getValue().toString();
                String imageValue = dataSnapshot.child("thumb_image").getValue().toString();

                Picasso.with(ChatActivity.this).load(imageValue).placeholder(R.drawable.user_img).into(mUserImage);
                if(onlineValue.equals("true")){
                    mUserLastSeen.setText("online");
                    enableVideoCall();
                }
                else{
                    disableVideoCall();
                    GetTimeAgo getTimeAgo = new GetTimeAgo();
                    long lastTime = Long.parseLong(onlineValue);
                    String lastSeen = getTimeAgo.getTimeAgo(lastTime,getApplicationContext());
                    mUserLastSeen.setText(lastSeen);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        //----ADDING SEEN OF MESSAGES----
        mRootReference.child("chats").child(mCurrentUserId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                if(!dataSnapshot.hasChild(mChatUser)){

                    Map chatAddMap = new HashMap();
                    chatAddMap.put("seen",false);
                    chatAddMap.put("time_stamp", ServerValue.TIMESTAMP);

                    Map chatUserMap = new HashMap();
                    chatUserMap.put("chats/"+mChatUser+"/"+mCurrentUserId,chatAddMap);
                    chatUserMap.put("chats/"+mCurrentUserId+"/"+mChatUser,chatAddMap);

                    mRootReference.updateChildren(chatUserMap, new DatabaseReference.CompletionListener() {
                        @Override
                        public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                            if(databaseError == null){
                                Toast.makeText(getApplicationContext(), "Successfully Added chats feature", Toast.LENGTH_SHORT).show();
                            }
                            else
                                Toast.makeText(getApplicationContext(), "Cannot Add chats feature", Toast.LENGTH_SHORT).show();
                        }


                    });

                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(getApplicationContext(), "Something went wrong.. Please go back..", Toast.LENGTH_SHORT).show();
            }
        });

        //----SEND MESSAGE--BUTTON----

        mChatSendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String message = mMessageView.getText().toString();
                if(!TextUtils.isEmpty(message)){

                   String current_user_ref = "messages/"+mCurrentUserId+"/"+mChatUser;
                    String chat_user_ref = "messages/"+ mChatUser +"/"+mCurrentUserId;

                    DatabaseReference user_message_push = mRootReference.child("messages")
                            .child(mCurrentUserId).child(mChatUser).push();

                    String push_id = user_message_push.getKey();

                    Map messageMap = new HashMap();
                    messageMap.put("message",message);
                    messageMap.put("seen",false);
                    messageMap.put("type","text");
                    messageMap.put("time", ServerValue.TIMESTAMP);
                    messageMap.put("from",mCurrentUserId);

                    Map messageUserMap = new HashMap();
                    messageUserMap.put(current_user_ref+"/"+push_id,messageMap);
                    messageUserMap.put(chat_user_ref+"/"+push_id,messageMap);

                    mRootReference.updateChildren(messageUserMap, new DatabaseReference.CompletionListener(){

                        @Override
                        public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                            if(databaseError != null){
                                Log.e("CHAT_ACTIVITY","Cannot add message to database");
                            }
                            else{
                                Toast.makeText(ChatActivity.this, "Message sent", Toast.LENGTH_SHORT).show();
                                mMessageView.setText("");
                            }

                        }
                    });




                }

            }
        });
       /*
        //----THE WRAP CONTENT OF IMAGE VIEW IS GIVING ERROR--- SO REMOVING THIS FUNCTIONALITY-------


       mChatAddButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent galleryIntent=new Intent();
                galleryIntent.setType("image/*");
                galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(galleryIntent,"Select Image"),GALLERY_PICK);
            }
        });
        */
        //----LOADING 10 MESSAGES ON SWIPE REFRESH----
        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                itemPos = 0;
                mCurrentPage++;
                loadMoreMessages();;

            }
        });
        Toast.makeText(this,"currentUser: "+mCurrentUserId+" Chat User: "+mChatUser,Toast.LENGTH_LONG).show();
        sinchClient = Sinch.getSinchClientBuilder()
                .context(this)
                .userId("rasel")
                .applicationKey(APP_KEY)
                .applicationSecret(APP_SECRET)
                .environmentHost(ENVIRONMENT)
                .build();

        sinchClient.setSupportCalling(true);
        sinchClient.startListeningOnActiveConnection();
        sinchClient.start();

        sinchClient.getCallClient().addCallClientListener(new SinchCallClientListener());

        //sinchHelper=SinchHelper.getInstance("rasel", this,new SinchCallClientListener() );
        //video call action
        callBtn.setOnClickListener(v -> {
//            Intent intent=new Intent(ChatActivity.this,CallActivity.class);
//            startActivity(intent);
            if (PermissionUtil.on(this).request(PermissionUtil.REQUEST_CODE_PERMISSION_CAMERA,
                    Manifest.permission.RECORD_AUDIO,Manifest.permission.CALL_PHONE)) {
               // sinchHelper.makeaVoiceCall("hasan",this);
                sinchClient.getCallClient().callUser("hasan");
            }

        });
    }

   //---FIRST 10 MESSAGES WILL LOAD ON START----
    private void loadMessages() {

        DatabaseReference messageRef = mRootReference.child("messages").child(mCurrentUserId).child(mChatUser);
        Query messageQuery = messageRef.limitToLast(mCurrentPage*TOTAL_ITEM_TO_LOAD);

        messageQuery.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                Messages messages = (Messages) dataSnapshot.getValue(Messages.class);

                itemPos++;

                if(itemPos == 1){
                    String mMessageKey = dataSnapshot.getKey();

                    mLastKey = mMessageKey;
                    mPrevKey = mMessageKey;
                }

                messagesList.add(messages);
                mMessageAdapter.notifyDataSetChanged();

                mMessagesList.scrollToPosition(messagesList.size()-1);

                mSwipeRefreshLayout.setRefreshing(false);
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }
    //video call button enable
    private void enableVideoCall()
    {
        if(callBtn!=null)
        {
            callBtn.setEnabled(true);
            callBtn.setAlpha(1f);
        }

    }
    //video call button disable
    private void disableVideoCall()
    {
        if(callBtn!=null)
        {
            callBtn.setEnabled(false);
            callBtn.setAlpha(0.5f);
        }
    }
    //---ON REFRESHING 10 MORE MESSAGES WILL LOAD----
    private void loadMoreMessages() {

        DatabaseReference messageRef = mRootReference.child("messages").child(mCurrentUserId).child(mChatUser);
        Query messageQuery = messageRef.orderByKey().endAt(mLastKey).limitToLast(10);

        messageQuery.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                Messages message = (Messages) dataSnapshot.getValue(Messages.class);
                String messageKey = dataSnapshot.getKey();


                if(!mPrevKey.equals(messageKey)){
                    messagesList.add(itemPos++,message);

                }
                else{
                    mPrevKey = mLastKey;
                }

                if(itemPos == 1){
                    String mMessageKey = dataSnapshot.getKey();
                    mLastKey = mMessageKey;
                }


                mMessageAdapter.notifyDataSetChanged();

                mSwipeRefreshLayout.setRefreshing(false);

                mLinearLayoutManager.scrollToPositionWithOffset(10,0);
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    //---THIS FUNCTION IS CALLED WHEN SYSTEM ACTIVITY IS CALLED---
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        //---FOR PICKING IMAGE FROM GALLERY ACTIVITY AND SENDING---
        if(requestCode == GALLERY_PICK && resultCode == RESULT_OK){

            //---GETTING IMAGE DATA IN FORM OF URI--
            Uri imageUri = data.getData();
            final String current_user_ref = "messages/"+mCurrentUserId+"/"+mChatUser;
            final String chat_user_ref = "messages/"+ mChatUser +"/"+mCurrentUserId;

            DatabaseReference user_message_push = mRootReference.child("messages")
                    .child(mCurrentUserId).child(mChatUser).push();

            final String push_id = user_message_push.getKey();

            //---PUSHING IMAGE INTO STORAGE---
            StorageReference filepath = mImageStorage.child("message_images").child(push_id+".jpg");
            filepath.putFile(imageUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {

                    if(task.isSuccessful()){

                        @SuppressWarnings("VisibleForTests")
                        String download_url = task.getResult().getDownloadUrl().toString();

                        Map messageMap = new HashMap();
                        messageMap.put("message",download_url);
                        messageMap.put("seen",false);
                        messageMap.put("type","image");
                        messageMap.put("time", ServerValue.TIMESTAMP);
                        messageMap.put("from",mCurrentUserId);

                        Map messageUserMap = new HashMap();
                        messageUserMap.put(current_user_ref+"/"+push_id,messageMap);
                        messageUserMap.put(chat_user_ref+"/"+push_id,messageMap);

                        mRootReference.updateChildren(messageUserMap, new DatabaseReference.CompletionListener(){

                            @Override
                            public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                                if(databaseError != null){
                                    Log.e("CHAT_ACTIVITY","Cannot add message to database");
                                }
                                else{
                                    Toast.makeText(ChatActivity.this, "Message sent", Toast.LENGTH_SHORT).show();
                                    mMessageView.setText("");
                                }

                            }
                        });
                    }

                }
            });


        }

    }

    @Override
    protected void onStart() {
        super.onStart();
        //mDatabaseReference.child(mCurrentUserId).child("online").setValue("true");
    }

    @Override
    protected void onStop() {
        super.onStop();
       // mDatabaseReference.child(mCurrentUserId).child("online").setValue(ServerValue.TIMESTAMP);

    }

//    @Override
//    public void onIncomingCall(CallClient callClient, Call call) {
//        Toast.makeText(this,"Incomming Call",Toast.LENGTH_LONG).show();
//    }
//
//    @Override
//    public void onCallProgressing(Call call) {
//        Toast.makeText(this,"Progress Call",Toast.LENGTH_LONG).show();
//    }
//
//    @Override
//    public void onCallEstablished(Call call) {
//        Toast.makeText(this,"Establish Call",Toast.LENGTH_LONG).show();
//    }
//
//    @Override
//    public void onCallEnded(Call call) {
//        Toast.makeText(this,"End Call",Toast.LENGTH_LONG).show();
//    }
//
//    @Override
//    public void onShouldSendPushNotification(Call call, List<PushPair> list) {
//        Toast.makeText(this,"Send Push",Toast.LENGTH_LONG).show();
//    }

    private class SinchCallClientListener implements CallClientListener {
        @Override
        public void onIncomingCall(CallClient callClient, Call incomingCall) {
         //   call = incomingCall;
            Toast.makeText(ChatActivity.this, "incoming call", Toast.LENGTH_SHORT).show();
            incomingCall.answer();
            incomingCall.addCallListener(new SinchCallListener());
          //  button.setText("Hang Up");
        }
    }
    private class SinchCallListener implements CallListener {
        @Override
        public void onCallEnded(Call endedCall) {
         //   call = null;
            SinchError a = endedCall.getDetails().getError();
          //  button.setText("Call");
          //  callState.setText("");
            setVolumeControlStream(AudioManager.USE_DEFAULT_STREAM_TYPE);
        }

        @Override
        public void onCallEstablished(Call establishedCall) {
           // callState.setText("connected");
            setVolumeControlStream(AudioManager.STREAM_VOICE_CALL);
            Toast.makeText(ChatActivity.this, "call connected", Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onCallProgressing(Call progressingCall) {
            Toast.makeText(ChatActivity.this, "call progressed", Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onShouldSendPushNotification(Call call, List<PushPair> pushPairs) {
        }
    }

}


 /*
            ActionBar action = getSupportActionBar();
            LayoutInflater inflater = (LayoutInflater)this.getSystemService(LAYOUT_INFLATER_SERVICE);
            View actionBarView = inflater.inflate(R.layout.app_bar_layout,null);
            action.setCustomView(actionBarView);
        */