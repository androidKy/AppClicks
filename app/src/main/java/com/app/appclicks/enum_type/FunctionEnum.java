package com.app.appclicks.enum_type;

/**
 * description:功能枚举类
 * author: kyXiao
 * date: 2019/4/23
 */
public enum FunctionEnum {
    SEND_MSG_ATTENTION("私信我的关注"),
    SEND_MSG_COMMENT("私信评论用户");

    private String functionEnum;

    FunctionEnum(String functionEnum) {
        this.functionEnum = functionEnum;
    }
}
