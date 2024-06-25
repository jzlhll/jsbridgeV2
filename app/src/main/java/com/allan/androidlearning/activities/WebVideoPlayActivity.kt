package com.allan.androidlearning.activities

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.FragmentContainerView
import com.allan.androidlearning.R

class WebVideoPlayActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.fragment_container_view)
        val root = findViewById<FragmentContainerView>(R.id.fragmentRoot)
        val h5 = WebVideoPlayFragment()
        supportFragmentManager.beginTransaction()
            .replace(root.id, h5)
            .commitAllowingStateLoss()
    }
}