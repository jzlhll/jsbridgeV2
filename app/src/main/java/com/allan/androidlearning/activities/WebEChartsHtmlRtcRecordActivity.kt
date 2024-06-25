package com.allan.androidlearning.activities

import android.os.Bundle
import android.util.Base64
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.FragmentContainerView
import androidx.lifecycle.lifecycleScope
import com.allan.androidlearning.R
import com.allan.androidlearning.utils.UnzipHelper
import com.allan.androidlearning.utils.logd
import com.github.lzyzsd.jsbridge.BridgeWebViewExFragment
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.text.SimpleDateFormat

class WebEChartsHtmlRtcRecordActivity : AppCompatActivity() {
    companion object {
        var unziped = false
    }

    private val unzipHelper = UnzipHelper()
    private fun echartsCacheDir() = cacheDir.path + "/echarts_record"

    private fun findIndexHtml() : String {
        val echartsCacheDir = echartsCacheDir()
        val path = "$echartsCacheDir/index.html"
        return path
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.fragment_container_view)
        val root = findViewById<FragmentContainerView>(R.id.fragmentRoot)
        val h5 = BridgeWebViewExFragment()
        supportFragmentManager.beginTransaction()
            .replace(root.id, h5)
            .commitAllowingStateLoss()

        unzip {
            lifecycleScope.launch {
                //可以寻找更合适的实际
                h5.webView.registerHandler("sendVideoBlobBase64") {
                        data, cb->
                    logd { "allanlog get js base64 blob size: " + data.length }
                    lifecycleScope.launch(Dispatchers.IO) {
                        cb.onCallBack("success: " + saveToCache(data))
                    }
                }

                h5.loadUrl("file://" + findIndexHtml())
            }
        }
    }

    fun millisToTime(): String {
        //首先获取当前的毫秒值
        val currentTimeMillis = System.currentTimeMillis()
        /**
         * 转换为年月日时分秒的形式,得到的currentTime就是转换之后的值了
         * yyyy-MM-dd HH-mm-ss
         * 年-月-日 时-分-秒
         */
        val currentTime: String = SimpleDateFormat("HHmmss").format(currentTimeMillis)
        return currentTime
    }

    private fun saveToCache(blobString:String) : String{
        val fixStr = blobString.split(',')[1]
        // 解码Base64字符串为字节数组
        val videoBytes: ByteArray = Base64.decode(fixStr, Base64.DEFAULT)

        // 写入MP4文件到设备存储
        val videoFile = File(cacheDir, "video_${millisToTime()}.mp4")
        try {
            FileOutputStream(videoFile).use { fos ->
                fos.write(videoBytes)
                fos.flush()
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return videoFile.name
    }

    private fun unzip(cb:()->Unit) {
        if (unziped) {
            cb()
            return
        }

        lifecycleScope.launch(Dispatchers.IO) {
            delay(50)
            unzipHelper.copyFromAssets(assets,
                "echarts_record",
                arrayOf(
                "echarts.js",
                "index.html",
                "recordRTC.min.js",
                "html2canvas.min.js"), echartsCacheDir())
            delay(10)
            unziped = true
            cb()
        }
    }
}