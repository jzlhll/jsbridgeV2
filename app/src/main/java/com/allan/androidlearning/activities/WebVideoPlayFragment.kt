package com.allan.androidlearning.activities

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import androidx.lifecycle.lifecycleScope
import com.allan.androidlearning.R
import com.allan.androidlearning.utils.onClick
import com.github.lzyzsd.jsbridge.BridgeWebViewExFragment
import kotlinx.coroutines.launch

class WebVideoPlayFragment : BridgeWebViewExFragment() {
    override fun bridgeWebViewLayoutId(): Int {
        return R.layout.bridge_web_view_play
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        root.findViewById<Button>(R.id.startEchartsHtmlBtn).onClick {
            startActivity(Intent(context, WebEChartsHtmlRtcRecordActivity::class.java))
        }

        root.findViewById<Button>(R.id.startEchartsSoftWebViewBtn).onClick {
            startActivity(Intent(context, WebEChartsWebViewSoftRecordActivity::class.java))
        }

        root.findViewById<Button>(R.id.startEchartsBitmapBtn).onClick {
            startActivity(Intent(context, WebEChartsGetDataUrlBitmapActivity::class.java))
        }

        lifecycleScope.launch {
            webView.loadUrl("https://v.qq.com/x/cover/mzc00200zi9e1k6/v3311o0ash6.html")
        }
    }
}