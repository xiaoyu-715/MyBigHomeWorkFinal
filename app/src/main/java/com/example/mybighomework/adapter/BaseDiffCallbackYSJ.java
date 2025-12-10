package com.example.mybighomework.adapter;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;

import java.util.List;

/**
 * 通用DiffUtil回调基类
 * 用于优化RecyclerView列表更新性能
 * 
 * 使用方式：
 * DiffUtil.DiffResult result = DiffUtil.calculateDiff(new BaseDiffCallbackYSJ<>(oldList, newList) {
 *     @Override
 *     public boolean areItemsTheSame(Item oldItem, Item newItem) {
 *         return oldItem.getId() == newItem.getId();
 *     }
 *     
 *     @Override
 *     public boolean areContentsTheSame(Item oldItem, Item newItem) {
 *         return oldItem.equals(newItem);
 *     }
 * });
 * result.dispatchUpdatesTo(adapter);
 */
public abstract class BaseDiffCallbackYSJ<T> extends DiffUtil.Callback {
    
    private final List<T> oldList;
    private final List<T> newList;
    
    public BaseDiffCallbackYSJ(List<T> oldList, List<T> newList) {
        this.oldList = oldList;
        this.newList = newList;
    }
    
    @Override
    public int getOldListSize() {
        return oldList != null ? oldList.size() : 0;
    }
    
    @Override
    public int getNewListSize() {
        return newList != null ? newList.size() : 0;
    }
    
    @Override
    public boolean areItemsTheSame(int oldItemPosition, int newItemPosition) {
        if (oldList == null || newList == null) return false;
        return areItemsTheSame(oldList.get(oldItemPosition), newList.get(newItemPosition));
    }
    
    @Override
    public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
        if (oldList == null || newList == null) return false;
        return areContentsTheSame(oldList.get(oldItemPosition), newList.get(newItemPosition));
    }
    
    /**
     * 判断两个Item是否为同一项（通常比较ID）
     */
    public abstract boolean areItemsTheSame(@NonNull T oldItem, @NonNull T newItem);
    
    /**
     * 判断两个Item的内容是否相同
     */
    public abstract boolean areContentsTheSame(@NonNull T oldItem, @NonNull T newItem);
}
