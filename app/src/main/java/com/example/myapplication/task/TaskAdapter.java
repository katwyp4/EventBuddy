package com.example.myapplication.task;

import android.view.*;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.myapplication.R;
import com.example.myapplication.data.TaskDto;
import java.util.*;

public class TaskAdapter extends RecyclerView.Adapter<TaskAdapter.VH>{

    private final List<TaskDto> items = new ArrayList<>();

    public void setData(List<TaskDto> list){ items.clear(); items.addAll(list); notifyDataSetChanged(); }
    public void add(TaskDto t){ items.add(t); notifyItemInserted(items.size()-1); }

    @NonNull @Override public VH onCreateViewHolder(@NonNull ViewGroup p,int v){
        View view = LayoutInflater.from(p.getContext()).inflate(R.layout.item_task,p,false);
        return new VH(view);
    }
    @Override public void onBindViewHolder(@NonNull VH h,int pos){
        TaskDto t = items.get(pos);
        h.title.setText(t.getTitle());
        h.assignee.setText(t.getAssigneeFullName());
        h.done.setChecked(t.isDone());

        h.done.setOnCheckedChangeListener((cb,checked)-> t.setDone(checked));
    }
    @Override public int getItemCount(){ return items.size(); }

    static class VH extends RecyclerView.ViewHolder{
        TextView title,assignee; CheckBox done;
        VH(View v){ super(v);
            title    = v.findViewById(R.id.tvTaskTitle);
            assignee = v.findViewById(R.id.tvTaskAssignee);
            done     = v.findViewById(R.id.checkDone);
        }
    }
}
