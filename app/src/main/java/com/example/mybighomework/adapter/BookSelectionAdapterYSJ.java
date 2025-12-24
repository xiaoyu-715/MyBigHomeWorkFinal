package com.example.mybighomework.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mybighomework.R;
import com.example.mybighomework.database.entity.BookEntity;

import java.util.List;

/**
 * 词书选择适配器
 */
public class BookSelectionAdapterYSJ extends RecyclerView.Adapter<BookSelectionAdapterYSJ.ViewHolder> {
    
    private Context context;
    private List<BookEntity> bookList;
    private OnBookSelectedListener listener;
    
    public interface OnBookSelectedListener {
        void onBookSelected(BookEntity book);
    }
    
    public BookSelectionAdapterYSJ(Context context, List<BookEntity> bookList, OnBookSelectedListener listener) {
        this.context = context;
        this.bookList = bookList;
        this.listener = listener;
    }
    
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_book_selection, parent, false);
        return new ViewHolder(view);
    }
    
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        BookEntity book = bookList.get(position);
        
        holder.tvBookName.setText(book.getName());
        
        String info = book.getFullName() != null ? book.getFullName() : "";
        if (book.getDirectItemNum() > 0) {
            info += " · " + book.getDirectItemNum() + "个单词";
        }
        holder.tvBookInfo.setText(info);
        
        holder.cardView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onBookSelected(book);
            }
        });
    }
    
    @Override
    public int getItemCount() {
        return bookList.size();
    }
    
    static class ViewHolder extends RecyclerView.ViewHolder {
        CardView cardView;
        TextView tvBookName;
        TextView tvBookInfo;
        
        ViewHolder(View itemView) {
            super(itemView);
            cardView = (CardView) itemView;
            tvBookName = itemView.findViewById(R.id.tv_book_name);
            tvBookInfo = itemView.findViewById(R.id.tv_book_info);
        }
    }
}
