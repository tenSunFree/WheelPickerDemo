package com.home.wheelpickerdemo;

import android.app.Application;
import android.content.SharedPreferences;

public class WPDApplication extends Application {

    private SharedPreferences sharedPreferences;

    @Override
    public void onCreate() {
        super.onCreate();

        /** 建立第一次啟動app時的相關數據設定 */
        sharedPreferences = getSharedPreferences("score", MODE_PRIVATE);
        if (sharedPreferences.getBoolean("isFirstOpen", true)) {
            sharedPreferences.edit()
                    .putBoolean("isFirstOpen", false)
                    .putInt("highestScore", 0)
                    .commit();
        }
    }
}
