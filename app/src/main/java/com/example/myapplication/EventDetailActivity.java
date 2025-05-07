package com.example.myapplication;

import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.myapplication.model.Event;

public class EventDetailActivity extends AppCompatActivity {

    TextView titleText, dateText, descText;
    ImageView eventImage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_detail);

        titleText = findViewById(R.id.eventDetailTitle);
        dateText = findViewById(R.id.eventDetailDate);
        descText = findViewById(R.id.eventDetailDescription);
        eventImage = findViewById(R.id.eventDetailImage);

        // Odbierz dane z Intentu
        String title = getIntent().getStringExtra("title");
        String date = getIntent().getStringExtra("date");
        String desc = getIntent().getStringExtra("description");
        String imageUrl = getIntent().getStringExtra("imageUrl");

        titleText.setText(title);
        dateText.setText(date);
        descText.setText(desc);

        Glide.with(this).load(imageUrl).into(eventImage);
    }
}
