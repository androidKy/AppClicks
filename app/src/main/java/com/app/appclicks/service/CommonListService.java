package com.app.appclicks.service;

import android.accessibilityservice.AccessibilityServiceInfo;
import android.graphics.Rect;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;

import com.app.appclicks.base.BaseAccessibilityService;
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
public class CommonListService extends BaseAccessibilityService {

    private int mCurCommentCount;
    private int mCommentClickedCount;
    private AccessibilityNodeInfo mLastNodeInfo;

    private int mRepeatCountForFindSendWidget = 0;


    private boolean mStarted = false; //评论是否已被点击
    private boolean mTalked = false;    //私信是否已被点击
    private boolean mSent = false;  //信息是否已发送
    //private boolean mListViewFound;
    private AccessibilityNodeInfo sendNode;
    private AccessibilityNodeInfo editNode;
    private AccessibilityNodeInfo imageNode;
    private boolean mIsBacked;

    @Override
    public void onCreate() {
        super.onCreate();
        L.init(CommonListService.class);
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
            findListViewInVideoPage(getRootInActiveWindow());
        }

        if (mStarted
                || eventType == AccessibilityEvent.TYPE_VIEW_CONTEXT_CLICKED
                || eventType == AccessibilityEvent.TYPE_VIEW_CLICKED
                || eventType == AccessibilityEvent.TYPE_VIEW_LONG_CLICKED) {
            doOnUserInfoPage();
        }
        if (mTalked
                || eventType == AccessibilityEvent.TYPE_VIEW_CONTEXT_CLICKED
                || eventType == AccessibilityEvent.TYPE_VIEW_CLICKED
                || eventType == AccessibilityEvent.TYPE_VIEW_LONG_CLICKED) {
            sleep();
            findEditTextInTalkPage(getRootInActiveWindow());
        }

    }

    private void sleep() {
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void performBack() {

        performBackClick(mHandler);
       /* AccessibilityNodeInfo backNode = findViewByRootID(Constants.ResourceId.Back_TextView);
        L.i("返回: backNode = " + backNode);
        if (backNode != null) {
            L.i("返回: backNode = " + backNode.getClassName());
            performViewClick(backNode);
        }*/
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
            sleep();
            doOnCommentPage(nodeInfo);
        }
    }

    /**
     * 处理评论的界面
     */
    private void doOnCommentPage(AccessibilityNodeInfo listViewNode) {
        // AccessibilityNodeInfo listViewNode = findViewByRootID(Constants.ResourceId.Video_ListView);
        if (listViewNode != null) {
            resetStatus();
            L.i("className: " + listViewNode.getClassName());   //ListView
            List<AccessibilityNodeInfo> clickNodeList = new ArrayList<>();  //头像的node列表
            // List<AccessibilityNodeInfo> titleNodeList = new ArrayList<>();  //用户名的node列表
            for (int i = 0; i < listViewNode.getChildCount(); i++) {
                AccessibilityNodeInfo childInfo = listViewNode.getChild(i); //LinearLayout
                if (childInfo.getClassName().equals(Constants.Widget.LinearLayout)) {
                    if (childInfo.getChildCount() > 0) {
                        AccessibilityNodeInfo frameLayoutNode = childInfo.getChild(0);  //FrameLayout
                        if (frameLayoutNode.getClassName().equals(Constants.Widget.FrameLayout)) {
                            clickNodeList.add(frameLayoutNode);
                        }
                    }
                   /* if (childInfo.getChildCount() > 1) {
                        AccessibilityNodeInfo linearLayoutNode = childInfo.getChild(1); //LinearLayout
                        AccessibilityNodeInfo textViewNode = findViewByNodeId(linearLayoutNode, Constants.ResourceId.Comment_TextView);
                        if (textViewNode != null) {
                            L.i("textView : " + textViewNode.getClassName() + " text: " + textViewNode.getText().toString());
                            titleNodeList.add(textViewNode);
                        }
                    }*/
                }
            }
            //已开始任务
            if (mCurCommentCount > 0) {
                //任务已做完一轮
                if (mCurCommentCount - 1 == mCommentClickedCount) {
                    //把所有状态重置
                    //  mLastNodeInfo = titleNodeList.get(mCommentClickedCount);
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
                final AccessibilityNodeInfo clickNodeInfo = clickNodeList.get(mCommentClickedCount);
               /* AccessibilityNodeInfo titleNodeInfo = titleNodeList.get(mCommentClickedCount);
                if (mLastNodeInfo != null) {    //最后一个,防止重复发最后一个
                    if (mLastNodeInfo.getText().toString().equals(titleNodeInfo.getText().toString())) {
                        L.i("the last comment already sent");
                        mCommentClickedCount++;
                        if (mCommentClickedCount < mCurCommentCount)
                            clickNodeInfo = clickNodeList.get(mCommentClickedCount);
                    }
                }*/

                mStarted = true;
                performViewClick(clickNodeInfo);
            } else {
                mCurCommentCount = 0;
                mCommentClickedCount = 0;
                resetStatus();
                L.i("当前界面无评论");
            }
        }
    }

    /**
     * 在用户信息界面点击私信
     */
    private void doOnUserInfoPage() {
        sleep();
        final AccessibilityNodeInfo talkInfo = findViewByText("私信", true);
        if (talkInfo != null && talkInfo.getClassName().equals(Constants.Widget.TextView)) {
            L.i("talk info: " + talkInfo.getText().toString());
            if (mSent) {  //返回视频播放评论界面
                //mListViewFound = false;
                resetStatus();
                imageNode = null;
                mIsBacked = false;
                performBack();
                return;
            }
            if (mTalked)
                return;
            mTalked = true;
            performViewClick(talkInfo);
        }
    }


    private void findEditTextInTalkPage(AccessibilityNodeInfo nodeInfo) {
        if (mSent) {
            return;
        }
        if (nodeInfo == null) return;
        L.i("node className: " + nodeInfo.getClassName());
        for (int i = 0; i < nodeInfo.getChildCount(); i++) {
            findEditTextInTalkPage(nodeInfo.getChild(i));
        }
        if (nodeInfo.getClassName() != null && nodeInfo.getClassName().equals(Constants.Widget.ImageView)) {
            L.i("image className: " + nodeInfo.getClassName());
            // if (imageNode == null)
            imageNode = nodeInfo;
        }
        if (nodeInfo.getClassName() != null && nodeInfo.getClassName().equals(Constants.Widget.EditText)) {

            L.i("editText ClassName: " + nodeInfo.getClassName());
            editNode = nodeInfo;
            L.i("ediText parent: " + editNode.getParent().getClassName());
            L.i("send node : " + editNode.getParent().getChild(1).getClassName());
            sendNode = editNode.getParent().getChild(1).getChild(0);
            mSent = true;
            if (imageNode == null) {
                doTalkOnPage(editNode, sendNode);
            } else {
                performBack();
            }
            L.i("send node className: " + sendNode.getText());
        }

    }

    /**
     * 在用户聊天界面发送消息
     */
    private void doTalkOnPage(AccessibilityNodeInfo editNode1, AccessibilityNodeInfo sendNode1) {
        Bundle arguments = new Bundle();
        arguments.putCharSequence(AccessibilityNodeInfo.ACTION_ARGUMENT_SET_TEXT_CHARSEQUENCE, mMsg);
        editNode.performAction(AccessibilityNodeInfo.ACTION_SET_TEXT, arguments);
        performViewClick(sendNode);
        performBack();  //todo 网络延时
    }


    private void resetStatus() {
        mStarted = false;
        mTalked = false;
        mSent = false;
    }

    private Handler mHandler = new Handler(Looper.getMainLooper());
}
