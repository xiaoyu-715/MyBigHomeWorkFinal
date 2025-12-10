package com.example.mybighomework.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mybighomework.R;
import com.example.mybighomework.model.ChatMessage;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * 聊天消息适配器
 */
public class ChatMessageAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    
    private Context context;
    private List<ChatMessage> messageList;
    private SimpleDateFormat timeFormat;
    private OnGeneratePlanClickListener generatePlanClickListener;
    
    public ChatMessageAdapter(Context context, List<ChatMessage> messageList) {
        this.context = context;
        this.messageList = messageList;
        this.timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
    }
    
    /**
     * 设置生成学习计划按钮点击监听器
     */
    public void setOnGeneratePlanClickListener(OnGeneratePlanClickListener listener) {
        this.generatePlanClickListener = listener;
    }
    
    @Override
    public int getItemViewType(int position) {
        return messageList.get(position).getType();
    }
    
    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == ChatMessage.TYPE_SENT) {
            View view = LayoutInflater.from(context).inflate(R.layout.item_chat_sent, parent, false);
            return new SentMessageViewHolder(view);
        } else {
            View view = LayoutInflater.from(context).inflate(R.layout.item_chat_received, parent, false);
            return new ReceivedMessageViewHolder(view);
        }
    }
    
    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        ChatMessage message = messageList.get(position);
        
        if (holder instanceof SentMessageViewHolder) {
            ((SentMessageViewHolder) holder).bind(message);
        } else if (holder instanceof ReceivedMessageViewHolder) {
            ((ReceivedMessageViewHolder) holder).bind(message);
        }
    }
    
    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position, 
                                @NonNull List<Object> payloads) {
        if (!payloads.isEmpty() && payloads.contains("payload")) {
            // 仅更新消息内容，不重新绑定整个ViewHolder
            ChatMessage message = messageList.get(position);
            if (holder instanceof ReceivedMessageViewHolder) {
                ((ReceivedMessageViewHolder) holder).updateContent(message.getContent());
            } else if (holder instanceof SentMessageViewHolder) {
                ((SentMessageViewHolder) holder).updateContent(message.getContent());
            }
        } else {
            // 完整绑定
            super.onBindViewHolder(holder, position, payloads);
        }
    }
    
    @Override
    public int getItemCount() {
        return messageList.size();
    }
    
    /**
     * 发送消息 ViewHolder
     */
    class SentMessageViewHolder extends RecyclerView.ViewHolder {
        TextView tvMessage, tvTime;
        
        public SentMessageViewHolder(@NonNull View itemView) {
            super(itemView);
            tvMessage = itemView.findViewById(R.id.tv_message);
            tvTime = itemView.findViewById(R.id.tv_time);
        }
        
        public void bind(ChatMessage message) {
            tvMessage.setText(message.getContent());
            tvTime.setText(timeFormat.format(new Date(message.getTimestamp())));
        }
        
        public void updateContent(String content) {
            tvMessage.setText(content);
        }
    }
    
    /**
     * 接收消息 ViewHolder
     */
    class ReceivedMessageViewHolder extends RecyclerView.ViewHolder {
        TextView tvMessage, tvTime;
        Button btnGeneratePlan;
        
        public ReceivedMessageViewHolder(@NonNull View itemView) {
            super(itemView);
            tvMessage = itemView.findViewById(R.id.tv_message);
            tvTime = itemView.findViewById(R.id.tv_time);
            btnGeneratePlan = itemView.findViewById(R.id.btn_generate_plan);
        }
        
        public void bind(ChatMessage message) {
            tvMessage.setText(message.getContent());
            tvTime.setText(timeFormat.format(new Date(message.getTimestamp())));
            
            // 控制生成学习计划按钮的可见性
            if (message.isShowGeneratePlanButton()) {
                btnGeneratePlan.setVisibility(View.VISIBLE);
                btnGeneratePlan.setOnClickListener(v -> {
                    if (generatePlanClickListener != null) {
                        generatePlanClickListener.onGeneratePlanClick(getAdapterPosition());
                    }
                });
            } else {
                btnGeneratePlan.setVisibility(View.GONE);
            }
        }
        
        public void updateContent(String content) {
            tvMessage.setText(content);
        }
    }
    
    /**
     * 生成学习计划按钮点击监听接口
     */
    public interface OnGeneratePlanClickListener {
        void onGeneratePlanClick(int position);
    }
}

