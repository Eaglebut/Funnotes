package ru.eaglebutt.funnotes.adapters;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import ru.eaglebutt.funnotes.R;
import ru.eaglebutt.funnotes.model.Event;

public class TestAdapter extends RecyclerView.Adapter<TestViewHolder> {

    private static final String TAG = "TestAdapter";
    private List<Event> mDataSet;

    public TestAdapter(List<Event> mDataSet) {
        this.mDataSet = new ArrayList<>(mDataSet);
    }

    public void setData(List<Event> mDataSet) {
        this.mDataSet = mDataSet;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public TestViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.text_row, parent, false);
        return new TestViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TestViewHolder holder, int position) {
        Log.d(TAG, "Element " + position + " set.");
        String text = mDataSet.get(position).toString();

        TextView view = holder.getTextView();
        view.setText(text);
    }

    @Override
    public int getItemCount() {
        if (mDataSet == null)
            return 0;
        return mDataSet.size();
    }


}
