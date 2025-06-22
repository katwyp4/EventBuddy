package com.example.myapplication;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.util.Pair;
import android.view.View;
import android.widget.*;



import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;


import com.bumptech.glide.Glide;

import com.example.myapplication.model.PollOption;
import com.example.myapplication.network.ApiService;
import com.example.myapplication.network.RetrofitClient;

import com.example.myapplication.budget.BudgetActivity;
import com.example.myapplication.chat.ChatActivity;

import java.util.Arrays;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class EventDetailActivity extends AppCompatActivity {

    TextView titleText, dateText, descText, locationText;
    ImageView eventImage;

    LinearLayout dateVotingOptionsContainer, locationVotingOptionsContainer;
    RadioGroup dateVotingRadioGroup, locationVotingRadioGroup;
    Button btnVoteDate, btnVoteLocation;

    ApiService apiService;

    List<PollOption> datePollOptions = new ArrayList<>();
    List<PollOption> locationPollOptions = new ArrayList<>();

    private Button btnOpenBudget;

    private ImageButton btnOpenChat;

    private ImageButton btnOpenTasks;

    private ImageButton btnOpenGallery;



    private boolean isParticipant = true;

    private String dateVotingEnd;
    private String locationVotingEnd;

    private TextView dateVotingEndInfo;
    private TextView locationVotingEndInfo;





    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_detail);

        apiService = RetrofitClient.getInstance(this).create(ApiService.class);

        titleText = findViewById(R.id.eventDetailTitle);
        dateText = findViewById(R.id.eventDetailDate);
        descText = findViewById(R.id.eventDetailDescription);
        eventImage = findViewById(R.id.eventDetailImage);
        locationText = findViewById(R.id.eventDetailLocation);
        dateVotingOptionsContainer = findViewById(R.id.dateVotingOptionsContainer);
        locationVotingOptionsContainer = findViewById(R.id.locationVotingOptionsContainer);
        dateVotingRadioGroup = findViewById(R.id.dateVotingRadioGroup);
        locationVotingRadioGroup = findViewById(R.id.locationVotingRadioGroup);
        btnVoteDate = findViewById(R.id.btnVoteDate);
        btnVoteLocation = findViewById(R.id.btnVoteLocation);
        btnOpenBudget = findViewById(R.id.btnOpenBudget);
        btnOpenTasks = findViewById(R.id.btnOpenTasks);
        btnOpenGallery = findViewById(R.id.btnOpenGallery);





        btnOpenTasks.setOnClickListener(v -> {
            Intent i = new Intent(EventDetailActivity.this,
                    com.example.myapplication.task.TaskActivity.class);

            i.putExtra("EVENT_ID",
                    getIntent().getLongExtra("eventId", -1));     // id eventu

            startActivity(i);
        });



        btnOpenChat  = findViewById(R.id.btnOpenChat);
        dateVotingEndInfo = findViewById(R.id.dateVotingEndInfo);
        locationVotingEndInfo = findViewById(R.id.locationVotingEndInfo);




        btnOpenChat.setOnClickListener(v -> {
            Intent i = new Intent(this, ChatActivity.class);
            i.putExtra("EVENT_ID", getIntent().getLongExtra("eventId", -1));
            startActivity(i);
        });

        btnOpenGallery.setOnClickListener(v -> {
            Intent i = new Intent(EventDetailActivity.this, com.example.myapplication.gallery.GalleryActivity.class);
            i.putExtra("EVENT_ID", getIntent().getLongExtra("eventId", -1));
            startActivity(i);
        });



        Long eventId = getIntent().getLongExtra("eventId", -1);

        if (eventId != -1) {
            apiService.getEvent(eventId).enqueue(new Callback<com.example.myapplication.model.Event>() {
                @Override
                public void onResponse(Call<com.example.myapplication.model.Event> call, Response<com.example.myapplication.model.Event> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        com.example.myapplication.model.Event event = response.body();

                        dateVotingEnd = event.getDatePollDeadline();
                        locationVotingEnd = event.getLocationPollDeadline();

                        isParticipant = event.isParticipant();

                        if (!isParticipant) {
                            btnOpenBudget.setVisibility(View.GONE);
                        } else {
                            btnOpenBudget.setVisibility(View.VISIBLE); // just in case
                            btnOpenBudget.setOnClickListener(v -> {
                                Intent i = new Intent(EventDetailActivity.this, BudgetActivity.class);
                                i.putExtra("EVENT_ID", event.getId());
                                i.putExtra("BUDGET_DEADLINE", event.getBudgetDeadline());
                                startActivity(i);
                            });
                        }

                        // Ustaw dane na ekranie
                        titleText.setText(event.getTitle());
                        dateText.setText(event.getDate());
                        descText.setText(event.getDescription());
                        locationText.setText(event.getLocation());

                        Glide.with(EventDetailActivity.this)
                                .load("http://10.0.2.2:8080" + event.getImageUrl())
                                .into(eventImage);

                        // POBIERZ POLLE
                        fetchUpdatedPollOptions(true);
                        fetchUpdatedPollOptions(false);

                        setupVotingOptions();
                        setupVoteButtons();
                    }
                }

                @Override
                public void onFailure(Call<com.example.myapplication.model.Event> call, Throwable t) {
                    Toast.makeText(EventDetailActivity.this, "Błąd ładowania wydarzenia", Toast.LENGTH_SHORT).show();
                }
            });
        }

    }

    private boolean isVotingActive(String endDateString) {
        if (endDateString == null || endDateString.isEmpty()) {
            return true;
        }
        try {
            java.util.Date endDate;
            if (endDateString.length() == 10) { // yyyy-MM-dd
                java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault());
                endDate = sdf.parse(endDateString);
            } else { // yyyy-MM-dd HH:mm
                java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm", java.util.Locale.getDefault());
                endDate = sdf.parse(endDateString);
            }

            java.util.Date now = new java.util.Date();
            return now.before(endDate);
        } catch (Exception e) {
            return true; // Jeśli błąd – domyślnie aktywne
        }
    }



    private void setupVotingOptions() {

        if (dateVotingEnd != null && !dateVotingEnd.isEmpty()) {
            dateVotingEndInfo.setVisibility(View.VISIBLE);
            if (isVotingActive(dateVotingEnd)) {
                dateVotingEndInfo.setText("Głosowanie na datę trwa do: " + dateVotingEnd);
            } else {
                dateVotingEndInfo.setText("Głosowanie na datę zakończone.");
            }
        } else {
            dateVotingEndInfo.setVisibility(View.GONE);
        }

        if (locationVotingEnd != null && !locationVotingEnd.isEmpty()) {
            locationVotingEndInfo.setVisibility(View.VISIBLE);
            if (isVotingActive(locationVotingEnd)) {
                locationVotingEndInfo.setText("Głosowanie na lokalizację trwa do: " + locationVotingEnd);
            } else {
                locationVotingEndInfo.setText("Głosowanie na lokalizację zakończone.");
            }
        } else {
            locationVotingEndInfo.setVisibility(View.GONE);
        }

        if (datePollOptions != null && !datePollOptions.isEmpty() && isVotingActive(dateVotingEnd)) {
            dateVotingOptionsContainer.setVisibility(View.VISIBLE);
            btnVoteDate.setVisibility(View.VISIBLE);
            dateVotingRadioGroup.removeAllViews();
            for (PollOption option : datePollOptions) {
                RadioButton rb = new RadioButton(this);
                rb.setText(option.getValue() + " (" + option.getVoteCount() + " głosów)");
                Log.d("TAG", "pollId = " + option.getPollId() + ", optionId = " + option.getId());
                rb.setTag(new Pair<>(option.getPollId(), option.getId()));
                rb.setTextColor(Color.WHITE);
                dateVotingRadioGroup.addView(rb);
            }

        } else {
            dateVotingOptionsContainer.setVisibility(View.GONE);
            btnVoteDate.setVisibility(View.GONE);
        }
        if (locationPollOptions != null && !locationPollOptions.isEmpty() && isVotingActive(locationVotingEnd)) {
            locationVotingOptionsContainer.setVisibility(View.VISIBLE);
            btnVoteLocation.setVisibility(View.VISIBLE);
            locationVotingRadioGroup.removeAllViews();
            for (PollOption option : locationPollOptions) {
                RadioButton rb = new RadioButton(this);
                rb.setText(option.getValue() + " (" + option.getVoteCount() + " głosów)");
                rb.setTag(new Pair<>(option.getPollId(), option.getId()));
                rb.setTextColor(Color.WHITE);
                locationVotingRadioGroup.addView(rb);
            }

        } else {
            locationVotingOptionsContainer.setVisibility(View.GONE);
            btnVoteLocation.setVisibility(View.GONE);
        }
    }



    private void setupVoteButtons() {
        btnVoteDate.setOnClickListener(v -> {
            int selectedId = dateVotingRadioGroup.getCheckedRadioButtonId();
            if (selectedId == -1) {
                Toast.makeText(this, "Wybierz opcję do głosowania na datę", Toast.LENGTH_SHORT).show();
                return;
            }
            RadioButton selectedRadio = findViewById(selectedId);
            Pair<Long, Long> ids = (Pair<Long, Long>) selectedRadio.getTag();
            sendVote(ids.first, ids.second, true);
        });

        btnVoteLocation.setOnClickListener(v -> {
            int selectedId = locationVotingRadioGroup.getCheckedRadioButtonId();
            if (selectedId == -1) {
                Toast.makeText(this, "Wybierz opcję do głosowania na lokalizację", Toast.LENGTH_SHORT).show();
                return;
            }
            RadioButton selectedRadio = findViewById(selectedId);
            Pair<Long, Long> ids = (Pair<Long, Long>) selectedRadio.getTag();
            sendVote(ids.first, ids.second, false);
        });
    }

    private void sendVote(Long pollId, Long optionId, boolean isDateVote) {
        apiService.vote(pollId, optionId).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(EventDetailActivity.this, "Głos oddany!", Toast.LENGTH_SHORT).show();
                    fetchUpdatedPollOptions(isDateVote); // lub false, jeśli lokalizacja – zależnie od kontekstu
                } else {
                    Toast.makeText(EventDetailActivity.this, "Błąd oddawania głosu", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Toast.makeText(EventDetailActivity.this, "Błąd sieci: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
    private void fetchUpdatedPollOptions(boolean isDateVote) {
        Long eventId = getIntent().getLongExtra("eventId", -1);
        if (eventId == -1) {
            Toast.makeText(this, "Błąd: brak ID wydarzenia", Toast.LENGTH_SHORT).show();
            return;
        }

        Call<List<PollOption>> call;

        if (isDateVote) {
            call = apiService.getDatePollOptions(eventId);
        } else {
            call = apiService.getLocationPollOptions(eventId);
        }

        call.enqueue(new Callback<List<PollOption>>() {
            @Override
            public void onResponse(Call<List<PollOption>> call, Response<List<PollOption>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<PollOption> updatedOptions = response.body();

                    Log.d("PollOptions", "Odebrano " + updatedOptions.size() + " opcji");


                    if (isDateVote) {
                        datePollOptions = updatedOptions;
                    } else {
                        locationPollOptions = updatedOptions;
                    }

                    runOnUiThread(() -> setupVotingOptions()); // odśwież UI z nowymi danymi
                } else {
                    Toast.makeText(EventDetailActivity.this, "Błąd pobierania wyników", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<PollOption>> call, Throwable t) {
                Toast.makeText(EventDetailActivity.this, "Błąd sieci: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }


    private void showSettlementDialog() {
        Long eventId = getIntent().getLongExtra("eventId", -1);
        if (eventId == -1) {
            Toast.makeText(this, "Brak ID wydarzenia", Toast.LENGTH_SHORT).show();
            return;
        }
        apiService.getSettlement(eventId).enqueue(new Callback<Map<String, Double>>() {
            @Override
            public void onResponse(Call<Map<String, Double>> call, Response<Map<String, Double>> response) {
                if (response.isSuccessful()) {
                    StringBuilder sb = new StringBuilder();
                    for (Map.Entry<String, Double> entry : response.body().entrySet()) {
                        double balance = entry.getValue();
                        if (balance < 0) sb.append(entry.getKey()).append(" ➜ do zapłaty: ").append(-balance).append(" zł\n");
                        else if (balance > 0) sb.append(entry.getKey()).append(" ➜ do otrzymania: ").append(balance).append(" zł\n");
                    }
                    new AlertDialog.Builder(EventDetailActivity.this)
                            .setTitle("Rozliczenie")
                            .setMessage(sb.toString())
                            .setPositiveButton("OK", null)
                            .show();
                }
            }

            @Override
            public void onFailure(Call<Map<String, Double>> call, Throwable t) {}
        });

    }


}