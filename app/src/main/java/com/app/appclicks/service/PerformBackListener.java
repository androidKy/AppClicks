package com.app.appclicks.service;

import com.app.appclicks.enum_type.PageTypeEnum;

/**
 * description:
 * author: kyXiao
 * date: 2019/4/23
 */
public interface PerformBackListener {
    void onBackPressed(PageTypeEnum lastPageType);
}
