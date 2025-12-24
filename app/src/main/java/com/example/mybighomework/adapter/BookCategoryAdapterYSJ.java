package com.example.mybighomework.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mybighomework.R;
import com.example.mybighomework.database.entity.BookEntity;

import java.util.List;

/**
 * 词书分类适配器
 * 网格布局显示分类卡片
 */
public class BookCategoryAdapterYSJ extends RecyclerView.Adapter<BookCategoryAdapterYSJ.ViewHolder> {
    
    private Context context;
    private List<BookEntity> categoryList;
    private OnCategorySelectedListener listener;
    
    public interface OnCategorySelectedListener {
        void onCategorySelected(BookEntity category);
    }
    
    public BookCategoryAdapterYSJ(Context context, List<BookEntity> categoryList, OnCategorySelectedListener listener) {
        this.context = context;
        this.categoryList = categoryList;
        this.listener = listener;
    }
    
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_book_category, parent, false);
        return new ViewHolder(view);
    }
    
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        BookEntity category = categoryList.get(position);
        
        holder.tvCategoryName.setText(category.getName());
        
        String info = category.getFullName();
        if (info == null || info.isEmpty()) {
            info = "点击查看词书";
        }
        holder.tvCategoryInfo.setText(info);
        
        holder.ivIcon.setImageResource(getCategoryIcon(category.getName()));
        
        holder.cardView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onCategorySelected(category);
            }
        });
    }
    
    @Override
    public int getItemCount() {
        return categoryList.size();
    }
    
    private int getCategoryIcon(String categoryName) {
        if (categoryName == null) return R.drawable.ic_book;
        
        if (categoryName.contains("四级")) return R.drawable.ic_book;
        if (categoryName.contains("六级")) return R.drawable.ic_book;
        if (categoryName.contains("考研")) return R.drawable.ic_book;
        if (categoryName.contains("托福")) return R.drawable.ic_book;
        if (categoryName.contains("雅思")) return R.drawable.ic_book;
        if (categoryName.contains("GRE")) return R.drawable.ic_book;
        
        return R.drawable.ic_book;
    }
    
    static class ViewHolder extends RecyclerView.ViewHolder {
        CardView cardView;
        ImageView ivIcon;
        TextView tvCategoryName;
        TextView tvCategoryInfo;
        
        ViewHolder(View itemView) {
            super(itemView);
            cardView = itemView.findViewById(R.id.card_view);
            ivIcon = itemView.findViewById(R.id.iv_icon);
            tvCategoryName = itemView.findViewById(R.id.tv_category_name);
            tvCategoryInfo = itemView.findViewById(R.id.tv_category_info);
        }
    }
}
