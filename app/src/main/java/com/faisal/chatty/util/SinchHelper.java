package com.faisal.chatty.util;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import com.sinch.android.rtc.ClientRegistration;
import com.sinch.android.rtc.Sinch;
import com.sinch.android.rtc.SinchClient;
import com.sinch.android.rtc.SinchClientListener;
import com.sinch.android.rtc.SinchError;
import com.sinch.android.rtc.calling.Call;
import com.sinch.android.rtc.calling.CallClientListener;
import com.sinch.android.rtc.calling.CallListener;

import java.io.Serializable;

import static com.faisal.chatty.util.Constrants.SnitchFlags.APP_KEY;
import static com.faisal.chatty.util.Constrants.SnitchFlags.APP_SECRET;
import static com.faisal.chatty.util.Constrants.SnitchFlags.ENVIRONMENT;


public class SinchHelper implements Serializable {
    private SinchClient sinchClient;
    private Call call;
    private String TAG = "snitchlogger";
    private static SinchHelper sinchHelper;
    private CallClientListener callClientListener;
    private CallStateUpdater callStateUpdater;
    private SinchHelper(String userid,Context context, CallClientListener callClientListener) {
        sinchClient = Sinch.getSinchClientBuilder()
                .context(context)
                .userId(userid)
                .applicationKey(APP_KEY)
                .applicationSecret(APP_SECRET)
                .environmentHost(ENVIRONMENT)
                .build();

        sinchClient.setSupportCalling(true);
        sinchClient.startListeningOnActiveConnection();
        if(!sinchClient.isStarted())
        {
            sinchClient.start();
            Toast.makeText(context,"Sinch Started!",Toast.LENGTH_LONG).show();
        }
        else {
            Toast.makeText(context,"Sinch Already Started!",Toast.LENGTH_LONG).show();
        }

        sinchClient.getCallClient().addCallClientListener(callClientListener);
     //   sinchClient.start();
    }

    public CallClientListener getCallClientListener() {
        return callClientListener;
    }

    public SinchClient getSinchClient() {
        return sinchClient;
    }

    public static SinchHelper getInstance(String userid, Context context, CallClientListener callClientListener) {
        if (sinchHelper == null) {
            sinchHelper = new SinchHelper(userid,context, callClientListener);
        }
        return sinchHelper;
    }

    public void setCallStateUpdater(CallStateUpdater callStateUpdater) {
        this.callStateUpdater = callStateUpdater;
    }

    public void setCall(Call call) {
        this.call = call;
    }

    public Call getCall() {
        return call;
    }
    public void stopSnitch() {
        sinchClient.stopListeningOnActiveConnection();
        sinchClient.terminate();
    }
    public void hangUpCall()
    {
        if(call!=null)
        {
            try
            {
                call.hangup();
            }catch (Exception e)
            {
                Log.d(TAG,"Hangup Error: "+e.getMessage());
            }
        }
    }
    public void callPhoneNumber(String number){
        sinchClient.getCallClient().callPhoneNumber(number);
    }
    public void answerCall()
    {

            try
            {
                call.answer();
            }catch (Exception e)
            {
                Log.d(TAG,"Recieve Error: "+e.getMessage());
            }

    }
    public void rejectCall(){
        call.hangup();
    }
    public void makeaVoiceCall(String userid, CallListener callListener)
    {

            call=sinchClient.getCallClient().callUser(userid);
            call.addCallListener(callListener);


    }
    public void makeaVideoCall(String userid, CallListener callListener)
    {
       call=sinchClient.getCallClient().callUserVideo(userid);
       call.addCallListener(callListener);
    }

    public interface CallStateUpdater{
        public void onCallConnected();
    }
}
