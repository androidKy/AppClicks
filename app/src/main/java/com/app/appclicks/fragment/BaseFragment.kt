package com.app.appclicks.fragment

import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

/**
 * description:
 * author: kyXiao
 * date: 2019/4/23
 */
abstract class BaseFragment : Fragment() {
    abstract fun getLayoutId(): Int
    abstract fun initView(view: View)
    abstract fun initListener()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(getLayoutId(), container, false)
        initView(view)
        initListener()
        return view
    }

    override fun onStart() {
        super.onStart()
    }

    override fun onResume() {
        super.onResume()
    }

    override fun onPause() {
        super.onPause()
    }

    override fun onDestroy() {
        super.onDestroy()
    }
}