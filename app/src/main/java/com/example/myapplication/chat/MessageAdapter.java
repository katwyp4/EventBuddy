package com.example.myapplication.chat;

import android.view.*;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.myapplication.R;
import com.example.myapplication.data.MessageDto;
import java.util.*;

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.VH> {

    public final List<MessageDto> items = new ArrayList<>();

    @NonNull @Override public VH onCreateViewHolder(@NonNull ViewGroup p, int v) {
        View view = LayoutInflater.from(p.getContext())
                .inflate(R.layout.item_message, p, false);
        return new VH(view);
    }
    @Override public void onBindViewHolder(@NonNull VH h, int pos) {
        MessageDto m = items.get(pos);
        h.sender.setText(m.getSenderFullName());
        h.content.setText(m.getContent());
        h.time.setText(m.getSentAt().substring(11,16)); // HH:mm
    }
    @Override public int getItemCount() { return items.size(); }

    public void addAll(List<MessageDto> list) {
        int start = items.size();
        items.addAll(list);
        notifyItemRangeInserted(start, list.size());
    }
    public void add(MessageDto m) {
        items.add(m);
        notifyItemInserted(items.size()-1);
    }

    static class VH extends RecyclerView.ViewHolder{
        TextView sender, content, time;
        VH(View v){ super(v);
            sender  = v.findViewById(R.id.tvSender);
            content = v.findViewById(R.id.tvContent);
            time    = v.findViewById(R.id.tvTime);
        }
    }
}
