package com.app.appclicks.service.type;

import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;

import com.app.appclicks.service.FactoryService;
import com.app.appclicks.service.PerformBackListener;

import org.jetbrains.annotations.NotNull;

/**
 * description:我的关注
 * author: kyXiao
 * date: 2019/4/23
 */
public class AttentionService implements FactoryService {
    private static volatile AttentionService mInstance;

    private AttentionService() {

    }

    public static AttentionService getInstance() {
        if (mInstance == null) {
            synchronized (AttentionService.class) {
                if (mInstance == null)
                    mInstance = new AttentionService();
            }
        }
        return mInstance;
    }

    @Override
    public void onAccessibilityEvent(@NotNull AccessibilityEvent event, @NotNull AccessibilityNodeInfo nodeInfo,
                                     @NotNull PerformBackListener performBackListener) {

    }
}
