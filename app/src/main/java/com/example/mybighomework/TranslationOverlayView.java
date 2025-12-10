package com.example.mybighomework;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.Nullable;

import com.google.mlkit.vision.text.Text;

import java.util.ArrayList;
import java.util.List;

/**
 * 自定义View，用于显示拍照图片并在文字位置叠加翻译结果
 * 图片自适应屏幕大小完整显示，支持缩放功能
 */
public class TranslationOverlayView extends View {

    private static final float MIN_ZOOM = 0.5f; // 最小缩放比例
    private static final float MAX_ZOOM = 3.0f; // 最大缩放比例

    private Bitmap originalBitmap;
    private Bitmap scaledBitmap;
    private float scaleFactor = 1.0f;  // 用户手势缩放比例
    private float fitToScreenScale = 1.0f;  // 适应屏幕的缩放比例
    // 注意：textPaint、backgroundPaint和borderPaint已被移除，因为合成图片已经包含翻译文字

    // 缩放相关
    private Matrix scaleMatrix;
    private ScaleGestureDetector scaleGestureDetector;
    private float currentScale = 1.0f;  // 用户手势缩放比例
    private float lastScale = 1.0f;

    // 滚动相关
    private float scrollX = 0f;  // 水平滚动偏移
    private float scrollY = 0f;  // 垂直滚动偏移
    private float lastTouchX;    // 上次触摸X坐标
    private float lastTouchY;    // 上次触摸Y坐标
    private boolean isScrolling = false;  // 是否正在滚动

    // 注意：textBlocks已被移除，因为合成图片已经包含翻译文字

    // 注意：缩放监听器已被移除，因为合成图片已经包含翻译文字

    public TranslationOverlayView(Context context) {
        super(context);
        init();
    }

    public TranslationOverlayView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public TranslationOverlayView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        // 初始化缩放相关
        scaleMatrix = new Matrix();
        scaleGestureDetector = new ScaleGestureDetector(getContext(), new ScaleListener());
    }

    /**
     * 设置要显示的图片
     */
    public void setImageBitmap(Bitmap bitmap) {
        // 释放之前的图片资源
        if (originalBitmap != null && originalBitmap != bitmap && !originalBitmap.isRecycled()) {
            originalBitmap.recycle();
        }
        if (scaledBitmap != null && scaledBitmap != bitmap && !scaledBitmap.isRecycled()) {
            scaledBitmap.recycle();
        }

        this.originalBitmap = bitmap;

        // 计算适应屏幕的缩放比例，保持原图宽高比
        calculateFitToScreenScale();

        // 创建适应屏幕大小的缩放图片
        createScaledBitmap();

        // 重置用户缩放比例为1.0，表示初始状态下就是适应屏幕的显示
        currentScale = 1.0f;
        lastScale = 1.0f;

        // 重置滚动位置
        scrollX = 0f;
        scrollY = 0f;

        invalidate();
    }

    // 注意：updateTextSize方法已被移除，因为合成图片已经包含翻译文字

    /**
     * 设置翻译结果（旧方法，保留向后兼容）
     */
    public void setTranslationResult(Text textResult, String translatedText) {
        if (originalBitmap == null || textResult == null) {
            return;
        }

        // 生成合成图片
        generateCompositeImage(textResult, translatedText);
    }

    /**
     * 生成带有翻译文字的合成图片
     */
    public void generateCompositeImage(Text textResult, String translatedText) {
        if (originalBitmap == null || textResult == null) {
            return;
        }

        Bitmap compositeBitmap = null;
        try {
            // 确保原始位图是软件渲染位图
            Bitmap sourceBitmap = originalBitmap.isMutable() ? originalBitmap : originalBitmap.copy(Bitmap.Config.ARGB_8888, true);

            // 检查图片尺寸，避免创建过大的Bitmap
            int width = sourceBitmap.getWidth();
            int height = sourceBitmap.getHeight();

            // 如果图片过大，进行压缩
            if (width * height > 4096 * 4096) { // 限制最大尺寸为4096x4096
                float scale = (float) Math.sqrt(4096.0 * 4096.0 / (width * height));
                width = (int) (width * scale);
                height = (int) (height * scale);

                // 创建缩放后的原图
                Bitmap scaledOriginal = Bitmap.createScaledBitmap(sourceBitmap, width, height, true);
                if (scaledOriginal != sourceBitmap) {
                    sourceBitmap.recycle();
                    sourceBitmap = scaledOriginal;
                }
            }

            // 创建一个新的Bitmap，大小和原图一样
            compositeBitmap = Bitmap.createBitmap(
                width,
                height,
                Bitmap.Config.ARGB_8888 // 使用ARGB_8888确保透明度支持
            );

            // 创建Canvas用于绘制
            Canvas canvas = new Canvas(compositeBitmap);

            // 绘制原图
            canvas.drawBitmap(sourceBitmap, 0, 0, null);

            // 解析OCR结果，按文字块分组绘制翻译文字
            List<Text.TextBlock> blocks = textResult.getTextBlocks();
            String[] translatedLines = translatedText.split("\n");

            // 初始化文字画笔
            Paint textPaint = new Paint();
            textPaint.setColor(Color.WHITE);
            textPaint.setAntiAlias(true);
            textPaint.setFakeBoldText(true);
            textPaint.setShadowLayer(3f, 2f, 2f, Color.BLACK);

            // 基础文字大小将在每个文字块中根据框的大小动态计算
            // 这里设置一个初始值
            float baseTextSize = Math.max(36f, Math.min(80f, width / 25f));
            textPaint.setTextSize(baseTextSize);

            int translationIndex = 0;
            for (Text.TextBlock block : blocks) {
                if (translationIndex >= translatedLines.length) {
                    break;
                }

                // 获取文字块的边界框，并按比例缩放
                Rect boundingBox = block.getBoundingBox();
                if (boundingBox != null) {
                    // 如果图片被压缩，需要调整边界框坐标
                    if (width != sourceBitmap.getWidth()) {
                        float scaleX = (float) width / sourceBitmap.getWidth();
                        float scaleY = (float) height / sourceBitmap.getHeight();
                        boundingBox = new Rect(
                            (int) (boundingBox.left * scaleX),
                            (int) (boundingBox.top * scaleY),
                            (int) (boundingBox.right * scaleX),
                            (int) (boundingBox.bottom * scaleY)
                        );
                    }

                    String translation = translatedLines[translationIndex];

                    // 绘制半透明黑色背景
                    Paint bgPaint = new Paint();
                    bgPaint.setColor(Color.argb(180, 0, 0, 0));
                    bgPaint.setStyle(Paint.Style.FILL);
                    bgPaint.setAntiAlias(true);
                    canvas.drawRect(boundingBox, bgPaint);

                    // 动态计算适合该矩形框的文字大小
                    float adaptiveTextSize = calculateAdaptiveTextSize(boundingBox, translation, textPaint);
                    textPaint.setTextSize(adaptiveTextSize);

                    // 绘制翻译文字
                    drawTextInRect(canvas, translation, boundingBox, textPaint);

                    translationIndex++;
                }
            }

            // 设置为合成图片
            if (originalBitmap != null && originalBitmap != compositeBitmap && !originalBitmap.isRecycled()) {
                originalBitmap.recycle();
            }
            originalBitmap = compositeBitmap;

            // 重新计算适应屏幕的缩放比例
            calculateFitToScreenScale();

            // 创建适应屏幕大小的缩放图片
            createScaledBitmap();

            // 刷新显示
            invalidate();

        } catch (OutOfMemoryError e) {
            Log.e("TranslationOverlayView", "内存不足，无法生成合成图片", e);
            // 在内存不足时，至少显示原图
            if (compositeBitmap != null && !compositeBitmap.isRecycled()) {
                compositeBitmap.recycle();
            }
            // 不调用setImageBitmap，保持原图显示
            // 在UI线程中显示提示
            post(() -> {
                Toast.makeText(getContext(), "图片过大，显示原图", Toast.LENGTH_SHORT).show();
            });
        } catch (Exception e) {
            Log.e("TranslationOverlayView", "生成合成图片失败", e);
            if (compositeBitmap != null && !compositeBitmap.isRecycled()) {
                compositeBitmap.recycle();
            }
        }
    }

    /**
     * 计算适合矩形框的自适应文字大小
     */
    private float calculateAdaptiveTextSize(Rect rect, String text, Paint paint) {
        // 根据矩形框高度计算初始文字大小
        float minTextSize = 24f;
        float maxTextSize = 120f;
        float initialSize = Math.min(maxTextSize, Math.max(minTextSize, rect.height() / 3f));
        
        paint.setTextSize(initialSize);
        
        // 测试是否需要多行显示
        float availableWidth = rect.width() - 24f; // 左右各留12dp边距
        List<String> lines = wrapText(text, availableWidth, paint);
        
        Paint.FontMetrics fontMetrics = paint.getFontMetrics();
        float lineHeight = fontMetrics.descent - fontMetrics.ascent + 4f;
        float totalHeight = lines.size() * lineHeight;
        
        // 如果总高度超过矩形高度，按比例缩小文字
        if (totalHeight > rect.height() - 16f) {
            float scale = (rect.height() - 16f) / totalHeight;
            initialSize = Math.max(minTextSize, initialSize * scale * 0.95f);
        }
        
        return initialSize;
    }

    /**
     * 在指定矩形区域内绘制文字（自动换行和居中）
     */
    private void drawTextInRect(Canvas canvas, String text, Rect rect, Paint paint) {
        Paint.FontMetrics fontMetrics = paint.getFontMetrics();
        float textHeight = fontMetrics.descent - fontMetrics.ascent;

        // 处理长文本换行，左右各留12dp边距
        float availableWidth = rect.width() - 24f;
        List<String> lines = wrapText(text, availableWidth, paint);

        // 行间距
        float lineSpacing = 4f;
        float lineHeight = textHeight + lineSpacing;
        float totalTextHeight = lines.size() * lineHeight - lineSpacing;

        // 绘制每一行文字
        for (int i = 0; i < lines.size(); i++) {
            String line = lines.get(i);
            float textWidth = paint.measureText(line);

            // 计算文字位置（水平居中，垂直居中）
            float x = rect.left + (rect.width() - textWidth) / 2f;

            float y;
            if (lines.size() == 1) {
                // 单行文字垂直居中
                y = rect.top + rect.height() / 2f + textHeight / 2f - fontMetrics.descent;
            } else {
                // 多行文字垂直居中显示
                float startY = rect.top + (rect.height() - totalTextHeight) / 2f + textHeight - fontMetrics.descent;
                y = startY + i * lineHeight;
            }

            canvas.drawText(line, x, y, paint);
        }
    }

    // 注意：文字位置现在跟随图片缩放，不再固定

    /**
     * 将图片坐标转换为View坐标（简化版 - 用于合成图片）
     * 因为合成图片已经包含了翻译文字，缩放逻辑像普通图片一样
     */
    private RectF convertRectToView(Rect imageRect) {
        if (originalBitmap == null || getWidth() == 0 || getHeight() == 0) {
            return new RectF(imageRect);
        }

        // 使用当前总缩放比例（适应屏幕比例 × 用户手势缩放比例）
        float currentTotalScale = fitToScreenScale * currentScale;

        // 计算缩放后的坐标
        float left = imageRect.left * currentTotalScale;
        float top = imageRect.top * currentTotalScale;
        float right = imageRect.right * currentTotalScale;
        float bottom = imageRect.bottom * currentTotalScale;

        // 计算基于当前总缩放比例的目标图片尺寸
        float targetWidth = originalBitmap.getWidth() * currentTotalScale;
        float targetHeight = originalBitmap.getHeight() * currentTotalScale;

        // 计算居中偏移（基于当前尺寸）
        float offsetX = (getWidth() - targetWidth) / 2f;
        float offsetY = (getHeight() - targetHeight) / 2f;

        // 应用居中偏移
        left += offsetX;
        top += offsetY;
        right += offsetX;
        bottom += offsetY;

        return new RectF(left, top, right, bottom);
    }

    /**
     * 计算图片适应屏幕的缩放比例，保持宽高比
     */
    private void calculateFitToScreenScale() {
        if (originalBitmap == null) {
            fitToScreenScale = 1.0f;
            return;
        }

        // 如果View尺寸还没有确定，设置为1.0f，稍后会在onSizeChanged中重新计算
        if (getWidth() == 0 || getHeight() == 0) {
            fitToScreenScale = 1.0f;
            return;
        }

        // 计算适应屏幕的缩放比例，同时保持图片宽高比
        float scaleX = getWidth() / (float) originalBitmap.getWidth();
        float scaleY = getHeight() / (float) originalBitmap.getHeight();

        // 选择较小的缩放比例，确保图片完全显示在屏幕内
        fitToScreenScale = Math.min(scaleX, scaleY);
    }

    /**
     * 创建适应屏幕大小的缩放图片（考虑用户手势缩放）
     */
    private void createScaledBitmap() {
        if (originalBitmap == null) {
            scaledBitmap = null;
            return;
        }

        // 计算最终缩放比例：适应屏幕比例 × 用户手势缩放比例
        float finalScale = fitToScreenScale * currentScale;

        if (finalScale <= 0) {
            scaledBitmap = originalBitmap;
            return;
        }

        try {
            // 计算缩放后的尺寸
            int scaledWidth = Math.max(1, (int) (originalBitmap.getWidth() * finalScale));
            int scaledHeight = Math.max(1, (int) (originalBitmap.getHeight() * finalScale));

            scaledBitmap = Bitmap.createScaledBitmap(
                    originalBitmap,
                    scaledWidth,
                    scaledHeight,
                    true);
        } catch (Exception e) {
            Log.e("TranslationOverlayView", "缩放图片失败", e);
            // 如果缩放失败，使用原始图片
            scaledBitmap = originalBitmap;
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        // View尺寸由父布局决定，match_parent填满整个可用空间
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        // 尺寸改变时重新计算缩放比例和创建缩放图片
        if (originalBitmap != null) {
            calculateFitToScreenScale();
            createScaledBitmap();
            invalidate();
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        // 绘制缩放后的图片，完全居中显示
        if (scaledBitmap != null && !scaledBitmap.isRecycled()) {
            // 计算居中位置（自由滚动，不限制边界）
            float offsetX = (getWidth() - scaledBitmap.getWidth()) / 2f;
            float offsetY = (getHeight() - scaledBitmap.getHeight()) / 2f;

            // 应用滚动偏移
        float finalOffsetX = offsetX + scrollX;
        float finalOffsetY = offsetY + scrollY;

        // 使用高质量的绘制方式
        Paint paint = new Paint();
        paint.setAntiAlias(true);
        paint.setFilterBitmap(true);
        paint.setDither(true);

        canvas.drawBitmap(scaledBitmap, finalOffsetX, finalOffsetY, paint);
        }

        // 注意：合成图片已经包含翻译文字，不需要额外绘制
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        // 处理缩放手势
        scaleGestureDetector.onTouchEvent(event);

        // 处理滚动手势
        handleScrollGesture(event);

        return true;
    }

    /**
     * 处理滚动手势
     */
    private void handleScrollGesture(MotionEvent event) {
        if (scaledBitmap == null) return;

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                // 记录初始触摸位置
                lastTouchX = event.getX();
                lastTouchY = event.getY();
                isScrolling = false;
                break;

            case MotionEvent.ACTION_MOVE:
                if (event.getPointerCount() == 1 && !scaleGestureDetector.isInProgress()) {
                    // 单指拖拽滚动
                    float deltaX = event.getX() - lastTouchX;
                    float deltaY = event.getY() - lastTouchY;

                    // 只有当移动距离足够大时才认为是滚动
                    if (Math.abs(deltaX) > 10 || Math.abs(deltaY) > 10) {
                        isScrolling = true;

                        // 更新滚动位置（自由滚动，无边界限制）
                        scrollX += deltaX;
                        scrollY += deltaY;

                        // 更新触摸位置
                        lastTouchX = event.getX();
                        lastTouchY = event.getY();

                        invalidate();
                    }
                }
                break;

            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                isScrolling = false;
                break;
        }
    }

    // 注意：边界约束方法已被移除，现在支持自由滚动

    /**
     * 绘制翻译文字块（简化版 - 用于合成图片）
     * 因为合成图片已经包含了翻译文字，这里不需要额外绘制
     */
    private void drawTranslationBlock(Canvas canvas, Object block) {
        // 注意：合成图片已经包含了翻译文字，这里不需要额外绘制
        // 这是为了保持向后兼容而保留的方法，参数类型改为Object避免编译错误
    }

    /**
     * 将文本按宽度换行（支持中英文混合）
     */
    private List<String> wrapText(String text, float maxWidth, Paint paint) {
        List<String> lines = new ArrayList<>();
        
        if (text == null || text.isEmpty()) {
            return lines;
        }
        
        // 先尝试按空格分词（适合英文）
        String[] words = text.split("\\s+");
        
        // 如果只有一个词或没有空格，按字符逐个检查
        if (words.length <= 1) {
            return wrapTextByCharacter(text, maxWidth, paint);
        }
        
        // 有多个单词，按词换行
        StringBuilder currentLine = new StringBuilder();
        
        for (String word : words) {
            String testLine = currentLine.length() == 0 ? word : currentLine + " " + word;
            float testWidth = paint.measureText(testLine);
            
            if (testWidth <= maxWidth) {
                currentLine = new StringBuilder(testLine);
            } else {
                // 当前词加不下
                if (currentLine.length() > 0) {
                    // 保存当前行
                    lines.add(currentLine.toString());
                    currentLine = new StringBuilder();
                }
                
                // 检查单个词是否太长
                if (paint.measureText(word) > maxWidth) {
                    // 单词太长，按字符拆分
                    lines.addAll(wrapTextByCharacter(word, maxWidth, paint));
                } else {
                    currentLine = new StringBuilder(word);
                }
            }
        }

        if (currentLine.length() > 0) {
            lines.add(currentLine.toString());
        }

        return lines;
    }
    
    /**
     * 按字符逐个检查宽度进行换行（用于中文或长单词）
     */
    private List<String> wrapTextByCharacter(String text, float maxWidth, Paint paint) {
        List<String> lines = new ArrayList<>();
        StringBuilder currentLine = new StringBuilder();
        
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            String testLine = currentLine.toString() + c;
            float testWidth = paint.measureText(testLine);
            
            if (testWidth <= maxWidth) {
                currentLine.append(c);
            } else {
                if (currentLine.length() > 0) {
                    lines.add(currentLine.toString());
                    currentLine = new StringBuilder();
                }
                currentLine.append(c);
            }
        }
        
        if (currentLine.length() > 0) {
            lines.add(currentLine.toString());
        }
        
        return lines;
    }

    // 注意：clearTranslations方法已被移除，因为合成图片已经包含翻译文字

    /**
     * 缩放手势监听器
     */
    private class ScaleListener extends ScaleGestureDetector.SimpleOnScaleGestureListener {
        @Override
        public boolean onScale(ScaleGestureDetector detector) {
            float scaleFactor = detector.getScaleFactor();

            // 计算新的用户缩放比例（基于适应屏幕的缩放比例）
            float newScale = currentScale * scaleFactor;

            // 限制缩放范围
            newScale = Math.max(MIN_ZOOM, Math.min(newScale, MAX_ZOOM));

            // 更新当前缩放比例
            currentScale = newScale;

            // 重新创建缩放图片（应用用户缩放）
            createScaledBitmap();

            // 注意：合成图片已经包含翻译文字，不需要重新设置翻译结果

            // 重新绘制
            invalidate();

            return true;
        }

        @Override
        public boolean onScaleBegin(ScaleGestureDetector detector) {
            lastScale = currentScale;
            return true;
        }

        @Override
        public void onScaleEnd(ScaleGestureDetector detector) {
            // 缩放结束后进行一些优化处理
            // 确保缩放比例精确，避免浮点数精度问题
            float scale = currentScale;
            if (Math.abs(scale - 1.0f) < 0.01f) {
                currentScale = 1.0f;
                createScaledBitmap();
                // 注意：不再更新文字大小，保持固定
                invalidate();
            }
        }
    }

    /**
     * 获取当前缩放比例
     */
    public float getCurrentScale() {
        return currentScale;
    }

    // 注意：setOnScaleListener方法已被移除，因为合成图片已经包含翻译文字

    /**
     * 重置缩放比例
     */
    public void resetZoom() {
        currentScale = 1.0f;
        lastScale = 1.0f;
        // 重置滚动位置
        scrollX = 0f;
        scrollY = 0f;
        // 重新创建缩放图片（回到适应屏幕的状态）
        createScaledBitmap();
        // 注意：不再更新文字大小，保持固定
        invalidate();
    }

    /**
     * 清理所有图片资源
     */
    public void clearResources() {
        if (originalBitmap != null && !originalBitmap.isRecycled()) {
            originalBitmap.recycle();
            originalBitmap = null;
        }
        if (scaledBitmap != null && !scaledBitmap.isRecycled()) {
            scaledBitmap.recycle();
            scaledBitmap = null;
        }
    }

    // 注意：TextBlockInfo类已被移除，因为合成图片已经包含翻译文字
}
