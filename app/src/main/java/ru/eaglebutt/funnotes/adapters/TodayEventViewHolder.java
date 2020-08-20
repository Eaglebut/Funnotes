package ru.eaglebutt.funnotes.adapters;

import android.graphics.Paint;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.SimpleDateFormat;

import ru.eaglebutt.funnotes.R;

public class TodayEventViewHolder extends RecyclerView.ViewHolder {

    int paintFlags;
    private TextView titleView;
    private TextView timeView;
    private int visibility = View.GONE;
    private ImageButton editButton;
    private TextView endTimeView;
    private TextView descriptionView;
    private CheckBox isDoneCheckBox;


    public TodayEventViewHolder(@NonNull View itemView) {
        super(itemView);
        titleView = itemView.findViewById(R.id.title_my_day);
        timeView = itemView.findViewById(R.id.time_my_day);
        editButton = itemView.findViewById(R.id.edit_button_my_day);
        endTimeView = itemView.findViewById(R.id.end_time_my_day);
        descriptionView = itemView.findViewById(R.id.description_my_day);
        isDoneCheckBox = itemView.findViewById(R.id.is_done_my_day);
        paintFlags = titleView.getPaintFlags();
        isDoneCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    titleView.setPaintFlags(titleView.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
                } else {
                    titleView.setPaintFlags(paintFlags);
                }
            }
        });
        setVisibility(visibility);
        itemView.setOnClickListener(v -> {
            if (getVisibility() == View.GONE) {
                setVisibility(View.VISIBLE);
            } else {
                setVisibility(View.GONE);
            }
        });
    }


    public void setTitle(String title) {
        titleView.setText(title);
    }

    public void setTime(long time) {
        Timestamp timestamp = new Timestamp(time * 1_000);
        DateFormat dateFormat = new SimpleDateFormat("EE, dd MMM HH:mm");
        String strDate = dateFormat.format(timestamp);
        timeView.setText(strDate);
    }

    public void setDescription(String description) {
        descriptionView.setText(description);
    }

    public void setEndTime(long time) {
        Timestamp timestamp = new Timestamp(time * 1_000);
        DateFormat dateFormat = new SimpleDateFormat("EE, dd MMM HH:mm");
        String strDate = dateFormat.format(timestamp);
        endTimeView.setText(strDate);
    }

    public int getVisibility() {
        return visibility;
    }

    public void setVisibility(int visibility) {
        this.visibility = visibility;
        endTimeView.setVisibility(visibility);
        descriptionView.setVisibility(visibility);
        editButton.setVisibility(visibility);
    }

}
