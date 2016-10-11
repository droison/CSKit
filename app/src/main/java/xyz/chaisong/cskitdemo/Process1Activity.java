package xyz.chaisong.cskitdemo;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import xyz.chaisong.cskitdemo.event.IEventChangeNightMode;
import xyz.chaisong.cskitdemo.idlbus.BusProvider;

public class Process1Activity extends AppCompatActivity {

    Button mSendBtn;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_process1);

        BusProvider.init(this);

        mSendBtn = (Button) findViewById(R.id.btn_send);
        mSendBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                BusProvider.getBus().getReceiver(IEventChangeNightMode.class).changeNightMode(true);
            }
        });
    }
}
