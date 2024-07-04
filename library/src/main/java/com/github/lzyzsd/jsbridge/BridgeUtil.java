package com.github.lzyzsd.jsbridge;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.webkit.WebView;

import androidx.annotation.NonNull;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

public final class BridgeUtil {
    private BridgeUtil() {}

    public final static String TAG = "JsBridge";
//    final static String JS_HANDLE_MESSAGE_FROM_JAVA = "WebViewJavascriptBridge._handleMessageFromNative(%s);";
  final static String JS_HANDLE_MESSAGE_FROM_JAVA = "WebViewJavascriptBridge._handleMessageFromNative('%s');";
    public static void webViewLoadLocalJs(WebView view, String path) {
        String jsContent = assetFile2Str(view.getContext(), path);
       // view.loadUrl("javascript:" + jsContent);
        view.evaluateJavascript(jsContent, null);
    }

    public static String assetFile2Str(Context c, String urlStr) {
        try (InputStream in = c.getAssets().open(urlStr);
             BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(in));) {
            String line;
            StringBuilder sb = new StringBuilder();
            do {
                line = bufferedReader.readLine();
                if (line != null && !line.matches("^\\s*//.*")) {
                    sb.append(line);
                }
            } while (line != null);

            return sb.toString();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private static Handler mainHandler;

    @NonNull
    public static Handler getMainHandler() {
        if (mainHandler == null) {
            mainHandler = new Handler(Looper.getMainLooper());
        }
        return mainHandler;
    }

    static void startActivity(WebView view, @NonNull String url) {
        startActivity(view, Uri.parse(url));
    }

    static void startActivity(WebView view, @NonNull Uri uri) {
        Intent intent = new Intent(Intent.ACTION_VIEW, uri);
        try {
            view.getContext().startActivity(intent);
        } catch (Exception e) {
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            try {
                view.getContext().startActivity(intent);
            } catch (Exception e2) {
                e2.printStackTrace();
            }
        }
    }
}
