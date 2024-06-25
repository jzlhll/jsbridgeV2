package com.allan.androidlearning.binding

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.CallSuper
import androidx.annotation.EmptySuper
import androidx.appcompat.app.AppCompatActivity
import androidx.viewbinding.ViewBinding
import java.lang.reflect.ParameterizedType

/**
 * @author au
 * Date: 2023/7/4
 * Description 指导基础类模板
 */
abstract class BindingActivity<VB: ViewBinding> : AppCompatActivity() {
    lateinit var binding:VB private set

    @CallSuper
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val v = onUiCreateView(layoutInflater, null, savedInstanceState)
        setContentView(v)
        onBindingCreated(savedInstanceState)
    }

    @EmptySuper
    open fun onBindingCreated(savedInstanceState: Bundle?) {}

    override fun setRequestedOrientation(requestedOrientation: Int) {
        //处理安卓8.0报错
        //Only fullscreen activities can request orientation
        try {
            super.setRequestedOrientation(requestedOrientation)
        } catch (e:Exception) {

        }
    }

    fun onUiCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val vb = createViewBinding(javaClass, inflater, container, false) as VB
        binding = vb
        return vb.root
    }

    fun <T : ViewBinding> createViewBinding(self: Class<*>, inflater: LayoutInflater, container: ViewGroup?, attach: Boolean): T {
        var clz: Class<T>? = findViewBinding(self)
        //修正框架，允许往上寻找3层superClass的第一个泛型做为ViewBinding
        if (clz == null) {
            val superClass = self.javaClass.superclass
            if (superClass != null) {
                clz = findViewBinding(superClass) ?: superClass.superclass?.let { findViewBinding(it) }
            }
        }
        if (clz == null) throw IllegalArgumentException("需要一个ViewBinding类型的泛型")
        return clz.getMethod(
            "inflate",
            LayoutInflater::class.java,
            ViewGroup::class.java,
            Boolean::class.java
        ).invoke(null, inflater, container, attach) as T
    }

    /**
     * 将class转为ParameterizedType，方便获取此类的类泛型
     */
    private fun Class<*>?.getParameterizedType(): ParameterizedType? {
        if (this == null) {
            return null
        }
        val type = this.genericSuperclass
        return if (type == null || type !is ParameterizedType) {
            this.superclass.getParameterizedType()
        } else {
            type
        }
    }

    private fun <T> findViewBinding(javaClass:Class<*>) : Class<T>? {
        val parameterizedType = javaClass.getParameterizedType() ?: return null
        val actualTypeArguments = parameterizedType.actualTypeArguments
        val type = actualTypeArguments[0]
        if ((ViewBinding::class.java).isAssignableFrom(type as Class<*>)) {
            return type as Class<T>
        }
        return null
    }
}