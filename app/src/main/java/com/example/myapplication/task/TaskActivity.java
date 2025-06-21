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
    private ArrayList<String> participants;
    private ApiService api;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_task);

        eventId      = getIntent().getLongExtra("EVENT_ID", -1);
        participants = getIntent().getStringArrayListExtra("PARTICIPANTS");
        if (participants == null) participants = new ArrayList<>();

        api = RetrofitClient.getInstance(this).create(ApiService.class);

        rvTasks = findViewById(R.id.rvTasks);
        rvTasks.setLayoutManager(new LinearLayoutManager(this));

        adapter = new TaskAdapter();
        rvTasks.setAdapter(adapter);

        btnAddTask = findViewById(R.id.btnAddTask);
        btnAddTask.setOnClickListener(v -> openAddTaskSheet());

        loadTasks();
    }
    private void loadTasks() {
        adapter.setData(new ArrayList<>());
    }

    private void openAddTaskSheet() {
        BottomSheetDialog d = new BottomSheetDialog(this);
        View v = getLayoutInflater().inflate(R.layout.bottom_add_task, null);

        EditText            etTitle    = v.findViewById(R.id.etTaskTitle);
        AutoCompleteTextView acAssignee = v.findViewById(R.id.acAssignee);
        Button              btnSave    = v.findViewById(R.id.btnSaveTask);

        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(
                this,
                R.layout.dropdown_item_white,
                participants
        );
        acAssignee.setAdapter(spinnerAdapter);
        acAssignee.setThreshold(1);

        acAssignee.setOnClickListener(v1 -> acAssignee.showDropDown());
        acAssignee.setOnFocusChangeListener((v1, hasFocus) -> {
            if (hasFocus) acAssignee.showDropDown();
        });

        btnSave.setOnClickListener(b -> {
            String title = etTitle.getText().toString().trim();
            String who   = acAssignee.getText().toString().trim();

            if (title.isEmpty() || who.isEmpty()) {
                Toast.makeText(this,
                        "Uzupełnij tytuł i wybierz osobę",
                        Toast.LENGTH_SHORT).show();
                return;
            }

            TaskDto t = new TaskDto();
            t.setTitle(title);
            t.setAssigneeFullName(who);
            t.setDone(false);
            adapter.add(t);
            d.dismiss();

        });

        d.setContentView(v);
        d.show();
    }
}
