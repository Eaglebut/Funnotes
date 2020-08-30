package ru.eaglebutt.funnotes.adapters;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.navigation.NavController;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import ru.eaglebutt.funnotes.R;
import ru.eaglebutt.funnotes.model.Event;
import ru.eaglebutt.funnotes.repositories.EventRepository;

public class EventsAdapter extends RecyclerView.Adapter<EventViewHolder> {

    private static final String TAG = "TestAdapter";
    private List<Event> mDataSet;
    private EventRepository repository;
    private NavController controller;

    public EventsAdapter(List<Event> mDataSet, NavController controller) {
        this.mDataSet = new ArrayList<>(mDataSet);
        this.controller = controller;
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
        repository = EventRepository.getInstance(view.getContext());
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
        holder.setTime(event.getStartTimeString());
        holder.setDescription(event.getDescription());
        holder.setEndTime(event.getEndTimeString());
        holder.itemView.setOnLongClickListener(v -> {
            repository.deleteEvent(event.getLocalId());
            return false;
        });

        holder.itemView.findViewById(R.id.edit_button_event).setOnClickListener(v -> {
            Bundle bundle = new Bundle();
            bundle.putInt("id", position);
            controller.navigate(R.id.action_to_edit_fragment, bundle);
        });

    }

    @Override
    public int getItemCount() {
        if (mDataSet == null)
            return 0;
        return mDataSet.size();
    }


}
