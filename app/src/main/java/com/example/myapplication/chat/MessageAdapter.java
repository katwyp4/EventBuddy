package com.example.myapplication.chat;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.R;
import com.example.myapplication.data.MessageDto;

import java.util.ArrayList;
import java.util.List;

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.VH> {

    private final List<MessageDto> items = new ArrayList<>();

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_message, parent, false);
        return new VH(view);
    }

    @Override
    public void onBindViewHolder(@NonNull VH holder, int position) {
        MessageDto m = items.get(position);
        holder.sender.setText(m.getSenderFullName());
        holder.content.setText(m.getContent());
        // wyciągamy HH:mm z sentAt, np. "2025-05-28T14:22:00"
        holder.time.setText(m.getSentAt().substring(11, 16));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    /** Zastępuje całą listę (np. przy ładowaniu z backendu). */
    public void addAll(List<MessageDto> list) {
        int start = items.size();
        items.addAll(list);
        notifyItemRangeInserted(start, list.size());
    }

    /** Dokłada jedną wiadomość (po wysłaniu). */
    public void add(MessageDto m) {
        items.add(m);
        notifyItemInserted(items.size() - 1);
    }

    static class VH extends RecyclerView.ViewHolder {
        TextView sender, content, time;
        VH(View v) {
            super(v);
            sender  = v.findViewById(R.id.tvMessageSender);
            content = v.findViewById(R.id.tvMessageContent);
            time    = v.findViewById(R.id.tvMessageTime);
        }
    }
}
