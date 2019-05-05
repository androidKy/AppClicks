package layout

import android.view.accessibility.AccessibilityEvent
import com.app.appclicks.base.BaseAccessibilityService
import com.app.appclicks.enum_type.PageTypeEnum
import com.app.appclicks.service.PerformBackListener
import com.app.appclicks.util.Constants
import com.app.appclicks.wxservice.TencentNews
import com.safframework.log.L

/**
 * description:
 * author: kyXiao
 * date: 2019/5/5
 */
class WxService : BaseAccessibilityService(), PerformBackListener {
    override fun onBackPressed(lastPageType: PageTypeEnum?) {
        performBackClick()
    }

    override fun onCreate() {
        super.onCreate()
        L.init(WxService::class.java)
    }

    override fun onServiceConnected() {
        val info = serviceInfo
        info.packageNames = arrayOf(
                //"android",
                //"com.google.android.packageinstaller",
                Constants.Package.WE_CHAT,
                Constants.Package.TODAY_NEWS)
        serviceInfo = info
        super.onServiceConnected()
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        val eventType = event!!.eventType
        if (eventType == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED || eventType == AccessibilityEvent.TYPE_VIEW_SCROLLED) {
            TencentNews.instance.onAccessibilityEvent(event,rootInActiveWindow,this)
        }
    }

}