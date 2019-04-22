package com.app.appclicks.service;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.AccessibilityServiceInfo;
import android.graphics.Rect;
import android.os.Bundle;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;

/**
 * @author: flint
 * @date: 2019/3/13 21:36
 */
public class AutoClickService extends AccessibilityService {
    private static final String TAG = "AutoClickService";

    // 发送的text
    private static String sendText = "";

    private static boolean isMyTextClick = false;
    private static boolean isAttentionClick = false;

    private static boolean isLatterClick = false;
    private static boolean isSetText = false;
    private static boolean isSendTextClick = false;


    private static boolean isItemClick = false;
    private static int clickItem = 0;

    // 关注数量
    private static int attentionCount = 0;
    
    private static int curItemCount = 0;
    // 第一屏item数量
    private static int firstItemCount = 0;

    public static void clearState() {
        isMyTextClick = false;
        isAttentionClick = false;
        isLatterClick = false;
        isSetText = false;
        isSendTextClick = false;

        isItemClick = false;
        clickItem = 0;
        attentionCount = 0;
        curItemCount = 0;
        firstItemCount = 0;
    }

    public static void setSendText(String text) {
        Log.d(TAG, "setSendText: " + text);
        sendText = text;
    }

    public static void resetPersonState() {
        isLatterClick = false;
        isSetText = false;
        isSendTextClick = false;
    }

    @Override
    protected void onServiceConnected() {
        Log.d(TAG, "onServiceConnected: ");

                AccessibilityServiceInfo info = getServiceInfo();
        //这里可以设置多个包名，监听多个应用
        info.packageNames = new String[]{
                "android",
                "com.google.android.packageinstaller",
                "com.ss.android.article.news" };
        setServiceInfo(info);

        super.onServiceConnected();
    }

    @Override
    public void onInterrupt() {

    }

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        try {
            //拿到根节点
            AccessibilityNodeInfo rootInfo = getRootInActiveWindow();
            if (rootInfo != null) {
                DFS(rootInfo);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 深度优先遍历寻找目标节点
     */
    private void DFS(final AccessibilityNodeInfo rootInfo) {

        if (rootInfo == null) {
            return;
        }

        for (int i = 0; i < rootInfo.getChildCount(); i++) {
            DFS(rootInfo.getChild(i));
        }


        if (rootInfo.getViewIdResourceName() != null &&
                rootInfo.getClassName() != null &&
                rootInfo.getClassName().equals("android.widget.TextView") &&
                rootInfo.getText() != null &&
                rootInfo.getText().equals("我的")) {

            if (!isMyTextClick) {
                Log.e(TAG, "点击我的");
                performClick(rootInfo.getParent());
                isMyTextClick = true;
            }
        }

        // 私信。需要放到关注前面，防止进入关注用户界面，点击到关注用户的关注
        if (rootInfo.getViewIdResourceName() != null &&
                rootInfo.getClassName() != null &&
                rootInfo.getClassName().equals("android.widget.TextView") &&
                rootInfo.getText() != null &&
                rootInfo.getText().equals("私信")) {

            if (!isLatterClick && isItemClick) {
                Log.e(TAG, "点击私信");
                performClick(rootInfo);
                isLatterClick = true;
            }
        }

        if (rootInfo.getViewIdResourceName() != null &&
                rootInfo.getClassName() != null &&
                rootInfo.getClassName().equals("android.widget.TextView") &&
                rootInfo.getText() != null &&
                rootInfo.getText().equals("关注")) {

            AccessibilityNodeInfo attentParent = rootInfo.getParent().getParent();
            for (int i = 0; i < attentParent.getChildCount(); i++) {
                if (attentParent.getChild(i).getContentDescription() != null &&
                        attentParent.getChild(i).getContentDescription().equals("头像")) {
                    return;
                }
            }

            if (!isAttentionClick) {
                Log.e(TAG, "点击关注");
                performClick(rootInfo.getParent());
                isAttentionClick = true;
            }
        }

        // 关注数量
        if (rootInfo.getViewIdResourceName() != null &&
                rootInfo.getClassName() != null &&
                rootInfo.getClassName().equals("android.widget.TextView") &&
                rootInfo.getParent() != null &&
                rootInfo.getParent().getChildCount() == 2 &&
                rootInfo.getParent().getChild(0) != null &&
                rootInfo.getParent().getChild(1) != null &&
                rootInfo.getParent().getChild(0).getClassName() != null &&
                rootInfo.getParent().getChild(1).getClassName() != null &&
                rootInfo.getParent().getChild(0).getClassName().equals("android.widget.TextView") &&
                rootInfo.getParent().getChild(1).getClassName().equals("android.widget.TextView") &&
                rootInfo.getParent().getChild(0).getText() != null &&
                rootInfo.getParent().getChild(1).getText() != null &&
                (rootInfo.getParent().getChild(0).getText().equals("关注") || rootInfo.getParent().getChild(1).getText().equals("关注"))) {

            if (rootInfo.getText() != null && !rootInfo.getText().equals("关注")
                    && !isItemClick && attentionCount == 0) {
                Log.e(TAG, "关注数量 : " + rootInfo.getText());
                attentionCount = Integer.valueOf(rootInfo.getText().toString());
            }
        }

        if (rootInfo.getViewIdResourceName() != null &&
                rootInfo.getClassName() != null &&
                rootInfo.getClassName().equals("android.widget.EditText") &&
                rootInfo.getViewIdResourceName().equals("com.ss.android.article.news:id/edit_message")) {

            if (!isSetText) {
                Log.e(TAG, "输入文本");
                performText(rootInfo);
                isSetText = true;
                DFS(rootInfo.getParent().getParent());
            }
        }

        if (rootInfo.getViewIdResourceName() != null &&
                rootInfo.getClassName() != null &&
                rootInfo.getClassName().equals("android.widget.TextView") &&
                rootInfo.getText() != null &&
                rootInfo.getText().equals("发送")) {

            if (!isSendTextClick) {
                Log.e(TAG, "点击发送");
                performClick(rootInfo.getParent());
                isSendTextClick = true;

                // 点击返回
                AccessibilityNodeInfo chatParent = rootInfo.getParent().getParent().getParent();
                for (int i = 0; i < chatParent.getChildCount(); i++) {
                    if (chatParent.getChild(i).getClassName() != null &&
                            chatParent.getChild(i).getClassName().equals("android.widget.TextView")) {
                        Rect rect = new Rect();
                        chatParent.getChild(i).getBoundsInScreen(rect);
                        if (rect.left == 0) {
                            performClick(chatParent.getChild(i));
                            Log.d(TAG, "点击聊天界面返回");
                            return;
                        }
                    }
                }

            }
        }

        if (rootInfo.getViewIdResourceName() != null &&
                rootInfo.getClassName() != null &&
                rootInfo.getClassName().equals("android.widget.TextView") &&
                rootInfo.getContentDescription() != null &&
                rootInfo.getContentDescription().equals("返回")) {

            if (isSendTextClick && isSetText && isItemClick) {
                // 重置 item 点击状态
                isItemClick = false;
                isSendTextClick = false;

                performClick(rootInfo);
                Log.e(TAG, "点击个人界面返回");
            }
        }

        if (rootInfo.getViewIdResourceName() != null &&
                rootInfo.getClassName() != null &&
                rootInfo.getClassName().equals("android.support.v7.widget.RecyclerView") &&
                rootInfo.getParent() != null &&
                rootInfo.getParent().getClassName() != null &&
                rootInfo.getParent().getClassName().equals("android.support.v4.view.ViewPager")) {


            // 判断是否是已关注界面
            if (!isAttentionLayoutView(rootInfo)) {
                return;
            }

            if (curItemCount == 0) {
                curItemCount = rootInfo.getChildCount();
                firstItemCount = curItemCount;
            }

            if (curItemCount != 0 && curItemCount != rootInfo.getChildCount()) {
                return;
            }

            Log.d(TAG, "DFS count: " + rootInfo.getChildCount());

            if (clickItem > 0 && clickItem == curItemCount) {
                Log.d(TAG, "滑动关注列表");
                rootInfo.performAction(AccessibilityNodeInfo.ACTION_SCROLL_FORWARD);
                clickItem = 0;
                curItemCount = firstItemCount + 1;
                return;
            }

            for (int i = 0; i < rootInfo.getChildCount(); i++) {
                if (clickItem == i && !isItemClick) {
                    if (curItemCount > firstItemCount && i == 0) {
                        Log.d(TAG, "跳过缓存的第一项");
                        clickItem++;
                        continue;
                    }

                    rootInfo.getChild(i).performAction(AccessibilityNodeInfo.ACTION_CLICK);
                    performClick(rootInfo.getChild(i));

                    resetPersonState();
                    clickItem++;
                    isItemClick = true;
                    Log.e(TAG, "点击第 : " + clickItem + " 项");
                    break;
                }
            }
        }
    }

    private void performClick(AccessibilityNodeInfo targetInfo) {
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        targetInfo.performAction(AccessibilityNodeInfo.ACTION_CLICK);
    }

    // 输入要发送的文字
    private void performText(AccessibilityNodeInfo targetInfo) {
        Bundle arguments = new Bundle();
        arguments.putCharSequence(AccessibilityNodeInfo.ACTION_ARGUMENT_SET_TEXT_CHARSEQUENCE, sendText);
        targetInfo.performAction(AccessibilityNodeInfo.ACTION_SET_TEXT, arguments);
    }

    private boolean isAttentionLayoutView(AccessibilityNodeInfo targetInfo) {
        if (targetInfo == null) {
            return false;
        }

        if (targetInfo.getText() != null && targetInfo.getText().equals("已关注")) {
            return true;
        }

        for (int i = 0; i < targetInfo.getChildCount(); i++) {
            if (isAttentionLayoutView(targetInfo.getChild(i))) {
                return true;
            }
        }

        return false;
    }
}
