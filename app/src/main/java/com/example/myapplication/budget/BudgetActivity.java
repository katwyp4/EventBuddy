package com.example.myapplication.budget;

import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.R;
import com.example.myapplication.data.CreateExpenseDto;
import com.example.myapplication.data.ExpenseDto;
import com.example.myapplication.event.ExpenseAdapter;
import com.google.android.material.bottomsheet.BottomSheetDialog;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class BudgetActivity extends AppCompatActivity {

    /*────────────────────────────  POLA  ────────────────────────────*/
    private ExpenseAdapter adapter;

    private long eventId       = -1;
    private long currentUserId = 1;          // TODO: pobierz z auth

    //private LocalDate deadline = LocalDate.now().minusDays(1); // TODO: z backendu
    private LocalDate deadline = LocalDate.now().plusDays(1);

    //private LocalDate deadline = LocalDate.now().plusDays(7);

    private View btnAddExpense;
    private View btnShowSettlement;

    /*────────────────────────────  LIFE-CYCLE  ──────────────────────*/
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_budget);

        /* --- odbiór danych z Intentu --- */
        eventId = getIntent().getLongExtra("EVENT_ID", -1);

        /* --- RecyclerView & adapter --- */
        RecyclerView rv = findViewById(R.id.rvExpenses);
        rv.setLayoutManager(new LinearLayoutManager(this));
        adapter = new ExpenseAdapter();
        rv.setAdapter(adapter);

        /* --- przyciski --- */
        btnAddExpense     = findViewById(R.id.btnAddExpense);
        btnShowSettlement = findViewById(R.id.btnShowSettlement);

        btnAddExpense.setOnClickListener(v -> openAddExpenseSheet());
        btnShowSettlement.setOnClickListener(v -> showSettlementDialog());

        /* --- mock listy wydatków --- */
        adapter.setData(new ArrayList<>());        // TODO: Retrofit -> api.getExpenses(eventId)

        toggleButtons();
    }

    /*────────────────────  UI: przyciski vs deadline  ───────────────*/
    private void toggleButtons() {
        boolean afterDeadline = LocalDate.now().isAfter(deadline);
        btnAddExpense.setVisibility(afterDeadline ? View.GONE  : View.VISIBLE);
        btnShowSettlement.setVisibility(afterDeadline ? View.VISIBLE : View.GONE);
    }

    /*────────────────────  Bottom-sheet: nowy wydatek  ──────────────*/
    private void openAddExpenseSheet() {
        BottomSheetDialog sheet = new BottomSheetDialog(this);
        View v = getLayoutInflater().inflate(R.layout.bottom_add_expense, null);

        EditText etDesc = v.findViewById(R.id.etExpenseDesc);
        EditText etAmt  = v.findViewById(R.id.etExpenseAmount);

        v.findViewById(R.id.btnSaveExpense).setOnClickListener(b -> {
            String desc   = etDesc.getText().toString().trim();
            String amtStr = etAmt.getText().toString().trim();
            if (desc.isEmpty() || amtStr.isEmpty()) {
                Toast.makeText(this,"Uzupełnij pola",Toast.LENGTH_SHORT).show(); return;
            }

            double amt = Double.parseDouble(amtStr);

            CreateExpenseDto dto = new CreateExpenseDto(desc, amt, currentUserId, eventId);
            // TODO: Retrofit -> api.addExpense(dto)
            // onResponse: adapter.add(response.body());

            /* --- MOCK lokalny --- */
            ExpenseDto mock = new ExpenseDto();
            mock.setDescription(desc);
            mock.setAmount(amt);
            mock.setPayerFullName("Ja");
            adapter.add(mock);

            sheet.dismiss();
        });

        sheet.setContentView(v);
        sheet.show();
    }

    /*────────────────────  Dialog rozliczenia  ─────────────────────*/
    private void showSettlementDialog() {
        // TODO: Retrofit -> api.getSettlement(eventId)
        String msg = "Jan ➜ Piotr : 25 zł\nPiotr ➜ Ola : 10 zł";

        new AlertDialog.Builder(this)
                .setTitle("Rozliczenie")
                .setMessage(msg)
                .setPositiveButton("OK", null)
                .show();
    }
}
