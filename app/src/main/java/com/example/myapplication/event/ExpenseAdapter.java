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


public class ExpenseAdapter extends RecyclerView.Adapter<ExpenseAdapter.VH> {

    private final List<ExpenseDto> items = new ArrayList<>();


    public void setData(List<ExpenseDto> list) {
        items.clear();
        items.addAll(list);
        notifyDataSetChanged();
    }

    public void add(ExpenseDto e) {
        items.add(e);
        notifyItemInserted(items.size() - 1);
    }


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
        h.payer.setText(e.getPayerFullName());
    }

    @Override
    public int getItemCount() { return items.size(); }


    static class VH extends RecyclerView.ViewHolder {
        TextView desc, amount, payer;

        VH(@NonNull View itemView) {
            super(itemView);
            desc   = itemView.findViewById(R.id.tvExpenseDesc);
            amount = itemView.findViewById(R.id.tvExpenseAmount);
            payer  = itemView.findViewById(R.id.tvExpensePayer);
        }
    }
}
