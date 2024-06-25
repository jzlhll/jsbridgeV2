package com.allan.androidlearning.activities

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.util.Base64
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.lifecycle.lifecycleScope
import com.allan.androidlearning.binding.BindingActivity
import com.allan.androidlearning.databinding.ActivityEchartsBinding
import com.allan.androidlearning.recordview.ContinuousBitmapRecorderManager
import com.allan.androidlearning.utils.UnzipHelper
import com.allan.androidlearning.utils.asOrNull
import com.allan.androidlearning.utils.logd
import com.github.lzyzsd.jsbridge.BridgeWebViewExFragment
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat

class WebEChartsGetDataUrlBitmapActivity : BindingActivity<ActivityEchartsBinding>() {
    companion object {
        var unziped = false
    }

    private val unzipHelper = UnzipHelper()
    private fun echartsCacheDir() = cacheDir.path + "/echarts_v2"

    private fun findIndexHtml() : String {
        val cd = echartsCacheDir()
        val path = "$cd/showEcharts.html"
        return path
    }

    private val viewRecorder by lazy(LazyThreadSafetyMode.NONE) { ContinuousBitmapRecorderManager() }

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

    private lateinit var h5Fragment:RecordWebV2Fragment
    private var lastBase64Image:String? = null

    private val bitmapOffer:()->Bitmap? = {
        val base = lastBase64Image
        if (base == null) {
            null
        } else base64ToBitmap(base)
    }

    private val customBase64PrefixLength = "base64Image:".length + "data:image/png;base64,".length
    private var imagePrintCount = 0

    override fun onBindingCreated(savedInstanceState: Bundle?) {
        try {
            cacheDir.listFiles()?.forEach {
                if (it.isFile && it.name.startsWith("video_")) {
                    it.delete()
                }
            } }catch (e:Exception) {}

        binding.toolBar.fakeToolbarTitle.text = "Demo View Record by Echarts Images"

        val h5 = RecordWebV2Fragment()
        supportFragmentManager.beginTransaction()
            .replace(binding.webViewHost.id, h5)
            .commitAllowingStateLoss()
        h5Fragment = h5

        h5.onCreatedCallback = {
            unzip {
                lifecycleScope.launch {
                    h5Fragment.loadUrl("file://" + findIndexHtml())
                }
            }

            h5.getWebView().registerHandler("webCallNative") { data, func->
                when (data) {
                    "initOver" -> {
                        logd { ">>>webCallNative initOver hasCallback:" }
                        val name = File(cacheDir, "video_${millisToTime()}.mp4").absolutePath
                        val suc = viewRecorder.setup(name, bitmapOffer)
                        binding.desc2Text.text = "generating $name ..."
                        logd { "setup $name $suc" }
                        if (suc) {
                            viewRecorder.startRecord()
                            h5Fragment.webView.sendEventToH5("nativeCallWeb", "startEcharts")
                        }
                    }
                    "runOver" -> {
                        logd { ">>>webCallNative runOver hasCallback:" }
                        if(viewRecorder.stopRecord()) { //xxx
                            val p = viewRecorder.curRecordFile?.absolutePath
                            logd { "toast suc! ${viewRecorder.curRecordFile}" }
                            binding.desc2Text.text = "$p suc!"
                            binding.desc2Text.setTextColor(Color.parseColor("#0c5199"))

                            val lp = binding.videoView.layoutParams.asOrNull<ConstraintLayout.LayoutParams>()
                            lp?.matchConstraintPercentWidth = 0.78f
                            binding.videoView.layoutParams = lp

                            val videoUri = Uri.parse(p)
                            binding.videoView.setVideoURI(videoUri)
                            binding.videoView.start()
                        }
                    }

                    else -> {
                        //减少打印
                        if (data.startsWith("base64Image:")) {
                            if (imagePrintCount++ % 50 == 0) {
                                logd { ">>>webCallNative $data hasCallback:" }
                                saveBitmapToFile(base64ToBitmap(data.substring(customBase64PrefixLength)), "pic_" + millisToTime() + ".png",
                                    cacheDir.absolutePath)
                            }
                            lastBase64Image = data.substring(customBase64PrefixLength)
                        }
                    }
                }
            }
        }
    }

    fun base64ToBitmap(base64String: String): Bitmap {
        // Decode Base64 string to byte array
        val decodedString: ByteArray = Base64.decode(base64String, Base64.DEFAULT)
        // Convert byte array to Bitmap
        return BitmapFactory.decodeByteArray(decodedString, 0, decodedString.size)
    }

    fun saveBitmapToFile(bitmap: Bitmap, fileName: String, folderPath: String?) {
        val file = File(folderPath, "$fileName.png") // 可以更改文件扩展名为.jpg或.jpeg以保存为JPEG
        try {
            val out = FileOutputStream(file)
            // 对于PNG，直接使用compress方法
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
            // 对于JPEG，你可以设置压缩质量（0-100）
            // bitmap.compress(Bitmap.CompressFormat.JPEG, 90, out);
            out.flush()
            out.close()
            // 成功保存文件后，你可以在这里做一些处理，如通知用户或更新UI
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * @param cb 回调在子线程
     */
    private fun unzip(cb:()->Unit) {
        if (unziped) {
            cb()
            return
        }

        lifecycleScope.launch(Dispatchers.IO) {
            unzipHelper.copyFromAssets(
                assets,
                "echarts_v2",
                arrayOf(
                "echarts.js",
                "showEcharts.html"), echartsCacheDir())
            delay(10)
            unziped = true
            cb()
        }
    }
}

class RecordWebV2Fragment : BridgeWebViewExFragment() {
    var onCreatedCallback:()->Unit = {}

    override fun getRoot(): ViewGroup {
        return super.getRoot()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val v = super.onCreateView(inflater, container, savedInstanceState)
        onCreatedCallback()
        return v
    }
}