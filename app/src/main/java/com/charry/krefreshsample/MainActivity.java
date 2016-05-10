package com.charry.krefreshsample;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.TypedValue;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.charry.krefresh.KRefreshLayout;
import com.charry.krefreshsample.widget.CustomRefreshHeadView;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity implements KRefreshLayout.KOnRefreshListener {

    private ListView mListView;
    private KRefreshLayout mRefreshLayout;
    private List<String> mDatas;
    private ArrayAdapter<String> mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mRefreshLayout = (KRefreshLayout) findViewById(R.id.refreshlayout);
        mRefreshLayout.setOnRefreshListener(this);
        mListView = (ListView) findViewById(R.id.listview);

        mDatas = new ArrayList<>();

        mAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, mDatas);
        mListView.setAdapter(mAdapter);

        mRefreshLayout.startRefreshWithCallBack(100);
    }

    private void initDatas() {
        mDatas.clear();
        for (int i = 0; i < 20; i++) {
            mDatas.add("item" + i);
        }
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
            case R.id.m_item_1:// 自动刷新
                mRefreshLayout.startRefreshWithCallBack();
                break;

            case R.id.m_item_2:// 默认覆盖模式 可切换为跟随模式
                mRefreshLayout.setOverlay(!mRefreshLayout.isOverlay());
                item.setTitle(mRefreshLayout.isOverlay() ? "Follow Model" : "Overlay Model");
                mRefreshLayout.startRefreshWithCallBack();
                Toast.makeText(MainActivity.this, mRefreshLayout.isOverlay() ? "Overlay Model" : "Follow Model", Toast.LENGTH_SHORT).show();
                break;

            case R.id.m_item_3:// 是否启用下拉刷新功能
                mRefreshLayout.setEnablePullRefresh(!mRefreshLayout.isEnablePullRefresh());
                item.setTitle(mRefreshLayout.isEnablePullRefresh() ? "PullRefresh Disabled" : "PullRefresh Enabled");
                Toast.makeText(MainActivity.this, mRefreshLayout.isEnablePullRefresh() ? "PullRefresh Enabled" : "PullRefresh Disabled", Toast.LENGTH_SHORT).show();
                break;

            case R.id.m_item_4:// 是否启用上拉刷新功能
                mRefreshLayout.setEnableLoadMore(!mRefreshLayout.isEnableLoadMore());
                item.setTitle(mRefreshLayout.isEnableLoadMore() ? "LoadMore Disabled" : "LoadMore Enabled");
                Toast.makeText(MainActivity.this, mRefreshLayout.isEnableLoadMore() ? "LoadMore Enabled" : "LoadMore Disabled", Toast.LENGTH_SHORT).show();
                break;

            case R.id.m_item_5:// 自定义刷新头部(注意setHeadViewHeight一定要在setCustomHeadView之前，否则头部高度不正确)
                mRefreshLayout.setHeadViewHeight((int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 90, getResources().getDisplayMetrics()));
                mRefreshLayout.setCustomHeadView(new CustomRefreshHeadView(this));
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
                        initDatas();
                        mAdapter.notifyDataSetChanged();
                    }
                });
            }
        };
        timer.schedule(task, 4000);
    }

    @Override
    public void onLoadMore() {
        Timer timer = new Timer(true);
        TimerTask task = new TimerTask() {
            @Override
            public void run() {
                MainActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mRefreshLayout.finishLoadMore();
                        mDatas.add("more data");
                        mAdapter.notifyDataSetChanged();
                    }
                });
            }
        };
        timer.schedule(task, 4000);
    }
}
