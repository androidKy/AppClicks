package com.app.appclicks.service;

/**
 * description:
 * author:kyXiao
 * date:2019/4/23
 */
public enum PageType {

    COMMENT_PAGE("comment_page"),
    USER_INFO_PAGE("user_info_page"),
    SEND_MSG_PAGE("send_msg_page");

    public String pageType;

    private PageType(String pageType) {
        this.pageType = pageType;
    }
}
