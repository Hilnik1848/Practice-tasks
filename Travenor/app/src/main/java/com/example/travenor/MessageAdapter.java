package com.example.travenor;

import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.travenor.Models.Message;
import java.util.List;

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.MessageViewHolder> {

    private List<Message> messageList;
    private String currentUserId;
    private OnMessageLongClickListener listener;

    public interface OnMessageLongClickListener {
        void onMessageLongClick(Message message);
    }

    public MessageAdapter(List<Message> messageList, String currentUserId, OnMessageLongClickListener listener) {
        this.messageList = messageList;
        this.currentUserId = currentUserId;
        this.listener = listener;
    }

    @NonNull
    @Override
    public MessageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_message, parent, false);
        return new MessageViewHolder(view, listener);
    }

    @Override
    public void onBindViewHolder(@NonNull MessageViewHolder holder, int position) {
        Message message = messageList.get(position);
        holder.textMessage.setText(message.getMessageText());

        boolean isMine = currentUserId.equals(message.getSenderId());
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );

        if (isMine) {
            params.gravity = Gravity.START;
        } else {
            params.gravity = Gravity.END;
        }

        holder.textMessage.setLayoutParams(params);
        holder.itemView.setTag(message);
    }

    @Override
    public int getItemCount() {
        return messageList.size();
    }

    public static class MessageViewHolder extends RecyclerView.ViewHolder {

        TextView textMessage;

        public MessageViewHolder(@NonNull View itemView, OnMessageLongClickListener listener) {
            super(itemView);
            textMessage = itemView.findViewById(R.id.textMessage);

            itemView.setOnLongClickListener(v -> {
                Message message = (Message) v.getTag();
                if (message != null && listener != null) {
                    listener.onMessageLongClick(message);
                }
                return true;
            });
        }
    }
}