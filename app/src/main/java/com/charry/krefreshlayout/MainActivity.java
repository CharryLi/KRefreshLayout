package com.charry.krefreshlayout;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.TypedValue;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.charry.krefreshlayout.widget.KRefreshLayout;
import com.charry.krefreshlayout.widget.RefreshNormalHead;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity implements KRefreshLayout.KOnRefreshListener {

    private ListView mListView;
    private KRefreshLayout mRefreshLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mRefreshLayout = (KRefreshLayout) findViewById(R.id.refreshlayout);
        mRefreshLayout.setOnRefreshListener(this);
        mRefreshLayout.setCustomHeadView(new RefreshNormalHead(this));
        mRefreshLayout.setHeadViewHeight((int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 70, getResources().getDisplayMetrics()));
        mListView = (ListView) findViewById(R.id.listview);

        List<String> datas = new ArrayList<>();
        for (int i = 0; i < 20; i++) {
            datas.add("item" + i);
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, datas);
        mListView.setAdapter(adapter);

        mRefreshLayout.startRefreshWithCallBack(100);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.m_item_1:
                mRefreshLayout.startRefreshWithCallBack();
                break;

            case R.id.m_item_2:
                mRefreshLayout.setOverlay(!mRefreshLayout.isOverlay());
                item.setTitle(mRefreshLayout.isOverlay() ? "Follow Model" : "Overlay Model");
                mRefreshLayout.startRefreshWithCallBack();
                break;
        }
        return true;
    }

    @Override
    public void onRefresh() {
        Timer timer = new Timer(true);
        TimerTask task = new TimerTask() {
            @Override
            public void run() {
                MainActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mRefreshLayout.finishRefresh();
                    }
                });
            }
        };
        timer.schedule(task, 5000);
    }
}
