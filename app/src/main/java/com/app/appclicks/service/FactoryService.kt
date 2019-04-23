package com.app.appclicks.service

import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo

/**
 * description:
 * author: kyXiao
 * date: 2019/4/23
 */
interface FactoryService {
    fun onAccessibilityEvent(event: AccessibilityEvent, nodeInfo: AccessibilityNodeInfo, performBackListener: PerformBackListener)
}
