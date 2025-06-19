package com.example.travenor;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.travenor.Models.Chat;
import com.example.travenor.Models.DataBinding;
import com.example.travenor.Models.Manager;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class MessagesFragment extends Fragment implements ChatAdapter.OnChatClickListener {
    private RecyclerView conversationsRecyclerView;
    private ChatAdapter chatAdapter;
    private List<Chat> chats = new ArrayList<>();
    private boolean isManager = false;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.activity_messages, container, false);
        conversationsRecyclerView = view.findViewById(R.id.conversationsRecyclerView);
        conversationsRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        checkIfManager();
        return view;
    }

    private void checkIfManager() {
        String userId = DataBinding.getUuidUser();
        if (userId == null || userId.isEmpty()) {
            Toast.makeText(getContext(), "User not logged in", Toast.LENGTH_SHORT).show();
            return;
        }
        SupabaseClient supabaseClient = new SupabaseClient();
        supabaseClient.checkIfManager(userId, new SupabaseClient.SBC_Callback() {
            @Override
            public void onFailure(IOException e) {
                loadUserChats();
            }

            @Override
            public void onResponse(String responseBody) {
                try {
                    Type listType = new TypeToken<ArrayList<Manager>>() {}.getType();
                    List<Manager> managers = new Gson().fromJson(responseBody, listType);
                    isManager = managers != null && !managers.isEmpty();
                    if (isManager) {
                        loadManagerChats();
                    } else {
                        loadUserChats();
                    }
                } catch (Exception e) {
                    loadUserChats();
                }
            }
        });
    }

    private void loadUserChats() {
        String userId = DataBinding.getUuidUser();
        SupabaseClient supabaseClient = new SupabaseClient();
        supabaseClient.fetchUserChats(userId, new SupabaseClient.SBC_Callback() {
            @Override
            public void onResponse(String responseBody) {
                processChats(responseBody, false);
            }

            @Override
            public void onFailure(IOException e) {
                showError();
            }
        });
    }

    private void loadManagerChats() {
        String userId = DataBinding.getUuidUser();
        SupabaseClient supabaseClient = new SupabaseClient();
        supabaseClient.fetchManagerId(userId, new SupabaseClient.SBC_Callback() {
            @Override
            public void onResponse(String responseBody) {
                try {
                    Type listType = new TypeToken<ArrayList<Manager>>() {}.getType();
                    List<Manager> managers = new Gson().fromJson(responseBody, listType);
                    if (managers != null && !managers.isEmpty()) {
                        int managerId = managers.get(0).getId(); // Получаем числовой ID
                        supabaseClient.fetchManagerChats(managerId, new SupabaseClient.SBC_Callback() {
                            @Override
                            public void onResponse(String chatsResponse) {
                                processChats(chatsResponse, true);
                            }

                            @Override
                            public void onFailure(IOException e) {
                                showError();
                            }
                        });
                    }
                } catch (Exception e) {
                    showError();
                }
            }

            @Override
            public void onFailure(IOException e) {
                showError();
            }
        });
    }

    private void processChats(String responseBody, boolean isManager) {
        try {
            Type listType = new TypeToken<ArrayList<Chat>>() {}.getType();
            List<Chat> newChats = new Gson().fromJson(responseBody, listType);

            requireActivity().runOnUiThread(() -> {
                chats.clear();
                if (newChats != null) {
                    chats.addAll(newChats);
                }
                chatAdapter = new ChatAdapter(chats, getContext(), this, isManager);
                conversationsRecyclerView.setAdapter(chatAdapter);
            });
        } catch (Exception e) {
            showError();
        }
    }

    private void showError() {
        requireActivity().runOnUiThread(() ->
                Toast.makeText(getContext(), "Error loading chats", Toast.LENGTH_SHORT).show()
        );
    }

    @Override
    public void onChatClick(Chat chat) {
        int chatId = chat.getId();
        if (chatId <= 0) return;

        requireContext().startActivity(new Intent(requireContext(), ChatDetailActivity.class)
                .putExtra("CHAT_ID", chat.getId()));
    }
}
