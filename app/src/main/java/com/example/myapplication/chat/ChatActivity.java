package com.example.myapplication.chat;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.R;
import com.example.myapplication.data.CreateMessageDto;
import com.example.myapplication.data.MessageDto;
import com.example.myapplication.network.ApiService;
import com.example.myapplication.network.RetrofitClient;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ChatActivity extends AppCompatActivity {

    private RecyclerView rvMessages;
    private EditText   etMessage;
    private Button     btnSend;

    private long   eventId;
    private long   userId = 1;        // TODO: pobierz z auth
    private ChatAdapter  adapter;
    private ApiService   api;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        // 1. Intent extras
        eventId = getIntent().getLongExtra("EVENT_ID", -1);
        userId  = getIntent().getLongExtra("USER_ID", 1);

        // 2. Retrofit
        api = RetrofitClient.getInstance(this).create(ApiService.class);

        // 3. View binding
        rvMessages = findViewById(R.id.rvMessages);
        etMessage  = findViewById(R.id.etMessage);
        btnSend    = findViewById(R.id.btnSend);

        // 4. RecyclerView + adapter
        rvMessages.setLayoutManager(new LinearLayoutManager(this));
        adapter = new ChatAdapter(new ArrayList<>());
        rvMessages.setAdapter(adapter);

        // 5. Load existing messages
        loadMessages();

        // 6. Send button
        btnSend.setOnClickListener(v -> {
            String content = etMessage.getText().toString().trim();
            if (content.isEmpty()) return;

            CreateMessageDto dto = new CreateMessageDto();
            dto.setEventId(eventId);
            dto.setContent(content);
            api.sendMessage(dto).enqueue(new Callback<MessageDto>() {
                @Override public void onResponse(Call<MessageDto> c, Response<MessageDto> r) {
                    if (r.isSuccessful() && r.body() != null) {
                        adapter.add(r.body());
                        rvMessages.scrollToPosition(adapter.getItemCount() - 1);
                        etMessage.setText("");
                    }
                }
                @Override public void onFailure(Call<MessageDto> c, Throwable t) { /* TODO: obsługa błędów */ }
            });
        });
    }

    private void loadMessages() {
        api.getMessages(eventId).enqueue(new Callback<List<MessageDto>>() {
            @Override public void onResponse(Call<List<MessageDto>> c, Response<List<MessageDto>> r) {
                if (r.isSuccessful() && r.body() != null) {
                    adapter.setData(r.body());
                    rvMessages.scrollToPosition(adapter.getItemCount() - 1);
                }
            }
            @Override public void onFailure(Call<List<MessageDto>> c, Throwable t) { /* TODO */ }
        });
    }
}
