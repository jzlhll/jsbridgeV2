package com.github.lzyzsd.jsbridge;

import android.os.Bundle;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.github.au.jsbridgev2.R;

public class BridgeWebViewExFragment extends Fragment implements BridgeExWebView.OnH5EventListener {
    private BridgeExWebView mWebView;
    private ViewGroup mRoot;
    @NonNull
    public ViewGroup getRoot() {return mRoot;}
    private ViewGroup mFullVideoLayout;
    @NonNull
    public ViewGroup getFullVideoLayout() {return mFullVideoLayout;}
    @NonNull
    public BridgeExWebView getWebView() {return mWebView;}

    /**
     * 参考bridge_web_view.xml，保留webView和fullVideoLayout。
     */
    protected int bridgeWebViewLayoutId() {
        return R.layout.bridge_web_view;
    }

    /**
     * 允许继承修改UA。
     */
    protected String customUserAgent(String originalUA) {
        return originalUA;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(bridgeWebViewLayoutId(), container, false);
        mRoot = (ViewGroup) view;
        mWebView = view.findViewById(R.id.webView);
        mWebView.getSettings().setTextZoom(100); // 禁止文字缩放
        mWebView.getSettings().setUserAgentString(customUserAgent(mWebView.getSettings().getUserAgentString()));
        mFullVideoLayout = view.findViewById(R.id.fullVideoLayout);
        mWebView.fullLayout = mFullVideoLayout;
        mWebView.activity = getActivity();
        mWebView.addOnH5EventListener(this);
        return view;
    }

    @Override
    public void onH5Event(@NonNull BridgeExWebView webView, @NonNull String eventName, String msg, @NonNull CallBackFunction call) {
    }

    /**
     * 发送消息给h5
     */
    protected void sendEventToH5(String event, boolean isCache) {
        if (Looper.getMainLooper().getThread() == Thread.currentThread()) {
            mWebView.sendEventToH5("nativeCallWeb", event);
        } else {
            BridgeUtil.getMainHandler().post(() -> mWebView.sendEventToH5("nativeCallWeb", event));
        }
    }

    /**
     * 开始加载链接
     */
    public void loadUrl(String url) {
        if (url == null || url.isEmpty()) {
            return;
        }
        mWebView.loadUrl(url);
    }

    protected void goBack() {
        if (mWebView.canGoBack()) {
            mWebView.goBack();
        } else {
            if (getActivity() != null) {
                getActivity().finishAfterTransition();
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        mWebView.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        mWebView.onPause();
    }

    @Override
    public void onDestroy() {
        BridgeExWebView webView = mWebView;
        webView.removeOnH5EventListener(this);

        ViewGroup parent = asOrNull(webView.getParent(), ViewGroup.class);
        if (parent != null) {
            parent.removeView(webView);
        }
        webView.stopLoading();
        // 退出时调用此方法，移除绑定的服务，否则某些特定系统会报错
        webView.getSettings().setJavaScriptEnabled(false);
        // webView.clearHistory();
        webView.removeAllViews();
        webView.destroy();

        webView.activity = null;
        webView.fullLayout = null;
        super.onDestroy();
    }

    @SuppressWarnings("unchecked")
    private <T> T asOrNull(Object obj, Class<T> cls) {
        if (cls.isInstance(obj)) {
            return (T) obj;
        }
        return null;
    }
}