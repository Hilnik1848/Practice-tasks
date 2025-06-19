package com.example.travenor;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.example.travenor.Models.Chat;
import com.example.travenor.Models.Profile;

import java.util.List;

public class ChatAdapter extends RecyclerView.Adapter<ChatAdapter.ChatViewHolder> {
    private List<Chat> chats;
    private Context context;
    private OnChatClickListener listener;
    private boolean isManager;

    public interface OnChatClickListener {
        void onChatClick(Chat chat);
    }

    public ChatAdapter(List<Chat> chats, Context context, OnChatClickListener listener, boolean isManager) {
        this.chats = chats;
        this.context = context;
        this.listener = listener;
        this.isManager = isManager;
    }

    @NonNull
    @Override
    public ChatViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.conversation_item, parent, false);
        return new ChatViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ChatViewHolder holder, int position) {
        Chat chat = chats.get(position);
        Profile otherProfile;
        String name;
        String avatarUrl;

        if (isManager) {
            otherProfile = chat.getUserProfile();
            name = otherProfile != null ? otherProfile.getFullName() : "Unknown User";
            avatarUrl = otherProfile != null ? otherProfile.getAvatarUrl() : null;
        } else {
            if (chat.getManager() != null && chat.getManager().getProfile() != null) {
                otherProfile = chat.getManager().getProfile();
                name = otherProfile.getFullName();
                avatarUrl = otherProfile.getAvatarUrl();
            } else {
                name = "Unknown Manager";
                avatarUrl = null;
            }
        }

        holder.contactName.setText(name);

        if (avatarUrl != null && !avatarUrl.isEmpty()) {
            if (!avatarUrl.startsWith("http")) {
                avatarUrl = "https://mmbdesfnabtcbpjwcwde.supabase.co/storage/v1/object/public/avatars/" + avatarUrl;
            }
            Glide.with(context)
                    .load(avatarUrl)
                    .placeholder(R.drawable.img1)
                    .error(R.drawable.img1)
                    .into(holder.profileImage);
        } else {
            holder.profileImage.setImageResource(R.drawable.img1);
        }

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onChatClick(chat);
            }
        });
    }

    @Override
    public int getItemCount() {
        return chats.size();
    }

    public static class ChatViewHolder extends RecyclerView.ViewHolder {
        ImageView profileImage;
        TextView contactName;

        public ChatViewHolder(@NonNull View itemView) {
            super(itemView);
            profileImage = itemView.findViewById(R.id.conversationProfileImage);
            contactName = itemView.findViewById(R.id.conversationContactName);
        }
    }
}
