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



import java.util.ArrayList;
import java.util.List;

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

    private boolean isParticipant = true;           // mock; backend/intent ustawi prawdę
    //private boolean isParticipant = false;



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

        // odczyt
        isParticipant = getIntent().getBooleanExtra("IS_PARTICIPANT", true); // wyswietlanie przycisku budzet czy user jest w evencie
        if (!isParticipant) {
            btnOpenBudget.setVisibility(View.GONE);
        } else {
            btnOpenBudget.setOnClickListener(v -> {
                Intent i = new Intent(this, BudgetActivity.class);
                i.putExtra("EVENT_ID", getIntent().getLongExtra("eventId", -1));
                startActivity(i);
            });
        }






        String title = getIntent().getStringExtra("title");
        String date = getIntent().getStringExtra("date");
        String desc = getIntent().getStringExtra("description");
        String imageUrl = getIntent().getStringExtra("imageUrl");
        String location = getIntent().getStringExtra("location");

        // Pobierz listy PollOption (musisz przekazać je jako Parcelable lub JSON i sparsować)
        Long eventId = getIntent().getLongExtra("eventId", -1);
        if (eventId != -1) {
            fetchUpdatedPollOptions(true);  // pobiera daty
            fetchUpdatedPollOptions(false); // pobiera lokalizacje
        }

        titleText.setText(title);
        dateText.setText(date);
        descText.setText(desc);
        locationText.setText(location);

        Glide.with(this)
                .load("http://10.0.2.2:8080" + imageUrl)
                .into(eventImage);












        btnOpenBudget.setOnClickListener(v -> {
            Intent i = new Intent(this, BudgetActivity.class);
            i.putExtra("EVENT_ID", getIntent().getLongExtra("eventId", -1));
            startActivity(i);
        });



        setupVotingOptions();
        setupVoteButtons();
    }

    private void setupVotingOptions() {
        // Data poll options
        if (datePollOptions != null && !datePollOptions.isEmpty()) {
            dateVotingOptionsContainer.setVisibility(View.VISIBLE);
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
        }

        // Location poll options
        if (locationPollOptions != null && !locationPollOptions.isEmpty()) {
            locationVotingOptionsContainer.setVisibility(View.VISIBLE);
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
        // Pobierz ID eventu z Intentu lub innego źródła
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
        // TODO: retrofit -> api.getSettlement(eventId)
        new AlertDialog.Builder(this)
                .setTitle("Rozliczenie")
                .setMessage("Jan ➜ Piotr : 25 zł\nPiotr ➜ Ola : 10 zł")
                .setPositiveButton("OK", null)
                .show();
    }


}
