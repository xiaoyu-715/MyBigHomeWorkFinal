package com.example.mybighomework.utils;

import android.media.MediaPlayer;
import android.util.Log;

import java.io.IOException;
import java.net.URLEncoder;

/**
 * 音频播放器
 * 负责播放单词发音
 */
public class AudioPlayerYSJ {
    
    private static final String TAG = "AudioPlayer";
    private MediaPlayer mediaPlayer;
    private boolean isPlaying = false;
    
    /**
     * 播放单词发音
     * @param word 单词
     * @param callback 回调接口
     */
    public void playWordPronunciation(String word, AudioCallback callback) {
        if (isPlaying) {
            if (callback != null) {
                callback.onError("正在播放中，请稍候");
            }
            return;
        }
        
        if (word == null || word.trim().isEmpty()) {
            if (callback != null) {
                callback.onError("没有可播放的单词");
            }
            return;
        }
        
        try {
            releaseMediaPlayer();
            mediaPlayer = new MediaPlayer();
            
            String encodedWord = URLEncoder.encode(word.trim(), "UTF-8");
            String url = "https://dict.youdao.com/dictvoice?audio=" + encodedWord + "&type=1";
            
            mediaPlayer.setDataSource(url);
            
            mediaPlayer.setOnPreparedListener(mp -> {
                isPlaying = true;
                if (callback != null) {
                    callback.onStart();
                }
                mp.start();
                Log.d(TAG, "开始播放单词发音: " + word);
            });
            
            mediaPlayer.setOnCompletionListener(mp -> {
                isPlaying = false;
                if (callback != null) {
                    callback.onComplete();
                }
                releaseMediaPlayer();
                Log.d(TAG, "播放完成: " + word);
            });
            
            mediaPlayer.setOnErrorListener((mp, what, extra) -> {
                isPlaying = false;
                if (callback != null) {
                    callback.onError("播放失败，请检查网络连接");
                }
                releaseMediaPlayer();
                Log.e(TAG, "播放失败: " + word + ", what=" + what + ", extra=" + extra);
                return true;
            });
            
            mediaPlayer.prepareAsync();
            
        } catch (IOException e) {
            isPlaying = false;
            if (callback != null) {
                callback.onError("播放失败: " + e.getMessage());
            }
            Log.e(TAG, "播放异常", e);
        }
    }
    
    /**
     * 释放MediaPlayer资源
     */
    public void releaseMediaPlayer() {
        if (mediaPlayer != null) {
            try {
                if (mediaPlayer.isPlaying()) {
                    mediaPlayer.stop();
                }
                mediaPlayer.release();
            } catch (Exception e) {
                Log.e(TAG, "释放MediaPlayer异常", e);
            }
            mediaPlayer = null;
        }
        isPlaying = false;
    }
    
    /**
     * 是否正在播放
     */
    public boolean isPlaying() {
        return isPlaying;
    }
    
    /**
     * 音频播放回调接口
     */
    public interface AudioCallback {
        void onStart();
        void onComplete();
        void onError(String message);
    }
}
