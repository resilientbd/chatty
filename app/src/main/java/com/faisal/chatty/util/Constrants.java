package com.faisal.chatty.util;

import android.content.Context;
import android.provider.Settings;

public class Constrants {
    public interface SnitchFlags{
        public static final String APP_KEY = "88eacdea-7ccd-49fb-8d93-6ac03988fad7";
        public static final String APP_SECRET = "xz8hYoa38Em05lBUw2KzKQ==";
        public static final String ENVIRONMENT = "clientapi.sinch.com";
        public static final String USER_ID = "222";
        //public static final String USER_ID = "1111";
    }
    public interface CallFlags{
        public static final int INCOMMING_CALL=1;
        public static final int OUTGOING_CALL=2;
    }
    public interface Intents{
        public static final String SINCH_HANDLER="sinch_handler";
        String USER_ID = "userid";
        String USER_NAME = "username";
        String USER_IMAGE = "userimg";
    }
    public static String getDeviceId(Context context)
    {
        return  Settings.Secure.getString(context.getContentResolver(),Settings.Secure.ANDROID_ID);
    }

}
