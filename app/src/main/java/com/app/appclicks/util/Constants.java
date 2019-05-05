package com.app.appclicks.util;

import org.jetbrains.annotations.Nullable;

/**
 * description:
 * author:kyXiao
 * date:2019/4/17
 */
public class Constants {


    public interface Package {
        String TODAY_NEWS = "com.ss.android.article.news";
        String WE_CHAT = "com.tencent.mm";
    }

    public interface Widget {
        String ListView = "android.widget.ListView";
        String TextView = "android.widget.TextView";
        String ImageView = "android.widget.ImageView";
        String LinearLayout = "android.widget.LinearLayout";
        String FrameLayout = "android.widget.FrameLayout";
        String EditText = "android.widget.EditText";
        String WebView = "com.tencent.tbs.core.webkit.WebView";
    }

    public interface ResourceId {
        String Video_ListView = "com.ss.android.article.news:id/a5i";
        String Comment_TextView = "com.ss.android.article.news:id/gw";
        String Back_TextView = "com.ss.android.article.news:id/a3";
        String Msg_EditText = "com.ss.android.article.news:id/edit_message";    //com.ss.android.article.news:id/edit_message
        String Send_TextView = "com.ss.android.article.news:id/send_message";
    }
}
