package com.example.mybighomework.adapter;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mybighomework.R;

/**
 * 滑动删除回调类
 * 实现左滑显示删除背景和图标，触发删除操作
 * 
 * Requirements: 5.1, 5.2
 */
public class SwipeToDeleteCallback extends ItemTouchHelper.SimpleCallback {

    /**
     * 滑动删除监听器接口
     */
    public interface OnSwipeDeleteListener {
        /**
         * 当用户滑动删除某项时调用
         * @param position 被删除项的位置
         */
        void onDelete(int position);
    }

    private final OnSwipeDeleteListener listener;
    private final Paint backgroundPaint;
    private final Drawable deleteIcon;
    private final int iconMargin;
    private final int backgroundColor;
    private final int iconTintColor;

    /**
     * 构造函数
     * @param context 上下文
     * @param listener 删除监听器
     */
    public SwipeToDeleteCallback(Context context, OnSwipeDeleteListener listener) {
        // 只允许左滑（END方向），不允许拖拽
        super(0, ItemTouchHelper.LEFT);
        this.listener = listener;
        
        // 初始化背景画笔
        backgroundPaint = new Paint();
        backgroundColor = ContextCompat.getColor(context, R.color.error);
        backgroundPaint.setColor(backgroundColor);
        
        // 初始化删除图标
        deleteIcon = ContextCompat.getDrawable(context, R.drawable.ic_delete);
        iconTintColor = ContextCompat.getColor(context, android.R.color.white);
        if (deleteIcon != null) {
            deleteIcon.setColorFilter(new PorterDuffColorFilter(iconTintColor, PorterDuff.Mode.SRC_IN));
        }
        
        // 图标边距
        iconMargin = (int) (16 * context.getResources().getDisplayMetrics().density);
    }

    @Override
    public boolean onMove(@NonNull RecyclerView recyclerView, 
                          @NonNull RecyclerView.ViewHolder viewHolder, 
                          @NonNull RecyclerView.ViewHolder target) {
        // 不支持拖拽移动
        return false;
    }

    @Override
    public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
        // 当滑动完成时，触发删除回调
        if (listener != null) {
            int position = viewHolder.getAdapterPosition();
            if (position != RecyclerView.NO_POSITION) {
                listener.onDelete(position);
            }
        }
    }

    @Override
    public void onChildDraw(@NonNull Canvas c, 
                            @NonNull RecyclerView recyclerView,
                            @NonNull RecyclerView.ViewHolder viewHolder, 
                            float dX, float dY,
                            int actionState, 
                            boolean isCurrentlyActive) {
        
        View itemView = viewHolder.itemView;
        
        // 只在左滑时绘制背景（dX < 0 表示左滑）
        if (dX < 0) {
            // 绘制红色背景
            float left = itemView.getRight() + dX;
            float top = itemView.getTop();
            float right = itemView.getRight();
            float bottom = itemView.getBottom();
            
            RectF background = new RectF(left, top, right, bottom);
            c.drawRect(background, backgroundPaint);
            
            // 绘制删除图标
            if (deleteIcon != null) {
                int iconSize = deleteIcon.getIntrinsicHeight();
                int itemHeight = itemView.getBottom() - itemView.getTop();
                
                // 图标垂直居中
                int iconTop = itemView.getTop() + (itemHeight - iconSize) / 2;
                int iconBottom = iconTop + iconSize;
                
                // 图标水平位置：距离右边缘 iconMargin
                int iconRight = itemView.getRight() - iconMargin;
                int iconLeft = iconRight - deleteIcon.getIntrinsicWidth();
                
                // 只有当滑动距离足够显示图标时才绘制
                if (-dX > iconMargin + deleteIcon.getIntrinsicWidth()) {
                    deleteIcon.setBounds(iconLeft, iconTop, iconRight, iconBottom);
                    deleteIcon.draw(c);
                }
            }
        }
        
        super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
    }

    @Override
    public float getSwipeThreshold(@NonNull RecyclerView.ViewHolder viewHolder) {
        // 滑动超过30%即触发删除
        return 0.3f;
    }

    @Override
    public float getSwipeEscapeVelocity(float defaultValue) {
        // 降低触发滑动的速度阈值，使滑动更灵敏
        return defaultValue * 0.5f;
    }

    @Override
    public float getSwipeVelocityThreshold(float defaultValue) {
        // 设置最大滑动速度
        return defaultValue * 1.5f;
    }
}
