package com.app.appclicks.service;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.AccessibilityServiceInfo;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;

import com.app.appclicks.util.Constants;
import com.safframework.log.L;

import java.util.ArrayList;
import java.util.List;

import static android.view.accessibility.AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED;

/**
 * description:
 * author:kyXiao
 * date:2019/4/15
 */
public class VideoCommentService extends AccessibilityService {
    public static final int MSG_LISG_VIEW = 1000;
    public static final int MSG_CHECK_PERSON_INFO = 2000;
    private int mCommentClickedCount; //评论已点击次数默认+1
    private boolean mAlreadyStartClickComment;    //是否已开始点击评论
    private int mCurCommentCount = 0;   //当前页面的ListView的个数

    @Override
    public void onCreate() {
        super.onCreate();
        L.init(this.getClass());
    }

    Handler mHandler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_CHECK_PERSON_INFO:

                    break;
            }
        }
    };


    @Override
    protected void onServiceConnected() {
        AccessibilityServiceInfo info = getServiceInfo();
        info.packageNames = new String[]{
               // "android",
              //  "com.google.android.packageinstaller",
                "com.ss.android.article.news"};
        setServiceInfo(info);

        super.onServiceConnected();
    }

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        L.i("event: " + event.getEventType());
        switch (event.getEventType()) {
            case TYPE_WINDOW_STATE_CHANGED:

                break;
        }
        try {
            final AccessibilityNodeInfo rootInfo = getRootInActiveWindow();
            if (rootInfo != null) {
                dfsComment(rootInfo);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private synchronized void dfsComment(AccessibilityNodeInfo rootInfo)  {
        // L.i("rootInfo className: " + rootInfo.getClassName());

        for (int i = 0; i < rootInfo.getChildCount(); i++) {
            dfsComment(rootInfo.getChild(i));
        }

        if (findListViewInVideoPage(rootInfo, Constants.Widget.ListView)) {
            L.i("找到视频页面的ListView");
            String resourceId = rootInfo.getClassName().toString();
            L.i("className : " + resourceId);
            //找到当前的评论item
            List<AccessibilityNodeInfo> commentNodeList = new ArrayList<>();
            for (int i = 0; i < rootInfo.getChildCount(); i++) {
                AccessibilityNodeInfo childInfo = rootInfo.getChild(i);
                if (childInfo.getClassName().equals(Constants.Widget.LinearLayout)) {
                    if (childInfo.getChildCount() > 0 && childInfo.getChild(0).getClassName().equals(Constants.Widget.FrameLayout)) {
                        commentNodeList.add(childInfo.getChild(0));
                    }
                }
            }
            if (mCurCommentCount - 1 == mCommentClickedCount && mAlreadyStartClickComment) {  //当前可看见的评论已发完信息
                mCommentClickedCount = 0; //当前可看见的评论已发完信息时才置为0
                mCurCommentCount = 0;
                //上拉加载评论
                L.i("上拉加载评论，重新开始下一轮任务");
                mAlreadyStartClickComment = true;   //todo 应该为false
                return;
            }

            if (mCurCommentCount != 0) {
                mCommentClickedCount++;
            }

            mCurCommentCount = commentNodeList.size();
            L.i("当前页面看得见的评论个数：" + mCurCommentCount);
            if (mCurCommentCount > 0 && mCommentClickedCount < mCurCommentCount) {
                mAlreadyStartClickComment = true;
                performClick(commentNodeList.get(mCommentClickedCount));
            } else {
                mCommentClickedCount = 0; //当前可看见的评论已发完信息时才置为0
                mCurCommentCount = 0;
                mAlreadyStartClickComment = false;
                L.i("找不到评论，上拉加载评论");
            }
            //performClick(rootInfo.getParent());
            //isMyTextClick = true;
        }
    }


    private void performClick(AccessibilityNodeInfo targetInfo) {
        targetInfo.performAction(AccessibilityNodeInfo.ACTION_CLICK);
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    /**
     * 通过控件全称和控件id找到相应的控件
     *
     * @param nodeInfo
     * @param widgetFullName
     * @return
     */
    private boolean findListViewInVideoPage(AccessibilityNodeInfo nodeInfo, String widgetFullName) {
        return nodeInfo.getViewIdResourceName() != null &&
                nodeInfo.getClassName() != null &&
                nodeInfo.getClassName().equals(widgetFullName);
    }

    private boolean findTextView(AccessibilityNodeInfo nodeInfo, String widgetFullName, String text) {
        return nodeInfo.getViewIdResourceName() != null &&
                nodeInfo.getClassName() != null &&
                nodeInfo.getClassName().equals(widgetFullName) &&
                nodeInfo.getText().equals("私信");
    }


    private boolean isWidgetExist(AccessibilityNodeInfo nodeInfo, String widgetFullName, String text) {
        List<AccessibilityNodeInfo> accessibilityNodeInfosByViewId = nodeInfo.findAccessibilityNodeInfosByViewId("com.ss.android.article.news:id/a5i");
        for (int i = 0; i < accessibilityNodeInfosByViewId.size(); i++) {
            AccessibilityNodeInfo accessibilityNodeInfo = accessibilityNodeInfosByViewId.get(i);
            if (accessibilityNodeInfo.getViewIdResourceName() != null &&
                    accessibilityNodeInfo.getClassName() != null &&
                    accessibilityNodeInfo.getClassName().equals(widgetFullName)) {
                return true;
            }
        }
        return false;
    }


    @Override
    public void onInterrupt() {

    }
}
