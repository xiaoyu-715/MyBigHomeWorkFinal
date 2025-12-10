package com.example.mybighomework.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.graphics.Shader;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.Nullable;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * 自定义学习时长图表视图
 * 显示最近7天的学习时长曲线图
 * 
 * 优化特性：
 * - 平滑贝塞尔曲线
 * - Y轴时长标签
 * - 渐变填充效果
 * - 优化的网格线样式
 * - 数据点值标签
 * - 改进的空数据处理
 */
public class StudyChartView extends View {
    
    private Paint linePaint;           // 曲线画笔
    private Paint pointPaint;          // 数据点画笔
    private Paint textPaint;           // 文本画笔
    private Paint gridPaint;           // 网格线画笔
    private Paint fillPaint;           // 填充区域画笔
    private Paint currentDayPaint;     // 当日标记画笔
    private Paint yAxisPaint;          // Y轴标签画笔
    private Paint shadowPaint;         // 阴影画笔
    private Paint valueTextPaint;      // 数值标签画笔
    
    private List<Float> dataPoints;    // 学习时长数据点（秒）
    private List<String> dateLabels;   // 日期标签
    private float maxValue = 0;        // 最大值
    private float minValue = 0;        // 最小值
    
    private float chartWidth;
    private float chartHeight;
    private float paddingLeft = 80;    // 增加左边距以显示Y轴标签
    private float paddingRight = 40;
    private float paddingTop = 60;     // 增加上边距以显示数值标签
    private float paddingBottom = 70;
    
    // 颜色配置
    private int lineColor = Color.parseColor("#FF9A6C");        // 橙色曲线
    private int pointColor = Color.parseColor("#FF9A6C");       // 橙色数据点
    private int fillColorStart = Color.parseColor("#66FF9A6C"); // 渐变起始色（更鲜艳）
    private int fillColorEnd = Color.parseColor("#00FF9A6C");   // 渐变结束色（透明）
    private int textColor = Color.parseColor("#666666");        // 深灰色文本
    private int gridColor = Color.parseColor("#E8E8E8");        // 更浅的网格线
    private int currentDayColor = Color.parseColor("#FF9A6C");  // 当日标记颜色
    private int yAxisTextColor = Color.parseColor("#999999");   // Y轴文本颜色
    private int valueTextColor = Color.parseColor("#FF9A6C");   // 数值标签颜色
    
    public StudyChartView(Context context) {
        super(context);
        init();
    }
    
    public StudyChartView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }
    
    public StudyChartView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }
    
    private void init() {
        // 初始化画笔
        linePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        linePaint.setColor(lineColor);
        linePaint.setStrokeWidth(8f);  // 加粗曲线
        linePaint.setStyle(Paint.Style.STROKE);
        linePaint.setStrokeCap(Paint.Cap.ROUND);
        linePaint.setStrokeJoin(Paint.Join.ROUND);
        
        pointPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        pointPaint.setColor(pointColor);
        pointPaint.setStyle(Paint.Style.FILL);
        
        textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        textPaint.setColor(textColor);
        textPaint.setTextSize(26f);
        textPaint.setTextAlign(Paint.Align.CENTER);
        
        // 虚线网格效果
        gridPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        gridPaint.setColor(gridColor);
        gridPaint.setStrokeWidth(1.5f);
        gridPaint.setStyle(Paint.Style.STROKE);
        gridPaint.setPathEffect(new DashPathEffect(new float[]{10, 5}, 0));
        
        fillPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        fillPaint.setStyle(Paint.Style.FILL);
        // 渐变将在onSizeChanged中设置
        
        currentDayPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        currentDayPaint.setColor(currentDayColor);
        currentDayPaint.setStyle(Paint.Style.FILL);
        
        // Y轴标签画笔
        yAxisPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        yAxisPaint.setColor(yAxisTextColor);
        yAxisPaint.setTextSize(24f);
        yAxisPaint.setTextAlign(Paint.Align.RIGHT);
        
        // 阴影画笔
        shadowPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        shadowPaint.setColor(Color.parseColor("#20000000"));
        shadowPaint.setStyle(Paint.Style.FILL);
        
        // 数值标签画笔
        valueTextPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        valueTextPaint.setColor(valueTextColor);
        valueTextPaint.setTextSize(22f);
        valueTextPaint.setTextAlign(Paint.Align.CENTER);
        valueTextPaint.setFakeBoldText(true);
        
        // 初始化默认数据
        dataPoints = new ArrayList<>();
        dateLabels = new ArrayList<>();
        initDefaultData();
    }
    
    /**
     * 初始化默认数据（最近7天，全为0）
     */
    private void initDefaultData() {
        SimpleDateFormat sdf = new SimpleDateFormat("MM/dd", Locale.getDefault());
        Calendar calendar = Calendar.getInstance();
        
        dataPoints.clear();
        dateLabels.clear();
        
        // 生成最近7天的日期
        for (int i = 6; i >= 0; i--) {
            calendar.setTimeInMillis(System.currentTimeMillis() - (i * 24L * 60 * 60 * 1000));
            dateLabels.add(sdf.format(calendar.getTime()));
            dataPoints.add(0f);
        }
        
        // 添加"今日"标签
        dateLabels.set(6, "今日");
    }
    
    /**
     * 设置图表数据
     * @param dailyData Map<日期, 学习时长（秒）>
     */
    public void setData(Map<String, Float> dailyData) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        Calendar calendar = Calendar.getInstance();
        
        dataPoints.clear();
        dateLabels.clear();
        maxValue = 0;
        minValue = 0;
        
        // 生成最近7天的日期，并匹配数据
        for (int i = 6; i >= 0; i--) {
            calendar.setTimeInMillis(System.currentTimeMillis() - (i * 24L * 60 * 60 * 1000));
            String date = sdf.format(calendar.getTime());
            
            // 获取对应日期的学习时长
            float studyTime = dailyData.getOrDefault(date, 0f);
            dataPoints.add(studyTime);
            
            // 更新最大值
            if (studyTime > maxValue) {
                maxValue = studyTime;
            }
            
            // 添加日期标签（格式：MM/dd）
            SimpleDateFormat labelFormat = new SimpleDateFormat("MM/dd", Locale.getDefault());
            if (i == 0) {
                dateLabels.add("今日");
            } else {
                dateLabels.add(labelFormat.format(calendar.getTime()));
            }
        }
        
        // 改进边界情况处理
        if (maxValue == 0) {
            // 所有数据都是0，设置默认最大值
            maxValue = 300; // 5分钟
        } else {
            // 在最大值基础上增加20%的空间，使图表更美观
            maxValue = maxValue * 1.2f;
            
            // 确保最大值至少为60秒（1分钟）
            if (maxValue < 60) {
                maxValue = 60;
            }
            
            // 将最大值向上取整到合适的数值
            maxValue = roundUpToNice(maxValue);
        }
        
        invalidate(); // 重绘
    }
    
    /**
     * 将数值向上取整到合适的显示数值
     */
    private float roundUpToNice(float value) {
        if (value <= 60) return 60;           // 1分钟
        if (value <= 120) return 120;         // 2分钟
        if (value <= 300) return 300;         // 5分钟
        if (value <= 600) return 600;         // 10分钟
        if (value <= 900) return 900;         // 15分钟
        if (value <= 1800) return 1800;       // 30分钟
        if (value <= 3600) return 3600;       // 1小时
        if (value <= 7200) return 7200;       // 2小时
        
        // 超过2小时，按小时向上取整
        return (float) Math.ceil(value / 3600) * 3600;
    }
    
    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        chartWidth = w - paddingLeft - paddingRight;
        chartHeight = h - paddingTop - paddingBottom;
        
        // 设置垂直渐变效果（从上到下，从鲜艳到透明）
        LinearGradient gradient = new LinearGradient(
            0, paddingTop,
            0, paddingTop + chartHeight,
            fillColorStart,
            fillColorEnd,
            Shader.TileMode.CLAMP
        );
        fillPaint.setShader(gradient);
    }
    
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        
        if (dataPoints.isEmpty()) {
            drawEmptyState(canvas);
            return;
        }
        
        // 绘制网格线和Y轴标签
        drawGridAndYAxis(canvas);
        
        // 绘制曲线和填充区域
        drawChart(canvas);
        
        // 绘制数据点
        drawDataPoints(canvas);
        
        // 绘制数据值标签
        drawValueLabels(canvas);
        
        // 绘制X轴标签（日期）
        drawXAxisLabels(canvas);
        
        // 绘制当日学习时长标签
        drawCurrentDayLabel(canvas);
    }
    
    /**
     * 绘制网格线和Y轴标签
     */
    private void drawGridAndYAxis(Canvas canvas) {
        // 绘制4条水平网格线，并添加Y轴标签
        int gridCount = 4;
        for (int i = 0; i <= gridCount; i++) {
            float y = paddingTop + (chartHeight / gridCount) * i;
            
            // 绘制网格线
            canvas.drawLine(paddingLeft, y, paddingLeft + chartWidth, y, gridPaint);
            
            // 计算对应的时长值
            float value = maxValue * (1 - (float) i / gridCount);
            String label = formatTimeShort(value);
            
            // 绘制Y轴标签（在网格线左侧）
            canvas.drawText(label, paddingLeft - 10, y + 8, yAxisPaint);
        }
    }
    
    /**
     * 绘制空数据状态
     */
    private void drawEmptyState(Canvas canvas) {
        Paint emptyPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        emptyPaint.setColor(Color.parseColor("#CCCCCC"));
        emptyPaint.setTextSize(32f);
        emptyPaint.setTextAlign(Paint.Align.CENTER);
        
        String emptyText = "暂无学习数据";
        float x = getWidth() / 2f;
        float y = getHeight() / 2f;
        canvas.drawText(emptyText, x, y, emptyPaint);
    }
    
    /**
     * 绘制图表曲线和填充区域（使用贝塞尔曲线平滑）
     */
    private void drawChart(Canvas canvas) {
        if (dataPoints.size() < 2) {
            return;
        }
        
        Path linePath = new Path();
        Path fillPath = new Path();
        Path shadowPath = new Path();
        
        float stepX = chartWidth / (dataPoints.size() - 1);
        
        // 计算第一个点的位置
        float x = paddingLeft;
        float y = getYPosition(dataPoints.get(0));
        
        linePath.moveTo(x, y);
        fillPath.moveTo(x, paddingTop + chartHeight); // 填充路径从底部开始
        fillPath.lineTo(x, y);
        shadowPath.moveTo(x, paddingTop + chartHeight + 3);
        shadowPath.lineTo(x, y + 3);
        
        // 使用贝塞尔曲线绘制平滑曲线
        for (int i = 0; i < dataPoints.size() - 1; i++) {
            float x1 = paddingLeft + stepX * i;
            float y1 = getYPosition(dataPoints.get(i));
            float x2 = paddingLeft + stepX * (i + 1);
            float y2 = getYPosition(dataPoints.get(i + 1));
            
            // 计算控制点（贝塞尔曲线）
            float controlX1 = x1 + stepX * 0.5f;
            float controlY1 = y1;
            float controlX2 = x2 - stepX * 0.5f;
            float controlY2 = y2;
            
            // 绘制平滑曲线
            linePath.cubicTo(controlX1, controlY1, controlX2, controlY2, x2, y2);
            fillPath.cubicTo(controlX1, controlY1, controlX2, controlY2, x2, y2);
            shadowPath.cubicTo(controlX1, controlY1 + 3, controlX2, controlY2 + 3, x2, y2 + 3);
        }
        
        // 填充路径闭合到底部
        float lastX = paddingLeft + stepX * (dataPoints.size() - 1);
        fillPath.lineTo(lastX, paddingTop + chartHeight);
        fillPath.close();
        
        shadowPath.lineTo(lastX, paddingTop + chartHeight + 3);
        shadowPath.close();
        
        // 先绘制阴影
        canvas.drawPath(shadowPath, shadowPaint);
        
        // 再绘制填充区域
        canvas.drawPath(fillPath, fillPaint);
        
        // 最后绘制曲线
        canvas.drawPath(linePath, linePaint);
    }
    
    /**
     * 绘制数据点（增强版）
     */
    private void drawDataPoints(Canvas canvas) {
        float stepX = chartWidth / (dataPoints.size() - 1);
        
        for (int i = 0; i < dataPoints.size(); i++) {
            float x = paddingLeft + stepX * i;
            float y = getYPosition(dataPoints.get(i));
            
            boolean isToday = (i == dataPoints.size() - 1);
            boolean hasData = dataPoints.get(i) > 0;
            
            if (!hasData && !isToday) {
                // 无数据的点显示为小灰点
                Paint grayPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
                grayPaint.setColor(Color.parseColor("#DDDDDD"));
                grayPaint.setStyle(Paint.Style.FILL);
                canvas.drawCircle(x, y, 5f, grayPaint);
                continue;
            }
            
            // 绘制外圈白色边框
            Paint whitePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
            whitePaint.setColor(Color.WHITE);
            whitePaint.setStyle(Paint.Style.FILL);
            float outerRadius = isToday ? 16f : 12f;
            canvas.drawCircle(x, y, outerRadius, whitePaint);
            
            // 绘制内圈橙色
            float innerRadius = isToday ? 12f : 8f;
            canvas.drawCircle(x, y, innerRadius, pointPaint);
            
            // 今日数据点添加脉冲效果（外圈半透明）
            if (isToday && hasData) {
                Paint pulsePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
                pulsePaint.setColor(Color.parseColor("#33FF9A6C"));
                pulsePaint.setStyle(Paint.Style.FILL);
                canvas.drawCircle(x, y, 22f, pulsePaint);
            }
        }
    }
    
    /**
     * 绘制数据值标签
     */
    private void drawValueLabels(Canvas canvas) {
        float stepX = chartWidth / (dataPoints.size() - 1);
        
        for (int i = 0; i < dataPoints.size(); i++) {
            float value = dataPoints.get(i);
            
            // 只显示有数据的值
            if (value > 0) {
                float x = paddingLeft + stepX * i;
                float y = getYPosition(value) - 20; // 在数据点上方
                
                String valueText = formatTimeShort(value);
                
                // 绘制半透明背景
                Paint bgPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
                bgPaint.setColor(Color.parseColor("#F0FFFFFF"));
                bgPaint.setStyle(Paint.Style.FILL);
                
                float textWidth = valueTextPaint.measureText(valueText);
                RectF bgRect = new RectF(
                    x - textWidth / 2 - 8,
                    y - 18,
                    x + textWidth / 2 + 8,
                    y + 6
                );
                canvas.drawRoundRect(bgRect, 8, 8, bgPaint);
                
                // 绘制数值文本
                canvas.drawText(valueText, x, y, valueTextPaint);
            }
        }
    }
    
    /**
     * 绘制X轴日期标签
     */
    private void drawXAxisLabels(Canvas canvas) {
        float stepX = chartWidth / (dataPoints.size() - 1);
        textPaint.setTextSize(28f);
        
        for (int i = 0; i < dateLabels.size(); i++) {
            float x = paddingLeft + stepX * i;
            float y = paddingTop + chartHeight + 40;
            
            // 当日标签使用橙色加粗
            if (i == dateLabels.size() - 1) {
                textPaint.setColor(currentDayColor);
                textPaint.setFakeBoldText(true);
            } else {
                textPaint.setColor(textColor);
                textPaint.setFakeBoldText(false);
            }
            
            canvas.drawText(dateLabels.get(i), x, y, textPaint);
        }
    }
    
    /**
     * 绘制当日学习时长标签
     */
    private void drawCurrentDayLabel(Canvas canvas) {
        if (dataPoints.isEmpty()) {
            return;
        }
        
        // 获取当日数据
        float currentDayValue = dataPoints.get(dataPoints.size() - 1);
        String timeText = formatTime(currentDayValue);
        
        // 计算位置（左上角）
        float x = paddingLeft;
        float y = paddingTop - 10;
        
        // 绘制文本
        Paint labelPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        labelPaint.setColor(Color.parseColor("#333333"));
        labelPaint.setTextSize(32f);
        labelPaint.setFakeBoldText(true);
        labelPaint.setTextAlign(Paint.Align.LEFT);
        
        canvas.drawText("当日学习时长: " + timeText, x, y, labelPaint);
    }
    
    /**
     * 根据数据值获取Y坐标
     */
    private float getYPosition(float value) {
        if (maxValue == 0) {
            return paddingTop + chartHeight;
        }
        
        float ratio = value / maxValue;
        return paddingTop + chartHeight - (chartHeight * ratio);
    }
    
    /**
     * 格式化时间显示（完整版）
     * @param seconds 秒数
     * @return 格式化后的时间字符串
     */
    private String formatTime(float seconds) {
        if (seconds < 60) {
            return String.format(Locale.getDefault(), "%.0fs", seconds);
        } else if (seconds < 3600) {
            int minutes = (int) (seconds / 60);
            int secs = (int) (seconds % 60);
            if (secs > 0) {
                return String.format(Locale.getDefault(), "%dm%ds", minutes, secs);
            } else {
                return String.format(Locale.getDefault(), "%dm", minutes);
            }
        } else {
            int hours = (int) (seconds / 3600);
            int minutes = (int) ((seconds % 3600) / 60);
            if (minutes > 0) {
                return String.format(Locale.getDefault(), "%dh%dm", hours, minutes);
            } else {
                return String.format(Locale.getDefault(), "%dh", hours);
            }
        }
    }
    
    /**
     * 格式化时间显示（简短版，用于Y轴和数值标签）
     * @param seconds 秒数
     * @return 简短的时间字符串
     */
    private String formatTimeShort(float seconds) {
        if (seconds == 0) {
            return "0";
        } else if (seconds < 60) {
            return String.format(Locale.getDefault(), "%.0fs", seconds);
        } else if (seconds < 3600) {
            int minutes = (int) (seconds / 60);
            return String.format(Locale.getDefault(), "%dm", minutes);
        } else {
            float hours = seconds / 3600;
            if (hours >= 10) {
                return String.format(Locale.getDefault(), "%.0fh", hours);
            } else {
                return String.format(Locale.getDefault(), "%.1fh", hours);
            }
        }
    }
}

