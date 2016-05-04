package xyz.chaisong.cskitdemo;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;

import xyz.chaisong.cskitdemo.network.QDNetUtil;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
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

        prefetcher.add(25268);
        prefetcher.add(25256);
        prefetcher.add(25203);
        prefetcher.add(25155);
        prefetcher.add(25045);
        prefetcher.add(25252);

        prefetcher.add(25268);
        prefetcher.add(25256);
        prefetcher.add(25203);
        prefetcher.add(25155);
        prefetcher.add(25045);
        prefetcher.add(25252);

        prefetcher.add(25268);
        prefetcher.add(25256);
        prefetcher.add(25203);
        prefetcher.add(25155);
        prefetcher.add(25045);
        prefetcher.add(25252);

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
