package com.example.myapplication.event;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.R;
import com.example.myapplication.data.ExpenseDto;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Adapter listy wydatków (opis + kwota + płatnik).
 */
public class ExpenseAdapter extends RecyclerView.Adapter<ExpenseAdapter.VH> {

    private final List<ExpenseDto> items = new ArrayList<>();

    /*────────────────────────  API publiczne  ──────────────────────────────*/

    /** Nadpisz całą listę wydatków (np. po pobraniu z backendu). */
    public void setData(List<ExpenseDto> list) {
        items.clear();
        items.addAll(list);
        notifyDataSetChanged();
    }

    /** Dodaj pojedynczy wydatek (np. po udanym POST /expenses). */
    public void add(ExpenseDto e) {
        items.add(e);
        notifyItemInserted(items.size() - 1);
    }

    /*────────────────────  Metody RecyclerView.Adapter  ────────────────────*/

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_expense, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int position) {
        ExpenseDto e = items.get(position);
        h.desc.setText(e.getDescription());
        h.amount.setText(String.format(Locale.getDefault(), "%.2f zł", e.getAmount()));
        h.payer.setText(e.getPayerFullName());                 // NEW – imię i nazwisko
    }

    @Override
    public int getItemCount() { return items.size(); }

    /*──────────────────────────  ViewHolder  ───────────────────────────────*/

    static class VH extends RecyclerView.ViewHolder {
        TextView desc, amount, payer;

        VH(@NonNull View itemView) {
            super(itemView);
            desc   = itemView.findViewById(R.id.tvExpenseDesc);
            amount = itemView.findViewById(R.id.tvExpenseAmount);
            payer  = itemView.findViewById(R.id.tvExpensePayer);   // NEW
        }
    }
}
