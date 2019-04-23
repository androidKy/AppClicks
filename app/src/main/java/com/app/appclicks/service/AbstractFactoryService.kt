package com.app.appclicks.service

import android.os.Handler
import android.os.Looper
import android.view.accessibility.AccessibilityNodeInfo
import com.safframework.log.L

/**
 * description:
 * author: kyXiao
 * date: 2019/4/23
 */
abstract class AbstractFactoryService : FactoryService {
    private var mHandler: Handler? = null
    protected var mMsgSentCount: Int = 0  //消息发送统计

    public fun getHandler(): Handler {
        if (mHandler == null)
            mHandler = Handler(Looper.getMainLooper())

        return mHandler as Handler
    }

    /**
     * 模拟点击事件
     *
     * @param nodeInfo nodeInfo
     */
    fun performViewClick(nodeInfo: AccessibilityNodeInfo?) {
        var nodeInfo: AccessibilityNodeInfo? = nodeInfo ?: return
        while (nodeInfo != null) {
            if (nodeInfo.isClickable) {
                nodeInfo.performAction(AccessibilityNodeInfo.ACTION_CLICK)
                break
            }
            nodeInfo = nodeInfo.parent
        }
    }

    /**
     * 模拟上滑操作
     */
    fun performScrollForward(nodeInfo: AccessibilityNodeInfo?) {
        try {
            if (nodeInfo != null) {
                Thread.sleep(500)
                nodeInfo.performAction(AccessibilityNodeInfo.ACTION_SCROLL_FORWARD)
            }
        } catch (e: InterruptedException) {
            L.e(e.message, e)
        }
    }

    /**
     * 查找对应文本的View
     *
     * @param text text
     * @return View
     */
    fun findViewByText(rootNode: AccessibilityNodeInfo, text: String): AccessibilityNodeInfo? {
        return findViewByText(rootNode, text, false)
    }

    /**
     * 查找对应文本的View
     *
     * @param text      text
     * @param clickable 该View是否可以点击
     * @return View
     */
    fun findViewByText(rootNode: AccessibilityNodeInfo, text: String, clickable: Boolean): AccessibilityNodeInfo? {
        val accessibilityNodeInfo = rootNode ?: return null
        val nodeInfoList = accessibilityNodeInfo!!.findAccessibilityNodeInfosByText(text)
        if (nodeInfoList != null && !nodeInfoList!!.isEmpty()) {
            for (nodeInfo in nodeInfoList!!) {
                if (nodeInfo != null && nodeInfo!!.isClickable() == clickable) {
                    return nodeInfo
                }
            }
        }
        return null
    }

}