package com.xiaohongshu.velocityydemo;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

public class MainActivity extends AppCompatActivity {

    private VelocityView mVelocityView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mVelocityView = (VelocityView) findViewById(R.id.velocity_view);
    }

    public void fling(View view) {
        mVelocityView.fling(2000);
    }
}
