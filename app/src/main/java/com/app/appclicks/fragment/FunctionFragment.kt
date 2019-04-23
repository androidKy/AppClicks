package com.app.appclicks.fragment

import android.content.Intent
import android.provider.Settings
import android.text.TextUtils
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import com.app.appclicks.R
import com.app.appclicks.base.BaseAccessibilityService
import com.app.appclicks.service.CommonService
import com.app.appclicks.util.Constants

/**
 * description:
 * author: kyXiao
 * date: 2019/4/23
 */
class FunctionFragment : BaseFragment(), View.OnClickListener {


    private var etComment: EditText? = null
    private var etAttention: EditText? = null
    private var btComment: Button? = null
    private var btAttention: Button? = null

    override fun getLayoutId(): Int {
        return R.layout.fragment_function
    }

    override fun initView(view: View) {
        etAttention = view.findViewById(R.id.et_attention)
        etComment = view.findViewById(R.id.et_comment)
        btAttention = view.findViewById(R.id.bt_attention)
        btComment = view.findViewById(R.id.bt_comment)
    }

    override fun initListener() {
        btAttention!!.setOnClickListener(this)
        btComment!!.setOnClickListener(this)
    }

    override fun onClick(v: View?) {
        if (!BaseAccessibilityService.isAccessibilitySettingsOn(context!!, CommonService::class.java.canonicalName)) {
            val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
            startActivity(intent)
            return
        }
        when (v!!.id) {
            R.id.bt_attention -> sendMsg(etAttention!!.text.toString(), 1)
            R.id.bt_comment -> sendMsg(etComment!!.text.toString(), 2)
        }
    }

    private fun sendMsg(msg: String, function: Int) {
        // val msg = etAttention!!.text.toString()
        if (TextUtils.isEmpty(msg)) {
            Toast.makeText(context!!, "请输入信息", Toast.LENGTH_SHORT).show()
            return
        }
        when (function) {
            1 -> BaseAccessibilityService.setAttentionMsg(msg)
            2 -> BaseAccessibilityService.setCommentMsg(msg)
        }

        val launchIntentForPackage = context!!.packageManager.getLaunchIntentForPackage(Constants.Package.TODAY_NEWS)
        if (launchIntentForPackage != null) {
            startActivity(launchIntentForPackage)
        }
    }

}