package com.faisal.chatty;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.faisal.chatty.util.Constrants;
import com.faisal.chatty.util.SinchHelper;
import com.sinch.android.rtc.ClientRegistration;
import com.sinch.android.rtc.PushPair;
import com.sinch.android.rtc.Sinch;
import com.sinch.android.rtc.SinchClient;
import com.sinch.android.rtc.SinchClientListener;
import com.sinch.android.rtc.SinchError;
import com.sinch.android.rtc.calling.Call;
import com.sinch.android.rtc.calling.CallClient;
import com.sinch.android.rtc.calling.CallClientListener;
import com.sinch.android.rtc.calling.CallDetails;
import com.sinch.android.rtc.internal.service.http.SinchHttpServiceObserver;
import com.sinch.android.rtc.video.VideoCallListener;
import com.sinch.android.rtc.video.VideoController;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class CallActivity extends AppCompatActivity implements VideoCallListener, CallClientListener, SinchHelper.CallStateUpdater {
    private Context context;
    private String TAG="snitchlogger";
    private Call _call;
    private SinchHelper sinchHelper;
    private ImageView btnAccept;
    private ImageView btnReject;
    private TextView callStatus;
    private CircleImageView userImage;
    private LinearLayout myview;
    private LinearLayout remoteview;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_call);

        sinchHelper= SinchHelper.getInstance(getIntent().getStringExtra(Constrants.Intents.USER_ID),this,this);

        //init views
        userImage=findViewById(R.id.userimg);
        btnAccept=findViewById(R.id.btn_accept);
        btnReject=findViewById(R.id.btn_reject);
        callStatus=findViewById(R.id.call_status);
        myview=findViewById(R.id.myview);
        remoteview=findViewById(R.id.remoteview);

        btnAccept.setOnClickListener(v -> {
//            if(sinchHelper.getCall().getDetails().isVideoOffered())
//            {
//                Toast.makeText(this,"VideoCall",Toast.LENGTH_LONG).show();
//                activeVideoCall();
//                sinchHelper.answerCall();
//            }
//            else {
//                Toast.makeText(this,"Voice Call Only",Toast.LENGTH_LONG).show();
//                deActiveVideoCall();
//                sinchHelper.answerCall();
//
//            }
            sinchHelper.answerCall();

        });
        btnReject.setOnClickListener(v -> {
            sinchHelper.rejectCall();
            finish();
        });



    }
    private void activeVideoCall()
    {
        myview.setVisibility(View.VISIBLE);
        remoteview.setVisibility(View.VISIBLE);
        btnAccept.setVisibility(View.GONE);
        btnAccept.setVisibility(View.GONE);
    }
    private void deActiveVideoCall()
    {
        myview.setVisibility(View.GONE);
        remoteview.setVisibility(View.GONE);
        btnAccept.setVisibility(View.VISIBLE);
        btnReject.setVisibility(View.VISIBLE);
    }


    //start snitch on resume state
    @Override
    protected void onResume() {
        super.onResume();

    }


    @Override
    public void onVideoTrackAdded(Call call) {
        VideoController vc = sinchHelper.getSinchClient().getVideoController();
        View myPreview = vc.getLocalView();
        View remoteView = vc.getRemoteView();

    }
    //method which sets up the video feeds from the server to the UI of the activity
    private void addVideoViews() {


        final VideoController vc = sinchHelper.getSinchClient().getVideoController();
        if (vc != null) {
            LinearLayout localView =  findViewById(R.id.remoteview);
            localView.addView(vc.getLocalView());

            localView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //this toggles the front camera to rear camera and vice versa
                    vc.toggleCaptureDevicePosition();
                }
            });

            LinearLayout view = (LinearLayout) findViewById(R.id.myview);
            view.addView(vc.getRemoteView());
         //   mVideoViewsAdded = true;
        }
    }
    @Override
    public void onVideoTrackPaused(Call call) {

    }

    @Override
    public void onVideoTrackResumed(Call call) {

    }

    @Override
    public void onCallProgressing(Call call) {

    }

    @Override
    public void onCallEstablished(Call call) {

    }

    @Override
    public void onCallEnded(Call call) {

    }

    @Override
    public void onShouldSendPushNotification(Call call, List<PushPair> list) {

    }

    @Override
    public void onIncomingCall(CallClient callClient, Call call) {
        call.hangup();
    }

    @Override
    public void onCallConnected() {
        activeVideoCall();
        Toast.makeText(CallActivity.this,"Display Video",Toast.LENGTH_LONG).show();
        addVideoViews();
    }
}
