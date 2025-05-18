package com.example.myapplication;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.myapplication.model.Event;

import java.util.ArrayList;
import java.util.List;
import com.example.myapplication.model.Poll;


public class EventAdapter extends RecyclerView.Adapter<EventAdapter.EventViewHolder> {

    private List<Event> eventList;

    public EventAdapter(List<Event> eventList) {
        this.eventList = eventList;
    }

    public static class EventViewHolder extends RecyclerView.ViewHolder {
        TextView eventDate, eventTitle, eventDescription;
        ImageView eventImage;
        Button readMoreBtn;

        TextView dateVotingInfo, locationVotingInfo;

        public EventViewHolder(View itemView) {
            super(itemView);
            eventDate = itemView.findViewById(R.id.eventDate);
            eventTitle = itemView.findViewById(R.id.eventTitle);
            eventDescription = itemView.findViewById(R.id.eventDescription);
            eventImage = itemView.findViewById(R.id.eventImage);
            readMoreBtn = itemView.findViewById(R.id.readMoreBtn);

            dateVotingInfo = itemView.findViewById(R.id.dateVotingInfo);
            locationVotingInfo = itemView.findViewById(R.id.locationVotingInfo);
        }
    }

    @Override
    public EventViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_event, parent, false);
        return new EventViewHolder(view);
    }

    @Override
    public void onBindViewHolder(EventViewHolder holder, int position) {
        Event event = eventList.get(position);
        holder.eventDate.setText(event.getDate());
        holder.eventTitle.setText(event.getTitle());
        holder.eventDescription.setText(event.getDescription());
        Glide.with(holder.itemView.getContext())
                .load("http://10.0.2.2:8080" + event.getImageUrl())
                .into(holder.eventImage);

        // Pokaz informację, czy jest głosowanie na datę
        if (event.getDatePoll() != null && event.getDatePoll() != null) {
            holder.dateVotingInfo.setText("Głosowanie na datę: TAK");
            holder.dateVotingInfo.setVisibility(View.VISIBLE);
        } else {
            holder.dateVotingInfo.setVisibility(View.GONE);
        }

        // Pokaz informację, czy jest głosowanie na lokalizację
        if (event.getLocationPoll() != null && event.getLocationPoll() != null) {
            holder.locationVotingInfo.setText("Głosowanie na lokalizację: TAK");
            holder.locationVotingInfo.setVisibility(View.VISIBLE);
        } else {
            holder.locationVotingInfo.setVisibility(View.GONE);
        }

        holder.readMoreBtn.setOnClickListener(v -> {
            Intent intent = new Intent(holder.itemView.getContext(), EventDetailActivity.class);
            intent.putExtra("eventId", event.getId());
            intent.putExtra("title", event.getTitle());
            intent.putExtra("date", event.getDate());
            intent.putExtra("description", event.getDescription());
            intent.putExtra("imageUrl", event.getImageUrl());
            intent.putExtra("location", event.getLocation());

//            if (event.getPolls() != null) {
//                ArrayList<String> datePollQuestions = new ArrayList<>();
//                ArrayList<String> locationPollQuestions = new ArrayList<>();
//
//                for (Poll poll : event.getPolls()) {
//                    if (poll.getQuestion().toLowerCase().contains("data")) {
//                        datePollQuestions.add(poll.getQuestion());
//                    }
//                    if (poll.getQuestion().toLowerCase().contains("lokalizację") || poll.getQuestion().toLowerCase().contains("lokalizacje")) {
//                        locationPollQuestions.add(poll.getQuestion());
//                    }
//                }
//
//                if (!datePollQuestions.isEmpty()) {
//                    intent.putExtra("eventId", event.getId());
//                }
//                if (!locationPollQuestions.isEmpty()) {
//                    intent.putExtra("eventId", event.getId());
//                }
//            }


            holder.itemView.getContext().startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return eventList.size();
    }
}
