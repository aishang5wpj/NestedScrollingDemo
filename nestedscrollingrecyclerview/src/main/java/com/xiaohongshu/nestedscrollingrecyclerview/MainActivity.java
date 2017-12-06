package com.xiaohongshu.nestedscrollingrecyclerview;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private MultiAdapter mAdapter;
    private NestedScrollingRecyclerView mRecyclerView;
    private List<Object> mData = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mRecyclerView = (NestedScrollingRecyclerView) findViewById(android.R.id.list);
        mRecyclerView.setLayoutManager(new NestedScrollingLinearLayoutManager(this));
        mAdapter = new MultiAdapter(mData);
        mRecyclerView.setAdapter(mAdapter);

        mData.add(R.mipmap.image01);
//        mData.add("http://www.xiaohongshu.com/discovery/item/59f0a0773460944cc76b83cb");
        mData.add("http://www.xiaohongshu.com/discovery/item/59e7ff6f910cf60770bf0724");
        mData.add(R.mipmap.image02);
        mData.add(R.mipmap.image03);
        mData.add(R.mipmap.image04);
        mData.add(R.mipmap.image05);
        mData.add(R.mipmap.image06);
//        mData.add(NESTED_SCROLLING_POS, getString(R.string.lyric));
        mAdapter.notifyDataSetChanged();
    }
}
