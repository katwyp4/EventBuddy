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

public class ChatAdapter extends RecyclerView.Adapter<ChatAdapter.VH> {

    private final List<MessageDto> items = new ArrayList<>();

    public ChatAdapter(List<MessageDto> initial) {
        items.addAll(initial);
    }

    /** Podmień całą listę wiadomości. */
    public void setData(List<MessageDto> list) {
        items.clear();
        items.addAll(list);
        notifyDataSetChanged();
    }

    /** Dodaj pojedynczą wiadomość. */
    public void add(MessageDto msg) {
        items.add(msg);
        notifyItemInserted(items.size() - 1);
    }

    @NonNull @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_message, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int position) {
        MessageDto m = items.get(position);
        h.sender.setText(m.getSenderFullName());
        h.content.setText(m.getContent());

        // Przytnij sentAt: "2025-05-28T22:58:25.151967" → "2025-05-28 22:58"
        String sentAt = m.getSentAt();
        if (sentAt != null && sentAt.length() >= 16) {
            String date = sentAt.substring(0, 10);
            String time = sentAt.substring(11, 16);
            h.time.setText(date + " " + time);
        } else {
            h.time.setText("");
        }
    }

    @Override public int getItemCount() {
        return items.size();
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
