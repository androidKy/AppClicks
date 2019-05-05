package com.app.appclicks.wxservice

/**
 * description:
 * author: kyXiao
 * date: 2019/5/5
 */
public enum class WxPageEnum private constructor(pageType: String) {
    STARTING_PAGE("start_page"),    //启动页面
    TALK_MAIN_PAGE("main_page"), //微信主页
    TENCENT_NEWS_PAGE("tencent_news"),   //腾讯新闻主页
    NEWS_PAGE("news_page")  //新闻页面
}