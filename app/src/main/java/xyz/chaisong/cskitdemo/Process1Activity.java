package xyz.chaisong.cskitdemo;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import xyz.chaisong.cskitdemo.event.IEventChangeNightMode;
import xyz.chaisong.mmbus.aidl.BusProvider;

public class Process1Activity extends AppCompatActivity implements IEventChangeNightMode{

    private static final String TAG = "Process1Activity";

    Button mSendBtn;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_process1);

        mSendBtn = (Button) findViewById(R.id.btn_send);
        mSendBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                BusProvider.getBus().getReceiver(IEventChangeNightMode.class).change(false, true, null, 2, "process1");
            }
        });


        BusProvider.getBus().register(IEventChangeNightMode.class,this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        BusProvider.getBus().unregister(this);
    }

    @Override
    public void change(Boolean arg1, boolean arg2, Integer arg3, int arg4, String arg5) {
        Log.i(TAG, System.currentTimeMillis() + " change() called with: arg1 = [" + arg1 + "], arg2 = [" + arg2 + "], arg3 = [" + arg3 + "], arg4 = [" + arg4 + "], arg5 = [" + arg5 + "]");
    }
}
