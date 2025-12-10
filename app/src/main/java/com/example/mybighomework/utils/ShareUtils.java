package com.example.mybighomework.utils;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Environment;
import android.text.TextPaint;
import android.widget.Toast;

import androidx.core.content.FileProvider;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * 分享工具类
 * 提供文字、图片、图文分享功能
 */
public class ShareUtils {
    
    private static final String TAG = "ShareUtils";
    
    /**
     * 分享纯文字
     */
    public static void shareText(Context context, String title, String content) {
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_SUBJECT, title);
        shareIntent.putExtra(Intent.EXTRA_TEXT, content);
        
        Intent chooser = Intent.createChooser(shareIntent, "分享到");
        if (shareIntent.resolveActivity(context.getPackageManager()) != null) {
            context.startActivity(chooser);
        } else {
            Toast.makeText(context, "没有找到可分享的应用", Toast.LENGTH_SHORT).show();
        }
    }
    
    /**
     * 分享图片
     */
    public static void shareImage(Context context, Uri imageUri, String title) {
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("image/*");
        shareIntent.putExtra(Intent.EXTRA_STREAM, imageUri);
        shareIntent.putExtra(Intent.EXTRA_SUBJECT, title);
        shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        
        Intent chooser = Intent.createChooser(shareIntent, "分享图片");
        if (shareIntent.resolveActivity(context.getPackageManager()) != null) {
            context.startActivity(chooser);
        } else {
            Toast.makeText(context, "没有找到可分享的应用", Toast.LENGTH_SHORT).show();
        }
    }
    
    /**
     * 分享文字和图片
     */
    public static void shareTextAndImage(Context context, String text, Uri imageUri) {
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("image/*");
        shareIntent.putExtra(Intent.EXTRA_TEXT, text);
        shareIntent.putExtra(Intent.EXTRA_STREAM, imageUri);
        shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        
        Intent chooser = Intent.createChooser(shareIntent, "分享");
        if (shareIntent.resolveActivity(context.getPackageManager()) != null) {
            context.startActivity(chooser);
        } else {
            Toast.makeText(context, "没有找到可分享的应用", Toast.LENGTH_SHORT).show();
        }
    }
    
    /**
     * 生成分享图片（带文字的精美卡片）
     */
    public static Bitmap generateShareImage(Context context, String englishText, String chineseText, 
                                           String author, String date) {
        // 图片尺寸
        int width = 1080;
        int height = 1920;
        
        // 创建Bitmap
        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        
        // 背景渐变色
        Paint bgPaint = new Paint();
        bgPaint.setStyle(Paint.Style.FILL);
        
        // 绘制渐变背景（从蓝色到紫色）
        android.graphics.LinearGradient gradient = new android.graphics.LinearGradient(
                0, 0, 0, height,
                new int[]{0xFF4A90E2, 0xFF9013FE},
                null,
                android.graphics.Shader.TileMode.CLAMP
        );
        bgPaint.setShader(gradient);
        canvas.drawRect(0, 0, width, height, bgPaint);
        
        // 绘制白色卡片
        Paint cardPaint = new Paint();
        cardPaint.setColor(Color.WHITE);
        cardPaint.setStyle(Paint.Style.FILL);
        cardPaint.setShadowLayer(20, 0, 10, 0x33000000);
        
        int cardMargin = 100;
        int cardTop = 400;
        int cardBottom = height - 600;
        canvas.drawRoundRect(cardMargin, cardTop, width - cardMargin, cardBottom, 40, 40, cardPaint);
        
        // 标题 "每日一句"
        TextPaint titlePaint = new TextPaint(Paint.ANTI_ALIAS_FLAG);
        titlePaint.setColor(Color.WHITE);
        titlePaint.setTextSize(80);
        titlePaint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
        titlePaint.setTextAlign(Paint.Align.CENTER);
        canvas.drawText("每日一句", width / 2f, 300, titlePaint);
        
        // 日期
        TextPaint datePaint = new TextPaint(Paint.ANTI_ALIAS_FLAG);
        datePaint.setColor(0xFFEEEEEE);
        datePaint.setTextSize(40);
        datePaint.setTextAlign(Paint.Align.CENTER);
        canvas.drawText(date, width / 2f, 360, datePaint);
        
        // 英文句子
        TextPaint englishPaint = new TextPaint(Paint.ANTI_ALIAS_FLAG);
        englishPaint.setColor(0xFF333333);
        englishPaint.setTextSize(60);
        englishPaint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
        
        // 分行绘制英文
        drawMultilineText(canvas, englishText, englishPaint, cardMargin + 80, cardTop + 150, 
                         width - cardMargin * 2 - 160, 80);
        
        // 中文翻译
        TextPaint chinesePaint = new TextPaint(Paint.ANTI_ALIAS_FLAG);
        chinesePaint.setColor(0xFF666666);
        chinesePaint.setTextSize(50);
        
        // 分行绘制中文
        drawMultilineText(canvas, chineseText, chinesePaint, cardMargin + 80, cardTop + 500, 
                         width - cardMargin * 2 - 160, 70);
        
        // 作者
        TextPaint authorPaint = new TextPaint(Paint.ANTI_ALIAS_FLAG);
        authorPaint.setColor(0xFF4A90E2);
        authorPaint.setTextSize(45);
        authorPaint.setTextAlign(Paint.Align.RIGHT);
        authorPaint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.ITALIC));
        canvas.drawText("—— " + author, width - cardMargin - 80, cardBottom - 80, authorPaint);
        
        // 底部水印
        TextPaint watermarkPaint = new TextPaint(Paint.ANTI_ALIAS_FLAG);
        watermarkPaint.setColor(0xFFEEEEEE);
        watermarkPaint.setTextSize(40);
        watermarkPaint.setTextAlign(Paint.Align.CENTER);
        canvas.drawText("来自 英语学习助手", width / 2f, height - 300, watermarkPaint);
        
        return bitmap;
    }
    
    /**
     * 绘制多行文字
     */
    private static void drawMultilineText(Canvas canvas, String text, TextPaint paint, 
                                         float x, float y, float maxWidth, float lineHeight) {
        String[] words = text.split(" ");
        StringBuilder line = new StringBuilder();
        float currentY = y;
        
        for (String word : words) {
            String testLine = line.length() == 0 ? word : line + " " + word;
            float testWidth = paint.measureText(testLine);
            
            if (testWidth > maxWidth && line.length() > 0) {
                canvas.drawText(line.toString(), x, currentY, paint);
                line = new StringBuilder(word);
                currentY += lineHeight;
            } else {
                line = new StringBuilder(testLine);
            }
        }
        
        if (line.length() > 0) {
            canvas.drawText(line.toString(), x, currentY, paint);
        }
    }
    
    /**
     * 保存Bitmap到文件
     */
    public static Uri saveBitmapToFile(Context context, Bitmap bitmap, String fileName) {
        try {
            // 使用应用私有目录
            File cacheDir = new File(context.getCacheDir(), "share");
            if (!cacheDir.exists()) {
                cacheDir.mkdirs();
            }
            
            File imageFile = new File(cacheDir, fileName);
            FileOutputStream fos = new FileOutputStream(imageFile);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, fos);
            fos.flush();
            fos.close();
            
            // 使用FileProvider获取Uri
            return FileProvider.getUriForFile(
                    context,
                    context.getPackageName() + ".fileprovider",
                    imageFile
            );
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(context, "保存图片失败", Toast.LENGTH_SHORT).show();
            return null;
        }
    }
    
    /**
     * 清理分享缓存
     */
    public static void clearShareCache(Context context) {
        File cacheDir = new File(context.getCacheDir(), "share");
        if (cacheDir.exists()) {
            File[] files = cacheDir.listFiles();
            if (files != null) {
                for (File file : files) {
                    file.delete();
                }
            }
        }
    }
}

