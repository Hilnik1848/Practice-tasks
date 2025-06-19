package com.example.travenor;

import android.os.Bundle;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.travenor.Models.DataBinding;
import com.example.travenor.Models.Message;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import com.google.gson.JsonObject;

public class ChatDetailActivity extends AppCompatActivity {

    private RecyclerView messagesRecyclerView;
    private EditText editMessage;
    private ImageButton btnSend, backBtn;
    private List<Message> messageList = new ArrayList<>();
    private MessageAdapter messageAdapter;
    private int chatId;
    private String currentUserId;
    private OkHttpClient client = new OkHttpClient();

    public static final MediaType JSON = MediaType.get("application/json; charset=utf-8");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_chat_detail);

        chatId = getIntent().getIntExtra("CHAT_ID", -1);
        if (chatId == -1) {
            Toast.makeText(this, "Invalid chat ID", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        currentUserId = DataBinding.getUuidUser();

        messagesRecyclerView = findViewById(R.id.messagesRecyclerView);
        editMessage = findViewById(R.id.editMessage);
        btnSend = findViewById(R.id.btnSend);
        backBtn = findViewById(R.id.BackBTN);

        messagesRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        messageAdapter = new MessageAdapter(messageList, currentUserId, this::deleteMessage);
        messagesRecyclerView.setAdapter(messageAdapter);

        loadMessages(chatId);

        backBtn.setOnClickListener(v -> finish());

        btnSend.setOnClickListener(v -> {
            String text = editMessage.getText().toString().trim();
            if (!text.isEmpty()) {
                sendMessage(text);
            }
        });
    }

    private void loadMessages(int chatId) {
        String url = "https://mmbdesfnabtcbpjwcwde.supabase.co/rest/v1/messages?select=*&chat_id=eq." + chatId;

        Request request = new Request.Builder()
                .url(url)
                .get()
                .addHeader("apikey", "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6Im1tYmRlc2ZuYWJ0Y2Jwandjd2RlIiwicm9sZSI6ImFub24iLCJpYXQiOjE3NDg5NTg4MDMsImV4cCI6MjA2NDUzNDgwM30.zU9xsd7HMVuLi6OkiKTaB723ek2YNomMgrqnKKvSvQk")
                .addHeader("Authorization", "Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6Im1tYmRlc2ZuYWJ0Y2Jwandjd2RlIiwicm9sZSI6ImFub24iLCJpYXQiOjE3NDg5NTg4MDMsImV4cCI6MjA2NDUzNDgwM30.zU9xsd7HMVuLi6OkiKTaB723ek2YNomMgrqnKKvSvQk")
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(() -> Toast.makeText(ChatDetailActivity.this, "Ошибка загрузки сообщений", Toast.LENGTH_SHORT).show());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful() && response.body() != null) {
                    String responseBody = response.body().string();
                    Gson gson = new Gson();
                    Type listType = new TypeToken<List<Message>>(){}.getType();
                    List<Message> messages = gson.fromJson(responseBody, listType);

                    runOnUiThread(() -> {
                        messageList.clear();
                        if (messages != null) messageList.addAll(messages);
                        messageAdapter.notifyDataSetChanged();
                        messagesRecyclerView.scrollToPosition(messageList.size() - 1);
                    });
                }
            }
        });
    }

    private void sendMessage(String messageText) {
        JsonObject json = new JsonObject();
        json.addProperty("sender_id", currentUserId);
        json.addProperty("message_text", messageText);
        json.addProperty("chat_id", chatId);

        RequestBody body = RequestBody.create(JSON, json.toString());
        String url = "https://mmbdesfnabtcbpjwcwde.supabase.co/rest/v1/messages";

        Request request = new Request.Builder()
                .url(url)
                .post(body)
                .addHeader("apikey", "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6Im1tYmRlc2ZuYWJ0Y2Jwandjd2RlIiwicm9sZSI6ImFub24iLCJpYXQiOjE3NDg5NTg4MDMsImV4cCI6MjA2NDUzNDgwM30.zU9xsd7HMVuLi6OkiKTaB723ek2YNomMgrqnKKvSvQk")
                .addHeader("Authorization", "Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6Im1tYmRlc2ZuYWJ0Y2Jwandjd2RlIiwicm9sZSI6ImFub24iLCJpYXQiOjE3NDg5NTg4MDMsImV4cCI6MjA2NDUzNDgwM30.zU9xsd7HMVuLi6OkiKTaB723ek2YNomMgrqnKKvSvQk")
                .addHeader("Content-Type", "application/json")
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(() -> Toast.makeText(ChatDetailActivity.this, "Ошибка отправки", Toast.LENGTH_SHORT).show());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    runOnUiThread(() -> {
                        editMessage.setText("");
                        loadMessages(chatId); // Обновляем список
                    });
                } else {
                    runOnUiThread(() -> Toast.makeText(ChatDetailActivity.this, "Ошибка: " + response.code(), Toast.LENGTH_SHORT).show());
                }
            }
        });
    }

    private void deleteMessage(Message message) {
        int messageId = message.getId();
        String url = "https://mmbdesfnabtcbpjwcwde.supabase.co/rest/v1/messages?id=eq." + messageId;

        Request request = new Request.Builder()
                .url(url)
                .delete()
                .addHeader("apikey", "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6Im1tYmRlc2ZuYWJ0Y2Jwandjd2RlIiwicm9sZSI6ImFub24iLCJpYXQiOjE3NDg5NTg4MDMsImV4cCI6MjA2NDUzNDgwM30.zU9xsd7HMVuLi6OkiKTaB723ek2YNomMgrqnKKvSvQk")
                .addHeader("Authorization", "Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6Im1tYmRlc2ZuYWJ0Y2Jwandjd2RlIiwicm9sZSI6ImFub24iLCJpYXQiOjE3NDg5NTg4MDMsImV4cCI6MjA2NDUzNDgwM30.zU9xsd7HMVuLi6OkiKTaB723ek2YNomMgrqnKKvSvQk")
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(() -> Toast.makeText(ChatDetailActivity.this, "Ошибка при удалении", Toast.LENGTH_SHORT).show());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    runOnUiThread(() -> {
                        loadMessages(chatId);
                        Toast.makeText(ChatDetailActivity.this, "Сообщение удалено", Toast.LENGTH_SHORT).show();
                    });
                } else {
                    runOnUiThread(() -> Toast.makeText(ChatDetailActivity.this, "Не удалось удалить", Toast.LENGTH_SHORT).show());
                }
            }
        });
    }
}