package com.home.wheelpickerdemo;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.OvershootInterpolator;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.zyyoona7.wheel.WheelView;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";

    private WheelView<String> leftWheelView, middleWheelView, rightWheelView;
    private List<String> stringList;
    private FrameLayout frameLayout, interceptSwipeGestureFrameLayout;
    private ImageView imageView;
    private boolean isClick = false;
    private Random random;
    private int randomLeftPosition, randomMiddlePosition, randomRightPosition,
            leftState, middleState, rightState, totalPosition;
    private SharedPreferences sharedPreferences;
    private TextView highestScoreTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        removeStatusBar();
        setContentView(R.layout.activity_main);

        initializationSharedPreferences();

        /** 初始化highestScoreTextView */
        highestScoreTextView = findViewById(R.id.highestScoreTextView);
        highestScoreTextView.setText("＄" + sharedPreferences.getInt("highestScore", 0));

        /** 不要讓使用者可以單獨滑動某個轉輪 */
        interceptSwipeGestureFrameLayout = findViewById(R.id.interceptSwipeGestureFrameLayout);
        interceptSwipeGestureFrameLayout.setClickable(true);

        /** 初始化按鈕 */
        random = new Random();
        imageView = findViewById(R.id.imageView);
        frameLayout = findViewById(R.id.frameLayout);
        setOnTouchListener(frameLayout);

        /** 初始化WheelView */
        stringList = new ArrayList<>(1);
        for (int i = 0; i < 1001; i++) {
            stringList.add("＄" + i);
        }
        leftWheelView = findViewById(R.id.leftWheelView);
        middleWheelView = findViewById(R.id.middleWheelView);
        rightWheelView = findViewById(R.id.rightWheelView);
        initializationWheelView(leftWheelView);
        initializationWheelView(middleWheelView);
        initializationWheelView(rightWheelView);

        /** 設置彎曲弧方向 */
        leftWheelView.setCurvedArcDirection(WheelView.CURVED_ARC_DIRECTION_LEFT);
        middleWheelView.setCurvedArcDirection(WheelView.CURVED_ARC_DIRECTION_CENTER);
        rightWheelView.setCurvedArcDirection(WheelView.CURVED_ARC_DIRECTION_RIGHT);

        /** 添加滑動音效, 只打開左邊的音效, 因為3個同時播放太吵了 */
        leftWheelView.setSoundEffectResource(R.raw.button_choose);
        middleWheelView.setSoundEffectResource(R.raw.button_choose);
        rightWheelView.setSoundEffectResource(R.raw.button_choose);
        leftWheelView.setSoundEffect(true);
        middleWheelView.setSoundEffect(false);
        rightWheelView.setSoundEffect(false);
    }

    /** 初始化SharedPreferences */
    private void initializationSharedPreferences() {
        sharedPreferences = getSharedPreferences("score", MODE_PRIVATE);
    }

    /** 初始化WheelView, 並設定相關細節 */
    private void initializationWheelView(final WheelView<String> wheelView) {
        wheelView.setOnItemSelectedListener(new WheelView.OnItemSelectedListener<String>() {
            @Override
            public void onItemSelected(WheelView<String> wheelView, String data, int position) {
                Log.i(TAG, "onItemSelected: data=" + data + ",position=" + position);
            }
        });
        wheelView.setOnWheelChangedListener(new WheelView.OnWheelChangedListener() {
            @Override
            public void onWheelScroll(int scrollOffsetY) {
            }
            @Override
            public void onWheelItemChanged(int oldPosition, int newPosition) {
            }
            @Override
            public void onWheelSelected(int position) {
            }
            @Override
            public void onWheelScrollStateChanged(int state) {

                /** 如果3個都滑動停止時, 就恢復按鈕的點擊模式 */
                switch (wheelView.getId()) {
                    case R.id.leftWheelView:
                        leftState = state;
                        break;
                    case R.id.middleWheelView:
                        middleState = state;
                        break;
                    case R.id.rightWheelView:
                        rightState = state;
                        break;
                }

                /** 監聽3個滑輪都滑動停止時, 判斷比較並保存顯示最高積分 */
                if (leftState == 0 && middleState == 0 && rightState == 0) {
                    totalPosition = randomLeftPosition + randomMiddlePosition + randomRightPosition;
                    if (sharedPreferences.getInt("highestScore", 0) == 0) {
                        sharedPreferences.edit()
                                .putInt("highestScore", totalPosition)
                                .commit();
                        highestScoreTextView.setText("＄" + totalPosition);
                    }
                    if (sharedPreferences.getInt("highestScore", 0) < totalPosition) {
                        sharedPreferences.edit()
                                .putInt("highestScore", totalPosition)
                                .commit();
                        highestScoreTextView.setText("＄" + totalPosition);
                    }
                    imageView.setClickable(false);
                }
            }
        });
        wheelView.setData(stringList);
    }

    /**
     * 去除状态栏
     */
    private void removeStatusBar() {
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
    }

    /**
     * 針對按下放開的行為, 設定對應的動畫效果
     */
    public void setOnTouchListener(View view) {
        view.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(final View view, MotionEvent motionEvent) {
                switch (motionEvent.getAction()) {
                    case MotionEvent.ACTION_DOWN: {
                        touchDownAnimation(frameLayout);
                        isClick = true;
                        delayedRecovery();
                        return true;
                    }
                    case MotionEvent.ACTION_UP: {
                        touchUpAnimation(frameLayout);
                        if (isClick) {
                            imageView.postDelayed(new Runnable() {
                                @Override
                                public void run() {

                                    /** 產生3個隨機亂數, 並滑動3個滾輪至該亂數 */
                                    imageView.setClickable(true);
                                    randomLeftPosition = random.nextInt(1001);
                                    leftWheelView.setSelectedItemPosition(randomLeftPosition, true, 4000);
                                    randomMiddlePosition = random.nextInt(1001);
                                    middleWheelView.setSelectedItemPosition(randomMiddlePosition, true, 4000);
                                    randomRightPosition = random.nextInt(1001);
                                    rightWheelView.setSelectedItemPosition(randomRightPosition, true, 4000);
                                }
                            }, 300);
                        }
                        return true;
                    }
                    default: {
                        return true;
                    }
                }
            }
        });
    }

    /**
     * 放大的動畫
     */
    void touchDownAnimation(View view) {
        view.animate()
                .scaleX(1.24f)
                .scaleY(1.24f)
                .setDuration(300)
                .setInterpolator(new OvershootInterpolator())
                .start();
    }

    /**
     * 還原的動畫
     */
    void touchUpAnimation(View view) {
        view.animate()
                .scaleX(1f)
                .scaleY(1f)
                .setDuration(300)
                .setInterpolator(new OvershootInterpolator())
                .start();
    }

    /**
     * 0.1秒後恢復數據
     */
    private void delayedRecovery() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(200);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } finally {
                    isClick = false;
                }
            }
        }).start();
    }
}
