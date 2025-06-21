package com.example.myapplication.budget;

import android.os.Bundle;
import android.util.Log;
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
import com.example.myapplication.network.ApiService;
import com.example.myapplication.network.RetrofitClient;
import com.google.android.material.bottomsheet.BottomSheetDialog;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class BudgetActivity extends AppCompatActivity {

    /*────────────────────────────  POLA  ────────────────────────────*/
    private ExpenseAdapter adapter;

    private long eventId = -1;

    private LocalDate deadline;
    private View btnAddExpense;
    private View btnShowSettlement;
    private ApiService apiService;

    /*────────────────────────────  LIFE-CYCLE  ──────────────────────*/
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_budget);

        apiService = RetrofitClient.getInstance(getApplicationContext()).create(ApiService.class);

        /* --- odbiór danych z Intentu --- */
        eventId = getIntent().getLongExtra("EVENT_ID", -1);

        // Fallback deadline z Intentu (na wszelki wypadek)
        String deadlineStr = getIntent().getStringExtra("BUDGET_DEADLINE");
        if (deadlineStr != null && !deadlineStr.isEmpty()) {
            try {
                deadline = LocalDate.parse(deadlineStr);
            } catch (Exception e) {
                deadline = LocalDate.MAX;
            }
        } else {
            deadline = LocalDate.MAX;
        }

        Log.d("DEBUG_BUDGET", "Fallback deadline z Intentu: " + deadline);

        /* --- RecyclerView & adapter --- */
        RecyclerView rv = findViewById(R.id.rvExpenses);
        rv.setLayoutManager(new LinearLayoutManager(this));
        adapter = new ExpenseAdapter();
        rv.setAdapter(adapter);

        /* --- przyciski --- */
        btnAddExpense = findViewById(R.id.btnAddExpense);
        btnShowSettlement = findViewById(R.id.btnShowSettlement);

        btnAddExpense.setOnClickListener(v -> openAddExpenseSheet());
        btnShowSettlement.setOnClickListener(v -> showSettlementDialog());

        // Pobierz aktualny deadline z serwera
        if (eventId != -1) {
            apiService.getBudgetDeadline(eventId).enqueue(new Callback<String>() {
                @Override
                public void onResponse(Call<String> call, Response<String> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        try {
                            deadline = LocalDate.parse(response.body());
                            Log.d("BUDGET", "Zaktualizowany deadline z serwera: " + deadline);
                            toggleButtons(); // odśwież po aktualnym deadline
                        } catch (Exception e) {
                            Log.e("BUDGET", "Błąd parsowania daty", e);
                        }
                    } else {
                        Log.w("BUDGET", "Nie udało się pobrać deadline'u z serwera");
                    }
                }

                @Override
                public void onFailure(Call<String> call, Throwable t) {
                    Log.e("BUDGET", "Błąd sieci przy pobieraniu deadline'u: " + t.getMessage());
                }
            });
        }

        // Pobierz wydatki
        apiService.getExpensesForEvent(eventId).enqueue(new Callback<List<ExpenseDto>>() {
            @Override
            public void onResponse(Call<List<ExpenseDto>> call, Response<List<ExpenseDto>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    adapter.setData(response.body());
                } else {
                    Toast.makeText(BudgetActivity.this, "Brak danych lub błąd serwera", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<ExpenseDto>> call, Throwable t) {
                Toast.makeText(BudgetActivity.this, "Błąd sieci: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

        // Wywołaj początkowo toggleButtons na wypadek, gdyby deadline z serwera się nie udał
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

            CreateExpenseDto dto = new CreateExpenseDto(desc, amt, eventId);

            apiService.addExpense(dto).enqueue(new Callback<ExpenseDto>() {
                @Override
                public void onResponse(Call<ExpenseDto> call, Response<ExpenseDto> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        adapter.add(response.body());  // dodaj do listy
                        sheet.dismiss();               // zamknij bottom sheet
                    } else {
                        Toast.makeText(BudgetActivity.this, "Błąd dodawania wydatku", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(Call<ExpenseDto> call, Throwable t) {
                    Toast.makeText(BudgetActivity.this, "Błąd sieci: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });

        });

        sheet.setContentView(v);
        sheet.show();
    }

    /*────────────────────  Dialog rozliczenia  ─────────────────────*/
    private void showSettlementDialog() {
        if (eventId == -1) {
            Toast.makeText(this, "Brak ID wydarzenia", Toast.LENGTH_SHORT).show();
            return;
        }

        apiService.getSettlement(eventId).enqueue(new Callback<Map<String, Double>>() {
            @Override
            public void onResponse(Call<Map<String, Double>> call, Response<Map<String, Double>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    StringBuilder sb = new StringBuilder();
                    for (Map.Entry<String, Double> entry : response.body().entrySet()) {
                        double balance = entry.getValue();
                        if (balance < 0)
                            sb.append(entry.getKey()).append(" ➜ do zapłaty: ").append(String.format("%.2f zł", -balance)).append("\n");
                        else if (balance > 0)
                            sb.append(entry.getKey()).append(" ➜ do otrzymania: ").append(String.format("%.2f zł", balance)).append("\n");
                    }

                    new AlertDialog.Builder(BudgetActivity.this)
                            .setTitle("Rozliczenie")
                            .setMessage(sb.toString().isEmpty() ? "Brak rozliczeń do wyświetlenia." : sb.toString())
                            .setPositiveButton("OK", null)
                            .show();
                } else {
                    Toast.makeText(BudgetActivity.this, "Błąd wczytywania rozliczenia", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Map<String, Double>> call, Throwable t) {
                Toast.makeText(BudgetActivity.this, "Błąd sieci: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

}
