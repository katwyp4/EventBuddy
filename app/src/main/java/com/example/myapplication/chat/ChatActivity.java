package com.example.myapplication.chat;

import android.os.*;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.*;
import com.example.myapplication.R;
import com.example.myapplication.data.*;
import com.example.myapplication.network.*;
import retrofit2.*;
import java.util.List;

public class ChatActivity extends AppCompatActivity {

    private MessageAdapter adapter;
    private ApiService api;
    private long eventId, userId;
    private final Handler ui = new Handler(Looper.getMainLooper());

    @Override protected void onCreate(Bundle s){
        super.onCreate(s);
        setContentView(R.layout.activity_chat);

        /* parametry */
        eventId = getIntent().getLongExtra("EVENT_ID",1);
        userId  = getIntent().getLongExtra("USER_ID", 1);

        /* Retrofit → ApiService */
        Retrofit retrofit = RetrofitClient.getInstance(this);
        api = retrofit.create(ApiService.class);

        /* RecyclerView */
        adapter = new MessageAdapter();
        RecyclerView rv = findViewById(R.id.rvMessages);
        rv.setLayoutManager(new LinearLayoutManager(this));
        rv.setAdapter(adapter);

        EditText et = findViewById(R.id.etMessage);
        Button   bt = findViewById(R.id.btnSend);

        /* 1. Historia */
        api.getMessages(eventId).enqueue(new Callback<>() {
            @Override public void onResponse(Call<List<MessageDto>> c, Response<List<MessageDto>> r){
                if(r.isSuccessful() && r.body()!=null){
                    adapter.addAll(r.body());
                    rv.scrollToPosition(adapter.getItemCount()-1);
                }
            }
            @Override public void onFailure(Call<List<MessageDto>> c, Throwable t){}
        });

        /* 2. Wysyłanie */
        bt.setOnClickListener(v -> {
            String text = et.getText().toString().trim();
            if(text.isEmpty()) return;
            et.setText("");
            api.sendMessage(new CreateMessageDto(userId,eventId,text))
                    .enqueue(new Callback<>() {
                        @Override public void onResponse(Call<MessageDto> c, Response<MessageDto> r){
                            if(r.isSuccessful() && r.body()!=null){
                                adapter.add(r.body());
                                rv.scrollToPosition(adapter.getItemCount()-1);
                            }
                        }
                        @Override public void onFailure(Call<MessageDto> c, Throwable t){}
                    });
        });

        /* 3. Polling */
        ui.postDelayed(new Runnable(){
            @Override public void run(){
                String after = adapter.items.isEmpty()
                        ? "1970-01-01T00:00:00"
                        : adapter.items.get(adapter.items.size()-1).getSentAt();
                api.getLatest(eventId, after).enqueue(new Callback<>() {
                    @Override public void onResponse(Call<List<MessageDto>> c, Response<List<MessageDto>> r){
                        if(r.isSuccessful() && r.body()!=null && !r.body().isEmpty()){
                            adapter.addAll(r.body());
                            rv.scrollToPosition(adapter.getItemCount()-1);
                        }
                    }
                    @Override public void onFailure(Call<List<MessageDto>> c, Throwable t){}
                });
                ui.postDelayed(this,3000);
            }
        },3000);
    }
    @Override protected void onDestroy(){
        super.onDestroy();
        ui.removeCallbacksAndMessages(null);
    }
}
