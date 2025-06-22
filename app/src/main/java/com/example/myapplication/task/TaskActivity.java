package com.example.myapplication.task;

import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.R;
import com.example.myapplication.data.EventParticipantDto;
import com.example.myapplication.data.TaskDto;
import com.example.myapplication.data.UserDto;
import com.example.myapplication.model.PaginatedResponse;
import com.example.myapplication.model.Task;
import com.example.myapplication.network.ApiService;
import com.example.myapplication.network.RetrofitClient;
import com.google.android.material.bottomsheet.BottomSheetDialog;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class TaskActivity extends AppCompatActivity {

    private RecyclerView rvTasks;
    private Button btnAddTask;
    private TaskAdapter adapter;
    private long eventId;
    private ArrayList<String> participants;   // lista imion i nazwisk
    private Map<String, Long> participantsIds;
    private ApiService api;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_task);

        /* 1. Retrofit */
        api = RetrofitClient.getInstance(this).create(ApiService.class);

        /* 2. Intent extras */
        eventId      = getIntent().getLongExtra("EVENT_ID", -1);
        participants = getIntent().getStringArrayListExtra("PARTICIPANTS");
        if (participants == null) participants = new ArrayList<>();
        loadParticipants();

        /* 3. RecyclerView + adapter */
        rvTasks = findViewById(R.id.rvTasks);
        rvTasks.setLayoutManager(new LinearLayoutManager(this));

        adapter = new TaskAdapter(api, this);                         // <-- NEW
        rvTasks.setAdapter(adapter);

        /* 4. Button „Dodaj zadanie” */
        btnAddTask = findViewById(R.id.btnAddTask);
        btnAddTask.setOnClickListener(v -> openAddTaskSheet());

        /* 5. Załaduj istniejące zadania */
        loadTasks();
    }

    // Pobieranie uczestników
    private void loadParticipants(){
        api.getEventParticipants(eventId, 0, 50, "ACTIVE").enqueue(new Callback<PaginatedResponse<EventParticipantDto>>() {
            @Override
            public void onResponse(Call<PaginatedResponse<EventParticipantDto>> call, Response<PaginatedResponse<EventParticipantDto>> response) {
                if (response.isSuccessful() && response.body() != null){
                    Stream<UserDto> participantUserDtosStream = response.body().getContent().stream().map(EventParticipantDto::getUser);
                    participantsIds = participantUserDtosStream.collect(Collectors.toMap(
                            u -> u.getFirstName() + " " + u.getLastName() + " ("+u.getEmail()+")",
                            UserDto::getId
                    ));
                    participants = new ArrayList<>(participantsIds.keySet());
                }
                else{
                    Toast.makeText(TaskActivity.this, "Błąd ładowania uczestników! Musisz być aktywym członkiem wydarzenia.", Toast.LENGTH_LONG).show();
                    finish();
                }
            }

            @Override
            public void onFailure(Call<PaginatedResponse<EventParticipantDto>> call, Throwable t) {
                Toast.makeText(TaskActivity.this, "Błąd ładowania uczestników! Musisz być aktywym członkiem wydarzenia.", Toast.LENGTH_LONG).show();
                finish();
            }
        });
    }

    /** Pobiera zadania z backendu */
    private void loadTasks() {
        api.getEventsTasks(0, 40, eventId).enqueue(new Callback<PaginatedResponse<TaskDto>>() {
            @Override
            public void onResponse(Call<PaginatedResponse<TaskDto>> call, Response<PaginatedResponse<TaskDto>> response) {
                if (response.isSuccessful() && response.body() != null){
                    PaginatedResponse<TaskDto> page = response.body();
                    List<TaskDto> tasks = page.getContent();
                    adapter.setData(tasks);
                }
                else{
                    Toast.makeText(TaskActivity.this, "Błąd ładowania zadań! Musisz być aktywym członkiem wydarzenia.", Toast.LENGTH_LONG).show();
                    finish();
                }
            }

            @Override
            public void onFailure(Call<PaginatedResponse<TaskDto>> call, Throwable t) {
                Toast.makeText(TaskActivity.this, "Błąd ładowania zadań! Musisz być aktywym członkiem wydarzenia.", Toast.LENGTH_LONG).show();
                finish();
            }
        });
    }

    /** BottomSheet do dodawania zadania */
    private void openAddTaskSheet() {
        BottomSheetDialog d = new BottomSheetDialog(this);
        View v = getLayoutInflater().inflate(R.layout.bottom_add_task, null);

        EditText            etTitle    = v.findViewById(R.id.etTaskTitle);
        AutoCompleteTextView acAssignee = v.findViewById(R.id.acAssignee);
        Button              btnSave    = v.findViewById(R.id.btnSaveTask);

        /* adapter z białą czcionką -------------------------------------- */
        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(
                this,
                R.layout.dropdown_item_white,          // twój layout z białą czcionką
                participants
        );
        acAssignee.setAdapter(spinnerAdapter);
        acAssignee.setThreshold(1);          // filtruj po 1 znaku

        /* pokaż listę natychmiast po kliknięciu lub focusie -------------- */
        acAssignee.setOnClickListener(v1 -> acAssignee.showDropDown());
        acAssignee.setOnFocusChangeListener((v1, hasFocus) -> {
            if (hasFocus) acAssignee.showDropDown();
        });

        /* -- Zapisz zadanie -- */
        btnSave.setOnClickListener(b -> {
            String title = etTitle.getText().toString().trim();
            String who   = acAssignee.getText().toString().trim();

            if (title.isEmpty() || who.isEmpty()) {
                Toast.makeText(this,
                        "Uzupełnij tytuł i wybierz osobę",
                        Toast.LENGTH_SHORT).show();
                return;
            }

            /*  // prawdziwe wywołanie:
            CreateTaskDto dto = new CreateTaskDto(title, who, eventId);
            api.addTask(dto).enqueue(new Callback<TaskDto>() { ... });
            */
            Task task = new Task();
            task.setTitle(title);
            task.setDone(false);
            api.addTask(eventId, task, participantsIds.get(who)).enqueue(
                    new Callback<TaskDto>() {
                        @Override
                        public void onResponse(Call<TaskDto> call, Response<TaskDto> response) {
                            if (response.isSuccessful() && response.body() != null){
                                TaskDto t = response.body();
                                adapter.add(t);
                            }
                            else{
                                Toast.makeText(TaskActivity.this, "Nie udało się dodać zadania!", Toast.LENGTH_SHORT).show();
                            }
                        }

                        @Override
                        public void onFailure(Call<TaskDto> call, Throwable t) {
                            Toast.makeText(TaskActivity.this, "Nie udało się dodać zadania!", Toast.LENGTH_SHORT).show();
                        }
                    }
            );
            d.dismiss();
        });

        d.setContentView(v);
        d.show();
    }
}
