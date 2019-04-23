package com.app.appclicks.service.type

import android.os.Bundle
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import com.app.appclicks.base.BaseAccessibilityService
import com.app.appclicks.enum_type.PageTypeEnum
import com.app.appclicks.service.AbstractFactoryService
import com.app.appclicks.service.PerformBackListener
import com.app.appclicks.util.Constants
import com.safframework.log.L
import java.util.*

/**
 * description:私发评论用户
 * author: kyXiao
 * date: 2019/4/23
 */
class CommentService private constructor() : AbstractFactoryService() {
    private var mCurPageType: PageTypeEnum? = null   //当前处于哪一界面
    private var mHistoryNode: AccessibilityNodeInfo? = null //历史消息
    private var mCurCommentCount: Int = 0   //当前可看见的评论个数
    private var mCommentClickedCount: Int = 0   //已点击的评论个数
    private var mLastNodeInfo: AccessibilityNodeInfo? = null    //当前可看见的评论个数的最后一个
    private var mRootNodeInfo: AccessibilityNodeInfo? = null
    private var mPerformBackListener: PerformBackListener? = null
    private val mPageSwitchTime = 1  //单位秒


    companion object {
        val instance: CommentService by lazy(mode = LazyThreadSafetyMode.SYNCHRONIZED) {
            CommentService()
        }
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent, nodeInfo: AccessibilityNodeInfo, performBackListener: PerformBackListener) {
        // if (mRootNodeInfo == null)
        mRootNodeInfo = nodeInfo
        if (mPerformBackListener == null)
            mPerformBackListener = performBackListener

        findListViewInVideoPage(nodeInfo)
        if (mCurPageType == PageTypeEnum.COMMENT_PAGE) {
            doOnUserInfoPage(nodeInfo)
        }
        if (mCurPageType == PageTypeEnum.USER_INFO_PAGE)
            findEditTextInTalkPage(nodeInfo)
    }

    /**
     * 找到评论界面的listView判断是否是评论界面
     */
    private fun findListViewInVideoPage(nodeInfo: AccessibilityNodeInfo?) {
        //if (mListViewFound) return;
        if (nodeInfo == null) return
        //L.i("node className: " + nodeInfo.getClassName());
        for (i in 0 until nodeInfo.childCount) {
            findListViewInVideoPage(nodeInfo.getChild(i))
        }
        if (nodeInfo.className != null && nodeInfo.className == Constants.Widget.ListView) {
            L.i("listView ClassName: " + nodeInfo.className)
            //mListViewFound = true;
            mCurPageType = PageTypeEnum.COMMENT_PAGE
            doOnCommentPage(nodeInfo)
        }
    }

    /**
     * 处理评论的界面
     */
    private fun doOnCommentPage(listViewNode: AccessibilityNodeInfo?) {
        // AccessibilityNodeInfo listViewNode = findViewByRootID(Constants.ResourceId.Video_ListView);
        if (listViewNode != null) {
            L.i("className: " + listViewNode.className)   //ListView
            val clickNodeList = ArrayList<AccessibilityNodeInfo>()  //头像的node列表
            val titleNodeList = ArrayList<AccessibilityNodeInfo>()  //用户名的node列表
            for (i in 0 until listViewNode.childCount) {
                val childInfo = listViewNode.getChild(i) //LinearLayout
                if (childInfo.className == Constants.Widget.LinearLayout) {
                    if (childInfo.childCount > 0) {
                        val frameLayoutNode: AccessibilityNodeInfo? = childInfo.getChild(0)  //FrameLayout
                        if (frameLayoutNode!!.className == Constants.Widget.FrameLayout) {
                            clickNodeList.add(frameLayoutNode)
                        }
                    }
                    if (childInfo.childCount > 1) {
                        val textViewNode = childInfo.getChild(1).getChild(0).getChild(0) //LinearLayout
                        titleNodeList.add(textViewNode)
                    }
                }
            }
            //已开始任务
            if (mCurCommentCount > 0) {
                //任务已做完一轮
                if (mCurCommentCount - 1 == mCommentClickedCount) {
                    //把所有状态重置
                    mLastNodeInfo = titleNodeList[mCommentClickedCount]
                    mCurCommentCount = 0
                    mCommentClickedCount = 0
                    //mListViewFound = false;
                    performScrollForward(listViewNode)
                    return
                }
                mCommentClickedCount++
            } else
                mCommentClickedCount = 0

            mCurCommentCount = clickNodeList.size
            val titleNodeSize = titleNodeList.size
            L.i("clickNode size: $mCurCommentCount titleNodeSize: $titleNodeSize")
            // L.i("titleNode size: " + titleNodeList.size());
            if (mCurCommentCount > 0 && titleNodeSize > 0 && mCommentClickedCount < mCurCommentCount) {
                var clickNodeInfo = clickNodeList[mCommentClickedCount]
                val titleNodeInfo: AccessibilityNodeInfo? = titleNodeList[mCommentClickedCount]
                if (mLastNodeInfo != null && titleNodeInfo != null) {    //最后一个,防止重复发最后一个
                    if (mLastNodeInfo?.text.toString() == titleNodeInfo.text.toString()) {
                        L.i("the last comment already sent")
                        mCommentClickedCount++
                        if (mCommentClickedCount < mCurCommentCount)
                            clickNodeInfo = clickNodeList[mCommentClickedCount]
                    }
                }

                performViewClick(clickNodeInfo)
            } else {
                mCurCommentCount = 0
                mCommentClickedCount = 0
                L.i("当前界面无评论 已评论个数： $mMsgSentCount")

            }
        }
    }

    /**
     * 在用户信息界面点击私信
     */
    private fun doOnUserInfoPage(nodeInfo: AccessibilityNodeInfo?) {
        val talkInfo = findViewByText(mRootNodeInfo!!, "私信", true)
        if (talkInfo != null && talkInfo.className == Constants.Widget.TextView) {
            L.i("talk info: " + talkInfo.text.toString())
            mCurPageType = PageTypeEnum.USER_INFO_PAGE

            getHandler().postDelayed({ performViewClick(talkInfo) }, (mPageSwitchTime * 1000).toLong())
        }
    }

    /**
     * 在聊天界面点击私信
     */
    private fun findEditTextInTalkPage(nodeInfo: AccessibilityNodeInfo?) {
        if (nodeInfo == null) return
        L.i("node className: " + nodeInfo.className)
        for (i in 0 until nodeInfo.childCount) {
            findEditTextInTalkPage(nodeInfo.getChild(i))
        }

        if (nodeInfo.className != null && nodeInfo.className == Constants.Widget.EditText) {
            L.i("editText ClassName: " + nodeInfo.className)
            mCurPageType = PageTypeEnum.SEND_MSG_PAGE
            findHistoryMsg(mRootNodeInfo)
            L.i("ediText parent: " + nodeInfo.parent.className)
            L.i("send node : " + nodeInfo.parent.getChild(1).className)
            //private boolean mListViewFound;
            val sendNode = nodeInfo.parent.getChild(1).getChild(0)
            if (mHistoryNode == null) {
                doTalkOnPage(nodeInfo, sendNode)
            } else {
                //performBack()
                mCurPageType = null
                mHistoryNode = null
                mPerformBackListener!!.onBackPressed(mCurPageType)
            }
        }
    }

    /**
     * 在用户聊天界面发送消息
     */
    private fun doTalkOnPage(editNode: AccessibilityNodeInfo, sendNode: AccessibilityNodeInfo) {
        val arguments = Bundle()
        arguments.putCharSequence(AccessibilityNodeInfo.ACTION_ARGUMENT_SET_TEXT_CHARSEQUENCE, BaseAccessibilityService.mCommentMsg)
        editNode.performAction(AccessibilityNodeInfo.ACTION_SET_TEXT, arguments)
        performViewClick(sendNode)
        mMsgSentCount++
        // performBack()  //todo 网络延时
        mCurPageType = null
        mHistoryNode = null
        mPerformBackListener!!.onBackPressed(mCurPageType)
    }

    /**
     * 查找是否有历史消息
     *
     * @param nodeInfo
     */
    private fun findHistoryMsg(nodeInfo: AccessibilityNodeInfo?) {
        if (nodeInfo == null) return
        for (i in 0 until nodeInfo.childCount) {
            findHistoryMsg(nodeInfo.getChild(i))
        }

        if (nodeInfo.className != null && nodeInfo.className == Constants.Widget.ImageView) {
            L.i("image className: " + nodeInfo.className)
            mHistoryNode = nodeInfo
        }
    }


}