package com.example.myapplication;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.*;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.myapplication.model.Event;
import com.example.myapplication.model.Poll;
import com.example.myapplication.model.PollOption;
import com.example.myapplication.network.ApiService;
import com.example.myapplication.network.RetrofitClient;
import com.example.myapplication.util.FileUtils;
import com.google.gson.Gson;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AddEventActivity extends AppCompatActivity {

    private static final int PICK_IMAGE_REQUEST = 1;

    private EditText editTitle, editDescription, editDate, editLocation;
    private ImageView imagePreview;
    private Button btnSave, btnSelectImage, btnAddDatePollOption, btnAddLocationPollOption;
    private EditText editBudgetDeadline, editDateVotingEnd, editNewDatePollOption, editNewLocationPollOption, editLocationVotingEnd;
    private CheckBox checkboxDateVoting, checkboxLocationVoting;
    private LinearLayout datePollOptionsContainer, locationPollOptionsContainer, datePollDynamicOptions, locationPollDynamicOptions, dateVotingEndContainer, locationVotingEndContainer;
    private List<PollOption> datePollOptionsList = new ArrayList<>();
    private List<PollOption> locationPollOptionsList = new ArrayList<>();
    private List<Poll> pollsList = new ArrayList<>();
    private Uri selectedImageUri;
    private ApiService apiService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (checkSelfPermission(Manifest.permission.READ_MEDIA_IMAGES) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.READ_MEDIA_IMAGES}, 100);
            }
        }

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_event);

        editTitle = findViewById(R.id.editTitle);
        editDescription = findViewById(R.id.editDescription);
        editDate = findViewById(R.id.editDate);
        editLocation = findViewById(R.id.editLocation);

        imagePreview = findViewById(R.id.imagePreview);
        btnSave = findViewById(R.id.btnSave);
        btnSelectImage = findViewById(R.id.selectImageButton);

        checkboxDateVoting = findViewById(R.id.checkboxDateVoting);
        checkboxLocationVoting = findViewById(R.id.checkboxLocationVoting);

        datePollOptionsContainer = findViewById(R.id.datePollOptionsContainer);
        locationPollOptionsContainer = findViewById(R.id.locationPollOptionsContainer);

        datePollDynamicOptions = findViewById(R.id.datePollDynamicOptions);
        locationPollDynamicOptions = findViewById(R.id.locationPollDynamicOptions);

        editNewDatePollOption = findViewById(R.id.editNewDatePollOption);
        btnAddDatePollOption = findViewById(R.id.btnAddDatePollOption);

        editNewLocationPollOption = findViewById(R.id.editNewLocationPollOption);
        btnAddLocationPollOption = findViewById(R.id.btnAddLocationPollOption);

        editBudgetDeadline = findViewById(R.id.editBudgetDeadline);

        dateVotingEndContainer = findViewById(R.id.dateVotingEndContainer);
        editDateVotingEnd = findViewById(R.id.editDateVotingEnd);

        locationVotingEndContainer = findViewById(R.id.locationVotingEndContainer);
        editLocationVotingEnd = findViewById(R.id.editLocationVotingEnd);


        checkboxDateVoting.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                editDate.setVisibility(View.GONE);
                datePollOptionsContainer.setVisibility(View.VISIBLE);
                dateVotingEndContainer.setVisibility(View.VISIBLE);
                datePollOptionsList.clear();
                datePollDynamicOptions.removeAllViews();
            } else {
                editDate.setVisibility(View.VISIBLE);
                datePollOptionsContainer.setVisibility(View.GONE);
                dateVotingEndContainer.setVisibility(View.GONE);
            }
        });

        checkboxLocationVoting.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                editLocation.setVisibility(View.GONE);
                locationPollOptionsContainer.setVisibility(View.VISIBLE);
                locationVotingEndContainer.setVisibility(View.VISIBLE);
                locationPollOptionsList.clear();
                locationPollDynamicOptions.removeAllViews();
            } else {
                editLocation.setVisibility(View.VISIBLE);
                locationPollOptionsContainer.setVisibility(View.GONE);
                locationVotingEndContainer.setVisibility(View.GONE);
            }
        });

        btnAddDatePollOption.setOnClickListener(v -> {
            String option = editNewDatePollOption.getText().toString().trim();
            if (!option.isEmpty()) {
                PollOption pollOption = new PollOption(option);
                datePollOptionsList.add(pollOption);
                addPollOptionView(datePollDynamicOptions, option);
                editNewDatePollOption.setText("");
            }
        });

        btnAddLocationPollOption.setOnClickListener(v -> {
            String option = editNewLocationPollOption.getText().toString().trim();
            if (!option.isEmpty()) {
                PollOption pollOption = new PollOption(option);
                locationPollOptionsList.add(pollOption);
                addPollOptionView(locationPollDynamicOptions, option);
                editNewLocationPollOption.setText("");
            }
        });

        btnSelectImage.setOnClickListener(v -> openImageChooser());

        btnSave.setOnClickListener(v -> createEvent());

        apiService = RetrofitClient.getInstance(getApplicationContext()).create(ApiService.class);
    }

    private void addPollOptionView(LinearLayout container, String option) {
        TextView optionView = new TextView(this);
        optionView.setText(option);
        optionView.setTextColor(Color.WHITE);
        optionView.setPadding(8, 8, 8, 8);
        container.addView(optionView);
    }

    private void openImageChooser() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        startActivityForResult(Intent.createChooser(intent, "Wybierz obraz"), PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            selectedImageUri = data.getData();
            imagePreview.setImageURI(selectedImageUri);
        }
    }

    private void createEvent() {
        String title = editTitle.getText().toString().trim();
        String description = editDescription.getText().toString().trim();
        String date = null;
        String location = null;

        Boolean dateVoting = checkboxDateVoting.isChecked();
        Boolean locationVoting = checkboxLocationVoting.isChecked();

        if (!dateVoting) {
            date = editDate.getText().toString().trim();
            if (date.isEmpty()) {
                Toast.makeText(this, "Wprowadź datę wydarzenia", Toast.LENGTH_SHORT).show();
                return;
            }
        }

        if (!locationVoting) {
            location = editLocation.getText().toString().trim();
            if (location.isEmpty()) {
                Toast.makeText(this, "Wprowadź lokalizację wydarzenia", Toast.LENGTH_SHORT).show();
                return;
            }
        }

        if (title.isEmpty() || description.isEmpty() || selectedImageUri == null) {
            Toast.makeText(this, "Wypełnij wszystkie pola i wybierz zdjęcie", Toast.LENGTH_SHORT).show();
            return;
        }

        if (dateVoting && datePollOptionsList.isEmpty()) {
            Toast.makeText(this, "Dodaj propozycje do głosowania nad datą", Toast.LENGTH_SHORT).show();
            return;
        }
        if (locationVoting && locationPollOptionsList.isEmpty()) {
            Toast.makeText(this, "Dodaj propozycje do głosowania nad lokalizacją", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            Event event = new Event();
            event.setTitle(title);
            event.setDescription(description);
            event.setDate(date);
            event.setLocation(location);
            event.setEventPrivacy("PUBLIC_CLOSED");
            event.setEnableDateVoting(dateVoting);
            event.setEnableLocationVoting(locationVoting);

            // 1. odczytaj termin z pola
            String budgetDeadline = editBudgetDeadline.getText().toString().trim();
            // 2. ustaw w obiekcie Event
            event.setBudgetDeadline(budgetDeadline);


            Log.d("EVENT", "enableDateVoting = " + event.getEnableDateVoting());


            pollsList.clear();

            if (dateVoting) {
                Poll datePoll = new Poll();
                datePoll.setQuestion("data");
                datePoll.setOptions(datePollOptionsList);
                event.setDatePoll(datePoll);
            }
            if (locationVoting) {
                Poll locationPoll = new Poll();
                locationPoll.setQuestion("lokalizacja");
                locationPoll.setOptions(locationPollOptionsList);
                event.setLocationPoll(locationPoll);
            }

//            if (!pollsList.isEmpty()) {
//                event.setPolls(pollsList);
//            }

            Gson gson = new Gson();
            String eventJson = gson.toJson(event);
            Log.d("EVENT_JSON", eventJson);
            RequestBody eventPart = RequestBody.create(eventJson, MediaType.parse("application/json"));

            File imageFile = new File(FileUtils.getPath(this, selectedImageUri));
            RequestBody imageBody = RequestBody.create(imageFile, MediaType.parse("image/*"));
            MultipartBody.Part imagePart = MultipartBody.Part.createFormData("image", imageFile.getName(), imageBody);

            apiService.createEventWithImage(imagePart, eventPart).enqueue(new Callback<Event>() {
                @Override
                public void onResponse(Call<Event> call, Response<Event> response) {
                    if (response.isSuccessful()) {
                        Toast.makeText(AddEventActivity.this, "Dodano wydarzenie!", Toast.LENGTH_SHORT).show();
                        Log.d("RESPONSE_OK", "Event added: " + response.body());
                        setResult(RESULT_OK);
                        finish();
                    } else {
                        Toast.makeText(AddEventActivity.this, "Błąd dodawania wydarzenia", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(Call<Event> call, Throwable t) {
                    Log.d("BLAD", t.getMessage());
                    Toast.makeText(AddEventActivity.this, "Błąd: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });

        } catch (Exception e) {
            Toast.makeText(this, "Błąd przy przetwarzaniu zdjęcia", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }
}
