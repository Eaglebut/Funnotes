package ru.eaglebutt.funnotes.adapters;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import ru.eaglebutt.funnotes.R;
import ru.eaglebutt.funnotes.model.Event;

public class EventsAdapter extends RecyclerView.Adapter<EventViewHolder> {

    private static final String TAG = "TestAdapter";
    private List<Event> mDataSet;

    public EventsAdapter(List<Event> mDataSet) {
        this.mDataSet = new ArrayList<>(mDataSet);
    }

    public synchronized void setData(List<Event> mDataSet) {
        this.mDataSet = mDataSet;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public EventViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.event_layout, parent, false);
        return new EventViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull EventViewHolder holder, int position) {
        Log.d(TAG, "Element " + position + " set.");
        if (mDataSet == null || mDataSet.isEmpty()) {
            return;
        }
        Event event = mDataSet.get(position);
        holder.setTitle(event.getTitle());
        holder.setTime(event.getStartTime());
        holder.setDescription(event.getDescription());
        holder.setEndTime(event.getEndTime());
    }

    @Override
    public int getItemCount() {
        if (mDataSet == null)
            return 0;
        return mDataSet.size();
    }


}
