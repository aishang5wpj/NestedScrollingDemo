package com.xiaohongshu.nestedscrollingrecyclerview;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

/**
 * Created by wupengjian on 17/11/14.
 */

public class MultiAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int TYPE_TEXT = 0;
    private static final int TYPE_IMAGE = 1;
    private static final int TYPE_WEB = 2;
    private List<Object> mData;

    public MultiAdapter(List<Object> list) {
        mData = list;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view;
        switch (viewType) {
            case TYPE_TEXT:
                view = LayoutInflater.from(parent.getContext()).inflate(R.layout.simple_list_text, null);
                return new SimpleTextViewHolder(view);
            case TYPE_WEB:
                view = LayoutInflater.from(parent.getContext()).inflate(R.layout.simple_list_web, null);
                return new SimpleWebViewHolder(view);
            case TYPE_IMAGE:
                view = LayoutInflater.from(parent.getContext()).inflate(R.layout.simple_list_image, null);
                return new SimpleImageViewHolder(view);
        }
        return null;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof SimpleImageViewHolder) {
            SimpleImageViewHolder viewHolder = (SimpleImageViewHolder) holder;
            int imageRes = (int) mData.get(position);
            viewHolder.mIv.setImageResource(imageRes);
        } else if (holder instanceof SimpleWebViewHolder) {
            SimpleWebViewHolder viewHolder = (SimpleWebViewHolder) holder;
            String url = (String) mData.get(position);
            viewHolder.mWebView.loadUrl(url);
        } else {
            SimpleTextViewHolder viewHolder = (SimpleTextViewHolder) holder;
            String text = (String) mData.get(position);
            viewHolder.mTv.setText(text);
        }
    }

    @Override
    public int getItemViewType(int position) {
        Object object = mData.get(position);
        if (object instanceof Integer) {
            return TYPE_IMAGE;
        } else if (object instanceof String) {
            String str = (String) object;
            if (str.startsWith("http")) {
                return TYPE_WEB;
            }
        }
        return TYPE_TEXT;
    }

    @Override
    public int getItemCount() {
        return mData == null ? 0 : mData.size();
    }

    private class SimpleTextViewHolder extends RecyclerView.ViewHolder {

        private TextView mTv;

        public SimpleTextViewHolder(View itemView) {
            super(itemView);
            mTv = (TextView) itemView.findViewById(R.id.text);
        }
    }

    private class SimpleImageViewHolder extends RecyclerView.ViewHolder {

        private ImageView mIv;

        public SimpleImageViewHolder(View itemView) {
            super(itemView);
            mIv = (ImageView) itemView.findViewById(R.id.image);
        }
    }

    private class SimpleWebViewHolder extends RecyclerView.ViewHolder {

        private WebView mWebView;

        public SimpleWebViewHolder(View itemView) {
            super(itemView);
            mWebView = (WebView) itemView.findViewById(R.id.webview);
        }
    }
}
