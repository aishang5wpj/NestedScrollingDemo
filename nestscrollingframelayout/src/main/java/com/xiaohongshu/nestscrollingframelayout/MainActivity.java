package com.xiaohongshu.nestscrollingframelayout;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.view.View;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity implements IPrint {

    private TextView mNestedInfoTv;
    private ParentView mParentView;
    private ChildView mChildView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mNestedInfoTv = (TextView) findViewById(R.id.nested_info_tv);
        mNestedInfoTv.setMovementMethod(ScrollingMovementMethod.getInstance());
        mParentView = (ParentView) findViewById(R.id.parent_view);
        mParentView.setPrint(this);
        mChildView = (ChildView) findViewById(R.id.child_view);
        mChildView.setPrint(this);
    }

    public void clearInfo(View view) {
        mNestedInfoTv.setText("");
    }

    @Override
    public void printInfo(String text) {
        String content = String.format("%03d: %s \n", mNestedInfoTv.getLineCount(), text);
        mNestedInfoTv.append(content);
        textViewScroll2Bottom(mNestedInfoTv);
    }

    private void textViewScroll2Bottom(TextView textView) {
        int offset = textView.getLineCount() * textView.getLineHeight();
        if (offset > (textView.getHeight() - textView.getLineHeight() - 20)) {
            textView.scrollTo(0, offset - textView.getHeight() + textView.getLineHeight() + 20);
        }
    }
}
