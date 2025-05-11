package com.example.myapplication;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.OpenableColumns;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.myapplication.model.Event;
import com.example.myapplication.network.ApiService;
import com.example.myapplication.network.RetrofitClient;
import com.example.myapplication.util.FileUtils;
import com.google.gson.Gson;

import java.io.File;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Build;


public class AddEventActivity extends AppCompatActivity {

    private static final int PICK_IMAGE_REQUEST = 1;

    private EditText editTitle, editDescription, editDate, editLocation;
    private ImageView imagePreview;
    private Button btnSave, btnSelectImage;



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

        imagePreview = findViewById(R.id.imagePreview); // dodaj w layout
        btnSave = findViewById(R.id.btnSave);
        btnSelectImage = findViewById(R.id.selectImageButton); // dodaj w layout

        apiService = RetrofitClient.getInstance(getApplicationContext()).create(ApiService.class);

        btnSelectImage.setOnClickListener(v -> openImageChooser());

        btnSave.setOnClickListener(v -> createEvent());
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
        String date = editDate.getText().toString().trim();
        String location = editLocation.getText().toString().trim();

        if (location.isEmpty() ) {
            Toast.makeText(this, "Wypełnij lokalizację i datę wygaśnięcia", Toast.LENGTH_SHORT).show();
            return;
        }


        if (title.isEmpty() || description.isEmpty() || date.isEmpty() || selectedImageUri == null) {
            Toast.makeText(this, "Wypełnij wszystkie pola i wybierz zdjęcie", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            // przygotuj dane wydarzenia
            Event event = new Event();
            event.setTitle(title);
            event.setDescription(description);
            event.setDate(date);
            event.setLocation(location);
            event.setEventPrivacy("PUBLIC_CLOSED");

            // JSON do @Part("event")
            Gson gson = new Gson();
            String eventJson = gson.toJson(event);
            RequestBody eventPart = RequestBody.create(eventJson, MediaType.parse("application/json"));

            // Obrazek do @Part MultipartBody
            File imageFile = new File(FileUtils.getPath(this, selectedImageUri));
            RequestBody imageBody = RequestBody.create(imageFile, MediaType.parse("image/*"));
            MultipartBody.Part imagePart = MultipartBody.Part.createFormData("image", imageFile.getName(), imageBody);

            apiService.createEventWithImage(imagePart, eventPart).enqueue(new Callback<Event>() {
                @Override
                public void onResponse(Call<Event> call, Response<Event> response) {
                    if (response.isSuccessful()) {
                        Toast.makeText(AddEventActivity.this, "Dodano wydarzenie!", Toast.LENGTH_SHORT).show();
                        setResult(RESULT_OK);
                        finish();

                    } else {
                        Toast.makeText(AddEventActivity.this, "Błąd dodawania wydarzenia", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(Call<Event> call, Throwable t) {
                    Toast.makeText(AddEventActivity.this, "Błąd: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });

        } catch (Exception e) {
            Toast.makeText(this, "Błąd przy przetwarzaniu zdjęcia", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }
}
