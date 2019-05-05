package com.app.appclicks.wxservice

import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import com.app.appclicks.service.AbstractFactoryService
import com.app.appclicks.service.PerformBackListener
import com.app.appclicks.util.Constants
import com.safframework.log.L

/**
 * description:
 * author: kyXiao
 * date: 2019/5/5
 */
class TencentNews : AbstractFactoryService() {

    private var mCurPage: WxPageEnum? = WxPageEnum.TALK_MAIN_PAGE

    companion object {
        val instance: TencentNews by lazy(mode = LazyThreadSafetyMode.SYNCHRONIZED) {
            TencentNews()
        }
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent, nodeInfo: AccessibilityNodeInfo,
                                      performBackListener: PerformBackListener) {
        when (mCurPage) {
            WxPageEnum.TALK_MAIN_PAGE -> findNewsItemInMainPage(nodeInfo)
            WxPageEnum.TENCENT_NEWS_PAGE -> doOnMainNewsPage(nodeInfo)
            WxPageEnum.NEWS_PAGE -> doOnNewsPage(nodeInfo)
        }

    }

    /**
     * 查找腾讯新闻
     */
    private fun findNewsItemInMainPage(nodeInfo: AccessibilityNodeInfo?) {
        if (nodeInfo == null)
            return

        val newsItem = findViewByNodeId(nodeInfo, "com.tencent.mm:id/b6c")
        L.i("newsItem: " + newsItem?.className + " text = " + newsItem?.text)


        if (newsItem != null && newsItem.className == Constants.Widget.LinearLayout) {
            mCurPage = WxPageEnum.TENCENT_NEWS_PAGE
            performViewClick(newsItem, 1L)
        }
        /* for (childIndex in 0 until nodeInfo.childCount) {
             findNewsItemInMainPage(nodeInfo.getChild(childIndex))
         }

         if (nodeInfo.className != null && nodeInfo.className == Constants.Widget.ListView) {
             L.i("listView ClassName: " + nodeInfo.className)
             mCurPage = WxPageEnum.TALK_MAIN_PAGE

             return
         }*/
    }

    private fun doOnMainNewsPage(nodeInfo: AccessibilityNodeInfo?) {
        if (nodeInfo == null) return

        for (childIndex in 0 until nodeInfo?.childCount!!) {
            doOnMainNewsPage(nodeInfo.getChild(childIndex))
        }

        if (nodeInfo.className != null && nodeInfo.className == Constants.Widget.ListView) {
            L.i("listView ClassName: " + nodeInfo.className)
            //todo 浏览新闻次数
            val child = nodeInfo.getChild(0).getChild(1).getChild(0)
            mCurPage = WxPageEnum.NEWS_PAGE
            performViewClick(child, 1L)

            return
        }
    }

    private fun doOnNewsPage(nodeInfo: AccessibilityNodeInfo?) {
          if (nodeInfo == null) return

          for (childIndex in 0 until nodeInfo.childCount) {
              doOnNewsPage(nodeInfo.getChild(childIndex))
          }

          if (nodeInfo.className != null && nodeInfo.className == Constants.Widget.WebView) {

              performScrollForward(nodeInfo,1)
          }
    }


}