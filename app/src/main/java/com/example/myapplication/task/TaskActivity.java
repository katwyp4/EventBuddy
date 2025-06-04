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
import com.example.myapplication.data.TaskDto;
import com.example.myapplication.network.ApiService;
import com.example.myapplication.network.RetrofitClient;
import com.google.android.material.bottomsheet.BottomSheetDialog;

import java.util.ArrayList;

public class TaskActivity extends AppCompatActivity {

    private RecyclerView rvTasks;
    private Button btnAddTask;
    private TaskAdapter adapter;
    private long eventId;
    private ArrayList<String> participants;   // lista imion i nazwisk
    private ApiService api;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_task);

        /* 1. Intent extras */
        eventId      = getIntent().getLongExtra("EVENT_ID", -1);
        participants = getIntent().getStringArrayListExtra("PARTICIPANTS");
        if (participants == null) participants = new ArrayList<>();

        /* 2. Retrofit */
        api = RetrofitClient.getInstance(this).create(ApiService.class);

        /* 3. RecyclerView + adapter */
        rvTasks = findViewById(R.id.rvTasks);
        rvTasks.setLayoutManager(new LinearLayoutManager(this));

        adapter = new TaskAdapter();                         // <-- NEW
        rvTasks.setAdapter(adapter);

        /* 4. Button „Dodaj zadanie” */
        btnAddTask = findViewById(R.id.btnAddTask);
        btnAddTask.setOnClickListener(v -> openAddTaskSheet());

        /* 5. Załaduj istniejące zadania */
        loadTasks();
    }

    /** Pobiera zadania z backendu */
    private void loadTasks() {
        adapter.setData(new ArrayList<>());
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

            /* MOCK lokalny – usuń gdy podłączysz backend */
            TaskDto t = new TaskDto();
            t.setTitle(title);
            t.setAssigneeFullName(who);
            t.setDone(false);
            adapter.add(t);
            d.dismiss();

            /*  // prawdziwe wywołanie:
            CreateTaskDto dto = new CreateTaskDto(title, who, eventId);
            api.addTask(dto).enqueue(new Callback<TaskDto>() { ... });
            */
        });

        d.setContentView(v);
        d.show();
    }
}
