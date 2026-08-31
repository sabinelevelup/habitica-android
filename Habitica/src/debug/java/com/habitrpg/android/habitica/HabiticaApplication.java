package com.habitrpg.android.habitica;

import leakcanary.AppWatcher;
import leakcanary.LeakCanary;

public class HabiticaApplication extends HabiticaBaseApplication {
    @Override
    public void onCreate() {
        AppWatcher.setConfig(
            AppWatcher.getConfig().newBuilder()
                .enabled(false)
                .build()
        );
        LeakCanary.setConfig(
            LeakCanary.getConfig().newBuilder()
                .dumpHeap(false)
                .showNotifications(false)
                .build()
        );
        super.onCreate();
    }
}
