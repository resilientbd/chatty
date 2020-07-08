package com.faisal.chatty;

import android.content.Context;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.faisal.chatty.util.Constrants;
import com.faisal.chatty.util.SinchHelper;
import com.sinch.android.rtc.PushPair;
import com.sinch.android.rtc.calling.Call;
import com.sinch.android.rtc.calling.CallClient;
import com.sinch.android.rtc.calling.CallClientListener;
import com.sinch.android.rtc.video.VideoCallListener;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class VideoCallActivity extends AppCompatActivity implements VideoCallListener, CallClientListener {
    private Context context;
    private String TAG="snitchlogger";
    private Call _call;
    private SinchHelper sinchHelper;
    private ImageView btnAccept;
    private ImageView btnReject;
    private TextView callStatus;
    private CircleImageView userImage;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_call);

        sinchHelper= SinchHelper.getInstance(getIntent().getStringExtra(Constrants.Intents.USER_ID),this,this);

        //init views
        userImage=findViewById(R.id.userimg);
        btnAccept=findViewById(R.id.btn_accept);
        btnReject=findViewById(R.id.btn_reject);
        callStatus=findViewById(R.id.call_status);

        btnAccept.setOnClickListener(v -> {
            sinchHelper.answerCall();
        });
        btnReject.setOnClickListener(v -> {
            sinchHelper.rejectCall();
        });

        if(sinchHelper.getCall().getDetails().isVideoOffered())
        {
            Toast.makeText(this,"VideoCall",Toast.LENGTH_LONG).show();
        }

    }

    //start snitch on resume state
    @Override
    protected void onResume() {
        super.onResume();

    }


    @Override
    public void onVideoTrackAdded(Call call) {
//        VideoController vc = getSinchServiceInterface().getVideoController();
//        View myPreview = vc.getLocalView();
//        View remoteView = vc.getRemoteView();
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
}
