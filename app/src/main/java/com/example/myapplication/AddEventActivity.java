package com.example.myapplication;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.myapplication.model.Event;
import com.example.myapplication.network.ApiService;
import com.example.myapplication.network.RetrofitClient;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AddEventActivity extends AppCompatActivity {

    private EditText editTitle, editDescription, editDate;
    private Button btnSave;

    private ApiService apiService;

    private Button btnAddImage;
    private String selectedImageUrl = "";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_event); // upewnij się, że masz ten plik

        // Inicjalizacja pól
        editTitle = findViewById(R.id.editTitle);
        editDescription = findViewById(R.id.editDescription);
        editDate = findViewById(R.id.editDate);
        btnSave = findViewById(R.id.btnSave);

        // Inicjalizacja Retrofit
        apiService = RetrofitClient.getInstance(getApplicationContext()).create(ApiService.class);


        // Obsługa kliknięcia "Zapisz"
        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                createEvent();
            }
        });

        btnAddImage = findViewById(R.id.btnAddImage);
        btnAddImage.setOnClickListener(v -> {
            // Przykład: ustawienie domyślnego URL lub otwieranie image pickera
            selectedImageUrl = "https://upload.wikimedia.org/wikipedia/commons/4/47/PNG_transparency_demonstration_1.png";
            Toast.makeText(this, "Ustawiono przykładowy obraz", Toast.LENGTH_SHORT).show();
        });

    }

    private void createEvent() {
        String title = editTitle.getText().toString();
        String description = editDescription.getText().toString();
        String date = editDate.getText().toString();

        if (title.isEmpty() || description.isEmpty() || date.isEmpty()) {
            Toast.makeText(this, "Wypełnij wszystkie pola", Toast.LENGTH_SHORT).show();
            return;
        }

        Event event = new Event();
        event.setTitle(editTitle.getText().toString());
        event.setDescription(editDescription.getText().toString());
        event.setDate(editDate.getText().toString());
        event.setImageUrl("https://upload.wikimedia.org/...");
        event.setLocation("");
        event.setEventPrivacy("PUBLIC");
        event.setImageUrl(selectedImageUrl);


        apiService.createEvent(event).enqueue(new Callback<Event>() {
            @Override
            public void onResponse(Call<Event> call, Response<Event> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(AddEventActivity.this, "Dodano wydarzenie!", Toast.LENGTH_SHORT).show();
                    finish(); // wróć do poprzedniego ekranu
                } else {
                    Toast.makeText(AddEventActivity.this, "Błąd dodawania wydarzenia", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Event> call, Throwable t) {
                Toast.makeText(AddEventActivity.this, "Błąd: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

    }
}
