package com.github.lzyzsd.jsbridge;

import static com.github.lzyzsd.jsbridge.BridgeUtil.startActivity;

import android.graphics.Bitmap;
import android.util.Log;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import androidx.annotation.NonNull;

/**
 * Created by bruce on 10/28/15.
 */
public class BridgeWebViewClient extends WebViewClient {
    private final BridgeWebView webView;

    private boolean isLoadedBridgeJs = false;

    public BridgeWebViewClient(BridgeWebView webView) {
        this.webView = webView;
    }

    @Override
    public void onPageStarted(WebView view, String url, Bitmap favicon) {
        super.onPageStarted(view, url, favicon);
        if (!isLoadedBridgeJs) {
            BridgeUtil.getMainHandler().postDelayed(() -> {
                if (!isLoadedBridgeJs && webView.isAttachedToWindow()) {
                    isLoadedBridgeJs = true;
                    loadJs(webView);
                }
            }, 500);
        }
    }

    @Override
    public boolean shouldOverrideUrlLoading(WebView view, String url) {
        boolean schemeUri = false;
        if (view instanceof BridgeWebView) {
            schemeUri = ((BridgeWebView) view).isSupportOverrideSchemeUri();
        }
        if (schemeUri && overrideUrlLoadUrl(view, url)) return true;
        return super.shouldOverrideUrlLoading(view, url);
    }

    @Override
    public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
        String uri = request.getUrl().toString();
        boolean schemeUri = false;
        if (view instanceof BridgeWebView) {
            schemeUri = ((BridgeWebView) view).isSupportOverrideSchemeUri();
        }
        if (schemeUri && !uri.isEmpty()) {
            if(overrideUrlLoadUrl(view, uri)) return true;
        }
        return super.shouldOverrideUrlLoading(view, request);
    }

    private boolean overrideUrlLoadUrl(WebView view, @NonNull String url) {
        if (url.contains("http://") || url.contains("https://")) { //加载的url是http/https协议地址
            view.loadUrl(url);
            return false; //返回false表示此url默认由系统处理,url未加载完成，会继续往下走
        } else { //加载的url是自定义协议地址
            startActivity(view, url);
            return true;
        }
    }

    @Override
    public void onPageFinished(WebView view, String url) {
        super.onPageFinished(view, url);
        if (!isLoadedBridgeJs) {
            isLoadedBridgeJs = true;
            loadJs(view);
        }
    }

    private void loadJs(WebView view) {
        Log.d(BridgeUtil.TAG, "load js!!");
        BridgeUtil.webViewLoadLocalJs(view, BridgeWebView.toLoadJs);
        Log.d(BridgeUtil.TAG, "load js end!!");
        onLoadJsExtra();
        webView.bridgeObject.clearStartupMessage();
    }

    /**
     * 允许当js完成后的回调。用于二次设定额外的js。
     */
    protected void onLoadJsExtra() {}
}