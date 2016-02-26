package cy.studiodemo.fragment;

import android.content.Context;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.media.AudioManager;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.OrientationEventListener;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Formatter;
import java.util.Locale;

import cy.studiodemo.R;
import cy.studiodemo.adapter.JieMuAdapter;
import cy.studiodemo.base.BaseFragment;
import cy.studiodemo.engine.GlobalParams;
import cy.studiodemo.player.VideoPlayer;
import cy.studiodemo.view.BigSeekBar;
import cy.studiodemo.view.VerticalSeekBar;
import cy.studiodemo.player.VideoPlayer.VideoPlayerListener;

/**
 * Created by cuiyue on 15/7/27.
 */
public class VideoFragment extends BaseFragment implements View.OnClickListener {


    private static final int MSG_START_PLAY = 1;
    private static final int MSG_PAUSE_PLAY = 3;
    private static final int MSG_CONTINUE_PLAY = 5;
    private static final int MSG_STOP_PLAY = 7;

    private static final int MSG_SHOW_SAMLL_CONTROLS = 11;
    private static final int MSG_HIDE_SAMLL_CONTROLS_ANIM = 13;
    private static final int MSG_HIDE_SAMLL_CONTROLS_NOW = 14;
    private static final int MSG_SHOW_SMALL_YINLIANG = 15;
    private static final int MSG_HIDE_SMALL_YINLIANG = 17;

    private static final int MSG_SHOW_BIG_CONTROLS = 21;
    private static final int MSG_HIDE_BIG_CONTROLS_ANIM = 22;
    private static final int MSG_HIDE_BIG_CONTROLS_NOW = 23;

    private static final int MSG_SHOW_JIEMU_LIST_ANIM = 30;
    private static final int MSG_HIDE_JIEMU_LIST_ANIM = 31;

    private AudioManager audioManager;
    private StringBuilder mFormatBuilder;
    private Formatter mFormatter;
    private VideoPlayer videoPlayer;
    private SurfaceView mSurfaceView;
    private RelativeLayout rlTopPart;
    private RelativeLayout flBottomPart;
    // 小控制
    private FrameLayout flSamllControls;
    private RelativeLayout small_controls_head;
    private Button small_controls_head_back_bt;
    private TextView small_controls_head_title_tv;
    private LinearLayout small_controls_Bottom;
    private ImageButton small_Play_bt;
    private ImageButton mFullButton;
    private SeekBar smallSeekBar;
    private LinearLayout small_yinliang_ll;
    private VerticalSeekBar small_yinliang_seekbar;
    private ImageButton small_yinliang;

    // 大屏幕控制
    private RelativeLayout flBigControls;
    private RelativeLayout big_controls_head;
    private Button big_controls_head_back_bt;
    private Button big_controls_head_list_bt;
    private ListView big_controls_jiemu_list;
    private BigSeekBar bigSeekBar;
    private TextView big_controls_duration_tv;
    private ProgressBar progressbar;
    private boolean ScreenOrientation = true; // fasle 代表横屏， true 代表竖屏
    private Boolean mIsClickIntoFull = false;

    private long mExitTime;


    private Handler mHandler = new Handler() {

        @Override
        public void handleMessage(Message msg) {
            int what = msg.what;
            switch (what) {
                case MSG_START_PLAY:
                    startPlay();
                    break;
                case MSG_PAUSE_PLAY:
                    pausePlay();
                    break;
                case MSG_CONTINUE_PLAY:
                    continuePlay();
                    break;
                case MSG_STOP_PLAY:
                    stopPlay();
                    break;
                case MSG_SHOW_SAMLL_CONTROLS:
                    showSamllControls();
                    break;
                case MSG_HIDE_SAMLL_CONTROLS_ANIM:
                    hideSmallControlsByAnimation();
                    break;
                case MSG_HIDE_SAMLL_CONTROLS_NOW:
                    hideSmallControlsNow();
                    break;
                case MSG_SHOW_SMALL_YINLIANG:
                    showYinLiang();
                    break;
                case MSG_HIDE_SMALL_YINLIANG:
                    hideYinLiang();
                    break;
                case MSG_SHOW_BIG_CONTROLS:
                    showBigControls();
                    break;
                case MSG_HIDE_BIG_CONTROLS_NOW:
                    hideBigControlsNow();
                    break;
                case MSG_HIDE_BIG_CONTROLS_ANIM:
                    hideBigControlsByAnimation();
                    break;
                case MSG_SHOW_JIEMU_LIST_ANIM:
                    showJieMuListByAnimation();
                    break;
                case MSG_HIDE_JIEMU_LIST_ANIM:
                    HideJieMuListByAnimation();
                    break;
            }
            super.handleMessage(msg);
        }

    };

    @Override
    protected void initVariable() {
        initData();
    }

    @Override
    protected int setContentView() {
        return R.layout.fragment_video;
    }

    @Override
    protected void findViews(View rootView) {
        mSurfaceView = (SurfaceView) rootView.findViewById(R.id.surface);
        rlTopPart = (RelativeLayout) rootView.findViewById(R.id.rlTopPart);
        flBottomPart = (RelativeLayout) rootView.findViewById(R.id.flBottomPart);
        progressbar = (ProgressBar) rootView.findViewById(R.id.progressbar);
        findSmallControlsViews(rootView);
        findBigControlsViews(rootView);
    }

    @Override
    public void onResume() {
        super.onResume();
        if(videoPlayer.isPlaying()){
            mHandler.removeMessages(MSG_CONTINUE_PLAY);
            mHandler.sendEmptyMessage(MSG_CONTINUE_PLAY);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        mHandler.removeMessages(MSG_PAUSE_PLAY);
        mHandler.sendEmptyMessage(MSG_PAUSE_PLAY);
    }

    @Override
    protected void setListensers() {
        rlTopPart.setOnClickListener(this);

        // 小控制
        small_controls_head_back_bt.setOnClickListener(this);
        mFullButton.setOnClickListener(this);
        small_Play_bt.setOnClickListener(this);
        small_yinliang.setOnClickListener(this);
        smallSeekBar.setOnSeekBarChangeListener(new MyOnPlaySeekBarChangeListener());
        bigSeekBar.setOnSeekBarChangeListener(new MyOnPlaySeekBarChangeListener());
        small_yinliang_seekbar.setOnSeekBarChangeListener(new MyOnVerticalSeekBar());

        // 横屏控制
        big_controls_head_back_bt.setOnClickListener(this);
        big_controls_head_list_bt.setOnClickListener(this);
        big_controls_jiemu_list.setOnItemClickListener(new MyJieMuOnItemClickListener());

        videoPlayer = new VideoPlayer(mContext);
        videoPlayer.setVideoPlayerListener(new MyVideoPlayerListener());
        mSurfaceView.getHolder().setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS); // 不缓冲
        mSurfaceView.getHolder().setKeepScreenOn(true); // 保持屏幕高亮
        mSurfaceView.getHolder().addCallback(new MySurfaceCallBack()); // 设置监听事件
        videoPlayer.setDisplay(mSurfaceView.getHolder());

    }

    private void initData() {
        audioManager = (AudioManager) mContext.getSystemService(Context.AUDIO_SERVICE);
        mFormatBuilder = new StringBuilder();
        mFormatter = new Formatter(mFormatBuilder, Locale.getDefault());
        int curVol = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
        int maxVol = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
        small_yinliang_seekbar.setMax(maxVol);
        small_yinliang_seekbar.setProgress(curVol);
        ArrayList<String> data = getJieMuData();
        big_controls_jiemu_list.setAdapter(new JieMuAdapter(mContext, data));

        initBigSeekBar();
    }

    private void initBigSeekBar() {
        bigSeekBar.setThumb(writeOnDrawable("00:00:00"));
    }

    private void findSmallControlsViews(View rootView) {
        flSamllControls = (FrameLayout) rootView.findViewById(R.id.flSamllControls);
        small_controls_head = (RelativeLayout) rootView.findViewById(R.id.small_controls_head);
        small_controls_head_back_bt = (Button) rootView.findViewById(R.id.small_controls_head_back_bt);
        small_controls_head_title_tv = (TextView) rootView.findViewById(R.id.small_controls_head_title_tv);
        small_controls_Bottom = (LinearLayout) rootView.findViewById(R.id.small_controls_Bottom);
        small_Play_bt = (ImageButton) rootView.findViewById(R.id.small_Play_bt);
        mFullButton = (ImageButton) rootView.findViewById(R.id.small_full);
        smallSeekBar = (SeekBar) rootView.findViewById(R.id.small_seekbar);
        small_yinliang = (ImageButton) rootView.findViewById(R.id.small_yinliang);
        small_yinliang_ll = (LinearLayout) rootView.findViewById(R.id.llsmall_yinliang);
        small_yinliang_seekbar = (VerticalSeekBar) rootView.findViewById(R.id.small_yinliang_seekbar);
    }

    private void findBigControlsViews(View rootView) {
        flBigControls = (RelativeLayout) rootView.findViewById(R.id.flBigControls);
        big_controls_head = (RelativeLayout) rootView.findViewById(R.id.big_controls_head);
        big_controls_head_back_bt = (Button) rootView.findViewById(R.id.big_controls_head_back_bt);
        big_controls_head_list_bt = (Button) rootView.findViewById(R.id.big_controls_head_list_bt);
        big_controls_jiemu_list = (ListView) rootView.findViewById(R.id.big_controls_jiemu_list);

        bigSeekBar = (BigSeekBar) rootView.findViewById(R.id.big_controls_seekbar);
        big_controls_duration_tv = (TextView) rootView.findViewById(R.id.big_controls_duration_tv);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.rlTopPart:
                if (ScreenOrientation) {
                    if (flSamllControls.isShown()) {
                        mHandler.removeMessages(MSG_HIDE_SAMLL_CONTROLS_ANIM);
                        mHandler.sendEmptyMessage(MSG_HIDE_SAMLL_CONTROLS_ANIM);
                    } else {
                        mHandler.removeMessages(MSG_SHOW_SAMLL_CONTROLS);
                        mHandler.sendEmptyMessage(MSG_SHOW_SAMLL_CONTROLS);
                        Message msg = Message.obtain();
                        msg.what = MSG_HIDE_SAMLL_CONTROLS_ANIM;
                        mHandler.sendMessageDelayed(msg, 4500);
                    }

                } else {
                    if (flBigControls.isShown()) {
                        mHandler.removeMessages(MSG_HIDE_BIG_CONTROLS_ANIM);
                        mHandler.sendEmptyMessage(MSG_HIDE_BIG_CONTROLS_ANIM);
                    } else {
                        mHandler.removeMessages(MSG_SHOW_BIG_CONTROLS);
                        mHandler.sendEmptyMessage(MSG_SHOW_BIG_CONTROLS);
                    }
                }
                break;

            case R.id.small_Play_bt:
                if (videoPlayer.isPlaying()) {
                    mHandler.removeMessages(MSG_PAUSE_PLAY);
                    mHandler.sendEmptyMessage(MSG_PAUSE_PLAY);
                } else {
                    mHandler.removeMessages(MSG_CONTINUE_PLAY);
                    mHandler.sendEmptyMessage(MSG_CONTINUE_PLAY);
                }
                break;
            case R.id.small_full:
                mIsClickIntoFull = true;
                goBigScreen();
                break;
            case R.id.small_yinliang:
                if (small_yinliang_ll.isShown()) {
                    mHandler.removeMessages(MSG_HIDE_SMALL_YINLIANG);
                    mHandler.sendEmptyMessage(MSG_HIDE_SMALL_YINLIANG);
                } else {
                    mHandler.removeMessages(MSG_SHOW_SMALL_YINLIANG);
                    mHandler.sendEmptyMessage(MSG_SHOW_SMALL_YINLIANG);
                }
                break;
            case R.id.small_controls_head_back_bt:
                stopVideo();
                break;
            case R.id.big_controls_head_list_bt:
                if (big_controls_jiemu_list.isShown()) {
                    mHandler.removeMessages(MSG_HIDE_JIEMU_LIST_ANIM);
                    mHandler.sendEmptyMessage(MSG_HIDE_JIEMU_LIST_ANIM);
                } else {
                    mHandler.removeMessages(MSG_SHOW_JIEMU_LIST_ANIM);
                    mHandler.sendEmptyMessage(MSG_SHOW_JIEMU_LIST_ANIM);
                }
                break;
        }
    }

    private void startPlay() {
        videoPlayer.start(GlobalParams.path);


    }

    public void pausePlay() {
        small_Play_bt.setImageResource(R.drawable.zanting);
        videoPlayer.pause();
    }

    private void continuePlay() {
        small_Play_bt.setImageResource(R.drawable.live_play_control_play_selector);
        videoPlayer.restart();
    }

    private void stopPlay() {
        videoPlayer.release();
    }

    public void stopVideo() {
        mHandler.removeMessages(MSG_STOP_PLAY);
        mHandler.sendEmptyMessage(MSG_STOP_PLAY);
    }

    private void showYinLiang() {
        small_yinliang_ll.setVisibility(View.VISIBLE);
        small_yinliang_seekbar.setVisibility(View.VISIBLE);
    }

    private void hideYinLiang() {
        small_yinliang_ll.setVisibility(View.GONE);
        small_yinliang_seekbar.setVisibility(View.GONE);
    }

    private void showSamllControls() {
        flSamllControls.setVisibility(View.VISIBLE);
        small_controls_head.startAnimation(AnimationUtils.loadAnimation(mContext, R.anim.top_down));
        small_controls_head.setVisibility(View.VISIBLE);
        small_controls_Bottom.startAnimation(AnimationUtils.loadAnimation(mContext, R.anim.bottom_in));
        small_controls_Bottom.setVisibility(View.VISIBLE);
    }

    private void hideSmallControlsNow() {
        small_controls_head.setVisibility(View.GONE);
        small_controls_Bottom.setVisibility(View.GONE);
        flSamllControls.setVisibility(View.GONE);
    }

    private ArrayList<String> getJieMuData() {
        ArrayList<String> data = new ArrayList<String>();
        for (int i = 1; i < 21; i++) {
            data.add("节目" + i);
        }
        return data;
    }

    public void goBigScreen() {
        if (getActivity().getRequestedOrientation() != ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE) {
            getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);// 横屏
            mHandler.removeMessages(MSG_HIDE_SAMLL_CONTROLS_ANIM);
            mHandler.sendEmptyMessage(MSG_HIDE_SAMLL_CONTROLS_ANIM);
        }
    }

    public void goSmallScreen() {
        mHandler.removeMessages(MSG_HIDE_BIG_CONTROLS_NOW);
        mHandler.sendEmptyMessage(MSG_HIDE_BIG_CONTROLS_NOW);
    }

    /**
     * 横屏
     */
    public void onBigScreen() {
        getActivity().getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        RelativeLayout.LayoutParams rlLayout = new RelativeLayout.LayoutParams(getResources().getDisplayMetrics().widthPixels, getResources()
                .getDisplayMetrics().heightPixels);
        rlLayout.topMargin = 0;
        rlTopPart.setLayoutParams(rlLayout);
        flBottomPart.setVisibility(View.GONE);
        mHandler.removeMessages(MSG_HIDE_SAMLL_CONTROLS_NOW);
        mHandler.sendEmptyMessage(MSG_HIDE_SAMLL_CONTROLS_NOW);
        ScreenOrientation = false;
    }

    /**
     * 竖屏
     */
    public void onSmallScreen() {
        getActivity().getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        int height = (int) getResources().getDimension(R.dimen.small_player_height);
        RelativeLayout.LayoutParams rlLayout = new RelativeLayout.LayoutParams(getResources().getDisplayMetrics().widthPixels, height);
        rlLayout.topMargin = 0;
        rlTopPart.setLayoutParams(rlLayout);
        flBottomPart.setVisibility(View.VISIBLE);
        mHandler.removeMessages(MSG_HIDE_BIG_CONTROLS_NOW);
        mHandler.sendEmptyMessage(MSG_HIDE_BIG_CONTROLS_NOW);
        ScreenOrientation = true;

    }

    private class MyVideoPlayerListener implements VideoPlayerListener {

        @Override
        public void onPrepared() {
            smallSeekBar.setMax(videoPlayer.getDuration());
            bigSeekBar.setMax(videoPlayer.getDuration());
            big_controls_duration_tv.setText(stringForTime(videoPlayer.getDuration()));

        }

        @Override
        public void onPlaying(int postion) {
            smallSeekBar.setProgress(postion);
            bigSeekBar.setThumb(writeOnDrawable(stringForTime(postion)));
        }

        @Override
        public void showOrHideProgressbar(boolean isShow) {
            if (isShow) {
                progressbar.setVisibility(View.VISIBLE);
            } else {
                progressbar.setVisibility(View.GONE);
            }
        }

        @Override
        public void onCompletion() {

        }

        @Override
        public void onError() {
        }

        @Override
        public void onBuffering(int percent) {
        }

        @Override
        public void onBufferStart() {
        }

        @Override
        public void onBufferEnd() {

        }

        @Override
        public void onSeekComplete() {
        }

        @Override
        public void onVideoSizeChanged(int width, int heiht) {
        }
    }

    private void hideSmallControlsByAnimation() {
        Animation loadAnimation = AnimationUtils.loadAnimation(mContext, R.anim.top_out);
        loadAnimation.setAnimationListener(new Animation.AnimationListener() {

            @Override
            public void onAnimationStart(Animation animation) {
                hideYinLiang();
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                small_controls_head.setVisibility(View.GONE);
                small_controls_Bottom.setVisibility(View.GONE);
                flSamllControls.setVisibility(View.GONE);
            }
        });
        small_controls_head.startAnimation(loadAnimation);
        small_controls_Bottom.startAnimation(AnimationUtils.loadAnimation(mContext, R.anim.bottom_out));
    }

    private void hideBigControlsByAnimation() {
        Animation loadAnimation = AnimationUtils.loadAnimation(mContext, R.anim.top_out);
        bigSeekBar.startAnimation(AnimationUtils.loadAnimation(mContext, R.anim.bottom_out));
        big_controls_duration_tv.startAnimation(AnimationUtils.loadAnimation(mContext, R.anim.bottom_out));
        loadAnimation.setAnimationListener(new Animation.AnimationListener() {

            @Override
            public void onAnimationStart(Animation arg0) {
                if (big_controls_jiemu_list.isShown()) {
                    mHandler.removeMessages(MSG_HIDE_JIEMU_LIST_ANIM);
                    mHandler.sendEmptyMessage(MSG_HIDE_JIEMU_LIST_ANIM);
                }
            }

            @Override
            public void onAnimationRepeat(Animation arg0) {

            }

            @Override
            public void onAnimationEnd(Animation arg0) {
                flBigControls.setVisibility(View.GONE);
                big_controls_head.setVisibility(View.GONE);
                bigSeekBar.setVisibility(View.GONE);
                big_controls_duration_tv.setVisibility(View.GONE);
            }
        });
        big_controls_head.startAnimation(loadAnimation);
    }

    private void showJieMuListByAnimation() {
        big_controls_jiemu_list.setAnimation(AnimationUtils.loadAnimation(mContext, R.anim.right_in));
        big_controls_jiemu_list.setVisibility(View.VISIBLE);
    }

    private void HideJieMuListByAnimation() {
        Animation loadAnimation = AnimationUtils.loadAnimation(mContext, R.anim.right_out);
        big_controls_jiemu_list.setAnimation(loadAnimation);
        loadAnimation.setAnimationListener(new Animation.AnimationListener() {

            @Override
            public void onAnimationStart(Animation arg0) {

            }

            @Override
            public void onAnimationRepeat(Animation arg0) {

            }

            @Override
            public void onAnimationEnd(Animation arg0) {
                big_controls_jiemu_list.setVisibility(View.GONE);
            }
        });
        big_controls_jiemu_list.startAnimation(loadAnimation);

    }

    private void showBigControls() {
        flBigControls.setVisibility(View.VISIBLE);
        big_controls_head.startAnimation(AnimationUtils.loadAnimation(mContext, R.anim.top_down));
        big_controls_head.setVisibility(View.VISIBLE);
        bigSeekBar.startAnimation(AnimationUtils.loadAnimation(mContext, R.anim.bottom_in));
        big_controls_duration_tv.startAnimation(AnimationUtils.loadAnimation(mContext, R.anim.bottom_in));
        bigSeekBar.setVisibility(View.VISIBLE);
        big_controls_duration_tv.setVisibility(View.VISIBLE);
        big_controls_jiemu_list.setVisibility(View.GONE);
    }

    private void hideBigControlsNow() {
        flBigControls.setVisibility(View.GONE);
        big_controls_head.setVisibility(View.GONE);
        bigSeekBar.setVisibility(View.GONE);
        big_controls_duration_tv.setVisibility(View.GONE);
    }


    private String stringForTime(int timeMs) {
        int totalSeconds = timeMs / 1000;
        int seconds = totalSeconds % 60;
        int minutes = (totalSeconds / 60) % 60;
        int hours = totalSeconds / 3600;
        mFormatBuilder.setLength(0);
        return mFormatter.format("%02d:%02d:%02d", hours, minutes, seconds).toString();
    }


    private Drawable writeOnDrawable(String text) {
        Bitmap bmpScaled = BitmapFactory.decodeResource(getResources(), R.drawable.playbar_time_box).copy(Bitmap.Config.ARGB_8888, true);
        Paint paint = new Paint();
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(Color.WHITE);
        float textSize = 28;
        paint.setTextSize(textSize);
        paint.setFlags(Paint.ANTI_ALIAS_FLAG);
        Canvas canvas = new Canvas(bmpScaled);
        canvas.drawText(text, convertDipToPx(11), convertDipToPx(26), paint);
        Drawable newThumb = new BitmapDrawable(getResources(), bmpScaled);
        return newThumb;
    }


    private int convertDipToPx(int dip) {
        float scale = getResources().getDisplayMetrics().density;
        return (int) (dip * scale + 0.5f * (dip >= 0 ? 1 : -1));
    }


    private class MyOnPlaySeekBarChangeListener implements SeekBar.OnSeekBarChangeListener {

        @Override
        public void onProgressChanged(SeekBar seekbar, int arg1, boolean fromUser) {
            if (fromUser) {
                videoPlayer.seekTo(seekbar.getProgress());
            }
        }

        @Override
        public void onStartTrackingTouch(SeekBar arg0) {

        }

        @Override
        public void onStopTrackingTouch(SeekBar arg0) {

        }
    }

    private class MyOnVerticalSeekBar implements VerticalSeekBar.OnSeekBarChangeListener {

        @Override
        public void onProgressChanged(VerticalSeekBar vBar, int progress, boolean fromUser) {
            if (fromUser) {
                small_yinliang_seekbar.setProgress(progress);
                audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, progress, 0);
            }
        }

        @Override
        public void onStartTrackingTouch(VerticalSeekBar vBar) {

        }

        @Override
        public void onStopTrackingTouch(VerticalSeekBar vBar) {

        }

    }

    private class MySurfaceCallBack implements SurfaceHolder.Callback {

        @Override
        public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

        }

        @Override
        public void surfaceCreated(SurfaceHolder holder) {
            mHandler.sendEmptyMessage(MSG_START_PLAY);
        }

        @Override
        public void surfaceDestroyed(SurfaceHolder holder) {

        }

    }

    private class MyJieMuOnItemClickListener implements AdapterView.OnItemClickListener {

        @Override
        public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
            Toast.makeText(mContext, "点击了节目" + (arg2 + 1), Toast.LENGTH_SHORT).show();
            mHandler.removeMessages(MSG_HIDE_BIG_CONTROLS_ANIM);
            mHandler.sendEmptyMessage(MSG_HIDE_BIG_CONTROLS_ANIM);
        }

    }
}
