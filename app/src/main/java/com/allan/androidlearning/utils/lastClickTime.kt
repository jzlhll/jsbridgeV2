package com.allan.androidlearning.utils

import android.util.Log
import android.view.View
import com.allan.androidlearning.BuildConfig

/**
 * 上一次按钮点击时间
 */
private var lastClickTime = 0L
private val globalPaddingClickTime = 120L

fun acceptClick(spaceTime:Long? = null) : Boolean {
    val space = spaceTime ?: globalPaddingClickTime
    val cur = System.currentTimeMillis()
    if (cur - lastClickTime < space) {
        return false
    }
    lastClickTime = cur
    return true
}

/**
 * @author au
 * Date: 2023/8/24
 * Description 有间隔的点击事件
 */
class PaddingClickListener(private val paddingTime:Long?, private val wrapClick:(view: View) ->Unit) : View.OnClickListener {
    override fun onClick(v: View?) {
        if (!acceptClick(paddingTime)) {
            return
        }
        v?.let {
            wrapClick(it)
        }
    }
}

/**
 * 默认的全局设置
 */
fun View.onClick(c:(view: View)->Unit) = setOnClickListener(PaddingClickListener(globalPaddingClickTime, c))
/**
 * 默认的全局设置
 */
fun View.onClick(paddingTime:Long, c:(view: View)->Unit) = setOnClickListener(PaddingClickListener(paddingTime, c))

inline fun logd(block:()->String) {
    if (BuildConfig.DEBUG) {
        val str = block()
        Log.d("au_log", str)
    }
}

/**
 * 类型转换
 */
inline fun <reified T> Any?.asOrNull(): T? {
    return if (this is T) {
        this
    } else {
        null
    }
}