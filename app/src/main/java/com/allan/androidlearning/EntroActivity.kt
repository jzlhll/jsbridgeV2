package com.allan.androidlearning

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.WindowManager
import android.widget.LinearLayout
import android.widget.Space
import com.allan.androidlearning.activities.WebVideoPlayActivity
import com.allan.androidlearning.binding.BindingActivity
import com.allan.androidlearning.databinding.ActivityEntroBinding
import com.allan.androidlearning.utils.onClick
import com.google.android.material.button.MaterialButton

class EntroActivity : BindingActivity<ActivityEntroBinding>() {
    private val allClasses: List<Class<out Activity>> by lazy(LazyThreadSafetyMode.NONE) {
        listOf(
            WebBridgeActivity::class.java,
            WebVideoPlayActivity::class.java,
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)

        allClasses.forEach { cls ->
            val btn = MaterialButton(this)
            btn.text = cls.simpleName.replace("Fragment", "")
            btn.onClick {
                startActivity(Intent(this, cls))
            }
            binding.buttonsHost.addView(btn, LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT))
        }

        binding.buttonsHost.addView(Space(this), LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 200))
    }
}