package com.faisal.chatty.main;
/**
 * Create by Sk. Faisal
 * purpose: main activity , display after login
 * email:faisal.hossain.pk@gmail.com
 * github:https://github.com/resilientbd
 * Date: 03/07/2020
 */

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;

import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.viewpager.widget.ViewPager;


import com.etebarian.meowbottomnavigation.MeowBottomNavigation;
import com.faisal.chatty.login.LoginActivity;
import com.faisal.chatty.R;
import com.faisal.chatty.setting.SettingActivity;
import com.faisal.chatty.user.UserActivity;
import com.faisal.util.local.ProgressbarHandler;
import com.faisal.chatty.base.BaseActivity;
import com.faisal.util.remote.SinchService;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.sinch.android.rtc.SinchError;

public class MainActivity extends BaseActivity implements SinchService.StartFailedListener {

    private FirebaseAuth mauth;
    ViewPager mviewPager;
    MyFragmentPagerAdapter mFragmentPagerAdapter;
    TabLayout mtabLayout;
    DatabaseReference mDatabaseReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ProgressbarHandler.ShowLoadingProgress(this);
        mauth = FirebaseAuth.getInstance();

        mviewPager = (ViewPager) findViewById(R.id.viewPager);

        //---ADDING ADAPTER FOR FRAGMENTS IN VIEW PAGER----
        mFragmentPagerAdapter = new MyFragmentPagerAdapter(getSupportFragmentManager());
        mviewPager.setAdapter(mFragmentPagerAdapter);

        //---SETTING TAB LAYOUT WITH VIEW PAGER
        mtabLayout = (TabLayout) findViewById(R.id.tabLayout);
        mtabLayout.setupWithViewPager(mviewPager);

        mDatabaseReference = FirebaseDatabase.getInstance().getReference().child("users");

        //check runtime permission
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requestPermissions(new String[]{Manifest.permission.RECORD_AUDIO, Manifest.permission.CAMERA, Manifest.permission.ACCESS_NETWORK_STATE, Manifest.permission.READ_PHONE_STATE}, 100);
        }


    }


    @Override
    public void onConnectService() {
        FirebaseUser user = mauth.getCurrentUser();
        if (user != null) {
            if (!getSinchServiceInterface().isStarted()) {
                getSinchServiceInterface().startClient(mauth.getCurrentUser().getUid());
                // Toast.makeText(MainActivity.this,"Sinch initialized!",Toast.LENGTH_LONG).show();
            }
        }
        ProgressbarHandler.DismissProgress(this);
    }

    //----SHOWING ALERT DIALOG FOR EXITING THE APP----
    @Override
    public void onBackPressed() {
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setMessage("Really Exit ??");
        builder.setTitle("Exit");
        builder.setCancelable(false);
        builder.setPositiveButton("Ok", new MainActivity.MyListener());
        builder.setNegativeButton("Cancel", null);
        builder.show();

    }

    @Override
    public void onStartFailed(SinchError error) {
        Toast.makeText(this, error.toString(), Toast.LENGTH_LONG).show();
    }

    @Override
    public void onStarted() {

    }

    @Override
    protected void onServiceConnected() {
        getSinchServiceInterface().setStartListener(this);
    }

    public class MyListener implements DialogInterface.OnClickListener {

        @Override
        public void onClick(DialogInterface dialog, int which) {
            finish();
        }
    }

    //---IF USER IS NULL , THEN GOTO LOGIN PAGE----
    @Override
    protected void onStart() {
        super.onStart();
        FirebaseUser user = mauth.getCurrentUser();
        if (user == null) {
            startfn();
        } else {
            //---IF LOGIN , ADD ONLINE VALUE TO TRUE---
            mDatabaseReference.child(user.getUid()).child("online").setValue("true");


        }
    }

    @Override
    protected void onStop() {
        super.onStop();

     /* //-----for disabling online function when appliction runs in background----
        FirebaseUser user=mauth.getCurrentUser();
        if(user!=null){
            mDatabaseReference.child(user.getUid()).child("online").setValue(ServerValue.TIMESTAMP);
        }
        */
    }

    //---CREATING OPTION MENU---
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        super.onOptionsItemSelected(item);

        if (item.getItemId() == R.id.settings) {
            Intent intent = new Intent(MainActivity.this, SettingActivity.class);
            startActivity(intent);
        }
        if (item.getItemId() == R.id.allUsers) {
            Intent intent = new Intent(MainActivity.this, UserActivity.class);
            startActivity(intent);

        }

        //---LOGGING OUT AND ADDING TIME_STAMP----
        if (item.getItemId() == R.id.logout) {
            mDatabaseReference.child(mauth.getCurrentUser().getUid()).child("online").setValue(ServerValue.TIMESTAMP).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if (task.isSuccessful()) {

                        FirebaseAuth.getInstance().signOut();
                        startfn();
                    } else {
                        Toast.makeText(MainActivity.this, "Try again..", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }
        return true;
    }

    //--OPENING LOGIN ACTIVITY--
    private void startfn() {
        Intent intent = new Intent(MainActivity.this, LoginActivity.class);
        startActivity(intent);
        finish();
    }
}
