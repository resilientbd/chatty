package com.faisal.chatty.util;

import android.content.Context;
import android.util.Log;

import com.sinch.android.rtc.ClientRegistration;
import com.sinch.android.rtc.Sinch;
import com.sinch.android.rtc.SinchClient;
import com.sinch.android.rtc.SinchClientListener;
import com.sinch.android.rtc.SinchError;
import com.sinch.android.rtc.calling.Call;
import com.sinch.android.rtc.calling.CallClientListener;
import com.sinch.android.rtc.calling.CallListener;


public class SinchHelper {
    private SinchClient sinchClient;
    private Call call;
    private String TAG = "snitchlogger";
    private static SinchHelper sinchHelper;
    private CallClientListener callClientListener;

    private SinchHelper(String userid,Context context, CallClientListener callClientListener) {
        sinchClient = Sinch.getSinchClientBuilder().context(context)
                .applicationKey(Constrants.SnitchFlags.APP_KEY)
                .applicationSecret(Constrants.SnitchFlags.APP_SECRET)
                .environmentHost(Constrants.SnitchFlags.ENVIRONMENT)
                .userId(userid)
                .build();
        this.callClientListener=callClientListener;
        // Specify the client capabilities.
        sinchClient.setSupportCalling(true);
        sinchClient.setSupportManagedPush(true);
        sinchClient.addSinchClientListener(new SinchClientListener() {

            public void onClientStarted(SinchClient client) {
                Log.d(TAG, "client started!");
            }

            public void onClientStopped(SinchClient client) {
                Log.d(TAG, "client stopped!");
            }

            public void onClientFailed(SinchClient client, SinchError error) {
                Log.d(TAG, "client failed!");
            }

            public void onRegistrationCredentialsRequired(SinchClient client, ClientRegistration registrationCallback) {
                Log.d(TAG, "client registration credential required! " + registrationCallback.toString());
            }

            public void onLogMessage(int level, String area, String message) {
                Log.d(TAG, "client log message: " + message);
            }
        });

        sinchClient.getCallClient().addCallClientListener(callClientListener);
        sinchClient.start();
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
        if(call!=null)
        {
            try
            {
                call.answer();
            }catch (Exception e)
            {
                Log.d(TAG,"Recieve Error: "+e.getMessage());
            }
        }
    }
    public void makeaVoiceCall(String userid, CallListener callListener)
    {

            call=sinchClient.getCallClient().callUser(userid);
            call.addCallListener(callListener);


    }
}
