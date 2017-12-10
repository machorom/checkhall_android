package com.checkhall;

import android.*;
import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;

import com.checkhall.util.DeviceUtil;

import java.util.HashMap;
import java.util.Hashtable;


public class IntroActivity extends AppCompatActivity {
    private static final String TAG = "LCheckhall:IntroActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_intro);
        //접근 권한 정책...
        checkPermissions();
    }

    private void initProcess(){
        if ( !checkFCMIntent() ) {
            checkLogined();
            Log.d(TAG , "uuid=" + DeviceUtil.getDeviceUUID(this));
            ImageView button = (ImageView) findViewById(R.id.button);
            button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent i = new Intent(IntroActivity.this, MainActivity.class);
                    startActivity(i);
                    finish();
                }
            });
        }
    }


    private int permissionOffset = 0;
    private void checkPermissions(){
        Log.d(TAG ,"checkPermissions permissionOffset="+permissionOffset);
        if( permissionOffset < targetPermissions.length ){
            int executeoffset = permissionOffset;
            permissionOffset++;
            checkPermission(targetPermissions[executeoffset][0], targetPermissions[executeoffset][1], getPermissionRequestId(targetPermissions[executeoffset][0]));
        } else {
            initProcess();
        }

    }
    private int getPermissionRequestId(String per){
        if(per.equals(android.Manifest.permission.READ_CONTACTS)){
            return MY_PERMISSIONS_REQUEST_READ_CONTACTS;
        }else if(per.equals(android.Manifest.permission.CALL_PHONE)){
            return MY_PERMISSIONS_REQUEST_CALL_PHONE;
        }else if(per.equals(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)){
            return MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE;
        }
        return 0;
    }

    private final static int MY_PERMISSIONS_REQUEST_READ_CONTACTS = 1;
    private final static int MY_PERMISSIONS_REQUEST_CALL_PHONE = 2;
    private final static int MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE = 3;
    String[][] targetPermissions = {
            {android.Manifest.permission.READ_CONTACTS ,"SMS 보내기 용도로 READ_CONTACTS 권한이 필요합니다."},
            {android.Manifest.permission.CALL_PHONE ,"전화걸기 용도로 READ_CONTACTS 권한이 필요합니다."},
            {android.Manifest.permission.WRITE_EXTERNAL_STORAGE ,"전화걸기 용도로 READ_CONTACTS 권한이 필요합니다."}
    };
    HashMap<Integer, Integer> completePermission = new HashMap<Integer, Integer>();
    private void addCompletePermission(int reqeustID){
        Log.d(TAG ,"addCompletePermission "+reqeustID);
        completePermission.put(reqeustID,1);
        if ( completePermission.size() >= 3)
            initProcess();
    }

    private void checkPermission(final String permission, final String msg, final int requestID){
        // Activity에서 실행하는경우
        if (ContextCompat.checkSelfPermission(this, permission)!= PackageManager.PERMISSION_GRANTED) {
            Log.d(TAG,"checkPermission["+permission+"] request");
            // 이 권한을 필요한 이유를 설명해야하는가?
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, permission)) {
                Log.d(TAG,"checkPermission - shouldShowRequestPermissionRationale["+permission+"] true");
                // 다이어로그같은것을 띄워서 사용자에게 해당 권한이 필요한 이유에 대해 설명합니다
                // 해당 설명이 끝난뒤 requestPermissions()함수를 호출하여 권한허가를 요청해야 합니다
                AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
                        this);
                alertDialogBuilder
                        .setMessage(msg)
                        .setPositiveButton("확인",
                                new DialogInterface.OnClickListener() {
                                    public void onClick(
                                            DialogInterface dialog, int id) {
                                        ActivityCompat.requestPermissions(IntroActivity.this,
                                                new String[]{ permission},
                                                requestID);
                                        dialog.cancel();
                                    }
                                })
                        .setNegativeButton("취소",
                                new DialogInterface.OnClickListener() {
                                    public void onClick(
                                            DialogInterface dialog, int id) {
                                        // 다이얼로그를 취소한다
                                        dialog.cancel();
                                    }
                                });
                AlertDialog alertDialog = alertDialogBuilder.create();
                alertDialog.show();

            } else {
                Log.d(TAG,"checkPermission - shouldShowRequestPermissionRationale["+permission+"] false");
                ActivityCompat.requestPermissions(this,
                        new String[]{ permission},
                        requestID);
                // 필요한 권한과 요청 코드를 넣어서 권한허가요청에 대한 결과를 받아야 합니다
            }
        } else {
            Log.d(TAG,"checkPermission - not need");
            checkPermissions();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_READ_CONTACTS:
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Log.d(TAG,"onRequestPermissionsResult/MY_PERMISSIONS_REQUEST_READ_CONTACTS OK");
                } else {
                    Log.d(TAG,"onRequestPermissionsResult/MY_PERMISSIONS_REQUEST_READ_CONTACTS reject");
                }
                break;
            case MY_PERMISSIONS_REQUEST_CALL_PHONE:
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Log.d(TAG,"onRequestPermissionsResult/MY_PERMISSIONS_REQUEST_CALL_PHONE OK");
                } else {
                    Log.d(TAG,"onRequestPermissionsResult/MY_PERMISSIONS_REQUEST_CALL_PHONE reject");
                }
                break;
            case MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE:
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Log.d(TAG,"onRequestPermissionsResult/MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE OK");
                } else {
                    Log.d(TAG,"onRequestPermissionsResult/MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE reject");
                }
                break;
        }
        checkPermissions();
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    private boolean checkFCMIntent(){
        Log.i(TAG,"title=" + getIntent().getStringExtra("title"));
        Log.i(TAG,"body=" + getIntent().getStringExtra("body"));
        Log.i(TAG,"action_url=" + getIntent().getStringExtra("action_url"));
        if(getIntent().getStringExtra("action_url") != null && !getIntent().getStringExtra("action_url").isEmpty() && !getIntent().getStringExtra("action_url").equals("")){
            Log.i(TAG,"intent with url=" + getIntent().getStringExtra("action_url"));
            Intent i = new Intent(IntroActivity.this, MainActivity.class);
            i.putExtra("action_url",getIntent().getStringExtra("action_url"));
            i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(i);
            finish();
            return true;
        }
        return false;
    }

    private void checkLogined(){
        if( DeviceUtil.isLogined(this)) {
            Intent i = new Intent(IntroActivity.this, MainActivity.class);
            startActivity(i);
            finish();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}

