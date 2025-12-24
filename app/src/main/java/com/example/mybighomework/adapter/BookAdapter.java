package com.example.mybighomework.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mybighomework.R;
import com.example.mybighomework.database.entity.BookEntity;

import java.util.ArrayList;
import java.util.List;

/**
 * 词书列表适配器
 * 支持分类和词书两种类型的显示
 */
public class BookAdapter extends RecyclerView.Adapter<BookAdapter.BookViewHolder> {

    private List<BookEntity> books = new ArrayList<>();
    private OnBookClickListener listener;

    public interface OnBookClickListener {
        void onBookClick(BookEntity book);
        void onBookLongClick(BookEntity book);
    }

    public BookAdapter(OnBookClickListener listener) {
        this.listener = listener;
    }

    public void setBooks(List<BookEntity> books) {
        this.books = books != null ? books : new ArrayList<>();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public BookViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_book, parent, false);
        return new BookViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull BookViewHolder holder, int position) {
        BookEntity book = books.get(position);
        holder.bind(book);
    }

    @Override
    public int getItemCount() {
        return books.size();
    }

    class BookViewHolder extends RecyclerView.ViewHolder {
        private ImageView ivFolderIcon, ivBookIcon, ivArrow;
        private TextView tvBookName, tvBookAuthor, tvWordCount, tvChildCount, tvProgressPercent;
        private LinearLayout layoutProgress;
        private ProgressBar progressBar;

        BookViewHolder(@NonNull View itemView) {
            super(itemView);
            ivFolderIcon = itemView.findViewById(R.id.iv_folder_icon);
            ivBookIcon = itemView.findViewById(R.id.iv_book_icon);
            ivArrow = itemView.findViewById(R.id.iv_arrow);
            tvBookName = itemView.findViewById(R.id.tv_book_name);
            tvBookAuthor = itemView.findViewById(R.id.tv_book_author);
            tvWordCount = itemView.findViewById(R.id.tv_word_count);
            tvChildCount = itemView.findViewById(R.id.tv_child_count);
            tvProgressPercent = itemView.findViewById(R.id.tv_progress_percent);
            layoutProgress = itemView.findViewById(R.id.layout_progress);
            progressBar = itemView.findViewById(R.id.progress_bar);
        }

        void bind(BookEntity book) {
            // 设置名称
            tvBookName.setText(book.getName());

            // 根据类型显示不同图标
            boolean isCategory = book.getLevel() == 1;
            if (isCategory) {
                // 分类显示文件夹图标
                ivFolderIcon.setVisibility(View.VISIBLE);
                ivBookIcon.setVisibility(View.GONE);
                tvChildCount.setVisibility(View.VISIBLE);
                tvChildCount.setText(book.getDirectItemNum() + " 本词书");
                layoutProgress.setVisibility(View.GONE);
            } else {
                // 词书显示书本图标
                ivFolderIcon.setVisibility(View.GONE);
                ivBookIcon.setVisibility(View.VISIBLE);
                tvChildCount.setVisibility(View.GONE);
                // TODO: 显示学习进度
                layoutProgress.setVisibility(View.GONE);
            }

            // 显示单词数量
            tvWordCount.setText(book.getItemNum() + " 词");

            // 显示作者/出版社
            String author = book.getAuthor();
            String publisher = book.getPublisher();
            if (author != null && !author.isEmpty()) {
                tvBookAuthor.setVisibility(View.VISIBLE);
                tvBookAuthor.setText(author);
            } else if (publisher != null && !publisher.isEmpty()) {
                tvBookAuthor.setVisibility(View.VISIBLE);
                tvBookAuthor.setText(publisher);
            } else {
                tvBookAuthor.setVisibility(View.GONE);
            }

            // 点击事件
            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onBookClick(book);
                }
            });

            itemView.setOnLongClickListener(v -> {
                if (listener != null) {
                    listener.onBookLongClick(book);
                }
                return true;
            });
        }

        /**
         * 设置学习进度
         */
        void setProgress(int learnedCount, int totalCount) {
            if (totalCount > 0) {
                layoutProgress.setVisibility(View.VISIBLE);
                int percent = (int) ((float) learnedCount / totalCount * 100);
                progressBar.setProgress(percent);
                tvProgressPercent.setText(percent + "%");
            } else {
                layoutProgress.setVisibility(View.GONE);
            }
        }
    }
}
