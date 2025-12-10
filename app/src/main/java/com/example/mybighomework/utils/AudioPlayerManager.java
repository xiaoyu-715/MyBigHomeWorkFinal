package com.example.mybighomework.utils;

import android.media.AudioAttributes;
import android.media.MediaPlayer;
import android.util.Log;

import java.io.IOException;

/**
 * 音频播放管理器
 * 负责管理MediaPlayer的生命周期和播放状态
 */
public class AudioPlayerManager {
    
    private static final String TAG = "AudioPlayerManager";
    
    private MediaPlayer mediaPlayer;
    private String currentUrl;
    private PlaybackStateListener stateListener;
    private boolean isPreparing = false;
    
    /**
     * 播放状态监听器
     */
    public interface PlaybackStateListener {
        void onPlaying();
        void onPaused();
        void onStopped();
        void onLoading();
        void onError(String error);
        void onCompletion();
        void onProgress(int currentPosition, int duration);
    }
    
    /**
     * 设置播放状态监听器
     */
    public void setPlaybackStateListener(PlaybackStateListener listener) {
        this.stateListener = listener;
    }
    
    /**
     * 播放音频
     * @param url 音频URL
     */
    public void play(String url) {
        if (url == null || url.isEmpty()) {
            notifyError("音频地址为空");
            return;
        }
        
        // 如果正在播放相同的URL，则暂停
        if (isPlaying() && url.equals(currentUrl)) {
            pause();
            return;
        }
        
        // 如果暂停状态且是相同URL，则继续播放
        if (mediaPlayer != null && url.equals(currentUrl) && !isPlaying()) {
            resume();
            return;
        }
        
        // 停止当前播放
        stop();
        
        // 准备新的播放
        currentUrl = url;
        isPreparing = true;
        notifyLoading();
        
        try {
            mediaPlayer = new MediaPlayer();
            
            // 设置音频属性
            AudioAttributes audioAttributes = new AudioAttributes.Builder()
                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                    .setUsage(AudioAttributes.USAGE_MEDIA)
                    .build();
            mediaPlayer.setAudioAttributes(audioAttributes);
            
            // 设置数据源
            mediaPlayer.setDataSource(url);
            
            // 准备完成监听
            mediaPlayer.setOnPreparedListener(mp -> {
                isPreparing = false;
                Log.d(TAG, "音频准备完成，开始播放");
                mp.start();
                notifyPlaying();
                startProgressUpdate();
            });
            
            // 播放完成监听
            mediaPlayer.setOnCompletionListener(mp -> {
                Log.d(TAG, "音频播放完成");
                notifyCompletion();
                stop();
            });
            
            // 错误监听
            mediaPlayer.setOnErrorListener((mp, what, extra) -> {
                isPreparing = false;
                String errorMsg = "播放错误: what=" + what + ", extra=" + extra;
                Log.e(TAG, errorMsg);
                notifyError("播放失败，请检查网络");
                stop();
                return true;
            });
            
            // 异步准备
            mediaPlayer.prepareAsync();
            
            Log.d(TAG, "开始准备音频: " + url);
            
        } catch (IOException e) {
            isPreparing = false;
            Log.e(TAG, "设置音频源失败: " + e.getMessage());
            notifyError("加载音频失败");
            stop();
        } catch (Exception e) {
            isPreparing = false;
            Log.e(TAG, "播放异常: " + e.getMessage());
            notifyError("播放异常");
            stop();
        }
    }
    
    /**
     * 暂停播放
     */
    public void pause() {
        if (mediaPlayer != null && mediaPlayer.isPlaying()) {
            mediaPlayer.pause();
            Log.d(TAG, "暂停播放");
            notifyPaused();
        }
    }
    
    /**
     * 恢复播放
     */
    public void resume() {
        if (mediaPlayer != null && !mediaPlayer.isPlaying()) {
            mediaPlayer.start();
            Log.d(TAG, "恢复播放");
            notifyPlaying();
            startProgressUpdate();
        }
    }
    
    /**
     * 停止播放
     */
    public void stop() {
        if (mediaPlayer != null) {
            try {
                if (mediaPlayer.isPlaying()) {
                    mediaPlayer.stop();
                }
                mediaPlayer.reset();
                mediaPlayer.release();
                Log.d(TAG, "停止播放");
            } catch (Exception e) {
                Log.e(TAG, "停止播放异常: " + e.getMessage());
            } finally {
                mediaPlayer = null;
                currentUrl = null;
                isPreparing = false;
                notifyStopped();
            }
        }
    }
    
    /**
     * 跳转到指定位置
     * @param position 位置（毫秒）
     */
    public void seekTo(int position) {
        if (mediaPlayer != null) {
            try {
                mediaPlayer.seekTo(position);
                Log.d(TAG, "跳转到: " + position + "ms");
            } catch (Exception e) {
                Log.e(TAG, "跳转失败: " + e.getMessage());
            }
        }
    }
    
    /**
     * 是否正在播放
     */
    public boolean isPlaying() {
        return mediaPlayer != null && mediaPlayer.isPlaying();
    }
    
    /**
     * 是否正在准备
     */
    public boolean isPreparing() {
        return isPreparing;
    }
    
    /**
     * 获取当前播放位置
     */
    public int getCurrentPosition() {
        if (mediaPlayer != null) {
            try {
                return mediaPlayer.getCurrentPosition();
            } catch (Exception e) {
                return 0;
            }
        }
        return 0;
    }
    
    /**
     * 获取音频总时长
     */
    public int getDuration() {
        if (mediaPlayer != null) {
            try {
                return mediaPlayer.getDuration();
            } catch (Exception e) {
                return 0;
            }
        }
        return 0;
    }
    
    /**
     * 开始进度更新
     */
    private void startProgressUpdate() {
        new Thread(() -> {
            while (mediaPlayer != null && mediaPlayer.isPlaying()) {
                try {
                    int currentPosition = getCurrentPosition();
                    int duration = getDuration();
                    notifyProgress(currentPosition, duration);
                    Thread.sleep(500); // 每500ms更新一次
                } catch (Exception e) {
                    break;
                }
            }
        }).start();
    }
    
    // 通知方法
    private void notifyPlaying() {
        if (stateListener != null) {
            stateListener.onPlaying();
        }
    }
    
    private void notifyPaused() {
        if (stateListener != null) {
            stateListener.onPaused();
        }
    }
    
    private void notifyStopped() {
        if (stateListener != null) {
            stateListener.onStopped();
        }
    }
    
    private void notifyLoading() {
        if (stateListener != null) {
            stateListener.onLoading();
        }
    }
    
    private void notifyError(String error) {
        if (stateListener != null) {
            stateListener.onError(error);
        }
    }
    
    private void notifyCompletion() {
        if (stateListener != null) {
            stateListener.onCompletion();
        }
    }
    
    private void notifyProgress(int currentPosition, int duration) {
        if (stateListener != null) {
            stateListener.onProgress(currentPosition, duration);
        }
    }
    
    /**
     * 释放资源
     */
    public void release() {
        stop();
        stateListener = null;
        Log.d(TAG, "释放资源");
    }
}

