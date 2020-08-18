package ru.eaglebutt.funnotes.adapters;

import android.util.Log;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import ru.eaglebutt.funnotes.R;

public class TestViewHolder extends RecyclerView.ViewHolder {

    private static final String TAG = "TestViewHolder";
    private TextView textView;


    public TestViewHolder(@NonNull View itemView) {
        super(itemView);
        itemView.setOnClickListener(v ->
                Log.d(TAG, "Element " + getAdapterPosition() + " clicked."));
        textView = itemView.findViewById(R.id.text_view);
    }

    public TextView getTextView() {
        return textView;
    }
}
