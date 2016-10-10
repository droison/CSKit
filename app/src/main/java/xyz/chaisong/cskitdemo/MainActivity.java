package xyz.chaisong.cskitdemo;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;

import xyz.chaisong.cskitdemo.idlBus.BusIDLService;
import xyz.chaisong.cskitdemo.network.QDNetUtil;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        startService(new Intent(this, BusIDLService.class));

        QDNetUtil.init(this);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });


        QDPrefetcher prefetcher = new QDPrefetcher(this);

        prefetcher.add(29158);
        prefetcher.add(29016);
        prefetcher.add(28657);
        prefetcher.add(28516);
        prefetcher.add(28848);
        prefetcher.add(28960);

        prefetcher.add(29158);
        prefetcher.add(29016);
        prefetcher.add(28657);
        prefetcher.add(28516);
        prefetcher.add(28848);
        prefetcher.add(28960);

        prefetcher.add(29158);
        prefetcher.add(29016);
        prefetcher.add(28657);
        prefetcher.add(28516);
        prefetcher.add(28848);
        prefetcher.add(28960);

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
