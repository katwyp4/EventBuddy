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

        String fullName = m.getSenderFullName();
        holder.sender.setText(fullName != null ? fullName : "");
        String content  = m.getContent();
        holder.content.setText(content != null ? content : "");

        String sentAt = m.getSentAt();
        holder.time.setText(
        sentAt != null && sentAt.length() >= 16 ? sentAt.substring(11, 16) : "");

    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public void addAll(List<MessageDto> list) {
        int start = items.size();
        items.addAll(list);
        notifyItemRangeInserted(start, list.size());
    }

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
