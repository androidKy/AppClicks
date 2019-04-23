package com.app.appclicks.service;

import android.accessibilityservice.AccessibilityServiceInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;

import com.app.appclicks.base.BaseAccessibilityService;
import com.app.appclicks.enum_type.PageTypeEnum;
import com.app.appclicks.service.type.AttentionService;
import com.app.appclicks.service.type.CommentService;
import com.app.appclicks.util.Constants;
import com.safframework.log.L;

import java.util.ArrayList;
import java.util.List;

/**
 * description:今日头条评论列表自动点击服务
 * author:kyXiao
 * date:2019/4/20
 * <p>
 * {
 * 1.先找到评论列表：打开今日头条，找到评论列表，如果没有或者已做完当前的评论，上拉查找。（ps:如果找不到评论，自动切换到下一个视频）
 * 2.记录当前界面能看到的评论个数，记录当前做到第几个评论
 * 3.自动点击一个评论，开始一轮任务
 * <p>
 * }
 */
public class CommonService extends BaseAccessibilityService implements PerformBackListener {

    private int mCurCommentCount;   //当前可看见的评论个数
    private int mCommentClickedCount;   //已点击的评论个数
    private AccessibilityNodeInfo mLastNodeInfo;    //当前可看见的评论个数的最后一个

    private PageTypeEnum mCurPageType;   //当前处于哪一界面

    private AccessibilityNodeInfo mHistoryNode; //历史消息

    private int mPageSwitchTime = 1;  //单位秒

    @Override
    public void onCreate() {
        super.onCreate();
        L.init(CommonService.class);
    }

    @Override
    protected void onServiceConnected() {
        AccessibilityServiceInfo info = getServiceInfo();
        info.packageNames = new String[]{
                //"android",
                //"com.google.android.packageinstaller",
                Constants.Package.TODAY_NEWS};
        setServiceInfo(info);

        super.onServiceConnected();
    }

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {

        // findEditTextInTalkPage(getRootInActiveWindow());
        int eventType = event.getEventType();
        if (eventType == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED
                || eventType == AccessibilityEvent.TYPE_VIEW_SCROLLED) {
            // sendMsg2Comment();
            CommentService.Companion.getInstance().onAccessibilityEvent(event, getRootInActiveWindow(), this);
            AttentionService.getInstance().onAccessibilityEvent(event, getRootInActiveWindow(), this);
        }
    }


    private void sendMsg2Comment() {
        findListViewInVideoPage(getRootInActiveWindow());
        if (mCurPageType == PageTypeEnum.COMMENT_PAGE) {
            doOnUserInfoPage();
        }
        if (mCurPageType == PageTypeEnum.USER_INFO_PAGE)
            findEditTextInTalkPage(getRootInActiveWindow());
    }

    /**
     * 从当前界面返回
     */
    private void performBack() {
        mHistoryNode = null;
        mCurPageType = null;
        performBackClick(mHandler, mPageSwitchTime);
    }

    private void findListViewInVideoPage(AccessibilityNodeInfo nodeInfo) {
        //if (mListViewFound) return;
        if (nodeInfo == null) return;
        //L.i("node className: " + nodeInfo.getClassName());
        for (int i = 0; i < nodeInfo.getChildCount(); i++) {
            findListViewInVideoPage(nodeInfo.getChild(i));
        }
        if (nodeInfo.getClassName() != null && nodeInfo.getClassName().equals(Constants.Widget.ListView)) {
            L.i("listView ClassName: " + nodeInfo.getClassName());
            //mListViewFound = true;
            mCurPageType = PageTypeEnum.COMMENT_PAGE;
            doOnCommentPage(nodeInfo);
        }
    }

    /**
     * 处理评论的界面
     */
    private void doOnCommentPage(AccessibilityNodeInfo listViewNode) {
        // AccessibilityNodeInfo listViewNode = findViewByRootID(Constants.ResourceId.Video_ListView);
        if (listViewNode != null) {

            L.i("className: " + listViewNode.getClassName());   //ListView
            List<AccessibilityNodeInfo> clickNodeList = new ArrayList<>();  //头像的node列表
            List<AccessibilityNodeInfo> titleNodeList = new ArrayList<>();  //用户名的node列表
            for (int i = 0; i < listViewNode.getChildCount(); i++) {
                AccessibilityNodeInfo childInfo = listViewNode.getChild(i); //LinearLayout
                if (childInfo.getClassName().equals(Constants.Widget.LinearLayout)) {
                    if (childInfo.getChildCount() > 0) {
                        AccessibilityNodeInfo frameLayoutNode = childInfo.getChild(0);  //FrameLayout
                        if (frameLayoutNode.getClassName().equals(Constants.Widget.FrameLayout)) {
                            clickNodeList.add(frameLayoutNode);
                        }
                    }
                    if (childInfo.getChildCount() > 1) {
                        AccessibilityNodeInfo textViewNode = childInfo.getChild(1).getChild(0).getChild(0); //LinearLayout
                        titleNodeList.add(textViewNode);
                    }
                }
            }
            //已开始任务
            if (mCurCommentCount > 0) {
                //任务已做完一轮
                if (mCurCommentCount - 1 == mCommentClickedCount) {
                    //把所有状态重置
                    mLastNodeInfo = titleNodeList.get(mCommentClickedCount);
                    mCurCommentCount = 0;
                    mCommentClickedCount = 0;
                    //mListViewFound = false;
                    performScrollForward(listViewNode);
                    return;
                }
                mCommentClickedCount++;
            } else mCommentClickedCount = 0;

            mCurCommentCount = clickNodeList.size();
            L.i("clickNode size: " + mCurCommentCount);
            // L.i("titleNode size: " + titleNodeList.size());
            if (mCurCommentCount > 0 && mCommentClickedCount < mCurCommentCount) {
                AccessibilityNodeInfo clickNodeInfo = clickNodeList.get(mCommentClickedCount);
                AccessibilityNodeInfo titleNodeInfo = titleNodeList.get(mCommentClickedCount);
                if (mLastNodeInfo != null) {    //最后一个,防止重复发最后一个
                    if (mLastNodeInfo.getText().toString().equals(titleNodeInfo.getText().toString())) {
                        L.i("the last comment already sent");
                        mCommentClickedCount++;
                        if (mCommentClickedCount < mCurCommentCount)
                            clickNodeInfo = clickNodeList.get(mCommentClickedCount);
                    }
                }

                performViewClick(clickNodeInfo);
            } else {
                mCurCommentCount = 0;
                mCommentClickedCount = 0;

                L.i("当前界面无评论");
            }
        }
    }

    /**
     * 在用户信息界面点击私信
     */
    private void doOnUserInfoPage() {
        final AccessibilityNodeInfo talkInfo = findViewByText("私信", true);
        if (talkInfo != null && talkInfo.getClassName().equals(Constants.Widget.TextView)) {
            L.i("talk info: " + talkInfo.getText().toString());
            mCurPageType = PageTypeEnum.USER_INFO_PAGE;

            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    performViewClick(talkInfo);
                }
            }, mPageSwitchTime * 1000);
        }
    }


    private void findEditTextInTalkPage(AccessibilityNodeInfo nodeInfo) {
        if (nodeInfo == null) return;
        L.i("node className: " + nodeInfo.getClassName());
        for (int i = 0; i < nodeInfo.getChildCount(); i++) {
            findEditTextInTalkPage(nodeInfo.getChild(i));
        }

        if (nodeInfo.getClassName() != null && nodeInfo.getClassName().equals(Constants.Widget.EditText)) {
            L.i("editText ClassName: " + nodeInfo.getClassName());
            mCurPageType = PageTypeEnum.SEND_MSG_PAGE;
            findHistoryMsg(getRootInActiveWindow());
            L.i("ediText parent: " + nodeInfo.getParent().getClassName());
            L.i("send node : " + nodeInfo.getParent().getChild(1).getClassName());
            //private boolean mListViewFound;
            AccessibilityNodeInfo sendNode = nodeInfo.getParent().getChild(1).getChild(0);
            if (mHistoryNode == null) {
                doTalkOnPage(nodeInfo, sendNode);
            } else {
                performBack();
            }
            L.i("send node className: " + sendNode.getText());
        }
    }


    /**
     * 在用户聊天界面发送消息
     */
    private void doTalkOnPage(AccessibilityNodeInfo editNode, AccessibilityNodeInfo sendNode) {
        Bundle arguments = new Bundle();
        arguments.putCharSequence(AccessibilityNodeInfo.ACTION_ARGUMENT_SET_TEXT_CHARSEQUENCE, mCommentMsg);
        editNode.performAction(AccessibilityNodeInfo.ACTION_SET_TEXT, arguments);
        performViewClick(sendNode);
        performBack();  //todo 网络延时
    }

    /**
     * 查找是否有历史消息
     *
     * @param nodeInfo
     */
    private void findHistoryMsg(AccessibilityNodeInfo nodeInfo) {
        if (nodeInfo == null) return;
        for (int i = 0; i < nodeInfo.getChildCount(); i++) {
            findHistoryMsg(nodeInfo.getChild(i));
        }

        if (nodeInfo.getClassName() != null && nodeInfo.getClassName().equals(Constants.Widget.ImageView)) {
            L.i("image className: " + nodeInfo.getClassName());
            // if (imageNode == null)
            mHistoryNode = nodeInfo;
        }
    }


    private Handler mHandler = new Handler(Looper.getMainLooper());

    @Override
    public void onBackPressed(PageTypeEnum lastPageType) {
        performBack();
    }
}
