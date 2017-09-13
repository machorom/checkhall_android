package com.checkhall;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;

import com.checkhall.util.DeviceUtil;


public class IntroActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_intro);

        if ( !checkFCMIntent() ) {
            checkLogined();
            Log.d("IntroActivity", "uuid=" + DeviceUtil.getDeviceUUID(this));
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

    private boolean checkFCMIntent(){
        Log.i("Intro","title=" + getIntent().getStringExtra("title"));
        Log.i("Intro","body=" + getIntent().getStringExtra("body"));
        Log.i("Intro","action_url=" + getIntent().getStringExtra("action_url"));
        if(getIntent().getStringExtra("action_url") != null && !getIntent().getStringExtra("action_url").isEmpty() && !getIntent().getStringExtra("action_url").equals("")){
            Log.i("Intro","intent with url=" + getIntent().getStringExtra("action_url"));
            Intent i = new Intent(IntroActivity.this, MainActivity.class);
            i.putExtra("action_url",getIntent().getStringExtra("action_url"));
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

