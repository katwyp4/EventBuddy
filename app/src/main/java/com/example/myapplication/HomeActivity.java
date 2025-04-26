package com.example.myapplication;

import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.model.Event;

import java.util.ArrayList;
import java.util.List;

import android.widget.PopupMenu;
import android.widget.Toast;
import android.widget.ImageButton;



public class HomeActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private EventAdapter adapter;
    private List<Event> eventList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        recyclerView = findViewById(R.id.eventsRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        ImageButton moreButton = findViewById(R.id.moreButton);
        moreButton.setOnClickListener(v -> showPopupMenu(v));


        eventList = new ArrayList<>();
        eventList.add(new Event("15.05–16.05.2025", "Juwenalia", "Święto studentów pełne atrakcji", R.drawable.juwenalia));
        eventList.add(new Event("19.07–25.07.2025", "Adapciak", "Obóz zerowy dla studentów", R.drawable.adapciak));
        eventList.add(new Event("19.07–25.07.2025", "koncerty", "Obóz zerowy dla studentów", R.drawable.koncert));

        adapter = new EventAdapter(eventList);



        recyclerView.setAdapter(adapter);
    }

    private void showPopupMenu(View anchor) {
        PopupMenu popup = new PopupMenu(this, anchor);
        popup.getMenuInflater().inflate(R.menu.menu, popup.getMenu());

        popup.setOnMenuItemClickListener(item -> {
            int id = item.getItemId();
            if (id == R.id.menu_home) {
                Toast.makeText(this, "Strona główna", Toast.LENGTH_SHORT).show();
                return true;
            } else if (id == R.id.menu_profile) {
                Toast.makeText(this, "Profil", Toast.LENGTH_SHORT).show();
                return true;
            } else if (id == R.id.menu_chat) {
                Toast.makeText(this, "Czat", Toast.LENGTH_SHORT).show();
                return true;
            }
            return false;

        });

        popup.show();
    }

}
