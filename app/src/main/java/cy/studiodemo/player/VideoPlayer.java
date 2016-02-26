package cy.studiodemo.player;

import android.content.Context;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnBufferingUpdateListener;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnErrorListener;
import android.media.MediaPlayer.OnInfoListener;
import android.media.MediaPlayer.OnPreparedListener;
import android.media.MediaPlayer.OnSeekCompleteListener;
import android.media.MediaPlayer.OnVideoSizeChangedListener;
import android.net.Uri;
import android.os.Handler;
import android.view.SurfaceHolder;

public class VideoPlayer {
    private Context mContext;
    private MediaPlayer mMediaPlayer;
    private SurfaceHolder mHolder;
    private VideoPlayerListener mListener;

    private static final int UPDATE_PROGRESS = 0;

    private boolean isPrepared = false;
    private boolean isReleased = false;

    private int mOldPostion = 0;

    public VideoPlayer(Context context) {
        this.mContext = context;
        mMediaPlayer = new MediaPlayer();
        mMediaPlayer.setOnPreparedListener(new MyOnPreparedListener());
        mMediaPlayer.setOnErrorListener(new MyOnErrorListener());
        mMediaPlayer.setOnBufferingUpdateListener(new MyOnBufferingUpdateListener());
        mMediaPlayer.setOnSeekCompleteListener(new MyOnSeekCompleteListener());
        mMediaPlayer.setOnCompletionListener(new MyOnCompletionListener());
        mMediaPlayer.setOnInfoListener(new MyOnInfoListener());
        mMediaPlayer.setOnVideoSizeChangedListener(new MyOnVideoSizeChangedListener());
    }

    private Handler mHandler = new Handler() {
        public void handleMessage(android.os.Message msg) {
            switch (msg.what) {
                case UPDATE_PROGRESS:
                    if (mMediaPlayer.isPlaying()) {
                        int currentPostion = mMediaPlayer.getCurrentPosition();
                        mListener.onPlaying(currentPostion);
                        if (currentPostion > mOldPostion) {
                            mListener.showOrHideProgressbar(false);
                        } else {
                            mListener.showOrHideProgressbar(true);
                        }
                        mOldPostion = currentPostion;
                        sendEmptyMessageDelayed(UPDATE_PROGRESS, 1000);
                    }
                    break;
            }
        }

        ;
    };

    public void start(final String path) {
        if (mMediaPlayer == null) {
            return;
        }
        if (mMediaPlayer.isPlaying()) {
            return;
        }

        new Thread() {
            @Override
            public void run() {
                try {
                    mMediaPlayer.reset();
                    Uri uri = Uri.parse(path);
                    mMediaPlayer.setDataSource(mContext, uri);
                    mMediaPlayer.setDisplay(mHolder);
                    mMediaPlayer.prepareAsync();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }.start();
    }

    public int getCurrentPosition() {
        if (mMediaPlayer == null) {
            return 0;
        }
        return mMediaPlayer.getCurrentPosition();
    }

    public int getDuration() {
        if (mMediaPlayer == null) {
            return 0;
        }
        return mMediaPlayer.getDuration();
    }

    public void release() {
        if (mMediaPlayer == null) {
            return;
        }
        isReleased = true;
        mMediaPlayer.stop();
        mMediaPlayer.release();
        mMediaPlayer = null;
        mHandler.removeMessages(UPDATE_PROGRESS);

    }

    public boolean isReleased() {
        return isReleased;
    }

    public void pause() {
        if (mMediaPlayer == null) {
            return;
        }
        if (mMediaPlayer.isPlaying()) {
            mMediaPlayer.pause();
        }
    }

    public boolean isPlaying() {
        if (mMediaPlayer == null) {
            return false;
        }
        return mMediaPlayer.isPlaying();
    }

    public void restart() {
        if (mMediaPlayer == null) {
            return;
        }
        if (!mMediaPlayer.isPlaying() && isPrepared) {
            mMediaPlayer.start();
            mHandler.sendEmptyMessage(UPDATE_PROGRESS);
        }
    }

    public void seekTo(int msec) {
        if (mMediaPlayer == null) {
            return;
        }
        mMediaPlayer.seekTo(msec);
    }

    public void stopUpdateProgress() {
        mHandler.removeMessages(UPDATE_PROGRESS);
    }

    public void setVideoPlayerListener(VideoPlayerListener mListener) {
        this.mListener = mListener;
    }

    public void setDisplay(SurfaceHolder holder) {
        this.mHolder = holder;
    }

    public interface VideoPlayerListener {
        void onPrepared();

        void onPlaying(int postion);

        void onCompletion();

        void onError();

        void onBuffering(int percent);

        void onBufferStart();

        void onBufferEnd();

        void onSeekComplete();

        void onVideoSizeChanged(int width, int heiht);

        void showOrHideProgressbar(boolean isShow);
    }

    private class MyOnPreparedListener implements OnPreparedListener {

        @Override
        public void onPrepared(MediaPlayer arg0) {
            mListener.onPrepared();
            mMediaPlayer.start();
            isPrepared = true;
            mHandler.sendEmptyMessage(UPDATE_PROGRESS);
        }
    }

    private class MyOnInfoListener implements OnInfoListener {

        @Override
        public boolean onInfo(MediaPlayer arg0, int arg1, int arg2) {
            switch (arg1) {
                case MediaPlayer.MEDIA_INFO_BUFFERING_START:
                    mListener.onBufferStart();
                    break;
                case MediaPlayer.MEDIA_INFO_BUFFERING_END:
                    mListener.onBufferEnd();
                    break;
                default:
                    break;
            }
            return true;
        }

    }

    private class MyOnErrorListener implements OnErrorListener {

        @Override
        public boolean onError(MediaPlayer arg0, int arg1, int arg2) {
            mListener.showOrHideProgressbar(false);
            mListener.onError();
            return false;
        }

    }

    private class MyOnBufferingUpdateListener implements OnBufferingUpdateListener {

        @Override
        public void onBufferingUpdate(MediaPlayer arg0, int percent) {
            mListener.onBuffering(percent);
        }
    }

    private class MyOnSeekCompleteListener implements OnSeekCompleteListener {

        @Override
        public void onSeekComplete(MediaPlayer arg0) {
            if (!isPlaying()) {
                return;
            }
            mMediaPlayer.start();
            mHandler.sendEmptyMessage(UPDATE_PROGRESS);
            mListener.onSeekComplete();
        }

    }

    private class MyOnCompletionListener implements OnCompletionListener {

        @Override
        public void onCompletion(MediaPlayer arg0) {
            mListener.onCompletion();
        }
    }

    private class MyOnVideoSizeChangedListener implements OnVideoSizeChangedListener {

        @Override
        public void onVideoSizeChanged(MediaPlayer mp, int width, int height) {
            mListener.onVideoSizeChanged(width, height);
            mListener.showOrHideProgressbar(false);
        }
    }
}
